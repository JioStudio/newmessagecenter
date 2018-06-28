package com.allstar.nmsc.scylla.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.RowMapper;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;

import com.allstar.nmsc.scylla.connector.ScyllaConnector;
import com.allstar.nmsc.scylla.repository.MessageEntity;
import com.allstar.nmsc.scylla.repository.MessageIDEntity;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.DriverException;

/**
 * Scylla DB Operations for Message
 * 
 * @author King.Gao
 *
 */
public class MessageDao
{

	/**
	 * Insert message to DB when send message
	 * 
	 * @param msg
	 *            Message Entity
	 */
	public void insertMessage(MessageEntity msg)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
		MessageIDEntity idIndex = new MessageIDEntity(msg.getSession_key(), msg.getTenant_id(), msg.getMessage_id(), msg.getMessage_index());
		op.batchOps().insert(msg, idIndex).execute();
	}

	/**
	 * Select message entity according to message index
	 * 
	 * @param session_key
	 *            Session Key
	 * @param tenant_id
	 *            Tenant ID
	 * @param message_id
	 *            Message ID
	 * @return
	 */
	public MessageEntity findMessageByMsgId(String session_key, String tenant_id, String message_id)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		// Get message index by message id
		MessageIDEntity idIndex = op.selectOne(Query.query(Criteria.where("session_key").is(session_key), Criteria.where("tenant_id").is(tenant_id), Criteria.where("msg_id").is(message_id)),
				MessageIDEntity.class);

		if (null != idIndex)
		{
			// get message by message index
			return findMessageByMsgIndex(session_key, tenant_id, idIndex.getMessage_index());
		}
		else
		{
			return null;
		}
	}

	/**
	 * Select message entity according to message index
	 * 
	 * @param session_key
	 *            Session Key
	 * @param tenant_id
	 *            Tenant ID
	 * @param msg_index
	 *            Message Index
	 * @return
	 */
	public MessageEntity findMessageByMsgIndex(String session_key, String tenant_id, long msg_index)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		return op.selectOne(Query.query(Criteria.where("session_key").is(session_key), Criteria.where("tenant_id").is(tenant_id), Criteria.where("msg_index").is(msg_index)), MessageEntity.class);
	}

	/**
	 * Get current max message index of conversation
	 * 
	 * @param session_key
	 *            Session Key
	 * @return
	 */
	public Long getMaxIndex(String session_key, String tenant_id)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
		RowMapper<Long> mapper = new RowMapper<Long>()
		{
			@Override
			public Long mapRow(Row row, int rowNum) throws DriverException
			{
				return row.getLong(0);
			}
		};

		String cql = "SELECT MAX(msg_index) FROM rcs_message WHERE session_key ='%s' AND tenant_id='%s'";
		List<Long> list = op.getCqlOperations().query(String.format(cql, session_key, tenant_id), mapper);

		if (list != null && list.size() > 0)
			return list.get(0);
		else
			return 0L;
	}

	/**
	 * Select history messages according message index
	 * 
	 * @param session_key
	 *            Session Key
	 * @param tenant_id
	 *            Tenant ID
	 * @param start_index
	 *            Query start Index
	 * @param number
	 *            How many message records to get
	 * @return
	 */
	public List<MessageEntity> findHistoryMessages(String session_key, String tenant_id, long start_index, long number)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
		long end_index = start_index - number + 1;
		if (end_index < 0)
		{
			end_index = 0;
		}
		String cql = "SELECT * FROM rcs_message WHERE session_key='%s' AND tenant_id='%s' AND msg_index <= %s AND msg_index >=%s";

		return op.select(String.format(cql, session_key, tenant_id, start_index, end_index), MessageEntity.class);
	}

	// --- Below method not implement and test till now

	public void delSingleMessage(long operator_id, String session_key, String tenant_id, long msg_index)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		MessageEntity m = findMessageByMsgIndex(session_key, tenant_id, msg_index);
		if (null != m)
		{
			if (session_key.startsWith(String.valueOf(operator_id)))
				m.setDelflag_max(2);
			else
			{
				m.setDelflag_min(2);
			}
			op.update(m);
		}
	}

	public void delSingleMessage(long operator_id, String session_key, String tenant_id, String message_id)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		MessageEntity m = findMessageByMsgId(session_key, tenant_id, message_id);
		if (null != m)
		{
			if (session_key.startsWith(String.valueOf(operator_id)))
				m.setDelflag_max(2);
			else
			{
				m.setDelflag_min(2);
			}
			op.update(m);
		}
	}

	public void updateMessageStatus(String session_key, String tenant_id, String message_id)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		MessageEntity m = findMessageByMsgId(session_key, tenant_id, message_id);
		if (null != m)
		{
			m.setMessage_status(0);
			op.update(m);
		}
	}

	public void updateMessage4JioMoney(String session_key, String tenant_id, String message_id, String message_content)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		MessageEntity m = findMessageByMsgId(session_key, tenant_id, message_id);
		if (null != m)
		{
			m.setMessage_content(message_content);
			op.update(m);
		}
	}

	public List<Integer> findUnDeliveryMsgIndexes(long requester_id, String session_key)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		String user = "delflag_min";
		if (session_key.startsWith(String.valueOf(requester_id)))
		{
			user = "delflag_max";
		}

		List<MessageEntity> list = op.select(Query.query(Criteria.where("session_key").is(session_key), Criteria.where("msg_status").is(1), Criteria.where(user).is(0)), MessageEntity.class);

		List<Integer> indexes = new ArrayList<Integer>();

		if (null != list && !list.isEmpty())
		{
			for (MessageEntity m : list)
			{
				indexes.add((int) m.getMessage_index());
			}
		}

		return indexes;
	}

	/**
	 * @param sessionKey
	 *            combination of Sender's User ID and Receiver's User ID
	 * @param messageIndex
	 *            message index of one conversation
	 * @param extMap
	 *            key, like {'key1':'value1','key2':'value2'}, at least one key.
	 */
	public void ExtMessageAppend(String sessionKey, String messageIndex, String extMap)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
		op.getCqlOperations().execute("UPDATE rcs_message set msg_ext = msg_ext + " + extMap + " WHERE session_key='" + sessionKey + "' AND msg_index=" + messageIndex);
	}

	public void ExtMessageUpdate(String sessionKey, String messageIndex, String extMap) throws Exception
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
		op.getCqlOperations().execute("UPDATE rcs_message set msg_ext = " + extMap + " WHERE session_key='" + sessionKey + "' AND msg_index=" + messageIndex);
	}

	public void ExtMessageUpdateOne(String sessionKey, String messageIndex, String extKey, String extValue)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
		op.getCqlOperations().execute("UPDATE rcs_message set msg_ext['" + extKey + "'] = '" + extValue + "' WHERE session_key='" + sessionKey + "' AND msg_index=" + messageIndex);
	}

	public void ExtMessageRemove(String sessionKey, String messageIndex, String extKey)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
		op.getCqlOperations().execute("UPDATE rcs_message set msg_ext = msg_ext - " + extKey + " WHERE session_key='" + sessionKey + "' AND msg_index=" + messageIndex);
	}
}
