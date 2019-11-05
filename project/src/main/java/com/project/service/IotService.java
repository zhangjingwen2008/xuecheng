package com.project.service;

import com.project.api.IotTestApi;
import org.springframework.stereotype.Service;

@Service
public class IotService {

    public int getData(int data) {
        System.out.println(data);
        return data;
    }

}
