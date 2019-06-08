package com.shiro.springboot.dao;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;

import java.io.Serializable;
import java.util.Collection;

public interface SessionDAO {
    /*如DefaultSessionManager在创建完session后会调用该方法；
     如保存到关系数据库/文件系统/NoSQL数据库；即可以实现会话的持久化；
     返回会话ID；主要此处返回的ID.equals(session.getId())；
   */
    Serializable create(Session session);

    //根据会话ID获取会话
    Session readSession(Serializable sessionId) throws UnknownSessionException;

    //更新会话；如更新会话最后访问时间/停止会话/设置超时时间/设置移除属性等会调用
    void update(Session session) throws UnknownSessionException;

    //删除会话；当会话过期/会话停止（如用户退出时）会调用
    void delete(Session session);

    //获取当前所有活跃用户，如果用户量多此方法影响性能
    Collection<Session> getActiveSessions();
}
