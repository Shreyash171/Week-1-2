import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UsernameChecker
{

    // username -> userId
    private ConcurrentHashMap<String, Integer> userMap;

    // username -> attempt frequency
    private ConcurrentHashMap<String, Integer> attemptMap;

    public UsernameChecker() {
        userMap = new ConcurrentHashMap<>();
        attemptMap = new ConcurrentHashMap<>();
    }

    // Register a username
    public void registerUser(String username, int userId) {
        userMap.put(username.toLowerCase(), userId);
    }

    // Check availability (O(1))
    public boolean checkAvailability(String username) {
        username = username.toLowerCase();

        // Track attempts
        attemptMap.put(username, attemptMap.getOrDefault(username, 0) + 1);

        return !userMap.containsKey(username);
    }

    // Suggest alternatives
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        username = username.toLowerCase();

        // Try appending numbers
        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;
            if (!userMap.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        // Replace '_' with '.'
        String modified = username.replace('_', '.');
        if (!userMap.containsKey(modified)) {
            suggestions.add(modified);
        }

        // Add random suffix
        String randomSuffix = username + (new Random().nextInt(1000));
        if (!userMap.containsKey(randomSuffix)) {
            suggestions.add(randomSuffix);
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {
        String mostAttempted = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : attemptMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostAttempted = entry.getKey();
            }
        }

        return mostAttempted + " (" + maxCount + " attempts)";
    }

    // Demo
    public static void main(String[] args) {
        UsernameChecker checker = new UsernameChecker();

        // Existing users
        checker.registerUser("john_doe", 1);
        checker.registerUser("admin", 2);

        // Availability checks
        System.out.println("john_doe available? " + checker.checkAvailability("john_doe"));
        System.out.println("jane_smith available? " + checker.checkAvailability("jane_smith"));

        // Suggestions
        System.out.println("Suggestions for john_doe: " + checker.suggestAlternatives("john_doe"));

        // Simulate multiple attempts
        for (int i = 0; i < 100; i++) {
            checker.checkAvailability("admin");
        }

        System.out.println("Most attempted: " + checker.getMostAttempted());
    }
}