package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */

public interface TeachplanMapper extends BaseMapper<Teachplan> {
    public List<TeachplanDto> selectTreeNodes(long courseId);

    /**
     * 获取改课程父级目录下的课程数量。
     * @param courseId
     * @param parentid
     * @return
     */
    public int getTeachplanCount(@Param("courseId") long courseId, @Param("parentid") long parentid);

    public int getDeleteTag(@Param("courseId") long courseId,@Param("id") long id);

    public Teachplan selectByOrderby(@Param("courseId") long courseId, @Param("parentid") long parentid,@Param("orderby") Integer orderby);

    public List<Teachplan> selectList(@Param("courseId") long courseId, @Param("parentid") long parentid,@Param("orderby") Integer orderby);
}
