package au.gov.nla.imageanalysis.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class InOut {
    private static final Logger log = LoggerFactory.getLogger(HttpHelper.class);


    /**
     * save the image of given url into local file for visualization
     * @param imageAsByteArray the image as an Inputstrean.
     * @param filepath the location of the image to be saved
     */
    public static void saveImage(byte[] imageAsByteArray, String filepath) throws IOException{
        BufferedImage image;
        File outputfile = new File(filepath);
        image = ImageIO.read(new ByteArrayInputStream(imageAsByteArray));
        ImageIO.write(image,"jpg",outputfile);
    }

    /**
     * The method read a image file and convert it into a input strean
     */
    public static InputStream loadImage(String filepath) throws IOException{
        return new FileInputStream(new File(filepath));
    }

    /**
     * The method read image files from a folder and stores their id and file path paris in a hash map
     */
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


    /**
     * This method read a pre-formatted csv file and store values in a hashmap.
     * @param filePath  the file path to the csv file
     * @return a hash map. The key is the pid and values are lists of instances of ImageLabels class
     */
    public static List<String[]> csvReaderOneByOne(String filePath) throws IOException{
        List<String[]> list = new ArrayList<>();
        FileReader fileReader = new FileReader(filePath);
        CSVReader csvReader = new CSVReader(fileReader);
        String[] line;
        try {
            while ((line = csvReader.readNext())!=null) {
                list.add(line);
            }
        }catch (CsvValidationException e) {
            log.error("CsvValidationException: "+e.getMessage(),e);
        }
        fileReader.close();
        csvReader.close();
        return list;
    }

    /**
     * The method writes a multiple-line csv file from a list of string array
     */
    public static void csvWriterOneByOne(List<String[]> stringArray, Writer fileWriter) throws IOException{
        CSVWriter writer = new CSVWriter(fileWriter);
        for (String [] array: stringArray){
            writer.writeNext(array);
        }
        writer.close();
    }

    /**
     * The method writes a one-line csv file from a list of string array
     */
    public static void csvWriterOneLine(String[] stringArray, Writer fileWriter) throws IOException{
        CSVWriter writer = new CSVWriter(fileWriter);
        writer.writeNext(stringArray);
        writer.flush();
        writer.close();
    }
}
