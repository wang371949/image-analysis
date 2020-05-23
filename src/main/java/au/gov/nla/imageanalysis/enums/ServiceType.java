package au.gov.nla.imageanalysis.enums;

public enum ServiceType {
    GL("GL",false, "Google Image Labeling Service"),
    AL("AL",false,"AWS Image Labeling Service"),
    ML("ML",false, "Microsoft Image Labeling Service"),
    MD("MD",true, "Microsoft Image Description Service");

    private final String code;
    private final boolean labelsAreSentences;
    private final String description;


    ServiceType(String code, boolean labelsAreSentences, String description){
        this.code=code;
        this.labelsAreSentences=labelsAreSentences;
        this.description =description;
    }


    public String getCode(){return this.code;}
    public String getDescription(){return this.description;}
    public boolean getLabelAreSentences(){return labelsAreSentences;}

    @Override
    public String toString(){
        return this.getCode();
    }
}
