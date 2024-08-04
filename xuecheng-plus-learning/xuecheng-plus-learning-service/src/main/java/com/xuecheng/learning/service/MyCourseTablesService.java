package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * @author Mr.M
 * @version 1.0
 * @description 选课相关的接口
 * @date 2023/2/25 12:00
 */
public interface MyCourseTablesService {


    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);


    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /**
     * 保存选课成功状态
     * @param chooseCourseId
     * @return
     */
    public boolean saveChooseCourseSuccess(String chooseCourseId);

    /**
     * @description 我的课程表
     * @param params
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.learning.model.po.XcCourseTables>
     */
    public PageResult<XcCourseTables> mycoursetables(MyCourseTableParams params);

}
