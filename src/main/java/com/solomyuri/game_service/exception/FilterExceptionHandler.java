package com.solomyuri.game_service.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solomyuri.game_service.model.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Component
@RequiredArgsConstructor
public class FilterExceptionHandler extends OncePerRequestFilter {

    private final ObjectMapper mapper;
    private final GlobalExceptionHandler exceptionHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {
	try {
	    filterChain.doFilter(request, response);
	} catch (Exception e) {
	    ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleException(e);
	    writeResponseEntity(responseEntity, response);
	}
    }

    private void writeResponseEntity(ResponseEntity<ErrorResponse> responseEntity, HttpServletResponse response)
            throws IOException {
	PrintWriter out = response.getWriter();
	ErrorResponse error = responseEntity.getBody();
	response.setContentType(MediaType.APPLICATION_JSON_VALUE);
	response.setStatus(responseEntity.getStatusCode().value());
	out.print(mapper.writeValueAsString(error));
	out.flush();
    }
}
