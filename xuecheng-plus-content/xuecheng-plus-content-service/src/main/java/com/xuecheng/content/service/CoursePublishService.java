package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

public interface CoursePublishService {
    /**
     * 获取课程预览信息。
     * @param courseId
     * @return
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核。
     * @param companyId
     * @param courseId
     */
    public void commitAudit(Long companyId,Long courseId);

    /**
     * @description 课程发布接口
     * @param companyId 机构id
     * @param courseId 课程id
     * @return void
     */
    public void publish(Long companyId,Long courseId);
    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     */
    public File generateCourseHtml(Long courseId);
    /**
     * @description 上传课程静态化页面
     * @param file  静态化文件
     * @return void
     */
    public void  uploadCourseHtml(Long courseId,File file);

    /**
     * 查询课程发布信息
     * @param courseId
     */
    public CoursePublish getCoursePublish(Long courseId);


}
