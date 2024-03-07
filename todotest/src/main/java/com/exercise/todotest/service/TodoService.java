package com.exercise.todotest.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.exercise.todotest.dto.TodoDto;
import com.exercise.todotest.entity.Todo;
import com.exercise.todotest.enums.TodoField;
import com.exercise.todotest.repository.TodoRepository;

import jakarta.validation.ValidationException;

@Service
public class TodoService {
	@Autowired
	TodoRepository todoRepository;

	@Autowired
	ModelMapper modelMapper;

	public boolean existsTodo(int id) {
		return todoRepository.existsById(id);
	}

	public TodoDto createTodo(TodoDto todoDto) {
		Todo convertedTodo = modelMapper.map(todoDto, Todo.class);
		Todo todo = todoRepository.save(convertedTodo);
		TodoDto responseTodoDto = modelMapper.map(todo, TodoDto.class);
		return responseTodoDto;
	}

	public Page<TodoDto> getAllTodos(int page, int size, TodoField sortfield, Sort.Direction sortorder) {
		Pageable pageableReq = PageRequest.of(page, size, Sort.by(sortorder, sortfield.getTodoField()));
		Page<Todo> todosPagination = todoRepository.findAll(pageableReq);

		Page<TodoDto> todoDtoPagination = todosPagination.map(todo -> modelMapper.map(todo, TodoDto.class));
		return todoDtoPagination;
	}

	public Todo getTodoById(int id) throws NoResourceFoundException {
		Todo todo = todoRepository.findById(id)
				.orElseThrow(() -> new NoResourceFoundException(null, "Todo with id '" + id + "'"));
		return todo;
	}

	public TodoDto getTodoDtoById(int id) throws NoResourceFoundException {
		Todo todo = getTodoById(id);
		TodoDto todoDto = modelMapper.map(todo, TodoDto.class);

		return todoDto;
	}

	public void removeTodoById(int id) throws NoResourceFoundException {
		getTodoById(id);
		todoRepository.deleteById(id);
		return;
	}

	public TodoDto fullUpdateTodo(int id, TodoDto todoDto) throws NoResourceFoundException {
		Todo todoToUpdate = getTodoById(id);

		todoToUpdate.setTitle(todoDto.getTitle());
		todoToUpdate.setDescription(todoDto.getDescription());
		todoToUpdate.setTargetDate(todoDto.getTargetDate());
		todoToUpdate.setPriority(todoDto.getPriority());

		Todo savedTodo = todoRepository.save(todoToUpdate);
		TodoDto convertedTodo = modelMapper.map(savedTodo, TodoDto.class);

		return convertedTodo;
	}

	public TodoDto partialUpdateTodo(int id, Map<String, ?> todo) throws NoResourceFoundException {
		Todo todoToUpdate = getTodoById(id);

		for (Entry<String, ?> entry : todo.entrySet()) {
			todoToUpdate = partialUpdateSwitchCase(todoToUpdate, entry.getKey(), entry.getValue());
		}
		
		Todo savedTodo = todoRepository.save(todoToUpdate);
		TodoDto convertedTodo = modelMapper.map(savedTodo, TodoDto.class);

		return convertedTodo;
	}

	private Todo partialUpdateSwitchCase(Todo todo, String key, Object value) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		switch (key) {
		case "title" -> {
			if (value.toString().length() < 2 || value.toString().length() > 20) {
				throw new ValidationException("'Title' must be between 2 and 20");
			} else {
				todo.setTitle(value.toString());
			}
			break;
		}
		case "description" -> {
			if (value.toString().length() < 10 || value.toString().length() > 200) {
				throw new ValidationException("'Description' must be between 10 and 200");
			} else {
				todo.setDescription(value.toString());
			}
			break;
		}
		case "targetDate" -> {
			try {
				LocalDate date = LocalDate.parse(value.toString(), formatter);

				if (date.isAfter(LocalDate.now())) {
					todo.setTargetDate(date);
				} else {
					throw new ValidationException("'Target Date' must be in future");
				}
			} catch (Exception e) {
				throw new ValidationException(e);
			}
			break;
		}
		case "priority" -> {
			int priority = Integer.parseInt(value.toString());

			if (priority < 1 || priority > 4) {
				throw new ValidationException("'Priority' must be between 1 and 4");
			} else {
				todo.setPriority(priority);
			}
			break;
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + key);
		}

		return todo;
	}
}
