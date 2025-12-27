package pt.uminho.di.aa.miradourum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.uminho.di.aa.miradourum.models.ImagePontoInteresse;
import pt.uminho.di.aa.miradourum.models.PontoInteresse;
import pt.uminho.di.aa.miradourum.repositories.ImagePontoInteresseRepository;


@Service
public class ImagePontoInteresseServiceImpl implements ImagePontoInteresseService {

    @Autowired
    private ImagePontoInteresseRepository imagePontoInteresseRepository;

    @Override
    public ImagePontoInteresse saveImage(String url, PontoInteresse ponto) {
        ImagePontoInteresse image = new ImagePontoInteresse();
        image.setUrl(url);
        image.setPontoInteresse(ponto);

        imagePontoInteresseRepository.save(image);
        return image;
    }
}
