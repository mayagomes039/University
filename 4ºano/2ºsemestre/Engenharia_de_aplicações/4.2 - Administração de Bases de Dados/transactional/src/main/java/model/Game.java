package model;

import java.time.LocalDate;
import java.util.List;

public record Game(
        int id,
        String name,
        LocalDate releaseDate,
        Double price,
        String description,
        String platforms,
        String languages,
        List<String> developers,
        List<String> publishers,
        List<String> categories,
        List<String> genres,
        List<String> tags
) {
}
