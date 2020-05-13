package au.gov.nla.imageanalysis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVHelper {
    private static Logger log = LoggerFactory.getLogger(HttpHelper.class);
    private CSVHelper(){
    }

    public static Map<String, List<String>> readCSV(String filePath){
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = null;
            while((line=br.readLine())!=null){
                String [] id_labels = line.split(",");
                String id = id_labels[0];
                List<String> labels = Arrays.asList(id_labels[1].split(" "));
                map.put(id,labels);
            }
        }catch (Exception e){{log.error("IOException: "+e.getMessage(),e);}
        }
        return map;
    }
}
