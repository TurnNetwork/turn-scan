package com.turn.browser.config;

import com.turn.browser.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BrowserCache {
	private BrowserCache (){}

	private static Logger logger = LoggerFactory.getLogger(BrowserCache.class);
	// Static variable used to record the current number of online connections. It should be designed to be thread-safe.
	private static volatile int onlineCount = 0;
	// The thread-safe Set of the concurrent package is used to store the MyWebSocket object corresponding to each client. If you want to achieve communication between the server and a single client, you can use Map to store it, where the Key can be the user ID.
	private static Map<String,  Session> webSocketSet = new ConcurrentHashMap<>();// A connection session with a client through which data is sent to the client
	private static Map<String, List<String>> keys = new ConcurrentHashMap<>();// The key storage value according to the number of pagination is the user list

	public static Map<String, Session> getWebSocketSet() {
		return webSocketSet;
	}

	public static void setWebSocketSet(Map<String, Session> webSocketSet) {
		BrowserCache.webSocketSet = webSocketSet;
	}

	public static Map<String, List<String>> getKeys() {
		return keys;
	}

	public static void setKeys(Map<String, List<String>> keys) {
		BrowserCache.keys = keys;
	}

	public static synchronized void setOnlineCount(int onlineCount) {
		BrowserCache.onlineCount = onlineCount;
	}

	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		BrowserCache.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		BrowserCache.onlineCount--;
	}


	/**
	 * * This method is different from the above methods. There are no annotations, just add methods according to your needs. * * @param message * @throws IOException
	 * @throws Exception 
	 */

	public static void sendMessage(String key,String message)  {
		try {
			/**
			 * Send information to the specified socket connection
			 */
			BrowserCache.getWebSocketSet().get(key).getBasicRemote().sendText(message);
		} catch (Exception e) {
			logger.error("sendMessage error", e);
			throw new BusinessException(e.getMessage());
		}
	}
}
