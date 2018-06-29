package com.allstar.nmsc.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.Assert;
import com.alibaba.fastjson.JSONObject;
import com.allstar.cinconnection.CinStack;
import com.allstar.cinrouter.CinRouter;
import com.allstar.cinrouter.CinServiceName;
import com.allstar.cintransaction.CinTransaction;
import com.allstar.cintransaction.CinTransactionEvent;
import com.allstar.cintransaction.cinmessage.CinBody;
import com.allstar.cintransaction.cinmessage.CinHeader;
import com.allstar.cintransaction.cinmessage.CinHeaderType;
import com.allstar.cintransaction.cinmessage.CinRequest;
import com.allstar.cintransaction.cinmessage.CinRequestMethod;
import com.allstar.cintransaction.cinmessage.CinResponse;
import com.allstar.cintransaction.cinmessage.CinResponseCode;
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

/**
 * 
 * P2P message, A send message to B with http request, extMap: optional,
 * key1:value1,key2:value2... all extend property are save in msg_txt column
 * 
 * @since 2018-06-29
 * @author vincent.ma
 */
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
			String message = bodyMap.get("message");// how about two bodys ...
			String groupId = bodyMap.get("groupId");
			String extMap = bodyMap.get("extMap");// extend column of message
			String tenantId = bodyMap.get("tenantId");
			
			String credential = bodyMap.get("credential");
			String fpId = bodyMap.get("fpId");
			String type = bodyMap.get("type");
			String encrypt = bodyMap.get("encrypt");
			
			Assert.notNull(to, "to must be not null.");
			Assert.notNull(from, "from must be not null.");
			Assert.notNull(extMap, "extMap must be not null.");
			Assert.notNull(groupId, "groupId must be not null.");
			Assert.notNull(message, "message must be not null.");
			Assert.notNull(messageId, "messageId must be not null.");

			long senderId = Long.valueOf(from);
			long receiverId = Long.valueOf(to);

			CinRequest request = new CinRequest(CinRequestMethod.InnerService);
//			request.addHeader(new CinHeader(CinHeaderType.Event, CinInnerServiceEvent.CheckCredential));// TODO to be add in cincommon
			request.addHeader(new CinHeader(CinHeaderType.From, senderId));
			request.addHeader(new CinHeader(CinHeaderType.Fpid, from));
			request.addHeader(new CinHeader(CinHeaderType.Type, type));
			request.addHeader(new CinHeader(CinHeaderType.Credential, credential));
			CinRouter.setRoute(request, CinServiceName.UserCacheCenter);
			CinTransaction tran = CinStack.instance().createTransaction(request);
			tran.TransactionEvent = new CinTransactionEvent() {

				@Override
				public void onTimeout(CinTransaction trans)
				{
				}

				@Override
				public void onSendFailed(CinTransaction trans)
				{
					Response resp = new Response(ResponseCode.ERROR);
					exchange.getResponseSender().send(resp.toString());
				}

				@Override
				public void onResponseReceived(CinTransaction trans, CinResponse response)
				{
					if (response.isResponseCode(CinResponseCode.OK))
					{
						try
						{
							// 1. save message to ScyllaDB
							Map<String, String> map_ext = new HashMap<String, String>();
							if (extMap != null && !extMap.equals(""))
							{
								// key1:value1,key2:value2...
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

							// 2. send message to MSC with DirectSendRequestHandler way
							CinRequest request = new CinRequest(CinRequestMethod.Notify);// to be add as MessageEvent
							request.addHeader(new CinHeader(CinHeaderType.Event, 0x26));// 0x01-0x30 is OK
							request.addHeader(new CinHeader(CinHeaderType.From, senderId));
							request.addHeader(new CinHeader(CinHeaderType.To, receiverId));
							request.addHeader(new CinHeader(CinHeaderType.Fpid, fpId));// optional
							request.addHeader(new CinHeader(CinHeaderType.Type, type));// messageType
							
							if(encrypt!=null)
								request.addHeader(new CinHeader(CinHeaderType.Encrypt, encrypt));
							if(extMap!=null)
								request.addHeader(new CinHeader(CinHeaderType.Index, extMap));
							
							request.addBody(new CinBody(message));// TODO should be add bodys
							CinRouter.setRoute(request, CinServiceName.MessageCenter);
							CinTransaction tran = CinStack.instance().createTransaction(request);
							tran.TransactionEvent = new CinTransactionEvent() {
								@Override
								public void onTimeout(CinTransaction trans)
								{
								}
								
								@Override
								public void onSendFailed(CinTransaction trans)
								{
									Response resp = new Response(ResponseCode.ERROR);
									exchange.getResponseSender().send(resp.toString());
								}
								
								@Override
								public void onResponseReceived(CinTransaction trans, CinResponse response)
								{
									// 3. send response
									if(response.isResponseCode(CinResponseCode.OK))
									{
										try
										{
											JSONObject resp = new JSONObject();
											resp.put("messageIndex", last_index);

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
									}
									else
									{
										Response resp = new Response(ResponseCode.ERROR);
										exchange.getResponseSender().send(resp.toString());
									}
								}
							};
							tran.SendRequest();
						}
						catch (Exception e)
						{
							e.printStackTrace();
							Response resp = new Response(ResponseCode.ERROR);
							exchange.getResponseSender().send(resp.toString());
						}
					}
					else
					{
						Response resp = new Response(ResponseCode.ERROR);
						resp.appendMsg("check credential failed.");
						exchange.getResponseSender().send(resp.toString());
					}
				}
			};
			tran.SendRequest();
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
