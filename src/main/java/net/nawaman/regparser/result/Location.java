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

public class Location {
	
	/** Returns the string representation of the source text at the offset */
	public static String of(CharSequence sourceText, int offset) {
		return locationOf(sourceText, offset);
	}
	
	/** Returns the string representation of the source text at the offset */
	public static String locationOf(CharSequence sourceText, int offset) {
		if (sourceText == null) {
			return null;
		}
		if ((offset < 0) || (offset > sourceText.length())) {
			return null;
		}
		
		boolean isShift = false;
		if ((offset == sourceText.length()) || (sourceText.charAt(offset) == '\n')) {
			// Shift to avoid point at NewLine
			isShift = true;
			offset--;
		}
		
		var originalText  = sourceText.toString();
		int thisLineBegin = originalText.lastIndexOf("\n", offset);
		int thisLineEnd   = originalText.indexOf("\n", offset);
		if (thisLineEnd == -1) {
			thisLineEnd = originalText.length();
		}
		
		int thisLineStart = (thisLineBegin + 1 <= thisLineEnd) ? (thisLineBegin + 1) : thisLineEnd;
		var thisLine = originalText.substring(thisLineStart, thisLineEnd);
		thisLineBegin++;
		
		var buffer = new StringBuffer();
		
		// Print (Row, Col)
		var position = Coordinate.of(sourceText, offset);
		if (position != null) {
			buffer.append("(").append(position.row()).append(",").append(position.col()).append(")\n");
		}
		
		if (thisLineBegin > 1) {
			int PrevLineBegin = originalText.lastIndexOf("\n", thisLineBegin - 2);
			if (PrevLineBegin == -1) {
				PrevLineBegin = 0;
			}
			
			var prevLine = originalText.substring(PrevLineBegin, thisLineBegin - 1);
			buffer.append("\n");
			buffer.append("\t-|");
			for (int i = 0; i < prevLine.length(); i++) {
				if (prevLine.charAt(i) == '\t') {
					buffer.append("    ");
				} else if (prevLine.charAt(i) == '\n') {
					continue;
				} else {
					buffer.append(prevLine.charAt(i));
				}
			}
		}
		
		buffer.append("\n");
		buffer.append("\t-|");
		buffer.append((thisLine.endsWith("\n")) ? thisLine.substring(0, thisLine.length() - 1) : thisLine);
		
		offset -= thisLineBegin;
		buffer.append("\n");
		buffer.append("\t-|");
		for (int i = 0; i < offset; i++) {
			buffer.append((thisLine.charAt(i) != '\t') ? " ": "    ");
		}
		if (isShift) {
			buffer.append(' ');
		}
		buffer.append('^');
		if (isShift) {
			buffer.append("-- At the end of the line");
		}
		buffer.append('\n');
		return buffer.toString();
	}
	
	//== Instance ======================================================================================================
	
	private final String     sourceText;
	private final int        position;
	private final Coordinate coordinate;
	
	public Location(String sourceText, int position) {
		this.sourceText = sourceText;
		this.position   = position;
		this.coordinate = Coordinate.of(sourceText, position);
	}
	
	public String sourceText() {
		return sourceText;
	}
	
	public int position() {
		return position;
	}
	
	public Coordinate colRow() {
		return coordinate;
	}
	
	public String asString() {
		return Location.of(sourceText, position);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(coordinate, sourceText);
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
		var other = (Location)obj;
		return Objects.equals(coordinate, other.coordinate)
			&& Objects.equals(sourceText, other.sourceText);
	}
	
	@Override
	public String toString() {
		return "Location [sourceText=" + sourceText + ", position=" + position + ", coordinate=" + coordinate + "]";
	}
	
}
