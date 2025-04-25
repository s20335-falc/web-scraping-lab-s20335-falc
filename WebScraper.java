import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebScraper {
    public static void main(String[] args) {
        String url = "https://bbc.com";  

        try {
            Document doc = Jsoup.connect(url).get();
            
            String title = doc.title();
            System.out.println("Page Title: " + title);
            System.out.println();

            System.out.println("Headings:");
            for (int i = 1; i <= 6; i++) {
                Elements headings = doc.select("h" + i);
                for (Element heading : headings) {
                    System.out.println(heading.text());
                }
            }

            System.out.println("\nLinks:");
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String linkHref = link.attr("abs:href");
                String linkText = link.text();
                System.out.println("  Text: " + linkText);
                System.out.println("  URL: " + linkHref);
                System.out.println();
            }

        } catch (Exception e) {
            System.out.println("Error fetching the page: " + e.getMessage());
        }
    }
}
