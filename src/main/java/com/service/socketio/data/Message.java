package com.service.socketio.data;

import lombok.Data;

@Data
public class Message {

    private String senderName;
    private String targetUserName;
    private Object message;

}
