package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="课程管理接口",description = "课程管理接口，提供页面的增删改差")
public interface CourseControllerApi {

    @ApiOperation("查询课程列表")
    public QueryResponseResult findCourseList(int page, int size, CourseListRequest courseListRequest);

    @ApiOperation("添加课程基础信息")
    public AddCourseResult addCourseBase(CourseBase courseBase);

    @ApiOperation("获得课程基础信息")
    public CourseBase getCourseBaseById(String courseId);

    @ApiOperation("修改课程基础信息")
    public ResponseResult updateCourseBase(String courseId, CourseBase courseBase);

    @ApiOperation("课程计划查询")
    public TeachplanNode findTeachplanList(String courseId);

    @ApiOperation("课程计划添加")
    public ResponseResult addTeachplan(Teachplan teachplan);

    @ApiOperation("课程分类查询")
    public CategoryNode findCategoryList();

    @ApiOperation("获得课程营销信息")
    public CourseMarket getCourseMarketById(String courseId);

    @ApiOperation("修改课程营销信息")
    public ResponseResult updateCourseMarket(String courseId, CourseMarket courseMarket);

    @ApiOperation("添加课程图片")
    public ResponseResult addCoursePic(String courseId, String pic);

    @ApiOperation("查询课程图片")
    public CoursePic findCoursePic(String courseId);

    @ApiOperation("删除课程图片")
    public ResponseResult deleteCoursePic(String courseId);

    @ApiOperation("获取课程视图")
    public CourseView courseview(String id);

    //课程预览
    @ApiOperation("预览课程")
    public CoursePublishResult preview(String id);

    //课程发布
    @ApiOperation("课程发布")
    public CoursePublishResult publish(String id);

    @ApiOperation("保存课程计划与媒资文件的关联")
    public ResponseResult savemedia(TeachplanMedia teachplanMedia);

}
