package com.cxytiandi.elasticjob.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "elasticJob.zk")
public class ZookeeperProperties {
	/**
	 * 连接Zookeeper服务器的列表. 包括IP地址和端口号. 多个地址用逗号分隔. 如: host1:2181,host2:2181
	 */
	private String serverLists;

	/**
	 * 命名空间.
	 */
	private String namespace;

	/**
	 * 等待重试的间隔时间的初始值. 单位毫秒.
	 */
	private int baseSleepTimeMilliseconds = 1000;

	/**
	 * 等待重试的间隔时间的最大值. 单位毫秒.
	 */
	private int maxSleepTimeMilliseconds = 3000;

	/**
	 * 最大重试次数.
	 */
	private int maxRetries = 3;

	/**
	 * 会话超时时间. 单位毫秒.
	 */
	private int sessionTimeoutMilliseconds;

	/**
	 * 连接超时时间. 单位毫秒.
	 */
	private int connectionTimeoutMilliseconds;

	/**
	 * 连接Zookeeper的权限令牌. 缺省为不需要权限验证.
	 */
	private String digest;

	public String getServerLists() {
		return serverLists;
	}

	public void setServerLists(String serverLists) {
		this.serverLists = serverLists;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public int getBaseSleepTimeMilliseconds() {
		return baseSleepTimeMilliseconds;
	}

	public void setBaseSleepTimeMilliseconds(int baseSleepTimeMilliseconds) {
		this.baseSleepTimeMilliseconds = baseSleepTimeMilliseconds;
	}

	public int getMaxSleepTimeMilliseconds() {
		return maxSleepTimeMilliseconds;
	}

	public void setMaxSleepTimeMilliseconds(int maxSleepTimeMilliseconds) {
		this.maxSleepTimeMilliseconds = maxSleepTimeMilliseconds;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public int getSessionTimeoutMilliseconds() {
		return sessionTimeoutMilliseconds;
	}

	public void setSessionTimeoutMilliseconds(int sessionTimeoutMilliseconds) {
		this.sessionTimeoutMilliseconds = sessionTimeoutMilliseconds;
	}

	public int getConnectionTimeoutMilliseconds() {
		return connectionTimeoutMilliseconds;
	}

	public void setConnectionTimeoutMilliseconds(int connectionTimeoutMilliseconds) {
		this.connectionTimeoutMilliseconds = connectionTimeoutMilliseconds;
	}

	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}

}
