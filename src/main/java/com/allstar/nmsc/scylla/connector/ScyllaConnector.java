package com.allstar.nmsc.scylla.connector;

import java.util.Map;

import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;

import com.allstar.nmsc.scylla.config.ScyllaConfig;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;

/**
 * 
 * @author King.Gao
 *
 */
public class ScyllaConnector {

	private static ScyllaConnector _instance;
	private CassandraOperations _scyllaOperations;

	private Cluster _cluster;
	private Session _session;

	private static Object _LOCK = new Object();

	public ScyllaConnector() {

		Map<String, String> scyllaConfig = ScyllaConfig.getScyllaConfig();

		PoolingOptions poolingOptions = new PoolingOptions();

		// 每个连接的最大请求数,并发数,32 ;
		poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL,
				Integer.valueOf(scyllaConfig.get("cassandra.maxrequestsperconnection")));

		// 表示和集群里的每个节点机器至少有2个连接 最多有4个连接
		poolingOptions.setCoreConnectionsPerHost(HostDistance.LOCAL,
				Integer.valueOf(scyllaConfig.get("cassandra.coreconnectionsperhost")));
		poolingOptions.setMaxConnectionsPerHost(HostDistance.LOCAL,
				Integer.valueOf(scyllaConfig.get("cassandra.maxconnectionsperhost")));

		// addContactPoints:cassandra节点ip withPort:cassandra节点端口 默认9042
		// withCredentials:cassandra用户名密码
		// 如果cassandra.yaml里authenticator：AllowAllAuthenticator 可以不用配置
		_cluster = Cluster.builder().addContactPoints(scyllaConfig.get("cassandra.contactpoints"))
				.withPort(Integer.valueOf(scyllaConfig.get("cassandra.port")))
				.withCredentials(scyllaConfig.get("cassandra.username"), scyllaConfig.get("cassandra.password"))
				.withPoolingOptions(poolingOptions).build();

		// 建立连接
		// session = cluster.connect("test");连接已存在的键空间
		_session = _cluster.connect(scyllaConfig.get("cassandra.keyspace"));

		_scyllaOperations = new CassandraTemplate(_session);
	}

	public CassandraOperations getTemplate() {
		return this._scyllaOperations;
	}

	public static ScyllaConnector instance() {
		if (null == _instance) {
			synchronized (_LOCK) {
				if (null == _instance) {
					_instance = new ScyllaConnector();
				}
			}
		}

		return _instance;
	}
}
