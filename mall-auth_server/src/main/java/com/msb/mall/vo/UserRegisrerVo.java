package com.msb.mall.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegisrerVo {
    @NotEmpty(message = "账号不能为空")
    @Length(min = 3,max = 15,message = "账号长度必须介于3-15之间")
    private String userName;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 3,max = 15,message = "密码长度必须介于6-18之间")
    private String password;

    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^1(3[0-9]|4[01456789]|5[0-35-9]|6[2567]|7[0-8]|8[0-9]|9[0-9])\\d{8}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码不能为空")
    private String code;
}
