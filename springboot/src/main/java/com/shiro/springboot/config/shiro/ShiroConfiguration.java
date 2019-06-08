package com.shiro.springboot.config.shiro;

import com.shiro.springboot.config.shiro.filter.KaptchaFilter;
import com.shiro.springboot.config.shiro.filter.KickoutSessionFilter;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;


@Configuration
@Order(1)
public class ShiroConfiguration {

/********************************************************/
    /**
     * ShiroFilterFactoryBean 处理拦截资源文件过滤器
     *	</br>1,配置shiro安全管理器接口securityManage;
     *	</br>2,shiro 连接约束配置filterChainDefinitions;
     */
//    @Bean public ShiroFilterFactoryBean shiroFilterFactoryBean(
//            org.apache.shiro.mgt.SecurityManager securityManager) {
//        //shiroFilterFactoryBean对象
//        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
//        logger.debug("-----------------Shiro拦截器工厂类注入开始");
//        // 配置shiro安全管理器 SecurityManager
//        shiroFilterFactoryBean.setSecurityManager(securityManager);
//        //添加kickout认证
//        HashMap<String,Filter> hashMap=new HashMap<String,Filter>();
//        hashMap.put("kickout",kickoutSessionFilter());
//        shiroFilterFactoryBean.setFilters(hashMap);
//
//        // 指定要求登录时的链接
//        shiroFilterFactoryBean.setLoginUrl("/toLogin");
//        // 登录成功后要跳转的链接
//        shiroFilterFactoryBean.setSuccessUrl("/home");
//        // 未授权时跳转的界面;
//        shiroFilterFactoryBean.setUnauthorizedUrl("/error");
//
//        // filterChainDefinitions拦截器=map必须用：LinkedHashMap，因为它必须保证有序
//        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
//        // 配置退出过滤器,具体的退出代码Shiro已经实现
//        filterChainDefinitionMap.put("/logout", "logout");
//        //配置记住我或认证通过可以访问的地址
//        filterChainDefinitionMap.put("/user/userList", "user");
//        filterChainDefinitionMap.put("/", "user");
////
////		// 配置不会被拦截的链接 从上向下顺序判断
//        filterChainDefinitionMap.put("/login", "anon");
//        filterChainDefinitionMap.put("/css/*", "anon");
//        filterChainDefinitionMap.put("/js/*", "anon");
//        filterChainDefinitionMap.put("/js/*/*", "anon");
//        filterChainDefinitionMap.put("/js/*/*/*", "anon");
//        filterChainDefinitionMap.put("/images/*/**", "anon");
//        filterChainDefinitionMap.put("/layui/*", "anon");
//        filterChainDefinitionMap.put("/layui/*/**", "anon");
//        filterChainDefinitionMap.put("/treegrid/*", "anon");
//        filterChainDefinitionMap.put("/treegrid/*/*", "anon");
//        filterChainDefinitionMap.put("/fragments/*", "anon");
//        filterChainDefinitionMap.put("/layout", "anon");
//
//        filterChainDefinitionMap.put("/user/sendMsg", "anon");
//        filterChainDefinitionMap.put("/user/login", "anon");
//        filterChainDefinitionMap.put("/home", "anon");
////		/*filterChainDefinitionMap.put("/page", "anon");
////		filterChainDefinitionMap.put("/channel/record", "anon");*/
//        filterChainDefinitionMap.put("/user/delUser", "authc,perms[usermanage]");
////		//add操作，该用户必须有【addOperation】权限
//////		filterChainDefinitionMap.put("/add", "perms[addOperation]");
////
////		// <!-- authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问【放行】-->
//        filterChainDefinitionMap.put("/**", "kickout,authc");
//        filterChainDefinitionMap.put("/*/*", "authc");
//        filterChainDefinitionMap.put("/*/*/*", "authc");
//        filterChainDefinitionMap.put("/*/*/*/**", "authc");
//
//        shiroFilterFactoryBean
//                .setFilterChainDefinitionMap(filterChainDefinitionMap);
//        logger.debug("-----------------Shiro拦截器工厂类注入成功");
//        return shiroFilterFactoryBean;
//    }
/********************************************************/
    /**
     * ShiroFilterFactoryBean 处理拦截资源文件问题。
     * 注意：单独一个ShiroFilterFactoryBean配置是或报错的，以为在
     * 初始化ShiroFilterFactoryBean的时候需要注入：SecurityManager Filter Chain定义说明
     * 1、一个URL可以配置多个Filter，使用逗号分隔 2、当设置多个过滤器时，全部验证通过，才视为通过
     * 3、部分过滤器可指定参数，如perms，roles
     */
    @Bean
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 必须设置 SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        //验证码过滤器
        Map<String, Filter> filtersMap = shiroFilterFactoryBean.getFilters();
        //剔除用户
        filtersMap.put("kickout",kickoutSessionFilter());

