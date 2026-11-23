# GridCore
### Spatial Grid & Multi-Agent Task Dispatcher

GridCore is a Java-based warehouse order-picking system that models a grocery dark store using a 2D grid. It aims to find obstacle-aware paths, optimize item collection routes, and assign orders to available pickers.

## Problem Statement
In a warehouse, pickers need to collect multiple items from different locations while avoiding obstacles. Assigning orders and planning efficient picking routes manually can be time-consuming.

GridCore aims to simplify this process using grid-based pathfinding and basic task-dispatching algorithms.

## Objectives
- Represent a warehouse layout using a 2D grid.
- Find shortest paths while avoiding obstacles.
- Plan item collection routes using a greedy approach.
- Assign orders to the nearest available picker.
- Manage picker availability and order status.
- Display warehouse routes in the terminal.

## Planned Features
- 10×10 warehouse grid representation.
- BFS-based shortest pathfinding.
- Greedy nearest-item route planning.
- PriorityQueue-based picker allocation.
- Picker state management.
- Terminal-based warehouse visualization.
- Completed order history using file handling.

## Technologies & Concepts
- **Language:** Java (Core Java)
- **Data Structures:** 2D Arrays, Queue, PriorityQueue
- **Algorithms:** Breadth-First Search (BFS), Greedy Approach
- **Java Concepts:** OOP, Collections, Enums, Exception Handling, File Handling

## Project Scope
GridCore focuses on internal warehouse operations, including item picking, route planning, and picker allocation.

It does not currently include customer delivery tracking, GPS navigation, payment processing, or a graphical user interface.

## Project Status
<em>Currently in the planning and initial development stage.</em>

Features will be updated as they are implemented.

## Author
Developed as an academic Java project.
