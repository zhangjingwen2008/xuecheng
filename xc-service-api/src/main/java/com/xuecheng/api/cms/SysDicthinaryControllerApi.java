package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.system.SysDictionary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "数据字典配置接口", description = "数据字典配置接口")
public interface SysDicthinaryControllerApi {
    //根据type查询数据配置
    @ApiOperation("数据字典查询接口")
    public SysDictionary getByType(String type);
}
