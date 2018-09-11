package com.dicoding.linebot.demo;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.AudioMessageContent;
import com.linecorp.bot.model.event.message.FileMessageContent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.message.VideoMessageContent;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.linecorp.bot.model.profile.UserProfileResponse;

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
				// Apabila Ingin membalas pesan sesuai pesan

				/*
				 * // if (event instanceof MessageEvent) { // MessageEvent messageEvent =
				 * (MessageEvent) event; // TextMessageContent textMessageContent =
				 * (TextMessageContent) messageEvent.getMessage(); //
				 * replyText(messageEvent.getReplyToken(), textMessageContent.getText()); //
				 * //replySticker(messageEvent.getReplyToken(), "1", "1"); // }
				 */

				if (((MessageEvent) event).getMessage() instanceof AudioMessageContent
						|| ((MessageEvent) event).getMessage() instanceof ImageMessageContent
						|| ((MessageEvent) event).getMessage() instanceof VideoMessageContent
						|| ((MessageEvent) event).getMessage() instanceof FileMessageContent) {
					String baseUrl = "https://botlinedi.herokuapp.com/";
					String contentUrl = baseUrl + "/content/" + ((MessageEvent) event).getMessage().getId();
					String contentType = ((MessageEvent) event).getMessage().getClass().getSimpleName();
					String textMsg = contentType.substring(0, contentType.length() - 14)
							+ " yang kamu kirim bisa diakses dari link:\n " + contentUrl;

					replyText(((MessageEvent) event).getReplyToken(), textMsg);
					getContent(textMsg);
				} else {
					MessageEvent messageEvent = (MessageEvent) event;
					TextMessageContent textMessageContent = (TextMessageContent) messageEvent.getMessage();
					// replyText(messageEvent.getReplyToken(), textMessageContent.getText());
					replySticker(messageEvent.getReplyToken(), "1", "1");
					getContent(textMessageContent.getText());
				}

			});

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("Error", HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/pushmessage/{id}/{message}", method = RequestMethod.GET)
	public ResponseEntity<String> pushmessage(@PathVariable("id") String userId,
			@PathVariable("message") String textMsg) {
		TextMessage textMessage = new TextMessage(textMsg);
		PushMessage pushMessage = new PushMessage(userId, textMessage);
		push(pushMessage);

		return new ResponseEntity<String>("Push message:" + textMsg + "\nsent to: " + userId, HttpStatus.OK);
	}

	@RequestMapping(value = "/pushsticker/{id}", method = RequestMethod.GET)
	public ResponseEntity<String> pushSticker(@PathVariable("id") String userId) {
		StickerMessage stickerMessage = new StickerMessage("1", "104");
		PushMessage pushMessage = new PushMessage(userId, stickerMessage);
		push(pushMessage);
		return new ResponseEntity<String>("PushMessage " + userId, HttpStatus.OK);
	}

	@RequestMapping(value = "/multicast", method = RequestMethod.GET)
	public ResponseEntity<String> multicast() {
		String[] userIdList = { "U674e4eadb0be05c9f8a94692fdf56aeb" };
		Set<String> listUser = new HashSet<String>(Arrays.asList(userIdList));
		if (listUser.size() > 0) {
			String textMsg = "Ini Pesan Multicast";
			sendMulticast(listUser, textMsg);
		}

		return new ResponseEntity<String>("Berhasil Mengirim Ke Semua ID", HttpStatus.OK);
	}

	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	public ResponseEntity<String> profile() {
		String userID = "U674e4eadb0be05c9f8a94692fdf56aeb";
		UserProfileResponse profile = getProfile(userID);
		if (profile != null) {
			String profileName = profile.getDisplayName();
			TextMessage textMessage = new TextMessage("Hello " + profileName);
			PushMessage pushMessage = new PushMessage(userID, textMessage);
			push(pushMessage);
			return new ResponseEntity<String>("Hello " + profileName, HttpStatus.OK);
		}

		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = "/profile/{id}", method = RequestMethod.GET)
	public ResponseEntity<String> profile(@PathVariable("id") String userId) {
		UserProfileResponse profile = getProfile(userId);

		if (profile != null) {
			String profileName = profile.getDisplayName();
			TextMessage textMessage = new TextMessage("Hello, " + profileName);
			PushMessage pushMessage = new PushMessage(userId, textMessage);
			push(pushMessage);

			return new ResponseEntity<String>("Hello, " + profileName, HttpStatus.OK);
		}

		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = "/content/{id}", method = RequestMethod.GET)
	public ResponseEntity content(@PathVariable("id") String messageId) {
		MessageContentResponse messageContent = getContent(messageId);

		if (messageContent != null) {
			HttpHeaders headers = new HttpHeaders();
			String[] mimeType = messageContent.getMimeType().split("/");
			headers.setContentType(new MediaType(mimeType[0], mimeType[1]));

			InputStream inputStream = messageContent.getStream();
			InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

			return new ResponseEntity(inputStreamResource, headers, HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	private MessageContentResponse getContent(String messageId) {
		try {
			return lineMessagingClient.getMessageContent(messageId).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private UserProfileResponse getProfile(String userId) {
		try {
			return lineMessagingClient.getProfile(userId).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void sendMulticast(Set<String> sourceUser, String txtMessage) {
		TextMessage txtMsg = new TextMessage(txtMessage);
		Multicast multicast = new Multicast(sourceUser, txtMsg);
		try {
			lineMessagingClient.multicast(multicast).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void push(PushMessage pushMessage) {
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