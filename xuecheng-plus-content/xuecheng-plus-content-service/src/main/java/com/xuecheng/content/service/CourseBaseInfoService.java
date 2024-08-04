package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

public interface CourseBaseInfoService {
    PageResult<CourseBase> QueryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
    CourseBaseInfoDto createCourseBase(long companyId, AddCourseDto addCourseDto);

    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);

    void deleteCourseBase(Long courseId);
}
