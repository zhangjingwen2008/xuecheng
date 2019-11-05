package com.project.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="测试Api接口",description = "获得api接口数据")
public interface IotTestApi {
    @ApiOperation("testApi")
    public int getData(int data);
}
