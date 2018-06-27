package com.allstar.nmsc.scylla.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.RowMapper;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;

import com.allstar.nmsc.scylla.connector.ScyllaConnector;
import com.allstar.nmsc.scylla.repository.MessageEntity;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.DriverException;

/**
 * Scylla DB Operations for Message
 * 
 * @author King.Gao
 *
 *         TODO: 获取最大索引的MAX()尚未实现 TODO: 批量删除消息尚未实现
 */
public class MessageDao {

	@org.springframework.data.cassandra.repository.Query("SELECT MAX(msg_index) FROM rcs_message WHERE session_key=?")
	public void delSingleMessage(long operator_id, String session_key, long msg_index) {
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		MessageEntity m = findMessageByMsgIndex(session_key, msg_index);
		if (null != m) {
			if (session_key.startsWith(String.valueOf(operator_id)))
				m.setDelflag_max(2);
			else {
				m.setDelflag_min(2);
			}
			op.update(m);
		}
	}

	public void delSingleMessage(long operator_id, String session_key, String message_id) {
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		MessageEntity m = findMessageByMsgId(session_key, message_id);
		if (null != m) {
			if (session_key.startsWith(String.valueOf(operator_id)))
				m.setDelflag_max(2);
			else {
				m.setDelflag_min(2);
			}
			op.update(m);
		}
	}

	public void updateMessageStatus(String session_key, String message_id) {
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		MessageEntity m = findMessageByMsgId(session_key, message_id);
		if (null != m) {
			m.setMessage_status(0);
			op.update(m);
		}
	}

	public void updateMessage4JioMoney(String session_key, String message_id, String message_content) {
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		MessageEntity m = findMessageByMsgId(session_key, message_id);
		if (null != m) {
			m.setMessage_content(message_content); // ByteBuffer.wrap(message_content)
			op.update(m);
		}
	}

	public MessageEntity findMessageByMsgId(String session_key, String message_id) {
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		return op.selectOne(
				Query.query(Criteria.where("session_key").is(session_key), Criteria.where("msg_id").is(message_id)),
				MessageEntity.class);
	}

	public MessageEntity findMessageByMsgIndex(String session_key, long msg_index) {
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		return op.selectOne(
				Query.query(Criteria.where("session_key").is(session_key), Criteria.where("msg_index").is(msg_index)),
				MessageEntity.class);
	}

	public List<Integer> findUnDeliveryMsgIndexes(long requester_id, String session_key) {
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		String user = "delflag_min";
		if (session_key.startsWith(String.valueOf(requester_id))) {
			user = "delflag_max";
		}

		List<MessageEntity> list = op.select(Query.query(Criteria.where("session_key").is(session_key),
				Criteria.where("msg_status").is(1), Criteria.where(user).is(0)), MessageEntity.class);

		List<Integer> indexes = new ArrayList<Integer>();

		if (null != list && !list.isEmpty()) {
			for (MessageEntity m : list) {
				indexes.add((int) m.getMessage_index());
			}
		}

		return indexes;
	}

	public void insertMessage(MessageEntity msg) {
		CassandraOperations op = ScyllaConnector.instance().getTemplate();

		op.insert(msg);
	}
	
	public MessageEntity findMessageBySessionKey(String sessionKey)
	{
		System.out.println("test method is called.");
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
//		List<MessageEntity> list = op.select(Query.empty(), MessageEntity.class);
//		if(list!=null){
//			System.out.println(list.size());
//		}

		// test max
//		Select select = QueryBuilder.select().max("msg_index").from("rcs_message");// where...
//		Long count = op.getCqlOperations().queryForObject(select, Long.class);
//		System.out.println("----->>Count=" + count);
		
		// spring count method
//		Select select = QueryBuilder.select().countAll().from("");
//		Long count = getCqlOperations().queryForObject(select, Long.class);
//		return getCqlOperations().queryForResultSet(select).iterator().hasNext();
		
		return op.selectOne(Query.query(Criteria.where("session_key").is(sessionKey)), MessageEntity.class);
	}

	public Long getMaxIndex(String sessionKey)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
//		op.getCqlOperations().query("", new RowCallbackHandler() {
//			
//			@Override
//			public void processRow(Row row) throws DriverException {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		RowMapper<Long> mapper  = new RowMapper<Long>() {
			@Override
			public Long mapRow(Row row, int rowNum) throws DriverException {
				return row.getLong(0);
			}
		};
		List <Long>list = op.getCqlOperations().query("SELECT MAX(msg_index) FROM rcs_message WHERE session_key ='" + sessionKey+"'", mapper);
		
		if(list!=null && list.size() > 0)
			return list.get(0);
		else
			return 0L;
	}
	
	/**
	 * @param sessionKey
	 * 		session key: Auserid + Buserid
	 * @param messageIndex
	 * 		messageIndex: message session num
	 * @param extMap
	 * 		key, like {'key1':'value1','key2':'value2'}, at least one key.
	 */
	public void ExtMessageAppend(String sessionKey, String messageIndex, String extMap)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
		op.getCqlOperations().execute("UPDATE rcs_message set msg_ext = msg_ext + " + extMap +" WHERE session_key='"+ sessionKey + "' AND msg_index=" + messageIndex);
	}
	
	public void ExtMessageUpdate(String sessionKey, String messageIndex, String extMap) throws Exception
	{
		// update or clear
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
		op.getCqlOperations().execute("UPDATE rcs_message set msg_ext = " + extMap +" WHERE session_key='"+ sessionKey + "' AND msg_index=" + messageIndex);
	}
	
	/**
	 * @param sessionKey
	 * 		session key: Auserid + Buserid
	 * @param messageIndex
	 * 		messageIndex: message session num
	 * @param extKey
	 * 		key, like name
	 * @param extValue
	 * 		value, like vincent.ma
	 */
	public void ExtMessageUpdateOne(String sessionKey, String messageIndex, String extKey, String extValue)
	{
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
		op.getCqlOperations().execute("UPDATE rcs_message set msg_ext['" + extKey + "'] = '" + extValue +"' WHERE session_key='"+ sessionKey + "' AND msg_index=" + messageIndex);
	}
	
	/**
	 * @param sessionKey
	 * 		session key: Auserid + Buserid
	 * @param messageIndex
	 * 		messageIndex: message session num
	 * @param extKey
	 * 		key, like {'key1','key2'}, at least one key.
	 */
	public void ExtMessageRemove(String sessionKey, String messageIndex, String extKey)
	{
		// DELETE favs['author'] FROM users WHERE id = 'jsmith';
		// UPDATE users SET favs = favs - { 'movie', 'band'} WHERE id = 'jsmith';
		CassandraOperations op = ScyllaConnector.instance().getTemplate();
		op.getCqlOperations().execute("UPDATE rcs_message set msg_ext = msg_ext - " + extKey +" WHERE session_key='"+ sessionKey + "' AND msg_index=" + messageIndex);
	}

}
