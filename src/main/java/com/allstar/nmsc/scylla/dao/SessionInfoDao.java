package com.allstar.nmsc.scylla.dao;

import java.util.Date;

import org.springframework.data.cassandra.core.CassandraOperations;

import com.allstar.nmsc.scylla.connector.ScyllaConnector;
import com.allstar.nmsc.scylla.repository.SessionInfoEntity;

/**
 * Scylla DB Operations for Message conversation information
 * 
 * @author King.Gao
 *
 */
public class SessionInfoDao
{
	/*
	 * This Method should be called when insert a new message record.
	 * 
	 * @param sender_id
	 *            Message Sender's User ID
	 * 
	 * @param receiver_id
	 *            Message Receiver's User ID
	 * 
	 * @param last_index
	 *            The last message Index
	 */
	public void updateSessionInfo(long sender_id, long receiver_id, long last_index, String tenant_id)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
		if (1 == last_index)
		{
			SessionInfoEntity senderInfo = new SessionInfoEntity();
			senderInfo.setSender_id(sender_id);
			senderInfo.setReceiver_id(receiver_id);
			senderInfo.setLast_index(last_index);
			senderInfo.setLast_opt_time(new Date());
			senderInfo.setTenant_id(tenant_id);

			op.batchOps().insert(senderInfo).execute();
		}
		else if (1 < last_index)
		{
			String senderCql = "UPDATE http_session_info SET last_index=%s WHERE receiver_id=%s AND sender_id=%s AND tenant_id='%s'";
			String receiverCql = "UPDATE http_session_info SET last_index=%s WHERE receiver_id=%s AND sender_id=%s AND tenant_id='%s'";

			op.getCqlOperations().execute(String.format(senderCql, last_index, sender_id, receiver_id, tenant_id));
			op.getCqlOperations().execute(String.format(receiverCql, last_index, receiver_id, sender_id, tenant_id));
		}
	}
}
