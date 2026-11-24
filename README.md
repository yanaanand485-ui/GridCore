# GridCore – Spatial Grid & Multi-Agent Task Dispatcher

A pure Core Java console application modeling a warehouse order-picking dark store using a 2D grid, Breadth-First Search (BFS) pathfinding, a greedy nearest-item collection heuristic, and a PriorityQueue-based task dispatcher.

---

## 1. Problem Statement

In automated dark stores and grocery fulfillment centers, warehouse pickers need to retrieve customer order items from distributed storage bins while navigating around fixed storage racks and aisles.

Assigning orders manually or without spatial awareness leads to picker congestion, suboptimal travel paths, missed service-level agreement (SLA) deadlines, and unbalanced workloads. **GridCore** solves this by modeling the warehouse as a discrete 2D spatial grid, automatically prioritizing incoming orders, dispatching the closest available picker, finding obstacle-free shortest paths, and planning efficient item collection tours.

---

## 2. Objectives

- Model a dark store warehouse layout using a 2D grid with obstacles and walkable aisles.
- Track multiple warehouse pickers across operational states (`IDLE`, `PICKING`, `PACKING`).
- Prioritize incoming customer orders using a `PriorityQueue` (`HIGH`, `MEDIUM`, `LOW`).
- Allocate each order to the nearest available idle picker based on BFS distance.
- Determine the optimal collection sequence of 5 item locations using a greedy nearest-next-item heuristic.
- Visualize warehouse layouts, item bins, picker locations, and planned routes in ASCII format.
- Calculate travel-time estimates and verify compliance against a 10-minute (600s) SLA target.

---

## 3. Features

- **2D Warehouse Grid Representation:** 7×7 grid modeling walkable aisles and rack obstacles.
- **PriorityQueue Task Dispatching:** Automatically dequeues and processes high-priority orders ahead of medium and low-priority tasks.
- **Dynamic Picker Allocation:** Evaluates distance from all idle pickers to the order's first item, selecting the closest eligible picker and ignoring busy pickers.
- **Obstacle-Aware BFS Pathfinding:** Traverses 4-connected orthogonal neighbors avoiding racks, guaranteeing the true shortest path in unweighted space.
- **Greedy Nearest-Item Route Planning:** Multi-hop collection sequence visiting all 5 item locations without coordinate redundancy.
- **10-Minute SLA Target Estimation:** Estimates total travel time using an explicit benchmark (30 seconds per grid step) and verifies if the order meets the 600-second target (`[WITHIN TARGET]` vs `[TARGET AT RISK]`).
- **State Machine & Lifecycle Tracking:** Seamlessly updates picker states (`IDLE -> PICKING -> PACKING -> IDLE`) and order statuses (`PENDING -> ASSIGNED -> COMPLETED`).
- **Terminal ASCII Map Overlays:** Clean visual rendering of aisles (`.`), racks (`#`), item bins (`I`), pickers (`P`), route steps (`*`), start (`S`), and destination (`E`).

---

## 4. Technologies & Tools

- **Language:** Java 21 (Core Java)
- **Data Structures:** 2D Arrays, `ArrayList`, `LinkedList`, `Queue`, `PriorityQueue`, `HashSet`, `HashMap`
- **Design:** Object-Oriented Programming (OOP)
- **Libraries/Frameworks:** None (Pure Core Java, zero external dependencies)

---

## 5. Algorithms Used & Detailed Explanation

### A. Breadth-First Search (BFS) Pathfinding
- **File:** `src/gridcore/algorithm/PathFinder.java`
- **Purpose:** Finds the shortest walkable path between two grid coordinates while routing around obstacle racks.
- **How it works:**
  1. Validates bounds and checks that start and end positions are walkable.
  2. Uses a FIFO `Queue<Position>` for frontier expansion and a `HashSet<Position>` to avoid re-visiting cells.
  3. Uses a `HashMap<Position, Position>` (parent map) to record the predecessor of each discovered cell.
  4. Explores orthogonal neighbors in 4 directions: North, South, West, East.
  5. When the destination is reached, backtracks through the parent map to reconstruct the exact path.
