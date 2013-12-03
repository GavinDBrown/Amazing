package com.TeamAmazing.drawing;

import java.util.ArrayList;
import java.util.Collections;

import android.graphics.Point;

public class Maze {
	int width;
	int height;
	Cell[][] cells;
	ArrayList<Wall> walls;

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
		this.walls = new ArrayList<Wall>(2 * width * height - width - height);
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

	private class Cell {
		int id;
		Cell ref = this;
		int rank = 1;
		Point coordinates;

		public Cell(int id, int x, int y) {
			this.id = id;
			this.coordinates = new Point(x, y);
		}
	}

	private class Wall {
		Cell v1;
		Cell v2;

		public Wall(Cell node1, Cell node2) {
			this.v1 = node1;
			this.v2 = node2;
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


	public void makePerfectMaze(Maze m) {
		Collections.shuffle(walls);
		for (Wall w : walls) {
			if (find(w.v1).ref.id != find(w.v2).ref.id) {
				// The two cells the wall is between are not connected
				// by a path, so delete the wall and union the cell's
				// partitions.
				union(w.v1, w.v2);
				walls.remove(w);
			}
		}
	}
}
