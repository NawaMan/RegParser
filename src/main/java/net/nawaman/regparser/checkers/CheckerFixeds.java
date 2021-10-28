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

package net.nawaman.regparser.checkers;

import java.util.ArrayList;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.PType;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.PTypeRef;
import net.nawaman.regparser.RPEntry;
import net.nawaman.regparser.RegParser;
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
    
    /** Constructs a checker fixed */
    public CheckerFixeds(Entry... entries) {
        if ((entries == null) || (entries.length == 0)) {
            return;
        }
        int totalLength       = 0;
        var list = new ArrayList<Entry>();
        for (int i = 0; i < entries.length; i++) {
            var entry = entries[i];
            if (entry == null) {
                continue;
            }
            list.add(entry);
            int length = entry.length();
            if (length == -1) {
                if (i != (entries.length - 1)) {
                    throw new IllegalArgumentException(
                            "Length cannot be negative (" + length + "), unless it is the last entry.");
                }
            } else {
                totalLength += length;
            }
        }
        if (list.size() == 0) {
            return;
        }
        this.entries = list.toArray(new Entry[list.size()]);
        this.length  = totalLength;
    }
    
    /** The group entries */
    static public class Entry {
        public Entry() {
            this(null, -1, (Object) null);
        }
        
        public Entry(String name) {
            this(name, -1, (Object) null);
        }
        
        public Entry(String name, Checker secondStage) {
            this(name, -1, (Object) secondStage);
        }
        
        public Entry(String name, PType secondStage) {
            this(name, -1, (Object) secondStage);
        }
        
        public Entry(String name, PTypeRef secondStage) {
            this(name, -1, (Object) secondStage);
        }
        
        public Entry(int length) {
            this(null, length, (Object) null);
        }
        
        public Entry(String name, int length) {
            this(name, length, (Object) null);
        }
        
        public Entry(String name, int length, Checker secondStage) {
            this(name, length, (Object) secondStage);
        }
        
        public Entry(String name, int length, PType secondStage) {
            this(name, length, (Object) secondStage);
        }
        
        public Entry(String name, int length, PTypeRef secondStage) {
            this(name, length, (Object) secondStage);
        }
        
        Entry(String name, int length, Object secondStage) {
            if (length < 0) {
                length = -1;
            }
            Checker C = null;
            if (secondStage != null) {
                RPEntry[] entries;
                if (secondStage instanceof Checker) {
                    entries = new RPEntry[] { RPEntry._new(name, (Checker) secondStage) };
                } else if (secondStage instanceof PType) {
                    entries = new RPEntry[] { RPEntry._new(name, (PType) secondStage) };
                } else if (secondStage instanceof PTypeRef) {
                    entries = new RPEntry[] { RPEntry._new(name, (PTypeRef) secondStage) };
                } else {
                    var errMsg = "Second stage must be null, Checker, PTypeRef or PType (" + secondStage + ").";
                    throw new IllegalArgumentException(errMsg);
                }
                C = new RegParser(entries);
            }
            this.entry = RPEntry._new(name, CheckerAny.getCheckerAny(length), null, C);
        }
        
        private final RPEntry entry;
        
        public String name() {
            return this.entry.name();
        }
        
        public int length() {
            return ((CheckerAny) this.entry.getChecker()).length();
        }
        
        public Checker secondStage() {
            return this.entry.secondStage();
        }
        
        public RPEntry entry() {
            return this.entry;
        }
    }
    
    private int     length   = 0;
    private Entry[] entries = null;
    
    /** Returns the overall length needed by this checker */
    public int neededLength() {
        return this.length;
    }
    
    /** Returns the number of fixed-entry */
    public int entryCount() {
        return (this.entries == null) ? 0 : this.entries.length;
    }
    
    /** Returns the entry at the index I */
    public Entry entry(int I) {
        return ((I < 0) || (I > this.entryCount())) ? null : this.entries[I];
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, PTypeProvider typeProvider) {
        return this.startLengthOf(text, offset, typeProvider, null);
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, PTypeProvider typeProvider, ParseResult parseResult) {
        return this.neededLength();
    }
    
    @Override
    public Checker optimize() {
        return this;
    }
    
}