- **Time Complexity:** $O(V + E)$ where $V = R \times C$ (total cells) and $E \le 4V$.

### B. Greedy Nearest-Next-Item Collection Heuristic
- **File:** `src/gridcore/dispatcher/TaskDispatcher.java`
- **Purpose:** Solves the multi-stop picking tour for an order containing 5 items (a variant of the Travelling Salesperson Problem).
- **How it works:**
  1. Starts at the assigned picker's current position.
  2. Evaluates the BFS distance to all remaining uncollected items.
  3. Selects the item with the minimum distance.
  4. Appends that path segment to the overall route, omitting the first coordinate of subsequent segments to avoid duplicate vertices.
  5. Moves the current position to that item and repeats until all 5 items are collected.
- **Time Complexity:** $O(K^2 \cdot V)$ where $K = 5$ (number of items).

### C. PriorityQueue Order Management
- **File:** `src/gridcore/dispatcher/TaskDispatcher.java` & `src/gridcore/model/Order.java`
- **Purpose:** Ensures high-priority orders are fulfilled before medium and low-priority orders.
- **How it works:**
  - `Order` implements `Comparable<Order>`.
  - Priorities have explicit rank levels: `HIGH(1)`, `MEDIUM(2)`, `LOW(3)`.
  - In `PriorityQueue<Order>`, orders with lower level values are dequeued first.

---

## 6. Object-Oriented Programming (OOP) Concepts Used

1. **Encapsulation:**
   - All model classes (`Position`, `Order`, `Picker`) use private fields with public getters and setters. State changes are controlled through methods like `isAvailable()` and `setState()`.
2. **Separation of Concerns (Single Responsibility):**
   - Grid representation (`WarehouseGrid`), pathfinding (`PathFinder`), agent management (`PickerManager`), orchestration (`TaskDispatcher`), and visualization (`AsciiMapPrinter`) reside in distinct, dedicated classes.
3. **Abstraction & Interfaces:**
   - Implemented standard Java interfaces (`Comparable<Order>`) for natural priority queue ordering.
4. **Enums for Type-Safe State Modeling:**
   - Used `Order.Priority` (`HIGH`, `MEDIUM`, `LOW`), `Order.Status` (`PENDING`, `ASSIGNED`, `COMPLETED`, `FAILED`), and `Picker.State` (`IDLE`, `PICKING`, `PACKING`).

---

## 7. Strict 11-File Folder Structure

```
GridCore/
├── README.md
├── .gitignore
└── src/
    └── gridcore/
        ├── Main.java
        ├── model/
        │   ├── Position.java
        │   ├── Order.java
        │   └── Picker.java
        ├── grid/
        │   └── WarehouseGrid.java
        ├── algorithm/
        │   └── PathFinder.java
        ├── worker/
        │   └── PickerManager.java
        ├── dispatcher/
        │   └── TaskDispatcher.java
        └── util/
            └── AsciiMapPrinter.java
```

---

## 8. Compilation and Execution

### Prerequisites
- JDK 21 or higher installed (`javac -version` and `java -version`).

### 1. Compile
From the root repository directory (`GridCore/`):
```bash
javac -d bin $(find src -name "*.java")
```

### 2. Run
```bash
java -cp bin gridcore.Main
```

---

## 9. Sample Execution Output