        KaptchaFilter kaptchaFilter = new KaptchaFilter();
        //验证码
        filtersMap.put("kaptchaFilter", kaptchaFilter);
        //实现自己规则roles,这是为了实现or的效果
        //RoleFilter roleFilter = new RoleFilter();
        //filtersMap.put("roles", roleFilter);
        shiroFilterFactoryBean.setFilters(filtersMap);


        // 拦截器.
        //rest：比如/admins/user/**=rest[user],根据请求的方法，相当于/admins/user/**=perms[user：method] ,其中method为post，get，delete等。
        //port：比如/admins/user/**=port[8081],当请求的url的端口不是8081是跳转到schemal：//serverName：8081?queryString,其中schmal是协议http或https等，serverName是你访问的host,8081是url配置里port的端口，queryString是你访问的url里的？后面的参数。
        //perms：比如/admins/user/**=perms[user：add：*],perms参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，比如/admins/user/**=perms["user：add：*,user：modify：*"]，当有多个参数时必须每个参数都通过才通过，想当于isPermitedAll()方法。
        //roles：比如/admins/user/**=roles[admin],参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，当有多个参数时，比如/admins/user/**=roles["admin,guest"],每个参数通过才算通过，相当于hasAllRoles()方法。//要实现or的效果看http://zgzty.blog.163.com/blog/static/83831226201302983358670/
        //anon：比如/admins/**=anon 没有参数，表示可以匿名使用。
        //authc：比如/admins/user/**=authc表示需要认证才能使用，没有参数
        //authcBasic：比如/admins/user/**=authcBasic没有参数表示httpBasic认证
        //ssl：比如/admins/user/**=ssl没有参数，表示安全的url请求，协议为https
        //user：比如/admins/user/**=user没有参数表示必须存在用户，当登入操作时不做检查

        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        // 配置退出过滤器,其中的具体的退出代码Shiro已经替我们实现了
        filterChainDefinitionMap.put("/static/**", "anon");//配置static文件下资源能被访问的，这是个例子
        filterChainDefinitionMap.put("/background/assets/**", "anon");
        filterChainDefinitionMap.put("/background/css/**", "anon");
        filterChainDefinitionMap.put("/background/img/**", "anon");
        filterChainDefinitionMap.put("/background/js/**", "anon");
        //柠檬之家登录页第二套
        filterChainDefinitionMap.put("/backlogin2/**", "anon");
        filterChainDefinitionMap.put("/backlogin2/css/**", "anon");
        filterChainDefinitionMap.put("/backlogin2/img/**", "anon");
        filterChainDefinitionMap.put("/backlogin2/js/**", "anon");
        // 展示商城首页需要的文件
        filterChainDefinitionMap.put("/homeWork/fonts/**", "anon");
        filterChainDefinitionMap.put("/homeWork/images/**", "anon");
        filterChainDefinitionMap.put("/homeWork/css/**", "anon");
        filterChainDefinitionMap.put("/homeWork/js/**", "anon");
        filterChainDefinitionMap.put("/homeWork/php/**", "anon");
        filterChainDefinitionMap.put("/homeWork/videos/**", "anon");

        filterChainDefinitionMap.put("/foreground/fonts/**", "anon");
        filterChainDefinitionMap.put("/foreground/images/**", "anon");
        filterChainDefinitionMap.put("/foreground/css/**", "anon");
        filterChainDefinitionMap.put("/foreground/js/**", "anon");


