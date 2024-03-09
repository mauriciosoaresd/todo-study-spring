package com.exercise.todotest.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.exercise.todotest.entity.Todo;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class TodoRepositoryTest {
	@Autowired
	TodoRepository todoRepository;
	
	@BeforeEach
	void setup() {
		LocalDate date = LocalDate.now();
		todoRepository.save(new Todo(0, "Title Test 0", "Description Test 0 ", date.plusDays(1), 1));
		todoRepository.save(new Todo(0, "Title Test 1", "Description Test 1 ", date.plusDays(2), 2));
	}
	
	@AfterEach
	void destroy() {
		todoRepository.deleteAll();
	}

	@Test
	void testGetAllTodos() {
		List<Todo> todosList = todoRepository.findAll();
		System.out.println(todosList);
		assertThat(todosList.size()).isEqualTo(2);
		assertThat(todosList.get(0).getId()).isNotNegative();
		assertThat(todosList.get(0).getId()).isEqualTo(1);
		assertThat(todosList.get(0).getTitle()).isEqualTo("Title Test 0");
	}
	
	@Test
	void testGetInvalidTodo() {
		Exception exception = assertThrows(NoSuchElementException.class, () -> {
			todoRepository.findById(99).get();
		});
		
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("No value present");
	}
	
	@Test
	void testCreateTodo() {
		Todo saved = new Todo(0, "Title Test 0", "Description Test 0 ", LocalDate.now().plusDays(1), 1);
		Todo returned = todoRepository.save(saved);
		
		assertThat(returned).isNotNull();
		assertThat(returned.getDescription()).isEqualTo(saved.getDescription());
	}
	

}
