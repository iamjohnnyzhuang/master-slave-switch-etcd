package me.zhuangjy.etcd;

public class EtcdResult {
	// General values
	public String action;
	public EtcdNode node;
	public EtcdNode prevNode;

	// For errors
	public Integer errorCode;
	public String message;
	public String cause;
	public int errorIndex;

	public boolean isError() {
		return errorCode != null;
	}

	@Override
	public String toString() {
		return EtcdClient.format(this);
	}

	public String getAction() {
		return action;
	}

	public EtcdNode getNode() {
		return node;
	}

	public EtcdNode getPrevNode() {
		return prevNode;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public String getMessage() {
		return message;
	}

	public String getCause() {
		return cause;
	}

	public int getErrorIndex() {
		return errorIndex;
	}
}
