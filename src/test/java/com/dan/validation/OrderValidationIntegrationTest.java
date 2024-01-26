package com.dan.validation;

import com.danservice.validation.Application;
import com.danservice.validation.api.v1.OrderValidationRequestDTO;
import com.danservice.validation.api.v1.OrderValidationRequestDTO.OrderValidationRequestDTOBuilder;
import com.danservice.validation.api.v1.OrderValidationResponseDTO;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.danservice.validation.api.v1.OrdersController.BASE_ENDPOINT_ORDERS;
import static com.danservice.validation.domain.OrderType.LIMIT;
import static java.math.BigDecimal.ZERO;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class OrderValidationIntegrationTest {
    private static final EasyRandom EASY_RANDOM = new EasyRandom();
    private static final String ENDPOINT_ORDERS_VALIDATE = BASE_ENDPOINT_ORDERS + "/validate";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldValidateOrder() {
        var orderValidationRequestDTO = newValidRequest();

        ResponseEntity<OrderValidationResponseDTO> result = testRestTemplate
                .postForEntity(ENDPOINT_ORDERS_VALIDATE, orderValidationRequestDTO, OrderValidationResponseDTO.class);

        var body = result.getBody();
        assertNotNull(body);
        assertTrue(body.isValid());
        assertTrue(body.getErrors().isEmpty());
    }

    @Test
    void shouldNotValidateOrderIfLogicallyInvalid() {
        var orderValidationRequestDTO = newValidRequest(o -> o.instrument(randomAlphabetic(10) + "-invalid"));

        ResponseEntity<OrderValidationResponseDTO> result = testRestTemplate
                .postForEntity(ENDPOINT_ORDERS_VALIDATE, orderValidationRequestDTO, OrderValidationResponseDTO.class);

        var body = result.getBody();
        assertNotNull(body);
        assertFalse(body.isValid());
        assertFalse(body.getErrors().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidRequestGenerator")
    void shouldNotValidateOrderIfFormallyInvalid(OrderValidationRequestDTO orderValidationRequestDTO) {
        ResponseEntity<OrderValidationResponseDTO> result = testRestTemplate
                .postForEntity(ENDPOINT_ORDERS_VALIDATE, orderValidationRequestDTO, OrderValidationResponseDTO.class);

        var body = result.getBody();
        assertNotNull(body);
        assertFalse(body.isValid());
        assertFalse(body.getErrors().isEmpty());
    }

    private static Stream<Arguments> invalidRequestGenerator() {
        return Stream.of(
                Arguments.of(newValidRequest(order -> order.price(ZERO))),
                Arguments.of(newValidRequest(order -> order.quantity(-1))),
                Arguments.of(newValidRequest(order -> order.instrument(EMPTY))),
                Arguments.of(newValidRequest(order -> order.price(BigDecimal.valueOf(0.00001)))),
                Arguments.of(newValidRequest(order -> order.instrument(randomAlphabetic(21)))));
    }

    private static OrderValidationRequestDTO newValidRequest() {
        return newValidRequest(x -> {
        });
    }

    private static OrderValidationRequestDTO newValidRequest(Consumer<OrderValidationRequestDTOBuilder> modifier) {
        var builder = OrderValidationRequestDTO.builder()
                .type(LIMIT)
                .instrument(randomAlphabetic(15))
                .quantity(EASY_RANDOM.nextInt(1, 100))
                .price(BigDecimal.valueOf(EASY_RANDOM.nextDouble(1.0d, 100.0d)));

        modifier.accept(builder);

        return builder.build();
    }
}
