package pt.uminho.di.aa.miradourum.dto;

public class ImageWithPontoInteresseDTO {
    private String url;
    private String pontoInteresseName;

    public ImageWithPontoInteresseDTO(String url, String pontoInteresseName) {
        this.url = url;
        this.pontoInteresseName = pontoInteresseName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPontoInteresseName() {
        return pontoInteresseName;
    }

    public void setPontoInteresseName(String pontoInteresseName) {
        this.pontoInteresseName = pontoInteresseName;
    }
}
