package com.project.response;

import com.project.pojo.Comment;
import lombok.Data;

import java.util.List;

@Data
public class CommentNode extends Comment {
    List<CommentNode> children;
}
