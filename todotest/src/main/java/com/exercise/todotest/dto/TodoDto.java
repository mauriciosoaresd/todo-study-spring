package com.exercise.todotest.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TodoDto {
	private int id;
	@NotEmpty
	@Size(min = 2, max = 20)
	private String title;
	@NotNull
	@Size(min = 10, max = 200)
	private String description;
	@NotNull
	@Future
	private LocalDate targetDate;
	@NotNull
	@Min(value = 1)
	@Max(value = 4)
	private int priority;
	
	public TodoDto() {}
	
	public TodoDto(int id, String title, String description, LocalDate targetDate, int priority) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.targetDate = targetDate;
		this.priority = priority;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public LocalDate getTargetDate() {
		return targetDate;
	}
	public void setTargetDate(LocalDate targetDate) {
		this.targetDate = targetDate;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
}
