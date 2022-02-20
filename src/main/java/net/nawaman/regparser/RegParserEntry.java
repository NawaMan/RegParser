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

package net.nawaman.regparser;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Objects;

import net.nawaman.regparser.checkers.WordChecker;

/**
 * Regular Parser Entry
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
abstract public class RegParserEntry implements AsRegParserEntry, Quantifiable<RegParserEntry>, Serializable {
	
	private static final long serialVersionUID = 2457845545454544122L;
	
	public static final RegParserEntry[] EmptyRegParserEntryArray = new RegParserEntry[0];
	
	//== Factory =======================================================================================================
	
	static public RegParserEntry newParserEntry(AsChecker checker) {
		return newParserEntry(null, checker, null, null);
	}
	
	static public RegParserEntry newParserEntry(AsChecker checker, Quantifier quantifier) {
		return newParserEntry(null, checker, quantifier, null);
	}
	
	static public RegParserEntry newParserEntry(String name, AsChecker checker) {
		return newParserEntry(name, checker, null, null);
	}
	
	static public RegParserEntry newParserEntry(String name, AsChecker checker, Quantifier quantifier) {
		return newParserEntry(name, checker, quantifier, null);
	}
	
	static public RegParserEntry newParserEntry(
									String     name,
									AsChecker  checker,
									Quantifier quantifier,
									AsChecker  secondStage) {
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
		return new TwoStage(firstStage, secondStage.asChecker());
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
									AsChecker     secondStage) {
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
		return new TwoStage(firstStage, secondStage.asChecker());
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
									AsChecker  secondStage) {
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
		return new TwoStage(firstStage, secondStage.asChecker());
	}
	
	//== Builder =======================================================================================================
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static Builder newParserEntry() {
		return new Builder();
	}
	
	public static class Builder {
		
		private String name;
		
		private Quantifier quantifier;
		
		private RegParser secondStage;
		
		private Object parser;
		
		public Builder name(String name) {
			this.name = requireNonNull(name, "`name` cannot be null.");
			return this;
		}
		
		public Builder checker(Checker checker) {
			this.parser = requireNonNull(checker, "`checker` cannot be null.");
			return this;
		}
		
		public Builder typeRef(ParserTypeRef typeRef) {
			this.parser = requireNonNull(typeRef, "`typeRef` cannot be null.");
			return this;
		}
		
		public Builder type(ParserType type) {
			this.parser = requireNonNull(type, "`type` cannot be null.");
			return this;
		}
		
		public Builder checkerOrNull(Checker checker) {
			this.parser = checker;
			return this;
		}
		
		public Builder typeRefOrNull(ParserTypeRef typeRef) {
			this.parser = typeRef;
			return this;
		}
		
		public Builder typeOrNull(ParserType type) {
			this.parser = type;
			return this;
		}
		
		public Builder quantifier(Quantifier quantifier) {
			this.quantifier = requireNonNull(quantifier, "`quantifier` cannot be null.");
			return this;
		}
		
		public Builder secondStage(RegParser secondStage) {
			this.secondStage = secondStage;
			return this;
		}
		
		public RegParserEntry build() {
			if (parser instanceof ParserType) {
				return newParserEntry(name, (ParserType)parser, quantifier, secondStage);
			}
			if (parser instanceof ParserTypeRef) {
				return newParserEntry(name, (ParserTypeRef)parser, quantifier, secondStage);
			}
			if (parser instanceof Checker) {
				return newParserEntry(name, (Checker)parser, quantifier, secondStage);
			}
			
			throw new IllegalStateException("Missing parser: " + name);
		}
		
	}
	
	//== Constructor ===================================================================================================
	
	RegParserEntry() {
	}
	
	//== Functional ====================================================================================================
	
	public RegParserEntry asRegParserEntry() {
		return this;
	}
	
	public String name() {
		return null;
	}
	
	public Checker checker() {
		return null;
	}
	
	public ParserTypeRef typeRef() {
		return null;
	}
	
	public ParserType type() {
		return null;
	}
	
	public Quantifier quantifier() {
		return Quantifier.One;
	}
	
	public RegParser secondStage() {
		return null;
	}
	
	@Override
	public String toString() {
		var buffer     = new StringBuffer();
		var name       = name();
		var checker    = checker();
		var typeRef    = typeRef();
		var type       = type();
		var quantifier = quantifier();
		
		if (type != null) {
			buffer.append("(");
			if (name != null) {
				buffer.append(name).append(":");
			}
			buffer.append(type);
			buffer.append(")");
			buffer.append(Quantifier.toString(quantifier));
			
		} else if (typeRef != null) {
			buffer.append("(");
			if (name != null) {
				buffer.append(name).append(":");
			}
			buffer.append(typeRef);
			buffer.append(")");
			buffer.append(Quantifier.toString(quantifier));
			
		} else if (checker instanceof RegParser) {
			buffer.append("(");
			if (name != null) {
				buffer.append(name).append(":~");
			}
			if ((checker instanceof WordChecker) && (!Quantifier.One.equals(quantifier))) {
				buffer.append("(");
				buffer.append(checker);
				buffer.append(")");
			} else {
				buffer.append(checker);
			}
			if (name != null)
				buffer.append("~)");
			else {
				buffer.append(")");
			}
			buffer.append(Quantifier.toString(quantifier));
			
		} else if (name != null) {
			buffer.append("(").append(name).append(":~");
			buffer.append(checker);
			buffer.append("~)");
			buffer.append(Quantifier.toString(quantifier));
			
		} else {
			if ((checker instanceof WordChecker) && (!Quantifier.One.equals(quantifier))) {
				buffer.append("(");
				buffer.append(checker);
				buffer.append(")");
			} else {
				buffer.append(checker);
			}
			buffer.append(Quantifier.toString(quantifier));
			
		}
		return buffer.toString();
	}
	
	//== Sub Class =====================================================================================================
	
	// Non-Typed & Non-Named -------------------------------------------------------------
	
	static private class Direct extends RegParserEntry {
		
		static private final long serialVersionUID = 6546356543546354612L;
		
		private final Checker checker;
		
		Direct(AsChecker checker) {
			this.checker = (checker instanceof RegParser)
			             ? ((RegParser)checker).optimize()
			             : checker.asChecker();
			Objects.requireNonNull(this.checker, "`checker` cannot be null.");
		}
		
		@Override
		public Checker checker() {
			return checker;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			return (quantifier == null)
			        ? this
			        : new DirectWithQuantifier(checker, quantifier);
		}
		
		@Override
		public Boolean isDeterministic() {
			return checker.isDeterministic();
		}
		
		@Override
		public String toString() {
			return checker.toString();
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(Direct.class, checker);
		}
	}
	
	static private class DirectWithQuantifier extends Direct {
		
		static private final long serialVersionUID = 8351352113651352625L;
		
		private final Quantifier quantifier;
		
		DirectWithQuantifier(AsChecker checker, Quantifier quantifier) {
			super(checker);
			this.quantifier = quantifier;
		}
		
		@Override
		public Quantifier quantifier() {
			return quantifier;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			var checker = checker();
			return (quantifier == null)
			        ? new Direct              (checker)
			        : new DirectWithQuantifier(checker, quantifier);
		}
		
		@Override
		public Boolean isDeterministic() {
			return super.isDeterministic() && quantifier.isPossessive();
		}
		
		@Override
		public String toString() {
			return checker().toString() + quantifier;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(DirectWithQuantifier.class, checker(), quantifier);
		}
	}
	
	// Named -----------------------------------------------------------------------------
	
	static private class NamedDirect extends Direct {
		
		static private final long serialVersionUID = 2245241556456325624L;
		
		private final String name;
		
		NamedDirect(String name, AsChecker checker) {
			super(checker);
			this.name = Objects.requireNonNull(name);
		}
		
		@Override
		public String name() {
			return name;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			var checker = checker();
			return (quantifier == null)
			        ? this
			        : new NamedDirectWithQuantifier(name, checker, quantifier);
		}
		
		@Override
		public String toString() {
			return "(" + name + ":~" + checker().toString() + "~)";
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(NamedDirect.class, name, checker());
		}
	}
	
	static private class NamedDirectWithQuantifier extends NamedDirect {
		
		static private final long serialVersionUID = 1354655635656936565L;
		
		private final Quantifier quantifier;
		
		NamedDirectWithQuantifier(String name, AsChecker checker, Quantifier quantifier) {
			super(name, checker);
			this.quantifier = quantifier;
		}
		
		@Override
		public Quantifier quantifier() {
			return quantifier;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			var name    = name();
			var checker = checker();
			return (quantifier == null)
			        ? new NamedDirect              (name, checker)
			        : new NamedDirectWithQuantifier(name, checker, quantifier);
		}
		
		@Override
		public Boolean isDeterministic() {
			return super.isDeterministic() && quantifier.isPossessive();
		}
		
		@Override
		public String toString() {
			return "(" + name() + ":~" + checker().toString() + "~)" + quantifier;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(NamedDirect.class, name(), checker(), quantifier);
		}
	}
	
	// Typed -----------------------------------------------------------------------------
	
	static private class TypeRef extends RegParserEntry {
		
		static private final long serialVersionUID = 3565652656351262366L;
		
		private final ParserTypeRef typeRef;
		
		TypeRef(ParserTypeRef typeRef) {
			this.typeRef = Objects.requireNonNull(typeRef);
		}
		
		@Override
		public ParserTypeRef typeRef() {
			return typeRef;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			return (quantifier == null)
			        ? this
			        : new TypeRefWithQuantifier(typeRef, quantifier);
		}
		
		@Override
		public Boolean isDeterministic() {
			return null;
		}
		
		@Override
		public String toString() {
			return "(" + typeRef.toString() +")";
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(TypeRef.class, typeRef);
		}
	}
	
	static private class TypeRefWithQuantifier extends TypeRef {
		
		static private final long serialVersionUID = 4123563534562456523L;
		
		private final Quantifier quantifier;
		
		TypeRefWithQuantifier(ParserTypeRef typeRef, Quantifier quantifier) {
			super(typeRef);
			this.quantifier = quantifier;
		}
		
		@Override
		public Quantifier quantifier() {
			return quantifier;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			var typeRef = typeRef();
			return (quantifier == null)
			        ? new TypeRef              (typeRef)
			        : new TypeRefWithQuantifier(typeRef, quantifier);
		}
		
		@Override
		public final Boolean isDeterministic() {
			return !quantifier.isPossessive() ? false : null;
		}
		
		@Override
		public String toString() {
			return super.toString() + quantifier;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(TypeRef.class, typeRef(), quantifier);
		}
	}
	
	static private class NamedTypeRef extends TypeRef {
		
		static private final long serialVersionUID = 3456412356352456523L;
		
		private String name;
		
		NamedTypeRef(String name, ParserTypeRef typeRef) {
			super(typeRef);
			this.name = name;
		}
		
		@Override
		public String name() {
			return name;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			var typeRef = typeRef();
			return (quantifier == null)
			        ? this
			        : new NamedTypeRefWithQuantifier(name, typeRef, quantifier);
		}
		
		@Override
		public String toString() {
			return "(" + name + ":" + typeRef() + ")";
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(NamedTypeRef.class, typeRef(), name);
		}
	}
	
	static private class NamedTypeRefWithQuantifier extends NamedTypeRef {
		
		static private final long serialVersionUID = 6312456522334564535L;
		
		private final Quantifier quantifier;
		
		NamedTypeRefWithQuantifier(String name, ParserTypeRef typeRef, Quantifier quantifier) {
			super(name, typeRef);
			this.quantifier = quantifier;
		}
		
		@Override
		public Quantifier quantifier() {
			return quantifier;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			var name    = name();
			var typeRef = typeRef();
			return (quantifier == null)
			        ? new NamedTypeRef              (name, typeRef)
			        : new NamedTypeRefWithQuantifier(name, typeRef, quantifier);
		}
		
		@Override
		public final Boolean isDeterministic() {
			return !quantifier.isPossessive() ? false : null;
		}
		
		@Override
		public String toString() {
			return "(" + name() + ":" + typeRef() + ")" + quantifier;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(NamedTypeRefWithQuantifier.class, typeRef(), name(), quantifier);
		}
	}
	
	// Typed -----------------------------------------------------------------------------
	
	static private class Typed extends RegParserEntry {
		
		static private final long serialVersionUID = 4566522331246354535L;
		
		private final ParserType type;
		
		Typed(ParserType type) {
			this.type = Objects.requireNonNull(type);
		}
		
		@Override
		public ParserType type() {
			return type;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			var type = type();
			return (quantifier == null)
			        ? this
			        : new TypedWithQuantifier(type, quantifier);
		}
		
		@Override
		public Boolean isDeterministic() {
			return null;
		}
		
		@Override
		public String toString() {
			return type.toString();
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(Typed.class, type);
		}
	}
	
	static private class TypedWithQuantifier extends Typed {
		
		static private final long serialVersionUID = 3125454566463522335L;
		
		private final Quantifier quantifier;
		
		protected TypedWithQuantifier(ParserType type, Quantifier quantifier) {
			super(type);
			this.quantifier = quantifier;
		}
		
		@Override
		public Quantifier quantifier() {
			return quantifier;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			var type = type();
			return (quantifier == null)
			        ? new Typed              (type)
			        : new TypedWithQuantifier(type, quantifier);
		}
		
		@Override
		public final Boolean isDeterministic() {
			return !quantifier.isPossessive() ? false : null;
		}
		
		@Override
		public String toString() {
			return super.toString() + quantifier;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(TypedWithQuantifier.class, typeRef(), quantifier);
		}
	}
	
	static private class NamedTyped extends Typed {
		
		static private final long serialVersionUID = 6312352354545266435L;
		
		private final String name;
		
		protected NamedTyped(String name, ParserType type) {
			super(type);
			this.name = name;
		}
		
		@Override
		public String name() {
			return name;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			var type = type();
			return (quantifier == null)
			        ? this
			        : new NamedTypedWithQuantifier(name, type, quantifier);
		}
		
		@Override
		public String toString() {
			return "(" + name + ":" + type() + ")";
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(NamedTyped.class, name, type());
		}
	}
	
	static private class NamedTypedWithQuantifier extends NamedTyped {
		
		static private final long serialVersionUID = 2613235452356436455L;
		
		private final Quantifier quantifier;
		
		NamedTypedWithQuantifier(String name, ParserType type, Quantifier quantifier) {
			super(name, type);
			this.quantifier = quantifier;
		}
		
		@Override
		public Quantifier quantifier() {
			return quantifier;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			var name = name();
			var type = type();
			return (quantifier == null)
			        ? new NamedTyped              (name, type)
			        : new NamedTypedWithQuantifier(name, type, quantifier);
		}
		
		@Override
		public final Boolean isDeterministic() {
			return !quantifier.isPossessive() ? false : null;
		}
		
		@Override
		public String toString() {
			return "(" + name() + ":" + type() + ")" + quantifier;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(NamedTypedWithQuantifier.class, name(), type(), quantifier);
		}
	}
	
	// TwoStage --------------------------------------------------------------------------------------------------------
	
	static private class TwoStage extends RegParserEntry {
		
		static private final long serialVersionUID = 2636435413256452355L;
		
		private final RegParserEntry delegate;
		private final RegParser      parser;
		
		TwoStage(RegParserEntry delegate, Checker checker) {
			this.delegate = Objects.requireNonNull(delegate);
			this.parser   = (checker instanceof RegParser)
			              ? (RegParser)checker
			              : RegParser.newRegParser(checker);
			Objects.requireNonNull(this.parser);
		}
		
		@Override
		public String name() {
			return delegate.name();
		}
		
		@Override
		public Checker checker() {
			return delegate.checker();
		}
		
		@Override
		public ParserTypeRef typeRef() {
			return delegate.typeRef();
		}
		
		@Override
		public ParserType type() {
			return delegate.type();
		}
		
		@Override
		public Quantifier quantifier() {
			return delegate.quantifier();
		}
		
		@Override
		public final Boolean isDeterministic() {
			return !delegate.quantifier().isPossessive() ? false : delegate.checker().isDeterministic();
		}
		
		@Override
		public RegParser secondStage() {
			return parser;
		}
		
		public RegParserEntry quantifier(Quantifier quantifier) {
			return new TwoStage(delegate.quantifier(quantifier), parser);
		}
		
		@Override
		public String toString() {
			return "(" + delegate + ":" + parser + ")";
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(delegate, parser);
		}
	}
	
}
