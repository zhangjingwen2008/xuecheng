package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.learning.XcLearningCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XcLearningCourseRepository extends JpaRepository<XcLearningCourse, String> {
    //根据课程id和用户id查询记录
    XcLearningCourse findByUserIdAndCourseId(String userId, String courseId);
}
