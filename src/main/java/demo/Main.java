package demo;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Crawler crawler = new Crawler();

        crawler.initProxy();
        crawler.testProxy();

        FileWriter fileWriter = new FileWriter("ads_3.json");
        BufferedWriter writer = new BufferedWriter(fileWriter);
        ObjectMapper mapper = new ObjectMapper();
        Set<String> queries = new HashSet<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("rawQuery3.txt"));
            String line;
            while((line = reader.readLine()) != null) {
                String[] strs = line.split(",");
                if(strs.length != 4) {
                    continue;
                }
                String query = strs[0].trim();
                if(queries.add(query)) {
                    double bidPrice = Double.parseDouble(strs[1].trim());
                    int campaignId = Integer.parseInt(strs[2].trim());
                    int queryGroupId = Integer.parseInt(strs[3].trim());
                    for(int page = 1; page <= 5; page++){
                        List<Ad> adList = crawler.getAmazonProds(query, page, bidPrice, campaignId, queryGroupId);
                        if(adList == null) continue;
                        for(Ad ad : adList) {
                            writer.write(mapper.writeValueAsString(ad));
                            writer.newLine();
                        }
//                        Thread.sleep(1000L);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
