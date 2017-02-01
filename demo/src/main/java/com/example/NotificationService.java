package com.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by apimentel on 1/31/17.
 */
@Service
public class NotificationService {
    String integrationEPURL;
    RestTemplate restTemplate= null;
    Random random = new Random();
    @PostConstruct
    void init(){
        System.out.println("Starting Message Handler for 269");
        integrationEPURL = "http://localhost:12050";
        restTemplate = new RestTemplate(clientHttpRequestFactory());
    }
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(1500);
        factory.setConnectTimeout(1500);
        return factory;
    }
    public void notifySystem() {
        Long id = ThreadLocalRandom.current().nextLong(10000000);
        String message = "[{\"id\":"+id+",\"tenantId\":269,\"name\":\"TEST"+id+"\",\"active\":true,\"createdAt\":1485899598353,\"updatedAt\":1485899598353,\"phoneNumber\":\"4444444444444\",\"companyUrl\":\"TESTA130.com\",\"logoImage\":null,\"timezone\":null,\"tin\":null,\"deleted\":false,\"createdBy\":2322,\"updatedBy\":2322,\"version\":0,\"relationsVersion\":null,\"orgSource\":\"ACME\",\"orgId\":null,\"externalOrgId\":null,\"externalOrgImportId\":null,\"externalVersion\":null,\"phoneNotes\":null,\"createdOn\":1485899598353,\"updatedOn\":1485899598353,\"master\":true,\"externalId\":null}]";
        String uuid = UUID.randomUUID().toString();
        String url = integrationEPURL + "/269/message/insert_Account/"+ uuid;
        ResponseEntity<?> response = restTemplate.postForEntity(url, message, String.class);
        System.out.println("\nSend notification to the message producer. \n Notification: \n" + message+ "\n");
        if (response.getStatusCode() != HttpStatus.OK) {
            System.out.println("\nCannot send notification to the message producer. \n Notification: \n" + message + "\n Response: \n" + response);
        }
    }
}
