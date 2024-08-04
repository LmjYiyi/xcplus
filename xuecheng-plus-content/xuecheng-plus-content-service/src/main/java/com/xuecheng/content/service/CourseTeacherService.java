package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {
    List<CourseTeacher>  getCourseTeacher(Long courseId);

    CourseTeacher setCourseTeacher(CourseTeacher courseTeacher);


     void deleteCourseTeacher(Long coureId,Long id);


}
