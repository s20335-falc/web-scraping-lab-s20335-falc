package com;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/scrape", "/json", "/download"})
public class ScraperServlet extends HttpServlet {

    public static class ScrapedData {
        private String type;
        private String content;

        public ScrapedData(String type, String content) {
            this.type = type;
            this.content = content;
        }

        public String getType() {
            return type;
        }

        public String getContent() {
            return content;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String url = request.getParameter("url");
        String[] options = request.getParameterValues("options");
        List<ScrapedData> scrapedDataList = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url).get();

            if (options != null) {
                for (String option : options) {
                    switch (option) {
                        case "title":
                            scrapedDataList.add(new ScrapedData("Title", doc.title()));
                            break;
                        case "links":
                            Elements links = doc.select("a[href]");
                            for (Element link : links) {
                                scrapedDataList.add(new ScrapedData("Link", link.absUrl("href")));
                            }
                            break;
                        case "images":
                            Elements images = doc.select("img[src]");
                            for (Element img : images) {
                                scrapedDataList.add(new ScrapedData("Image", img.absUrl("src")));
                            }
                            break;
                    }
                }
            }

            HttpSession session = request.getSession();
            Integer visitCount = (Integer) session.getAttribute("visitCount");
            if (visitCount == null) visitCount = 0;
            session.setAttribute("visitCount", ++visitCount);
            session.setAttribute("scrapedData", scrapedDataList);

            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

            out.println("<html><head><title>Scraped Results</title></head><body>");
            out.println("<h3>You have visited this page " + visitCount + " times.</h3>");
            out.println("<h2>Scraped Data:</h2>");

            if (scrapedDataList.isEmpty()) {
                out.println("<p>No data was scraped.</p>");
            } else {
                out.println("<table border='1'><tr><th>Type</th><th>Content</th></tr>");
                for (ScrapedData data : scrapedDataList) {
                    out.println("<tr><td>" + data.getType() + "</td><td>" + data.getContent() + "</td></tr>");
                }
                out.println("</table>");
            }

            out.println("<br><a href='json'>Download JSON</a>");
            out.println(" | <a href='download'>Download CSV</a>");
            out.println("</body></html>");

        } catch (Exception e) {
            response.getWriter().println("Error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();
        HttpSession session = request.getSession();
        List<ScrapedData> scrapedDataList = (List<ScrapedData>) session.getAttribute("scrapedData");

        if (scrapedDataList == null || scrapedDataList.isEmpty()) {
            response.getWriter().println("No scraped data available. Please submit the form first.");
            return;
        }

        switch (path) {
            case "/json":
                response.setContentType("application/json");
                String json = new Gson().toJson(scrapedDataList);
                response.getWriter().write(json);
                break;

            case "/download":
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment;filename=scraped_data.csv");

                PrintWriter writer = response.getWriter();
                writer.println("Type,Content");
                for (ScrapedData data : scrapedDataList) {
                    writer.printf("\"%s\",\"%s\"\n",
                            data.getType().replace("\"", "\"\""),
                            data.getContent().replace("\"", "\"\""));
                }
                break;

            default:
                response.getWriter().println("Invalid endpoint.");
        }
    }
}
