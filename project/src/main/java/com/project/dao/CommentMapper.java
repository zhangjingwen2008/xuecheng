package com.project.dao;

import com.project.pojo.Comment;
import com.project.pojo.Project;
import com.project.response.CommentNode;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    //查询所有数据
    public List<Comment> selectList();

    //查询project数据
    public List<Project> selectProjectList();
}
