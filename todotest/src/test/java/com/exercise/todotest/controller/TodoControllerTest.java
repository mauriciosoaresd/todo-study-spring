package com.exercise.todotest.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.exercise.todotest.dto.TodoDto;
import com.exercise.todotest.entity.Todo;
import com.exercise.todotest.enums.TodoField;
import com.exercise.todotest.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ValidationException;

@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private TodoService todoService;

	@Test
	void getAllTodos_Success() throws Exception {
		@SuppressWarnings("serial")
		List<TodoDto> todosList = new ArrayList<>() {{
				add(new TodoDto());
		}};

		Page<TodoDto> pagedTodos = new PageImpl<>(todosList);

		when(todoService.getAllTodos(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(TodoField.class), Mockito.any(Sort.Direction.class))).thenReturn(pagedTodos);
		mockMvc.perform(MockMvcRequestBuilders.get("/todo")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", is(0)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", is(1)));
	}

	@Test
	void getAllTodos_InvalidSortingDirectionValue_Failure() throws Exception {
		TodoDto todo1 = new TodoDto(3, null, null, null, 0);
		TodoDto todo2 = new TodoDto(1, null, null, null, 0);

		@SuppressWarnings("serial")
		List<TodoDto> todosList = new ArrayList<>() {{
				add(todo1);
				add(todo2);
			}};

		Page<TodoDto> pagedTodos = new PageImpl<>(todosList);

		when(todoService.getAllTodos(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(TodoField.class), Mockito.any(Sort.Direction.class))).thenReturn(pagedTodos);
		mockMvc.perform(MockMvcRequestBuilders.get("/todo?sortorder={sortorder}", "desc"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.message", is("Invalid Argument Type")));
	}

	@Test
	void getAllTodos_InvalidLimit_Failure() throws Exception {

		Page<TodoDto> pagedTodos = new PageImpl<>(new ArrayList<>());

		when(todoService.getAllTodos(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(TodoField.class), Mockito.any(Sort.Direction.class))).thenReturn(pagedTodos);
		mockMvc.perform(MockMvcRequestBuilders.get("/todo?limit={limit}", 21))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.message", is("Invalid Parameter Input")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.details", is("Limit must be between 1 and 20")));
	}
	
	@Test
	void getTodoById_ValidId_Success() throws Exception {
		TodoDto todoDto = new TodoDto();
		when(todoService.getTodoDtoById(Mockito.anyInt())).thenReturn(todoDto);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/todo/{id}", 0))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.id", is(0)));
	}
	
	@Test
	void getTodoById_IdNotFound_Failure() throws Exception {
		when(todoService.getTodoDtoById(Mockito.anyInt())).thenThrow(new NoResourceFoundException(null, "Todo with id 'x'"));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/todo/{id}", 0))
		.andExpect(MockMvcResultMatchers.status().isNotFound())
		.andExpect(MockMvcResultMatchers.jsonPath("$.message", is("Resource not found")));
	}

	@Test
	void createTodo_NoContent_Failure() throws Exception {
		TodoDto todoDto = new TodoDto();
		when(todoService.createTodo(Mockito.any())).thenReturn(todoDto);
		mockMvc.perform(MockMvcRequestBuilders.post("/todo").contentType(MediaType.APPLICATION_JSON).content(""))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.message", is("Invalid body request")));
	}

	@Test
	void createTodo_MissingAttributes_Failure() throws Exception {
		String stubObject = "{}";
		when(todoService.createTodo(Mockito.any())).thenReturn(new TodoDto());

		mockMvc.perform(
				MockMvcRequestBuilders.post("/todo").contentType(MediaType.APPLICATION_JSON).content(stubObject))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.message", is("Invalid field inputs")));
	}

	@Test
	void createTodo_Success() throws Exception {
		TodoDto todoDto = new TodoDto(0, "Title test", "Description test", LocalDate.now().plusDays(1), 1);
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();

		String todoObj = mapper.writeValueAsString(todoDto);
		when(todoService.createTodo(Mockito.any())).thenReturn(todoDto);

		mockMvc.perform(MockMvcRequestBuilders.post("/todo").contentType(MediaType.APPLICATION_JSON).content(todoObj))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.redirectedUrl("http://localhost/todo/0"));
	}

	@Test
	void deleteTodo_InvalidIdAttribute_Failure() throws Exception {
		when(todoService.existsTodo(Mockito.anyInt())).thenReturn(false);

		mockMvc.perform(MockMvcRequestBuilders.delete("/todo/{id}", "l"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.message", is("Invalid Argument Type")));
	}

	@Test
	void deleteTodo_InexistentId_Failure() throws Exception {
		doThrow(new NoResourceFoundException(null, "Todo with id 'x'")).when(todoService).removeTodoById(Mockito.anyInt());
		
		mockMvc.perform(MockMvcRequestBuilders.delete("/todo/{id}", 1))
				.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$.message", is("Resource not found")));
	}

	@Test
	void deleteTodo_ValidAttritubet_Success() throws Exception {
		when(todoService.existsTodo(Mockito.anyInt())).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.delete("/todo/{id}", 3))
				.andExpect(MockMvcResultMatchers.status().isAccepted());
	}

	@Test
	void updateTodo_InexistentId_Failure() throws NoResourceFoundException, Exception {
		Todo todo = new Todo(0, "title test", "description test", LocalDate.now().plusDays(1), 1);
		when(todoService.fullUpdateTodo(Mockito.anyInt(), Mockito.any()))
				.thenThrow(new NoResourceFoundException(null, "Todo with id 'x'"));

		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		String todoObj = mapper.writeValueAsString(todo);

		mockMvc.perform(
				MockMvcRequestBuilders.put("/todo/{id}", 0).contentType(MediaType.APPLICATION_JSON).content(todoObj))
				.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$.message", is("Resource not found")));
	}

	@Test
	void updateTodo_MissingField_Failure() throws Exception {
		Map<String, String> updateFields = new HashMap<>();
		updateFields.put("title", "Title to update");

		when(todoService.existsTodo(Mockito.anyInt())).thenReturn(true);
		when(todoService.fullUpdateTodo(Mockito.anyInt(), Mockito.any())).thenReturn(new TodoDto());

		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		String todoObj = mapper.writeValueAsString(updateFields);

		mockMvc.perform(
				MockMvcRequestBuilders.put("/todo/{id}", 0).contentType(MediaType.APPLICATION_JSON).content(todoObj))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("$.message", is("Invalid field inputs")));
	}

	@Test
	void updateTodo_ValidFields_Success() throws Exception {
		TodoDto updatedTodo = new TodoDto();
		updatedTodo.setTitle("update title");
		updatedTodo.setDescription("update description");
		updatedTodo.setTargetDate(LocalDate.now().plusDays(1));
		updatedTodo.setPriority(4);

		when(todoService.existsTodo(Mockito.anyInt())).thenReturn(true);
		when(todoService.fullUpdateTodo(Mockito.anyInt(), Mockito.any())).thenReturn(updatedTodo);

		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		String todoObj = mapper.writeValueAsString(updatedTodo);

		mockMvc.perform(
				MockMvcRequestBuilders.put("/todo/{id}", 0).contentType(MediaType.APPLICATION_JSON).content(todoObj))
				.andExpect(MockMvcResultMatchers.status().isAccepted())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", is(0)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.title", is("update title")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description", is("update description")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.targetDate", is(updatedTodo.getTargetDate().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.priority", is(4)));
	}

	@Test
	void partialUpdateTodo_InvalidField_Failure() throws Exception {
		@SuppressWarnings("serial")
		Map<String, ?> updateFields = new HashMap<>() {{
				put("invalid_field", "test");
			}};

		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		String updateObj = mapper.writeValueAsString(updateFields);

		when(todoService.existsTodo(Mockito.anyInt())).thenReturn(true);
		when(todoService.partialUpdateTodo(Mockito.anyInt(), Mockito.anyMap()))
				.thenThrow(new IllegalArgumentException("Unexpected value: " + "invalid_field"));

		mockMvc.perform(MockMvcRequestBuilders.patch("/todo/{id}", 0).contentType(MediaType.APPLICATION_JSON)
				.content(updateObj))
		.andExpect(MockMvcResultMatchers.status().is5xxServerError());
	}
	
	@Test
	void partialUpdateTodo_InvalidTargetDateValue_Failure() throws Exception {
		@SuppressWarnings("serial")
		Map<String, ?> updateFields = new HashMap<>() {{
				put("targetDate", "202a1-08-05");
			}};

		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		String updateObj = mapper.writeValueAsString(updateFields);

		when(todoService.existsTodo(Mockito.anyInt())).thenReturn(true);
		when(todoService.partialUpdateTodo(Mockito.anyInt(), Mockito.anyMap()))
				.thenThrow(new ValidationException("Text '202a1-08-05' could not be parsed at index 0"));

		mockMvc.perform(MockMvcRequestBuilders.patch("/todo/{id}", 0).contentType(MediaType.APPLICATION_JSON)
				.content(updateObj))
		.andExpect(MockMvcResultMatchers.status().is5xxServerError());
	}

	@Test
	void partialUpdateTodo_IdNotFound_Failure() throws Exception {
		@SuppressWarnings("serial")
		Map<String, ?> updateFields = new HashMap<>() {{
				put("title", "new title");
				put("targetDate", LocalDate.now().plusDays(1).toString());
			}};

		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		String updateObj = mapper.writeValueAsString(updateFields);

		when(todoService.partialUpdateTodo(Mockito.anyInt(), Mockito.anyMap()))
				.thenThrow(new NoResourceFoundException(null, "Todo with id 'x'"));

		mockMvc.perform(MockMvcRequestBuilders.patch("/todo/{id}", 10).contentType(MediaType.APPLICATION_JSON)
				.content(updateObj)).andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$.message", is("Resource not found")));
	}

	@Test
	void partialUpdateTodo_ValidInputs_Success() throws Exception {
		@SuppressWarnings("serial")
		Map<String, ?> updateFields = new HashMap<>() {{
				put("title", "new title");
				put("targetDate", LocalDate.now().plusDays(1).toString());
			}};

		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		String updateObj = mapper.writeValueAsString(updateFields);

		TodoDto todo = new TodoDto();
		todo.setTitle(updateFields.get("title").toString());
		todo.setTargetDate(LocalDate.parse(updateFields.get("targetDate").toString()));

		when(todoService.existsTodo(Mockito.anyInt())).thenReturn(true);
		when(todoService.partialUpdateTodo(Mockito.anyInt(), Mockito.anyMap())).thenReturn(todo);

		mockMvc.perform(MockMvcRequestBuilders.patch("/todo/{id}", 0).contentType(MediaType.APPLICATION_JSON)
				.content(updateObj)).andExpect(MockMvcResultMatchers.status().isAccepted())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", is(0)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.title", is("new title")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description").doesNotExist())
				.andExpect(MockMvcResultMatchers.jsonPath("$.targetDate", is(updateFields.get("targetDate").toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.priority", is(0)));
	}

}