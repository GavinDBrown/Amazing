//    Amazing, the maze game.
//    Copyright (C) 2014  Gavin Brown
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package com.TeamAmazing.Maze;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class Wall implements Parcelable{

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
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(v1, flags);
		out.writeParcelable(v2, flags);
		out.writeParcelable(bounds, flags);
	}

	private void readFromParcel(Parcel in) {
		v1 = in.readParcelable(Cell.class.getClassLoader());
		v2 = in.readParcelable(Cell.class.getClassLoader());
		bounds = in.readParcelable(Rect.class.getClassLoader());
	}

	/**
	 * Constructor to use when re-constructing object from a parcel
	 * 
	 * @param in
	 *            a parcel from which to read this object
	 */
	public Wall(Parcel in) {
		readFromParcel(in);
	}

	public static final Parcelable.Creator<Wall> CREATOR = new Parcelable.Creator<Wall>() {
		public Wall createFromParcel(Parcel in) {
			return new Wall(in);
		}

		public Wall[] newArray(int size) {
			return new Wall[size];
		}
	};
}