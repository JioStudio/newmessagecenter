package com.allstar.nmsc;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Methods;

import com.allstar.cinconnection.CinStack;
import com.allstar.cinutil.CinCommonHelper;
import com.allstar.nmsc.handler.ExtMessageAppendHandler;
import com.allstar.nmsc.handler.ExtMessageRemoveHandler;
import com.allstar.nmsc.handler.ExtMessageUpdateHandler;
import com.allstar.nmsc.handler.ExtMessageUpdateOneHandler;
import com.allstar.nmsc.handler.MessageGetHandler;
import com.allstar.nmsc.handler.MessageSendHandler;
import com.allstar.nmsc.handler.MessageSessionUpdateHandler;
import com.networknt.server.HandlerProvider;

/**
 * Initialize Http request path router table, CinCommonHelper
 * 
 * @since 2018-06-29
 * @author vincent.ma
 */
public class PathHandlerProvider implements HandlerProvider
{
	CinStack _cinStack;
	@Override
	public HttpHandler getHandler()
	{
		try
		{
			CinCommonHelper.Initialize();
			
			// TODO add cinStack or not to be confirm.
			_cinStack = CinStack.instance();
			_cinStack.listen("0.0.0.0", 9001);
			_cinStack.registerDefaultTransactionCreated(new TransactionCreated());
			
			System.out.println("--- initialize http request path router table OK---");
			RoutingHandler handler = Handlers.routing()
				.add(Methods.POST, "/v1/getmessage", new MessageGetHandler())
				.add(Methods.POST, "/v1/sendmessage", new MessageSendHandler())
				.add(Methods.POST, "/v1/deletemessage", new MessageSessionUpdateHandler())
				.add(Methods.POST, "/v1/extmessageremove", new ExtMessageRemoveHandler())
				.add(Methods.POST, "/v1/extmessageappend", new ExtMessageAppendHandler())
				.add(Methods.POST, "/v1/extmessageupdate", new ExtMessageUpdateHandler())
				.add(Methods.POST, "/v1/extmessageupdateone", new ExtMessageUpdateOneHandler());
			
			return handler;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			System.out.println("--- initialize http request path router table Exception---" + e.getMessage());
		}
		return null;
	}
}
