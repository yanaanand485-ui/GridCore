package gridcore.dispatcher;

import gridcore.algorithm.PathFinder;
import gridcore.grid.WarehouseGrid;
import gridcore.model.Order;
import gridcore.model.Picker;
import gridcore.model.Position;
import gridcore.util.AsciiMapPrinter;
import gridcore.worker.PickerManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Manages order processing, picker assignment, multi-stop greedy route calculation,
 * and picking/packing workflow tracking.
 */
public class TaskDispatcher {
    public static final int STEP_DURATION_SECONDS = 30;     // 30 seconds per grid step
    public static final int TARGET_TIME_SECONDS = 600;       // 10-minute SLA window

    private final WarehouseGrid grid;
    private final PickerManager pickerManager;
    private final PathFinder pathFinder;
    private final AsciiMapPrinter printer;

    // High-priority orders are processed first via PriorityQueue
    private final PriorityQueue<Order> orderQueue;

    public TaskDispatcher(WarehouseGrid grid,
                          PickerManager pickerManager,
                          PathFinder pathFinder,
                          AsciiMapPrinter printer) {
        this.grid = grid;
        this.pickerManager = pickerManager;
        this.pathFinder = pathFinder;
        this.printer = printer;
        this.orderQueue = new PriorityQueue<>();
    }

    /**
     * Enqueues an incoming order into the PriorityQueue.
     */
    public void addOrder(Order order) {
        if (order != null) {
            orderQueue.offer(order);
            System.out.println("[Dispatcher] Enqueued: Order " + order.getOrderId() +
                    " [Priority: " + order.getPriority() + "] with " +
                    order.getItemLocations().size() + " items.");
        }
    }

    public boolean hasPendingOrders() {
        return !orderQueue.isEmpty();
    }

    public int getPendingOrderCount() {
        return orderQueue.size();
    }

    /**
     * Returns an unmodifiable snapshot of currently pending orders.
     */
    public List<Order> getPendingOrders() {
        List<Order> list = new ArrayList<>(orderQueue);
        Collections.sort(list); // Sort by priority
        return Collections.unmodifiableList(list);
    }

    /**
     * Dispatches the next single highest-priority order from the PriorityQueue.
     */
    public boolean dispatchNext() {
        if (orderQueue.isEmpty()) {
            System.out.println("[Dispatcher] No pending orders in queue.");
            return false;
        }

        Order order = orderQueue.poll();
        System.out.println("\n------------------------------------------------------------------");
        System.out.printf("Dispatching Next Order: %s [Priority: %s]%n",
                order.getOrderId(), order.getPriority());
        System.out.println("------------------------------------------------------------------");

        return processOrder(order);
    }

    /**
     * Dispatches all queued orders sequentially in priority order.
     */
    public void dispatchAll() {
        if (orderQueue.isEmpty()) {
            System.out.println("[Dispatcher] No pending orders in queue to dispatch.");
            return;
        }

        System.out.println("\n>>> STARTING BATCH ORDER DISPATCHING WORKFLOW...");
        int count = 0;

        while (!orderQueue.isEmpty()) {
            Order order = orderQueue.poll();
            count++;
            System.out.println("\n==================================================================");
            System.out.printf("Processing Queue Task #%d: Order %s [Priority: %s]%n",
                    count, order.getOrderId(), order.getPriority());
            System.out.println("==================================================================");

            processOrder(order);
        }

        System.out.println("\n>>> ALL QUEUED ORDERS HAVE BEEN COMPLETED.");
    }

