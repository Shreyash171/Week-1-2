import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleInventoryManager {

    // productId -> stock count
    private ConcurrentHashMap<String, AtomicInteger> stockMap;

    // productId -> waiting list (FIFO)
    private ConcurrentHashMap<String, Queue<Integer>> waitingListMap;

    public FlashSaleInventoryManager() {
        stockMap = new ConcurrentHashMap<>();
        waitingListMap = new ConcurrentHashMap<>();
    }

    // Add product with stock
    public void addProduct(String productId, int stock) {
        stockMap.put(productId, new AtomicInteger(stock));
        waitingListMap.put(productId, new LinkedList<>());
    }

    // Check stock (O(1))
    public String checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        return stock.get() + " units available";
    }

    // Purchase item (thread-safe)
    public synchronized String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        // If stock available
        if (stock.get() > 0) {
            int remaining = stock.decrementAndGet();
            return "Success! Remaining stock: " + remaining;
        }

        // Add to waiting list
        Queue<Integer> queue = waitingListMap.get(productId);
        queue.offer(userId);

        return "Out of stock. Added to waiting list. Position: " + queue.size();
    }

    // Process restock and allocate to waiting users
    public synchronized void restock(String productId, int quantity) {
        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) {
            return;
        }

        stock.addAndGet(quantity);

        Queue<Integer> queue = waitingListMap.get(productId);

        // Fulfill waiting list
        while (stock.get() > 0 && !queue.isEmpty()) {
            int userId = queue.poll();
            stock.decrementAndGet();
            System.out.println("Allocated product to waiting user: " + userId);
        }
    }

    // Get waiting list size
    public int getWaitingListSize(String productId) {
        Queue<Integer> queue = waitingListMap.get(productId);
        return queue != null ? queue.size() : 0;
    }

    // Demo
    public static void main(String[] args) {
        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

        String product = "IPHONE15_256GB";

        manager.addProduct(product, 3);

        System.out.println(manager.checkStock(product));

        System.out.println(manager.purchaseItem(product, 101));
        System.out.println(manager.purchaseItem(product, 102));
        System.out.println(manager.purchaseItem(product, 103));

        // Now stock is 0
        System.out.println(manager.purchaseItem(product, 104));
        System.out.println(manager.purchaseItem(product, 105));

        System.out.println("Waiting list size: " + manager.getWaitingListSize(product));

        // Restock
        manager.restock(product, 2);

        System.out.println("Final stock: " + manager.checkStock(product));
    }
}
