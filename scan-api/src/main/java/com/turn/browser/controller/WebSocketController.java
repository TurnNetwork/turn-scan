package com.turn.browser.controller;

import com.turn.browser.config.BrowserCache;
import com.turn.browser.config.MessageDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Verify the node's web connection controller
 */
@Slf4j
@RestController
@ServerEndpoint("/websocket/{message}")
@CrossOrigin
public class WebSocketController {
	private String userno = "";
	private Lock lock = new ReentrantLock();

	/**
	 * * Method called when the connection is successfully established * * @param session optional parameter. Session is a connection session with a client, which needs to be used to send data to the client.
	 */
	@OnOpen
	public void onOpen(@PathParam(value = "message") String message, Session session, EndpointConfig config) {
		MessageDto messageDto = new MessageDto();
		messageDto = messageDto.analysisData(message);
		BrowserCache.getWebSocketSet().put(messageDto.getUserNo(), session);// 加入map中
		/**
		 * Use combined key to store user list
		 * Determine whether you already have the key
		 * Return directly if owned
		 */
		try {
			lock.lock();
			List<String> userList = null;
			if(BrowserCache.getKeys().containsKey(messageDto.getMessageKey())) {
				userList = BrowserCache.getKeys().get(messageDto.getMessageKey());
			} else {
				userList = new ArrayList<>();
			}
			userList.add(messageDto.getUserNo());
			BrowserCache.getKeys().put(messageDto.getMessageKey(),userList);
		} finally {
			lock.unlock();
		}
		userno = messageDto.getUserNo();
		BrowserCache.addOnlineCount();// Add 1 to the online number
		log.debug("A new connection has been added! The current number of people online is:{}",BrowserCache.getOnlineCount());
	}

	/**
	 * * Method called on connection close
	 */
	@OnClose
	public void onClose() {
		if (StringUtils.isNotBlank(userno)) {
			/**
			 * Loop to remove corresponding users
			 */
			for (Entry<String, List<String>> m : BrowserCache.getKeys().entrySet()) {
				if(m.getValue().contains(userno)) {
					m.getValue().remove(userno);
				}
			}
			BrowserCache.getWebSocketSet().remove(userno); // Remove from set
		}
		BrowserCache.subOnlineCount(); // Decrease the online number by 1
		log.debug("A connection is closed! The current number of people online is:{}",BrowserCache.getOnlineCount());
	}

	/**
	 * * Method called after receiving client message * * @param message Message sent by client * @param session Optional parameter
	 */

	@OnMessage
	public void onMessage(String message, Session session) {
		log.debug("Message from client:{}",message);
	}

	/**
	 * * Called when an error occurs * * @param session * @param error
	 */

	@OnError
	public void onError(Session session, Throwable error) {
		log.error(" error", error);
		if (StringUtils.isNotBlank(userno)) {
			/**
			 * Loop to remove corresponding users
			 */
			for (Entry<String, List<String>> m : BrowserCache.getKeys().entrySet()) {
				if(m.getValue().contains(userno)) {
					m.getValue().remove(userno);
				}
			}
			BrowserCache.getWebSocketSet().remove(userno); // 从set中删除
			BrowserCache.subOnlineCount(); // 在线数减1
			log.debug("A connection is closed! The current number of people online is:{}",BrowserCache.getOnlineCount());
		}
	}
}