package com.project.service;

import com.project.dao.CommentMapper;
import com.project.pojo.Comment;
import com.project.pojo.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentMapper commentMapper;

    public List<Comment> findList() {
        return commentMapper.selectList();
    }

    public List<Comment> sortList(){
        List<Comment> list = commentMapper.selectList();
        List<Comment> result = this.getChildsManyGroup(list, 0);
        return result;
    }

    //将一维数据转换成多维数据
    public List<Comment> getChildsManyGroup(List<Comment> list, int pid){
        List<Comment> arr = new ArrayList<>();
        for(Comment comment : list){        //遍历所有数据
            if(pid == comment.getParentid()){       //当前parentId匹配pid时，代表有子数据，进入递归
                comment.setSubitems(getChildsManyGroup(list, comment.getId()));     //将子项的Id作为下一个子项的parentId去匹配
                arr.add(comment);
            }
        }
        return arr;
    }



    //获取project的列表
    public List<Project> getProjectList() {
        List<Project> projects = commentMapper.selectProjectList();
        List<Project> result = this.getProjectChildren(projects, 0);
        return result;
    }

    public List<Project> getProjectChildren(List<Project> list, int pid) {
        List<Project> arr = new ArrayList<>();
        for (Project p : list) {
            if (pid == p.getParentid()) {
                p.setChildren(getProjectChildren(list, p.getId()));
                arr.add(p);
            }
        }
        return arr;
    }


}
