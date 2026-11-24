package gridcore.algorithm;

import gridcore.grid.WarehouseGrid;
import gridcore.model.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Implements Breadth-First Search (BFS) to compute the shortest obstacle-free
 * path between two positions on the warehouse grid.
 */
public class PathFinder {
    private final WarehouseGrid grid;

    public PathFinder(WarehouseGrid grid) {
        if (grid == null) {
            throw new IllegalArgumentException("WarehouseGrid cannot be null");
        }
        this.grid = grid;
    }

    /**
     * Calculates the shortest path from start to end using Breadth-First Search.
     *
     * @param start Starting position
     * @param end Destination position
     * @return List of positions forming the shortest path from start to end.
     *         Returns an empty list if no valid path exists or destination is unreachable.
     *         Returns a single-element list if start equals end.
     */
    public List<Position> findShortestPath(Position start, Position end) {
        List<Position> path = new ArrayList<>();

        // Validate boundary constraints and walkability
        if (start == null || end == null) {
            return path;
        }
        if (!grid.isWalkable(start) || !grid.isWalkable(end)) {
            return path;
        }

        // If start and destination are identical
        if (start.equals(end)) {
            path.add(start);
            return path;
        }

        Queue<Position> queue = new LinkedList<>();
        Set<Position> visited = new HashSet<>();
        Map<Position, Position> parentMap = new HashMap<>();

        queue.add(start);
        visited.add(start);

        boolean destinationReached = false;

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            if (current.equals(end)) {
                destinationReached = true;
                break;
            }

            for (Position neighbor : grid.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // If destination was not reached, return empty path
        if (!destinationReached) {
            return path;
        }

        // Reconstruct path by backtracking through parentMap
        Position curr = end;
        while (curr != null) {
            path.add(curr);
            curr = parentMap.get(curr);
        }

        Collections.reverse(path);
        return path;
    }

    /**
     * Returns the shortest distance in steps between two positions.
     * Returns -1 if unreachable.
     */
    public int getDistance(Position start, Position end) {
        List<Position> path = findShortestPath(start, end);
        if (path.isEmpty()) {
            return -1;
        }
        return path.size() - 1;
    }
}

