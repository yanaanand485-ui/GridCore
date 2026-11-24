package gridcore.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a customer order in the dark store warehouse.
 * Implements Comparable to allow PriorityQueue processing by order priority.
 */
public class Order implements Comparable<Order> {

    /**
     * Order priority levels: HIGH (1), MEDIUM (2), LOW (3).
     */
    public enum Priority {
        HIGH(1),
        MEDIUM(2),
        LOW(3);

        private final int level;

        Priority(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * Lifecycle statuses for an order.
     */
    public enum Status {
        PENDING,
        ASSIGNED,
        COMPLETED,
        FAILED
    }

    private final String orderId;
    private final List<Position> itemLocations;
    private final Priority priority;
    private Status status;
    private Picker assignedPicker;

    public Order(String orderId, List<Position> itemLocations, Priority priority) {
        this.orderId = orderId;
        this.itemLocations = itemLocations != null ? new ArrayList<>(itemLocations) : new ArrayList<>();
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.status = Status.PENDING;
        this.assignedPicker = null;
    }

    public String getOrderId() {
        return orderId;
    }

    public List<Position> getItemLocations() {
        return Collections.unmodifiableList(itemLocations);
    }

    public Priority getPriority() {
        return priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Picker getAssignedPicker() {
        return assignedPicker;
    }

    public void setAssignedPicker(Picker assignedPicker) {
        this.assignedPicker = assignedPicker;
    }

    /**
     * Orders are compared primarily by priority (HIGH first).
     * If priorities are identical, orderId is used as a tie-breaker.
     */
    @Override
    public int compareTo(Order other) {
        int cmp = Integer.compare(this.priority.getLevel(), other.priority.getLevel());
        if (cmp != 0) {
            return cmp;
        }
        return this.orderId.compareTo(other.orderId);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + orderId + '\'' +
                ", priority=" + priority +
                ", itemsCount=" + itemLocations.size() +
                ", status=" + status +
                ", picker=" + (assignedPicker != null ? assignedPicker.getId() : "None") +
                '}';
    }
}