        filterChainDefinitionMap.put("/logout", "logout");
        //配置记住我或认证通过可以访问的地址
        filterChainDefinitionMap.put("/foreground/index", "anon");

        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/home", "user");
        filterChainDefinitionMap.put("/background/login", "kaptchaFilter");
        filterChainDefinitionMap.put("/background/login", "anon");//后台登录
        filterChainDefinitionMap.put("/gifCaptcha", "anon");//后台验证码
        filterChainDefinitionMap.put("/background/register", "anon");//后台注册
        filterChainDefinitionMap.put("/background/active", "anon");//后台激活
        filterChainDefinitionMap.put("/background/retrieve", "anon");//邮件重发
        filterChainDefinitionMap.put("/background/forgetpassword", "anon");//找回密码
        filterChainDefinitionMap.put("/background/changepassword", "anon");//重置密码
        //临时测试不部分
        filterChainDefinitionMap.put("/userInfo/add", "anon");//后台登录
        filterChainDefinitionMap.put("/userInfo/all/**", "anon");//后台登录
        filterChainDefinitionMap.put("/background/index", "anon");//重置密码
        //
        filterChainDefinitionMap.put("/foreground/login", "anon");//前台登录
        filterChainDefinitionMap.put("/foreground/register", "anon");//前台注册

        filterChainDefinitionMap.put("/background/index", "user");
        // <!-- 过滤链定义，从上向下顺序执行，一般将 /**放在最为下边 -->:这是一个坑呢，一不小心代码就不好使了;

        //这段是配合 actuator框架使用的，配置相应的角色才能访问
//        filterChainDefinitionMap.put("/health", "roles[aix]");//服务器健康状况页面
//        filterChainDefinitionMap.put("/info", "roles[aix]");//服务器信息页面
//        filterChainDefinitionMap.put("/env", "roles[aix]");//应用程序的环境变量
//        filterChainDefinitionMap.put("/metrics", "roles[aix]");
//        filterChainDefinitionMap.put("/configprops", "roles[aix]");

