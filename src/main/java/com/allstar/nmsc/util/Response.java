package com.allstar.nmsc.util;

import org.springframework.util.Assert;
import com.alibaba.fastjson.JSONObject;

/**
 * build response
 * 
 * @author vincent.ma
 */
public class Response
{
	JSONObject resp;

	public Response(ResponseCode code)
	{
		resp = new JSONObject();
		resp.put("respcode", code.getIndex());
		resp.put("msg", code.getName());
	}

	/**
	 * default response: OK
	 */
	public Response()
	{
		resp = new JSONObject();
		resp.put("respcode", ResponseCode.OK.getIndex());
		resp.put("msg", ResponseCode.OK.getName());
	}

	public void put(String key, Object value)
	{
		Assert.notNull(key, "key must be not null.");
		if (key.equalsIgnoreCase("respcode") || key.equalsIgnoreCase("msg"))
			System.out.println("-->Error: when build response, key must be not 'respcode' or 'msg'.");

		resp.put(key, value);
	}

	public void appendMsg(String message)
	{
		if (resp == null)
			new Response();
		resp.put("msg", resp.get("msg") + "-" + message);
	}

	public String toString()
	{
		return resp.toJSONString();
	}
}
