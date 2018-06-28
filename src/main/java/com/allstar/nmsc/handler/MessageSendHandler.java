package com.allstar.nmsc.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.Assert;
import com.alibaba.fastjson.JSONObject;
//import com.allstar.cinconnection.CinStack;
//import com.allstar.cinrouter.CinRouter;
//import com.allstar.cinrouter.CinServiceName;
//import com.allstar.cintransaction.CinTransaction;
//import com.allstar.cintransaction.CinTransactionEvent;
//import com.allstar.cintransaction.cinmessage.CinHeader;
//import com.allstar.cintransaction.cinmessage.CinHeaderType;
//import com.allstar.cintransaction.cinmessage.CinRequest;
//import com.allstar.cintransaction.cinmessage.CinRequestMethod;
//import com.allstar.cintransaction.cinmessage.CinResponse;
//import com.allstar.cintransaction.cinmessage.CinResponseCode;
//import com.allstar.event.CinLogonEvent;
import com.allstar.nmsc.scylla.dao.MessageDao;
import com.allstar.nmsc.scylla.dao.SessionInfoDao;
import com.allstar.nmsc.scylla.repository.MessageEntity;
import com.allstar.nmsc.util.MessageConstant;
import com.allstar.nmsc.util.Response;
import com.allstar.nmsc.util.ResponseCode;
import com.networknt.body.BodyHandler;

public class MessageSendHandler implements HttpHandler
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
			String tenantId = bodyMap.get("tenantId");

			Assert.notNull(to, "to must be not null.");
			Assert.notNull(from, "from must be not null.");
			Assert.notNull(extMap, "extMap must be not null.");
			Assert.notNull(groupId, "groupId must be not null.");
			Assert.notNull(message, "message must be not null.");
			Assert.notNull(messageId, "messageId must be not null.");

			long senderId = Long.valueOf(from);
			long receiverId = Long.valueOf(to);

//			CinRequest request = new CinRequest(CinRequestMethod.Logon);
//			request.addHeader(new CinHeader(CinHeaderType.Event, CinLogonEvent.CHECKCREDENTIAL));
//			request.addHeader(new CinHeader(CinHeaderType.From, senderId));
//			request.addHeader(new CinHeader(CinHeaderType.Fpid, from));
//			request.addHeader(new CinHeader(CinHeaderType.Type, from));
//			request.addHeader(new CinHeader(CinHeaderType.Version, from));
//			request.addHeader(new CinHeader(CinHeaderType.Language, 0));
//			request.addHeader(new CinHeader(CinHeaderType.DeviceToken, from));
//			request.addHeader(new CinHeader(CinHeaderType.Credential, from));
//
//			CinRouter.setRoute(request, CinServiceName.UserCacheCenter);
//			CinTransaction tran = CinStack.instance().createTransaction(request);
//			tran.TransactionEvent = new CinTransactionEvent() {
//
//				@Override
//				public void onTimeout(CinTransaction trans)
//				{
//				}
//
//				@Override
//				public void onSendFailed(CinTransaction trans)
//				{
//					Response resp = new Response(ResponseCode.ERROR);
//					exchange.getResponseSender().send(resp.toString());
//				}
//
//				@Override
//				public void onResponseReceived(CinTransaction trans, CinResponse response)
//				{
//					if (response.isResponseCode(CinResponseCode.OK))
//					{
						try
						{
							Map<String, String> map_ext = new HashMap<String, String>();
							if (extMap != null && !extMap.equals(""))
							{
								for (String pair : extMap.split(","))
								{
									map_ext.put(pair.split(":")[0], pair.split(":")[1]);
								}
							}

							MessageEntity entity = new MessageEntity(senderId, receiverId);
							entity.setMessage_id(messageId);
							entity.setMessage_status(MessageConstant.MSG_STATUS_SENT);
							entity.setMessage_content(message);
							entity.setGroup_sender(Long.valueOf(groupId));
							entity.setMsg_ext(map_ext);

							long maxIndex = new MessageDao().getMaxIndex(entity.getSession_key(), tenantId);
							long last_index = maxIndex + 1;
							entity.setMessage_index(last_index);

							new MessageDao().insertMessage(entity);
							new SessionInfoDao().updateSessionInfo(senderId, receiverId, last_index);

							JSONObject resp = new JSONObject();
							resp.put("messageIndex", last_index);

							// send response
							Response r = new Response(ResponseCode.OK);
							r.put("resp", resp);
							exchange.getResponseSender().send(r.toString());
						}
						catch (Exception e)
						{
							e.printStackTrace();
							Response resp = new Response(ResponseCode.ERROR);
							exchange.getResponseSender().send(resp.toString());
						}
//					}
//					else
//					{
//						Response resp = new Response(ResponseCode.ERROR);
//						resp.appendMsg("check credential failed");
//						exchange.getResponseSender().send(resp.toString());
//					}
//				}
//			};
//			tran.SendRequest();
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
