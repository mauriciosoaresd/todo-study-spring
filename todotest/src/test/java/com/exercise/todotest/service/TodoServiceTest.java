package com.exercise.todotest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.exercise.todotest.dto.TodoDto;
import com.exercise.todotest.entity.Todo;
import com.exercise.todotest.enums.TodoField;
import com.exercise.todotest.repository.TodoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class TodoServiceTest {
	@Mock
	TodoRepository todoRepository;	
	@Spy
	ModelMapper modelMapper;
	@InjectMocks
	TodoService todoService;

	@Test
	void existsTodo_ValidId_Success() {
		when(todoRepository.existsById(Mockito.anyInt())).thenReturn(true);
		boolean todoExists = todoService.existsTodo(0);
		assertTrue(todoExists);
	}

	@Test
	void existsTodo_IdNotFound_Failure() {
		when(todoRepository.existsById(Mockito.anyInt())).thenReturn(false);
		boolean todoExists = todoService.existsTodo(1);
		assertFalse(todoExists);
	}

	@Test
	void createTodo_ValidTodo_Success() {
		Todo todo = new Todo();
		todo.setTitle("TITLE TEST");

		when(todoRepository.save(Mockito.any(Todo.class))).thenReturn(todo);
		TodoDto repositoryResponse = todoService.createTodo(new TodoDto());
		assertThat(repositoryResponse.getTitle()).isSameAs(todo.getTitle());
	}

	@Test
	void getAllTodos_Success() {
		@SuppressWarnings("serial")
		List<Todo> todoList = new ArrayList<>() {{
			add(new Todo(0, null, null, null, 1));
			add(new Todo(1, null, null, null, 1));
		}};
		Page<Todo> pagedRepositoryResponse = new PageImpl<>(todoList);
		
		Pageable pageableReq = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id"));
		when(todoRepository.findAll(pageableReq)).thenReturn(pagedRepositoryResponse);

		Page<TodoDto> repositoryResponse = todoService.getAllTodos(0, 1, TodoField.ID, Sort.Direction.ASC);

		assertAll(() -> assertTrue(repositoryResponse.getContent().get(0).getId() == 0),
				() -> assertTrue(repositoryResponse.getContent().get(1).getId() == 1)
				);
	}

	@Test
	void getTodoById_ValidId_Success() {
		Todo todo = new Todo();

		when(todoRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(todo));

		Optional<Todo> repositoryResponse = todoRepository.findById(0);
		assertThat(repositoryResponse.get().getId()).isSameAs(0);
	}

	@Test
	void getTodoById_IdNotFound_Failure() {
		when(todoRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());

		Exception exception = assertThrows(NoResourceFoundException.class, () -> {
			todoService.getTodoById(0);
		});

		assertTrue(exception.getMessage().contains("No static resource Todo with id '0'"));
	}

	@Test
	void removeTodoById_IdNotFound_Failure() {
		doNothing().when(todoRepository).deleteById(Mockito.anyInt());

		Exception exception = assertThrows(NoResourceFoundException.class, () -> {
			todoService.removeTodoById(0);
		});

		assertTrue(exception.getMessage().contains("No static resource Todo with id '0'"));
	}

	@Test
	void fullUpdateTodo_ValidIdAndTodo_Success() throws NoResourceFoundException {
		TodoDto updatedTodoDto = new TodoDto(0, "new title", "new description", LocalDate.now().plusDays(1), 1);
		Todo updatedTodo = new Todo(0, "new title", "new description", LocalDate.now().plusDays(1), 1);

		when(todoRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(new Todo()));
		when(todoRepository.save(Mockito.any(Todo.class))).thenReturn(updatedTodo);

		TodoDto repositoryResponse = todoService.fullUpdateTodo(0, updatedTodoDto);

		assertAll(() -> assertTrue(repositoryResponse.getTitle() == "new title"),
				() -> assertTrue(repositoryResponse.getDescription() == "new description"),
				() -> assertTrue(repositoryResponse.getTargetDate() == updatedTodo.getTargetDate()),
				() -> assertTrue(repositoryResponse.getPriority() == 1));
	}

	@Test
	void fullUpdateTodo_IdNotFound_Failure() throws NoResourceFoundException {
		TodoDto updatedTodo = new TodoDto(0, "new title", "new description", LocalDate.now().plusDays(1), 1);
		when(todoRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());

		Exception exception = assertThrows(NoResourceFoundException.class, () -> {
			todoService.fullUpdateTodo(0, updatedTodo);
		});

		assertTrue(exception.getMessage().contains("No static resource Todo with id '0'"));
	}

	// PARTIAL UPDATE TODO
	@Test
	void partialUpdateTodo_ValidIdAndFields_Success() throws NoResourceFoundException {
		@SuppressWarnings("serial")
		Map<String, ?> fieldsToUpdate = new HashMap<>() {
			{
				put("title", "updated title");
				put("priority", 3);
			}
		};

		when(todoRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(new Todo()));
		when(todoRepository.save(Mockito.any(Todo.class))).thenReturn(new Todo(0, "updated title", null, null, 3));

		TodoDto repositoryResponse = todoService.partialUpdateTodo(0, fieldsToUpdate);

		assertAll(() -> assertTrue(repositoryResponse.getTitle() == "updated title"),
				() -> assertTrue(repositoryResponse.getPriority() == 3));

	}

	@Test
	void partialUpdateTodo_ValidIdAndInvalidFields_Success() throws NoResourceFoundException {
		@SuppressWarnings("serial")
		Map<String, ?> fieldsToUpdate = new HashMap<>() {
			{
				put("wrongField", "field updated");
			}
		};

		Todo todo = new Todo();

		when(todoRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(todo));
		when(todoRepository.save(Mockito.any(Todo.class))).thenReturn(todo);

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			todoService.partialUpdateTodo(0, fieldsToUpdate);
		});

		assertTrue(exception.getMessage().contains("Unexpected value"));

	}

}
