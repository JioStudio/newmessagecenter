package com.allstar.nmsc.scylla.repository;

import java.util.Date;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table(value = "rcs_broadcast_message")
public class BroadcastMessageEntity
{

	@PrimaryKeyColumn(name = "sender_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private long sender_id;

	@PrimaryKeyColumn(name = "msg_index", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
	private long msg_index;

	@Column(value = "msg_id")
	private String msg_id;

	@Column(value = "msg_content")
	private String msg_content;

	@Column(value = "msg_time")
	private Date msg_time;

	public long getSender_id()
	{
		return sender_id;
	}

	public void setSender_id(long sender_id)
	{
		this.sender_id = sender_id;
	}

	public long getMsg_index()
	{
		return msg_index;
	}

	public void setMsg_index(long msg_index)
	{
		this.msg_index = msg_index;
	}

	public String getMsg_id()
	{
		return msg_id;
	}

	public void setMsg_id(String msg_id)
	{
		this.msg_id = msg_id;
	}

	public String getMsg_content()
	{
		return msg_content;
	}

	public void setMsg_content(String msg_content)
	{
		this.msg_content = msg_content;
	}

	public Date getMsg_time()
	{
		return msg_time;
	}

	public void setMsg_time(Date msg_time)
	{
		this.msg_time = msg_time;
	}
}
