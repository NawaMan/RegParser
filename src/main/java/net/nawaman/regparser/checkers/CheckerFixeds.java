/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2022 Nawapunth Manusitthipol.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's RegParser.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawa<at>nawaman<dot>net.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.regparser.checkers;

import static java.lang.String.format;
import static net.nawaman.regparser.RegParser.newRegParser;

import java.util.ArrayList;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.RegParserEntry;
import net.nawaman.regparser.result.ParseResult;

/**
 * Checker that separate text into pre-defined groups
 * 
 * This checks allows the simulation of RegParser by using Checker. This is very useful if a user want to create a
 *   complex checker (a checker that result in a ParseResult) without using RegParser. For example, if a java checker
 *   used to parser a date in the format "YYYY-MM-DD". The parse results will be in the following group:
 *   1: 0-3 is the year  
 *   2: 5-6 is the month
 *   3: 8-9 is the date
 *   The developer can create a Parser Type that return CheckerFixeds as a Checker by using the CheckerFixeds.Entry as
 *     the following:
 *     
 *   return new CheckerFixeds(
 *               new CheckerFixeds.Entry("Year",  4, PTP.getType("Number").getTypeRef()),
 *               new CheckerFixeds.Entry(1),
 *               new CheckerFixeds.Entry("Month", 2, PTP.getType("Number").getTypeRef()),
 *               new CheckerFixeds.Entry(1),
 *               new CheckerFixeds.Entry("Date",  2, PTP.getType("Number").getTypeRef())
 *           );
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 **/
public class CheckerFixeds implements Checker {
	
	private static final long serialVersionUID = 5484946354964465247L;
	
	private int     length  = 0;
	private Entry[] entries = Entry.EMPTY_ARRAY;
	
	/** Constructs a checker fixed */
	public CheckerFixeds(Entry... entries) {
		if ((entries == null)
		 || (entries.length == 0))
			return;
		
		int totalLength = 0;
		var list        = new ArrayList<Entry>();
		for (int i = 0; i < entries.length; i++) {
			var entry = entries[i];
			if (entry == null)
				continue;
			
			list.add(entry);
			int length = entry.length();
			if (length == -1) {
				if (i != (entries.length - 1)) {
					var errorMessage = format("Length cannot be negative (%d), unless it is the last entry.", length);
					throw new IllegalArgumentException(errorMessage);
				}
			} else {
				totalLength += length;
			}
		}
		int listSize = list.size();
		if (listSize == 0)
			return;
		
		this.entries = list.toArray(new Entry[listSize]);
		this.length  = totalLength;
	}
	
	/** Returns the overall length needed by this checker */
	public int neededLength() {
		return length;
	}
	
	/** Returns the number of fixed-entry */
	public int entryCount() {
		return entries.length;
	}
	
	/** Returns the entry at the index I */
	public Entry entry(int index) {
		return ((index < 0) || (index > entryCount()))
		        ? null
		        : entries[index];
	}
	
	@Override
	public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider) {
		return startLengthOf(text, offset, typeProvider, null);
	}
	
	@Override
	public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider, ParseResult parseResult) {
		return neededLength();
	}
	
	@Override
	public Checker optimize() {
		return this;
	}
	
	/** The group entries */
	static public class Entry {
		
		public static final Entry[] EMPTY_ARRAY = new Entry[0];
		
		Entry() {
			this(null, -1, (Object)null);
		}
		
		Entry(String name) {
			this(name, -1, (Object)null);
		}
		
		Entry(String name, Checker secondStage) {
			this(name, -1, (Object)secondStage);
		}
		
		Entry(String name, ParserType secondStage) {
			this(name, -1, (Object)secondStage);
		}
		
		Entry(String name, ParserTypeRef secondStage) {
			this(name, -1, (Object)secondStage);
		}
		
		Entry(int length) {
			this(null, length, (Object)null);
		}
		
		Entry(String name, int length) {
			this(name, length, (Object)null);
		}
		
		Entry(String name, int length, Checker secondStage) {
			this(name, length, (Object)secondStage);
		}
		
		Entry(String name, int length, ParserType secondStage) {
			this(name, length, (Object)secondStage);
		}
		
		Entry(String name, int length, ParserTypeRef secondStage) {
			this(name, length, (Object)secondStage);
		}
		
		Entry(String name, int length, Object secondStage) {
			if (length < 0) {
				length = -1;
			}
			
			Checker checker = null;
			if (secondStage != null) {
				RegParserEntry[] entries;
				if (secondStage instanceof Checker) {
					entries = new RegParserEntry[] { RegParserEntry.newParserEntry(name, (Checker)secondStage) };
				} else if (secondStage instanceof ParserType) {
					entries = new RegParserEntry[] { RegParserEntry.newParserEntry(name, (ParserType)secondStage) };
				} else if (secondStage instanceof ParserTypeRef) {
					entries = new RegParserEntry[] { RegParserEntry.newParserEntry(name, (ParserTypeRef)secondStage) };
				} else {
					var errMsg = format("Second stage must be null, Checker, PTypeRef or PType (%s).", secondStage);
					throw new IllegalArgumentException(errMsg);
				}
				checker = newRegParser(entries);
			}
			this.entry = RegParserEntry.newParserEntry(name, CheckerAny.getCheckerAny(length), null, checker);
		}
		
		private final RegParserEntry entry;
		
		public String name() {
			return entry.name();
		}
		
		public int length() {
			return ((CheckerAny)entry.checker()).length();
		}
		
		public Checker secondStage() {
			return entry.secondStage();
		}
		
		public RegParserEntry entry() {
			return entry;
		}
	}
	
}
