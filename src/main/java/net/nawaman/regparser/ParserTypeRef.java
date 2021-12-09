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

import static net.nawaman.regparser.utils.Util.escapeText;

import java.io.Serializable;

/**
 * Reference to a type
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
abstract public class ParserTypeRef implements Serializable {
	
	private static final long serialVersionUID = -2335767886881850411L;
	
	public static ParserTypeRef of(String typeName) {
		return new Simple(typeName, null);
	}
	
	public static ParserTypeRef of(String typeName, String parameter) {
		return new Simple(typeName, parameter);
	}
	
	abstract public String name();
	
	public String parameter() {
		return null;
	}
	
	static public class Simple extends ParserTypeRef {
		
		private static final long serialVersionUID = -1749338159178519582L;
		
		public Simple(String typeName) {
			this(typeName, null);
		}
		
		public Simple(String typeName, String parameter) {
			this.typeName  = typeName;
			this.parameter = parameter;
		}
		
		private final String typeName;
		private final String parameter;
		
		/**{@inheritDoc}*/
		@Override
		public String name() {
			return typeName;
		}
		
		/**{@inheritDoc}*/
		@Override
		public String parameter() {
			return parameter;
		}
	}
	
	// Object --------------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/
	@Override
	public String toString() {
		var name      = name();
		var parameter = parameter();
		var param     = (parameter == null) ? "" : ("(\"" + escapeText(parameter) + "\")");
		return "!" + name + param + "!";
	}
	
	public String toDetail() {
		return toString();
	}
	
	/**{@inheritDoc}*/
	@Override
	public boolean equals(Object O) {
		if (!(O instanceof ParserTypeRef))
			return false;
		
		var thisName = this.name();
		var thatName = ((ParserTypeRef)O).name();
		if ((thisName != thatName)
		|| ((thisName != null) && !thisName.equals(thatName)))
			return false;
		
		var thisParameter = parameter();
		var thatParameter = ((ParserTypeRef)O).parameter();
		if ((thisParameter != thatParameter)
		 || ((thisParameter != null) && !thisParameter.equals(thatParameter)))
			return false;
		
		return true;
	}
}
