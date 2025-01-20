package com.nttbank.microservices.debitcardservice.exception;


import static com.nttbank.microservices.debitcardservice.util.Constants.ERROR_KEY;
import static com.nttbank.microservices.debitcardservice.util.Constants.MESSAGE_KEY;
import static com.nttbank.microservices.debitcardservice.util.Constants.PATH_KEY;
import static com.nttbank.microservices.debitcardservice.util.Constants.STATUS_KEY;

import feign.FeignException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * A custom exception handler for handling various types of errors in a reactive Spring WebFlux
 * application. It extends the {@link AbstractErrorWebExceptionHandler} to provide custom error
 * handling logic for validation errors, illegal arguments, and other exceptions, formatting the
 * errors in a consistent response format.
 */
@Component
@Order(-1)
public class WebExceptionHandler extends AbstractErrorWebExceptionHandler {

  /**
   * Creates a new instance of the {@link WebExceptionHandler} class with the specified error
   * attributes, resources, application context, and server codec configurer.
   *
   * @param errorAttributes    the error attributes to use
   * @param resources          the resources to use
   * @param applicationContext the application context to use
   * @param configure          the server codec configurer to use
   */
  public WebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources,
      ApplicationContext applicationContext, ServerCodecConfigurer configure) {
    super(errorAttributes, resources, applicationContext);
    this.setMessageWriters(configure.getWriters());
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(),
        this::renderErrorResponse);
  }

  private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
    Throwable error = getError(request);

    if (error instanceof WebExchangeBindException bindException) {
      return handleValidationErrors(bindException);
    }

    if (error instanceof IllegalArgumentException || error instanceof IllegalStateException) {
      Map<String, Object> errorDetails = new HashMap<>();
      errorDetails.put(ERROR_KEY, "Invalid request");
      errorDetails.put(MESSAGE_KEY, error.getMessage());
      errorDetails.put(PATH_KEY, request.path());

      return ServerResponse.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
          .bodyValue(errorDetails);
    }

    if (error instanceof ResponseStatusException responseStatusException) {
      Map<String, Object> errorDetails = new HashMap<>();
      errorDetails.put(ERROR_KEY, "Invalid request");
      errorDetails.put(MESSAGE_KEY, responseStatusException.getReason());
      errorDetails.put(PATH_KEY, request.path());
      return ServerResponse.status(responseStatusException.getStatusCode())
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(errorDetails);
    }

    if (error instanceof FeignException.NotFound) {
      Map<String, Object> errorAttributes = new HashMap<>();
      errorAttributes.put(STATUS_KEY, HttpStatus.NOT_FOUND.value());
      errorAttributes.put(ERROR_KEY, "Resource Not Found");
      errorAttributes.put(MESSAGE_KEY, "Customer not found.");
      return ServerResponse.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
          .bodyValue(errorAttributes);
    }

    Map<String, Object> errorAttributes = getErrorAttributes(request,
        ErrorAttributeOptions.defaults());
    int statusCode = (int) errorAttributes.getOrDefault(STATUS_KEY, 500);

    return ServerResponse.status(statusCode).contentType(MediaType.APPLICATION_JSON)
        .bodyValue(errorAttributes);

  }

  private Mono<ServerResponse> handleValidationErrors(WebExchangeBindException bindException) {
    List<String> errors = bindException.getFieldErrors().stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage).toList();

    Map<String, Object> response = new HashMap<>();
    response.put(STATUS_KEY, 400);
    response.put(MESSAGE_KEY, "Invalid input data");
    response.put(ERROR_KEY, errors);

    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON).bodyValue(response);
  }

}
