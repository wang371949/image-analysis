package au.gov.nla.imageanalysis.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class InOut {


    /**
     * save the image of given url into local file for visualization
     * @param imageAsByteArray the image as an Inputstrean.
     * @param filepath the location of the image to be saved
     */
    public static void saveImage(byte[] imageAsByteArray, String filepath) throws IOException{
        BufferedImage image = null;
        File outputfile = new File(filepath);
        image = ImageIO.read(new ByteArrayInputStream(imageAsByteArray));
        ImageIO.write(image,"jpg",outputfile);
    }


    public static InputStream loadImage(String filepath) throws IOException{
        InputStream inputStream = new FileInputStream(new File(filepath));
        return inputStream;
    }

    public static Map<String,String> loadImagePathAsList(String folderPath) throws IOException{
        Map<String,String> result = new HashMap<>();
        Stream<Path> walk = Files.walk(Paths.get(folderPath));
        List<String> filePathList = walk.filter(Files::isRegularFile)
                    .filter(p->p.getFileName().toString().toLowerCase().contains(".jpg"))
                    .map(x->x.toString())
                    .collect(Collectors.toList());
        for(String filePath: filePathList){
            String [] splitFilePath = filePath.split("/");
            String filename = splitFilePath[splitFilePath.length-1];
            String pid = filename.substring(0,filename.length()-4);
            result.put(pid,filePath);
        }
        return result;
    }
}
