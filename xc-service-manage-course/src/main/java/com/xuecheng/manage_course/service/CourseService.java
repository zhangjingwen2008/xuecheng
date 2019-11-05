package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.netflix.discovery.converters.Auto;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.cms.response.CoursePreviewResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CoursePubRepository coursePubRepository;
    @Autowired
    CmsPageClient cmsPageClient;
    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;


    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;


    public TeachplanNode selectList(String courseId) {
        return teachplanMapper.selectList(courseId);
    }

    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (null == teachplan || StringUtils.isEmpty(teachplan.getCourseid()) || StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        //当前课程计划
        String courseid = teachplan.getCourseid();
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)) {        //若无根节点，则新建根节点
            parentid = this.getTeachplanRoot(courseid);
        }

        //获得父节点的Grade，用于新节点的Grade
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parendNode = optional.get();
        String grade = parendNode.getGrade();

        //新节点
        Teachplan teachplanNew = new Teachplan();
        BeanUtils.copyProperties(teachplan, teachplanNew);
        teachplanNew.setCourseid(courseid);
        teachplanNew.setParentid(parentid);
        if (grade.equals("1")) {        //级别要根据父节点来设置
            teachplanNew.setGrade("2");
        }else{
            teachplanNew.setGrade("3");
        }
        teachplanRepository.save(teachplanNew);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //获取课程根节点列表，若无根节点则新建根节点
    private String getTeachplanRoot(String courseId){
        //获得课程信息
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            return null;
        }
        CourseBase courseBase = optional.get();

        //获得课程根节点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList == null || teachplanList.size() <= 0) {
            //查询不到，新建节点
            Teachplan teachplan = new Teachplan();
            teachplan.setGrade("1");
            teachplan.setParentid("0");
            teachplan.setStatus("0");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(courseBase.getId());
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }

        //返回根节点id
        return teachplanList.get(0).getId();
    }

    //查询所有分类列表
    public CategoryNode findCategoryList() {
        return categoryMapper.selectList();
    }

    //增加课程基本信息
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS, courseBase.getId());
    }

    //获得课程基本信息
    public CourseBase getCourseBaseById(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    //修改课程基本信息
    @Transactional
    public ResponseResult updateCourseBase(String courseId, CourseBase courseBase) {
        CourseBase one = this.getCourseBaseById(courseId);
        if(null==one){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //修改课程信息
        one.setName(courseBase.getName());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setUsers(courseBase.getUsers());
        one.setDescription(courseBase.getDescription());
        courseBaseRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //获得课程营销信息
    public CourseMarket getCourseMarketById(String courseId) {
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    //保存课程图片信息
    @Transactional
    public ResponseResult saveCoursePic(String courseId, String pic) {
        CoursePic coursePic = null;
        //获得课程图片信息
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()) {
            coursePic = optional.get();
        }
        if (coursePic == null) {
            coursePic = new CoursePic();
        }
        //将课程图片信息保存进数据库
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //修改课程营销信息
    @Transactional
    public ResponseResult updateCourseMarket(String courseId, CourseMarket courseMarket) {
        CourseMarket one = this.getCourseMarketById(courseId);
        if (null == one) {
            one = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, one);
            one.setId(courseId);
            courseMarketRepository.save(one);
        }else{
            one.setCharge(courseMarket.getCharge());
            one.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
            one.setEndTime(courseMarket.getEndTime());//课程有效期，结束时间
            one.setPrice(courseMarket.getPrice());
            one.setQq(courseMarket.getQq());
            one.setValid(courseMarket.getValid());
            courseMarketRepository.save(one);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程列表
    public QueryResponseResult findCourseList(String company_id, int page, int size, CourseListRequest courseListRequest) {
        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }
        courseListRequest.setCompanyId(company_id);
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);

        QueryResult<CourseInfo> courseInfoQueryResult = new QueryResult<CourseInfo>();
        List<CourseInfo> list = courseListPage.getResult();
        long total = courseListPage.getTotal();
        courseInfoQueryResult.setList(list);
        courseInfoQueryResult.setTotal(total);
        return new QueryResponseResult(CommonCode.SUCCESS,courseInfoQueryResult);
    }

//    //(原)查询课程列表
//    public QueryResponseResult findCourseList(String company_id, int page, int size, CourseListRequest courseListRequest) {
//
//        if (null == courseListRequest) {
//            courseListRequest = new CourseListRequest();
//        }
//        if (page <= 0) {
//            page = 0;
//        }
//        if (size <= 0) {
//            size = 20;
//        }
//        //设置分页参数
//        PageHelper.startPage(page, size);
//        //分页查询
//        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
//
//        QueryResult<CourseInfo> queryResult = new QueryResult<>();
//        queryResult.setList(courseListPage.getResult());
//        queryResult.setTotal(courseListPage.getTotal());
//        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
//        return queryResponseResult;
//    }

    //查询图片信息
    public CoursePic findCoursePic(String courseId) {
        Optional<CoursePic> coursePic = coursePicRepository.findById(courseId);
        if (!coursePic.isPresent()) {
            return null;
        }
        return coursePic.get();
    }

    //删除课程图片
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        long result = coursePicRepository.deleteByCourseid(courseId);
        if(result>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //课程视图
    public CourseView courseView(String id) {
        CourseView courseView = new CourseView();

        //基本信息
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (optional.isPresent()) {
            courseView.setCourseBase(optional.get());
        }

        //课程图片
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(id);
        if (optionalCoursePic.isPresent()) {
            courseView.setCoursePic(optionalCoursePic.get());
        }

        //课程价格
        Optional<CourseMarket> optionalCourseMarket = courseMarketRepository.findById(id);
        if (optionalCourseMarket.isPresent()) {
            courseView.setCourseMarket(optionalCourseMarket.get());
        }

        //课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;
    }

    //课程预览
    public CoursePublishResult preview(String id) {
        //请求cms添加页面
        CourseBase courseBase = this.findCourseBaseById(id);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        cmsPage.setTemplateId(publish_templateId);
        cmsPage.setPageName(id+".html");
        cmsPage.setPageAliase(courseBase.getName());
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setDataUrl(publish_dataUrlPre+id);

        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();

        //拼装页面预览的url
        String url = previewUrl + pageId;

        //返回CoursePublishResult对象（包含了预览页面url）
        return new CoursePublishResult(CommonCode.SUCCESS, url);
    }

    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        return null;
    }

    //课程发布
    public CoursePublishResult publish(String id) {

        //请求cms添加页面
        CourseBase courseBaseById = this.findCourseBaseById(id);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        cmsPage.setTemplateId(publish_templateId);
        cmsPage.setPageName(id+".html");
        cmsPage.setPageAliase(courseBaseById.getName());
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setDataUrl(publish_dataUrlPre+id);

        //调用远程Cms服务，进行一键发布
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (null == cmsPostPageResult) {
            ExceptionCast.cast(CommonCode.FAIL);
        }

        //更改发布状态
        CourseBase courseBase = this.saveStatus(id);
        if (null == courseBase) {
            ExceptionCast.cast(CommonCode.FAIL);
        }

        //保存课程索引信息
        CoursePub coursePub = createCoursePub(id);
        //将CoursePub保存到数据库
        saveCoursePub(id, coursePub);

        //缓存课程的信息

        //获得发布后的url
        String url = cmsPostPageResult.getPageUrl();

        //向teachplanMediaPub中保存媒资信息
        saveTeachplanMediaPub(id);
        return new CoursePublishResult(CommonCode.SUCCESS, url);
    }

    //向teachplanMediaPub中保存媒资信息
    private void saveTeachplanMediaPub(String courseId) {
        //先删除TeachplanMediaPub中的数据
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        //获得TeachplanMedia数据
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        for (TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);
            //添加时间戳
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubs.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubs);
    }

    //将CoursePub保存到数据库
    private CoursePub saveCoursePub(String id, CoursePub coursePub) {
        CoursePub coursePubNew = null;
        //根据课程ID查询CoursePub
        Optional<CoursePub> optional = coursePubRepository.findById(id);
        if (optional.isPresent()) {
            coursePubNew = optional.get();
        }else{
            coursePubNew = new CoursePub();
        }

        //将CoursePub保存到CoursePubNew中
        BeanUtils.copyProperties(coursePubNew, coursePub);
        coursePubNew.setId(id);
        //时间戳
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }

    //创建coursePub对象
    private CoursePub createCoursePub(String id) {
        CoursePub coursePub = new CoursePub();

        //根据id查询coursePub
        Optional<CourseBase> optionalCoursePub = courseBaseRepository.findById(id);
        if (optionalCoursePub.isPresent()) {
            BeanUtils.copyProperties(coursePub, optionalCoursePub.get());
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }
        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }

        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);
        return coursePub;
    }

    //更改课程的状态为“已发布” 202002
    private CourseBase saveStatus(String courseId) {
        CourseBase courseBase = this.findCourseBaseById(courseId);
        courseBase.setStatus("202002");
        courseBaseRepository.save(courseBase);
        return courseBase;
    }

    //保存课程计划与媒资文件的关联
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if (null == teachplanMedia || StringUtils.isEmpty(teachplanMedia.getTeachplanId())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //校验课程计划是否是3级
        String teachplanId = teachplanMedia.getTeachplanId();
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //查询到教学计划
        Teachplan teachplan = optional.get();
        //取出等级
        String grade = teachplan.getGrade();
        if (null == grade || !grade.equals("3")) {
            //只允许第三级的课程计划关联视频
            ExceptionCast.cast(CourseCode.COURSE_MEDIS_TEACHPLAN_GRADEERROR);
        }

        //查询TeachplanMedia
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia one = null;
        if (mediaOptional.isPresent()) {
            one = mediaOptional.get();
        } else {
            one = new TeachplanMedia();
        }

        //将one保存到数据库
        one.setCourseId(teachplan.getCourseid());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        one.setTeachplanId(teachplanMedia.getTeachplanId());
        teachplanMediaRepository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);
    }
}
