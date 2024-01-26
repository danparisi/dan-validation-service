package com.danservice.validation.service;

import com.danservice.validation.api.v1.OrderValidationRequestDTO;
import com.danservice.validation.exception.OrderValidationException;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

@Service
public class OrderValidationService {

    @SneakyThrows
    public void validate(OrderValidationRequestDTO orderDTO) {
        /*  Dummy logic: adding some delay and returning invalid
            only if the word is contained in the instrument field */

        sleep(new Random().nextInt(0, 500));

        var instrument = orderDTO.getInstrument();
        if (instrument.contains("invalid")) {
            throw new OrderValidationException(List.of(
                    format("Error 1: Instrument [%s] is invalid", instrument),
                    "Error 2: " + randomAlphabetic(40)));
        }
    }
}
