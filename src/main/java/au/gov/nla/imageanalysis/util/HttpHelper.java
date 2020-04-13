package au.gov.nla.imageanalysis.util;


import au.gov.nla.imageanalysis.exceptions.InternalServerException;
import au.gov.nla.imageanalysis.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


// this is a class to download image from DLC and return as a input Stream.
public class HttpHelper {

    private static Logger log = LoggerFactory.getLogger(HttpHelper.class);

    private HttpHelper(){

    }

    public static String get(String myUrl){
        HttpURLConnection conn = null;
        BufferedReader br = null;
        try{
            conn = getConnection(myUrl);
            conn.setRequestMethod("GET");
            log.debug(("Requesting" + myUrl));
            if (conn.getResponseCode() == 404){
                log.error("Request to {} returned http 404", myUrl);
                throw new ResourceNotFoundException();
            }else if (conn.getResponseCode() !=200){
                log.error("Request to {} returned http code {}", myUrl, conn.getResponseCode());
                throw new InternalServerException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String result="";
            String output;
            while ((output = br.readLine())!=null){
                result = result+output;
            }

            return result;
        }catch (IOException e){
            log.error(e.getMessage(),e);
            throw new InternalServerException("Failed : "+e.getMessage(),e);
        }finally {
            try{
                if (conn!=null){
                    conn.disconnect();
                }
                if(br != null){
                    br.close();
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }


    public static InputStream getAsStream(String queryUrl) throws IOException{
        log.debug("Requesting " + queryUrl);

        HttpURLConnection conn = getConnection(queryUrl);
        conn.setRequestProperty("Content-Encoding", "gzip");
        int responseCode = conn.getResponseCode();
        if (responseCode == 404){
            log.error("Request to {} returned http 404", queryUrl);
            throw new ResourceNotFoundException();
        }else if (conn.getResponseCode() !=200){
            log.error("Request to {} returned http code {}", queryUrl, conn.getResponseCode());

            throw new InternalServerException("Failed : HTTP error code : "+ responseCode);
        }
        return conn.getInputStream();

    }


    public static HttpURLConnection getConnection(String queryUrl) throws IOException {
        URL url = new URL(queryUrl);
        return (HttpURLConnection) url.openConnection();
    }
}

