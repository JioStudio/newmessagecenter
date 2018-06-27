package com.allstar.nmsc.util;

public enum ResponseCode {

	OK("ok", 1), ERROR("error", 2), NotExists("not exists",3), Exception("exception", 4);
	
	private int index; 
	private String name; 
	
	private ResponseCode(String name, int code){
		this.index = code;
		this.name = name;
	}

	//getter setter
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
