package com.coasho.wschat.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.coasho.wschat.entity.User;
import com.coasho.wschat.entity.vo.UserVo;
import com.coasho.wschat.service.UserService;
import com.coasho.wschat.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/chat/{userId}")
public class ChatServer {
    private static int ONLINE_COUNT = 0;
    private static ConcurrentHashMap<String, ChatServer> CHAT_MAP = new ConcurrentHashMap<>();
    private String userId = "";
    private String viceUserId = "";
    private Session session;
    private static UserService userService;

    private static RedisTemplate<String, String> redisTemplate;

    @Autowired
    public void setUserService(UserService userService) {
        ChatServer.userService = userService;
    }

    @Autowired
    public void RedisTemplate(RedisTemplate<String, String> redisTemplate) {
        ChatServer.redisTemplate = redisTemplate;
    }

    @OnOpen
    public void onOpen(@PathParam("userId") String userId, Session session) {
        this.session = session;
        this.userId = userId;
        if (getChatMap().containsKey(userId)) {
            viceUserId = userId + RandomUtil.randomInt(100000, 999999);
            getChatMap().put(viceUserId, this);
        } else {
            addNumber();
        }

        sendInfo("用户上线", null, true, true);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info(message);
        if (!StringUtils.isEmpty(message)) {
            JSONObject jsonObject = JSONObject.parseObject(message);
            jsonObject.put("fromUserId", userId);
            String token = jsonObject.getString("token");
            String toUserId = jsonObject.getString("toUserId");
            String content = jsonObject.getString("content");
            boolean isDatePoint = jsonObject.getBoolean("isDatePoint");
            String fromUserId = JwtUtils.getMemberIdByJwtTokenString(token);
            if ((!StringUtils.isEmpty(fromUserId)) && getChatMap().containsKey(fromUserId)) {
                ChatServer server = getChatMap().get(fromUserId);
                //发送消息业务
                server.sendInfo(content, toUserId, isDatePoint, false);
            } else {
                sendMessage("token-fail", userId, isDatePoint, true, false);
            }

        }
    }

    @OnClose
    public void onClose() {
        removeNumber();
        sendInfo("用户下线", null, true, true);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:" + this.userId + ",原因:" + error.getMessage());
        error.printStackTrace();
    }

    private void sendInfo(String message, String toUserId, boolean isDatePoint, Boolean isSystem) {
        if (StringUtils.isEmpty(toUserId)) {
            Iterator<String> iterator = getChatMap().keySet().iterator();
            while (iterator.hasNext()) {
                String userId = iterator.next();
                ChatServer server = getChatMap().get(userId);
                server.sendMessage(message, this.userId, isDatePoint, true, isSystem);
            }
        } else {
            ChatServer server = getChatMap().get(toUserId);
            server.sendMessage(message, userId, isDatePoint, false, false);
            sendMessage(message, userId, isDatePoint, false, false);
        }
    }

    private List<UserVo> getOnlineUsers() {
        List<String> onlineUserIds = new ArrayList<>();
        Iterator<String> iterator = getChatMap().keySet().iterator();
        while (iterator.hasNext()) {
            String id = iterator.next();
            onlineUserIds.add(id);
        }
        Collection<User> users = userService.listByIds(onlineUserIds);
        ArrayList<UserVo> userVos = new ArrayList<>();
        Iterator<User> userIterator = users.iterator();
        while (userIterator.hasNext()) {
            User user = userIterator.next();
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            userVos.add(userVo);
        }
        return userVos;
    }

    private void sendMessage(String content, String fromUserId, boolean isDatePoint, Boolean isOpen, Boolean isSystem) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fromUserId", fromUserId);
        map.put("content", content);
        map.put("isSystem", isSystem);
        map.put("isOpen", isOpen);
        TimeZone cnTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatePattern.CHINESE_DATE_TIME_PATTERN);
        simpleDateFormat.setTimeZone(cnTimeZone);
        String date = simpleDateFormat.format(new Date());
        map.put("date", date);
        map.put("isDatePoint", isDatePoint);
        if (isSystem) {
            map.put("onlineCount", getOnlineCount());
            List<UserVo> onlineUsers = getOnlineUsers();
            map.put("onlineUserItems", onlineUsers);
        } else {
            UserVo userVo = new UserVo();
            if (isOpen) {
                BeanUtils.copyProperties(userService.getById(userId), userVo);
            } else {
                BeanUtils.copyProperties(userService.getById(fromUserId), userVo);
            }
            map.put("userInfo", userVo);
        }
        String systemMessage = JSONObject.toJSONString(map);
        synchronized (session) {
            try {
                session.getBasicRemote().sendText(systemMessage);
            } catch (IOException e) {
                log.info("服务器推送失败:" + e.getMessage());
            }
        }
        if (!isSystem) {
            if (!isOpen && userId != fromUserId) {
                redisTemplate.opsForHash().putIfAbsent(userId, fromUserId, IdUtil.fastSimpleUUID());
                Object chatListId = redisTemplate.opsForHash().get(userId, fromUserId);
                redisTemplate.opsForHash().putIfAbsent(fromUserId, userId, chatListId);
                Long size = redisTemplate.opsForList().size(chatListId.toString());
                if (size > 100) {
                    redisTemplate.opsForList().leftPop(chatListId.toString());
                }
                redisTemplate.opsForList().rightPush(chatListId.toString(), systemMessage);
            } else if (isOpen && userId == fromUserId) {
                Long size = redisTemplate.opsForList().size("openChatList");
                if (size > 300) {
                    redisTemplate.opsForList().leftPop("openChatList");
                }
                redisTemplate.opsForList().rightPush("openChatList", systemMessage);
            }
        }
    }

    private void addNumber() {
        getChatMap().put(userId, this);
        addOnlineCount();
    }

    private void removeNumber() {
        if (getChatMap().containsKey(viceUserId)) {
            getChatMap().remove(viceUserId);
            return;
        }
        if (getChatMap().containsKey(userId)) {
            getChatMap().remove(userId);
            subOnlineCount();
        }
    }

    private static synchronized void addOnlineCount() {
        ONLINE_COUNT++;
    }

    private static synchronized void subOnlineCount() {
        ONLINE_COUNT--;
    }

    private static synchronized int getOnlineCount() {
        return ONLINE_COUNT;
    }

    public String getUserId() {
        return this.userId;
    }

    private static synchronized ConcurrentHashMap<String, ChatServer> getChatMap() {
        return CHAT_MAP;
    }

}
