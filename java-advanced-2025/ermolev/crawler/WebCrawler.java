
package info.kgeorgiy.ja.ermolev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import static info.kgeorgiy.java.advanced.crawler.URLUtils.getHost;

public class WebCrawler implements NewCrawler {

    private final Downloader downloader;
    private final ExecutorService downloadQue;
    private final ExecutorService extractQue;
    private final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloadQue = Executors.newFixedThreadPool(downloaders);
        this.extractQue = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 5 || args[0] == null) {
            System.err.println("We expect: WebCrawler url [depth [downloads [extractors [perHost]]]]");
            return;
        }

        String url = args[0];
        int depth = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        int downloads = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        int extractors = args.length > 3 ? Integer.parseInt(args[3]) : 1;
        int perHost = args.length > 4 ? Integer.parseInt(args[4]) : 1;

        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(1.0, Paths.get("crawler")), downloads, extractors, perHost)) {
            Result result = crawler.download(url, depth);
            System.out.println("Downloaded:");
            result.getDownloaded().forEach(System.out::println);

            if (!result.getErrors().isEmpty()) {
                System.out.println("\nErrors:");
                result.getErrors().forEach((u, e) -> System.out.println(u + ": " + e));
            }
        } catch (IOException e) {
            System.err.println("We can't create CachingDownloader: " + e.getMessage());
        }
    }

    @Override
    public Result download(String url, int depth, List<String> excludes) {
         Set<String> visited = ConcurrentHashMap.newKeySet();
         //note -- для такого лучше юзать CopyOnWriteArrayList
         List<String> result = Collections.synchronizedList(new ArrayList<>());
         ConcurrentMap<String, IOException> errors = new ConcurrentHashMap<>();
         List<String> urls = new ArrayList<>();

         Phaser phaser = new Phaser(1);
         if(!Check(url, excludes)) {
             return new Result(new ArrayList<>(result), errors);
         }
         visited.add(url);
         urls.add(url);

         recDownload(depth, urls, result, errors, visited, phaser, excludes);
         phaser.arriveAndDeregister();

         close();

         return new Result(new ArrayList<>(result), errors);
    }

    private void recDownload(int depth, List<String> urls, List<String> result, ConcurrentMap<String, IOException> errors, Set<String> visited, Phaser phaser, List<String> excludes) {
        ConcurrentLinkedQueue<Document> docs = new ConcurrentLinkedQueue<>();
        for (String url : urls) {
            phaser.register();
            downloadQue.submit(() -> taskDownload(url, result, errors, docs, phaser));
        }
        phaser.arriveAndAwaitAdvance();

        if (depth > 1) {
            ConcurrentLinkedQueue<List<String>> lists = new ConcurrentLinkedQueue<>();
            for (Document doc : docs) {
                phaser.register();
                extractQue.submit(() -> taskExtract(doc, lists, phaser));
            }
            phaser.arriveAndAwaitAdvance();
            List<String> newUrls = new ArrayList<>();
            for(List<String> list : lists) {
                for(String url : list) {
                    if (visited.add(url) && Check(url, excludes)) {
                        newUrls.add(url);
                    }
                }
            }

            recDownload(depth - 1, newUrls, result, errors, visited, phaser, excludes);
        }
    }

    private void taskDownload(final String url, List<String> result, ConcurrentMap<String, IOException> errors, ConcurrentLinkedQueue<Document> docs, Phaser phaser) {
        try {
            Document document = downloader.download(url);
            result.add(url);
            docs.add(document);
        } catch (final IOException e) {
            errors.put(url, e);
        } finally {
            phaser.arriveAndDeregister();
        }
    }

    private void taskExtract(final Document document, ConcurrentLinkedQueue<List<String>> lists, Phaser phaser) {
        try {
            lists.add(document.extractLinks());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            phaser.arriveAndDeregister();
        }
    }

    private boolean Check(String url, List<String> excludes) {
        try {
            String host = getHost(url);
            for (String substr : excludes) {
                if (host.contains(substr)) {
                    return false;
                }
            }
        } catch (MalformedURLException ignored) {
        }
        return true;
    }

    @Override
    public void close() {
        //note -- use .close() method
        downloadQue.shutdownNow();
        extractQue.shutdownNow();
    }
}