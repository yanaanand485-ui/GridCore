package gridcore.worker;

import gridcore.algorithm.PathFinder;
import gridcore.model.Picker;
import gridcore.model.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the pool of warehouse pickers and allocates orders to the nearest idle picker.
 */
public class PickerManager {
    private final List<Picker> pickers;

    public PickerManager() {
        this.pickers = new ArrayList<>();
    }

    public void addPicker(Picker picker) {
        if (picker != null && !pickers.contains(picker)) {
            pickers.add(picker);
        }
    }

    public List<Picker> getPickers() {
        return Collections.unmodifiableList(pickers);
    }

    /**
     * Returns all pickers that are currently IDLE.
     */
    public List<Picker> getAvailablePickers() {
        List<Picker> available = new ArrayList<>();
        for (Picker p : pickers) {
            if (p.isAvailable()) {
                available.add(p);
            }
        }
        return available;
    }

    /**
     * Finds the nearest IDLE picker to the given target position using BFS distance.
     * Resolves ties deterministically by comparing picker IDs.
     *
     * @param target Destination coordinate (e.g. first item location)
     * @param pathFinder PathFinder instance for distance calculation
     * @return Nearest available Picker, or null if none are available or reachable
     */
    public Picker findNearestIdlePicker(Position target, PathFinder pathFinder) {
        if (target == null || pathFinder == null) {
            return null;
        }

        List<Picker> idlePickers = getAvailablePickers();
        if (idlePickers.isEmpty()) {
            return null;
        }

        Picker bestPicker = null;
        int minDistance = Integer.MAX_VALUE;

        for (Picker picker : idlePickers) {
            int dist = pathFinder.getDistance(picker.getPosition(), target);

            // Ignore unreachable pickers
            if (dist == -1) {
                continue;
            }

            if (dist < minDistance) {
                minDistance = dist;
                bestPicker = picker;
            } else if (dist == minDistance && bestPicker != null) {
                // Tie-breaker by picker ID alphabetical order
                if (picker.getId().compareTo(bestPicker.getId()) < 0) {
                    bestPicker = picker;
                }
            }
        }

        return bestPicker;
    }

    public void assignPicker(Picker picker) {
        if (picker != null) {
            picker.setState(Picker.State.PICKING);
        }
    }

    public void releasePicker(Picker picker) {
        if (picker != null) {
            picker.setState(Picker.State.IDLE);
        }
    }
}

