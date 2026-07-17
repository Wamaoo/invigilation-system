package com.invigilation.invigilation.mapper;

import com.invigilation.invigilation.entity.InvigilationConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InvigilationConfigMapper {
    List<InvigilationConfig> selectByCondition(
            @Param("examName") String examName,
            @Param("classroom") String classroom,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("semesterId") String semesterId);

    int insert(InvigilationConfig config);

    int updateById(InvigilationConfig config);

    int deleteById(@Param("arrangeId") String arrangeId);

    List<InvigilationConfig> selectByIds(@Param("arrangeIds") List<String> arrangeIds);

    long countInvigilations();

    long countConflicts();

    List<InvigilationConfig> selectRecentInvigilations(@Param("limit") Integer limit);

    // 查询全部监考安排（用于图表数据）
    List<InvigilationConfig> selectAll();

    // 获取所有安排ID（用于自动生成ID）
    @Select("SELECT arrange_id FROM invigilation_config WHERE arrange_id LIKE 'ARRANGE%'")
    List<String> selectAllArrangeIds();

    // 根据安排ID查询
    @Select("SELECT * FROM invigilation_config WHERE arrange_id = #{arrangeId}")
    InvigilationConfig selectByArrangeId(@Param("arrangeId") String arrangeId);

    // 带分页的条件查询（支持日期筛选）
    List<InvigilationConfig> selectByConditionWithPage(
            @Param("examName") String examName,
            @Param("classroom") String classroom,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("semesterId") String semesterId,
            @Param("offset") int offset,
            @Param("size") int size);

    // 条件查询总数（支持日期筛选）
    long countByCondition(
            @Param("examName") String examName,
            @Param("classroom") String classroom,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("semesterId") String semesterId);

    // 查询某教师在某时间范围内的冲突安排数
    long countTeacherConflictsInTimeRange(
            @Param("teacherId") String teacherId,
            @Param("examDate") String examDate,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("excludeArrangeId") String excludeArrangeId);

    // 获取每位教师的监考次数统计
    @Select("SELECT ti.teacher_id, COUNT(*) AS cnt FROM teacher_invigilation ti " +
            "JOIN invigilation_config ic ON ti.arrange_id = ic.arrange_id " +
            "WHERE ic.status != '已结束' GROUP BY ti.teacher_id ORDER BY cnt ASC")
    List<java.util.Map<String, Object>> countTeacherWorkload();
}