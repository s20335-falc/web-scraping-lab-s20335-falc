import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class WebScraper {
    public static class NewsArticle {
        private String headline;
        private String publicationDate;
        private String author;
        private String url;

        public NewsArticle(String headline, String publicationDate, String author, String url) {
            this.headline = headline;
            this.publicationDate = publicationDate;
            this.author = author;
            this.url = url;
        }

        @Override
        public String toString() {
            return "Headline: " + headline + "\n" +
                   "Date: " + publicationDate + "\n" +
                   "Author: " + author + "\n" +
                   "URL: " + url + "\n";
        }

        // Getters
        public String getHeadline() { return headline; }
        public String getPublicationDate() { return publicationDate; }
        public String getAuthor() { return author; }
        public String getUrl() { return url; }
    }

    public static void main(String[] args) {
        String url = "https://bbc.com";
        List<NewsArticle> articles = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url).get();
            
            System.out.println("Page Title: " + doc.title());
            System.out.println("\nHeadings:");
            for (int i = 1; i <= 6; i++) {
                Elements headings = doc.select("h" + i);
                for (Element heading : headings) {
                    System.out.println("  h" + i + ": " + heading.text());
                }
            }

            System.out.println("\nScraping Articles:");
            Elements links = doc.select("a[href]");
            Set<String> visited = new HashSet<>(); // avoid duplicates

            for (Element link : links) {
                String linkHref = link.absUrl("href");
                String linkText = link.text().trim();

                if (linkHref.contains("/news/") && !visited.contains(linkHref)) {
                    visited.add(linkHref);

                    try {
                        Document linkDoc = Jsoup.connect(linkHref).get();
                        String author = linkDoc.select("[data-testid=byline-new-contributors] div div span").text();
                        String publicationDate = linkDoc.select("[data-testid=byline-new] time").text();

                        if (!linkText.isEmpty()) {
                            NewsArticle article = new NewsArticle(linkText, publicationDate, author, linkHref);
                            articles.add(article);

                            System.out.println(article);
                        }
                    } catch (Exception e) {
                        System.out.println("  Skipped (could not load): " + linkHref);
                    }
                }
            }

            System.out.println("\nTotal Articles Scraped: " + articles.size());

        } catch (Exception e) {
            System.out.println("Error fetching the main page: " + e.getMessage());
        }
    }
}
