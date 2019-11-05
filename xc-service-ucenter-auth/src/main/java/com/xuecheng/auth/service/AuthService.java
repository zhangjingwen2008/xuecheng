package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //用户认证申请令牌，将令牌存储到redis
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //请求SpringSecurity申请令牌
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if (null == authToken) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }

        //用户身份令牌
        String access_token = authToken.getAccess_token();
        //存储到redis中的内容
        String content = JSON.toJSONString(authToken);

        //将令牌存储到Redis
        boolean token = this.saveToken(access_token, content, tokenValiditySeconds);
        if (!token) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authToken;
    }

    //申请令牌
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri();

        String authUrl = uri.toString() + "/auth/oauth/token";
        //header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String basic = getHttpBasic(clientId, clientSecret);
        header.add("Authorization",basic);

        //body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(body, header);
        ResponseEntity<Map> exchang//令牌信息
        Map bodyMap = exchange.getBody();e = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);


        //异常处理
        if (null == bodyMap || null == bodyMap.get("access_token") || null == bodyMap.get("refresh_token") || null == bodyMap.get("jti")) {
            String error_description = (String)bodyMap.get("error_description");
            if (error_description.indexOf("UserDetailsService returned null, which is an interface contract violation")>=0) {
                ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
            }else if (error_description.indexOf("坏的凭证")>=0) {
                ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
            }
        }
        AuthToken authToken = new AuthToken();
        authToken.setJwt_token((String) bodyMap.get("access_token"));
        authToken.setRefresh_token((String)bodyMap.get("refresh_token"));
        authToken.setAccess_token((String)bodyMap.get("jti"));
        return authToken;
    }

    //将令牌存到redis
    /**
     *
     * @param access_token 用户身份令牌
     * @param content AuthToken内容
     * @param ttl 过期时间
     * @return
     */
    private boolean saveToken(String access_token, String content, long ttl) {
        String key = "user_token:" + access_token;
        stringRedisTemplate.boundValueOps(key).set(content, ttl, TimeUnit.SECONDS);
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire > 0;
    }
    //将Redis中的Token删除
    /**
     *
     * @param access_token 用户身份令牌
     * @return
     */
    public boolean delToken(String access_token) {
        String key = "user_token:" + access_token;
        stringRedisTemplate.delete(key);
        return true;
    }

    //从redis查询令牌
    public AuthToken getUserToken(String token) {
        String key = "user_token:" + token;
        //从redis中获取令牌信息
        String value = stringRedisTemplate.opsForValue().get(key);
        try {
            //转换数据
            AuthToken authToken = JSON.parseObject(value, AuthToken.class);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //获取httpBasic串
    private String getHttpBasic(String clientId, String clientSecret) {
        String http = clientId + ":" + clientSecret;
        byte[] basic = Base64Utils.encode(http.getBytes());
        return "Basic " + new String(basic);
    }

}
