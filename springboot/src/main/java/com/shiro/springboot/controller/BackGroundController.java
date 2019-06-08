package com.shiro.springboot.controller;

import com.google.code.kaptcha.Constants;
import com.shiro.springboot.bean.UserInfo;
import com.shiro.springboot.config.shiro.Exception.IncorrectCaptchaException;
import com.shiro.springboot.config.shiro.utills.MailUtil;
import com.shiro.springboot.config.shiro.utills.PasswordUtil;
import com.shiro.springboot.config.shiro.utills.TimeUtil;
import com.shiro.springboot.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/background/")
public class BackGroundController {
    private static final Logger logger = LoggerFactory.getLogger(BackGroundController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;  //自动注入

    private MailUtil mailUtil = new MailUtil();

    private TimeUtil timeUtil = new TimeUtil();

    @RequestMapping({"index"})
    public String backgroundindex(Model model, HttpServletRequest request) {
        model.addAttribute("user",request.getSession().getAttribute("userSession"));
        return "background/index";
    }

    @RequestMapping(value = "register", method = RequestMethod.GET)
    public String backgroundregister() {
        return "background/register";
    }

    @RequestMapping(value = "register", method = RequestMethod.POST)
    public String backgroundregisterUser(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        response.setContentType("application/json;charset=utf-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        if (!MailUtil.checkEmailExpression(userInfo.getUemail())) {
            request.setAttribute("msg", "邮箱不合法！");
            request.setAttribute("state", "error");
            return "background/register";
        }
        if (userService.findByUserEmail(userInfo.getUemail()) != null) {
            request.setAttribute("msg", "邮箱已被注册！");
            request.setAttribute("state", "error");
            return "background/register";

        }
        if (userService.findByUserEmail(userInfo.getUemail()) != null) {
            request.setAttribute("msg", "邮箱已被注册！");
            request.setAttribute("state", "error");
            return "background/register";

        }
        if (StringUtils.isEmpty(userInfo.getUsername()) || StringUtils.isEmpty(userInfo.getPassword())) {
            request.setAttribute("msg", "用户名或密码不能为空！");
            request.setAttribute("state", "error");

            return "background/login";
        }
        boolean flag = false;
        if (null == userService.findByUsername(userInfo.getUsername())) {
            flag = mailUtil.getVCode(userInfo.getUsername(),userInfo.getUemail(), session, mailUtil, timeUtil,mailSender,freeMarkerConfigurer);
            if (flag) {
                String code = (String) session.getAttribute("vcodeTime");
                userInfo.setState((byte) 0);
                userInfo.setSalt(PasswordUtil.createSalt().toString());
                userInfo.setUactiveinfo(code);
                String codepw = PasswordUtil.generatePassword(userInfo.getPassword(),userInfo.getSalt());
                userInfo.setPassword(codepw);
                userService.addUser(userInfo);
                request.setAttribute("msg", "邮箱激活验证码发送成功！");
                request.setAttribute("state", "success");
                return "background/register";

            } else {
                request.setAttribute("msg", "邮箱激活验证码发送失败！");
                request.setAttribute("state", "error");
                return "background/register";
            }
        }

        request.setAttribute("msg", "该用户名已被注册！");
        request.setAttribute("state", "error");
        return "background/register";
    }

    @RequestMapping("login")
    public String backgroundlogin(HttpServletRequest request, String captcha, UserInfo user, RedirectAttributes redirectAttributes, Map<String, Object> map) throws Exception {
//        // 登录失败从request中获取shiro处理的异常信息。
//        // shiroLoginFailure:就是shiro异常类的全类名.
        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPassword())) {
//            request.setAttribute("msg", "用户名或密码不能为空！");
            return "background/login2";
        }
        String captchas = (String) request.getSession().getAttribute("captchaWord");
        if (!captcha.equalsIgnoreCase(captchas)) {
            request.setAttribute("username", user.getUsername());
            request.setAttribute("msg", "验证码错误");
            request.setAttribute("state", "error");
            return "background/login2";
        }
        Subject subject = SecurityUtils.getSubject();
//        UsernamePasswordToken token=new UsernamePasswordToken(user.getUsername(),user.getPassword());
        UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), user.getPassword());
        Object exception = request.getAttribute("shiroLoginFailure");
        String msg = "";
        if (exception != null) {
//            if (IncorrectCaptchaException.class.isInstance(exception)) {
//                request.setAttribute("msg", "验证码错误");
//                request.setAttribute("state", "error");
//                return "background/login3";
//            } else
                if (UnknownAccountException.class.isInstance(exception)) {
                    request.setAttribute("username", user.getUsername());
                    request.setAttribute("msg", "账户不存在");
                    request.setAttribute("state", "error");
            } else if (IncorrectCredentialsException.class.isInstance(exception)) {
                    request.setAttribute("username", user.getUsername());
                    request.setAttribute("msg", "密码不正确");
                    request.setAttribute("state", "error");
            }
            else if (LockedAccountException.class.isInstance(exception)) {
                token.clear();
                request.setAttribute("msg", "用户已经被锁定不能登录，请与管理员联系！");
                request.setAttribute("state", "error");
                return "background/login2";
            }
        } else {

            try {
                subject.login(token);
                logger.info(token.getUsername());
                return "redirect:/background/index";
            } catch (LockedAccountException lae) {
                token.clear();
                request.setAttribute("msg", "用户已经被锁定不能登录，请与管理员联系！");
                request.setAttribute("state", "error");
                return "background/login2";
            }
             catch (DisabledAccountException dis) {
                    token.clear();
                    request.setAttribute("msg", "用户尚未激活，请前去邮箱激活！");
                 request.setAttribute("state", "error");
                    return "background/login2";
                }
             catch (AuthenticationException e) {
                token.clear();
                request.setAttribute("username", user.getUsername());
                request.setAttribute("msg", "用户或密码不正确！");
                 request.setAttribute("state", "error");
                return "background/login2";
            } catch (Exception exceptions) {
                if (exceptions != null) {
                    {
                        msg = "其他异常,请联系系统管理员！";
                    }
                }
            }
        }

        map.put("msg", msg);
        map.put("state", "error");
        // 此方法不处理登录成功,由shiro进行处理.
        return "background/login2";
    }

    /**
     * 进入邮箱激活页面
     *
     * @return
     */
    @RequestMapping(value = "active", method = RequestMethod.GET)
    public String activeUserView( HttpServletRequest request) {
        request.setAttribute("username", request.getParameter("username"));
        request.setAttribute("uemail",request.getParameter("email"));
        return "active";
    }

    @RequestMapping(value = "active", method = RequestMethod.POST)
    public String activeUser(UserInfo userInfo,String activeCode, HttpServletRequest request, HttpSession session) {

        boolean flag = false;
        if (null != userService.findByUsername(userInfo.getUsername())) {
            if(null!=userService.findByUserEmail(userInfo.getUemail())) {
                UserInfo checkCodeUser=userService.findByUserEmail(userInfo.getUemail());
                if (null == userService.findByUsernameAndEmail(userInfo.getUemail(), userInfo.getUsername())) {

                    flag = mailUtil.checkMailCode(activeCode,checkCodeUser.getUactiveinfo(), timeUtil);
                    if (flag == true) {
                        //激活

                        userService.updateUserActive(userInfo.getUsername(),userInfo.getUemail());
                        request.setAttribute("msg", "激活邮箱成功，请登录！");
                        request.setAttribute("state", "success");
                        return "background/login2";
                    }

                    request.setAttribute("msg", "邮箱验证码已失效，请重新获取！");
                    request.setAttribute("state", "error");
                    return "background/register";
                }
                request.setAttribute("msg", "该邮箱已被注册并激活，请更换其他邮箱注册！！");
                request.setAttribute("state", "error");
                return "active";

            }
            request.setAttribute("msg", "该邮箱尚未注册，请核实！");
            request.setAttribute("state", "error");
            return "active";
           }
        request.setAttribute("msg", "该用户尚未注册！");
        request.setAttribute("state", "error");
        return "active";
    }

    /**
     * 重新获取激活码
     *
     * @return
     */
    @RequestMapping(value = "retrieve", method = RequestMethod.GET)
    public String retrieveUserView() {
        return "retrieve";
    }

    @RequestMapping(value = "retrieve", method = RequestMethod.POST)
    public String retrieveUser(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        if (StringUtils.isEmpty(userInfo.getUsername()) || StringUtils.isEmpty(userInfo.getUemail())) {
            request.setAttribute("msg", "用户名或邮箱账号不能为空！");
            request.setAttribute("state", "error");

            return "retrieve";
        }
        boolean flag = false;
        if (null == userService.findByUsernameAndEmail(userInfo.getUsername(),userInfo.getUemail())) {

            if (null!= userService.findByUserEmail(userInfo.getUemail())) {
                flag = mailUtil.getVCode(userInfo.getUsername(),userInfo.getUemail(), session, mailUtil, timeUtil, mailSender, freeMarkerConfigurer);
                if (flag) {

                   userService.updateActiveCode(userService.findByUserEmail(userInfo.getUemail()).getUid(), (String)session.getAttribute("vcodeTime"));
                    request.setAttribute("msg", "邮箱激活验证码发送成功！");
                    request.setAttribute("state", "success");
                    return "background/register";

                } else {
                    request.setAttribute("msg", "邮箱激活验证码发送失败！");
                    request.setAttribute("state", "error");
                    return "background/register";
                }
            }
            request.setAttribute("msg", "该邮箱尚未注册，请核实，谢谢！");
            request.setAttribute("state", "error");
            return "retrieve";
        }
        request.setAttribute("msg", "该邮箱已被注册并激活，请更换其他邮箱注册！");
        request.setAttribute("state", "error");
        return "retrieve";
    }

    /**
     * 通过邮箱找回密码发送（重置密码）
     * @return
     */
    @RequestMapping(value = "forgetpassword", method = RequestMethod.GET)
    public String forgetPasswordView() {
        return "forgetPassword";
    }


    @RequestMapping(value = "forgetpassword", method = RequestMethod.POST)
    public String Userforgetpassword(UserInfo userInfo ,String captcha, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String captchas = (String) request.getSession().getAttribute("captchaWord");
        if (!captcha.equalsIgnoreCase(captchas)) {
            request.setAttribute("email", userInfo.getUemail());
            request.setAttribute("msg", "验证码错误");
            request.setAttribute("state", "error");
            return "forgetPassword";
        }
        if ( StringUtils.isEmpty(userInfo.getUemail())) {
            request.setAttribute("msg", "邮箱账号不能为空！");
            request.setAttribute("state", "error");

            return "forgetPassword";
        }
        boolean flag = false;
        UserInfo userInfos=userService.findByUserEmail(userInfo.getUemail());
            if (null!=userInfos) {
                flag = mailUtil.changepassword(userInfos.getUsername(),userInfos.getUemail(), session, mailUtil, timeUtil, mailSender, freeMarkerConfigurer);
                if (flag) {
                    request.setAttribute("msg", "邮箱重置密码后，需重新登录！");
                    request.setAttribute("state", "success");
                    return "redirect:/background/login";

                } else {
                    request.setAttribute("email", userInfo.getUemail());
                    request.setAttribute("msg", "发送邮件，重置密码失败！");
                    request.setAttribute("state", "error");
                    return "forgetPassword";
                }
            }
            request.setAttribute("email", userInfo.getUemail());
            request.setAttribute("msg", "该邮箱尚未注册，请核实，谢谢！");
            request.setAttribute("state", "error");
            return "retrieve";
    }
    /**
     * 通过点击邮箱链接实现密码重置
     */
    @RequestMapping(value = "changepassword", method = RequestMethod.GET)
    public String UserchangepasswordView(HttpServletRequest request, HttpSession session) {
        request.setAttribute("username", request.getParameter("username"));
        request.setAttribute("uemail",request.getParameter("email"));
        request.setAttribute("time",request.getParameter("time"));
        return "background/changepassword";

    }

    @RequestMapping(value = "changepassword", method = RequestMethod.POST)
    public String Userchangepassword(UserInfo userinfo,String time,String newpassword,String captcha, HttpServletRequest request, HttpSession session) {
        String captchas = (String) request.getSession().getAttribute("captchaWord");
        if (!captcha.equalsIgnoreCase(captchas)) {
            request.setAttribute("msg", "验证码错误");
            request.setAttribute("state", "error");
            return "background/changepassword";
        }
        boolean flag = false;
        if (null != userService.findByUsername(userinfo.getUsername())) {
            if(null!=userService.findByUserEmail(userinfo.getUemail())) {

                userinfo=userService.findByUsernameAndEmail(userinfo.getUsername(),userinfo.getUemail());
                if (null != userinfo) {

                    flag = TimeUtil.cmpTime(time);
                    if (flag == true) {
                        //重置密码
                        userinfo.setSalt(PasswordUtil.createSalt().toString());
                        String codepw = PasswordUtil.generatePassword(newpassword,userinfo.getSalt());
                        userinfo.setPassword(codepw);
                        userService.updateUserPassword(userinfo);
                        request.setAttribute("msg", "重置密码成功，请登录！");
                        request.setAttribute("state", "success");
                        return "background/login2";
                    }

                    request.setAttribute("msg", "该重置链接已失效，请重新获取！");
                    request.setAttribute("state", "error");
                    return "redirect:/background/forgetpassword";
                }
                request.setAttribute("msg", "用户信息不存在，请核实！");
                request.setAttribute("state", "error");
                return "background/changepassword";

            }
            request.setAttribute("msg", "该邮箱尚未注册，请核实！");
            request.setAttribute("state", "error");
            return "background/changepassword";
        }
        request.setAttribute("msg", "该用户尚未注册！");
        request.setAttribute("state", "error");
        return "background/changepassword";
    }

}
