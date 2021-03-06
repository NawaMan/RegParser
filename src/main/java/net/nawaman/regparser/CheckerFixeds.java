/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
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

package net.nawaman.regparser;

import java.util.Vector;

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
 *   			new CheckerFixeds.Entry("Year",  4, PTP.getType("Number").getTypeRef()),
 *   			new CheckerFixeds.Entry(1),
 *   			new CheckerFixeds.Entry("Month", 2, PTP.getType("Number").getTypeRef()),
 *   			new CheckerFixeds.Entry(1),
 *   			new CheckerFixeds.Entry("Date",  2, PTP.getType("Number").getTypeRef())
 *   		);
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 **/
public class CheckerFixeds implements Checker {
	
	static private final long serialVersionUID = 5484946354964465247L;
	
	/** Constructs a checker fixed */
	public CheckerFixeds(Entry ... pGEntries) {
		if((pGEntries == null) || (pGEntries.length == 0)) return;
		int L = 0;
		Vector<Entry> Entries = new Vector<Entry>();
		for(int i = 0; i < pGEntries.length; i++) {
			Entry GE = pGEntries[i];
			if(GE == null) continue;
			Entries.add(GE);
			int l = GE.getLength();
			if(l == -1) {
				if(i != (pGEntries.length - 1))
					throw new IllegalArgumentException("Length cannot be negative ("+l+"), unless it is the last entry.");
			} else L += l;
		}
		if(Entries.size() == 0) return;
		this.GEntries = Entries.toArray(new Entry[Entries.size()]);
		this.Length   = L;
	}
	
	/** The group entries */
	static public class Entry {
		public Entry()                                    { this(null,  -1, (Object)null);         }
		public Entry(String pName)                        { this(pName, -1, (Object)null);         }
		public Entry(String pName, Checker  pSecondStage) { this(pName, -1, (Object)pSecondStage); }
		public Entry(String pName, PType    pSecondStage) { this(pName, -1, (Object)pSecondStage); }
		public Entry(String pName, PTypeRef pSecondStage) { this(pName, -1, (Object)pSecondStage); }
		
		public Entry(int pLength)                                      { this(null,  pLength, (Object)null);         }
		public Entry(String pName, int pLength)                        { this(pName, pLength, (Object)null);         }
		public Entry(String pName, int pLength, Checker  pSecondStage) { this(pName, pLength, (Object)pSecondStage); }
		public Entry(String pName, int pLength, PType    pSecondStage) { this(pName, pLength, (Object)pSecondStage); }
		public Entry(String pName, int pLength, PTypeRef pSecondStage) { this(pName, pLength, (Object)pSecondStage); }
		
		Entry(String pName, int pLength, Object pSecondStage) {
			if(pLength < 0) pLength = -1;
			Checker C = null;
			if(pSecondStage != null) {
				RegParser RP = new RegParser();
				if(     pSecondStage instanceof  Checker) RP.Entries = new RPEntry[] { RPEntry._new(pName, (Checker) pSecondStage) };
				else if(pSecondStage instanceof    PType) RP.Entries = new RPEntry[] { RPEntry._new(pName, (PType)   pSecondStage) };
				else if(pSecondStage instanceof PTypeRef) RP.Entries = new RPEntry[] { RPEntry._new(pName, (PTypeRef)pSecondStage) };
				else throw new IllegalArgumentException("Second stage must be null, Checker, PTypeRef or PType ("+pSecondStage+")."); 
				C = RP;
			}
			this.Entry = RPEntry._new(pName, CheckerAny.getCheckerAny(pLength), null, C);
		}
		
		RPEntry Entry;
		
		public String  getName()        { return              this.Entry.getName();                 }
		public int     getLength()      { return ((CheckerAny)this.Entry.getChecker()).getLength(); }
		public Checker getSecondStage() { return              this.Entry.getSecondStage();          }
		public RPEntry getRPEntry()     { return              this.Entry;                           }
	}
	
	int     Length   =    0;
	Entry[] GEntries = null;
	
	/** Returns the overall length needed by this checker */
	public int   getNeededLength() { return this.Length; }
	/** Returns the number of fixed-entry */
	public int   getEntryCount()   { return (this.GEntries == null)?0:this.GEntries.length; }
	/** Returns the entry at the index I */
	public Entry getEntry(int I)   { return ((I < 0) || (I > this.getEntryCount()))?null:this.GEntries[I]; }

	/**{@inheritDoc}*/ @Override public int     getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider)                      { return this.getStartLengthOf(S, pOffset, pProvider, null); }
	/**{@inheritDoc}*/ @Override public int     getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider, ParseResult pResult) { return this.getNeededLength(); }
	/**{@inheritDoc}*/ @Override public Checker getOptimized()                                                                              { return this;                   }
	
}
