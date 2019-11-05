package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserReposicotry;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserReposicotry;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    XcUserReposicotry xcUserReposicotry;
    @Autowired
    XcCompanyUserReposicotry xcCompanyUserReposicotry;
    @Autowired
    XcMenuMapper xcMenuMapper;

    //根据用户名称获取XcUser信息
    public XcUser findXcUserByUsername(String username) {
        return xcUserReposicotry.findByUsername(username);
    }

    //根据帐号查询用户信息
    public XcUserExt getUserExt(String username) {
        //获取XcUser信息
        XcUser xcUser = this.findXcUserByUsername(username);
        if (null == xcUser) {
            return null;
        }

        //用户id
        String userId = xcUser.getId();
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);

        //根据用户id查询用户所属公司id
        XcCompanyUser xcCompanyUser = xcCompanyUserReposicotry.findByUserId(userId);
        //排除异常
        String companyId = null;
        if (xcCompanyUser != null) {
            companyId = xcCompanyUser.getCompanyId();
        }

        //构建XcUserExt
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        xcUserExt.setCompanyId(xcCompanyUser.getCompanyId());
        //设置权限
        xcUserExt.setPermissions(xcMenus);

        return xcUserExt;
    }
}
