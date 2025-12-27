package pt.uminho.di.aa.miradourum.services;

import pt.uminho.di.aa.miradourum.models.Image;
import pt.uminho.di.aa.miradourum.models.Review;


public interface ImageService {
    Image saveImage(String url, Review review);
    void deleteImageById(Long id);
}
