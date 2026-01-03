package com.msb.mall.fegin;

import com.msb.common.utils.R;
import com.msb.mall.vo.LoginVo;
import com.msb.mall.vo.SocialUser;
import com.msb.mall.vo.UserRegisrerVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mall-member")
public interface MemberFeginService {


    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisrerVo vo);

    @RequestMapping("/member/member/login")
    R login( @RequestBody LoginVo vo);

    @RequestMapping("/member/member/oauth2/login")
    R socialLogin(@RequestBody SocialUser vo);
}
