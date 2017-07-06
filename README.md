# amazon-web-crawler

This is a web crawler to grab product information from Amazon website. 

### Requirements
- Java JDK 
- Maven

### Input Query Data
In `rawQuery3.txt`,
```
query, bid, campaignID, queryGroupID 
Prenatal DHA, 3.4, 8040, 10
```

### Get Started
1. Download file
```
git clone https://github.com/xujiahaha/amazon-web-crawler.git
```
2. Add proxy list file and modify file name of `proxylist_bittiger.csv` in `Crawler.java` before running.
3. Build
```
mvn clean install
```

### Sample Crawled Data
```
{
	"adId":12,
	"campaignId":8040,
	"keyWords":["diet","standard","prenatal","dha","algae","base","100","vegan","pill","best","omega","3","epa","dha","supplement","pair","prenatal","vitamin","healthy","pregnancy","fish","oil","mercury","3rd","party","lab","test"],
	"relevanceScore":0.0,
	"pClick":0.0,
	"bidPrice":3.4,
	"rankScore":0.0,
	"qualityScore":0.0,
	"costPerClick":0.0,
	"position":0,
	"title":"Diet Standards Prenatal DHA - Algae-Based = 100% Vegan Pills - Best Omega 3 EPA & DHA Supplement to pair with Prenatal Vitamins for a Healthy Pregnancy - No Fish Oil, No Mercury, 3rd Party Lab Tested!",
	"price":39.9,
	"thumbnail":"https://images-na.ssl-images-amazon.com/images/I/41CSVCDu2cL._AC_US218_.jpg",
	"description":null,
	"brand":"Diet Standards",
	"detailUrl":"https://www.amazon.com/Diet-Standards-Prenatal-DHA-Algae-Based/dp/B01FGCTZFW/ref=sr_1_13_a_it/142-1034917-0549932?ie=UTF8&qid=1499315139&sr=8-13&keywords=Prenatal+DHA",
	"query":"Prenatal DHA",
	"queryGroupId":10,
	"category":"Health & Household"
}

```

### Dependencies
- Jsoup
- Jackson
- Lucene
- slf4j



----------
