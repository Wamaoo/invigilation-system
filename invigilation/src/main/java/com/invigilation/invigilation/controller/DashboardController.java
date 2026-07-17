package com.invigilation.invigilation.controller;
import com.invigilation.invigilation.entity.InvigilationConfig;
import com.invigilation.invigilation.entity.TeacherInfo;
import com.invigilation.invigilation.entity.TeacherInvigilation;
import com.invigilation.invigilation.mapper.ExamSubjectMapper;
import com.invigilation.invigilation.mapper.InvigilationConfigMapper;
import com.invigilation.invigilation.mapper.TeacherInfoMapper;
import com.invigilation.invigilation.mapper.TeacherInvigilationMapper;
import com.invigilation.invigilation.service.InvigilationConfigService;
import com.invigilation.invigilation.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Resource
    private TeacherInfoMapper teacherInfoMapper;

    @Resource
    private ExamSubjectMapper examSubjectMapper;

    @Resource
    private InvigilationConfigMapper invigilationConfigMapper;

    @Resource
    private TeacherInvigilationMapper teacherInvigilationMapper;

    @Resource
    private InvigilationConfigService invigilationConfigService;

    @GetMapping("/overview")
    public Result<Map<String, Long>> getOverview() {
        Map<String, Long> overview = new HashMap<>();
        overview.put("teacherCount", teacherInfoMapper.countTeachers());
        overview.put("examCount", examSubjectMapper.countExams());
        overview.put("invigilationCount", invigilationConfigMapper.countInvigilations());
        overview.put("conflictCount", invigilationConfigMapper.countConflicts());
        return Result.success(overview);
    }

    @GetMapping("/recent-invigilation")
    public Result<List<InvigilationConfig>> getRecentInvigilation(@RequestParam(defaultValue = "3") Integer limit) {
        List<InvigilationConfig> recentList = invigilationConfigMapper.selectRecentInvigilations(limit);
        invigilationConfigService.enrichStatusList(recentList);
        invigilationConfigService.enrichTeachers(recentList);
        return Result.success(recentList);
    }

    @GetMapping("/charts")
    public Result<Map<String, Object>> getChartsData() {
        List<InvigilationConfig> allConfigs = invigilationConfigMapper.selectAll();
        invigilationConfigService.enrichStatusList(allConfigs);
        List<TeacherInfo> allTeachers = teacherInfoMapper.selectByCondition(null, null);

        Map<String, TeacherInfo> teacherMap = allTeachers.stream()
                .collect(Collectors.toMap(TeacherInfo::getTeacherId, t -> t, (a, b) -> a));

        // ====== 监考状态分布 ======
        Map<String, Long> statusCount = new LinkedHashMap<>();
        for (InvigilationConfig c : allConfigs) {
            String s = c.getStatus() != null ? c.getStatus() : "未知";
            statusCount.merge(s, 1L, Long::sum);
        }
        List<Map<String, Object>> statusData = new ArrayList<>();
        for (Map.Entry<String, Long> e : statusCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", e.getKey());
            item.put("value", e.getValue());
            statusData.add(item);
        }

        // ====== 各月监考数量趋势 ======
        Map<String, Long> monthCount = new TreeMap<>();
        for (InvigilationConfig c : allConfigs) {
            String d = c.getExamDate();
            if (d != null && d.length() >= 7) {
                String month = d.substring(0, 7);
                monthCount.merge(month, 1L, Long::sum);
            }
        }
        List<String> monthLabels = new ArrayList<>(monthCount.keySet());
        List<Long> monthValues = new ArrayList<>(monthCount.values());

        // ====== 教师监考工作量统计（基于 teacher_invigilation 关联表）=====
        Map<String, Long> teacherWorkload = new HashMap<>();
        List<TeacherInvigilation> allRelations = new ArrayList<>();
        for (InvigilationConfig c : allConfigs) {
            allRelations.addAll(teacherInvigilationMapper.selectByArrangeId(c.getArrangeId()));
        }
        for (TeacherInvigilation ti : allRelations) {
            teacherWorkload.merge(ti.getTeacherId(), 1L, Long::sum);
        }

        List<Map<String, Object>> workloadTop10 = teacherWorkload.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    TeacherInfo ti = teacherMap.get(e.getKey());
                    item.put("teacherName", ti != null ? ti.getTeacherName() : e.getKey());
                    item.put("count", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        // ====== 各部门监考次数统计 ======
        Map<String, Long> departmentCount = new HashMap<>();
        for (Map.Entry<String, Long> e : teacherWorkload.entrySet()) {
            String tid = e.getKey();
            Long count = e.getValue();
            TeacherInfo ti = teacherMap.get(tid);
            String dept = ti != null && ti.getDepartment() != null ? ti.getDepartment() : "未知";
            departmentCount.merge(dept, count, Long::sum);
        }
        List<Map<String, Object>> departmentData = departmentCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("department", e.getKey());
                    item.put("count", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("statusData", statusData);
        result.put("monthLabels", monthLabels);
        result.put("monthValues", monthValues);
        result.put("workloadTop10", workloadTop10);
        result.put("departmentData", departmentData);

        return Result.success(result);
    }
}
