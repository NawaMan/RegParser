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

/**
 * Reference to a type
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
abstract public class ParserTypeRef implements Serializable {
	
	private static final long serialVersionUID = -2335767886881850411L;
	
	abstract public String name();
	
	public String parameter() {
		return null;
	}
	
	static public class Simple extends ParserTypeRef {
		
		private static final long serialVersionUID = -1749338159178519582L;
		
		public Simple(String pTypeName) {
			this.TypeName = pTypeName;
		}
		
		public Simple(String pTypeName, String pParam) {
			this.TypeName = pTypeName;
			this.Param    = pParam;
		}
		
		String TypeName;
		String Param;
		
		/**{@inheritDoc}*/
		@Override
		public String name() {
			return this.TypeName;
		}
		
		/**{@inheritDoc}*/
		@Override
		public String parameter() {
			return this.Param;
		}
	}
	
	// Object --------------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/
	@Override
	public String toString() {
		return "!" + this.name()
		        + ((this.parameter() == null) ? "" : ("(\"" + Util.escapeText(this.parameter()) + "\")")) + "!";
	}
	
	public String toDetail() {
		return this.toString();
	}
	
	/**{@inheritDoc}*/
	@Override
	public boolean equals(Object O) {
		if (!(O instanceof ParserTypeRef))
			return false;
		
		String TN = this.name();
		String ON = ((ParserTypeRef)O).name();
		if ((TN != ON) || ((TN != null) && !TN.equals(ON)))
			return false;
		
		String TP = this.parameter();
		String OP = ((ParserTypeRef)O).parameter();
		if ((TP != OP) || ((TP != null) && !TP.equals(OP)))
			return false;
		
		return true;
	}
}
