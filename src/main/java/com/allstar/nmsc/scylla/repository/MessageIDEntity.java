package com.allstar.nmsc.scylla.repository;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table(value = "rcs_message_id_index")
public class MessageIDEntity
{
	@PrimaryKeyColumn(name = "session_key", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String session_key;

	@PrimaryKeyColumn(name = "tenant_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	private String tenant_id;

	@PrimaryKeyColumn(name = "msg_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
	private String message_id;

	@Column(value = "msg_index")
	private long message_index;

	public MessageIDEntity()
	{
	}

	public MessageIDEntity(String session_key, String tenant_id, String message_id, long message_index)
	{
		this.session_key = session_key;
		this.tenant_id = tenant_id;
		this.message_id = message_id;
		this.message_index = message_index;
	}

	public String getSession_key()
	{
		return session_key;
	}

	public void setSession_key(String session_key)
	{
		this.session_key = session_key;
	}

	public String getTenant_id()
	{
		return tenant_id;
	}

	public void setTenant_id(String tenant_id)
	{
		this.tenant_id = tenant_id;
	}

	public String getMessage_id()
	{
		return message_id;
	}

	public void setMessage_id(String message_id)
	{
		this.message_id = message_id;
	}

	public long getMessage_index()
	{
		return message_index;
	}

	public void setMessage_index(long message_index)
	{
		this.message_index = message_index;
	}
}
