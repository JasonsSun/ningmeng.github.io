package com.shiro.springboot.config.shiro.Exception;


import org.apache.ibatis.javassist.NotFoundException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        if (e instanceof UnauthorizedException) {
            ModelAndView mv = new ModelAndView("/background/403");
            return mv;
        }
        if(e instanceof NotFoundException)
        {
            ModelAndView mv = new ModelAndView("/background/404");
            return mv;
        }
        return null;
    }
}

