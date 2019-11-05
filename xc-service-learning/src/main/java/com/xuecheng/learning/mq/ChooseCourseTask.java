package com.xuecheng.learning.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.dao.XcLearningCourseRepository;
import com.xuecheng.learning.service.LearningService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class ChooseCourseTask {

    @Autowired
    LearningService learningService;
    @Autowired
    RabbitTemplate rabbitTemplate;
//    @Autowired
//    XcLearningCourseRepository xcLearningCourseRepository;

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE)
    public void receiveChoosecourseTask(XcTask xcTask) {
        //从request获得userid和courseid
        String request = xcTask.getRequestBody();
        Map<String, String> map = JSON.parseObject(request, Map.class);
        String userId = map.get("userId");
        String courseId = map.get("courseId");
        //获得valid、startTime、endTime参数
//        XcLearningCourse xcLearningCourse = xcLearningCourseRepository.findByUserIdAndCourseId(userId, courseId);
//        String valid = xcLearningCourse.getValid();
//        Date startTime = xcLearningCourse.getStartTime();
//        Date endTime = xcLearningCourse.getEndTime();

        //添加选课
        //String userId, String courseId, String valid, Date startTime, Date endTime, XcTask xcTask
        ResponseResult result = learningService.addCourse(userId, courseId, null, null, null, xcTask);

        //完成选课，向mq发送消息
        if (result.isSuccess()) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE, RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY, xcTask);
        }
    }

}
