package com.lxw.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.lxw.reggie.common.BaseContext;
import com.lxw.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 这里我们使用servlet的原生过滤器实现（注解实现），需要@WebFilter+@ServletComponentScan注解
 * 原生servlet的注入还有另一种方式：通过配置类注入
 * 当然，使用springmvc为我们设计的拦截器也可以实现相同的效果
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request= (HttpServletRequest) servletRequest;
        HttpServletResponse response= (HttpServletResponse) servletResponse;
        //1.获取本次请求的url
        String requestURI = request.getRequestURI();
        //定义不需要拦截的请求路径
        String[] noNeedToHandle =new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };

        //定义后台管理者可访问的资源
        String[] employeeUrls =new String[]{
                "/category/**",
                "/dish/**",
                "/employee/**",
                "/setmeal/**",
                "/common/**"
        };

        //定义用户可访问的资源
        String[] userUrls =new String[]{
                "/category/**",
                "/common/**",
                "/addressBook/**",
                "/order/**",
                "/orderDetail/**",
                "/shoppingCart/**",
                "/user/**",
                "/front/**",
                "/dish/**",
                "/setmeal/**"
        };

        //2.判断本次请求是否需要处理
        boolean check=check(noNeedToHandle,requestURI);
        //3.如果不需要处理，直接放行
        if(check){
            filterChain.doFilter(request,response);
            return;
        }
        //4.判断登陆状态，如果已登陆，则直接放行
        if(request.getSession().getAttribute("employee")!= null && check(employeeUrls,requestURI)){
            log.info("用户已登录，用户id为,{}",request.getSession().getAttribute("employee"));
            //将用户信息保存在当前线程内
            Long employeeId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(employeeId);

            filterChain.doFilter(request,response);
            return;
        }

        if(request.getSession().getAttribute("user")!= null && check(userUrls,requestURI)){
            log.info("用户已登录，用户id为,{}",request.getSession().getAttribute("user"));
            //将用户信息保存在当前线程内
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        //5.用户未登录
        log.info("用户未登录，拦截的请求为{}",requestURI);
        //NOTLOGIN是前端为我们规定的返回值，当前端收到这串字符时，就会拦截请求并跳转到首页
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    /**
     * 匹配路径是否需要拦截
     * @param urls
     * @param requestURI
     * @return
     */
    private boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
