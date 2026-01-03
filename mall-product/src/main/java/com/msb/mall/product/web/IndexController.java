package com.msb.mall.product.web;

import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.vo.Catalog2VO;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    // @Trace
    @GetMapping({"/","/home","/index"})
    public String index(Model  model) {
        log.info("进入首页");
        List<CategoryEntity> list= categoryService.getLevelCategory();
        model.addAttribute("categories",list);
        return "index";
    }
    @ResponseBody
    @RequestMapping("/index/catalog.json")
    public Map<String, List<Catalog2VO>> getCategory(){
        log.info("访问三级分类资源");
        Map<String, List<Catalog2VO>> map =categoryService.getCatelog2JSON();
        /*Map<String, List<Catalog2VO>> map =null;*/
        return map;
    }
    @GetMapping("/write")
    @ResponseBody
    public String writeValue(){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        //加写锁
        RLock rLock = readWriteLock.writeLock();
        String s=null;
        rLock.lock();//加写锁
        try {
            log.info("....开始写...."+Thread.currentThread().getName());
            s= UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue().set("msg",s);
            Thread.sleep(30000);
        }catch (Exception e){
            log.error("异常",e);
        }finally {
            rLock.unlock();
        }
        return s;
    }
    @GetMapping("/read")
    @ResponseBody
    public String read(){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.readLock();
        rLock.lock();
        String s=null;
        try {
            log.info("....开始读...."+Thread.currentThread().getName());
            s = stringRedisTemplate.opsForValue().get("msg");
            Thread.sleep(30000);
        } catch (Exception e) {
            log.error("异常",e);
        }finally {
            rLock.unlock();
        }


        return s;
    }



    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        RLock lock = redissonClient.getLock("my-lock");
        lock.lock(10, TimeUnit.SECONDS);
        try {
            log.info("....执行业务逻辑...."+Thread.currentThread().getName());
            Thread.sleep(30000);
        } catch (Exception e) {
            log.error("异常",e);
        }finally {
            log.info("释放锁成功。。。。。"+Thread.currentThread().getName());
            lock.unlock();

        }
        return "hello";
    }

}
