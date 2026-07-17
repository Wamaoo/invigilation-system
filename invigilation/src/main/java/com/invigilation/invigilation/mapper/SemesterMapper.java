package com.invigilation.invigilation.mapper;

import com.invigilation.invigilation.entity.Semester;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SemesterMapper {
    List<Semester> selectAll();

    @Select("SELECT * FROM semester WHERE id = #{id}")
    Semester selectById(@Param("id") Integer id);

    @Select("SELECT * FROM semester WHERE is_current = 1 LIMIT 1")
    Semester selectCurrent();

    int insert(Semester semester);

    int updateById(Semester semester);

    int deleteById(@Param("id") Integer id);

    @org.apache.ibatis.annotations.Update("UPDATE semester SET is_current = 0 WHERE is_current = 1")
    int clearCurrent();

    int setCurrent(@Param("id") Integer id);
}
