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

import java.io.Serializable;

import net.nawaman.regparser.checkers.WordChecker;

/**
 * Regular Parser Entry
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
abstract public class RegParserEntry implements Serializable {
	
	private static final long serialVersionUID = 2457845545454544122L;
	
	public static final RegParserEntry[] EmptyRPEntryArray = new RegParserEntry[0];
	
	//== Factory =======================================================================================================
	
	static public RegParserEntry newParserEntry(Checker checker) {
		return newParserEntry(null, checker, null, null);
	}
	
	static public RegParserEntry newParserEntry(Checker checker, Quantifier quantifier) {
		return newParserEntry(null, checker, quantifier, null);
	}
	
	static public RegParserEntry newParserEntry(String name, Checker checker) {
		return newParserEntry(name, checker, null, null);
	}
	
	static public RegParserEntry newParserEntry(String name, Checker checker, Quantifier quantifier) {
		return newParserEntry(name, checker, quantifier, null);
	}
	
	static public RegParserEntry newParserEntry(
									String     name,
									Checker    checker,
									Quantifier quantifier,
									Checker    secondStage) {
		if (secondStage == null) {
			if (name == null) {
				return (quantifier == null)
				        ? new Direct(checker)
				        : new DirectWithQuantifier(checker, quantifier);
			} else {
				return (quantifier == null)
				        ? new NamedDirect(name, checker)
				        : new NamedDirectWithQuantifier(name, checker, quantifier);
			}
		}
		
		var firstStage = newParserEntry(name, checker, quantifier, null);
		return new TwoStage(firstStage, secondStage);
	}
	
	// TypeRef -------------------------------------------------------------------------------------
	
	static public RegParserEntry newParserEntry(ParserTypeRef typeRef) {
		return newParserEntry(null, typeRef, null, null);
	}
	
	static public RegParserEntry newParserEntry(ParserTypeRef typeRef, Quantifier quantifier) {
		return newParserEntry(null, typeRef, quantifier, null);
	}
	
	static public RegParserEntry newParserEntry(String name, ParserTypeRef typeRef) {
		return newParserEntry(name, typeRef, null, null);
	}
	
	static public RegParserEntry newParserEntry(String name, ParserTypeRef typeRef, Quantifier quantifier) {
		return newParserEntry(name, typeRef, quantifier, null);
	}
	
	static public RegParserEntry newParserEntry(
									String        name,
									ParserTypeRef typeRef,
									Quantifier    quantifier,
									Checker       secondStage) {
		if (secondStage == null) {
			if (name == null) {
				return (quantifier == null)
				        ? new TypeRef(typeRef)
				        : new TypeRefWithQuantifier(typeRef, quantifier);
			} else {
				return (quantifier == null)
				        ? new NamedTypeRef(name, typeRef)
				        : new NamedTypeRefWithQuantifier(name, typeRef, quantifier);
			}
		}
		
		var firstStage = newParserEntry(name, typeRef, quantifier, null);
		return new TwoStage(firstStage, secondStage);
	}
	
	// Type ----------------------------------------------------------------------------------------
	
	static public RegParserEntry newParserEntry(ParserType parserType) {
		return newParserEntry(null, parserType, null, null);
	}
	
	static public RegParserEntry newParserEntry(ParserType parserType, Quantifier quantifier) {
		return newParserEntry(null, parserType, quantifier, null);
	}
	
	static public RegParserEntry newParserEntry(String name, ParserType parserType) {
		return newParserEntry(name, parserType, null, null);
	}
	
	static public RegParserEntry newParserEntry(String name, ParserType parserType, Quantifier quantifier) {
		return newParserEntry(name, parserType, quantifier, null);
	}
	
	static public RegParserEntry newParserEntry(
									String     name,
									ParserType parserType,
									Quantifier quantifier,
									RegParser  secondStage) {
		if (secondStage == null) {
			if (name == null) {
				return (quantifier == null)
				        ? new Typed(parserType)
				        : new TypedWithQuantifier(parserType, quantifier);
			} else {
				return (quantifier == null)
				        ? new NamedTyped(name, parserType)
				        : new NamedTypedWithQuantifier(name, parserType, quantifier);
			}
		}
		
		var firstStage = newParserEntry(name, parserType, quantifier, null);
		return new TwoStage(firstStage, secondStage);
	}
	
	//== Constructor ===================================================================================================
	
	RegParserEntry() {
	}
	
	//== Functional ====================================================================================================
	
	public String name() {
		return null;
	}
	
	public Checker getChecker() {
		return null;
	}
	
	public ParserTypeRef typeRef() {
		return null;
	}
	
	public ParserType type() {
		return null;
	}
	
	public Quantifier getQuantifier() {
		return Quantifier.One;
	}
	
	public RegParser secondStage() {
		return null;
	}
	
	@Override
	public String toString() {
		StringBuffer  SB = new StringBuffer();
		String        N  = this.name();
		Checker       C  = this.getChecker();
		ParserTypeRef TR = this.typeRef();
		ParserType    T  = this.type();
		Quantifier    Q  = this.getQuantifier();
		
		if (T != null) {
			SB.append("(");
			if (N != null)
				SB.append(N).append(":");
			SB.append(T);
			SB.append(")");
			SB.append(Quantifier.toString(Q));
			
		} else
			if (TR != null) {
				SB.append("(");
				if (N != null)
					SB.append(N).append(":");
				SB.append(TR);
				SB.append(")");
				SB.append(Quantifier.toString(Q));
				
			} else
				if (C instanceof RegParser) {
					SB.append("(");
					if (N != null)
						SB.append(N).append(":~");
					if ((C instanceof WordChecker) && (!Quantifier.One.equals(Q))) {
						SB.append("(");
						SB.append(C);
						SB.append(")");
					} else
						SB.append(C);
					if (N != null)
						SB.append("~)");
					else
						SB.append(")");
					SB.append(Quantifier.toString(Q));
					
				} else
					if (N != null) {
						SB.append("(").append(N).append(":~");
						SB.append(C);
						SB.append("~)");
						SB.append(Quantifier.toString(Q));
						
					} else {
						if ((C instanceof WordChecker) && (!Quantifier.One.equals(Q))) {
							SB.append("(");
							SB.append(C);
							SB.append(")");
						} else
							SB.append(C);
						SB.append(Quantifier.toString(Q));
						
					}
		return SB.toString();
	}
	
	//== Sub Class =====================================================================================================
	
	// Non-Typed & Non-Named -------------------------------------------------------------
	
	static private class Direct extends RegParserEntry {
		
		static private final long serialVersionUID = 6546356543546354612L;
		
		protected Direct(Checker pChecker) {
			this.TheChecker = (pChecker instanceof RegParser) ? ((RegParser)pChecker).optimize() : pChecker;
		}
		
		protected Checker TheChecker = null;
		
		@Override
		public Checker getChecker() {
			return this.TheChecker;
		}
	}
	
	static private class DirectWithQuantifier extends Direct {
		
		static private final long serialVersionUID = 8351352113651352625L;
		
		protected DirectWithQuantifier(Checker pChecker, Quantifier pQuantifier) {
			super(pChecker);
			this.TheQuantifier = pQuantifier;
		}
		
		protected Quantifier TheQuantifier = null;
		
		@Override
		public Quantifier getQuantifier() {
			return this.TheQuantifier;
		}
	}
	
	// Named -----------------------------------------------------------------------------
	
	static private class NamedDirect extends Direct {
		
		static private final long serialVersionUID = 2245241556456325624L;
		
		protected NamedDirect(String pName, Checker pChecker) {
			super(pChecker);
			this.Name = pName;
		}
		
		String Name = null;
		
		@Override
		public String name() {
			return this.Name;
		}
	}
	
	static private class NamedDirectWithQuantifier extends NamedDirect {
		
		static private final long serialVersionUID = 1354655635656936565L;
		
		protected NamedDirectWithQuantifier(String pName, Checker pChecker, Quantifier pQuantifier) {
			super(pName, pChecker);
			this.TheQuantifier = pQuantifier;
		}
		
		protected Quantifier TheQuantifier = null;
		
		@Override
		public Quantifier getQuantifier() {
			return this.TheQuantifier;
		}
	}
	
	// Typed -----------------------------------------------------------------------------
	
	static private class TypeRef extends RegParserEntry {
		
		static private final long serialVersionUID = 3565652656351262366L;
		
		protected TypeRef(ParserTypeRef pRPTypeRef) {
			this.TheTypeRef = pRPTypeRef;
			if (this.TheTypeRef == null)
				throw new NullPointerException();
		}
		
		ParserTypeRef TheTypeRef = null;
		
		@Override
		public ParserTypeRef typeRef() {
			return this.TheTypeRef;
		}
	}
	
	static private class TypeRefWithQuantifier extends TypeRef {
		
		static private final long serialVersionUID = 4123563534562456523L;
		
		protected TypeRefWithQuantifier(ParserTypeRef pRPTypeRef, Quantifier pQuantifier) {
			super(pRPTypeRef);
			this.TheQuantifier = pQuantifier;
		}
		
		protected Quantifier TheQuantifier = null;
		
		@Override
		public Quantifier getQuantifier() {
			return this.TheQuantifier;
		}
	}
	
	static private class NamedTypeRef extends TypeRef {
		
		static private final long serialVersionUID = 3456412356352456523L;
		
		protected NamedTypeRef(String pName, ParserTypeRef pRPTypeRef) {
			super(pRPTypeRef);
			this.Name = pName;
		}
		
		String Name = null;
		
		@Override
		public String name() {
			return this.Name;
		}
	}
	
	static private class NamedTypeRefWithQuantifier extends NamedTypeRef {
		
		static private final long serialVersionUID = 6312456522334564535L;
		
		protected NamedTypeRefWithQuantifier(String pName, ParserTypeRef pRPTypeRef, Quantifier pQuantifier) {
			super(pName, pRPTypeRef);
			this.TheQuantifier = pQuantifier;
		}
		
		protected Quantifier TheQuantifier = null;
		
		@Override
		public Quantifier getQuantifier() {
			return this.TheQuantifier;
		}
	}
	
	// Typed -----------------------------------------------------------------------------
	
	static private class Typed extends RegParserEntry {
		
		static private final long serialVersionUID = 4566522331246354535L;
		
		protected Typed(ParserType pRPType) {
			this.TheType = pRPType;
			if (this.TheType == null)
				throw new NullPointerException();
		}
		
		ParserType TheType = null;
		
		@Override
		public ParserType type() {
			return this.TheType;
		}
	}
	
	static private class TypedWithQuantifier extends Typed {
		
		static private final long serialVersionUID = 3125454566463522335L;
		
		protected TypedWithQuantifier(ParserType pRPType, Quantifier pQuantifier) {
			super(pRPType);
			this.TheQuantifier = pQuantifier;
		}
		
		protected Quantifier TheQuantifier = null;
		
		@Override
		public Quantifier getQuantifier() {
			return this.TheQuantifier;
		}
	}
	
	static private class NamedTyped extends Typed {
		
		static private final long serialVersionUID = 6312352354545266435L;
		
		protected NamedTyped(String pName, ParserType pRPType) {
			super(pRPType);
			this.Name = pName;
		}
		
		String Name = null;
		
		@Override
		public String name() {
			return this.Name;
		}
	}
	
	static private class NamedTypedWithQuantifier extends NamedTyped {
		
		static private final long serialVersionUID = 2613235452356436455L;
		
		protected NamedTypedWithQuantifier(String pName, ParserType pRPType, Quantifier pQuantifier) {
			super(pName, pRPType);
			this.TheQuantifier = pQuantifier;
		}
		
		protected Quantifier TheQuantifier = null;
		
		@Override
		public Quantifier getQuantifier() {
			return this.TheQuantifier;
		}
	}
	
	// TwoStage --------------------------------------------------------------------------------------------------------
	
	static private class TwoStage extends RegParserEntry {
		
		static private final long serialVersionUID = 2636435413256452355L;
		
		TwoStage(RegParserEntry pDelegate, Checker pChecker) {
			this.Delegate = pDelegate;
			this.Parser   = (pChecker instanceof RegParser) ? (RegParser)pChecker : RegParser.newRegParser(pChecker);
		}
		
		RegParserEntry Delegate;
		RegParser      Parser;
		
		@Override
		public String name() {
			return this.Delegate.name();
		}
		
		@Override
		public Checker getChecker() {
			return this.Delegate.getChecker();
		}
		
		@Override
		public ParserTypeRef typeRef() {
			return this.Delegate.typeRef();
		}
		
		@Override
		public ParserType type() {
			return this.Delegate.type();
		}
		
		@Override
		public Quantifier getQuantifier() {
			return this.Delegate.getQuantifier();
		}
		
		@Override
		public RegParser secondStage() {
			return this.Parser;
		}
	}
}
