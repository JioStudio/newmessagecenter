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
 * @author aminiy
 * 
 *         CinMessage extend property, all extend key-value save to msg_ext column.
 *         this interface is append to msg_ext column: msg_ext = msg_ext + value(like: {'key1':'value1','key2':'vaue2'})
 */
public class ExtMessageAppendHandler implements HttpHandler {

	@SuppressWarnings("unchecked")
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		
		try {
			HashMap<String, String>  bodyMap = (HashMap<String, String>) exchange.getAttachment(BodyHandler.REQUEST_BODY);
			String from = bodyMap.get("from");
			String to = bodyMap.get("to");
			String messageIndex = bodyMap.get("messageIndex");
			String extMap = bodyMap.get("extMap");// key1:value1,key2:value2
			
//			InputStream stream = exchange.getInputStream();
//			String requestBody = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));// System.lineSeparator()

			Assert.notNull(from, "from must be not null.");
			Assert.notNull(to, "to must be not null.");
			Assert.notNull(extMap, "extMap must be not null.");
			Assert.notNull(messageIndex, "messageIndex must be not null.");
			
			String sessionKey;
			long fromId = Long.valueOf(from);
			long toId = Long.valueOf(to);
			
			if(fromId > toId)
				sessionKey= fromId + "" + toId;
			else
				sessionKey= toId + "" + fromId;
			
			extMap = "{'" + extMap.replaceAll(":", "':'").replaceAll(",", "','") + "'}";
			new MessageDao().ExtMessageAppend(sessionKey, messageIndex, extMap);
			
			Response resp = new Response(ResponseCode.OK);
			exchange.getResponseSender().send(resp.toString());
		} catch (Exception e) {
			e.printStackTrace();
			
			Response resp = new Response(ResponseCode.ERROR);
			exchange.getResponseSender().send(resp.toString());
		}
		exchange.endExchange();
	}
	
//	private byte[] readRequestBody(HttpServerExchange httpExchange) throws IOException {
//        Pooled<ByteBuffer> pooledByteBuffer = httpExchange.getConnection().getBufferPool().allocate();
//        ByteBuffer byteBuffer = pooledByteBuffer.getResource();
//
//        byteBuffer.clear();
//
//        httpExchange.getRequestChannel().read(byteBuffer);
//        int pos = byteBuffer.position();
//        byteBuffer.rewind();
//        byte[] bytes = new byte[pos];
//        byteBuffer.get(bytes);
//
//        byteBuffer.clear();
//        pooledByteBuffer.free();
//        return bytes;
//    }
	
//	private String getRequestBody(HttpServerExchange exchange){
//		StringBuilder requestBody = new StringBuilder();
//		exchange.getRequestReceiver().receiveFullString((ex, data) -> {
//		    requestBody.append(data);
//		});
//		
//		return requestBody.toString();
//	}

}
