package com.exercise.todotest.enums;

public enum TodoField {

	ID("id"),
	TITLE("title"),
	DESCRIPTION("description"),
	TARGETDATE("targetDate"),
	PRIORITY("priority");
	
	private final String field;

	private TodoField(String field) {
		this.field = field;
	}
	
	public String getTodoField() {
		return this.field;
	}
	
}
