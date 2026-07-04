package com.example.ticketsystem.controller;

import com.example.ticketsystem.models.ApiDtos.ErrorResponse;
import com.example.ticketsystem.models.ApiDtos.FieldErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ApiController.class)
public class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> validationError(MethodArgumentNotValidException exception, HttpServletRequest request) {
		List<FieldErrorResponse> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
				.toList();
		return ResponseEntity.badRequest().body(ErrorResponse.of(
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Invalid request",
				request.getRequestURI(),
				fieldErrors
		));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<ErrorResponse> notFoundOrInvalid(IllegalArgumentException exception, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
				HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(),
				exception.getMessage(),
				request.getRequestURI()
		));
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<ErrorResponse> accessDenied(AccessDeniedException exception, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.of(
				HttpStatus.FORBIDDEN.value(),
				HttpStatus.FORBIDDEN.getReasonPhrase(),
				"Access denied",
				request.getRequestURI()
		));
	}
}
