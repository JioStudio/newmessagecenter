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
import com.allstar.cintransaction.cinmessage.CinHeader;
import com.allstar.cintransaction.cinmessage.CinHeaderType;
import com.allstar.cintransaction.cinmessage.CinRequest;
import com.allstar.cintransaction.cinmessage.CinRequestMethod;
import com.allstar.cintransaction.cinmessage.CinResponse;
import com.allstar.cintransaction.cinmessage.CinResponseCode;
import com.allstar.cinutil.CinConvert;
import com.allstar.event.CinInnerServiceEvent;
import com.allstar.nmsc.scylla.dao.MessageDao;
import com.allstar.nmsc.scylla.dao.SessionInfoDao;
import com.allstar.nmsc.scylla.repository.MessageEntity;
import com.allstar.nmsc.util.MessageConstant;
import com.allstar.nmsc.util.Response;
import com.allstar.nmsc.util.ResponseCode;
import com.networknt.body.BodyHandler;

/**
 * 
 * A send message to B with http request, extMap: optional,
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
			String message = bodyMap.get("message");// CinRequest Message
			String groupId = bodyMap.get("groupId");
			String extMap = bodyMap.get("extMap");// extend column of message
			String tenantId = bodyMap.get("tenantId");

//			String credential = bodyMap.get("credential");
//			String fpId = bodyMap.get("fpId");// 0A878C7617B20000016170429C090000017E0100000000017FAD
//			String type = bodyMap.get("type");// message type
//			String encrypt = bodyMap.get("encrypt");
//			String status = bodyMap.get("status");// neng li zhi
//			String version = bodyMap.get("version");// optional

			Assert.notNull(to, "to must be not null.");
			Assert.notNull(from, "from must be not null.");
			Assert.notNull(extMap, "extMap must be not null.");
			Assert.notNull(groupId, "groupId must be not null.");
			Assert.notNull(message, "message must be not null.");
			Assert.notNull(messageId, "messageId must be not null.");

			long senderId = Long.valueOf(from);
			long receiverId = Long.valueOf(to);

			// CinRequest request = new
			// CinRequest(CinRequestMethod.InnerService);
			// request.addHeader(new CinHeader(CinHeaderType.Event,
			// CinInnerServiceEvent.CheckCredential));// TODO to be add in
			// cincommon
			// request.addHeader(new CinHeader(CinHeaderType.From, senderId));
			// request.addHeader(new CinHeader(CinHeaderType.Fpid, from));
			// request.addHeader(new CinHeader(CinHeaderType.Type, type));
			// request.addHeader(new CinHeader(CinHeaderType.Credential,
			// credential));
			// CinRouter.setRoute(request, CinServiceName.UserCacheCenter);
			// CinTransaction tran =
			// CinStack.instance().createTransaction(request);
			// tran.TransactionEvent = new CinTransactionEvent() {
			//
			// @Override
			// public void onTimeout(CinTransaction trans)
			// {
			// }
			//
			// @Override
			// public void onSendFailed(CinTransaction trans)
			// {
			// Response resp = new Response(ResponseCode.ERROR);
			// exchange.getResponseSender().send(resp.toString());
			// }
			//
			// @Override
			// public void onResponseReceived(CinTransaction trans, CinResponse
			// response)
			// {
			// if (response.isResponseCode(CinResponseCode.OK))
			// {
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
				entity.setMessage_content(message);// message need convert to
													// text ?
				entity.setGroup_sender(Long.valueOf(groupId));
				entity.setMsg_ext(map_ext);
				entity.setTenant_id(tenantId);

				long maxIndex = new MessageDao().getMaxIndex(entity.getSession_key(), tenantId);
				long last_index = maxIndex + 1;
				entity.setMessage_index(last_index);

				new MessageDao().insertMessage(entity);
				new SessionInfoDao().updateSessionInfo(senderId, receiverId, last_index, tenantId);

				// 2. send response
				JSONObject resp = new JSONObject();
				resp.put("messageIndex", last_index);
				resp.put("dateTime", entity.getMessage_time().getTime());

				Response r = new Response(ResponseCode.OK);
				r.put("resp", resp);
				exchange.getResponseSender().send(r.toString());

				// 3. cast http message and send it to MSC
				sendMessage(CinConvert.hexToBytes(message));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Response resp = new Response(ResponseCode.ERROR);
				exchange.getResponseSender().send(resp.toString());
			}
			// }
			// else
			// {
			// Response resp = new Response(ResponseCode.ERROR);
			// resp.appendMsg("check credential failed.");
			// exchange.getResponseSender().send(resp.toString());
			// }
			// }
			// };
			// tran.SendRequest();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Response resp = new Response(ResponseCode.ERROR);
			resp.appendMsg(e.getMessage());
			exchange.getResponseSender().send(resp.toString());
		}
		exchange.endExchange();
	}

	private void sendMessage(byte[] message)
	{
		CinRequest request  = new CinRequest(CinRequestMethod.InnerService);
		request.addHeader(new CinHeader(CinHeaderType.Event, CinInnerServiceEvent.SendMessage));
		request.addBody(message);// message is CinRequest-Message Request
		CinRouter.setRoute(request, CinServiceName.MessageCenter);
		CinTransaction tran = CinStack.instance().createTransaction(request);
		tran.TransactionEvent = new CinTransactionEvent() {

			@Override
			public void onResponseReceived(CinTransaction trans, CinResponse response)
			{
				if (response.isResponseCode(CinResponseCode.OK))
				{
					// do nothing
					System.out.println("--------------OK---------");
				}
				else
				{
					System.out.println("--------------send messge with not ok response code---------");
				}
			}

			@Override
			public void onSendFailed(CinTransaction trans) {
				System.out.println("--------------send message onSendFailed---------");
			}

			@Override
			public void onTimeout(CinTransaction trans) {
				// TODO Auto-generated method stub
			}
		};
		tran.SendRequest();
	}
}
