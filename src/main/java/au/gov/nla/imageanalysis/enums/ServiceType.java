package au.gov.nla.imageanalysis.enums;

public enum ServiceType {
    GOOGLE_LABELING_SERVICE("1"),
    AWS_LABELING_SERVICE("2");

    private String code;

    private ServiceType(String code){this.code=code;}

    public String code(){return this.code;}
}
