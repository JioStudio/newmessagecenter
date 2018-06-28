package com.allstar.nmsc;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.allstar.nmsc.handler.ExtMessageAppendHandler;
import com.allstar.nmsc.handler.ExtMessageRemoveHandler;
import com.allstar.nmsc.handler.ExtMessageUpdateHandler;
import com.allstar.nmsc.handler.ExtMessageUpdateOneHandler;
import com.allstar.nmsc.handler.MessageGetBatchHandler;
import com.allstar.nmsc.handler.MessageGetHandler;
import com.allstar.nmsc.handler.MessageSendHandler;
import com.allstar.nmsc.handler.MessageSessionUpdateHandler;
import com.networknt.server.HandlerProvider;

/**
 * Initialize Http request path router table, CinCommonHelper
 * 
 * @author vincent.ma
 */
public class PathHandlerProvider implements HandlerProvider
{
	static final Logger logger = LoggerFactory.getLogger(PathHandlerProvider.class);

	@Override
	public HttpHandler getHandler()
	{
		try
		{
//			CinCommonHelper.Initialize();
			logger.info("--- initialize http request path router table OK---");
			System.out.println("--- initialize http request path router table OK---");
			RoutingHandler handler = Handlers.routing()
				.add(Methods.POST, "/v1/getmessage", new MessageGetHandler())
				.add(Methods.POST, "/v1/getbatchmessage", new MessageGetBatchHandler())
				.add(Methods.POST, "/v1/addmessage", new MessageSendHandler())
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
			
			logger.info("--- initialize http request path router table Exception---" + e.getMessage());
			System.out.println("--- initialize http request path router table Exception---" + e.getMessage());
		}
		return null;
	}
}
