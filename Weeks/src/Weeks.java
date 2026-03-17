import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AutocompleteSystem {

    // Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        Map<String, Integer> queryFrequency = new HashMap<>(); // queries passing through node
    }

    private TrieNode root;

    // Global frequency store
    private ConcurrentHashMap<String, Integer> globalFreq;

    public AutocompleteSystem() {
        root = new TrieNode();
        globalFreq = new ConcurrentHashMap<>();
    }

    // Insert or update query
    public void insert(String query) {
        globalFreq.put(query, globalFreq.getOrDefault(query, 0) + 1);

        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            // Update frequency at each prefix node
            node.queryFrequency.put(query, globalFreq.get(query));
        }
    }

    // Get top 10 suggestions for prefix
    public List<String> search(String prefix) {
        TrieNode node = root;

        // Traverse trie
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return Collections.emptyList();
            }
            node = node.children.get(c);
        }

        // Min Heap for top 10
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : node.queryFrequency.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }

        List<String> result = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            result.add(minHeap.poll().getKey());
        }

        Collections.reverse(result); // highest frequency first
        return result;
    }

    // Handle typo (simple edit distance = 1)
    public List<String> suggestWithTypo(String input) {
        List<String> suggestions = new ArrayList<>();

        for (String query : globalFreq.keySet()) {
            if (isOneEditAway(input, query)) {
                suggestions.add(query);
            }
        }

        return suggestions;
    }

    // Check edit distance = 1
    private boolean isOneEditAway(String s1, String s2) {
        if (Math.abs(s1.length() - s2.length()) > 1) return false;

        int i = 0, j = 0, edits = 0;

        while (i < s1.length() && j < s2.length()) {
            if (s1.charAt(i) != s2.charAt(j)) {
                edits++;
                if (edits > 1) return false;

                if (s1.length() > s2.length()) i++;
                else if (s1.length() < s2.length()) j++;
                else {
                    i++;
                    j++;
                }
            } else {
                i++;
                j++;
            }
        }

        return true;
    }

    // Demo
    public static void main(String[] args) {
        AutocompleteSystem system = new AutocompleteSystem();

        system.insert("java tutorial");
        system.insert("javascript");
        system.insert("java download");
        system.insert("java tutorial");
        system.insert("java 21 features");

        System.out.println("Search 'jav':");
        List<String> results = system.search("jav");
        for (String r : results) {
            System.out.println(r);
        }

        System.out.println("\nTypo suggestions for 'jvaa':");
        System.out.println(system.suggestWithTypo("jvaa"));
    }
}