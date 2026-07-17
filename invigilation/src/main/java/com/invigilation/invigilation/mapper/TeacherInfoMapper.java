//操作数据库接口
package com.invigilation.invigilation.mapper;

import com.invigilation.invigilation.entity.TeacherInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
//标记这是MyBatis的Mapper接口
@Mapper
public interface TeacherInfoMapper {
    // 按条件查询教师列表（姓名/部门）
    List<TeacherInfo> selectByCondition(
            @Param("teacherName") String teacherName,
            @Param("department") String department);
    //新增教师
    int insert(TeacherInfo teacherInfo);
    int updateById(TeacherInfo teacherInfo);
    int deleteById(@Param("teacherId") String teacherId);

    // 分页条件查询
    List<TeacherInfo> selectByConditionWithPage(
            @Param("teacherName") String teacherName,
            @Param("department") String department,
            @Param("offset") int offset,
            @Param("size") int size);

    // 条件统计总数
    long countByCondition(
            @Param("teacherName") String teacherName,
            @Param("department") String department);

    //统计教师总数
    long countTeachers();
    //根据工号查教师信息
    @Select("SELECT * FROM teacher_info WHERE teacher_id = #{teacherId}")
    TeacherInfo selectByTeacherId(String teacherId);
}