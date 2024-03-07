package com.exercise.todotest.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.exercise.todotest.dto.TodoDto;
import com.exercise.todotest.enums.TodoField;
import com.exercise.todotest.service.TodoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;

@RestController
public class TodoController {
	
	@Autowired
	TodoService todoService;

	@GetMapping("/todo")
	public ResponseEntity<?> retrieveAllTodos(@RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "20") @Max(value = 20, message = "Size must be between 1 and 20") Integer limit,
			@RequestParam(defaultValue = "ID") TodoField sortfield,
			@RequestParam(defaultValue = "DESC") Sort.Direction sortorder) {
		
		Page<TodoDto> todoDtoPage = todoService.getAllTodos(page, limit, sortfield, sortorder);
		return ResponseEntity.ok().body(todoDtoPage);
	}

	@GetMapping("/todo/{id}")
	public ResponseEntity<?> retrieveTodoById(@PathVariable int id) throws NoResourceFoundException {
		TodoDto todoDto = todoService.getTodoDtoById(id);
		return ResponseEntity.ok(todoDto);
	}

	@PostMapping("/todo")
	public ResponseEntity<TodoDto> createTodo(@Valid @RequestBody TodoDto todo) {
		TodoDto todoDto = todoService.createTodo(todo);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(todoDto.getId())
				.toUri();

		return ResponseEntity.created(location).body(todoDto);
	}

	@DeleteMapping("/todo/{id}")
	public ResponseEntity<Object> deleteTodo(@PathVariable int id) throws NoResourceFoundException {
		todoService.removeTodoById(id);
		return ResponseEntity.accepted().build();
	
	}

	@PutMapping("/todo/{id}")
	public ResponseEntity<TodoDto> updateTodo(@PathVariable int id, @Valid @RequestBody TodoDto todo) throws NoResourceFoundException {
		TodoDto todoDto = todoService.fullUpdateTodo(id, todo);
		
		return ResponseEntity.accepted().body(todoDto);
	}

	@PatchMapping("/todo/{id}")
	public ResponseEntity<TodoDto> partialUpdateTodo(@PathVariable int id, @RequestBody Map<String, ?> todo) throws NoResourceFoundException {
		TodoDto todoDto = todoService.partialUpdateTodo(id, todo);
		return ResponseEntity.accepted().body(todoDto);

	}

}
