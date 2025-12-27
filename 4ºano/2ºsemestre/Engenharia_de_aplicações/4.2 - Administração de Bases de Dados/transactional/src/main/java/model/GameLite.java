package model;

import java.time.LocalDate;

public record GameLite(
        int id,
        String name,
        LocalDate releaseDate
) {
}
