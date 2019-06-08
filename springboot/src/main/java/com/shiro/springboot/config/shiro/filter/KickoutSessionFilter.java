package com.shiro.springboot.config.shiro.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiro.springboot.bean.ResponseResult;
import com.shiro.springboot.bean.UserInfo;
import com.shiro.springboot.config.shiro.utills.IStatusMessage;
import com.shiro.springboot.config.shiro.utills.ShiroFilterUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 当用户数量超出限制剔除其他用户的登录
 * 用shiro强大的自定义访问控制拦截器：AccessControlFilter，集成这个接口后要实现下面这2个方法
 * protected abstract boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception;
 * protected abstract boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception;
 */
public class KickoutSessionFilter  extends AccessControlFilter {
    private static final Logger logger = LoggerFactory
            .getLogger(KickoutSessionFilter.class);

    private String kickoutUrl; // 踢出后到的地址
    private boolean kickoutAfter = false; // 踢出之前登录的/之后登录的用户 默认false踢出之前登录的用户
    private int maxSession = 1; // 同一个帐号最大会话数 默认1
    private SessionManager sessionManager;
    private Cache<String, Deque<Serializable>> cache;
    private final static ObjectMapper objectMapper = new ObjectMapper();


    public void setKickoutUrl(String kickoutUrl) {
        this.kickoutUrl = kickoutUrl;
    }

    public void setKickoutAfter(boolean kickoutAfter) {
        this.kickoutAfter = kickoutAfter;
    }

    public void setMaxSession(int maxSession) {
        this.maxSession = maxSession;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // 设置Cache的key的前缀
    public void setCacheManager(CacheManager cacheManager) {
        //必须和ehcache缓存配置中的缓存name一致
        this.cache = cacheManager.getCache("shiro-activeSessionCache");
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request,
                                      ServletResponse response, Object mappedValue) throws Exception {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request,
                                     ServletResponse response) throws Exception {
        Subject subject = getSubject(request, response);

        // 没有登录授权 且没有记住我
        if (!subject.isAuthenticated() && !subject.isRemembered()) {
            // 如果没有登录，直接进行之后的流程
            return true;
        }


        Session session = subject.getSession();
        logger.debug("==session时间设置：" + String.valueOf(session.getTimeout())
                + "===========");
        try {
            // 当前用户
            UserInfo user = (UserInfo) subject.getPrincipal();
            String username = user.getUsername();
            logger.debug("===当前用户username：==" + username);
            Serializable sessionId = session.getId();
            logger.debug("===当前用户sessionId：==" + sessionId);
            // 读取缓存用户 没有就存入
            Deque<Serializable> deque = cache.get(username);
            logger.debug("===当前deque：==" + deque);
            if (deque == null) {
                // 初始化队列
                deque = new ArrayDeque<Serializable>();
            }
            // 如果队列里没有此sessionId，且用户没有被踢出；放入队列
            if (!deque.contains(sessionId)
                    && session.getAttribute("kickout") == null) {
                // 将sessionId存入队列
                deque.push(sessionId);
                // 将用户的sessionId队列缓存
                cache.put(username, deque);
            }
            // 如果队列里的sessionId数超出最大会话数，开始踢人
            while (deque.size() > maxSession) {
                logger.debug("===deque队列长度：==" + deque.size());
                Serializable kickoutSessionId = null;
                // 是否踢出后来登录的，默认是false；即后者登录的用户踢出前者登录的用户；
                if (kickoutAfter) { // 如果踢出后者
                    kickoutSessionId = deque.removeFirst();
                } else { // 否则踢出前者
                    kickoutSessionId = deque.removeLast();
                }
                // 踢出后再更新下缓存队列
                cache.put(username, deque);
                try {
                    // 获取被踢出的sessionId的session对象
                    Session kickoutSession = sessionManager
                            .getSession(new DefaultSessionKey(kickoutSessionId));
                    if (kickoutSession != null) {
                        // 设置会话的kickout属性表示踢出了
                        kickoutSession.setAttribute("kickout", true);
                    }
                } catch (Exception e) {// ignore exception
                }
            }
            // ajax请求

            /**
             * 判断是否已经踢出
             * 1.如果是Ajax 访问，那么给予json返回值提示。
             * 2.如果是普通请求，直接跳转到登录页
             */
            //判断是不是Ajax请求
            ResponseResult responseResult = new ResponseResult();
            if (ShiroFilterUtils.isAjax(request) ) {
                logger.debug(getClass().getName()+ "当前用户已经在其他地方登录，并且是Ajax请求！");
                responseResult.setCode(IStatusMessage.SystemStatus.MANY_LOGINS.getCode());
                responseResult.setMessage("您已在别处登录，请您修改密码或重新登录");
                out(response, responseResult);
                }else{
                // 重定向
                WebUtils.issueRedirect(request, response, kickoutUrl);
                }

            // 如果被踢出了，(前者或后者)直接退出，重定向到踢出后的地址
            if ((Boolean) session.getAttribute("kickout") != null
                    && (Boolean) session.getAttribute("kickout") == true) {
                // 会话被踢出了
                try {
                    // 退出登录
                    subject.logout();
                } catch (Exception e) { // ignore
                }
                saveRequest(request);
                logger.debug("==踢出后用户重定向的路径kickoutUrl:" + kickoutUrl);
                // 重定向
                WebUtils.issueRedirect(request, response, kickoutUrl);
                return false;
            }
            return true;
        } catch (Exception e) { // ignore
            //重定向到登录界面
            WebUtils.issueRedirect(request, response, "/login");
            return false;
        }
    }
    /**
     *
     * @描述：response输出json
     * @创建人：wyait
     * @创建时间：2018年4月24日 下午5:14:22
     * @param response
     * @param result
     */
    public static void out(ServletResponse response, ResponseResult result){
        PrintWriter out = null;
        try {
            response.setCharacterEncoding("UTF-8");//设置编码
            response.setContentType("application/json");//设置返回类型
            out = response.getWriter();
            out.println(objectMapper.writeValueAsString(result));//输出
            logger.error("用户在线数量限制【wyait-manager-->KickoutSessionFilter.out】响应json信息成功");
        } catch (Exception e) {
            logger.error("用户在线数量限制【wyait-manager-->KickoutSessionFilter.out】响应json信息出错", e);
        }finally{
            if(null != out){
                out.flush();
                out.close();
            }
        }
    }
}
