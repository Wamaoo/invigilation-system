package com.invigilation.invigilation.service;

import com.invigilation.invigilation.entity.TeacherInvigilation;
import java.util.List;

public interface TeacherInvigilationService {
    List<TeacherInvigilation> getByTeacherId(String teacherId);

    List<TeacherInvigilation> getByArrangeId(String arrangeId);

    void addTeacher(TeacherInvigilation record);

    void batchAddTeachers(List<TeacherInvigilation> list);

    void removeByArrangeId(String arrangeId);

    void removeByTeacherId(String teacherId);
}