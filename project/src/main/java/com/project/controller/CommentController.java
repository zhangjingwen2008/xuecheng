package com.project.controller;

import com.project.api.CommentControllerApi;
import com.project.pojo.Comment;
import com.project.pojo.Project;
import com.project.response.CommentNode;
import com.project.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController implements CommentControllerApi {
    @Autowired
    CommentService commentService;

    @Override
    @GetMapping("/get")
    public List<Comment> getList() {
        return commentService.sortList();
    }

    @Override
    @GetMapping("/getProject")
    public List<Project> getProjectList() {
        return commentService.getProjectList();
    }
}
