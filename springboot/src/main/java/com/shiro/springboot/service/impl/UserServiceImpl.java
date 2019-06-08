package com.shiro.springboot.service.impl;

import com.github.pagehelper.PageHelper;
import com.shiro.springboot.bean.UserInfo;
import com.shiro.springboot.config.shiro.utills.MailUtil;
import com.shiro.springboot.config.shiro.utills.TimeUtil;
import com.shiro.springboot.dao.UserInfoMapper;
import com.shiro.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by Administrator on 2017/8/16.
 */
@Service(value = "userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userMapper;//这里会报错，但是并不会影响

    private MailUtil mailUtil = new MailUtil();

    private TimeUtil timeUtil = new TimeUtil();
    @Override
    public int addUser(UserInfo user) {

        return userMapper.insertSelective(user);
    }

    /*
     * 这个方法中用到了我们开头配置依赖的分页插件pagehelper
     * 很简单，只需要在service层传入参数，然后将参数传递给一个插件的一个静态方法即可；
     * pageNum 开始页数
     * pageSize 每页显示的数据条数
     * */
    @Override
    public List<UserInfo> findAllUser(int pageNum, int pageSize) {
        //将参数传给这个方法就可以实现物理分页了，非常简单。
        PageHelper.startPage(pageNum, pageSize);
        return userMapper.selectAllUser();
    }

    @Override
    public UserInfo findByUserEmail(String email) {
            return userMapper.findByUserEmail(email);
    }

    @Override
    public UserInfo findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public UserInfo findByUsernameAndEmail(String username, String uemail) {
        return userMapper.findByUsernameAndEmail(username,uemail);
    }

    @Override
    public int updateUserActive(String username, String uemail) {
        return userMapper.updateUserActive(username,uemail);
    }

    @Override
    public int updateActiveCode(Long uid, String uactiveinfo) {
        return userMapper.updateActiveCode(uid,uactiveinfo);
    }

    @Override
    public int updateUserPassword(UserInfo user) {
        return userMapper.updateUserPassword(user);
    }


}