package com.coasho.wschat.service;

import com.coasho.wschat.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author coasho
 * @since 2023-06-03
 */
public interface UserService extends IService<User> {

    String uploadAvatar(MultipartFile file);
}
