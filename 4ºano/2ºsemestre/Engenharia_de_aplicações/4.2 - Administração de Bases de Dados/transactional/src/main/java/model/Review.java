package model;

import java.time.LocalDateTime;

public record Review(
        int userId,
        String username,
        LocalDateTime createdDate,
        boolean recommend,
        String text,
        int playtime
) {
}
