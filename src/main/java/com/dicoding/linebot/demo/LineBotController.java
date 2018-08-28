package com.dicoding.linebot.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.io.IOException;
import java.util.concurrent.ExecutionException;
 
@RestController
public class LineBotController {
 
    @Autowired
    @Qualifier("lineMessagingClient")
    private LineMessagingClient lineMessagingClient;
 
    @Autowired
    @Qualifier("lineSignatureValidator")
    private LineSignatureValidator lineSignatureValidator;
 
    @RequestMapping(value="/webhook", method= RequestMethod.POST)
    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String xLineSignature,
            @RequestBody String eventsPayload)
    {
        try {
//            if (!lineSignatureValidator.validateSignature(eventsPayload.getBytes(), xLineSignature)) {
//                throw new RuntimeException("Invalid Signature Validation");
//            }
 
            // parsing event
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            EventsModel eventsModel = objectMapper.readValue(eventsPayload, EventsModel.class);
            System.out.println("Ini Kepanggil");
            eventsModel.getEvents().forEach((event)->{
                
            });
 
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>("Error",HttpStatus.BAD_REQUEST);
        }
    }
}