package com.example.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Service
public class WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Replace with your actual details
    private final String name = "Your Name";
    private final String regNo = "REG12348"; // Example: ends with even → Question 2
    private final String email = "you@example.com";

    @Value("${generate.webhook.url}")
    private String generateWebhookUrl;

    @Value("${submit.webhook.url}")
    private String submitWebhookUrl;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void start() {
        try {
            System.out.println("start() triggered");

            // Step 1: Request webhook + token
            Map<String, String> requestBody = Map.of(
                    "name", name,
                    "regNo", regNo,
                    "email", email);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate
                    .postForEntity(
                            generateWebhookUrl, request, Map.class);

            System.out.println("Response from generateWebhookUrl: " + response);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> data = response.getBody();
                System.out.println("Received data: " + data);

                String webhookUrl = (String) data.get("webhook");
                String accessToken = (String) data.get("accessToken");

                // Step 2: Create final SQL
                String finalQuery = getFinalQuery();

                // Step 3: Submit answer
                sendFinalQuery(webhookUrl, accessToken, finalQuery);

            } else {
                System.out.println("Webhook generation failed: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.out.println("Exception occurred:");
            e.printStackTrace();
        }
    }

    private void sendFinalQuery(String webhookUrl, String token, String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(
                Map.of("finalQuery", query), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
        System.out.println("Response from testWebhook: " + response.getBody());
    }

    private String getFinalQuery() {
        return """
                 SELECT
                     p.AMOUNT AS SALARY,
                     CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME,
                     FLOOR(DATEDIFF(CURRENT_DATE, e.DOB)/365) AS AGE,
                     d.DEPARTMENT_NAME
                 FROM PAYMENTS p
                 JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
                 JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
                 WHERE DAY(p.PAYMENT_TIME) != 1
                 ORDER BY p.AMOUNT DESC
                 LIMIT 1;
                """;
    }
}
