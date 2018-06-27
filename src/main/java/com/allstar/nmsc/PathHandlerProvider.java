
package com.allstar.nmsc;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

import com.allstar.nmsc.handler.ExtMessageAppendHandler;
import com.allstar.nmsc.handler.ExtMessageRemoveHandler;
import com.allstar.nmsc.handler.ExtMessageUpdateHandler;
import com.allstar.nmsc.handler.ExtMessageUpdateOneHandler;
import com.allstar.nmsc.handler.MessageGetHandler;
import com.allstar.nmsc.handler.MessageInsertHandler;
import com.allstar.nmsc.handler.MessageSessionUpdateHandler;
import com.networknt.server.HandlerProvider;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        return Handlers.routing()
	        .add(Methods.POST, "/v1/getmessage", new MessageGetHandler())
	        .add(Methods.POST, "/v1/addmessage", new MessageInsertHandler())
	        .add(Methods.POST, "/v1/deletemessage", new MessageSessionUpdateHandler())
	        .add(Methods.POST, "/v1/extmessageremove", new ExtMessageRemoveHandler())
	        .add(Methods.POST, "/v1/extmessageappend", new ExtMessageAppendHandler())
	        .add(Methods.POST, "/v1/extmessageupdate", new ExtMessageUpdateHandler())
	        .add(Methods.POST, "/v1/extmessageupdateone", new ExtMessageUpdateOneHandler())
        ;
    }
}
