package com.shiro.springboot.config.shiro.utills;


import com.shiro.springboot.controller.BackGroundController;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.internet.MimeMessage;
import javax.management.*;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

@Service
@RestController
@Transactional
public class MailUtil {
    private static final Logger logger = LoggerFactory.getLogger(BackGroundController.class);
    private  static  String url="/background/active";
    private  static  String changepasswordurl="/background/changepassword";
    //使用对象注入的方式  记得配置文件
//    private JavaMailSenderImpl senderImpl;
//
//    public void setSenderImpl(JavaMailSenderImpl senderImpl) {
//        this.senderImpl = senderImpl;
//    }

//    @Autowired
//    private JavaMailSender mailSender;


//    private SimpleMailMessage mailMessage;
//
//    public void setMailMessage(SimpleMailMessage mailMessage) {
//        this.mailMessage = mailMessage;
//    }

//    private Properties prop;
//
//    public void setProp(Properties prop) {
//        this.prop = prop;
//    }

//    @Value("#{properties['spring.mail.username']}")
//    private String username;

//    @Autowired
//    private JavaMailSender mailSender;
//    @Autowired
//    private TemplateEngine templateEngine;
//    @Value("${mail.fromMail.addr}")
//    private String from;
//    public boolean sendMail(String email, String code) {
//        MimeMessage message = mailSender.createMimeMessage();
//        String register_link = "http://localhost:8080/background/active/email=" + email + "/code=" +code;
////创建邮件正文
//        Context context = new Context();
//        context.setVariable("register_link", register_link);
//        String emailContent = templateEngine.process("emailTemplate.html", context);
//        try {
////true表示需要创建一个multipart message
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setFrom(from);
//            helper.setTo(email);
//            helper.setSubject("柠檬之家注册");
//            helper.setText(emailContent, true);
//            mailSender.send(message);
//            System.out.println("发送邮件成功");
//            return true;
//        } catch (MessagingException e) {
//
//            System.out.println("发送邮件失败:"+e);
//
//            return false;
//        }
//    }
    //发送验证码的方法,to是目标邮箱地址，text是发送的验证码（事先生成）
    public boolean sendMail(String username,String to, String text,JavaMailSender mailSender,FreeMarkerConfigurer freeMarkerConfigurer) {
        MimeMessage message = null;
        try {
            //设定mail server
//            senderImpl.setHost("smtp.qq.com");

            // 设置收件人，寄件人 用数组发送多个邮件
            // String[] array = new String[]    {"sun111@163.com","sun222@sohu.com"};
            // mailMessage.setTo(array);

            /*测试
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom("1659108996@qq.com");
//            message.setTo(to);
//            message.setSubject("柠檬之家注册");
//            message.setText("迎使用柠檬之家，你的激活码是：" + text+"，请点击此链接<a href=\""+url+"\"></a>进行邮箱激活。");
//
//            mailSender.send(message);*/


                message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setFrom("1659108996@qq.com");
                helper.setTo(to);
                helper.setSubject("柠檬之家");


                Map<String, Object> model = new HashMap();
                model.put("code",text);
                model.put("url","http://"+getLocalHostLANAddress()+":8080"+url);
                model.put("activeEmail",to);
                model.put("username",username);
                //修改 application.properties 文件中的读取路径
//            FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
//            configurer.setTemplateLoaderPath("classpath:templates");
                //读取 html 模板
                freemarker.template.Template template = freeMarkerConfigurer.getConfiguration().getTemplate("emailTemplate.html");
                String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                helper.setText(html, true);

//            mailMessage.setTo(to);
//            mailMessage.setFrom("1659108996@qq.com");
//            mailMessage.setSubject("柠檬之家注册");
//            mailMessage.setText("欢迎使用柠檬之家，你的激活码是：" + text+"，请点击此链接"+url+"进行邮箱激活。");
//
//            senderImpl.setUsername("1659108996@qq.com");
//            senderImpl.setPassword("yfcjwbewaxcvbfji");

//            prop.put("mail.smtp.auth", "true");
//            prop.put("mail.smtp.timeout", "25000");
//            senderImpl.setJavaMailProperties(prop);

            //发送邮件
//            senderImpl.send(mailMessage);

            System.out.println("发送邮件成功");
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.out.println("发送邮件失败");
            System.out.println(e);
            return false;
        }

    }

    public static boolean checkEmailExpression(String email) {
        if (email.matches("^\\w+@(\\w+\\.)+\\w+$")) {
            return true;
        }
        return false;
    }

