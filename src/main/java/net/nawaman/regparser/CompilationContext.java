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

import static java.lang.String.format;

/**
 * Compilation Context
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public interface CompilationContext {
	
	/** Returns the position as a string */
	public String getLocationAsString(int offset);
	
	/** Reports a warning with a cause*/
	public void reportWarning(String message, Throwable cause);
	
	/** Reports an error with a cause */
	public void reportError(String message, Throwable cause);
	
	/** Reports a fatal error with a cause */
	public void reportFatalError(String message, Throwable cause);
	
	/** Reports a warning with a cause*/
	public void reportWarning(String message, Throwable cause, int offset);
	
	/** Reports an error with a cause */
	public void reportError(String message, Throwable cause, int offset);
	
	/** Reports a fatal error with a cause */
	public void reportFatalError(String message, Throwable cause, int offset);
	
	/** A simple implementation of Context */
	static public class Simple implements CompilationContext {
		
		public Simple() {
			this(true);
		}
		
		public Simple(boolean useErrorPrintStreamForWarning) {
			this.useErrorPrintStreamForWarning = useErrorPrintStreamForWarning;
		}
		
		private boolean useErrorPrintStreamForWarning = true;
		
		/** Returns the position as a string */
		public String getLocationAsString(int offset) {
			return String.format("Near [%s]", offset);
		}
		
		/** Reports a warning with a cause*/
		public void reportWarning(String message, Throwable cause) {
			try (var printStream = (this.useErrorPrintStreamForWarning) ? System.err : System.out) {
				printStream.println("WARNING: " + message);
				if (cause != null) {
					cause.printStackTrace(printStream);
				}
			}
		}
		
		/** Reports an error with a cause */
		public void reportError(String message, Throwable cause) {
			System.err.println("ERROR: " + message);
			if (cause != null) {
				cause.printStackTrace(System.err);
			}
		}
		
		/** Reports a fatal error with a cause */
		public void reportFatalError(String message, Throwable cause) {
			System.err.println("FATAL ERROR: " + message);
			if (cause != null) {
				cause.printStackTrace(System.err);
			}
		}
		
		/** Reports a warning with a cause*/
		public void reportWarning(String message, Throwable cause, int offset) {
			var location     = (offset < 0) ? "" : format(" near %s", this.getLocationAsString(offset));
			var errorMessage = format("WARNING: %s%s", message, location);
			try (var printStream = (this.useErrorPrintStreamForWarning) ? System.err : System.out) {
				printStream.println(errorMessage);
				if (cause != null) {
					cause.printStackTrace(printStream);
				}
			}
		}
		
		/** Reports an error with a cause */
		public void reportError(String message, Throwable cause, int offset) {
			var location     = (offset < 0) ? "" : format(" near %s", this.getLocationAsString(offset));
			var errorMessage = format("ERROR: %s%s", message, location);
			System.err.println(errorMessage);
			if (cause != null) {
				cause.printStackTrace(System.err);
			}
		}
		
		/** Reports a fatal error with a cause */
		public void reportFatalError(String message, Throwable cause, int offset) {
			var location     = (offset < 0) ? "" : format(" near %s", this.getLocationAsString(offset));
			var errorMessage = format("FATAL ERROR: %s%s", message, location);
			System.err.println(errorMessage);
			if (cause != null) {
				cause.printStackTrace(System.err);
			}
		}
		
		/** Returns the string representation of this compilation context */
		@Override
		public String toString() {
			return "CompilationContext$Simple:" + this.hashCode();
		}
	}
}
