package com.shiro.springboot.dao;

import com.shiro.springboot.bean.UserInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserInfoMapper {
    int deleteByPrimaryKey(Long uid);

    int insert(UserInfo record);

    int insertSelective(UserInfo record);

    UserInfo selectByPrimaryKey(Long uid);

    int updateByPrimaryKeySelective(UserInfo record);

    int updateByPrimaryKey(UserInfo record);

    List<UserInfo> selectAllUser();

    UserInfo findByUsername(String username);

    UserInfo findByUserEmail(String email);

    UserInfo findByUsernameAndEmail(@Param("username") String username, @Param("uemail")String uemail);

    int updateUserActive(@Param("username") String username, @Param("uemail")String uemail);

    int updateActiveCode(@Param("uid") Long uid, @Param("uactiveinfo")String uactiveinfo);

    int updateUserPassword(UserInfo record);

}