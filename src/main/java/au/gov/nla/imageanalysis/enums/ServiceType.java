package au.gov.nla.imageanalysis.enums;

public enum ServiceType {
    GOOGLE_LABELING_SERVICE("GL", "Google Image Labeling Serivce"),
    AWS_LABELING_SERVICE("AL", "AWS Image Labeling Service"),
    MICROSOFT_AZURE_LABELING_SERVICE("ML", "Microsoft Image Labeling Service"),
    MICROSOFT_AZURE_DESCRIPTION_SERVICE("MD", "Microsoft Image Description Service");

    private String code;
    private String description;

    private ServiceType(String code, String description){this.code=code; this.description =description;}


    public String getCode(){return this.code;}
    public String getDescription(){return this.description;}
}
