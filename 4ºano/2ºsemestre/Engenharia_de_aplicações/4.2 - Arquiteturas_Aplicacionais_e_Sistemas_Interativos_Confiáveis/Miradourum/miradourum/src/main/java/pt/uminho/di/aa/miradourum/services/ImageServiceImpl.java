package pt.uminho.di.aa.miradourum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.uminho.di.aa.miradourum.models.Image;
import pt.uminho.di.aa.miradourum.models.Review;
import pt.uminho.di.aa.miradourum.repositories.ImageRepository;


@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Override
    public Image saveImage(String url, Review review) {
        Image image = new Image();
        image.setUrl(url);
        image.setReview(review);

        imageRepository.save(image);
        return image;
    }
    @Override
    public void deleteImageById(Long id){
        imageRepository.deleteById(id);
    }

}
