package com.allstar.nmsc.handler;

import java.util.HashMap;
import java.util.List;
import org.springframework.util.Assert;
import com.alibaba.fastjson.JSONObject;
import com.allstar.nmsc.scylla.dao.MessageDao;
import com.allstar.nmsc.scylla.repository.MessageEntity;
import com.allstar.nmsc.util.Response;
import com.allstar.nmsc.util.ResponseCode;
import com.networknt.body.BodyHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * get batch messages
 * 
 * @author aminiy
 */
public class MessageGetBatchHandler implements HttpHandler
{
	@SuppressWarnings("unchecked")
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception
	{
		try
		{
			HashMap<String, String> bodyMap = (HashMap<String, String>) exchange.getAttachment(BodyHandler.REQUEST_BODY);
			String senderId = bodyMap.get("from");
			String receiverId = bodyMap.get("to");

			Assert.notNull(senderId, "sender id must be not null.");
			Assert.notNull(receiverId, "receiver id must be not null.");

			JSONObject resp = new JSONObject();
			List<MessageEntity> entityList = new MessageDao().findMessageListBySessionKey(MessageEntity.getSessionKey(senderId, receiverId));
			if (entityList != null)
			{
				for (MessageEntity entity : entityList)
				{
					JSONObject message = new JSONObject();
					message.put("sessionKey", entity.getSession_key());
					message.put("messageId", entity.getMessage_id());
					message.put("messageIndex", entity.getMessage_index());
					message.put("messageStatus", entity.getMessage_status());
					message.put("senderId", entity.getSender_id());
					message.put("receiverId", entity.getReceiver_id());
					message.put("message", entity.getMessage_content());

					resp.put("message", message);
				}
			}

			Response response = new Response(ResponseCode.OK);
			response.put("resp", resp);
			exchange.getResponseSender().send(response.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();

			Response res = new Response(ResponseCode.ERROR);
			exchange.getResponseSender().send(res.toString());
		}
		exchange.endExchange();
	}

}
