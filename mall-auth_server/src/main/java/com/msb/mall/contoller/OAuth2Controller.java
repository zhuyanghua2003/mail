package com.msb.mall.contoller;

import com.alibaba.fastjson.JSON;
import com.msb.common.constant.AuthConstant;
import com.msb.common.utils.HttpUtils;
import com.msb.common.utils.R;
import com.msb.common.vo.MemberVO;
import com.msb.mall.fegin.MemberFeginService;
import com.msb.mall.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class OAuth2Controller {
    @Autowired
    private MemberFeginService memberFeginService;

    @RequestMapping("/oauth/weibo/success")
    public String weiboAuth(@RequestParam("code") String code,
                            HttpSession session) throws Exception {
        Map<String, String> body=new HashMap<>();
        body.put("client_id", "950574373");
        body.put("client_secret", "5001de0f9b060b80faa71d99f80208b7");
        body.put("grant_type", "authorization_code");
        body.put("redirect_uri", "http://auth.msb.com/oauth/weibo/success");
        body.put("code", code);
        HttpResponse post = HttpUtils.doPost("https://api.weibo.com"
                , "/oauth2/access_token",
                "POST",
                new HashMap<>(),
                null,
                body);

        int statusCode = post.getStatusLine().getStatusCode();
        if (statusCode != 200){
            log.error("微博登录失败，获取到的access_token:{}",post.toString());
            return "redirect:http://mall.msb.com/login.html";
        }
        String json = EntityUtils.toString(post.getEntity());
        SocialUser socialUser= JSON.parseObject(json, SocialUser.class);
        log.info("socialUser:{}",socialUser);
        R r = memberFeginService.socialLogin(socialUser);
        if (r.getCode()!=0){
            log.error("微博登录失败");
            return "redirect:http://mall.msb.com/login.html";
        }
        String entityJson=(String) r.get("entity");
        MemberVO memberVo=JSON.parseObject(entityJson, MemberVO.class);
        session.setAttribute(AuthConstant.AUTH_SESSION_REDIS,memberVo);


        return "redirect:http://mall.msb.com/home";
    }
}
