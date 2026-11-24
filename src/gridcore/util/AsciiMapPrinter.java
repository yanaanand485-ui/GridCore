package gridcore.util;

import gridcore.grid.WarehouseGrid;
import gridcore.model.Picker;
import gridcore.model.Position;

import java.util.List;

/**
 * Utility class for displaying ASCII terminal maps of the warehouse,
 * including obstacles, item bins, pickers, and planned traversal routes.
 */
public class AsciiMapPrinter {

    /**
     * Displays the static warehouse grid with registered item bins and active pickers.
     */
    public void printGrid(WarehouseGrid grid, List<Picker> pickers) {
        char[][] display = buildBaseGrid(grid);

        // Overlay item bins ('I')
        for (Position bin : grid.getItemBins()) {
            if (grid.isValid(bin)) {
                display[bin.getRow()][bin.getCol()] = 'I';
            }
        }

        // Overlay pickers ('P')
        if (pickers != null) {
            for (Picker picker : pickers) {
                Position pos = picker.getPosition();
                if (grid.isValid(pos)) {
                    display[pos.getRow()][pos.getCol()] = 'P';
                }
            }
        }

        render(display, "WAREHOUSE GRID LAYOUT");
        System.out.println("Legend: [.] Aisle  [#] Rack/Obstacle  [I] Item Bin  [P] Picker");
    }

    /**
     * Displays the warehouse grid with a planned route overlay.
     */
    public void printRoute(WarehouseGrid grid, List<Position> route, Position start, Position end, List<Picker> pickers) {
        char[][] display = buildBaseGrid(grid);

        // Overlay route path ('*')
        if (route != null) {
            for (Position p : route) {
                if (grid.isValid(p)) {
                    display[p.getRow()][p.getCol()] = '*';
                }
            }
        }

        // Overlay item bins ('I') - keeps item stops visible along the route
        for (Position bin : grid.getItemBins()) {
            if (grid.isValid(bin)) {
                display[bin.getRow()][bin.getCol()] = 'I';
            }
        }

        // Overlay start ('S') and destination ('E')
        if (start != null && grid.isValid(start)) {
            display[start.getRow()][start.getCol()] = 'S';
        }
        if (end != null && grid.isValid(end)) {
            display[end.getRow()][end.getCol()] = 'E';
        }

        render(display, "PLANNED ROUTE VISUALIZATION");
        System.out.println("Route Legend: [S] Start  [E] End Destination  [*] Traversal Step  [I] Item Bin  [#] Rack");
    }

    private char[][] buildBaseGrid(WarehouseGrid grid) {
        int rows = grid.getRows();
        int cols = grid.getCols();
        char[][] display = new char[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                display[r][c] = grid.getCell(r, c);
            }
        }
        return display;
    }

    private void render(char[][] display, String title) {
        int rows = display.length;
        int cols = display[0].length;

        System.out.println();
        System.out.println("================== " + title + " ==================");
        System.out.print("     ");
        for (int c = 0; c < cols; c++) {
            System.out.print("C" + c + " ");
        }
        System.out.println();
        System.out.print("   +");
        for (int c = 0; c < cols; c++) {
            System.out.print("---");
        }
        System.out.println("+");

        for (int r = 0; r < rows; r++) {
            System.out.printf("R%d | ", r);
            for (int c = 0; c < cols; c++) {
                System.out.print(" " + display[r][c] + " ");
            }
            System.out.println("|");
        }

        System.out.print("   +");
        for (int c = 0; c < cols; c++) {
            System.out.print("---");
        }
        System.out.println("+");
    }
}
