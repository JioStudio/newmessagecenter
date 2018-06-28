package com.allstar.nmsc.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.util.HashMap;
import org.springframework.util.Assert;
import com.alibaba.fastjson.JSONObject;
import com.allstar.nmsc.scylla.dao.MessageDao;
import com.allstar.nmsc.util.Response;
import com.allstar.nmsc.util.ResponseCode;
import com.networknt.body.BodyHandler;

/**
 * update message status
 * 
 * @author vincent.ma
 *
 */
public class MessageSessionUpdateHandler implements HttpHandler
{
	@SuppressWarnings("unchecked")
	@Override
	public void handleRequest(HttpServerExchange exchange)
	{
		try
		{
			HashMap<String, String> bodyMap = (HashMap<String, String>) exchange.getAttachment(BodyHandler.REQUEST_BODY);
			exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
			String to = bodyMap.get("to");
			String from = bodyMap.get("from");
			String messageId = bodyMap.get("messageId");
			String tenantId = bodyMap.get("tenantId");
			
			Assert.notNull(to, "to must be not null.");
			Assert.notNull(from, "from must be not null.");
			Assert.notNull(messageId, "messageId must be not null.");
			Assert.notNull(tenantId, "tenantId must be not null.");
			
			String sessionKey;
			long fromId = Long.valueOf(from);
			long toId = Long.valueOf(to);
			if(fromId > toId)
			{
				sessionKey = fromId + "" + toId;
			}
			else
				sessionKey = toId + "" + fromId;
			
			new MessageDao().updateMessageStatus(sessionKey, tenantId, messageId);

			// send response
			Response response  = new Response(ResponseCode.OK);
			exchange.getResponseSender().send(response.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();

			JSONObject response = new JSONObject();
			response.put("respcode", 2);
			response.put("msg", e.getMessage());

			exchange.getResponseSender().send(response.toJSONString());
		}
		exchange.endExchange();
	}
}
