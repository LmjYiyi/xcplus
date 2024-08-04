package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.execption.XueChengPlusExecption;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CoursBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    private  CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public PageResult<CourseBase> QueryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件，根据课程名称查询
        queryWrapper.like(org.apache.commons.lang3.StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        //构建查询条件，根据课程审核状态查询
        queryWrapper.eq(org.apache.commons.lang3.StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
//构建查询条件，根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());
//todo:根据课程发布状态查询
        queryWrapper.eq(CourseBase::getCompanyId,companyId);
        //分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseBase> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;

    }

    @Override
    public CourseBaseInfoDto createCourseBase(long companyId, AddCourseDto addCourseDto) {
//        //合法性校验
//        if (StringUtils.isBlank(addCourseDto.getName())){
//            //throw new RuntimeException("课程名称为空");
//            throw new XueChengPlusExecption("课程名称为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getMt())){
////            throw new RuntimeException("课程分类为空");
//            throw new XueChengPlusExecption("课程分类为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getSt())){
//            //throw new RuntimeException("课程分类为空");
//            throw new XueChengPlusExecption("课程分类为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getGrade())){
//            throw new RuntimeException("课程等级为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getTeachmode())){
//            throw new RuntimeException("教育模式为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getUsers())){
//            throw new RuntimeException("适应人群为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getCharge())){
//            throw new RuntimeException("收费规则为空");
//        }

        //新增对象
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto,courseBase);

        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        courseBase.setCompanyId(companyId);

        courseBase.setCreateDate(LocalDateTime.now());


        int insert = courseBaseMapper.insert(courseBase);
        if (insert<=0){
            throw new RuntimeException("新增课程信息失败");
        }

        //往营销表中保存营销信息。
        CourseMarket courseMarket = new CourseMarket();
        Long courseId = courseBase.getId();
        BeanUtils.copyProperties(addCourseDto,courseMarket);
        courseMarket.setId(courseId);

        int i = saveCourseMarket(courseMarket);
        if (i<=0){
            throw new RuntimeException("保存课程营销信息失败");
        }

        return getCourseBaseInfo(courseId);
    }



    private int saveCourseMarket(CourseMarket courseMarket) {
        //参数合法性校验
        String charge = courseMarket.getCharge();
        if (StringUtils.isBlank(charge)){
            throw new RuntimeException("收费规则没有选择");
        }
        if (charge.equals("201001")){
            if (courseMarket.getPrice() == null || courseMarket.getPrice().floatValue()<=0){
                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
            }
        }

        //从数据库中查询营销信息，存在则更新，不存在则添加
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarket.getId());
        if (courseMarketObj==null){
            int insert = courseMarketMapper.insert(courseMarket);
            return insert;
        }else{
            BeanUtils.copyProperties(courseMarket,courseMarketObj);
            courseMarketObj.setId(courseMarket.getId());
            int i = courseMarketMapper.updateById(courseMarketObj);
            return i;
        }


    }
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
              return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if (courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }
        //查询分类名称。
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());

        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        Long id = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if (courseBase==null){
             XueChengPlusExecption.cast("课程不存在");
        }
        //校验：只有本机构才能修改本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusExecption.cast("本机构只能修改本机构的课程");
        }

        //封装课程基本数据
        BeanUtils.copyProperties(editCourseDto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());

        //更新数据库数据-课程基本数据
        int i = courseBaseMapper.updateById(courseBase);

        //封装营销数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        saveCourseMarket(courseMarket);

        //展示数据
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        return courseBaseInfoDto;
    }

    @Override
    public void deleteCourseBase(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        String auditStatus = courseBase.getAuditStatus();
        if (!auditStatus.equals("202002")){
            //只有未发布状态的课程才能删除。
            XueChengPlusExecption.cast("只有未发布状态的课程才能删除");

        }
//        if (!courseBase.getCompanyId().equals(companyId)) {
//            XueChengPlusException.cast("本机构只能删除本机构课程");
//        }
        //删除课程基本信息
        //删除课程老师信息
        //删除课程计划
        //删除课程营销
        //删除媒资信息
        courseBaseMapper.deleteById(courseId);
        courseMarketMapper.deleteById(courseId);


        if (teachplanMapper.selectById(courseId)!=null){
            LambdaQueryWrapper<Teachplan> lambdaQueryWrapper_teachPlan = new LambdaQueryWrapper<>();
            lambdaQueryWrapper_teachPlan.eq(Teachplan::getCourseId,courseId);
            teachplanMapper.delete(lambdaQueryWrapper_teachPlan);
        }

        if (courseTeacherMapper.selectById(courseId)!=null){
            LambdaQueryWrapper<CourseTeacher> lambdaQueryWrapper_coureTeacher  = new LambdaQueryWrapper<>();
            lambdaQueryWrapper_coureTeacher.eq(CourseTeacher::getCourseId,courseId);
            courseTeacherMapper.delete(lambdaQueryWrapper_coureTeacher);
        }


        if (teachplanMediaMapper.selectById(courseId)!=null){
            LambdaQueryWrapper<TeachplanMedia> lambdaQueryWrapper_teachplanMedia = new LambdaQueryWrapper<>();
            lambdaQueryWrapper_teachplanMedia.eq(TeachplanMedia::getCourseId,courseId);
            teachplanMediaMapper.delete(lambdaQueryWrapper_teachplanMedia);

        }



    }

}
