package au.gov.nla.imageanalysis.enums;

public enum ServiceType {
    GL("GL",1,false, "Google Image Labeling"),
    AL("AL",2,false,"AWS Image Labeling"),
    ML("ML",3,false, "Microsoft Image Labeling"),
    MD("MD",4,true, "Microsoft Image Description");

    private final String code;
    private final int id;
    private final boolean labelsAreSentences;
    private final String description;


    ServiceType(String code, int id, boolean labelsAreSentences, String description){
        this.code=code;
        this.id = id;
        this.labelsAreSentences=labelsAreSentences;
        this.description =description;
    }


    public String getCode(){return this.code;}
    public int getId(){return this.id;}
    public String getDescription(){return this.description;}
    public boolean getLabelAreSentences(){return labelsAreSentences;}

    @Override
    public String toString(){
        return this.getCode();
    }
}
