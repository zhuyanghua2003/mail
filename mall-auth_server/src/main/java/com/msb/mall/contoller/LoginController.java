package com.msb.mall.contoller;


import com.alibaba.fastjson.JSON;
import com.msb.common.constant.AuthConstant;
import com.msb.common.constant.SMSConstant;
import com.msb.common.exception.BizCodeEnume;
import com.msb.common.utils.R;
import com.msb.common.vo.MemberVO;
import com.msb.mall.fegin.MemberFeginService;
import com.msb.mall.fegin.ThridPartFeginService;
import com.msb.mall.vo.LoginVo;
import com.msb.mall.vo.UserRegisrerVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class LoginController {

    @Autowired
    private ThridPartFeginService thridPartFeginService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MemberFeginService memberFeginService;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendSmsCode(@RequestParam ("phone") String phone, HttpServletResponse response){
        Object redisCode=redisTemplate.opsForValue().get(SMSConstant.SMS_CODE_PERFIX +phone);
        if (redisCode != null ){
            String s=redisCode.toString();
            if (!"".equals( s)){
                Long l= Long.parseLong(redisCode.toString().split("_")[1]);
                if (System.currentTimeMillis() - l < 60000) {
                    log.error("一分钟不能重复发送验证码");
                    response.setStatus(400);
                    return R.error(BizCodeEnume.VALID_SMS_EXCEPTION.getCode(),BizCodeEnume.VALID_SMS_EXCEPTION.getMsg());
                }
            }


        }
        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            codeBuilder.append(random.nextInt(10)); // 生成0-9的随机数字
        }
        String code = codeBuilder.toString();
        R r = thridPartFeginService.sendSmsCode(phone, code);
        code=code+"_"+System.currentTimeMillis();
        redisTemplate.opsForValue().set(SMSConstant.SMS_CODE_PERFIX +phone, code,10, TimeUnit.MINUTES);
        return R.ok();

    }

    @PostMapping("/sms/register")
    public String register(@Valid UserRegisrerVo vo, BindingResult result, Model model){
        Map<String,String> map=new HashMap<>();
        if (result.hasErrors()){
            List<FieldError> fieldErrors = result.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                log.error("错误字段{}",fieldError.getField());
                log.error("错误信息{}",fieldError.getDefaultMessage());
                String field = fieldError.getField();
                String defaultMessage = fieldError.getDefaultMessage();
                map.put(field,defaultMessage);
            }
            model.addAttribute("error", map);
            return "/reg";
        }else {
            //验证验证码是否正确
            String redisCode = (String) redisTemplate.opsForValue().get(SMSConstant.SMS_CODE_PERFIX + vo.getPhone());
            if (redisCode == null){
                log.error("验证码已过期");
                map.put("code","验证码已过期");
                model.addAttribute("error", map);
                return "/reg";
            }
            redisCode=redisCode.split("_")[0];
            if (!Objects.equals(redisCode, vo.getCode())){
                log.error("验证码错误");
                log.error("验证码为：{},你输入的验证码为{}", redisCode, vo.getCode());
                map.put("code","验证码错误");
                model.addAttribute("error", map);
                return "/reg";
            }else {
                redisTemplate.delete(SMSConstant.SMS_CODE_PERFIX + vo.getPhone());
                log.info("验证码正确");
                R r = memberFeginService.register(vo);
                if (r.getCode() == 0) {
                    log.info("注册成功");
                    return "redirect:http://auth.msb.com/login.html";
                } else{
                    log.error("注册失败");
                    map.put("msg", "注册失败，手机号或账号重复");
                    model.addAttribute("error", map);
                    return "/reg";
                }
            }
        }


    }

    @PostMapping("/login")
    public String login(LoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){
        R r = memberFeginService.login(vo);
        Map<String,Object> map=new HashMap<>();
        if (r.getCode() == 0) {
            session.setAttribute(AuthConstant.AUTH_SESSION_REDIS, "登录成功");
            log.info("登录成功");
            String entityJson=(String) r.get("entity");
            MemberVO memberVo= JSON.parseObject(entityJson, MemberVO.class);
            session.setAttribute(AuthConstant.AUTH_SESSION_REDIS,memberVo);
            return "redirect:http://mall.msb.com/home";
        } else{
            log.error("登录失败");
            redirectAttributes.addFlashAttribute("error", r.get("msg"));
            return "redirect:http://auth.msb.com/login.html";
        }

    }
}
