package com.allstar.nmsc.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;
import com.allstar.nmsc.scylla.dao.MessageDao;
import com.allstar.nmsc.scylla.dao.SessionInfoDao;
import com.allstar.nmsc.scylla.repository.MessageEntity;
import com.allstar.nmsc.util.MessageConstant;
import com.allstar.nmsc.util.Response;
import com.allstar.nmsc.util.ResponseCode;
import com.networknt.body.BodyHandler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class MessageInsertHandler implements HttpHandler
{

	@SuppressWarnings("unchecked")
	@Override
	public void handleRequest(HttpServerExchange exchange)
	{
		try
		{
			HashMap<String, String> bodyMap = (HashMap<String, String>) exchange.getAttachment(BodyHandler.REQUEST_BODY);
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

			long sender_id = Long.valueOf(from);
			long receiver_id = Long.valueOf(to);

			Map<String, String> map_ext = new HashMap<String, String>();
			if (extMap != null && !extMap.equals(""))
			{
				for (String pair : extMap.split(","))
				{
					map_ext.put(pair.split(":")[0], pair.split(":")[1]);
				}
			}

			MessageEntity entity = new MessageEntity(sender_id, receiver_id);
			entity.setMessage_id(messageId);
			entity.setMessage_status(MessageConstant.MSG_STATUS_SENT);
			entity.setMessage_content(message);
			entity.setGroup_sender(Long.valueOf(groupId));
			entity.setMsg_ext(map_ext);

			long maxIndex = new MessageDao().getMaxIndex(entity.getSession_key());
			long last_index = maxIndex + 1;
			entity.setMessage_index(last_index);

			new MessageDao().insertMessage(entity);
			new SessionInfoDao().updateSessionInfo(sender_id, receiver_id, last_index);

			JSONObject resp = new JSONObject();
			resp.put("messageIndex", last_index);

			// send response
			Response response = new Response(ResponseCode.OK);
			response.put("resp", resp);
			exchange.getResponseSender().send(response.toString());

		}
		catch (Exception e)
		{
			e.printStackTrace();
			Response resp = new Response(ResponseCode.ERROR);
			exchange.getResponseSender().send(resp.toString());
		}
		exchange.endExchange();
	}
}
