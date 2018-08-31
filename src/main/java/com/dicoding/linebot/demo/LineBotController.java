package com.dicoding.linebot.demo;

import java.util.concurrent.ExecutionException;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;

@RestController
public class LineBotController {

	@Autowired
	@Qualifier("lineMessagingClient")
	private LineMessagingClient lineMessagingClient;

	@Autowired
	@Qualifier("lineSignatureValidator")
	private LineSignatureValidator lineSignatureValidator;

	@RequestMapping(value = "/tes", method = RequestMethod.POST)
	public ResponseEntity<String> tes() {
		return new ResponseEntity<String>("Berhasil", HttpStatus.OK);
	}

	@RequestMapping(value = "/webhook", method = RequestMethod.POST)
	public ResponseEntity<String> callback(@RequestHeader("X-Line-Signature") String xLineSignature,
			@RequestBody String eventsPayload) {
		try {
			// if (!lineSignatureValidator.validateSignature(eventsPayload.getBytes(),
			// xLineSignature)) {
			// throw new RuntimeException("Invalid Signature Validation");
			// }

			// parsing event
			ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
			EventsModel eventsModel = objectMapper.readValue(eventsPayload, EventsModel.class);
			System.out.println("Ini Kepanggil");

			eventsModel.getEvents().forEach((event) -> {
				if (event instanceof MessageEvent) {
					MessageEvent messageEvent = (MessageEvent) event;
					TextMessageContent textMessageContent = (TextMessageContent) messageEvent.getMessage();
					replyText(messageEvent.getReplyToken(), textMessageContent.getText());
				}
			});

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("Error", HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value="/pushmessage/{id}/{message}", method=RequestMethod.GET)
	public ResponseEntity<String> pushmessage(
	    @PathVariable("id") String userId,
	    @PathVariable("message") String textMsg
	){
	    TextMessage textMessage = new TextMessage(textMsg);
	    PushMessage pushMessage = new PushMessage(userId, textMessage);
	    push(pushMessage);
	 
	    return new ResponseEntity<String>("Push message:"+textMsg+"\nsent to: "+userId, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/pushsticker/{id}/{stickerId}", method = RequestMethod.POST)
	public ResponseEntity<String> pushSticker(@PathVariable String sourceId, @PathVariable String stickerId) {
		
			StickerMessage stickerMessage = new StickerMessage(sourceId, stickerId);
			PushMessage pushMessage = new PushMessage(stickerId, stickerMessage);
			push(pushMessage);
			return new ResponseEntity<String>("PushMessage " + sourceId + "\nSent to " + stickerId, HttpStatus.OK);
	
		}

	
	private void push(PushMessage pushMessage){
	    try {
	        lineMessagingClient.pushMessage(pushMessage).get();
	    } catch (InterruptedException | ExecutionException e) {
	        throw new RuntimeException(e);
	    }
	}

	private void reply(ReplyMessage replyMessage) {
		try {
			lineMessagingClient.replyMessage(replyMessage).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void replyText(String replyToken, String messageToUser) {
		TextMessage textMessage = new TextMessage(messageToUser);
		ReplyMessage replyMessage = new ReplyMessage(replyToken, textMessage);
		reply(replyMessage);
	}

	private void replySticker(String replyToken, String packageId, String stickerId) {
		StickerMessage stickerMessage = new StickerMessage(packageId, stickerId);
		ReplyMessage replyMessage = new ReplyMessage(replyToken, stickerMessage);
		reply(replyMessage);
	}
}