package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.execption.CommonError;
import com.xuecheng.base.execption.XueChengPlusExecption;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service

public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    TeachplanService teachplanService;
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);

        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;
    }

    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {

        CoursePublishPre coursePublishPre = new CoursePublishPre();

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase.getAuditStatus().equals("202003")){
            XueChengPlusExecption.cast("当前为审核状态，审核完可以再次提交");
        }
        if (!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusExecption.cast("不允许提交其他机构的课程");
        }
        if (StringUtils.isEmpty(courseBase.getPic())){
            XueChengPlusExecption.cast("请提交课程图片");
        }

        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);

        coursePublishPre.setMarket(courseMarketJson);

        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree.size()<=0){
            XueChengPlusExecption.cast("还没有课程计划");
        }
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);


        coursePublishPre.setStatus("202003");
        coursePublishPre.setCompanyId(companyId);
        coursePublishPre.setCreateDate(LocalDateTime.now());

        //查询课程预发布表，有则更新，没有则插入。
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreUpdate==null){
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本信息表和审核状态。
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre==null){
            XueChengPlusExecption.cast("请先提交课程审核");
        }
        if (!coursePublishPre.getCompanyId().equals(companyId)){
            XueChengPlusExecption.cast("不可以提交其他机构的课程");
        }

        if (!coursePublishPre.getStatus().equals("202004")){
            XueChengPlusExecption.cast("发布失败，审核通过才可以发布");
        }

        //保存消息表。
        saveCoursePublish(courseId);
        //保存消息表。
        saveCoursePublishMessage(courseId);
        //删除课程预发布信息记录。
        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public File generateCourseHtml(Long courseId) {

        //静态化文件
        File htmlFile  = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course",".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.toString());
            XueChengPlusExecption.cast("课程静态化异常");
        }

        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.uploadFile(multipartFile, "course/"+courseId+".html");
        if(course==null){
            XueChengPlusExecption.cast("上传静态文件异常");
        }
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId){
//        return  coursePublishMapper.selectById(courseId);
        //1.redis缓存，先查缓存，没有再查数据库。
        // 缺点：当并发很大的时候，很多线程会同时请求到数据库，对于不存在的null值会一直请求数据库，发生缓存穿透的现象。
//        Object obj = redisTemplate.opsForValue().get("course" + courseId);
//        if (obj!=null){
//            System.out.println("缓存");
//            String objString = obj.toString();
//            CoursePublish coursePublish = JSON.parseObject(objString, CoursePublish.class);
//            return coursePublish;
//        }else{
//            System.out.println("数据库");
//            CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
//            if (coursePublish!=null){
//                redisTemplate.opsForValue().set("course"+courseId,JSON.toJSONString(coursePublish));
//            }
//            return coursePublish;
//        }
        //2.使用布隆过滤器,或设置特殊值如下：
//        Object jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
//        if (jsonObj != null) {
//            String jsonString = jsonObj.toString();
//            // 检查缓存中的字符串是否为 "null"
//            if (jsonString.equals("null")){
//                return null;
//            }
//            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
//            return coursePublish;
//        } else {
//            // 从数据库查询
//            System.out.println("从数据库查询数据...");
//            CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
//            // 仅在非空时缓存结果
//            if (coursePublish != null) {
//                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish), 30, TimeUnit.SECONDS);
//            } else {
//                // 缓存空值避免缓存穿透
//                redisTemplate.opsForValue().set("course:" + courseId, "null", 30, TimeUnit.SECONDS);
//            }
//            return coursePublish;
//        }
        //当缓存中大量的key失效了，高并发下请求数据库，导致数据库资源耗尽，无法正常工作。这现象称为缓存雪崩，造成的原因是大量的key过期时间相同，当缓存的key同时失效时出现缓存雪崩问题。
        //解决的问题有，使用同步锁限制只有一个线程访问数据库，查询到数据存入缓存。
//        Object  jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
//        if(jsonObj!=null){
//            String jsonString = jsonObj.toString();
//            if(jsonString.equals("null")){
//                return null;
//            }
//            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
//            return coursePublish;
//        }else{
//            synchronized(this){
//                //再次查一下缓存。防止上一个拿到锁的存入了缓存释放锁，不要直接查询数据库，避免多次查询数据库。
//                Object  Obj = redisTemplate.opsForValue().get("course:" + courseId);
//                if(Obj!=null){
//                    String jsonString = Obj.toString();
//                    if(jsonString.equals("null")){
//                        return null;
//                    }
//                    CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
//                    return coursePublish;
//                }
//                //从数据库查询
//                CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
//                //设置过期时间300秒
//                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish),300+new Random().nextInt(100), TimeUnit.SECONDS);
//                return coursePublish;
//            }
//        }

        // 对于同一类型的信息，设置不同的过期时间。
        //redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish),300+new Random().nextInt(100), TimeUnit.SECONDS);缓存预热。
        //缓存击穿：对于热点数据的缓存，缓存过期了，高并发访问热点数据，同时访问数据库，导致数据库资源耗尽。

        //多个虚拟机部署的情况下，同步锁只能对当前JVM有效，无法保证分布式部署只查一次数据库。
        //redis的setnx来实现分布式锁。
        // 缺点：1.过期时间不好设置，过期时间太短，还没执行完查找数据库让别的线程抢到锁，会多次查询数据库。过期时间过长，效率会下降。
        //     2.手动释放锁，但手动释放锁与过期自动删除可能会存在冲突。执行查询数据库时，key过期了，手动删锁把别的线程的锁给删除了。解决方案是判断锁是否为当前线程设置的然后删除，需要使用lua脚本保证原子性。

        //使用redisson实现分布式锁。redisson封装了lua脚本，保证删锁的原子性。同时启用了看门狗线程，对于抢到锁没有执行完任务的自动续期。

        Object obj = redisTemplate.opsForValue().get("course:" + courseId);
        if (obj!=null){
            String objString = obj.toString();
            if (objString.equals("null")){
                return null;
            }
            CoursePublish coursePublish = JSON.parseObject(objString, CoursePublish.class);
            return coursePublish;
        }else {
            RLock lock = redissonClient.getLock("coursequerylock:" + courseId);
            lock.lock();
            try {
                Object  Obj = redisTemplate.opsForValue().get("course:" + courseId);
                if(Obj!=null){
                    String jsonString = Obj.toString();
                    if(jsonString.equals("null")){
                        return null;
                    }
                    CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
                    return coursePublish;
                }
                System.out.println("----DB----");
                CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
                // 仅在非空时缓存结果
                if (coursePublish != null) {
                    redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish), 30+new Random().nextInt(10), TimeUnit.SECONDS);
                } else {
                    // 缓存空值避免缓存穿透
                    redisTemplate.opsForValue().set("course:" + courseId, "null", 30+new Random().nextInt(10), TimeUnit.SECONDS);
                }
                return coursePublish;

            }finally {
                lock.unlock();
            }
        }

    }

    private void saveCoursePublish(Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre==null){
            XueChengPlusExecption.cast("预发布信息为空");
        }

        CoursePublish coursePublish = new CoursePublish();

        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUdate = coursePublishMapper.selectById(courseId);
        if (coursePublishUdate==null){
            coursePublishMapper.insert(coursePublish);
        }else {
            coursePublishMapper.updateById(coursePublish);
        }

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
    }

    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage("course_publish",
                String.valueOf(courseId),null,null);
        if (mqMessage == null){
            XueChengPlusExecption.cast(CommonError.UNKOWN_ERROR);
        }
    }



}
