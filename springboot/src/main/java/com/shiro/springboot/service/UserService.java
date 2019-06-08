package com.shiro.springboot.service;


import com.shiro.springboot.bean.UserInfo;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by Administrator on 2017/8/16.
 */
public interface UserService {

    int addUser(UserInfo user);

    List<UserInfo> findAllUser(int pageNum, int pageSize);

    UserInfo findByUserEmail(String email);

    UserInfo findByUsername(String username);

    UserInfo findByUsernameAndEmail(String username,String uemail);

    int  updateUserActive(String username,String uemail);

    int updateActiveCode(Long uid,String uactiveinfo);

    int updateUserPassword(UserInfo user);


}