    /**
     * Coordinates the fulfillment of an order.
     */
    public boolean processOrder(Order order) {
        if (order == null || order.getItemLocations().isEmpty()) {
            System.err.println("[Dispatcher] Error: Cannot process empty or null order.");
            return false;
        }

        Position firstItem = order.getItemLocations().get(0);

        // 1. Assign the nearest available idle picker
        Picker assignedPicker = pickerManager.findNearestIdlePicker(firstItem, pathFinder);

        if (assignedPicker == null) {
            System.out.println("[Dispatcher] Notice: No IDLE picker currently available for Order " +
                    order.getOrderId() + ". Keeping order in PENDING state.");
            order.setStatus(Order.Status.PENDING);
            orderQueue.offer(order); // Re-queue
            return false;
        }

        order.setAssignedPicker(assignedPicker);
        order.setStatus(Order.Status.ASSIGNED);
        pickerManager.assignPicker(assignedPicker);

        System.out.println(">> Picker Assigned: " + assignedPicker.getId() +
                " at position " + assignedPicker.getPosition());
        System.out.println(">> Initial Target Item: " + firstItem);

        // 2. Plan collection tour using Greedy Nearest-Next-Item Heuristic
        List<RouteLeg> routeLegs = planGreedyRouteLegs(assignedPicker.getPosition(), order.getItemLocations());

        if (routeLegs.isEmpty()) {
            System.err.println("[Dispatcher] Critical: Route planning failed due to unreachable item(s).");
            order.setStatus(Order.Status.FAILED);
            pickerManager.releasePicker(assignedPicker);
            return false;
        }

        // Assemble full stitched path without duplicate connecting points
        List<Position> completeRoute = new ArrayList<>();
        completeRoute.add(assignedPicker.getPosition());
        for (RouteLeg leg : routeLegs) {
            List<Position> legPath = leg.getPath();
            if (legPath.size() > 1) {
                for (int i = 1; i < legPath.size(); i++) {
                    completeRoute.add(legPath.get(i));
                }
            }
        }

        // 3. Print detailed multi-stop tour breakdown
        System.out.println("\n----------------- GREEDY MULTI-STOP ROUTE BREAKDOWN -----------------");
        int legIndex = 1;
        int totalMovementSteps = 0;
        for (RouteLeg leg : routeLegs) {
            System.out.printf("  [Leg %d/5] From %s -> Target Item at %s (%d steps)%n",
                    legIndex, leg.getFrom(), leg.getTo(), leg.getSteps());
            System.out.println("             Path: " + formatPath(leg.getPath()));
            totalMovementSteps += leg.getSteps();
            legIndex++;
        }
        System.out.println("  Total Movement Steps across all 5 items: " + totalMovementSteps + " steps");
        System.out.println("--------------------------------------------------------------------");

        // 4. Render ASCII route visualization
        Position startPos = assignedPicker.getPosition();
        Position finalDestination = completeRoute.get(completeRoute.size() - 1);
        printer.printRoute(grid, completeRoute, startPos, finalDestination, pickerManager.getPickers());

        // 5. Travel-time calculation & 10-minute SLA verification
        int estimatedTimeSeconds = totalMovementSteps * STEP_DURATION_SECONDS;
        boolean meetsTarget = estimatedTimeSeconds <= TARGET_TIME_SECONDS;

        System.out.println("\n----------------- ROUTE EXECUTION & TIMING REPORT -----------------");
        System.out.println("Order ID               : " + order.getOrderId());
        System.out.println("Priority               : " + order.getPriority());
        System.out.println("Assigned Picker        : " + assignedPicker.getId());
        System.out.println("Total Movement Steps   : " + totalMovementSteps + " steps");
        System.out.println("Travel Time Formula    : " + totalMovementSteps + " steps × " + STEP_DURATION_SECONDS + "s/step");
        System.out.printf("Estimated Picking Time : %d seconds (%dm %ds)%n",
                estimatedTimeSeconds, (estimatedTimeSeconds / 60), (estimatedTimeSeconds % 60));
        System.out.println("10-Minute SLA Target   : " + TARGET_TIME_SECONDS + " seconds (600s)");
        System.out.println("SLA Performance Check  : " + (meetsTarget ? "[WITHIN TARGET]" : "[TARGET AT RISK]"));
        System.out.println("Explanation            : " + (meetsTarget
                ? "The order is completed well within the 10-minute dark store delivery SLA."
                : "Travel time exceeds 10 minutes; order is flagged as at risk."));
        System.out.println("--------------------------------------------------------------------");

        // 6. Execute step-by-step picking & packing workflow
        simulatePickingWorkflow(assignedPicker, order, routeLegs);

        return true;
    }

    /**
     * Solves the multi-stop collection tour using the greedy nearest-neighbor heuristic:
     * from the current position, continually visits the closest remaining item.
     */
    private List<RouteLeg> planGreedyRouteLegs(Position startPos, List<Position> itemLocations) {
        List<Position> uncollected = new ArrayList<>(itemLocations);
        List<RouteLeg> legs = new ArrayList<>();

        Position current = startPos;

        while (!uncollected.isEmpty()) {
            Position nearestItem = null;
            List<Position> bestSegment = null;
            int minDistance = Integer.MAX_VALUE;

            for (Position itemPos : uncollected) {
                List<Position> segment = pathFinder.findShortestPath(current, itemPos);
                if (segment.isEmpty() && !current.equals(itemPos)) {
                    continue; // Destination unreachable
                }

                int distance = segment.isEmpty() ? 0 : segment.size() - 1;
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestItem = itemPos;
                    bestSegment = segment;
                }
            }

            if (nearestItem == null || bestSegment == null) {
                return new ArrayList<>(); // Unreachable item detected
            }

            legs.add(new RouteLeg(current, nearestItem, bestSegment));
            uncollected.remove(nearestItem);
            current = nearestItem;
        }

        return legs;
    }

    /**
     * Executes the simulated picking and packing status workflow.
     */
    private void simulatePickingWorkflow(Picker picker, Order order, List<RouteLeg> legs) {
        System.out.println("\n>>> SIMULATING WAREHOUSE PICKING WORKFLOW:");
        for (int i = 0; i < legs.size(); i++) {
            RouteLeg leg = legs.get(i);
            System.out.printf("    [Step %d/5] Picker %s collected item at %s.%n",
                    (i + 1), picker.getId(), leg.getTo());
        }

        // Picker arrives at last item location
        Position finalPos = legs.get(legs.size() - 1).getTo();
        picker.setPosition(finalPos);

        // State transition: PICKING -> PACKING
        picker.setState(Picker.State.PACKING);
        System.out.println();
        System.out.println(">> [State Change] Picker " + picker.getId() + " status -> PACKING order " + order.getOrderId() + ".");

        // State transition: PACKING -> IDLE, Order -> COMPLETED
        pickerManager.releasePicker(picker);
        order.setStatus(Order.Status.COMPLETED);

        System.out.println(">> [State Change] Order " + order.getOrderId() + " status -> COMPLETED.");
        System.out.println(">> [State Change] Picker " + picker.getId() + " status reset to IDLE at " + picker.getPosition() + ".");
        System.out.println("==================================================================");
    }

    private String formatPath(List<Position> path) {
        if (path == null || path.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            if (i < path.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }

    /**
     * Helper class representing a single hop between consecutive stops.
     */
    public static class RouteLeg {
        private final Position from;
        private final Position to;
        private final List<Position> path;

        public RouteLeg(Position from, Position to, List<Position> path) {
            this.from = from;
            this.to = to;
            this.path = path;
        }

        public Position getFrom() {
            return from;
        }

        public Position getTo() {
            return to;
        }

        public List<Position> getPath() {
            return path;
        }

        public int getSteps() {
            return path.isEmpty() ? 0 : path.size() - 1;
        }
    }
}
