package com.service.socketio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SocketIOService {

    @Value("${selfcheck.licence.url}")
    private String licenceUrl;

    private final RestTemplate restTemplate;

    public SocketIOService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<String> fetchNamespaces() {
        try {
            NamespaceResponse response = restTemplate.getForObject(licenceUrl, NamespaceResponse.class);
            if (response != null && response.getData() != null) {
                return response.getData().stream()
                        .map(Namespace::getSerial)
                        .collect(Collectors.toList());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return List.of("serial","serial1","serial2");
    }
}

class NamespaceResponse {
    private List<Namespace> data;
    private int status;
    private String message;

    public List<Namespace> getData() {
        return data;
    }

    public void setData(List<Namespace> data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

class Namespace {
    private String serial;

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }
}
