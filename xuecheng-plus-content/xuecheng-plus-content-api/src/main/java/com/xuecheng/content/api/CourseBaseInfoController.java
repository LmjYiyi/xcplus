package com.xuecheng.content.api;

import com.xuecheng.base.execption.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Api(value = "课程信息管理接口",tags = "课程信息管理接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;


    @ApiOperation("课程分页查询接口")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    @PostMapping("course/list")
    public PageResult<CourseBase> list(PageParams pageParams,@RequestBody QueryCourseParamsDto queryCourseParamsDto){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String companyId = user.getCompanyId();
        PageResult<CourseBase> pageResult = courseBaseInfoService.QueryCourseBaseList(Long.parseLong(companyId),pageParams,queryCourseParamsDto);
        return pageResult;

    }
    @ApiOperation("新增课程基础信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto){
        long companyId = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBaseInfoDto;
    }

    @ApiOperation("课程查询接口")
    @GetMapping("/course/{cousrseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long cousrseId){
       // Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SecurityUtil.XcUser xcUser = SecurityUtil.getUser();
        System.out.println("xcUser = " + xcUser);
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(cousrseId);
        return courseBaseInfo;
    }

    @ApiOperation("修改课程信息接口")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto){
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
        return courseBaseInfoDto;
    }
    @ApiOperation("删除课程信息接口")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourseBase(@PathVariable Long courseId) {
        courseBaseInfoService.deleteCourseBase(courseId);
    }


}
