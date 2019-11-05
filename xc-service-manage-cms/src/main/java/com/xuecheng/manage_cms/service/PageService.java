package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.*;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.netty.util.internal.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    CmsConfigRepository cmsConfigRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;
    @Autowired
    RabbitTemplate rabbitTemplate;

    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        if(queryPageRequest==null)
            queryPageRequest = new QueryPageRequest();
        CmsPage cms = new CmsPage();
        if(StringUtils.isNotEmpty(queryPageRequest.getSiteId()))
            cms.setSiteId(queryPageRequest.getSiteId());
        if(StringUtils.isNotEmpty(queryPageRequest.getTemplateId()))
            cms.setTemplateId(queryPageRequest.getTemplateId());
        if(StringUtils.isNotEmpty(queryPageRequest.getPageId()))
            cms.setPageId(queryPageRequest.getPageId());
        if(StringUtils.isNotEmpty(queryPageRequest.getPageName()))
            cms.setPageName(queryPageRequest.getPageName());
        if(StringUtils.isNotEmpty(queryPageRequest.getPageAliase()))
            cms.setPageAliase(queryPageRequest.getPageAliase());
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<CmsPage> example = Example.of(cms, exampleMatcher);

        if (page <= 0)
            page = 1;
        page = page - 1;
        if (size <= 0)
            size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);
        QueryResult<CmsPage> queryResult = new QueryResult<>();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

    //增加页面
    public CmsPageResult add(CmsPage cmsPage) {
        if (cmsPage == null) {
            //抛出参数异常
        }
        CmsPage cmsPage1 = cmsPageRepository.findBySiteIdAndPageNameAndPageWebPath(cmsPage.getSiteId(), cmsPage.getPageName(), cmsPage.getPageWebPath());
        if (cmsPage1 != null) {
            //抛出页面已存在的异常
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        cmsPage.setPageId(null);
        CmsPage cms=cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS, cms);
    }

    //根据id获得页面
    public CmsPage getById(String id) {
        Optional<CmsPage> cmsPage = cmsPageRepository.findById(id);
        if (cmsPage.isPresent()) {
            return cmsPage.get();
        }
        return null;
    }

    //修改页面
    public CmsPageResult update(String id, CmsPage cmsPage) {
        CmsPage one = this.getById(id);
        if (one != null) {
            //更新模板id
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataUrl
            one.setDataUrl(cmsPage.getDataUrl());
            //保存
            cmsPageRepository.save(one);
            return new CmsPageResult(CommonCode.SUCCESS, one);
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    //删除页面
    public ResponseResult delete(String id) {
        Optional<CmsPage> cmsPage = cmsPageRepository.findById(id);
        if (cmsPage.isPresent()) {
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //获得页面配置
    public CmsConfig getConfigById(String id) {
        Optional<CmsConfig> optinal = cmsConfigRepository.findById(id);
        if (optinal.isPresent()) {
            CmsConfig config = optinal.get();
            return config;
        }
        return null;
    }

    //页面发布
    public ResponseResult post(String pageId) {
        //1.执行静态化
        String pageHtml = this.getPageHtml(pageId);
        if (StringUtils.isEmpty(pageHtml)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }

        //2.保存静态化文件
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);

        //3.发送消息
        sentPostPage(pageId);

        return new ResponseResult(CommonCode.SUCCESS);
    }


    /**
     * 静态化页面
     *
     * 1.获取模型数据
     * 2.获取页面模板
     * 3.静态化页面
     */
    public String getPageHtml(String pageId) {
        //1.模型数据
        Map model = getModelByPageId(pageId);
        if (null == model) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }

        //2.获取页面模板
        String templateByPageId = getTemplateByPageId(pageId);
        if (null == templateByPageId) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }

        //3.页面静态化
        String html = generateHtml(templateByPageId, model);
        return html;
    }

    //页面静态化
    private String generateHtml(String templateContent, Map model) {
        //创建配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //创建模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateContent);
        //向configuration中配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        try {
            //获取模板
            Template template = configuration.getTemplate("template");
            //调用api执行静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取页面模板
    private String getTemplateByPageId(String id) {
        //根据id获得cmsPage对象
        CmsPage cmsPage = this.getById(id);
        if (cmsPage == null)
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);

        //获得templateId
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId))
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);

        //根据模板id，获得template对象
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()) {
            CmsTemplate cmsTemplate = optional.get();
            //获得templateFileId
            String templateFileId = cmsTemplate.getTemplateFileId();

            //获得GridFS里模板文件
            //根据文件id查询文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开一个下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFsResource对象，获取流
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            //从流中获取数据
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    //获取模型数据
    private Map getModelByPageId(String id) {

        //根据id获得cmsPage对象
        CmsPage cmsPage = this.getById(id);
        if (null == cmsPage) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }

        //得到dataUrl
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }

        //使用restTemplate请求dataUrl获得数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();

        return body;
    }

    //保存静态化文件
    public CmsPage saveHtml(String pageId, String htmlContent) {
        //得到页面信息
        CmsPage cmsPage = this.getById(pageId);
        if(null==cmsPage)
            ExceptionCast.cast(CommonCode.INVALID_PARAM);

        Object objectId=null;
        try {
            //将htmlContent转换为输入流
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            //将html保存到GridFs
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //更新了htmlId后的CmsPage保存
        cmsPage.setHtmlFileId(objectId.toString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }

    //向mq发送消息
    public void sentPostPage(String pageId) {
        //得到页面信息
        CmsPage cmsPage = this.getById(pageId);
        if(null==cmsPage)
            ExceptionCast.cast(CommonCode.INVALID_PARAM);

        //创建消息对象
        Map<String, String> map = new HashMap<>();
        map.put("pageId", pageId);
        //将对象转换成json对象
        String jsonString = JSON.toJSONString(map);

        //发送给mq
        String siteId = cmsPage.getSiteId();
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, siteId, jsonString);
    }

    //保存页面
    public CmsPageResult save(CmsPage cmsPage) {
        CmsPage one = cmsPageRepository.findBySiteIdAndPageNameAndPageWebPath(cmsPage.getSiteId(), cmsPage.getPageName(), cmsPage.getPageWebPath());
        if (null != one) {
            return this.update(one.getPageId(), cmsPage);
        }
        return this.add(cmsPage);
    }

    //页面发布
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //1.添加页面
        CmsPageResult cmsPageResult = this.save(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        CmsPage cms = cmsPageResult.getCmsPage();
        String pageId = cms.getPageId();

        //2.静态化，发布页面
        ResponseResult responseResult = this.post(pageId);
        if (!responseResult.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }

        //3.拼接url=站点域名+站点webpath+页面webpath+页面名称
        //获得站点信息
        String siteId = cms.getSiteId();
        CmsSite cmsSite = this.getCmsSiteById(siteId);
        String url = cmsSite.getSiteDomain() + cmsSite.getSiteWebPath() + cmsPage.getPageWebPath() + cmsPage.getPageName();

        return new CmsPostPageResult(CommonCode.SUCCESS, url);
    }

    public CmsSite getCmsSiteById(String pageId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(pageId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
