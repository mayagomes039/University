package pt.uminho.di.aa.miradourum.services;

import pt.uminho.di.aa.miradourum.models.Image;
import pt.uminho.di.aa.miradourum.models.PontoInteresse;
import pt.uminho.di.aa.miradourum.models.Review;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ReviewService {
    Review saveReview(Integer rating, String comment, LocalDateTime creationDateConverted, Long userId, PontoInteresse point);
    void addImages(Long revId, List<Image> images);
    public Optional<Review> getById(Long reviewID);
    public void updateReview(Review review);
    public void deleteReviewById(Review review);
    public List<Review> getAllReviewsUser(Long userId);
    public void updateAverageScore(PontoInteresse pontoInteresse);
}
