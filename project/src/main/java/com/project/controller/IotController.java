package com.project.controller;

import com.project.api.IotTestApi;
import com.project.service.IotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/testIot")
public class IotController implements IotTestApi {
    @Autowired
    IotService iotService;

    @Override
    @GetMapping("/get")
    public int getData(int data) {
        int result = iotService.getData(data);
        return result;
    }
}
