package com.xuecheng.media;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class testHandler {
    @Test
    @XxlJob("demoJobHandler")
    public void demoJobHandler() throws Exception {
        System.out.println("处理视频.......");
        log.debug("处理视频");

    }
    @Test
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();//执行器的序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal();//执行器总数

        System.out.println("shardIndex="+shardIndex+",shardTotal="+shardTotal);

    }
}
