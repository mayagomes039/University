package model;

import java.time.LocalDate;
import java.util.List;

public record User(
        int id,
        String username,
        LocalDate createdDate,
        boolean vacBanned,
        String profileDescription,
        String country,
        List<UserProfileGame> topGames
) {
}
