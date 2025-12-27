package pt.uminho.di.aa.miradourum.services;

import pt.uminho.di.aa.miradourum.models.ImagePontoInteresse;
import pt.uminho.di.aa.miradourum.models.PontoInteresse;


public interface ImagePontoInteresseService {
    ImagePontoInteresse saveImage(String url, PontoInteresse ponto);
}
