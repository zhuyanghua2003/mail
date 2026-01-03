package com.msb.mall.member.exception;


public class PhoneExsitException extends RuntimeException {

    public PhoneExsitException(){
        super("手机号已存在");
    }
}
