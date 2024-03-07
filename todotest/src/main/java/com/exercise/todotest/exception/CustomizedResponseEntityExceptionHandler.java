package com.exercise.todotest.exception;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.TypeMismatchException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.exercise.todotest.enums.TodoField;

@ControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) throws Exception {
		
		String message = ex.getCause() != null? ex.getCause().getLocalizedMessage() : ex.getMessage();
		
		ErrorDetails details = new ErrorDetails(LocalDateTime.now(), message , request.getDescription(false));
		return new ResponseEntity<ErrorDetails>(details, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, Object> errorsMap = new HashMap<>();
		
		for(ObjectError error: ex.getAllErrors()) {
			String fieldError = ((FieldError) error).getField();
			errorsMap.put(fieldError, error.getDefaultMessage());
		}
		
		ErrorDetails details = new ErrorDetails(LocalDateTime.now(), "Invalid field inputs", errorsMap );
		
		return new ResponseEntity<Object>(details, HttpStatus.BAD_REQUEST);
	}
	
	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Object[] args = {ex.getPropertyName(), ex.getValue()};
		String variableType = ex.getRequiredType().getSimpleName();
		String defaultDetail = "Failed to convert '" + args[0] + "' with value: '" + args[1] + "' -  Must be '" + variableType +"'";
		
		if(ex.getRequiredType().isEnum()) {
			defaultDetail += getEnumSupportedValues(ex.getRequiredType().getSimpleName());
		}
		
		ErrorDetails details = new ErrorDetails(LocalDateTime.now(), "Invalid Argument Type", defaultDetail);
		return new ResponseEntity<Object>(details, HttpStatus.BAD_REQUEST);
	}
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ErrorDetails details = new ErrorDetails(LocalDateTime.now(), "Invalid body request", ex.getMessage());
		return new ResponseEntity<Object>(details, HttpStatus.BAD_REQUEST);
	}
	
	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ErrorDetails details = new ErrorDetails(LocalDateTime.now(), "Invalid Parameter Input", "Limit must be between 1 and 20");
		return new ResponseEntity<Object>(details, HttpStatus.BAD_REQUEST);
	}	
	
	@Override
	protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ErrorDetails details = new ErrorDetails(LocalDateTime.now(), "Resource not found", ex.getMessage());
		return new ResponseEntity<Object>(details, HttpStatus.NOT_FOUND);
	}
	
	private List<?> getEnumSupportedValues(String enumName) {
		List<?> values = switch (enumName) {
		case "Direction": {
			yield Arrays.asList(Sort.Direction.values());
		}
		case "TodoField": {
			yield Arrays.asList(TodoField.values());
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + enumName);
		};
		return values;
	}

	

}
