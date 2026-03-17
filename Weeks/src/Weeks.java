import java.util.*;
import java.util.concurrent.*;

public class RealTimeAnalytics {

    // pageUrl -> total visit count
    private ConcurrentHashMap<String, Integer> pageViews;

    // pageUrl -> unique users
    private ConcurrentHashMap<String, Set<String>> uniqueVisitors;

    // traffic source -> count
    private ConcurrentHashMap<String, Integer> sourceCount;

    // Constructor
    public RealTimeAnalytics() {
        pageViews = new ConcurrentHashMap<>();
        uniqueVisitors = new ConcurrentHashMap<>();
        sourceCount = new ConcurrentHashMap<>();

        startDashboardUpdater();
    }

    // Event structure
    static class Event {
        String url;
        String userId;
        String source;

        Event(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    // Process incoming event (O(1))
    public void processEvent(Event event) {

        // Update page views
        pageViews.merge(event.url, 1, Integer::sum);

        // Update unique visitors
        uniqueVisitors
                .computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet())
                .add(event.userId);

        // Update traffic source
        sourceCount.merge(event.source, 1, Integer::sum);
    }

    // Get Top 10 pages
    public List<Map.Entry<String, Integer>> getTopPages() {
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>(minHeap);
        result.sort((a, b) -> b.getValue() - a.getValue());

        return result;
    }

    // Dashboard output
    public void getDashboard() {
        System.out.println("\n===== REAL-TIME DASHBOARD =====");

        List<Map.Entry<String, Integer>> topPages = getTopPages();

        int rank = 1;
        for (Map.Entry<String, Integer> entry : topPages) {
            String url = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();

            System.out.println(rank + ". " + url +
                    " - " + views + " views (" + unique + " unique)");
            rank++;
        }

        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, Integer> entry : sourceCount.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("================================\n");
    }

    // Auto-refresh every 5 seconds
    private void startDashboardUpdater() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            getDashboard();
        }, 5, 5, TimeUnit.SECONDS);
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        RealTimeAnalytics analytics = new RealTimeAnalytics();

        // Simulate streaming events
        String[] urls = {"/article/breaking-news", "/sports/championship", "/tech/ai"};
        String[] sources = {"google", "facebook", "direct"};

        Random rand = new Random();

        for (int i = 0; i < 100; i++) {
            String url = urls[rand.nextInt(urls.length)];
            String userId = "user_" + rand.nextInt(50);
            String source = sources[rand.nextInt(sources.length)];

            analytics.processEvent(new Event(url, userId, source));

            Thread.sleep(50); // simulate stream
        }
    }
}