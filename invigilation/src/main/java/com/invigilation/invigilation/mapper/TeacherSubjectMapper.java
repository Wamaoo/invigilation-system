package com.invigilation.invigilation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Set;

@Mapper
public interface TeacherSubjectMapper {

    /** 查询教授某门课的所有教师ID */
    @Select("SELECT teacher_id FROM teacher_subject WHERE subject_id = #{subjectId}")
    List<String> selectTeacherIdsBySubjectId(@Param("subjectId") String subjectId);
}
