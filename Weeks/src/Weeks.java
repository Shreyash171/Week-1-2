import java.util.*;

public class MultiLevelCache {

    static class VideoData {
        String videoId;
        String content;
        VideoData(String id, String content) {
            this.videoId = id;
            this.content = content;
        }
    }

    private final int L1_CAPACITY = 10000;
    private final int L2_CAPACITY = 100000;
    private LinkedHashMap<String, VideoData> L1;
    private LinkedHashMap<String, VideoData> L2;
    private Map<String, VideoData> L3;
    private Map<String, Integer> accessCount;
    private int L1Hits, L2Hits, L3Hits, totalRequests;

    public MultiLevelCache() {
        L1 = new LinkedHashMap<>(L1_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                return size() > L1_CAPACITY;
            }
        };
        L2 = new LinkedHashMap<>(L2_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                return size() > L2_CAPACITY;
            }
        };
        L3 = new HashMap<>();
        accessCount = new HashMap<>();
    }

    public void addToDatabase(VideoData video) {
        L3.put(video.videoId, video);
    }

    public VideoData getVideo(String videoId) {
        totalRequests++;
        if (L1.containsKey(videoId)) {
            L1Hits++;
            return L1.get(videoId);
        }
        if (L2.containsKey(videoId)) {
            L2Hits++;
            accessCount.put(videoId, accessCount.getOrDefault(videoId,0)+1);
            if (accessCount.get(videoId) >= 3) L1.put(videoId, L2.get(videoId));
            return L2.get(videoId);
        }
        if (L3.containsKey(videoId)) {
            L3Hits++;
            VideoData v = L3.get(videoId);
            L2.put(videoId, v);
            accessCount.put(videoId,1);
            return v;
        }
        return null;
    }

    public void invalidate(String videoId) {
        L1.remove(videoId);
        L2.remove(videoId);
        L3.remove(videoId);
        accessCount.remove(videoId);
    }

    public void getStatistics() {
        System.out.println("L1 Hit Rate: " + (L1Hits*100.0/totalRequests) + "%");
        System.out.println("L2 Hit Rate: " + (L2Hits*100.0/totalRequests) + "%");
        System.out.println("L3 Hit Rate: " + (L3Hits*100.0/totalRequests) + "%");
        double overallHit = (L1Hits+L2Hits+L3Hits)*100.0/totalRequests;
        System.out.println("Overall Hit Rate: " + overallHit + "%");
    }

    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache();

        for(int i=1;i<=5;i++) cache.addToDatabase(new VideoData("video_"+i,"content_"+i));

        System.out.println(cache.getVideo("video_1").content);
        System.out.println(cache.getVideo("video_1").content);
        System.out.println(cache.getVideo("video_3").content);
        System.out.println(cache.getVideo("video_6"));

        cache.getStatistics();
    }
}