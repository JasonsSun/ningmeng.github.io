package com.shiro.springboot.controller;

import com.shiro.springboot.bean.UserInfo;
import com.shiro.springboot.config.shiro.Exception.IncorrectCaptchaException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping({"/", "/foreground/"})
public class HomeController {
    @RequestMapping({"index"})
    public String foregroundindex() {
        return "foreground/index";
    }

    @RequestMapping({"/", "/home"})
    public String foregroundhome() {
        return "foreground/home";
    }

    @RequestMapping({"register"})
    public String backgroundregister() {
        return "foreground/register";
    }

    @RequestMapping("login")
    public String foregroundlogin(HttpServletRequest request, UserInfo user,Map<String, Object> map) throws Exception {

        // 登录失败从request中获取shiro处理的异常信息。
        // shiroLoginFailure:就是shiro异常类的全类名.
        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPassword())) {
//            request.setAttribute("msg", "用户名或密码不能为空！");
            return "foreground/login";
        }

        Subject subject = SecurityUtils.getSubject();
//        UsernamePasswordToken token=new UsernamePasswordToken(user.getUsername(),user.getPassword());
        UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), user.getPassword());
        Object exception = request.getAttribute("shiroLoginFailure");

        String msg = "";
        if (IncorrectCaptchaException.class.isInstance(exception)) {
            request.setAttribute("msg", "验证码错误");
        } else if (UnknownAccountException.class.isInstance(exception)) {
            request.setAttribute("msg", "账户不存在");
        } else if (IncorrectCredentialsException.class.isInstance(exception)) {
            request.setAttribute("msg", "密码不正确");
        }
        try {
            subject.login(token);
            return "redirect:/foreground/success";
        } catch (LockedAccountException lae) {
            token.clear();
            request.setAttribute("msg", "用户已经被锁定不能登录，请与管理员联系！");
            return "foreground/login";
        } catch (AuthenticationException e) {
            token.clear();

            request.setAttribute("msg", "用户或密码不正确！");
            return "foreground/login";
        } catch (Exception exceptions) {
             msg = "其他异常,请联系系统管理员！";
        }



        map.put("msg", msg);
        // 此方法不处理登录成功,由shiro进行处理.
        return "foreground/login";
    }
}
