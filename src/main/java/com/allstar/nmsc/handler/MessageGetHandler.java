
package com.allstar.nmsc.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.HashMap;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;
import com.allstar.nmsc.scylla.dao.MessageDao;
import com.allstar.nmsc.scylla.repository.MessageEntity;
import com.allstar.nmsc.util.Response;
import com.allstar.nmsc.util.ResponseCode;
import com.networknt.body.BodyHandler;

public class MessageGetHandler implements HttpHandler {
	
	@SuppressWarnings("unchecked")
	@Override
	public void handleRequest(HttpServerExchange exchange) {
		try {
			HashMap<String, String>  bodyMap = (HashMap<String, String>) exchange.getAttachment(BodyHandler.REQUEST_BODY);
			String to = bodyMap.get("to");
			String from = bodyMap.get("from");
			String messageIndex = bodyMap.get("messageIndex");
			
			Assert.notNull(from, "from must be not null.");
			Assert.notNull(to, "to must be not null.");
			Assert.notNull(messageIndex, "messageIndex must be not null.");
			
			String sessionKey;
			long fromId = Long.valueOf(from);
			long toId = Long.valueOf(to);
			
			if(fromId > toId)
				sessionKey= fromId + "" + toId;
			else
				sessionKey= toId + "" + fromId;
			
			MessageEntity entity = new MessageDao().findMessageByMsgIndex(sessionKey, Long.valueOf(messageIndex));// 100001231100001441
			
			JSONObject resp = new JSONObject();
			if(entity!=null){
				resp.put("sessionKey", entity.getSession_key());
				resp.put("messageId", entity.getMessage_id());
				resp.put("messageIndex", entity.getMessage_index());
				resp.put("messageStatus", entity.getMessage_status());
				resp.put("senderId", entity.getSender_id());
				resp.put("receiverId", entity.getReceiver_id());
				
//				ByteBuffer buffer = entity.getMessage_content();
//				byte[] b = new byte[buffer.capacity()];
//				buffer.get(b, 0, b.length);
//				System.out.println("----string---" + new String (b));
				resp.put("message", entity.getMessage_content());
			}
			
			Response response = new Response(ResponseCode.OK);
			response.put("resp", resp);
			exchange.getResponseSender().send(response.toString());
		} catch (Exception e) {
			e.printStackTrace();
			
			Response res = new Response(ResponseCode.ERROR);
			exchange.getResponseSender().send(res.toString());
		}
		exchange.endExchange();
	}
}
