package com.danservice.validation.api.v1;

import com.danservice.validation.exception.OrderValidationException;
import com.danservice.validation.service.OrderValidationService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.danservice.validation.api.v1.OrdersController.BASE_ENDPOINT_ORDERS;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(BASE_ENDPOINT_ORDERS)
public class OrdersController {
    public static final String BASE_ENDPOINT_ORDERS = "/orders/v1";

    private final OrderValidationService orderValidationService;

    @ExceptionHandler(value = {MethodArgumentNotValidException.class, ValidationException.class})
    public ResponseEntity<OrderValidationResponseDTO> exceptionHandler(Exception exception) {
        return ResponseEntity
                .ok(getExceptionBody(List.of(exception.getMessage())));
    }

    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<OrderValidationResponseDTO> exceptionHandler(OrderValidationException exception) {
        return ResponseEntity
                .ok(getExceptionBody(exception.getErrors()));
    }

    @PostMapping("/validate")
    public ResponseEntity<OrderValidationResponseDTO> add(@RequestBody @Valid OrderValidationRequestDTO orderDTO) {
        log.info("Validating order [{}]", orderDTO);

        orderValidationService.validate(orderDTO);

        return ok(OrderValidationResponseDTO.builder()
                .valid(true).build());
    }

    private OrderValidationResponseDTO getExceptionBody(List<String> errors) {
        return OrderValidationResponseDTO.builder()
                .valid(false)
                .errors(errors).build();
    }

}
