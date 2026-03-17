import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter {

    // Token Bucket class
    static class TokenBucket {
        private final int maxTokens;
        private final double refillRatePerSec; // tokens per second

        private double tokens;
        private long lastRefillTime;

        public TokenBucket(int maxTokens, int refillPerHour) {
            this.maxTokens = maxTokens;
            this.refillRatePerSec = refillPerHour / 3600.0;
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }

        // Refill tokens based on elapsed time
        private synchronized void refill() {
            long now = System.currentTimeMillis();
            double elapsedSeconds = (now - lastRefillTime) / 1000.0;

            double tokensToAdd = elapsedSeconds * refillRatePerSec;
            tokens = Math.min(maxTokens, tokens + tokensToAdd);

            lastRefillTime = now;
        }

        // Try consuming a token
        public synchronized boolean allowRequest() {
            refill();

            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        public synchronized int getRemainingTokens() {
            refill();
            return (int) tokens;
        }

        public synchronized long getRetryAfterSeconds() {
            if (tokens >= 1) return 0;

            double tokensNeeded = 1 - tokens;
            return (long) Math.ceil(tokensNeeded / refillRatePerSec);
        }
    }

    // clientId -> TokenBucket
    private ConcurrentHashMap<String, TokenBucket> clientBuckets;

    private final int MAX_TOKENS = 1000; // per hour

    public RateLimiter() {
        clientBuckets = new ConcurrentHashMap<>();
    }

    private TokenBucket getBucket(String clientId) {
        return clientBuckets.computeIfAbsent(
                clientId,
                k -> new TokenBucket(MAX_TOKENS, MAX_TOKENS)
        );
    }

    // Check rate limit (O(1))
    public String checkRateLimit(String clientId) {
        TokenBucket bucket = getBucket(clientId);

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            long retry = bucket.getRetryAfterSeconds();
            return "Denied (0 remaining, retry after " + retry + " sec)";
        }
    }

    // Status API
    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = getBucket(clientId);

        int remaining = bucket.getRemainingTokens();
        int used = MAX_TOKENS - remaining;

        long resetTime = System.currentTimeMillis() / 1000 + bucket.getRetryAfterSeconds();

        return "{used: " + used +
                ", limit: " + MAX_TOKENS +
                ", remaining: " + remaining +
                ", reset: " + resetTime + "}";
    }

    // Demo
    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter();

        String client = "abc123";

        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit(client));
        }

        // Simulate hitting limit
        for (int i = 0; i < 1000; i++) {
            limiter.checkRateLimit(client);
        }

        System.out.println(limiter.checkRateLimit(client)); // should deny
        System.out.println(limiter.getRateLimitStatus(client));
    }
}