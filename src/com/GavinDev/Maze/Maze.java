/*  Amazing, the maze game.
 * Copyright (C) 2014  Gavin Brown
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.GavinDev.Maze;

import android.os.Parcel;
import android.os.Parcelable;

import com.GavinDev.activities.StartMenu;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Maze implements Parcelable {
    private int mWidth; // The number of cells per row
    private int mHeight; // The number of cells per column
    private Cell[] mCells; // An array of the cells in row major order
    private List<Wall> mWalls;
    private Random mRand = new Random();

    public Maze(int width, int height, int mazeType) {
        mWidth = width;
        mHeight = height;
        int id = 0;
        mCells = new Cell[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                mCells[i * width + j] = new Cell(id, i, j);
                id++;
            }
        }
        switch (mazeType) {
            case StartMenu.PERFECT_MAZE:
                kruskalMaze();
                break;
            case StartMenu.DFS_MAZE:
                dfsMaze();
                break;
            case StartMenu.GROWING_TREE_MAZE:
                growingTreeMaze();
                break;
            default:
                throw new IllegalArgumentException("Invalid mazeType");
        }

        // Set the start and finish cells.
        mCells[0].setType(Cell.START_CELL);
        mCells[mCells.length - 1].setType(Cell.END_CELL);
    }

    /**
     * Creates a maze using a union-find algorithm. Also known as Kruskal's
     * algorithm. Puts each cell into it's own set, randomly removes walls
     * between distinct sets and merges the sets. Uses a union/find data
     * structure to efficiently combine sets.
     */
    private void kruskalMaze() {
        makeAllWalls();
        Collections.shuffle(mWalls);
        for (Iterator<Wall> iterator = mWalls.iterator(); iterator.hasNext();) {
            Wall wall = iterator.next();
            // Avoid the walls on the boundary
            if (wall.getCell1() != null && wall.getCell2() != null) {
                if (find(wall.getCell1()).ref.id != find(wall.getCell2()).ref.id) {
                    // The two cells the wall is between are not connected
                    // by a path, so delete the wall and union the cell's
                    // partitions.
                    union(wall.getCell1(), wall.getCell2());
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Recursive backtracker Depth First Search algorithm. Starts at a random
     * cell in the bottom half of the maze and traverse to unvisited neighbors
     * removing walls along the way. If there are no unvisited neighbors then
     * backtrack until one is found or if none are found pick a random unvisited
     * cell and recurse until all cells are visited.
     */
    private void dfsMaze() {
        makeAllWalls();
        Deque<Cell> stack = new ArrayDeque<Cell>();
        // Make the initial cell the current cell and mark it as visited
        Cell currentCell = mCells[mCells.length / 2 + mRand.nextInt(mCells.length / 2)];
        // Make a copy of the cells and shuffle it.
        Cell[] shuffledCells = mCells.clone();
        Collections.shuffle(Arrays.asList(shuffledCells));
        currentCell.markVisited();
        Cell nextCell = null;
        int numOfUnvisitedCells = mWidth * mHeight - 1;
        // While there are unvisited cells
        while (numOfUnvisitedCells > 0) {
            // If the current cell has any neighbors which have not been visited
            // Choose randomly one of the unvisited neighbors
            nextCell = getRandomUnvistedNeighbor(currentCell);
            if (nextCell != null) {
                // Push the current cell to the stack
                stack.addFirst(currentCell);
                // Remove the wall between the current cell and the chosen cell
                mWalls.remove(new Wall(currentCell, nextCell));
                // Make the chosen cell the current cell and mark it as visited
                currentCell = nextCell;
                currentCell.markVisited();
                numOfUnvisitedCells--;
            } else if (!stack.isEmpty()) {
                // Pop a cell from the stack and make it the current cell
                currentCell = stack.removeFirst();
            } else {
                // Pick a random unvisited cell, make it the current cell and
                // mark it as visited
                for (Cell c : shuffledCells) {
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

    /**
     * Very similar to the DFS algorithm. The difference is when the algorithm
     * backtracks there is a 50% chance that it will backtrack to the most
     * recently visited cell and a 50% chance it will backtrack to a random cell
     * on the stack of visited cells.
     */
    private void growingTreeMaze() {
        makeAllWalls();
        Deque<Cell> stack = new ArrayDeque<Cell>();
        // Make the initial cell the current cell and mark it as visited
        Cell currentCell = mCells[mCells.length / 2 + mRand.nextInt(mCells.length / 2)];
        // Make a copy of the cells and shuffle it.
        Cell[] shuffledCells = mCells.clone();
        Collections.shuffle(Arrays.asList(shuffledCells));
        currentCell.markVisited();
        Cell nextCell = null;
        int numOfUnvisitedCells = mWidth * mHeight - 1;
        // While there are unvisited cells
        while (numOfUnvisitedCells > 0) {
            // If the current cell has any neighbors which have not been visited
            // Choose randomly one of the unvisited neighbors
            nextCell = getRandomUnvistedNeighbor(currentCell);
            if (nextCell != null) {
                // Push the current cell to the stack
                stack.addFirst(currentCell);
                // Remove the wall between the current cell and the chosen cell
                mWalls.remove(new Wall(currentCell, nextCell));
                // Make the chosen cell the current cell and mark it as visited
                currentCell = nextCell;
                currentCell.markVisited();
                numOfUnvisitedCells--;
            } else if (!stack.isEmpty()) {
                // 50/50 chance to pop the first cell or pop a random cell
                if (mRand.nextBoolean()) {
                    // Pop a cell from the stack and make it the current cell
                    currentCell = stack.removeFirst();
                } else {
                    // Choose a random cell from the stack and make it the
                    // current cell
                    currentCell = (Cell) stack.toArray()[mRand.nextInt(stack.size())];
                }
            } else {
                // Pick a unvisited cell, make it the current cell and mark it
                // as visited
                for (Cell c : shuffledCells) {
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

    /**
     * Makes all walls in a maze, i.e. a grid. Useful for maze generation
     * algorithms that delete walls.
     */
    private void makeAllWalls() {
        mWalls = new ArrayList<Wall>(2 * mWidth * mHeight + mWidth + mHeight);
        for (int j = 0; j < mWidth; j++) {
            // Add walls on the top and bottom
            mWalls.add(new Wall(mCells[j], null));
            mWalls.add(new Wall(mCells[(mHeight - 1) * mWidth + j], null));
        }
        for (int i = 0; i < mHeight; i++) {
            // Add walls on the left and right
            mWalls.add(new Wall(mCells[i * mWidth], null));
            mWalls.add(new Wall(mCells[i * mWidth + (mWidth - 1)], null));
        }

        // Add the walls on the inside of the maze.
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                // add wall to the right if there is space
                if (j + 1 < mWidth) {
                    mWalls.add(new Wall(mCells[i * mWidth + j], mCells[i * mWidth + (j + 1)]));
                }

                // add wall to the bottom if there is space
                if (i + 1 < mHeight) {
                    mWalls.add(new Wall(mCells[i * mWidth + j], mCells[(i + 1) * mWidth + j]));
                }
            }
        }
    }

    /**
     * Unions the two sets that cell1 and cell2 are members of.
     */
    private void union(Cell cell1, Cell cell2) {
        Cell ref1 = find(cell1);
        Cell ref2 = find(cell2);
        if (ref1.rank > ref2.rank)
            ref2.ref = ref1;
        else if (ref1.rank < ref2.rank)
            ref1.ref = ref2;
        else if (!ref1.equals(ref2)) {
            ref2.ref = ref1;
            ref1.rank++;
        }
    }

    /**
     * Returns the representative cell of the set cell belongs to and performs
     * path compression along the way.
     */
    private Cell find(Cell cell) {
        if (cell.id != cell.ref.id) {
            cell.ref = find(cell.ref);
            return cell.ref;
        } else
            return cell;
    }

    private List<Cell> getNeighborCells(Cell cell) {
        List<Cell> neighbors = new ArrayList<Cell>(4);
        if (cell.coordinates.y != 0) {
            // Cell is not in the left column
            neighbors.add(mCells[cell.coordinates.x * mWidth + (cell.coordinates.y - 1)]);
        }
        if (cell.coordinates.y != mWidth - 1) {
            // Cell is not in the right column
            neighbors.add(mCells[cell.coordinates.x * mWidth + (cell.coordinates.y + 1)]);
        }
        if (cell.coordinates.x != 0) {
            // Cell is not in the top row
            neighbors.add(mCells[(cell.coordinates.x - 1) * mWidth + cell.coordinates.y]);
        }
        if (cell.coordinates.x != mHeight - 1) {
            // Cell is not in the bottom row
            neighbors.add(mCells[(cell.coordinates.x + 1) * mWidth + cell.coordinates.y]);
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

    /**
     * Return the first Cell of the given type.
     * 
     * @param type The type of the Cell to be returned.
     * @return Returns the first Cell found of the given type, if no Cell is
     *         found returns null.
     */
    public Cell getCell(int type) {
        for (int i = 0; i < mWidth; i++) {
            for (int j = 0; j < mHeight; j++) {
                if (mCells[j * mWidth + i].type == type)
                    return mCells[j * mWidth + i];
            }

        }
        return null;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public Cell[] getCells() {
        return mCells;
    }

    public List<Wall> getWalls() {
        return mWalls;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mWidth);
        out.writeInt(mHeight);
        out.writeTypedArray(mCells, flags);
        out.writeList(mWalls);

    }

    private void readFromParcel(Parcel in) {
        mWidth = in.readInt();
        mHeight = in.readInt();
        in.readTypedArray(mCells, Cell.CREATOR);
        in.readList(mWalls, Wall.class.getClassLoader());
    }

    /**
     * Constructor to use when re-constructing object from a parcel
     * 
     * @param in a parcel from which to read this object
     */
    public Maze(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<Maze> CREATOR = new Parcelable.Creator<Maze>() {
        @Override
        public Maze createFromParcel(Parcel in) {
            return new Maze(in);
        }

        @Override
        public Maze[] newArray(int size) {
            return new Maze[size];
        }
    };
}
