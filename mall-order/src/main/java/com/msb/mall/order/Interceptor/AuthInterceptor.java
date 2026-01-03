package com.msb.mall.order.Interceptor;

import com.msb.common.constant.AuthConstant;
import com.msb.common.vo.MemberVO;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberVO> threadLocal=new ThreadLocal();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 先获取当前请求的路径，判断是否是支付宝回调路径 → 是则直接放行
        String requestPath = request.getServletPath();
        // 白名单：支付宝异步回调、同步回调的路径
        if ("/payed/notify".equals(requestPath) || "/orderPay/returnUrl".equals(requestPath) || "/test".equals(requestPath)) {
            return true; // 直接放行，不做登录校验
        }


        HttpSession session = request.getSession();
        Object attribute = session.getAttribute(AuthConstant.AUTH_SESSION_REDIS);
        if (attribute != null){
            MemberVO memberVO = (MemberVO) attribute;
            threadLocal.set(memberVO);
            return true;
        }
        session.setAttribute(AuthConstant.AUTH_SESSION_MSG,"请先登录");
        response.sendRedirect("http://auth.msb.com/login.html");
        return false;



    }
}
