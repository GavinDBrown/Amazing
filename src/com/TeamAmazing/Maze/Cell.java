package com.TeamAmazing.Maze;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

public class Cell implements Parcelable {

	public static final int REGULAR_CELL = 0;
	public static final int START_CELL = 1;
	public static final int END_CELL = 2;
	int id;
	Cell ref = this;
	/**
	 * Rank is used differently in different algorithms. In Kruskal's algorithm
	 * it's used as part of a union-find data structure. In the DFS algorithm
	 * it's used to mark cells as visited or not.
	 */
	int rank = 1;
	Point coordinates;
	int type = REGULAR_CELL;

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

	void markVisited() {
		this.rank = 0;
	}

	boolean isUnvisited() {
		return this.rank != 0;
	}

	@Override
	public int hashCode() {
		final int prime = 17;
		int result = 1;
		result = prime * result
				+ ((coordinates == null) ? 0 : coordinates.hashCode());
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
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		return true;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(coordinates, flags);
		out.writeInt(type);

	}

	private void readFromParcel(Parcel in) {
		coordinates = in.readParcelable(Point.class.getClassLoader());
		type = in.readInt();
	}

	/**
	 * Constructor to use when re-constructing object from a parcel
	 * 
	 * @param in
	 *            a parcel from which to read this object
	 */
	public Cell(Parcel in) {
		readFromParcel(in);
	}

	public static final Parcelable.Creator<Cell> CREATOR = new Parcelable.Creator<Cell>() {
		public Cell createFromParcel(Parcel in) {
			return new Cell(in);
		}

		public Cell[] newArray(int size) {
			return new Cell[size];
		}
	};

}