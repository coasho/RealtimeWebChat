package com.coasho.wschat.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.coasho.wschat.entity.User;
import com.coasho.wschat.entity.vo.UserVo;
import com.coasho.wschat.service.UserService;
import com.coasho.wschat.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
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

    @Autowired
    public void setUserService(UserService userService) {
        ChatServer.userService = userService;
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

        sendInfo("用户上线", null, true);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info(message);
        if (!StringUtils.isEmpty(message)) {
            JSONObject jsonObject = JSONObject.parseObject(message);
            jsonObject.put("fromUserId", userId);
            String token = jsonObject.getString("token");
            String fromUserId = JwtUtils.getMemberIdByJwtTokenString(token);
            String toUserId = jsonObject.getString("toUserId");
            String content = jsonObject.getString("content");
            if ((!StringUtils.isEmpty(fromUserId)) && getChatMap().containsKey(fromUserId)) {
                ChatServer server = getChatMap().get(fromUserId);
                //发送消息业务
                server.sendInfo(content, toUserId, false);
            } else {
                sendMessage("token-fail", userId,true, false);
            }

        }
    }

    @OnClose
    public void onClose() {
        removeNumber();
        sendInfo("用户下线", null, true);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:" + this.userId + ",原因:" + error.getMessage());
        error.printStackTrace();
    }

    private void sendInfo(String message, String toUserId, Boolean isSystem) {
        if (StringUtils.isEmpty(toUserId)) {
            Iterator<String> iterator = getChatMap().keySet().iterator();
            while (iterator.hasNext()) {
                String userId = iterator.next();
                ChatServer server = getChatMap().get(userId);
                server.sendMessage(message, this.userId,true, isSystem);
            }
        } else {
            ChatServer server = getChatMap().get(toUserId);
            server.sendMessage(message, userId, false,false);
            sendMessage(message, userId,false, false);
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

    private void sendMessage(String content, String fromUserId,Boolean isOpen, Boolean isSystem) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fromUserId", fromUserId);
        map.put("content", content);
        map.put("isSystem", isSystem);
        map.put("isOpen",isOpen);
        map.put("date", DateUtil.now());
        if (isSystem) {
            map.put("onlineCount", getOnlineCount());
            List<UserVo> onlineUsers = getOnlineUsers();
            map.put("onlineUserItems", onlineUsers);
        }
        String systemMessage = JSONObject.toJSONString(map);
        synchronized (session) {
            try {
                session.getBasicRemote().sendText(systemMessage);
            } catch (IOException e) {
                log.info("服务器推送失败:" + e.getMessage());
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
