package pt.uminho.di.aa.miradourum.projections.User;

import pt.uminho.di.aa.miradourum.projections.PontoInteresse.PIMapProjection;

import java.util.List;

public interface UserProfileProjection {
    String getId();
    String getUsername();
    String getProfileImage();
    List<PIMapProjection> getPontoInteresse();
    Integer getRole();
    String getEmail();
}
