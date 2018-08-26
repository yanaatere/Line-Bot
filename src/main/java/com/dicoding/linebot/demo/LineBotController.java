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
import org.springframework.context.event.EventListenerMethodProcessor;
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

	@RequestMapping(value = "/webhook", method = RequestMethod.POST)
	public ResponseEntity<String> callback(@RequestHeader("X-Line-Signature") String xLineSignature,
			@RequestBody String eventPayload) {
		
		try {
//			if (!lineSignatureValidator.validateSignature(eventPayload.getBytes(), xLineSignature)) {
//				throw new RuntimeException("Invalid Signature Validation");
//			}
			
			//Parsing Event
			ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
			EventsModel  eventsModel = objectMapper.readValue(eventPayload, EventsModel.class);
			
			eventsModel.geEvents().forEach((event)->{
				//Reply Disini
			});
			
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (IOException e) {
				e.printStackTrace();
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}
