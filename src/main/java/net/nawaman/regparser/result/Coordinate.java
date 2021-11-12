/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2021 Nawapunth Manusitthipol. Implements with and for Java 11 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's RegParser.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via me<at>nawaman<dot>net.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */
package net.nawaman.regparser.result;

import java.util.Objects;

/**
 * Coordinate (Column and Row) in the code.
 */
public class Coordinate {
	
	public static final Coordinate START = new Coordinate(0, 0);
	
	/** @return  the column of the coordinate or -1 if null */
	public static int colOf(Coordinate coordinate) {
		return (coordinate == null) ? -1 : coordinate.col();
	}
	
	/** @return  the row of the coordinate or -1 if null */
	public static int rowOf(Coordinate coordinate) {
		return (coordinate == null) ? -1 : coordinate.row();
	}
	
	/** @return a Coordinate from col and row. */
	public static Coordinate of(int col, int row) {
		return new Coordinate(col, row);
	}
	
	/** @return a Coordinate from col and row array. */
	public static Coordinate of(int[] colRow) {
		return ((colRow == null) || (colRow.length < 2)) ? null : new Coordinate(colRow[0], colRow[1]);
	}
	
	/** Returns the array of column and row of the source text at the offset */
	public static Coordinate of(CharSequence sourceText, int offset) {
		return coordinateOf(sourceText, offset);
	}
	
	/** Returns the array of column and row of the source text at the offset */
	public static Coordinate coordinateOf(CharSequence sourceText, int offset) {
		if (sourceText == null) {
			return null;
		}
		if (offset >= sourceText.length()) {
			offset = sourceText.length() - 1;
		}
		if (offset <= 0) {
			return Coordinate.START;
		}
		
		int prevLine   = 0;
		int lineCount  = 0;
		int textLength = sourceText.length();
		
		for (int i = 0; i < textLength; i++) {
			if (sourceText.charAt(i) == '\r') {
				if ((textLength > i) && (sourceText.charAt(i + 1) == '\n')) {
					i++;
				}
			} else if (sourceText.charAt(i) == '\n') {
				if (offset <= i) {
					// Found it
					if (prevLine != 0) {
						prevLine++;
					}
					return new Coordinate(offset - prevLine, lineCount);
					
				} else {
					prevLine = i;
				}
				lineCount++;
			} else {
				continue;
			}
		}
		
		// Not found yet, so it is the last line
		if (prevLine != 0) {
			prevLine++;
		}
		return new Coordinate(offset - prevLine, lineCount);
	}
	
	//== Instance ======================================================================================================
	
	private final int col;
	private final int row;
	
	public Coordinate(int col, int row) {
		this.col = col;
		this.row = row;
	}
	
	public int col() {
		return col;
	}
	
	public int row() {
		return row;
	}
	
	public int[] toArray() {
		return new int[] {col, row};
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(col, row);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		var other = (Coordinate)obj;
		return col == other.col && row == other.row;
	}
	
	@Override
	public String toString() {
		return "Position [col=" + col + ", row=" + row + "]";
	}
	
}
