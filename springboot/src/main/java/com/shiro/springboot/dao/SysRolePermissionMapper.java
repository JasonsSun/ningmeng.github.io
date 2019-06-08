package com.shiro.springboot.dao;

import com.shiro.springboot.bean.SysRolePermission;

public interface SysRolePermissionMapper {
    int insert(SysRolePermission record);

    int insertSelective(SysRolePermission record);
}