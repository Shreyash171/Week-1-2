import java.util.*;

public class TransactionAnalyzer {

    static class Transaction {
        int id;
        int amount;
        String merchant;
        String account;
        long time;

        Transaction(int id, int amount, String merchant, String account, long time) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.time = time;
        }
    }

    public List<int[]> findTwoSum(List<Transaction> transactions, int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, t.id});
            }
            map.put(t.amount, t);
        }
        return result;
    }

    public List<int[]> findTwoSumWithTimeWindow(List<Transaction> transactions, int target, long windowMs) {
        List<int[]> result = new ArrayList<>();
        transactions.sort(Comparator.comparingLong(t -> t.time));
        Map<Integer, List<Transaction>> map = new HashMap<>();

        int left = 0;

        for (int right = 0; right < transactions.size(); right++) {
            Transaction curr = transactions.get(right);

            while (curr.time - transactions.get(left).time > windowMs) {
                Transaction old = transactions.get(left);
                List<Transaction> list = map.get(old.amount);
                list.remove(old);
                if (list.isEmpty()) map.remove(old.amount);
                left++;
            }

            int complement = target - curr.amount;
            if (map.containsKey(complement)) {
                for (Transaction t : map.get(complement)) {
                    result.add(new int[]{t.id, curr.id});
                }
            }

            map.computeIfAbsent(curr.amount, k -> new ArrayList<>()).add(curr);
        }

        return result;
    }

    public List<List<Integer>> findKSum(List<Transaction> transactions, int k, int target) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(transactions, k, target, 0, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(List<Transaction> transactions, int k, int target, int start,
                           List<Integer> path, List<List<Integer>> result) {
        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(path));
            return;
        }
        if (k == 0 || target < 0) return;

        for (int i = start; i < transactions.size(); i++) {
            path.add(transactions.get(i).id);
            backtrack(transactions, k - 1, target - transactions.get(i).amount, i + 1, path, result);
            path.remove(path.size() - 1);
        }
    }

    public List<String> detectDuplicates(List<Transaction> transactions) {
        Map<String, Set<String>> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.amount + "|" + t.merchant;
            map.computeIfAbsent(key, k -> new HashSet<>()).add(t.account);
        }

        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.add(entry.getKey() + " -> " + entry.getValue());
            }
        }

        return result;
    }

    public static void main(String[] args) {
        TransactionAnalyzer ta = new TransactionAnalyzer();

        List<Transaction> transactions = Arrays.asList(
                new Transaction(1, 500, "StoreA", "acc1", 1000),
                new Transaction(2, 300, "StoreB", "acc2", 2000),
                new Transaction(3, 200, "StoreC", "acc3", 3000),
                new Transaction(4, 500, "StoreA", "acc2", 4000)
        );

        System.out.println("Two Sum:");
        for (int[] pair : ta.findTwoSum(transactions, 500)) {
            System.out.println(Arrays.toString(pair));
        }

        System.out.println("\nTwo Sum (Time Window):");
        for (int[] pair : ta.findTwoSumWithTimeWindow(transactions, 500, 3000)) {
            System.out.println(Arrays.toString(pair));
        }

        System.out.println("\nK Sum:");
        for (List<Integer> list : ta.findKSum(transactions, 3, 1000)) {
            System.out.println(list);
        }

        System.out.println("\nDuplicates:");
        for (String s : ta.detectDuplicates(transactions)) {
            System.out.println(s);
        }
    }
}