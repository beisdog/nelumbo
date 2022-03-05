package com.deep.nelumbo.dynform.dto;

import lombok.Data;

@Data
public class Message {

    protected String message;
    protected MessageType type = MessageType.Error;
}