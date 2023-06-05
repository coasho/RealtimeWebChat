package com.coasho.wschat.utils;

import lombok.Data;

@Data
public class LoginException extends RuntimeException {
    private int code;
    private String message;
    public LoginException(int code, String message) {
        this.code=code;
        this.message=message;
    }

    @Override
    public String toString() {
        return "LoginException:{"+code+","+message+"}";
    }
}
