package com.solomyuri.game_service.util;

import com.solomyuri.game_service.exception.ApplicationException;
import com.solomyuri.game_service.model.entity.Cell;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class AppUtil {

    public boolean getRandomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public Cell getRandomByAvailable(Map<String, Cell> availableCells) {
        return availableCells.values().stream()
                .skip(ThreadLocalRandom.current().nextInt(availableCells.size()))
                .findFirst()
                .orElseThrow(() -> new ApplicationException("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
