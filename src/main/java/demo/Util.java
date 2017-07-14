package demo;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Util {
    private static final String stopwordsFile = "/Users/jiaxu/Dropbox/workspace/cs504-2/web-crawler/stopwords.txt";
    private static CharArraySet getStopwords() {
        List<String> stopwordsList = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(stopwordsFile));
            String line;
            while((line = reader.readLine()) != null) {
                stopwordsList.add(line.trim());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CharArraySet(stopwordsList, true);
    }

    // tokenize, remove stop word, stem
    public static List<String> cleanedTokenize(String input) throws IOException {
        List<String> tokens = new ArrayList<String>();
        StringReader reader = new StringReader(input.toLowerCase());
        Tokenizer tokenizer = new StandardTokenizer();
        tokenizer.setReader(reader);
        TokenStream tokenStream = new StandardFilter(tokenizer);
        tokenStream = new StopFilter(tokenStream, getStopwords());
        tokenStream = new KStemFilter(tokenStream);
        CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            String token = charTermAttribute.toString();
            if(token.length() < 2 || token.matches("[0-9.]+")) continue;
            tokens.add(token);
        }
        tokenStream.end();
        tokenStream.close();
        tokenizer.close();
        return tokens;
    }
}
