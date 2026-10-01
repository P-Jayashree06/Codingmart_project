package com.bloodbank.exception; import lombok.Getter; import org.springframework.http.HttpStatus;
@Getter public class BusinessException extends RuntimeException { private final String errorCode; private final HttpStatus httpStatus; public BusinessException(String errorCode,HttpStatus httpStatus,String message){super(message);this.errorCode=errorCode;this.httpStatus=httpStatus;} }
