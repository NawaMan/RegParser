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

import java.util.List;

import net.nawaman.regparser.result.entry.ParseResultEntry;
import net.nawaman.regparser.utils.IStream;


/** Node Result - For sub result*/
public final class PRNode extends PRNormal {
	
	static private final long serialVersionUID = 2545684654651635454L;
	
	private ParseResult parent;
	private int         index;
	
	PRNode(int startPosition, ParseResult parentResult) {
		this(startPosition, parentResult, null);
	}
	
	private PRNode(int startPosition, ParseResult parentResult, List<ParseResultEntry> resultEntries) {
		super(startPosition, resultEntries);
		this.parent = parentResult;
		
		int index      = 0;
		int entryCount = parentResult.entryCount();
		for (int i = 0; i < entryCount; i++) {
			if (startPosition == parentResult.endPositionOf(i)) {
				index = i;
				break;
			}
		}
		this.index = index;
	}
	
	final void parent(ParseResult parent) {
		this.parent = parent;
	}
	
	@Override
	public final ParseResult parent() {
		return parent;
	}
	
	@Override
	public final CharSequence originalCharSequence() {
		return parent.originalCharSequence();
	}
	
	@Override
	public final ParseResult duplicate() {
		// Duplication of Node cannot be optimize the same way with Temp (by avoiding recursive) 
		//     because Node hold structure that is important for verification and compilation.
		int startPosition = startPosition();
		var duplicate     = parent.duplicate();
		var entryList     = entryList();
		
		var node = new PRNode(startPosition, duplicate, entryList);
		node.index = index;
		return node;
	}
	
	// Get Element by name -----------------------------------------------------------------------
	
	@Override
	public final IStream<String> names() {
		var names = super.names();
		return names.concatWith((parent == null) ? null : parent.names());
	}
	
	/**{@inheritDoc}*/
	@Override
	public final String lastStringOf(String name) {
		var string = super.lastStringOf(name);
		if (string != null) {
			return string;
		}
		if (parent != null) {
			return parent.lastStringOf(name);
		}
		return null;
	}
	
}
