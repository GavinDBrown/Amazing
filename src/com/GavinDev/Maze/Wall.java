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

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class Wall implements Parcelable {

    private Cell mCell1;
    private Cell mCell2;
    private Rect mBounds = null;

    public Wall(Cell cell1, Cell cell2) {
        mCell1 = cell1;
        mCell2 = cell2;
    }

    public Cell getCell1() {
        return mCell1;
    }

    public Cell getCell2() {
        return mCell2;
    }

    public Rect getBounds() {
        return mBounds;
    }

    public void setBounds(Rect newBounds) {
        mBounds = newBounds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mCell1 == null) ? 0 : mCell1.hashCode());
        result = prime * result + ((mCell2 == null) ? 0 : mCell2.hashCode());
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
        if (mCell1 == null) {
            if (other.mCell1 != null)
                return false;
        } else if (!mCell1.equals(other.mCell1) && !mCell1.equals(other.mCell2))
            return false;
        if (mCell2 == null) {
            if (other.mCell2 != null)
                return false;
        } else if (!mCell2.equals(other.mCell2) && !mCell2.equals(other.mCell1))
            return false;
        if (mCell1 != null && mCell2 != null) {
            if (!(mCell1.equals(other.mCell1) && mCell2.equals(other.mCell2))
                    && !(mCell1.equals(other.mCell2) && mCell2.equals(other.mCell1)))
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
        out.writeParcelable(mCell1, flags);
        out.writeParcelable(mCell2, flags);
        out.writeParcelable(mBounds, flags);
    }

    private void readFromParcel(Parcel in) {
        mCell1 = in.readParcelable(Cell.class.getClassLoader());
        mCell2 = in.readParcelable(Cell.class.getClassLoader());
        mBounds = in.readParcelable(Rect.class.getClassLoader());
    }

    /**
     * Constructor to use when re-constructing object from a parcel
     * 
     * @param in a parcel from which to read this object
     */
    public Wall(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<Wall> CREATOR = new Parcelable.Creator<Wall>() {
        @Override
        public Wall createFromParcel(Parcel in) {
            return new Wall(in);
        }

        @Override
        public Wall[] newArray(int size) {
            return new Wall[size];
        }
    };
}
