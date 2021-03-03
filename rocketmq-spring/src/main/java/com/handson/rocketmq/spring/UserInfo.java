package com.handson.rocketmq.spring;

public class UserInfo {

	private Long id;
	
	private String name;
	
	private String action;
	
	private String operation;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	@Override
	public String toString() {
		return "UserInfo [id=" + id + ", name=" + name + ", action=" + action + ", operation=" + operation + "]";
	}

	
}
