import java.util.*;

public class PlagiarismDetector {

    // n-gram -> set of document IDs
    private Map<String, Set<String>> index;

    // documentId -> list of n-grams
    private Map<String, List<String>> documentMap;

    private int N = 5; // 5-grams (can change to 7)

    public PlagiarismDetector(int n) {
        this.N = n;
        this.index = new HashMap<>();
        this.documentMap = new HashMap<>();
    }

    // Add document to system
    public void addDocument(String docId, String content) {
        List<String> ngrams = generateNGrams(content);
        documentMap.put(docId, ngrams);

        for (String gram : ngrams) {
            index.computeIfAbsent(gram, k -> new HashSet<>()).add(docId);
        }
    }

    // Analyze a document for plagiarism
    public void analyzeDocument(String docId) {
        List<String> ngrams = documentMap.get(docId);

        if (ngrams == null) {
            System.out.println("Document not found");
            return;
        }

        Map<String, Integer> matchCount = new HashMap<>();

        // Count matches
        for (String gram : ngrams) {
            Set<String> docs = index.get(gram);
            if (docs != null) {
                for (String otherDoc : docs) {
                    if (!otherDoc.equals(docId)) {
                        matchCount.put(otherDoc,
                                matchCount.getOrDefault(otherDoc, 0) + 1);
                    }
                }
            }
        }

        // Calculate similarity
        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            String otherDoc = entry.getKey();
            int matches = entry.getValue();

            double similarity = (matches * 100.0) / ngrams.size();

            System.out.println("Compared with: " + otherDoc);
            System.out.println("Matching n-grams: " + matches);
            System.out.printf("Similarity: %.2f%%", similarity);

            if (similarity > 60) {
                System.out.println(" → PLAGIARISM DETECTED");
            } else if (similarity > 15) {
                System.out.println(" → Suspicious");
            } else {
                System.out.println(" → Safe");
            }
            System.out.println("-------------------------");
        }
    }

    // Generate n-grams
    private List<String> generateNGrams(String text) {
        List<String> result = new ArrayList<>();

        String[] words = text.toLowerCase().split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder gram = new StringBuilder();
            for (int j = 0; j < N; j++) {
                gram.append(words[i + j]).append(" ");
            }
            result.add(gram.toString().trim());
        }

        return result;
    }

    // Demo
    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector(5);

        String doc1 = "Machine learning is a method of data analysis that automates analytical model building";
        String doc2 = "Machine learning is a method of data analysis that automates model building process";
        String doc3 = "Cooking recipes require ingredients and proper steps to prepare food";

        detector.addDocument("essay_001", doc1);
        detector.addDocument("essay_002", doc2);
        detector.addDocument("essay_003", doc3);

        detector.analyzeDocument("essay_001");
    }
}