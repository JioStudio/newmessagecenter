package com.allstar.nmsc.scylla.repository;

import java.util.Date;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table(value = "http_session_info")
public class SessionInfoEntity {
	@PrimaryKeyColumn(name = "receiver_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private long receiver_id;
	@PrimaryKeyColumn(name = "tenant_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	private String tenant_id;
	@PrimaryKeyColumn(name = "sender_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
	private long sender_id;

	@Column(value = "last_index")
	private long last_index;

	@Column(value = "last_read_index")
	private long last_read_index;

	@Column(value = "unread_num")
	private long unread_number;

	@Column(value = "last_opt_time")
	private Date last_opt_time;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("---SessionInfo---\n");
		sb.append("sender_id=").append(sender_id).append("\n");
		sb.append("receiver_id=").append(receiver_id).append("\n");
		sb.append("last_index=").append(last_index).append("\n");
		sb.append("last_read_index=").append(last_read_index).append("\n");
		sb.append("unread_number=").append(unread_number).append("\n");

		return sb.toString();
	}

	public long getSender_id() {
		return sender_id;
	}

	public void setSender_id(long sender_id) {
		this.sender_id = sender_id;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public long getReceiver_id() {
		return receiver_id;
	}

	public void setReceiver_id(long receiver_id) {
		this.receiver_id = receiver_id;
	}

	public long getLast_index() {
		return last_index;
	}

	public void setLast_index(long last_index) {
		this.last_index = last_index;
	}

	public long getLast_read_index() {
		return last_read_index;
	}

	public void setLast_read_index(long last_read_index) {
		this.last_read_index = last_read_index;
	}

	public long getUnread_number() {
		return unread_number;
	}

	public void setUnread_number(long unread_number) {
		this.unread_number = unread_number;
	}

	public Date getLast_opt_time() {
		return last_opt_time;
	}

	public void setLast_opt_time(Date last_opt_time) {
		this.last_opt_time = last_opt_time;
	}
}
