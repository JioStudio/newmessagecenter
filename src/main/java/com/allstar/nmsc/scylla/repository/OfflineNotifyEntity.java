package com.allstar.nmsc.scylla.repository;

import java.util.Date;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table(value = "rcs_offline_notify")
public class OfflineNotifyEntity
{

	@PrimaryKeyColumn(name = "receiver_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private long receiver_id;
	
	/* like: tenant_jiochat */
	@PrimaryKeyColumn(name = "tenant_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	private String tenant_id;
	
	@PrimaryKeyColumn(name = "notify_index", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
	private long notify_index;

	@Column(value = "sender_id")
	private long sender_id;

	@Column(value = "notify_content")
	private String notify_content;

	@Column(value = "notify_type")
	private int notify_type;

	@Column(value = "notify_status")
	private int notify_status;

	@Column(value = "notify_time")
	private Date notify_time;

	public long getReceiver_id()
	{
		return receiver_id;
	}

	public void setReceiver_id(long receiver_id)
	{
		this.receiver_id = receiver_id;
	}

	public long getNotify_index()
	{
		return notify_index;
	}

	public void setNotify_index(long notify_index)
	{
		this.notify_index = notify_index;
	}

	public long getSender_id()
	{
		return sender_id;
	}

	public void setSender_id(long sender_id)
	{
		this.sender_id = sender_id;
	}

	public String getNotify_content()
	{
		return notify_content;
	}

	public void setNotify_content(String notify_content)
	{
		this.notify_content = notify_content;
	}

	public int getNotify_type()
	{
		return notify_type;
	}

	public void setNotify_type(int notify_type)
	{
		this.notify_type = notify_type;
	}

	public int getNotify_status()
	{
		return notify_status;
	}

	public void setNotify_status(int notify_status)
	{
		this.notify_status = notify_status;
	}

	public Date getNotify_time()
	{
		return notify_time;
	}

	public void setNotify_time(Date notify_time)
	{
		this.notify_time = notify_time;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}
	
}
