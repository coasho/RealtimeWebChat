package com.coasho.wschat.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coasho.wschat.entity.R;
import com.coasho.wschat.entity.User;
import com.coasho.wschat.entity.vo.UserVo;
import com.coasho.wschat.service.UserService;
import com.coasho.wschat.utils.JwtUtils;
import com.coasho.wschat.utils.LoginException;
import com.coasho.wschat.utils.SignupException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public R Login(@RequestBody User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("name", user.getName());
        if (userService.getOne(wrapper) == null) {
            throw new LoginException(25001, "用户不存在");
        }
        wrapper.eq("password", user.getPassword());
        User queryUser = userService.getOne(wrapper);
        if (queryUser == null) {
            throw new LoginException(25001, "密码错误");
        }
        String token = JwtUtils.getJwtToken(queryUser.getId(), queryUser.getName());
        return R.OK().data("token", token);
    }

    @PostMapping("/signup")
    public R signup(@RequestBody User user) {
        if (StringUtils.isEmpty(user.getName())) {
            throw new SignupException(25001, "用户名不能为空");
        }
        if (user.getPassword().length() < 6) {
            throw new SignupException(25001, "密码长度不得低于6位");
        }
        if (userService.getOne(new QueryWrapper<User>().eq("name", user.getName())) != null) {
            throw new SignupException(25001, "用户名已存在");
        }
        boolean saved = userService.save(user);
        if (saved) {
            return R.OK();
        } else {
            return R.error(25001);
        }
    }

    @GetMapping("/info")
    public R getInfo(HttpServletRequest request) {
        String id = JwtUtils.getMemberIdByJwtToken(request);
        User user = userService.getOne(new QueryWrapper<User>().eq("id", id));
        if (user == null) {
            throw new LoginException(25001, "请重新登录");
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return R.OK().data("info", userVo);
    }

    @GetMapping("/infoById/{id}")
    public R getInfoById(@PathVariable("id") String id) {
        User user = userService.getOne(new QueryWrapper<User>().eq("id", id));
        if (user == null) {
            return R.OK().data("info", null);
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return R.OK().data("info", userVo);
    }

    @PostMapping("/upload/avatar")
    public R uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = userService.uploadAvatar(file);
        return R.OK().data("url", url);
    }

}
