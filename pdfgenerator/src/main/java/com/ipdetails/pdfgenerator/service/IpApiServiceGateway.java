package com.ipdetails.pdfgenerator.service;

import com.ipdetails.pdfgenerator.model.Content;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Log4j2
@Service
@ConfigurationProperties(prefix="endpoint")
public class IpApiServiceGateway {

    private RestTemplate restTemplate = new RestTemplate();
    @Value("${endpoint.resourceUrl}")
    private String resourceUrl;
    private final String apiKey = "e4M64J3tDVtWw71oRJRFXrzY4ImyveX9P4SRf5bLzhICF6SUd3";

    public Content getInfoOfIp(String ip, String format) {
        log.info("calling backend with ip " + ip);
        String url = resourceUrl + ip + "/" + format + "/?key=" + apiKey;
        ResponseEntity<Content> response = restTemplate.getForEntity(url, Content.class);
        log.info("HTTP status code received" + response.getStatusCode());
        log.info("response received");
        return response.getBody();
    }
}
