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

package net.nawaman.regparser;

import static net.nawaman.regparser.RegParser.newRegParser;

import java.io.Serializable;

import net.nawaman.regparser.result.ParseResult;

/**
 * Regular Parser Type
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
abstract public class ParserType implements Serializable {
	
	static private final long serialVersionUID = 7148744076563340787L;
	
	/** An empty array of RPType */
	static public final ParserType[] EmptyTypeArray = new ParserType[0];
	
	private PTypeRef      defaultRef   = null;
	private int           flags        = 0;
	private RegParser     parser       = null;
	private PTypeProvider typeProvider = null;
	
	/** Returns the name of the type */
	abstract public String name();
	
	/** Returns the checker for parsing the type */
	abstract public Checker checker(ParseResult hostResult, String param, PTypeProvider typeProvider);
	
	
	/** Return the default TypeRef of this type */
	public final PTypeRef typeRef() {
		return (defaultRef != null)
		        ? defaultRef
		        : (defaultRef = new PTypeRef.Simple(name(), null));
	}
	
	/** Return the default TypeRef of this type with the parameter */
	public final PTypeRef typeRef(String parameter) {
		return (parameter == null)
		        ? typeRef()
		        : new PTypeRef.Simple(name(), parameter);
	}
	
	/** Checks if this type will not record the sub-result but record as a text */
	public final boolean isText() {
		if ((flags & 0x80) != 0)
			return ((flags & 0x08) != 0);
		
		boolean isText = name().startsWith("$");
		flags = (flags | 0x80) | (isText ? 0x08 : 0x00);
		
		return isText;
	}
	
	/** Checks if the continuous text results of this type will collapse into one */
	public final boolean isCollective() {
		if ((flags & 0x40) != 0)
			return ((flags & 0x04) != 0);
		
		boolean isCollective = name().endsWith("[]");
		flags = (flags | 0x40) | (isCollective ? 0x04 : 0x00);
		
		return isCollective;
	}
	
	/**
	 * Checks if the boundary of the result of this type can be determine by the its checker alone and validation is not
	 *    mandatory to determine its length
	 **/
	public final boolean hasValidation() {
		if ((flags & 0x20) != 0)
			return ((flags & 0x02) != 0);
		
		var     name          = name();
		boolean hasValidation = name.contains("?") || name.contains("~");
		flags = (flags | 0x20) | (hasValidation ? 0x02 : 0x00);
		
		return hasValidation;
	}
	
	/**
	 * Checks if the boundary of the result of this type can be determine by the its checker alone and validation is not
	 *    mandatory to determine its length
	 **/
	public final boolean isSelfContain() {
		if ((flags & 0x10) != 0)
			return ((flags & 0x01) != 0);
		
		var     name          = name();
		boolean isSelfContain = !name.contains("~");
		flags = (flags | 0x10) | (isSelfContain ? 0x01 : 0x00);
		
		return isSelfContain;
	}
	
	/**
	 * Checks if the boundary of the result of this type can be determine by the its checker alone and validation is not
	 *    mandatory to determine its length
	 **/
	public final boolean hasFlatAlways() {
		if ((flags & 0x2000) != 0)
			return ((flags & 0x0200) != 0);
		
		var     name          = name();
		boolean hasValidation = name.contains("*");
		flags = (flags | 0x2000) | (hasValidation ? 0x0200 : 0x0000);
		
		return hasValidation;
	}
	
	/**
	 * Checks if the boundary of the result of this type can be determine by the its checker alone and validation is not
	 *    mandatory to determine its length
	 **/
	public final boolean isFlatSingle() {
		if ((flags & 0x1000) != 0)
			return ((flags & 0x0100) != 0);
		
		var     name          = name();
		boolean isSelfContain = !name.contains("+");
		flags = (flags | 0x1000) | (isSelfContain ? 0x0100 : 0x0000);
		
		return isSelfContain;
	}
	
	public final PTypeProvider typeProvider() {
		return typeProvider;
	}
	
	final void setTypeProvider(PTypeProvider typeProvider) {
		this.typeProvider = typeProvider;
	}
	
	final PTypeProvider defaultTypeProvider() {
		return typeProvider;
	}
	
	/** Returns the RegParser wrapping this type */
	public final RegParser parser() {
		if (parser == null) {
			parser = newRegParser(this);
		}
		return parser;
	}
	
	// == Parse ==
	
	/** Returns the the match if the text is start with a match or -1 if not */
	public final ParseResult parse(CharSequence text) {
		return parse(text, 0, null);
	}
	
	/** Returns the match if the text is start with a match (from pOffset on) or -1 if not */
	public final ParseResult parse(CharSequence text, int offset) {
		return parse(text, offset, null);
	}
	
	/** Returns the the match if the text is start with a match or -1 if not */
	public final ParseResult parse(CharSequence text, PTypeProvider typeProvider) {
		return parse(text, 0, typeProvider);
	}
	
	/** Returns the match if the text is start with a match (from pOffset on) or -1 if not */
	public final ParseResult parse(CharSequence text, int offset, PTypeProvider typeProvider) {
		return doParse(text, offset, typeProvider);
	}
	
	/** Returns the match if the text is start with a match (from pOffset on) or -1 if not */
	public final ParseResult doParse(CharSequence text, int offset, PTypeProvider typeProvider) {
		var provider = PTypeProvider.Library.getEither(typeProvider, this.typeProvider);
		return parser()
				.parse(text, offset, provider);
	}
	
	// Match
	
	/** Returns the match if the text is start with a match (from start to the end) or -1 if not */
	public final ParseResult match(CharSequence text) {
		int endPosition = text.length();
		return match(text, 0, endPosition, null);
	}
	
	/** Returns the match if the text is start with a match (from start to the pEndPosition) or -1 if not */
	public final ParseResult match(CharSequence text, int offset, int endPosition) {
		int end = (endPosition == -1)
		        ? text.length()
		        : endPosition;
		return match(text, offset, end, null);
	}
	
	/** Returns the match if the text is start with a match (from start to the end) or -1 if not */
	public final ParseResult match(CharSequence text, PTypeProvider typeProvider) {
		int endPosition = text.length();
		return match(text, 0, endPosition, typeProvider);
	}
	
	/** Returns the match if the text is start with a match (from start to the pEndPosition) or -1 if not */
	public final ParseResult match(CharSequence text, int offset) {
		int endPosition = text.length();
		return match(text, offset, endPosition, null);
	}
	
	/** Returns the match if the text is start with a match (from start to the end) or -1 if not */
	public final ParseResult match(CharSequence text, int offset, PTypeProvider typeProvider) {
		int endPosition = text.length();
		return match(text, offset, endPosition, typeProvider);
	}
	
	/** Returns the match if the text is start with a match (from start to the pEndPosition) or -1 if not */
	public final ParseResult match(CharSequence text, int offset, int endPosition, PTypeProvider typeProvider) {
		return doMatch(text, offset, endPosition, typeProvider);
	}
	
	/** Returns the match if the text is start with a match (from start to the pEndPosition) or -1 if not */
	protected ParseResult doMatch(CharSequence text, int offset, int endPosition, PTypeProvider typeProvider) {
		var provider = PTypeProvider.Library.getEither(typeProvider, this.typeProvider);
		int end      = (endPosition == -1)
		             ? text.length()
		             : endPosition;
		return parser().match(text, offset, end, provider);
	}
	
	// Validation ----------------------------------------------------------------------------------
	
	/** Returns a display string that represent a validation code */
	public String validation() {
		return "...";
	}
	
	/** Validate the parse result */
	public final boolean validate(
							ParseResult   hostResult,
							ParseResult   thisResult,
							String        parameter,
							PTypeProvider typeProvider) {
		var provider = PTypeProvider.Library.getEither(typeProvider, this.typeProvider);
		return doValidate(hostResult, thisResult, parameter, provider);
	}
	
	/** Validate the parse result */
	protected boolean doValidate(
							ParseResult   hostResult,
							ParseResult   thisResult,
							String        parameter,
							PTypeProvider typeProvider) {
		return true;
	}
	
	// Compilation ---------------------------------------------------------------------------------
	
	/** Returns a display string that represent a compilation code */
	public String compilation() {
		return "...";
	}
	
	
	/** Compiles a ParseResult in to an object */
	public final Object compile(String text) {
		var thisResult = newRegParser(this).match(text);
		if (thisResult == null)
			return null;
		
		return compile(thisResult, 0, null, null, null);
	}
	
	/** Compiles a ParseResult in to an object */
	public final Object compile(String text, PTypeProvider typeProvider) {
		var thisResult = newRegParser(this).match(text, typeProvider);
		if (thisResult == null)
			return null;
		
		return compile(thisResult, 0, null, null, typeProvider);
	}
	
	/** Compiles a ParseResult in to an object */
	public final Object compile(String text, String parameter, PTypeProvider typeProvider) {
		return compile(text, parameter, null, typeProvider);
	}
	
	/** Compiles a ParseResult in to an object */
	public final Object compile(
							String             text,
							String             parameter,
							CompilationContext compilationContext,
							PTypeProvider      typeProvider) {
		RegParser regParser = null;
		
		if (parameter == null) {
			regParser = newRegParser(this);
		} else {
			// The provide does not hold this type
			if (typeProvider.type(name()) == null) {
				// Add it in
				var newProvider = new PTypeProvider.Extensible();
				var newLibrary  = new PTypeProvider.Library(typeProvider, newProvider);
				((PTypeProvider.Extensible)newProvider).addRPType(this);
				typeProvider = newLibrary;
			}
			var typeRef = new PTypeRef.Simple(name(), parameter);
			regParser = newRegParser(typeRef);
		}
		
		var thisResult = regParser.match(text, typeProvider);
		if (thisResult == null)
			return null;
		
		return compile(thisResult, 0, parameter, compilationContext, typeProvider);
	}
	
	// Parse from Result -----------------------------------------------------------------------------------------------
	
	/** Compiles a ParseResult in to an object */
	public final Object compile(ParseResult thisResult) {
		return compile(thisResult, 0, null, null, null);
	}
	
	/** Compiles a ParseResult in to an object */
	public final Object compile(ParseResult thisResult, PTypeProvider typeProvider) {
		return compile(thisResult, 0, null, null, typeProvider);
	}
	
	/** Compiles a ParseResult in to an object with a parameter */
	public final Object compile(
							ParseResult        thisResult,
							String             parameter,
							CompilationContext compilationContext,
							PTypeProvider      typeProvider) {
		return compile(thisResult, 0, null, compilationContext, typeProvider);
	}
	
	/** Compiles a ParseResult in to an object with a parameter */
	public final Object compile(
							ParseResult        thisResult,
							int                entryIndex,
							String             parameter,
							CompilationContext compilationContext,
							PTypeProvider      typeProvider) {
		var provider = PTypeProvider.Library.getEither(typeProvider, this.typeProvider);
		return doCompile(thisResult, entryIndex, parameter, compilationContext, provider);
	}
	
	/** Compiles a ParseResult in to an object with a parameter */
	protected Object doCompile(
						ParseResult        thisResult,
						int                entryIndex,
						String             parameter,
						CompilationContext compilationContext,
						PTypeProvider      typeProvider) {
		return (thisResult == null)
		        ? null
		        : thisResult.textOf(entryIndex);
	}
	
	// Object --------------------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return "!" + name() + "!";
	}
	
}
