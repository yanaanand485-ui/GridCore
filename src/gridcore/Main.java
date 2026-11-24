package gridcore;

import gridcore.algorithm.PathFinder;
import gridcore.dispatcher.TaskDispatcher;
import gridcore.grid.WarehouseGrid;
import gridcore.model.Order;
import gridcore.model.Picker;
import gridcore.model.Position;
import gridcore.util.AsciiMapPrinter;
import gridcore.worker.PickerManager;

import java.util.List;

/**
 * Main application entry point for GridCore.
 * Initializes the 7x7 warehouse grid, mobile pickers, and sample orders,
 * then triggers the prioritized multi-agent task dispatcher.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   GRIDCORE – SPATIAL GRID & MULTI-AGENT TASK DISPATCHER         ");
        System.out.println("==================================================================");

        // 1. Initialize a 7x7 Warehouse Grid with Obstacles
        WarehouseGrid grid = new WarehouseGrid(7, 7);

        // Configure 2x2 rack obstacle blocks
        // Block 1 (Top-Left)
        grid.addObstacle(1, 1);
        grid.addObstacle(1, 2);
        grid.addObstacle(2, 1);
        grid.addObstacle(2, 2);

        // Block 2 (Top-Right)
        grid.addObstacle(1, 4);
        grid.addObstacle(1, 5);
        grid.addObstacle(2, 4);
        grid.addObstacle(2, 5);

        // Block 3 (Bottom-Left)
        grid.addObstacle(4, 1);
        grid.addObstacle(4, 2);
        grid.addObstacle(5, 1);
        grid.addObstacle(5, 2);

        // Block 4 (Bottom-Right)
        grid.addObstacle(4, 4);
        grid.addObstacle(4, 5);
        grid.addObstacle(5, 4);
        grid.addObstacle(5, 5);

        // Register 5 item bin locations in walkable aisles
        Position bin1 = new Position(0, 3);
        Position bin2 = new Position(1, 3);
        Position bin3 = new Position(3, 1);
        Position bin4 = new Position(3, 5);
        Position bin5 = new Position(6, 3);

        grid.addItemBin(bin1);
        grid.addItemBin(bin2);
        grid.addItemBin(bin3);
        grid.addItemBin(bin4);
        grid.addItemBin(bin5);

        // 2. Initialize Pickers with different starting positions and initial states
        PickerManager pickerManager = new PickerManager();
        Picker p1 = new Picker("PK-01", new Position(0, 0), Picker.State.IDLE);
        Picker p2 = new Picker("PK-02", new Position(3, 3), Picker.State.IDLE);
        Picker p3 = new Picker("PK-03", new Position(6, 6), Picker.State.PACKING); // Initially busy

        pickerManager.addPicker(p1);
        pickerManager.addPicker(p2);
        pickerManager.addPicker(p3);

        // 3. Initialize Helpers & Task Dispatcher
        PathFinder pathFinder = new PathFinder(grid);
        AsciiMapPrinter printer = new AsciiMapPrinter();
        TaskDispatcher dispatcher = new TaskDispatcher(grid, pickerManager, pathFinder, printer);

        // 4. Display Initial Warehouse Map & Picker States
        printer.printGrid(grid, pickerManager.getPickers());

        System.out.println("\nInitial Pickers Status:");
        System.out.println("------------------------------------------------------------------");
        System.out.printf("%-10s | %-15s | %-12s | %-12s%n", "PICKER ID", "POSITION", "STATE", "AVAILABLE?");
        System.out.println("------------------------------------------------------------------");
        for (Picker p : pickerManager.getPickers()) {
            System.out.printf("%-10s | %-15s | %-12s | %-12s%n",
                    p.getId(), p.getPosition(), p.getState(), p.isAvailable() ? "YES" : "NO");
        }
        System.out.println("------------------------------------------------------------------");

        // 5. Create Sample Orders (Each containing 5 item positions, with different priorities)
        Order orderLow = new Order("ORD-101",
                List.of(bin1, bin2, bin3, bin4, bin5),
                Order.Priority.LOW);

        Order orderHigh = new Order("ORD-102",
                List.of(bin5, bin4, bin3, bin2, bin1),
                Order.Priority.HIGH);

        Order orderMed = new Order("ORD-103",
                List.of(bin3, bin1, bin2, bin5, bin4),
                Order.Priority.MEDIUM);

        System.out.println("\nEnqueuing Sample Orders into PriorityQueue:");
        dispatcher.addOrder(orderLow);  // Enqueued 1st (LOW priority)
        dispatcher.addOrder(orderHigh); // Enqueued 2nd (HIGH priority - will be dispatched first!)
        dispatcher.addOrder(orderMed);  // Enqueued 3rd (MEDIUM priority)

        // 6. Execute Task Dispatching Workflow (PriorityQueue will dispatch HIGH -> MEDIUM -> LOW)
        dispatcher.dispatchAll();

        // 7. Display Final Pickers State
        System.out.println("\nFinal Pickers Status:");
        System.out.println("------------------------------------------------------------------");
        System.out.printf("%-10s | %-15s | %-12s%n", "PICKER ID", "FINAL POSITION", "FINAL STATE");
        System.out.println("------------------------------------------------------------------");
        for (Picker p : pickerManager.getPickers()) {
            System.out.printf("%-10s | %-15s | %-12s%n",
                    p.getId(), p.getPosition(), p.getState());
        }
        System.out.println("------------------------------------------------------------------");
        System.out.println("\nGridCore execution completed successfully.");
    }
}
