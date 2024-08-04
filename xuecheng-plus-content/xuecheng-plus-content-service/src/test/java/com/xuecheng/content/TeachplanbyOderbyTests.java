package com.xuecheng.content;


import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.po.Teachplan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
public class TeachplanbyOderbyTests {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Test
    public void testSelectByOrderby(){
        Teachplan teachplan = teachplanMapper.selectByOrderby(74L, 113L, 2);
        System.out.println("teachplan = " + teachplan);
    }

    @Test
    public void testSelectList(){
        List<Teachplan> teachplans =
                teachplanMapper.selectList(74L, 242L, 2);
        System.out.println("teachplans = " + teachplans.toString());
    }

}
