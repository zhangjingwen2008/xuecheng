package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XcCompanyUserReposicotry extends JpaRepository<XcCompanyUser,String> {
    //根据用户id查询所属公司id
    XcCompanyUser findByUserId(String userId);
}
