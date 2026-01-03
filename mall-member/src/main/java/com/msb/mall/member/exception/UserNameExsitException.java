package com.msb.mall.member.exception;


public class UserNameExsitException extends RuntimeException {

    public UserNameExsitException(){
        super("账号已经存在");
    }
}
