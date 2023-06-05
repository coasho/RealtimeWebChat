package com.coasho.wschat.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OssUtils implements InitializingBean {
    @Value("${aliyun.oss.Endpoint}")
    private String Endpoint;
    @Value("${aliyun.oss.AccessKeyID}")
    private String AccessKeyID;
    @Value("${aliyun.oss.secret}")
    private String secret;
    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    public static String END_POINT;

    public static String ACCESS_KEY_ID;
    public static String SECRET;
    public static String BUCKET_NAME;

    @Override
    public void afterPropertiesSet() throws Exception {
        END_POINT = Endpoint;
        ACCESS_KEY_ID = AccessKeyID;
        SECRET = secret;
        BUCKET_NAME = bucketName;
    }

    public OSS getOssClient() {
        return new OSSClientBuilder().build(END_POINT, ACCESS_KEY_ID, SECRET);
    }
}
