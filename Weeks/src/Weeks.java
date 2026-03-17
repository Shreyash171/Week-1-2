import java.util.*;
import java.util.concurrent.*;

public class DNSCache {

    // DNS Entry class
    static class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime;

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    // LRU Cache using LinkedHashMap
    private final Map<String, DNSEntry> cache;

    private final int capacity;

    // Stats
    private long hits = 0;
    private long misses = 0;
    private long totalLookupTime = 0;

    public DNSCache(int capacity) {
        this.capacity = capacity;

        this.cache = Collections.synchronizedMap(
                new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
                    protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                        return size() > DNSCache.this.capacity;
                    }
                }
        );

        // Start cleanup thread
        startCleanupThread();
    }

    // Resolve domain
    public String resolve(String domain) {
        long start = System.nanoTime();

        DNSEntry entry;

        synchronized (cache) {
            entry = cache.get(domain);

            if (entry != null && !entry.isExpired()) {
                hits++;
                totalLookupTime += (System.nanoTime() - start);
                return "Cache HIT → " + entry.ipAddress;
            }

            // Remove expired entry if exists
            if (entry != null && entry.isExpired()) {
                cache.remove(domain);
            }
        }

        // Cache MISS → query upstream
        misses++;
        String ip = queryUpstreamDNS(domain);

        // Simulated TTL (e.g., 5 seconds)
        DNSEntry newEntry = new DNSEntry(domain, ip, 5);

        synchronized (cache) {
            cache.put(domain, newEntry);
        }

        totalLookupTime += (System.nanoTime() - start);

        return "Cache MISS → " + ip;
    }

    // Simulate upstream DNS query
    private String queryUpstreamDNS(String domain) {
        try {
            Thread.sleep(100); // simulate latency (100ms)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Fake IP generator
        return "192.168." + new Random().nextInt(255) + "." + new Random().nextInt(255);
    }

    // Cleanup expired entries periodically
    private void startCleanupThread() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            synchronized (cache) {
                Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
                while (it.hasNext()) {
                    if (it.next().getValue().isExpired()) {
                        it.remove();
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    // Cache stats
    public String getCacheStats() {
        long total = hits + misses;
        double hitRate = total == 0 ? 0 : (hits * 100.0) / total;
        double avgTimeMs = total == 0 ? 0 : (totalLookupTime / 1_000_000.0) / total;

        return String.format(
                "Hit Rate: %.2f%%, Avg Lookup Time: %.2f ms (Hits=%d, Misses=%d)",
                hitRate, avgTimeMs, hits, misses
        );
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        DNSCache dnsCache = new DNSCache(3);

        System.out.println(dnsCache.resolve("google.com")); // MISS
        System.out.println(dnsCache.resolve("google.com")); // HIT

        Thread.sleep(6000); // wait for TTL expiry

        System.out.println(dnsCache.resolve("google.com")); // EXPIRED → MISS

        System.out.println(dnsCache.getCacheStats());
    }
}