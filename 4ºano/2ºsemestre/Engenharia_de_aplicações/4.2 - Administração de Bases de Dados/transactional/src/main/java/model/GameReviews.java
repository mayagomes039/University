package model;

import java.util.List;

public record GameReviews(
        double overallScore,
        List<Review> recentReviews
) {
}
