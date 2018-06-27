package com.allstar.nmsc.util;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;

public class Response {
	
	JSONObject resp;
	public Response(ResponseCode code)
	{
		resp = new JSONObject();
		resp.put("respcode", code.getIndex());
		resp.put("msg", code.getName());
	}
	
	/**
	 * default is OK response
	 */
	public Response()
	{
		resp = new JSONObject();
		resp.put("respcode", ResponseCode.OK.getIndex());
		resp.put("msg", ResponseCode.OK.getName());
	}
	
	public void put(String key, Object value) throws Exception
	{
		Assert.notNull(key, "key must be not null.");
		if(key.equalsIgnoreCase("respcode") || key.equalsIgnoreCase("msg"))
			throw new Exception("key must be not 'respcode' or 'msg'.");
		
		resp.put(key, value);
	}
	
	public String toString()
	{
		return resp.toJSONString();
	}
}
