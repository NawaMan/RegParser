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
 * package. You can inform me via nawa<at>nawaman<dot>net.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */
package net.nawaman.regparser;

import static net.nawaman.regparser.RegParserEntry.newParserEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * The regular parser builder.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class RegParserBuilder {
	
	private ParserTypeProvider typeProvider = null;
	
	private final List<RegParserEntry> entries = new ArrayList<RegParserEntry>();
	
	
	public RegParserBuilder typeProvider(ParserTypeProvider typeProvider) {
		this.typeProvider = typeProvider;
		return this;
	}
	
	public RegParserBuilder entry(RegParserEntry entry) {
		entries.add(entry);
		return this;
	}
	
	public RegParserBuilder entry(Checker checker) {
		return entry(null, checker, null, null);
	}
	
	public RegParserBuilder entry(Checker checker, Checker secondStage) {
		return entry(RegParserEntry.newParserEntry(null, checker, null, secondStage));
	}
	
	public RegParserBuilder entry(Checker checker, Quantifier quantifier) {
		return entry(null, checker, quantifier, null);
	}
	
	public RegParserBuilder entry(Checker checker, Quantifier quantifier, Checker secondStage) {
		return entry(null, checker, quantifier, secondStage);
	}
	
	public RegParserBuilder entry(String name, Checker checker) {
		return entry(name, checker, null, null);
	}
	
	public RegParserBuilder entry(String name, Checker checker, Checker secondStage) {
		return entry(name, checker, null, secondStage);
	}
	
	public RegParserBuilder entry(String name, Checker checker, Quantifier quantifier) {
		return entry(name, checker, quantifier, null);
	}
	
	public RegParserBuilder entry(String name, Checker checker, Quantifier quantifier, Checker secondStage) {
		return entry(newParserEntry(name, checker, quantifier, secondStage));
	}
	
	public RegParserBuilder entry(ParserType type) {
		return entry(null, type, null, null);
	}
	
	public RegParserBuilder entry(ParserType type, Checker secondStage) {
		return entry(null, type, null, secondStage);
	}
	
	public RegParserBuilder entry(ParserType type, Quantifier quantifier) {
		return entry(null, type, quantifier, null);
	}
	
	public RegParserBuilder entry(ParserType type, Quantifier quantifier, Checker secondStage) {
		return entry(null, type, quantifier, secondStage);
	}
	
	public RegParserBuilder entry(String name, ParserType type) {
		return entry(name, type, null, null);
	}
	
	public RegParserBuilder entry(String name, ParserType type, Checker secondStage) {
		return entry(name, type, null, secondStage);
	}
	
	public RegParserBuilder entry(String name, ParserType type, Quantifier quantifier) {
		return entry(name, type, quantifier, null);
	}
	
	public RegParserBuilder entry(String name, ParserType type, Quantifier quantifier, Checker secondStage) {
		return entry(newParserEntry(name, type, quantifier, secondStage));
	}
	
	public RegParserBuilder entry(ParserTypeRef typeRef) {
		return entry(null, typeRef, null, null);
	}
	
	public RegParserBuilder entry(ParserTypeRef typeRef, Checker secondStage) {
		return entry(null, typeRef, null, secondStage);
	}
	
	public RegParserBuilder entry(ParserTypeRef typeRef, Quantifier quantifier) {
		return entry(null, typeRef, quantifier, null);
	}
	
	public RegParserBuilder entry(ParserTypeRef typeRef, Quantifier quantifier, Checker secondStage) {
		return entry(null, typeRef, quantifier, secondStage);
	}
	
	public RegParserBuilder entry(String name, ParserTypeRef typeRef) {
		return entry(name, typeRef, null, null);
	}
	
	public RegParserBuilder entry(String name, ParserTypeRef typeRef, Checker secondStage) {
		return entry(name, typeRef, null, secondStage);
	}
	
	public RegParserBuilder entry(String name, ParserTypeRef typeRef, Quantifier quantifier) {
		return entry(name, typeRef, quantifier, null);
	}
	
	public RegParserBuilder entry(String name, ParserTypeRef typeRef, Quantifier quantifier, Checker secondStage) {
		return entry(newParserEntry(name, typeRef, quantifier, secondStage));
	}
	
	public RegParser build() {
		return RegParser.newRegParser(typeProvider, entries);
	}
	
}
