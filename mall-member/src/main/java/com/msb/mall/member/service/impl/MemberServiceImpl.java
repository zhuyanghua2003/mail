package com.msb.mall.member.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.msb.common.utils.HttpUtils;
import com.msb.mall.member.entity.MemberLevelEntity;
import com.msb.mall.member.exception.PhoneExsitException;
import com.msb.mall.member.exception.UserNameExsitException;
import com.msb.mall.member.service.MemberLevelService;
import com.msb.mall.member.vo.MemberLoginVO;
import com.msb.mall.member.vo.MemberReigerVO;
import com.msb.mall.member.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.member.dao.MemberDao;
import com.msb.mall.member.entity.MemberEntity;
import com.msb.mall.member.service.MemberService;

@Slf4j
@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberReigerVO vo) throws UserNameExsitException, PhoneExsitException{
        MemberEntity entity=new MemberEntity();
        MemberLevelEntity levelEntity= memberLevelService.queryMemberLevelDefault();
        entity.setLevelId(levelEntity.getId());
        //添加对应的账号和手机号不能重复
        checkUsernameUnique(vo.getUserName());
        checkPhoneUnique(vo.getPhone());

        entity.setUsername(vo.getUserName());
        entity.setMobile(vo.getPhone());
        entity.setNickname(vo.getUserName());
        //需要对密码做加密处理
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        String encode = encoder.encode(vo.getPassword());
        entity.setPassword(encode);

        entity.setCreateTime(new Date());

        this.save( entity);
    }

    @Override
    public MemberEntity login(MemberLoginVO vo) {
        //1.根据账号和手机号查询会员信息
        MemberEntity entity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", vo.getUserName()).or().eq("mobile", vo.getPhone()));
        log.info("{}",vo);
        log.info("会员信息：{}",entity);
        //2.如果账号或手机号根据密码解密校验是否登录成功
        if (entity!=null){
            BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
            if (encoder.matches(vo.getPassword(),entity.getPassword())){
                return entity;
            }else {
                log.error("密码错误");
            }
        }
        log.error("账号不存在");

        return null;
    }

    @Override
    public MemberEntity login(SocialUser vo) {
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("social_uid", vo.getUid()));
        if (memberEntity!=null){
            MemberEntity entity=new MemberEntity();
            entity.setId(memberEntity.getId());
            entity.setAccessToken(vo.getAccessToken());
            entity.setExpiresIn(vo.getExpiresIn());
            this.updateById(entity);
            return memberEntity;

        }
        MemberEntity entity=new MemberEntity();
        entity.setAccessToken(vo.getAccessToken());
        entity.setExpiresIn(vo.getExpiresIn());
        entity.setSocialUid(vo.getUid());

        try {
            Map<String, String> querys = new HashMap<>();
            querys.put("access_token", vo.getAccessToken());
            querys.put("uid", vo.getUid());
            HttpResponse response = HttpUtils.doGet("https://api.weibo.com",
                    "/2/users/show.json",
                    "GET",
                    new HashMap<>(), querys);
            if (response.getStatusLine().getStatusCode()==200){
                String json = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = JSONObject.parseObject(json);
                String nickName=jsonObject.getString("screen_name");
                String gender=jsonObject.getString("gender");
                entity.setNickname(nickName);
                entity.setGender("m".equals(gender)?1:2);
            }
        }catch (Exception e){
            log.error("社交登录失败");

        }
        this.save(entity);
        return entity;
    }

    private void checkPhoneUnique(String phone) throws PhoneExsitException {
        int count = this.count(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count>0){
            throw new PhoneExsitException();
        }
    }

    private void checkUsernameUnique(String userName) throws UserNameExsitException {
        int count = this.count(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count>0){
            throw new UserNameExsitException();
        }

    }

}