package com.allstar.nmsc.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.HashMap;

import org.springframework.util.Assert;

import com.allstar.nmsc.scylla.dao.MessageDao;
import com.allstar.nmsc.util.Response;
import com.allstar.nmsc.util.ResponseCode;
import com.networknt.body.BodyHandler;

/**
 * All ExtMessage...Handler is operation on msg_ext column.
 * 	msg_ext = msg_ext - key
 * @author aminiy
 *
 */
public class ExtMessageRemoveHandler implements HttpHandler{

	@SuppressWarnings("unchecked")
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		try {
			HashMap<String, String>  bodyMap = (HashMap<String, String>) exchange.getAttachment(BodyHandler.REQUEST_BODY);

			String to = bodyMap.get("to");
			String from = bodyMap.get("from");
			
			String extKey = bodyMap.get("extKey");// key1,key2
			String messageIndex = bodyMap.get("messageIndex");
			
			Assert.notNull(from, "from must be not null.");
			Assert.notNull(to, "to must be not null.");
			Assert.notNull(extKey, "extKey must be not null.");
			Assert.notNull(messageIndex, "messageIndex must be not null.");
			
			String sessionKey;
			long fromId = Long.valueOf(from);
			long toId = Long.valueOf(to);
			
			if(fromId > toId)
				sessionKey= fromId + "" + toId;
			else
				sessionKey= toId + "" + fromId;
			
//			StringBuffer b = new StringBuffer("{");
//			for(String key : extKey.split(","))
//			{
//				b.append("'" + key + "',");
//			}
//			b.substring(0, b.length()-2);
//			b.append("}");
			
			extKey = "{'" + extKey.replaceAll(",","','") + "'}";
			new MessageDao().ExtMessageRemove(sessionKey, messageIndex, extKey);
			
			Response r = new Response(ResponseCode.OK);
			exchange.getResponseSender().send(r.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
			
			Response r = new Response(ResponseCode.ERROR);
			exchange.getResponseSender().send(r.toString());
		}
		exchange.endExchange();
	}

}
