package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.model.response.QueryResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="cms模板管理接口",description = "cms模板管理接口")
public interface CmsTemplateControllerApi {
    //模板查询
    @ApiOperation("模板查询页面列表")
    public QueryResult<CmsTemplate> findAll();
}
