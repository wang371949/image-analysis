package au.gov.nla.imageanalysis.util;

import au.gov.nla.imageanalysis.logic.ImageLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CSVHelper {
    private static Logger log = LoggerFactory.getLogger(HttpHelper.class);
    private CSVHelper(){
    }

    /**
     * This method read a pre-formatted csv file and store values in a hashmap.
     * @param filePath  the file path to the csv file
     * @return a hash map. The key is the pid and values are lists of instances of ImageLabels class
     */
    public static Map<String, List<ImageLabel>> readCSV(String filePath) throws IOException {
        Map<String, List<ImageLabel>> map = new HashMap<String, List<ImageLabel>>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] id_labels = line.split(",");
            List<ImageLabel> imageLabels = new ArrayList<>();
            for (String label : id_labels[1].split(" ")) {
                imageLabels.add(new ImageLabel(label));
            }
            map.put(id_labels[0], imageLabels);
        }
        return map;
    }
}
