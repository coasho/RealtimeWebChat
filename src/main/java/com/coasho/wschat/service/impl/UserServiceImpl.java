package com.coasho.wschat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coasho.wschat.entity.User;
import com.coasho.wschat.mapper.UserMapper;
import com.coasho.wschat.service.UserService;
import com.coasho.wschat.utils.OssUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author coasho
 * @since 2023-06-03
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private OssUtils ossUtils;

    @Override
    public String uploadAvatar(MultipartFile file) {
        OSS ossClient = ossUtils.getOssClient();
        String dir = "avatar/" + IdUtil.fastSimpleUUID() + "." + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
            ossClient.putObject(OssUtils.BUCKET_NAME, dir, inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ossClient.shutdown();
        return "https://" + OssUtils.BUCKET_NAME + "." + OssUtils.END_POINT + "/" + dir;
    }
}
