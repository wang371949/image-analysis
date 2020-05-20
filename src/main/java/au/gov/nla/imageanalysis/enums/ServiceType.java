package au.gov.nla.imageanalysis.enums;

public enum ServiceType {
    GL("GL", "Google Image Labeling Service"),
    AL("AL", "AWS Image Labeling Service"),
    ML("ML", "Microsoft Image Labeling Service"),
    MD("MD", "Microsoft Image Description Service");

    private String code;
    private String description;

    private ServiceType(String code, String description){this.code=code; this.description =description;}


    public String getCode(){return this.code;}
    public String getDescription(){return this.description;}

    @Override
    public String toString(){
        return this.getCode();
    }
}
