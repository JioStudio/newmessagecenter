package com.allstar.nmsc.scylla.repository;

import java.util.Date;
import java.util.Map;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table(value = "rcs_message")
public class MessageEntity
{
	@PrimaryKeyColumn(name = "session_key", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String session_key;

	/* like: tenant_jiochat */
	@PrimaryKeyColumn(name = "tenant_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	private String tenant_id;

	@PrimaryKeyColumn(name = "msg_index", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
	private long message_index;

	@Column(value = "sender_id")
	private long sender_id;

	@Column(value = "receiver_id")
	private long receiver_id;

	@Column(value = "group_sender")
	private long group_sender;

	@Column(value = "msg_id")
	private String message_id;

	/* message value is a CinMessage */
	@Column(value = "msg_content")
	private String message_content;
	
	/* 0:has sent; 1:has delivery */
	@Column(value = "msg_status")
	private int message_status;

	/* message send time */
	@Column(value = "msg_time")
	private Date message_time;

	/* message delete flag, 0:not delete;1:delete, min user id */
	@Column(value = "delflag_min")
	private int delflag_min;

	/* message delete flag, 0:not delete;1:delete, max user id */
	@Column(value = "delflag_max")
	private int delflag_max;

	/* message extend column */
	@Column(value = "msg_ext")
	private Map<String, String> msg_ext;

	public MessageEntity()
	{
	}

	public MessageEntity(long sender_id, long receiver_id)
	{
		this.sender_id = sender_id;
		this.receiver_id = receiver_id;
		if (sender_id > receiver_id)
		{
			this.session_key = sender_id + "" + receiver_id;
		}
		else
		{
			this.session_key = receiver_id + "" + sender_id;
		}
		this.message_time = new Date();
	}

	public static String getSessionKey(String sender_id, String receiver_id)
	{
		return getSessionKey(Long.valueOf(sender_id), Long.valueOf(receiver_id));
	}

	public static String getSessionKey(long sender_id, long receiver_id)
	{
		String session_key = sender_id + "" + receiver_id;
		if (sender_id < receiver_id)
		{
			session_key = receiver_id + "" + sender_id;
		}
		return session_key;
	}

	public String getSession_key()
	{
		return session_key;
	}

	public String getTenant_id()
	{
		return tenant_id;
	}

	public void setTenant_id(String tenant_id)
	{
		this.tenant_id = tenant_id;
	}

	public void setSession_key(String session_key)
	{
		this.session_key = session_key;
	}

	public long getMessage_index()
	{
		return message_index;
	}

	public void setMessage_index(long message_index)
	{
		this.message_index = message_index;
	}

	public long getSender_id()
	{
		return sender_id;
	}

	public void setSender_id(long sender_id)
	{
		this.sender_id = sender_id;
	}

	public long getReceiver_id()
	{
		return receiver_id;
	}

	public void setReceiver_id(long receiver_id)
	{
		this.receiver_id = receiver_id;
	}

	public long getGroup_sender()
	{
		return group_sender;
	}

	public void setGroup_sender(long group_sender)
	{
		this.group_sender = group_sender;
	}

	public String getMessage_id()
	{
		return message_id;
	}

	public void setMessage_id(String message_id)
	{
		this.message_id = message_id;
	}

	public String getMessage_content()
	{
		return message_content;
	}

	public void setMessage_content(String message_content)
	{
		this.message_content = message_content;
	}

	public int getMessage_status()
	{
		return message_status;
	}

	public void setMessage_status(int message_status)
	{
		this.message_status = message_status;
	}

	public Date getMessage_time()
	{
		return message_time;
	}

	public void setMessage_time(Date message_time)
	{
		this.message_time = message_time;
	}

	public int getDelflag_min()
	{
		return delflag_min;
	}

	public void setDelflag_min(int delflag_min)
	{
		this.delflag_min = delflag_min;
	}

	public int getDelflag_max()
	{
		return delflag_max;
	}

	public void setDelflag_max(int delflag_max)
	{
		this.delflag_max = delflag_max;
	}

	public Map<String, String> getMsg_ext()
	{
		return msg_ext;
	}

	public void setMsg_ext(Map<String, String> msg_ext)
	{
		this.msg_ext = msg_ext;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("sender_id=").append(sender_id).append("\n");
		sb.append("receiver_id=").append(receiver_id).append("\n");
		sb.append("group_sender=").append(group_sender).append("\n");
		sb.append("session_key=").append(session_key).append("\n");
		sb.append("message_index=").append(message_index).append("\n");
		sb.append("message_id=").append(message_id).append("\n");
		sb.append("message_status=").append(message_status).append("\n");
		sb.append("message_time=").append(message_time).append("\n");
		sb.append("delflag_min=").append(delflag_min).append("\n");
		sb.append("delflag_max=").append(delflag_max).append("\n");

		return sb.toString();
	}
}
