package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.execption.XueChengPlusExecption;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;
    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        Long id = saveTeachplanDto.getId();
        //判断课程计划id是否为空，如果为空则插入，如果不为空则更新
        if (id != null){
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }else{
            Teachplan teachplan = new Teachplan();
            //处理排序字段的取值。
            //先找到同父节的课程计划数
            int teachplanCount = teachplanMapper.getTeachplanCount(saveTeachplanDto.getCourseId(),
                    saveTeachplanDto.getParentid());
            teachplan.setOrderby(teachplanCount+1);
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplanMapper.insert(teachplan);
        }

    }

    @Override
    public void deleteTeachplan(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        int deleteTag = teachplanMapper.getDeleteTag(teachplan.getCourseId(), id);
        List<Teachplan> teachplans = teachplanMapper.selectList(teachplan.getCourseId(), teachplan.getParentid(), teachplan.getOrderby());
        String mediaType = teachplan.getMediaType();
        if (teachplan.getParentid()==0){
            //父目录
            if (deleteTag>0){
                XueChengPlusExecption.cast("存在子信息");
            }else{
                teachplanMapper.deleteById(id);
                teachplanMediaMapper.deleteById(id);
            }
        }else {
            if (mediaType==null){
                //子目录且没有关联视频直接删除
                teachplanMapper.deleteById(id);

            }else {
                //关联了视频,删除视频
                teachplanMediaMapper.deleteById(id);
                teachplanMapper.deleteById(id);

            }
        }
        if (teachplans == null || teachplans.size()==0){
          return;
        }else {
            //如果删除中间数据，则需要更新排序字段。
            for (Teachplan t : teachplans) {
                t.setOrderby(t.getOrderby()-1);
                teachplanMapper.updateById(t);
            }
        }



    }

    @Override
    public void movedownTeachplan(long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);

        Long parentid = teachplan.getParentid();
        Long courseId = teachplan.getCourseId();
        Integer orderbyNow = teachplan.getOrderby();

        int teachplanCount = teachplanMapper.getTeachplanCount(courseId, parentid);
        if (orderbyNow == teachplanCount){
            XueChengPlusExecption.cast("该节无法下移");
        }else {

            Integer orderbyNew = orderbyNow+1;
            Teachplan teachplan_change = teachplanMapper.selectByOrderby(courseId, parentid, orderbyNow + 1);
            teachplan_change.setOrderby(orderbyNow);
            teachplan.setOrderby(orderbyNew);
            teachplanMapper.updateById(teachplan_change);
            teachplanMapper.updateById(teachplan);
        }

    }

    @Override
    public void moveupTeachplan(long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        Integer orderbyNow = teachplan.getOrderby();
        Long parentid = teachplan.getParentid();
        Long courseId = teachplan.getCourseId();

        if (orderbyNow==1){
            XueChengPlusExecption.cast("该节无法上移");
        }else {
            Integer orderbyNew = orderbyNow - 1;
            Teachplan teachplan_change = teachplanMapper.selectByOrderby(courseId, parentid, orderbyNew);
            teachplan_change.setOrderby(orderbyNow);
            teachplan.setOrderby(orderbyNew);

            teachplanMapper.updateById(teachplan_change);
            teachplanMapper.updateById(teachplan);

        }
    }


    @Override
    @Transactional
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplanId==null){
            XueChengPlusExecption.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if (grade!=2){
            XueChengPlusExecption.cast("只允许第二级教学计划和媒资文件绑定");
        }
        Long courseId = teachplan.getCourseId();

        //先删除原来的教学计划绑定的媒资信息。
        teachplanMediaMapper.delete(
                new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,teachplanId)
        );

        //添加绑定关系。
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);

        return teachplanMedia;

    }
}
