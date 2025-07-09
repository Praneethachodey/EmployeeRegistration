package com.example.employee.ejb;

import com.example.employee.config.HttpClientConfig;
import com.example.employee.config.PropertyConfig;
import com.example.employee.dto.EmployeeWithPoliciesDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class EmployeeDetailsEJB
{

    @Inject
    private HttpClientConfig httpClientConfig;

    @Inject
    private PropertyConfig propertyConfig;

    private final ObjectMapper objectMapper;

    public EmployeeDetailsEJB()
    {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public EmployeeWithPoliciesDTO getEmployeeWithPolicies(String employeeId, String sessionId)
    {
        return sendPostRequest("view", employeeId, sessionId, null, null, null, null);
    }

    public EmployeeWithPoliciesDTO updateEmployee(String employeeId, String name, String department, String email, String phone, String sessionId)
    {
        return sendPostRequest("update", employeeId, sessionId, name, department, email, phone);
    }

    public void deleteEmployee(String employeeId, String sessionId)
    {
        sendPostRequest("delete", employeeId, sessionId, null, null, null, null);
    }

    private EmployeeWithPoliciesDTO sendPostRequest(String action, String employeeId, String sessionId, String name, String department, String email, String phone)
    {
        String url = propertyConfig.getBaseUrl() + propertyConfig.getEmployeeUrl();

        CloseableHttpClient httpClient = httpClientConfig.getHttpClient(); // ✅ Do not close shared client

        try
        {
            HttpPost post = new HttpPost(url);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(propertyConfig.getTimeoutMs()).setSocketTimeout(propertyConfig.getTimeoutMs()).build();
            post.setConfig(requestConfig);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("action", action));
            params.add(new BasicNameValuePair("employeeId", employeeId));
            params.add(new BasicNameValuePair("sessionId", sessionId));
            if (name != null)
                params.add(new BasicNameValuePair("name", name));
            if (department != null)
                params.add(new BasicNameValuePair("department", department));
            if (email != null)
                params.add(new BasicNameValuePair("email", email));
            if (phone != null)
                params.add(new BasicNameValuePair("phone", phone));
            post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(post))
            {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseJson = EntityUtils.toString(response.getEntity());

                if (statusCode != 200)
                {
                    throw new RuntimeException("Microservice error: HTTP " + statusCode + " - " + responseJson);
                }

                return objectMapper.readValue(responseJson, EmployeeWithPoliciesDTO.class);
            }

        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to call microservice (" + action + ")", e);
        }

    }
}
