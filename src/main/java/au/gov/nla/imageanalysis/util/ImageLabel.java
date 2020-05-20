package au.gov.nla.imageanalysis.util;


public class ImageLabel {
    private final String name;
    private final Float confidence;

    /**  constructors */
    public ImageLabel(String name, Float confidence){this.name = name;this.confidence =confidence;}
    public ImageLabel(String name){this.name = name;this.confidence =1.0f;}

    public String getName() {return name;}
    public float getConfidence(){return confidence;}
}
