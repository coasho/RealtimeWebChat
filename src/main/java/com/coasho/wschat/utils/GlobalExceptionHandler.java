package com.coasho.wschat.utils;

import com.coasho.wschat.entity.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(LoginException.class)
    @ResponseBody
    public R loginError(LoginException e) {
        log.error(e.getMessage());
        e.printStackTrace();
        return R.error(e.getCode()).message(e.getMessage());
    }
    @ExceptionHandler(SignupException.class)
    @ResponseBody
    public R SignupError(SignupException e) {
        log.error(e.getMessage());
        e.printStackTrace();
        return R.error(e.getCode()).message(e.getMessage());
    }
}
