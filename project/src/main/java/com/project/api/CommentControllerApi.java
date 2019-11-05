package com.project.api;

import com.project.pojo.Comment;
import com.project.pojo.Project;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import com.project.response.CommentNode;

import java.util.List;

@Api(value="获得数据接口",description = "获得数据接口")
public interface CommentControllerApi {
    @ApiOperation("Comment")
    public List<Comment> getList();

    @ApiOperation("Project")
    public List<Project> getProjectList();
}
