package au.gov.nla.imageanalysis.enums;

public enum ServiceType {
    GL("GL",1, "Google Image Labeling"),
    AL("AL",2,"AWS Image Labeling"),
    ML("ML",3, "Microsoft Image Labeling"),
    MD("MD",4, "Microsoft Image Description");

    private final String code;
    private final int id;
    private final String description;


    ServiceType(String code, int id, String description){
        this.code=code;
        this.id = id;
        this.description =description;
    }


    public String getCode(){return this.code;}
    public int getId(){return this.id;}
    public String getDescription(){return this.description;}

    @Override
    public String toString(){
        return this.getCode();
    }
}
