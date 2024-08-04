package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

public interface TeachplanService {
    /**
     * 查询课程计划的树形结构
     * @param courseId
     * @return
     */
    public List<TeachplanDto> findTeachplanTree(long courseId);

    /**
     * 保存课程计划
     * @param saveTeachplanDto
     */
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 删除课程计划信息
     * @param teachplanId
     */
    public void deleteTeachplan(Long teachplanId);

    /**
     * 下移课程计划信息
     * @param id
     */
    public void movedownTeachplan(long id);

    /**
     * 上移课程计划信息
     * @param id
     */
    public void  moveupTeachplan(long id);

    /**
     * 绑定媒资和课程计划信息
     * @param bindTeachplanMediaDto
     * @return
     */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

}
