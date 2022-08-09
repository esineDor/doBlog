package com.todo.doBlog.objects;

public class SimpleObject {

	protected String type;
	protected String content;
	
	protected Integer index;

	public String getType() {
		return type;
	}

	public String getContent() {
		return content;
	}

	public Integer getIndex() {
		return index;
	}

	public SimpleObject(String type, String content, Integer index) {
		super();
		this.type = type;
		this.content = content;
		this.index = index;
	}
	
	public SimpleObject(String type, String content) {
		super();
		this.type = type;
		this.content = content;
	}
	
}
