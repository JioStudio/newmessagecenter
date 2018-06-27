package com.allstar.nmsc.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;
import com.allstar.nmsc.scylla.dao.MessageDao;
import com.allstar.nmsc.scylla.repository.MessageEntity;
import com.allstar.nmsc.util.Response;
import com.allstar.nmsc.util.ResponseCode;
import com.networknt.body.BodyHandler;

public class MessageInsertHandler implements HttpHandler {

	@SuppressWarnings("unchecked")
	@Override
	public void handleRequest(HttpServerExchange exchange) {
		try {
			HashMap<String, String>  bodyMap = (HashMap<String, String>) exchange.getAttachment(BodyHandler.REQUEST_BODY);
			String from = bodyMap.get("from");
			String to = bodyMap.get("to");
			String messageId = bodyMap.get("messageId");
			String message = bodyMap.get("message");
			String groupId = bodyMap.get("groupId");
			String extMap = bodyMap.get("extMap");
			
			Assert.notNull(messageId, "messageId must be not null.");
			Assert.notNull(from, "sender user Id must be not null.");
			Assert.notNull(to, "receiver user Id must be not null.");
			Assert.notNull(groupId, "groupId must be not null.");
			Assert.notNull(message, "message must be not null.");
			
			long fromId = Long.valueOf(from);
			long toId = Long.valueOf(to);
			String sessionKey;
			
			if (fromId > toId)
				sessionKey = fromId + "" + toId;
			else
				sessionKey = toId + "" + fromId;

			long maxIndex = new MessageDao().getMaxIndex(sessionKey);
			long messIndex = maxIndex + 1;
			
			Map<String, String> map_ext = new HashMap<String, String>();
			if (extMap != null && !extMap.equals("")) {
				for (String pair : extMap.split(",")) {
					map_ext.put(pair.split(":")[0], pair.split(":")[1]);
				}
			}
			
			MessageEntity entity = new MessageEntity();
			entity.setSession_key(sessionKey);
			entity.setMessage_id(messageId);
			entity.setMessage_index(messIndex);
			entity.setMessage_status(0);
			entity.setMessage_content(message);// ByteBuffer.wrap(message.getBytes())
			entity.setMessage_time(new Date(System.currentTimeMillis()));
			entity.setSender_id(fromId);
			entity.setReceiver_id(toId);
			entity.setGroup_sender(Long.valueOf(groupId));
			entity.setDelflag_max(0);
			entity.setDelflag_min(0);
			entity.setMsg_ext(map_ext);
			new MessageDao().insertMessage(entity);
			
			JSONObject resp = new JSONObject();
			resp.put("messageIndex", messIndex);
			
			// send response
			Response response = new Response(ResponseCode.OK);
			response.put("resp", resp);
			exchange.getResponseSender().send(response.toString());
		
		} catch (Exception e) {
			e.printStackTrace();
			Response resp = new Response(ResponseCode.ERROR);
			exchange.getResponseSender().send(resp.toString());
		}
		exchange.endExchange();
	}
}
