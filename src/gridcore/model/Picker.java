package gridcore.model;

/**
 * Represents a warehouse picker agent responsible for collecting order items.
 */
public class Picker {

    /**
     * Operational states of a picker.
     */
    public enum State {
        IDLE,
        PICKING,
        PACKING
    }

    private final String id;
    private Position position;
    private State state;

    public Picker(String id, Position position) {
        this(id, position, State.IDLE);
    }

    public Picker(String id, Position position, State state) {
        this.id = id;
        this.position = position;
        this.state = state != null ? state : State.IDLE;
    }

    public String getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    /**
     * Checks if the picker is ready to accept a new task.
     */
    public boolean isAvailable() {
        return this.state == State.IDLE;
    }

    @Override
    public String toString() {
        return "Picker{" +
                "id='" + id + '\'' +
                ", position=" + position +
                ", state=" + state +
                '}';
    }
}

