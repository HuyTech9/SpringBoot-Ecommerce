package com.brian.ecommerce.project.exceptions;


import com.brian.ecommerce.project.payload.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class MyGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArguementNotValidException(MethodArgumentNotValidException e){
        Map<String, String> response = new HashMap<>();
        // Get the list of all validation errors
        List<ObjectError> errorList = e.getBindingResult().getAllErrors();

        // Loop through each error
        for (ObjectError error : errorList) {
            // Convert ObjectError to FieldError (since we are dealing with field validation)
            FieldError fieldError = (FieldError) error;
            // Get the field name where the error occurred
            String fieldName = fieldError.getField();
            // Get the error message for that field
            String errorMessage = fieldError.getDefaultMessage();
            // Add the field and its error message to the map
            response.put(fieldName, errorMessage);
        }
        return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> myResourceNotFoundException(ResourceNotFoundException e){
        String message = e.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> myAPIException(APIException e){
        String message = e.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

}