    public  boolean checkMailCode(String mailCode,String  uactiveinfo,TimeUtil timeUtil)
    {
        try{
            String vcodeTime =  uactiveinfo;
            String vcodeTimeArray[] = vcodeTime.split("#");

            //先比较验证码是否正确
            if(vcodeTimeArray[0].equals(mailCode)) {
                boolean flag = timeUtil.cmpTime(vcodeTimeArray[1]);

                if(flag == true){
                    return true;
                }

            }

            return false;

        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        }
    }
    public boolean getVCode(String username,String email,HttpSession session,MailUtil mailUtil,TimeUtil timeUtil,JavaMailSender mailSender,FreeMarkerConfigurer freeMarkerConfigurer) {
        //随机生成5验证码
        Integer x =(int)((Math.random()*9+1)*10000);
        String text = x.toString();
        boolean flag = mailUtil.sendMail(username,email, text,mailSender,freeMarkerConfigurer);
        if(flag == true){
            //发送成功，把验证码和时间记录
            String nowTime = timeUtil.getTime();

            //存入session  验证码#时间
            session.setAttribute("vcodeTime",text+"#"+nowTime);
            System.out.println(session.getAttribute("vcodeTime"));
            return true;

        } else {
            return false;
        }
    }

    public boolean changepassword(String username,String email,HttpSession session,MailUtil mailUtil,TimeUtil timeUtil,JavaMailSender mailSender,FreeMarkerConfigurer freeMarkerConfigurer) {

        String nowTime = timeUtil.getTime();
        boolean flag = mailUtil.sendchangePasswordMail(username,email,nowTime,mailSender,freeMarkerConfigurer);
        if(flag == true){
            //发送成功，把验证码和时间记录


            //存入session  验证码#时间
            session.setAttribute("changePasswordTime",nowTime);
            System.out.println("重置密码信息已发送");
            return true;

        } else {
            System.out.println("重置密码信息发送失败");
            return false;
        }
    }

    public boolean sendchangePasswordMail(String username,String to,String time,JavaMailSender mailSender,FreeMarkerConfigurer freeMarkerConfigurer) {
        MimeMessage message = null;
        try {
            message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("1659108996@qq.com");
            helper.setTo(to);
            helper.setSubject("柠檬之家-密码重置");
            Map<String, Object> model = new HashMap();
            model.put("url","http://"+getLocalHostLANAddress()+":8080"+changepasswordurl);
            model.put("email",to);
            model.put("username",username);
            model.put("time",time);
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("emailchangepassword.html");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            helper.setText(html, true);
            System.out.println("发送重置密码邮件成功");
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.out.println("发送重置密码邮件失败");
            System.out.println(e);
            return false;
        }

    }


    public static InetAddress getLocalHostLANAddress() throws Exception {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            return jdkSuppliedAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String getServerPort(boolean secure) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
        MBeanServer mBeanServer = null;
        if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
            mBeanServer = (MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
        }

        if (mBeanServer == null) {
            logger.debug("调用findMBeanServer查询到的结果为null");
            return "";
        }

        Set<ObjectName> names = null;
        try {
            names = mBeanServer.queryNames(new ObjectName("Catalina:type=Connector,*"), null);
        } catch (Exception e) {
            return "";
        }
        Iterator<ObjectName> it = names.iterator();
        ObjectName oname = null;
        while (it.hasNext()) {
            oname = (ObjectName)it.next();
            String protocol = (String)mBeanServer.getAttribute(oname, "protocol");
            String scheme = (String)mBeanServer.getAttribute(oname, "scheme");
            Boolean secureValue = (Boolean)mBeanServer.getAttribute(oname, "secure");
            Boolean SSLEnabled = (Boolean)mBeanServer.getAttribute(oname, "SSLEnabled");
            if (SSLEnabled != null && SSLEnabled) {// tomcat6开始用SSLEnabled
                secureValue = true;// SSLEnabled=true但secure未配置的情况
                scheme = "https";
            }
            if (protocol != null && ("HTTP/1.1".equals(protocol) || protocol.contains("http"))) {
                if (secure && "https".equals(scheme) && secureValue) {
                    return ((Integer)mBeanServer.getAttribute(oname, "port")).toString();
                } else if (!secure && !"https".equals(scheme) && !secureValue) {
                    return ((Integer)mBeanServer.getAttribute(oname, "port")).toString();
                }
            }
        }
        return "";
    }
}
