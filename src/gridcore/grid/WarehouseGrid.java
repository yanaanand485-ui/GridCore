package gridcore.grid;

import gridcore.model.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the 2D warehouse grid layout with obstacle racks,
 * walkable aisles, and registered item storage bins.
 */
public class WarehouseGrid {
    public static final char OBSTACLE = '#';
    public static final char WALKABLE = '.';

    private final int rows;
    private final int cols;
    private final char[][] grid;
    private final List<Position> itemBins;

    /**
     * Creates a warehouse grid with the given dimensions.
     * All cells are initialized as walkable by default.
     */
    public WarehouseGrid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new char[rows][cols];
        this.itemBins = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = WALKABLE;
            }
        }
    }

    /**
     * Places an obstacle (rack) at the specified position.
     */
    public void addObstacle(int row, int col) {
        if (isValid(row, col)) {
            grid[row][col] = OBSTACLE;
        }
    }

    /**
     * Registers an item bin coordinate.
     */
    public void addItemBin(Position pos) {
        if (isValid(pos) && isWalkable(pos) && !itemBins.contains(pos)) {
            itemBins.add(pos);
        }
    }

    public List<Position> getItemBins() {
        return Collections.unmodifiableList(itemBins);
    }

    /**
     * Checks if coordinates fall within the grid boundaries.
     */
    public boolean isValid(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public boolean isValid(Position pos) {
        return pos != null && isValid(pos.getRow(), pos.getCol());
    }

    /**
     * Checks if a cell is valid and not blocked by an obstacle.
     */
    public boolean isWalkable(int row, int col) {
        return isValid(row, col) && grid[row][col] == WALKABLE;
    }

    public boolean isWalkable(Position pos) {
        return pos != null && isWalkable(pos.getRow(), pos.getCol());
    }

    public char getCell(int row, int col) {
        if (!isValid(row, col)) {
            throw new IllegalArgumentException("Position out of bounds: (" + row + ", " + col + ")");
        }
        return grid[row][col];
    }

    /**
     * Returns valid orthogonal neighbors (Up, Down, Left, Right) that are walkable.
     */
    public List<Position> getNeighbors(Position pos) {
        List<Position> neighbors = new ArrayList<>();
        if (pos == null) return neighbors;

        int[][] directions = {
            {-1, 0}, // Up
            {1, 0},  // Down
            {0, -1}, // Left
            {0, 1}   // Right
        };

        for (int[] dir : directions) {
            int newRow = pos.getRow() + dir[0];
            int newCol = pos.getCol() + dir[1];
            if (isWalkable(newRow, newCol)) {
                neighbors.add(new Position(newRow, newCol));
            }
        }

        return neighbors;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}

