package com.handson.rocketmq.spring;

public class OperationInfo {

	private String userInfoId;
	
	private String operation;
	
	private long operationTime;

	public String getUserInfoId() {
		return userInfoId;
	}

	public void setUserInfoId(String userInfoId) {
		this.userInfoId = userInfoId;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public long getOperationTime() {
		return operationTime;
	}

	public void setOperationTime(long operationTime) {
		this.operationTime = operationTime;
	}

	
	
	
	
	
}
