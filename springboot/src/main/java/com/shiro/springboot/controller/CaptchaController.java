package com.shiro.springboot.controller;

import com.shiro.springboot.captcha.Captcha;
import com.shiro.springboot.captcha.GifCaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@Controller
public class CaptchaController {
    private Logger logger = LoggerFactory.getLogger(CaptchaController.class);
    /**
     * 获取Gif验证码
     * @param response
     */
    @RequestMapping(value = "gifCaptcha",method= RequestMethod.GET)
    public void getGifCaptcha(HttpServletResponse response, HttpServletRequest request) throws FileNotFoundException {
        //告诉客户端，输出的格式
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/gif");
        Captcha  captcha = new GifCaptcha(150,40,5);//   gif格式动画验证码
        try {
            captcha.out(response.getOutputStream());
            logger.info("获取验证码！验证码字符为："+captcha.text());
            HttpSession session = request.getSession(true);
            //存入Session
            session.setAttribute("captchaWord",captcha.text());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}