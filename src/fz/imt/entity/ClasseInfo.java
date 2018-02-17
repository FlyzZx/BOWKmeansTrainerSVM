package fz.imt.entity;

public class ClasseInfo extends Object {

    private String brandname;
    private String url;
    private String classifier;

    public ClasseInfo() {
    }

    public ClasseInfo(String brandname, String url, String classifier) {

        this.brandname = brandname;
        this.url = url;
        this.classifier = classifier;
    }

    public String getBrandname() {
        return brandname;
    }

    public void setBrandname(String brandname) {
        this.brandname = brandname;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }
}
