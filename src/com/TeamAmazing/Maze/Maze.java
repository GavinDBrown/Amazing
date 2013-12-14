package com.TeamAmazing.Maze;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import android.graphics.Point;
import android.graphics.Rect;

public class Maze {
	private int width;
	private int height;
	private Cell[][] cells;
	private List<Wall> walls;
	public static final int REGULAR_CELL = 0;
	public static final int START_CELL = 1;
	public static final int END_CELL = 2;

	public Maze(int width, int height) {
		this.width = width;
		this.height = height;
		int id = 0;
		this.cells = new Cell[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				cells[i][j] = new Cell(id, i, j);
				id++;
			}
		}
	}

	public class Cell {
		private int id;
		private Cell ref = this;
		// int rank is used differently in different algorithms. In Kruskal's
		// algorithm it's used as part of a union-find data structure. In the
		// DFS algorithm it's used to mark cells as visited or not.
		private int rank = 1;
		private Point coordinates;
		private int type = REGULAR_CELL;

		public Cell(int id, int x, int y) {
			this.id = id;
			this.coordinates = new Point(x, y);
		}

		public Point getCoords() {
			return coordinates;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		private void markVisited() {
			this.rank = 0;
		}

		private boolean isUnvisited() {
			return this.rank != 0;
		}

		@Override
		public int hashCode() {
			final int prime = 17;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((coordinates == null) ? 0 : coordinates.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Cell other = (Cell) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (coordinates == null) {
				if (other.coordinates != null)
					return false;
			} else if (!coordinates.equals(other.coordinates))
				return false;
			return true;
		}

		private Maze getOuterType() {
			return Maze.this;
		}

	}

	private void makeAllWalls() {
		this.walls = new ArrayList<Wall>(2 * (width + 1) * (height + 1));
		for (int i = 0; i < width; i++) {
			// Add walls on the top and bottom
			walls.add(new Wall(cells[i][0], null));
			walls.add(new Wall(cells[i][height - 1], null));
		}
		for (int j = 0; j < height; j++) {
			// Add walls on the left and right
			walls.add(new Wall(cells[0][j], null));
			walls.add(new Wall(cells[width - 1][j], null));
		}

		// Add the walls on the inside of the maze.
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (i < width - 1) {
					walls.add(new Wall(cells[i][j], cells[i + 1][j]));
				}
				if (j < height - 1) {
					walls.add(new Wall(cells[i][j], cells[i][j + 1]));
				}
			}
		}
	}

	public class Wall {
		private Cell v1;
		private Cell v2;
		private Rect bounds = null;

		public Wall(Cell node1, Cell node2) {
			this.v1 = node1;
			this.v2 = node2;
		}

		public Cell getV1() {
			return this.v1;
		}

		public Cell getV2() {
			return this.v2;
		}

		public Rect getBounds() {
			return bounds;
		}

		public void setBounds(Rect r) {
			this.bounds = r;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((v1 == null) ? 0 : v1.hashCode());
			result = prime * result + ((v2 == null) ? 0 : v2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Wall other = (Wall) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (v1 == null) {
				if (other.v1 != null)
					return false;
			} else if (!v1.equals(other.v1) && !v1.equals(other.v2))
				return false;
			if (v2 == null) {
				if (other.v2 != null)
					return false;
			} else if (!v2.equals(other.v2) && !v2.equals(other.v1))
				return false;
			if (v1 != null && v2 != null) {
				if (!(v1.equals(other.v1) && v2.equals(other.v2))
						&& !(v1.equals(other.v2) && v2.equals(other.v1)))
					return false;
			}
			return true;
		}

		private Maze getOuterType() {
			return Maze.this;
		}

	}

	private void union(Cell n1, Cell n2) {
		Cell r1 = find(n1);
		Cell r2 = find(n2);
		if (r1.rank > r2.rank)
			r2.ref = r1;
		else if (r1.rank < r2.rank)
			r1.ref = r2;
		else if (!r1.equals(r2)) {
			r2.ref = r1;
			r1.rank++;
		}
	}

	private Cell find(Cell node) {
		if (node.id != node.ref.id) {
			node.ref = find(node.ref);
			return node.ref;
		} else
			return node;
	}

	/**
	 * Creates a perfect maze using a union-find algorithm. Also known as
	 * Kruskal's algorithm.
	 */
	public void makePerfectMaze() {
		makeAllWalls();
		Collections.shuffle(walls);
		for (Iterator<Wall> it = walls.iterator(); it.hasNext();) {
			Wall w = it.next();
			// Avoid the walls on the boundary
			if (w.v1 != null && w.v2 != null) {
				if (find(w.v1).ref.id != find(w.v2).ref.id) {
					// The two cells the wall is between are not connected
					// by a path, so delete the wall and union the cell's
					// partitions.
					union(w.v1, w.v2);
					it.remove();
				}
			}
		}
	}

	private List<Cell> getNeighborCells(Cell cell) {
		List<Cell> neighbors = new ArrayList<Cell>(4);
		if (cell.coordinates.x != 0) {
			// Cell is not in the left column
			neighbors.add(cells[cell.coordinates.x - 1][cell.coordinates.y]);
		}
		if (cell.coordinates.x != width - 1) {
			// Cell is not in the right column
			neighbors.add(cells[cell.coordinates.x + 1][cell.coordinates.y]);
		}
		if (cell.coordinates.y != 0) {
			// Cell is not in the top row
			neighbors.add(cells[cell.coordinates.x][cell.coordinates.y - 1]);
		}
		if (cell.coordinates.y != height - 1) {
			// Cell is not in the bottom row
			neighbors.add(cells[cell.coordinates.x][cell.coordinates.y + 1]);
		}
		return neighbors;
	}

	private Cell getRandomUnvistedNeighbor(Cell cell) {
		List<Cell> neighbors = getNeighborCells(cell);
		Collections.shuffle(neighbors);

		for (Cell c : neighbors) {
			if (c.isUnvisited())
				return c;
		}
		return null;
	}

	// Recursive backtracker DFS algorithm
	public void makeDFSMaze() {
		makeAllWalls();
		Deque<Cell> stack = new ArrayDeque<Cell>();
		// Make the initial cell the current cell and mark it as visited
		Cell currentCell = cells[width-1][height-1];
		currentCell.markVisited();
		Cell nextCell = null;
		int numOfUnvisitedCells = width * height - 1;
		// While there are unvisited cells
		while (numOfUnvisitedCells > 0) {
			// If the current cell has any neighbors which have not been visited
			// Choose randomly one of the unvisited neighbors
			nextCell = getRandomUnvistedNeighbor(currentCell);
			if (nextCell != null) {
				// Push the current cell to the stack
				stack.addFirst(currentCell);
				// Remove the wall between the current cell and the chosen cell
				walls.remove(new Wall(currentCell, nextCell));
				// Make the chosen cell the current cell and mark it as visited
				currentCell = nextCell;
				currentCell.markVisited();
				numOfUnvisitedCells--;
			} else if (!stack.isEmpty()) {
				// Pop a cell from the stack and make it the current cell
				currentCell = stack.removeFirst();
			} else {
				// Pick a random cell, make it the current cell and mark it as
				// visited
				for (Cell[] col : cells) {
					for (Cell c : col) {
						if (c.isUnvisited()) {
							currentCell = c;
							currentCell.markVisited();
							numOfUnvisitedCells--;
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Return the first Cell of the given type.
	 * 
	 * @param type
	 *            The type of the Cell to be returned.
	 * @return Returns the first Cell found of the given type, if no Cell is
	 *         found returns null.
	 */
	public Cell getCell(int type) {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[0].length; j++) {
				if (cells[i][j].type == type)
					return cells[i][j];
			}

		}
		return null;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return the cells
	 */
	public Cell[][] getCells() {
		return cells;
	}

	/**
	 * @return the walls
	 */
	public List<Wall> getWalls() {
		return walls;
	}
}
