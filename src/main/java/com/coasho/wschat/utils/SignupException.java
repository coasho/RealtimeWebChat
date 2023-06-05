package com.coasho.wschat.utils;

import lombok.Data;

@Data
public class SignupException extends RuntimeException {
    private int code;
    private String message;
    public SignupException(int code, String message) {
        this.code=code;
        this.message=message;
    }

    @Override
    public String toString() {
        return "SignupException:{"+code+","+message+"}";
    }
}
