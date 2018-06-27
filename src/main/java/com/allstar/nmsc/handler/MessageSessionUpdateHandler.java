
package com.allstar.nmsc.handler;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;
import com.allstar.nmsc.scylla.dao.MessageDao;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

/**
 * update message status
 * 
 * @author aminiy
 *
 */
public class MessageSessionUpdateHandler implements HttpHandler {

	@Override
	public void handleRequest(HttpServerExchange exchange) {
		try {
			System.out.println("----->>enter into delete[update] message." );
			exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
			HeaderMap map = exchange.getRequestHeaders();
			String sessionKey = map.getFirst("sessionKey");
			String messageId = map.getFirst("messageId");
			
			Assert.notNull(sessionKey, "sessionKey must be not null.");
			Assert.notNull(messageId, "messageId must be not null.");
			
			new MessageDao().updateMessageStatus(sessionKey, messageId);
			
			// send response
			JSONObject response = new JSONObject();
			response.put("respcode", 1);
			response.put("msg", "OK");
			
			exchange.getResponseSender().send(response.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			
			JSONObject response = new JSONObject();
			response.put("respcode", 2);
			response.put("msg", e.getMessage());

			exchange.getResponseSender().send(response.toJSONString());
		}
		exchange.endExchange();
	}
}
