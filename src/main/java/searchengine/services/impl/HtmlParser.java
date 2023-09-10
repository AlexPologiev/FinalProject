package searchengine.services.impl;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.config.JsoupConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;




public class HtmlParser {

    private int codeResponse = 404;
    private String content = "";
    private List<String> listLinks = new ArrayList<>();

    private final JsoupConfig jsoupConfig;

    public HtmlParser(JsoupConfig jsoupConfig) {

        this.jsoupConfig = jsoupConfig;
    }


    private Connection getConnection(String url){
        return Jsoup.connect(url)
                .ignoreHttpErrors(true)
                .timeout(jsoupConfig.getTimeOut())
                .userAgent(jsoupConfig.getUserAgent())
                .referrer(jsoupConfig.getReferrer());

    }


    public Document getCodeOfPage(Connection connection) throws Exception {
        return connection.get();
    }

    public List<String> parserLines(String url) throws Exception {
        Thread.sleep(500);
        Connection connection = getConnection(url);
        Thread.sleep(500);
        codeResponse = connection.execute().statusCode();

        Document document = getCodeOfPage(connection);
        content = document.toString();

        Elements elements = document.select("a[href]");
        for (Element element : elements) {
            String link = element.attr("abs:href");
            if (isCorrect(link,url)) {
                listLinks.add(link);
            }
        }
        return listLinks;

    }

    public int getCodeResponse() {
        return codeResponse;
    }

    public String getContent() {
        return content;
    }

    public int connectAndGetCodeResponse(String url) throws IOException {
        Connection connection = getConnection(url);
        return connection.execute().statusCode();
    }

    public String connectAndGetContent(String url) throws IOException {
        Connection connection = getConnection(url);
        Document document = connection.get();
        return document.body().toString();
    }



    private boolean isCorrect(String link,String url){

        return !listLinks.contains(link)
                && isForwardLink(link,url)
                && !link.contains("#")
                && !link.contains(".pdf")
                && !link.contains(".png")
                && !link.contains(".jpg");

    }

    private boolean isForwardLink(String link, String url){
        String domain = separateDomain(url);
        String regex1 = "^" + "https://www." + domain + ".*";
        String regex2 = "^" + "https://" + domain + ".*";
        String dotSign = ".";
        if(link.contains(domain)){
            return link.matches(regex1)|| link.matches(regex2);
        } else {
            return !link.contains(dotSign);
        }
    }


    private String separateDomain(String url){
        return url.replaceAll("https://","").trim();

    }
}
