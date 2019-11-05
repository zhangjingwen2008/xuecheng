package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(value="cms站点管理接口",description = "cms站点管理接口")
public interface CmsSiteControllerApi {
    //站点查询
    @ApiOperation("站点查询页面列表")
    public QueryResponseResult findAll();
}
