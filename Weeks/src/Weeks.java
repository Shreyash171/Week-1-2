import java.util.*;

public class ParkingLot {

    enum Status { EMPTY, OCCUPIED, DELETED }

    static class Slot {
        String license;
        long entryTime;
        Status status;

        Slot() {
            status = Status.EMPTY;
        }
    }

    private Slot[] table;
    private int capacity;
    private int size;
    private int totalProbes;
    private List<Integer> hourlyCount;

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        this.table = new Slot[capacity];
        for (int i = 0; i < capacity; i++) table[i] = new Slot();
        this.size = 0;
        this.totalProbes = 0;
        this.hourlyCount = new ArrayList<>(Collections.nCopies(24, 0));
    }

    private int hash(String license) {
        return Math.abs(license.hashCode()) % capacity;
    }

    public String parkVehicle(String license) {
        int index = hash(license);
        int probes = 0;

        while (table[index].status == Status.OCCUPIED) {
            index = (index + 1) % capacity;
            probes++;
        }

        table[index].license = license;
        table[index].entryTime = System.currentTimeMillis();
        table[index].status = Status.OCCUPIED;

        size++;
        totalProbes += probes;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        hourlyCount.set(hour, hourlyCount.get(hour) + 1);

        return "Assigned spot #" + index + " (" + probes + " probes)";
    }

    public String exitVehicle(String license) {
        int index = hash(license);
        int probes = 0;

        while (table[index].status != Status.EMPTY) {
            if (table[index].status == Status.OCCUPIED &&
                    table[index].license.equals(license)) {

                long durationMs = System.currentTimeMillis() - table[index].entryTime;
                double hours = durationMs / (1000.0 * 60 * 60);
                double fee = Math.ceil(hours) * 5;

                table[index].status = Status.DELETED;
                table[index].license = null;

                size--;

                return "Freed spot #" + index + ", Duration: " +
                        String.format("%.2f", hours) + "h, Fee: $" + fee;
            }

            index = (index + 1) % capacity;
            probes++;
        }

        return "Vehicle not found";
    }

    public String getStatistics() {
        double occupancy = (size * 100.0) / capacity;
        double avgProbes = size == 0 ? 0 : (totalProbes * 1.0 / size);

        int peakHour = 0;
        int max = 0;
        for (int i = 0; i < 24; i++) {
            if (hourlyCount.get(i) > max) {
                max = hourlyCount.get(i);
                peakHour = i;
            }
        }

        return "Occupancy: " + String.format("%.2f", occupancy) + "%" +
                ", Avg Probes: " + String.format("%.2f", avgProbes) +
                ", Peak Hour: " + peakHour + "-" + (peakHour + 1);
    }

    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(500);

        System.out.println(lot.parkVehicle("ABC-1234"));
        System.out.println(lot.parkVehicle("ABC-1235"));
        System.out.println(lot.parkVehicle("XYZ-9999"));

        Thread.sleep(2000);

        System.out.println(lot.exitVehicle("ABC-1234"));
        System.out.println(lot.getStatistics());
    }
}