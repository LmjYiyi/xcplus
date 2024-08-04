package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.execption.XueChengPlusExecption;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    CourseTeacherMapper courseTeacherMapper;
    @Override
    public List<CourseTeacher> getCourseTeacher(Long courseId) {
        //SELECT * FROM courseteacher WHERE courseid = #{courseid};
        LambdaQueryWrapper<CourseTeacher> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(CourseTeacher::getCourseId,courseId);
        return courseTeacherMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public CourseTeacher setCourseTeacher(CourseTeacher courseTeacher) {
        Long id = courseTeacher.getId();
        //有则修改，没有则插入。
        if (id!=null){
            //有，修改-更新
            courseTeacherMapper.updateById(courseTeacher);
            return courseTeacherMapper.selectById(id);
        }else {
            //没有，插入
            courseTeacher.setCreateDate(LocalDateTime.now());
            courseTeacherMapper.insert(courseTeacher);
            return courseTeacherMapper.selectById(id);
        }

    }

    @Override
    public void deleteCourseTeacher(Long coureId, Long id) {
        LambdaQueryWrapper<CourseTeacher> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(CourseTeacher::getCourseId,coureId).eq(CourseTeacher::getId,id);
        int delete = courseTeacherMapper.delete(lambdaQueryWrapper);
        if (delete<=0){
            XueChengPlusExecption.cast("删除失败");
        }
    }
}
