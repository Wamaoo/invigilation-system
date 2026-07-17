package com.invigilation.invigilation.mapper;

import com.invigilation.invigilation.entity.TeacherInvigilation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TeacherInvigilationMapper {
    List<TeacherInvigilation> selectByTeacherId(@Param("teacherId") String teacherId);

    List<TeacherInvigilation> selectByArrangeId(@Param("arrangeId") String arrangeId);

    int insert(TeacherInvigilation record);

    int batchInsert(@Param("list") List<TeacherInvigilation> list);

    int deleteByArrangeId(@Param("arrangeId") String arrangeId);

    int deleteByTeacherId(@Param("teacherId") String teacherId);
}