package com.coasho.wschat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class R {
    private Boolean success;
    private String message;
    private int code;
    private HashMap<String, Object> data=new HashMap<>();

    public static R OK() {
        R result = new R(true, "success", 20001);
        return result;
    }

    public R(Boolean success, String message, int code) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public static R error(int code) {
        R result = new R(false, "error", code);
        return result;
    }

    public R message(String message) {
        this.message = message;
        return this;
    }
    public R data(String key,Object data){
        this.data.put(key, data);
        return this;

    }
}
