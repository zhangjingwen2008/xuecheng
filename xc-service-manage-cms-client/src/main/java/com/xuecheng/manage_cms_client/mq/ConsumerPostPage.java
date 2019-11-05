package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class ConsumerPostPage {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConsumerPostPage.class);

    @Autowired
    PageService pageService;

    @RabbitListener(queues={"${xuecheng.mq.queue}"})
    public void postPage(String msg) throws IOException {
        //解析消息
        Map map = JSON.parseObject(msg, Map.class);
        //得到页面消息的id
        String pageId = (String) map.get("pageId");
        //校验id
        CmsPage cmsPage = pageService.findCmsPageById(pageId);
        if (null == cmsPage) {
            LOGGER.error("receive cms post page,cmsPage is null:{}",msg.toString());
            return;
        }
        //调用service方法将html从GridFS中下载到服务器
        pageService.savePageToServerPath(pageId);
    }
}
