package com.coasho.wschat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 */
@MapperScan({"com.coasho.wschat.mapper"})
@SpringBootApplication
public class WSChatMainApp {
    public static void main(String[] args) {
        SpringApplication.run(WSChatMainApp.class);
    }
}