        //开放的静态资源
        filterChainDefinitionMap.put("/favicon.ico", "anon");//网站图标
        filterChainDefinitionMap.put("/home", "anon");//网站图标
        filterChainDefinitionMap.put("/kaptcha.jpg", "anon");//图片验证码(kaptcha框架)


//        项目完成后修改
//        filterChainDefinitionMap.put("/**", "authc");
        filterChainDefinitionMap.put("/**", "anon");

        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
//        shiroFilterFactoryBean.setLoginUrl("/");
        shiroFilterFactoryBean.setLoginUrl("/toLogin");
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/index");
//        shiroFilterFactoryBean.setSuccessUrl("/background/index");
        // 未授权界面
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");//不生效(详情原因看MyExceptionResolver)
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }


    @Bean
    public MyShiroRealm myShiroRealm() {
        MyShiroRealm myShiroRealm = new MyShiroRealm();
        myShiroRealm.setCredentialsMatcher(hashedCredentialsMatcher()); //设置解密规则
        return myShiroRealm;
    }

    //因为我们的密码是加过密的，所以，如果要Shiro验证用户身份的话，需要告诉它我们用的是md5加密的，并且是加密了两次。同时我们在自己的Realm中也通过SimpleAuthenticationInfo返回了加密时使用的盐。这样Shiro就能顺利的解密密码并验证用户名和密码是否正确了。
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("md5");//散列算法:这里使用MD5算法;
        hashedCredentialsMatcher.setHashIterations(2);//散列的次数，比如散列两次，相当于 md5(md5(""));
        hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);//表示是否存储散列后的密码为16进制，需要和生成密码时的一样，默认是base64；
        return hashedCredentialsMatcher;
    }

    /**
     * 开启shiro aop注解支持.
     * 使用代理方式;所以需要开启代码支持;
     *
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }


    @Bean
    public EhCacheManager ehCacheManager() {
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManagerConfigFile("classpath:configure/ehcache-shiro.xml");
        return ehCacheManager;
    }

    //cookie对象;
    @Bean
    public SimpleCookie rememberMeCookie() {
        //这个参数是cookie的名称，对应前端的checkbox的name = rememberMe
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");

        //<!-- 记住我cookie生效时间3天 ,单位秒;-->
        simpleCookie.setMaxAge(259200);
        return simpleCookie;
    }

    //cookie管理对象;
    @Bean
    public CookieRememberMeManager cookieRememberMeManager() {
        CookieRememberMeManager manager = new CookieRememberMeManager();
        manager.setCookie(rememberMeCookie());
        return manager;
    }

    //SecurityManager 是 Shiro 架构的核心，通过它来链接Realm和用户(文档中称之为Subject.)
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(myShiroRealm()); //将Realm注入到SecurityManager中。
        securityManager.setCacheManager(ehCacheManager()); //注入缓存对象。
        // //注入session管理器;
        securityManager.setSessionManager(sessionManager());
        securityManager.setRememberMeManager(cookieRememberMeManager()); //注入rememberMeManager;
        return securityManager;
    }

    /**
     * EnterpriseCacheSessionDAO shiro sessionDao层的实现；
     * 提供了缓存功能的会话维护，默认情况下使用MapCache实现，内部使用ConcurrentHashMap保存缓存的会话。
     */
    @Bean
    public EnterpriseCacheSessionDAO enterCacheSessionDAO() {
        EnterpriseCacheSessionDAO enterCacheSessionDAO = new EnterpriseCacheSessionDAO();
        //添加缓存管理器
        //enterCacheSessionDAO.setCacheManager(ehCacheManager());
        //添加ehcache活跃缓存名称（必须和ehcache缓存名称一致）
        //session有效时间1天（毫秒）
//        SecurityUtils.getSubject().getSession().setTimeout(86400000);
        enterCacheSessionDAO.setActiveSessionsCacheName("shiro-activeSessionCache");
        return enterCacheSessionDAO;
    }
    /**
     *
     * @描述：sessionManager添加session缓存操作DAO
     * @创建人：wyait
     * @创建时间：2018年4月24日 下午8:13:52
     * @return
     */
    @Bean
    public DefaultWebSessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        //sessionManager.setCacheManager(ehCacheManager());
        sessionManager.setSessionDAO(enterCacheSessionDAO());
        return sessionManager;
    }
    /**
     *
     * @描述：kickoutSessionFilter同一个用户多设备登录限制
     * @创建人：wyait
     * @创建时间：2018年4月24日 下午8:14:28
     * @return
     */
    public KickoutSessionFilter kickoutSessionFilter(){
        KickoutSessionFilter kickoutSessionFilter = new KickoutSessionFilter();
        //使用cacheManager获取相应的cache来缓存用户登录的会话；用于保存用户—会话之间的关系的；
        //这里我们还是用之前shiro使用的ehcache实现的cacheManager()缓存管理
        //也可以重新另写一个，重新配置缓存时间之类的自定义缓存属性
        kickoutSessionFilter.setCacheManager(ehCacheManager());
        //用于根据会话ID，获取会话进行踢出操作的；
        kickoutSessionFilter.setSessionManager(sessionManager());
        //是否踢出后来登录的，默认是false；即后者登录的用户踢出前者登录的用户；踢出顺序。
        kickoutSessionFilter.setKickoutAfter(false);
        //同一个用户最大的会话数，默认1；比如2的意思是同一个用户允许最多同时两个人登录；
        kickoutSessionFilter.setMaxSession(1);
        //被踢出后重定向到的地址；
        kickoutSessionFilter.setKickoutUrl("/toLogin?kickout=1");
        return kickoutSessionFilter;
    }
    /**
     *
     * @描述：开启Shiro的注解(如@RequiresRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的类,并在必要时进行安全逻辑验证
     * 配置以下两个bean(DefaultAdvisorAutoProxyCreator和AuthorizationAttributeSourceAdvisor)即可实现此功能
     * </br>Enable Shiro Annotations for Spring-configured beans. Only run after the lifecycleBeanProcessor(保证实现了Shiro内部lifecycle函数的bean执行) has run
     * </br>不使用注解的话，可以注释掉这两个配置
     * @创建人：wyait
     * @创建时间：2018年5月21日 下午6:07:56
     * @return
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

}
