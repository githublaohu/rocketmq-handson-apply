package com.handson.rocketmq.spring;

public class ResultObject<T> {

	public int code;
	
	public String message;
	
	public T data;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "ResultObject [code=" + code + ", message=" + message + ", data=" + data + "]";
	}
	
	
	
}
