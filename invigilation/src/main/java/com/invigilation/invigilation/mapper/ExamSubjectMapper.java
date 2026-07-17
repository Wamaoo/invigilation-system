package com.invigilation.invigilation.mapper;

import com.invigilation.invigilation.entity.ExamSubject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ExamSubjectMapper {
    //按条件查询考试科目
    List<ExamSubject> selectByCondition(
            @Param("examName") String examName,
            @Param("major") String major,
            @Param("examType") String examType,
            @Param("semesterId") String semesterId);

    // 分页条件查询
    List<ExamSubject> selectByConditionWithPage(
            @Param("examName") String examName,
            @Param("major") String major,
            @Param("examType") String examType,
            @Param("semesterId") String semesterId,
            @Param("offset") int offset,
            @Param("size") int size);

    // 条件统计总数
    long countByCondition(
            @Param("examName") String examName,
            @Param("major") String major,
            @Param("examType") String examType,
            @Param("semesterId") String semesterId);
    //考试科目
    int insert(ExamSubject examSubject);
    //根据ID更新考试科目
    int updateById(ExamSubject examSubject);
    //根据ID删除考试科目
    int deleteById(String subjectId);

    //统计考试科目总数
    long countExams();

    // 获取所有科目ID（用于自动生成ID）
    @Select("SELECT subject_id FROM exam_subject WHERE subject_id LIKE 'EXAM%'")
    List<String> selectAllExamIds();

    // 根据ID查询
    @Select("SELECT * FROM exam_subject WHERE subject_id = #{subjectId}")
    ExamSubject selectBySubjectId(@Param("subjectId") String subjectId);
}