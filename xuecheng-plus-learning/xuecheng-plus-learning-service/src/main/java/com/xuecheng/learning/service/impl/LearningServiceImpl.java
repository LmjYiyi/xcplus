package com.xuecheng.learning.service.impl;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Mr.M
 * @version 1.0
 * @description 在线学习接口
 * @date 2023/2/28 10:20
 */
@Slf4j
@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        try {
            //查询课程信息
            CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
            //判断如果为null不再继续
            if (coursepublish == null) {
                log.error("Course with ID {} does not exist", courseId);
                return RestResponse.validfail("课程不存在");
            }

            //用户已登录
            if (StringUtils.isNotEmpty(userId)) {
                //获取学习资格
                XcCourseTablesDto learningStatus = myCourseTablesService.getLearningStatus(userId, courseId);
                if (learningStatus == null) {
                    log.error("Learning status is null for userId: {} and courseId: {}", userId, courseId);
                    return RestResponse.validfail("无法获取学习状态");
                }
                //学习资格，[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
                String learnStatus = learningStatus.getLearnStatus();
                if ("702002".equals(learnStatus)) {
                    return RestResponse.validfail("无法学习，因为没有选课或选课后没有支付");
                } else if ("702003".equals(learnStatus)) {
                    return RestResponse.validfail("已过期需要申请续期或重新支付");
                } else {
                    //有资格学习，要返回视频的播放地址
                    //程调用媒资获取视频播放地址
                    RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                    if (playUrlByMediaId == null || playUrlByMediaId.getResult() == null) {
                        log.error("Failed to get play URL for mediaId: {}", mediaId);
                        return RestResponse.validfail("无法获取视频播放地址");
                    }
                    return playUrlByMediaId;
                }
            }

            //如果用户没有登录
            //取出课程的收费规则
            String charge = coursepublish.getCharge();
            if ("201000".equals(charge)) {
                //有资格学习，要返回视频的播放地址
                //远程调用媒资获取视频播放地址
                RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                if (playUrlByMediaId == null || playUrlByMediaId.getResult() == null) {
                    log.error("Failed to get play URL for mediaId: {}", mediaId);
                    return RestResponse.validfail("无法获取视频播放地址");
                }
                return playUrlByMediaId;
            }
            return RestResponse.validfail("课程需要购买");
        } catch (Exception e) {
            log.error("Exception in getVideo method", e);
            return RestResponse.validfail("系统异常");
        }
    }
}
