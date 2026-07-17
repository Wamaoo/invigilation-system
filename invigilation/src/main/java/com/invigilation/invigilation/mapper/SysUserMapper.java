package com.invigilation.invigilation.mapper;

import com.invigilation.invigilation.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserMapper {
    SysUser selectByUsername(@Param("username") String username);
    //插入用户方法
    int insert(SysUser sysUser);

    // 更新密码
    @Update("UPDATE sys_user SET password = #{password} WHERE username = #{username}")
    int updatePassword(@Param("username") String username, @Param("password") String password);
}