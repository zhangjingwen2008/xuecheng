package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJwt {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Test
    public void testRedis(){
        String key = "user_token:asdfasdfasfasf";
        Map<String, String> value = new HashMap<>();
        value.put("jwt", "asdfasfdsa");
        value.put("refresh_token", "asdfasfasfds");
        String jsonString = JSON.toJSONString(value);
        stringRedisTemplate.boundValueOps(key).set(jsonString, 30, TimeUnit.SECONDS);
        String string = stringRedisTemplate.opsForValue().get(key);
        System.out.println(string);
    }

    @Test
    public void testClient() {
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri();

        String authUrl = uri + "/auth/oauth/token";
        //header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String basic = getHttpBasic("XcWebApp", "XcWebApp");
        header.add("Authorization",basic);

        //body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", "itcast");
        body.add("password", "123");

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(body, header);
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        //令牌信息
        Map bodyMap = exchange.getBody();
        System.out.println(bodyMap);
    }

    private String getHttpBasic(String clientId, String clientSecret) {
        String http = clientId + ":" + clientSecret;
        byte[] basic = Base64Utils.encode(http.getBytes());
        return "Basic " + new String(basic);
    }



}
