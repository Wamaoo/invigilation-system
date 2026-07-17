package com.invigilation.invigilation.controller;

import com.invigilation.invigilation.entity.InvigilationConfig;
import com.invigilation.invigilation.entity.TeacherInfo;
import com.invigilation.invigilation.entity.TeacherInvigilation;
import com.invigilation.invigilation.mapper.InvigilationConfigMapper;
import com.invigilation.invigilation.mapper.TeacherInfoMapper;
import com.invigilation.invigilation.mapper.TeacherInvigilationMapper;
import com.invigilation.invigilation.service.InvigilationConfigService;
import com.invigilation.invigilation.util.Result;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher/invigilation")
public class TeacherInvigilationController {
    private static final Logger log = LoggerFactory.getLogger(TeacherInvigilationController.class);

    @Resource
    private InvigilationConfigService invigilationConfigService;

    @Resource
    private TeacherInvigilationMapper teacherInvigilationMapper;

    @Resource
    private TeacherInfoMapper teacherInfoMapper;

    @Resource
    private InvigilationConfigMapper invigilationConfigMapper;

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> getMyInvigilation(@RequestParam String teacherId) {
        log.info("【我的监考】接收到请求，教师ID：{}", teacherId);

        // 通过 teacher_invigilation 关联表查询该教师的所有安排ID
        List<TeacherInvigilation> relations = teacherInvigilationMapper.selectByTeacherId(teacherId);
        if (relations.isEmpty()) {
            return Result.success(List.of());
        }

        List<String> arrangeIds = relations.stream()
                .map(TeacherInvigilation::getArrangeId)
                .collect(Collectors.toList());

        List<InvigilationConfig> configs = invigilationConfigMapper.selectByIds(arrangeIds);
        invigilationConfigService.enrichStatusList(configs);

        TeacherInfo teacher = teacherInfoMapper.selectByTeacherId(teacherId);
        String teacherName = teacher != null ? teacher.getTeacherName() : "";

        List<Map<String, Object>> result = new ArrayList<>();
        for (InvigilationConfig config : configs) {
            if ("已结束".equals(config.getStatus())) continue;

            // 查出该安排的所有教师姓名
            List<TeacherInvigilation> tis = teacherInvigilationMapper.selectByArrangeId(config.getArrangeId());
            List<String> names = tis.stream()
                    .map(ti -> {
                        TeacherInfo tiInfo = teacherInfoMapper.selectByTeacherId(ti.getTeacherId());
                        return tiInfo != null ? tiInfo.getTeacherName() : ti.getTeacherId();
                    })
                    .collect(Collectors.toList());

            Map<String, Object> item = new HashMap<>();
            item.put("arrangeId", config.getArrangeId());
            item.put("examName", config.getExamName());
            item.put("classroom", config.getClassroom());
            item.put("examTime", config.getExamTime());
            item.put("examDate", config.getExamDate() != null ? config.getExamDate() : "");
            item.put("startTime", config.getStartTime() != null ? config.getStartTime() : "");
            item.put("endTime", config.getEndTime() != null ? config.getEndTime() : "");
            item.put("examDuration", config.getExamDuration() != null ? config.getExamDuration() : "");
            item.put("remark", config.getRemark() != null ? config.getRemark() : "");
            item.put("teacherNames", String.join(", ", names));
            item.put("myName", teacherName);
            result.add(item);
        }

        log.info("【我的监考】返回安排数量：{}", result.size());
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getInvigilationList(
            @RequestParam(required = false) String examName,
            @RequestParam(required = false) String classroom,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        int offset = (page - 1) * size;
        List<InvigilationConfig> list = invigilationConfigMapper.selectByConditionWithPage(
                examName, classroom, null, null, null, offset, size);
        invigilationConfigService.enrichStatusList(list);
        invigilationConfigService.enrichTeachers(list);
        long total = invigilationConfigMapper.countByCondition(examName, classroom, null, null, null);
        Map<String, Object> result = Map.of("list", list, "total", total);
        return Result.success(result);
    }
}
