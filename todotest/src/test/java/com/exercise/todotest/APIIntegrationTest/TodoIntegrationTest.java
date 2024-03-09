package com.exercise.todotest.APIIntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import com.exercise.todotest.entity.Todo;
import com.exercise.todotest.repository.TodoRepository;

@SuppressWarnings("serial")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TodoIntegrationTest {
	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private TodoRepository todoRepository;

	private static HttpHeaders headers;


	@BeforeAll
	public static void init() {
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
	}
	

	@Test
	void testRetrieveAllTodosDefaultOrder() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo"), HttpMethod.GET, entity,
				String.class);

		
		String expected = """
				[{"id":3,"title":"Learn RabbitMQ","description":"Learn messaging and streaming broker RabbitMQ","targetDate":"2025-01-01","priority":2},{"id":2,"title":"Learn Spring","description":"Learn Spring Framework and some starters.","targetDate":"2024-06-01","priority":4},{"id":1,"title":"Learn AWS","description":"Learn AWS Cloud, get certifieds.","targetDate":"2025-01-01","priority":3}]""";
		System.out.println(response);
		assertTrue(response.getBody().contains(expected));
	}

	@Test
	void testRetrieveAllTodosOrderByIdAscending() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo?sortfield=ID&sortorder=ASC"),
				HttpMethod.GET, entity, String.class);

		String expected = """
				[{"id":1,"title":"Learn AWS","description":"Learn AWS Cloud, get certifieds.","targetDate":"2025-01-01","priority":3},{"id":2,"title":"Learn Spring","description":"Learn Spring Framework and some starters.","targetDate":"2024-06-01","priority":4},{"id":3,"title":"Learn RabbitMQ","description":"Learn messaging and streaming broker RabbitMQ","targetDate":"2025-01-01","priority":2}]""";
		assertTrue(response.getBody().contains(expected));
	}

	@Test
	void testRetrieveAllTodosInvalidSort() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo?sortfield=ANY&sortorder=ASC"),
				HttpMethod.GET, entity, String.class);

		String expected = """
				"message":"Invalid Argument Type","details":"Failed to convert 'sortfield' with value: 'ANY' -  Must be 'TodoField'[ID, TITLE, DESCRIPTION, TARGETDATE, PRIORITY]""";
		assertTrue(response.getBody().contains(expected));
	}

	@Test
	void testRetrieveATodo() throws JSONException {

		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo/1"), HttpMethod.GET, entity,
				String.class);

		
		String expected = """
				{"id":1,"title":"Learn AWS","description":"Learn AWS Cloud, get certifieds.","targetDate":"2025-01-01","priority":3}""";
		
		assertTrue(response.getBody().contains(expected));
	}

	@Test
	void testRetrieveAnInexistentTodo() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo/55"), HttpMethod.GET, entity,
				String.class);

		String expected = """
				"message":"Resource not found","details":"No static resource Todo with id '55'."}""";
		assertTrue(response.getBody().contains(expected));
	}

	@Test
	void testRetrieveAnInvalidTodo() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo/l"), HttpMethod.GET, entity,
				String.class);

		System.out.println(response.getBody());

		String expected = """
				"message":"Invalid Argument Type","details":"Failed to convert 'id' with value: 'l' -  Must be 'int'"}""";
		assertTrue(response.getBody().contains(expected));
	}

	@Test
	void testCreateATodo() {
		Todo createTodo = new Todo(0, "integration test", "integration test", LocalDate.now().plusDays(1), 1);

		HttpEntity<Todo> entity = new HttpEntity<Todo>(createTodo, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo"), HttpMethod.POST, entity,
				String.class);

		String expected = String.format(
				"\"title\":\"integration test\",\"description\":\"integration test\",\"targetDate\":\"%s\",\"priority\":1}",
				createTodo.getTargetDate());
		
		String id = response.getBody().substring(6, response.getBody().indexOf(",\"title\""));
	
		assertTrue(response.getBody().contains(expected));
		todoRepository.deleteById(Integer.parseInt(id));
	}

	@Test
	void testCreateATodoWithMissingProperties() {
		Map<String, ?> stubInvalidTodo = new HashMap<>() {
			{
				put("id", 0);
			}
		};

		HttpEntity<?> entity = new HttpEntity<>(stubInvalidTodo, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo"), HttpMethod.POST, entity,
				String.class);

		String expected = """
				"message":"Invalid field inputs""";

		assertTrue(response.getBody().contains(expected));

	}

	@Test
	void testRemoveTodoById() {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		Todo todoToRemove = todoRepository
				.save(new Todo(0, "integration test", "integration test", LocalDate.now().plusDays(1), 1));

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo/" + todoToRemove.getId()),
				HttpMethod.DELETE, entity, String.class);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

	}

	@Test
	void testRemoveTodoWithInvalidId() {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo/" + "-64545"),
				HttpMethod.DELETE, entity, String.class);

		String expected = """
				"message":"Resource not found","details":"No static resource Todo with id '-64545'.""";

		assertTrue(response.getBody().contains(expected));
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

	}

	@Test
	void testFullTodoUpdate() {
		Map<String, ?> fieldsToUpdate = new HashMap<>() {
			{
				put("title", "title update");
				put("description", "description update");
				put("targetDate", LocalDate.now().plusDays(2));
				put("priority", 3);
			}
		};

		Todo todoToUpdate = todoRepository
				.save(new Todo(0, "integration test", "integration test", LocalDate.now().plusDays(1), 1));

		HttpEntity<?> entity = new HttpEntity<>(fieldsToUpdate, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo/" + todoToUpdate.getId()),
				HttpMethod.PUT, entity, String.class);

		String expected = String.format(
				"\"title\":\"title update\",\"description\":\"description update\",\"targetDate\":\"%s\",\"priority\":3}",
				fieldsToUpdate.get("targetDate"));

		assertTrue(response.getBody().contains(expected));
		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

		todoRepository.deleteById(todoToUpdate.getId());
	}

	@Test
	void testFullTodoUpdateWithInvalidProp() {
		Map<String, ?> fieldsToUpdate = new HashMap<>() {
			{
				put("invalidProp", "update");
			}
		};

		Todo todoToUpdate = todoRepository
				.save(new Todo(0, "integration test", "integration test", LocalDate.now().plusDays(1), 1));

		HttpEntity<?> entity = new HttpEntity<>(fieldsToUpdate, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo/" + todoToUpdate.getId()),
				HttpMethod.PUT, entity, String.class);

		String expected = """
				"message":"Invalid field inputs\"""";

		assertTrue(response.getBody().contains(expected));
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

		todoRepository.deleteById(todoToUpdate.getId());
	}

	@Test
	void testPartialTodoUpdate() {
		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

		Map<String, ?> fieldsToUpdate = new HashMap<>() {
			{
				put("title", "title update");
				put("description", "description update");
			}
		};

		Todo todoToUpdate = todoRepository
				.save(new Todo(0, "integration test", "integration test", LocalDate.now().plusDays(1), 1));

		HttpEntity<?> entity = new HttpEntity<>(fieldsToUpdate, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo/" + todoToUpdate.getId()),
				HttpMethod.PATCH, entity, String.class);

		String expected = String.format(
				"\"title\":\"title update\",\"description\":\"description update\",\"targetDate\":\"%s\",\"priority\":1}",
				todoToUpdate.getTargetDate());

		assertTrue(response.getBody().contains(expected));
		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

		todoRepository.deleteById(todoToUpdate.getId());
	}
	
	@Test
	void testPartialTodoUpdateWithInvalidProps() {
		restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

		Map<String, ?> fieldsToUpdate = new HashMap<>() {
			{
				put("prop", "invalid");
			}
		};

		Todo todoToUpdate = todoRepository
				.save(new Todo(0, "integration test", "integration test", LocalDate.now().plusDays(1), 1));

		HttpEntity<?> entity = new HttpEntity<>(fieldsToUpdate, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/todo/" + todoToUpdate.getId()),
				HttpMethod.PATCH, entity, String.class);

		String expected = String.format(
				"\"message\":\"Unexpected value: prop\"");
		
		assertTrue(response.getBody().contains(expected));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		
		todoRepository.deleteById(todoToUpdate.getId());
	}

	private String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}

}
