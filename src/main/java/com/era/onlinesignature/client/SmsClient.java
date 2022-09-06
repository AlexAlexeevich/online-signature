package com.era.onlinesignature.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class SmsClient {

    private final RestTemplate restTemplate;

    @Value("${sms.url}")
    private String url;

    @Value("${sms.login}")
    private String login;

    @Value("${sms.password}")
    private String password;

    public String sendSms(String phone, String code) {
        return restTemplate.getForObject(url + "/sys/send.php?login=" + login + "&psw=" + password + "&phones=" + phone +
                        "&mes=" + code + "&fmt=3&all=2&charset=utf-8",
                String.class);
    }

    public String sendLink(String phone, String link) {
        return restTemplate.getForObject(url + "/sys/send.php?login=" + login + "&psw=" + password + "&phones=" + phone +
                        "&mes=" + link + "&fmt=3&all=2&charset=utf-8",
                String.class);


    }
}
