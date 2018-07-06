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

		// every connection max current count 32
		poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, Integer.valueOf(scyllaConfig.get("cassandra.maxrequestsperconnection")));

		// connection to every node, at least 2 connection, at most 4 connection
		poolingOptions.setCoreConnectionsPerHost(HostDistance.LOCAL, Integer.valueOf(scyllaConfig.get("cassandra.coreconnectionsperhost")));
		poolingOptions.setMaxConnectionsPerHost(HostDistance.LOCAL, Integer.valueOf(scyllaConfig.get("cassandra.maxconnectionsperhost")));

		// addContactPoints:cassandra node IP withPort:cassandra, default is 9042
		// withCredentials:cassandra,username/password
		//if cassandra.yaml file authenticator:AllowAllAuthenticator, you can not configure this
		_cluster = Cluster.builder().addContactPoints(scyllaConfig.get("cassandra.contactpoints"))
				.withPort(Integer.valueOf(scyllaConfig.get("cassandra.port")))
				.withCredentials(scyllaConfig.get("cassandra.username"), scyllaConfig.get("cassandra.password"))
				.withPoolingOptions(poolingOptions).build();

		// build connection
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