```text
==================================================================
   GRIDCORE – SPATIAL GRID & MULTI-AGENT TASK DISPATCHER         
==================================================================

================== WAREHOUSE GRID LAYOUT ==================
     C0 C1 C2 C3 C4 C5 C6 
   +---------------------+
R0 |  P  .  .  I  .  .  . |
R1 |  .  #  #  I  #  #  . |
R2 |  .  #  #  .  #  #  . |
R3 |  .  I  .  P  .  I  . |
R4 |  .  #  #  .  #  #  . |
R5 |  .  #  #  .  #  #  . |
R6 |  .  .  .  I  .  .  P |
   +---------------------+
Legend: [.] Aisle  [#] Rack/Obstacle  [I] Item Bin  [P] Picker

Initial Pickers Status:
------------------------------------------------------------------
PICKER ID  | POSITION        | STATE        | AVAILABLE?  
------------------------------------------------------------------
PK-01      | (0, 0)          | IDLE         | YES         
PK-02      | (3, 3)          | IDLE         | YES         
PK-03      | (6, 6)          | PACKING      | NO          
------------------------------------------------------------------

Enqueuing Sample Orders into PriorityQueue:
[Dispatcher] Enqueued: Order ORD-101 [Priority: LOW] with 5 items.
[Dispatcher] Enqueued: Order ORD-102 [Priority: HIGH] with 5 items.
[Dispatcher] Enqueued: Order ORD-103 [Priority: MEDIUM] with 5 items.

>>> STARTING BATCH ORDER DISPATCHING WORKFLOW...

==================================================================
Processing Queue Task #1: Order ORD-102 [Priority: HIGH]
==================================================================
>> Picker Assigned: PK-02 at position (3, 3)
>> Initial Target Item: (6, 3)

----------------- GREEDY MULTI-STOP ROUTE BREAKDOWN -----------------
  [Leg 1/5] From (3, 3) -> Target Item at (3, 1) (2 steps)
             Path: (3, 3) -> (3, 2) -> (3, 1)
  [Leg 2/5] From (3, 1) -> Target Item at (1, 3) (4 steps)
             Path: (3, 1) -> (3, 2) -> (3, 3) -> (2, 3) -> (1, 3)
  [Leg 3/5] From (1, 3) -> Target Item at (0, 3) (1 steps)
             Path: (1, 3) -> (0, 3)
  [Leg 4/5] From (0, 3) -> Target Item at (3, 5) (5 steps)
             Path: (0, 3) -> (1, 3) -> (2, 3) -> (3, 3) -> (3, 4) -> (3, 5)
  [Leg 5/5] From (3, 5) -> Target Item at (6, 3) (5 steps)
             Path: (3, 5) -> (3, 4) -> (3, 3) -> (4, 3) -> (5, 3) -> (6, 3)
  Total Movement Steps across all 5 items: 17 steps
--------------------------------------------------------------------

================== PLANNED ROUTE VISUALIZATION ==================
     C0 C1 C2 C3 C4 C5 C6 
   +---------------------+
R0 |  .  .  .  I  .  .  . |
R1 |  .  #  #  I  #  #  . |
R2 |  .  #  #  *  #  #  . |
R3 |  .  I  *  S  *  I  . |
R4 |  .  #  #  *  #  #  . |
R5 |  .  #  #  *  #  #  . |
R6 |  .  .  .  E  .  .  . |
   +---------------------+
Route Legend: [S] Start  [E] End Destination  [*] Traversal Step  [I] Item Bin  [#] Rack

----------------- ROUTE EXECUTION & TIMING REPORT -----------------
Order ID               : ORD-102
Priority               : HIGH
Assigned Picker        : PK-02
Total Movement Steps   : 17 steps
Travel Time Formula    : 17 steps × 30s/step
Estimated Picking Time : 510 seconds (8m 30s)
10-Minute SLA Target   : 600 seconds (600s)
SLA Performance Check  : [WITHIN TARGET]
Explanation            : The order is completed well within the 10-minute dark store delivery SLA.
--------------------------------------------------------------------

>>> SIMULATING WAREHOUSE PICKING WORKFLOW:
    [Step 1/5] Picker PK-02 collected item at (3, 1).
    [Step 2/5] Picker PK-02 collected item at (1, 3).
    [Step 3/5] Picker PK-02 collected item at (0, 3).
    [Step 4/5] Picker PK-02 collected item at (3, 5).
    [Step 5/5] Picker PK-02 collected item at (6, 3).

>> [State Change] Picker PK-02 status -> PACKING order ORD-102.
>> [State Change] Order ORD-102 status -> COMPLETED.
>> [State Change] Picker PK-02 status reset to IDLE at (6, 3).
==================================================================
...
```

---

## 10. Author
Developed as a Core Java academic project for spatial grid simulation and multi-agent dispatching.
