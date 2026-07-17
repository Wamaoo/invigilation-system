//处理业务逻辑接口
package com.invigilation.invigilation.service;

import com.invigilation.invigilation.entity.TeacherInfo;
import java.util.List;

public interface TeacherInfoService {
    List<TeacherInfo> getTeacherList(String teacherName, String department);
    List<TeacherInfo> getTeacherListByPage(String teacherName, String department, int offset, int size);
    long getTeacherTotal(String teacherName, String department);
    boolean addTeacher(TeacherInfo teacherInfo);
    boolean editTeacher(TeacherInfo teacherInfo);
    boolean deleteTeacher(String teacherId);
    //按工号查教师
    TeacherInfo getByTeacherId(String teacherId);
}