//操作数据库（相当于 “数据库操作员”）
package com.invigilation.invigilation.mapper;

import com.invigilation.invigilation.entity.ConflictApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConflictApplicationMapper {
    // 提交申请
    int insert(ConflictApplication application);
    // 管理员查询所有申请
    List<ConflictApplication> selectAll();
    // 管理员更新申请状态
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    // 删除申请
    int deleteById(@Param("id") Integer id);

    // 根据ID查询
    ConflictApplication selectById(@Param("id") Integer id);
}