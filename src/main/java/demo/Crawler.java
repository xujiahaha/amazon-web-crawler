package demo;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

@Data
public class Crawler {
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";
    private final String authUser = "bittiger";
    private final String authPassword = "cs504";
    private static final String AMAZON_QUERY_URL = "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=";
    private List<String> proxyList;
    private List<Ad> adList;
    private final String exceptionLog = "exception_log.txt";

    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
    private BufferedWriter errorWriter;

    public Crawler() {
        try {
            FileWriter fileWriter = new FileWriter(exceptionLog);
            errorWriter = new BufferedWriter(fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initProxy() {
        proxyList = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("proxylist_bittiger.csv"));
            String line;
            while((line = reader.readLine()) != null) {
                proxyList.add(line.split(",")[0]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.setProperty("http.proxyPort", "60099"); // set proxy port
        System.setProperty("socksProxyPort", "61336"); // set socks proxy port
        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );
    }

    public void setProxyHost() {
        Random rand = new Random();
        System.setProperty("socksProxyHost", proxyList.get(rand.nextInt(proxyList.size()))); // set socks proxy server
//        System.setProperty("http.proxyHost", proxyList.get(rand.nextInt(proxyList.size())));
    }

    public void testProxy() {

        String test_url = "http://www.toolsvoid.com/what-is-my-ip-address";
        try {
            HashMap<String,String> headers = new HashMap<String,String>();
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            headers.put("Accept-Encoding", "gzip, deflate, br");
            headers.put("Accept-Language", "en-US,en;q=0.8");
            Document doc = Jsoup.connect(test_url).headers(headers).userAgent(USER_AGENT).timeout(10000).get();
            String iP = doc.select("body > section.articles-section > div > div > div > div.col-md-8.display-flex > div > div.table-responsive > table > tbody > tr:nth-child(1) > td:nth-child(2) > strong").first().text(); //get used IP.
            System.out.println("IP-Address: " + iP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<String,String>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        return headers;
    }


    private int adId = 0;
    public List<Ad> getAmazonProds(String query, int page, double bidPrice, int campaignId, int queryGroupId) {
        setProxyHost();
        adList = new ArrayList<Ad>();
        String queryEncoded = "";
        try {
            queryEncoded = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = AMAZON_QUERY_URL + queryEncoded + "&page=" + page;
        System.out.println("url: " + url);
        try {
            Map<String, String> headers = getHeaders();
            Document doc = Jsoup.connect(url).maxBodySize(0).headers(headers).userAgent(USER_AGENT).timeout(10000).get();
            Elements prods = doc.getElementsByClass("s-result-item celwidget ");

            System.out.println("number of prod: " + prods.size());
            String categoryStr = getCategory(doc);
            System.out.println("prod category: " + categoryStr);

            if(prods.size() == 0) {
                return null;
            }
            String id = prods.first().attr("id");
            System.out.println(id);
            int startIndex = Integer.parseInt(id.substring(7));
            System.out.println(startIndex);

            for(Integer i = 0;i < prods.size();i++){
                int index = i + startIndex;

                // skip the element whose title is null
                String title = getTitle(doc, index);
                if(title == null || title.length() == 0) {
                    continue;
                }

                Ad ad = new Ad();

                ad.adId = adId++;
                ad.query = query;
                ad.bidPrice = bidPrice;
                ad.campaignId = campaignId;
                ad.queryGroupId = queryGroupId;
                ad.title = title;
                ad.price = getPrice(doc, index);
                ad.detailUrl = getDetailUrl(doc, index);
                ad.brand = getBrand(doc, index);
                ad.thumbnail = getThumbnailUrl(doc, index);
                ad.category = categoryStr;
                ad.keyWords = Util.cleanedTokenize(ad.title);

                adList.add(ad);
                System.out.println(index);
                System.out.println("title: " + getTitle(doc, index));;
                System.out.println("price: " + getPrice(doc, index));
                System.out.println("brand: " + getBrand(doc, index));
                System.out.println("detailUrl: " + getDetailUrl(doc, index));
                System.out.println("thumbnailUrl: " + getThumbnailUrl(doc, index));
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
        return adList;
    }

    private String getTitle(Document doc, int index) {
        // #result_1 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a
        // #result_18 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a
        String queryStr = "#result_" + index + " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a";
        Element titleEle = doc.select(queryStr).first();
        if(titleEle != null) {
            return titleEle.attr("title");
        } else {
            logger.info("Title is null");
            try {
                errorWriter.write("Title is null for result_" + index + " @ " + doc.baseUri() );
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    private String getDetailUrl(Document doc, int index) {
        // #result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a
        String queryStr = "#result_" + index + " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a";
        Element detailUrlEle = doc.select(queryStr).first();
        if(detailUrlEle != null) {
            String detailUrl = detailUrlEle.attr("href");
            if(detailUrl.contains("url")) {
                // extract url from redirected url
                return decodeRedirectUrl(detailUrl);
            } else {
                return detailUrl;
            }
        } else {
            logger.info("Detail url is null");
            try {
                errorWriter.write("Detail is null for result_" + index + " @ " + doc.baseUri() );
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    private String getBrand(Document doc, int index) {
        // #result_18 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(2) > span:nth-child(2)
        String queryStr = "#result_" + index + " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(2) > span:nth-child(2)";
        Element brandEle = doc.select(queryStr).first();
        if(brandEle != null) {
            return brandEle.text();
        } else {
            logger.info("Brand is null");
            try {
                errorWriter.write("Brand is null for result_" + index + " @ " + doc.baseUri() );
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    private double getPrice(Document doc, int index) {
        String id = "result_" + index;
        Element prod = doc.getElementById(id);
        Element priceWholeEle = prod.getElementsByClass("sx-price-whole").first();
        Element priceFractionalEle = prod.getElementsByClass("sx-price-fractional").first();
        if(priceWholeEle != null && priceFractionalEle != null) {
            String priceStr = priceWholeEle.text().replace(",", "")+"."+priceFractionalEle.text();
            return Double.parseDouble(priceStr);
        } else {
            logger.info("Error to parse price");
            try {
                errorWriter.write("Error to parse price for result_" + index + " @ " + doc.baseUri() );
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    private String getThumbnailUrl(Document doc, int index) {
        // #result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img
        String queryStr = "#result_" + index + " > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img";
        Element thumbnailUrlEle = doc.select(queryStr).first();
        if(thumbnailUrlEle != null) {
            return thumbnailUrlEle.attr("src");
        } else {
            logger.info("Thumbnail url is null");
            try {
                errorWriter.write("Thumbnail url is null for result_" + index + " @ " + doc.baseUri() );
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    private String getCategory(Document doc) {
        //#leftNavContainer > ul:nth-child(2) > div > li:nth-child(1) > span > a > h4
        String queryStr = "#leftNavContainer > ul:nth-child(2) > div > li:nth-child(1) > span > a > h4";
        Element categoryEle = doc.select(queryStr).first();
        if(categoryEle != null) {
            return categoryEle.text();
        } else {
            logger.info("Category is null");
            try {
                errorWriter.write("Category is null for results" + " @ " + doc.baseUri() );
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    private String decodeRedirectUrl(String url) {
        int start = url.indexOf("url=")+4;
        try {
            return URLDecoder.decode(url.substring(start), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

}
