package com.example;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by apimentel on 1/31/17.
 */
@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    @Value("${integration.url}")
    String integrationEPURL;
    RestTemplate restTemplate = null;
    Random random = new Random();
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    void init() {
        logger.info("Starting Message Handler");
        restTemplate = new RestTemplate(clientHttpRequestFactory());
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(1500);
        factory.setConnectTimeout(1500);
        return factory;
    }

    public void notifySystemDummy() {
        Long id = ThreadLocalRandom.current().nextLong(10000000);
        String message = "[{\"id\":" + id + ",\"tenantId\":269,\"name\":\"TEST" + id + "\",\"active\":true,\"createdAt\":1485899598353,\"updatedAt\":1485899598353,\"phoneNumber\":\"4444444444444\",\"companyUrl\":\"TESTA130.com\",\"logoImage\":null,\"timezone\":null,\"tin\":null,\"deleted\":false,\"createdBy\":2322,\"updatedBy\":2322,\"version\":0,\"relationsVersion\":null,\"orgSource\":\"ACME\",\"orgId\":null,\"externalOrgId\":null,\"externalOrgImportId\":null,\"externalVersion\":null,\"phoneNotes\":null,\"createdOn\":1485899598353,\"updatedOn\":1485899598353,\"master\":true,\"externalId\":null}]";
        String uuid = UUID.randomUUID().toString();
        String url = integrationEPURL + "/269/message/insert_Account/" + uuid;
        sendMessage(message, url);
    }

    private void sendMessage(String message, String url) {
        ResponseEntity<?> response = restTemplate.postForEntity(url, message, String.class);
        logger.info("\nSend notification to the message producer. \n Notification: \n" + message + "\n");
        if (response.getStatusCode() != HttpStatus.OK) {
            logger.info(
                    "\nCannot send notification to the message producer. \n Notification: \n" + message + "\n Response: \n" + response);
        }
        logger.info("\nResponse\n" + response);
    }

    public void notifySystem() {
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "select * from data_change_notification where status= 0 order by operation_name asc LIMIT 100");
        logger.info("Records found: "+results.size());
        for (Map<String, Object> result : results) {
            Long id = (Long) result.get("id");
            String uuid = UUID.randomUUID().toString();
            String url = integrationEPURL + "/" + result.get("configuration_id") + "/message/" + result.get(
                    "operation_name") + "/" + uuid;
            String message = result.get("message_payload").toString();
            try {
                sendMessage(message, url);
                jdbcTemplate.update("update data_change_notification set status = 3 where id = ? ", id);
            } catch (Exception e) {
                e.printStackTrace();
                String stack = ExceptionUtils.getStackTrace(e);
                if (e instanceof org.springframework.web.client.RestClientException) {
                    jdbcTemplate.update(
                            "update data_change_notification set status = 4, error_message = ? where id = ? ", stack,
                            id
                    );
                } else {
                    jdbcTemplate.update(
                            "update data_change_notification set status = 1, error_message = ? where id = ? ", stack,
                            id
                    );
                }

            }
        }
    }
}
