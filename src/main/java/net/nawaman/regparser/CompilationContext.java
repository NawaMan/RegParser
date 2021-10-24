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

/**
 * Compilation Context
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public interface CompilationContext {
    
    /** Returns the position as a string */
    public String getLocationAsString(int pPos);
    
    /** Reports a warning with a cause*/
    public void reportWarning(String pMessage, Throwable pCause);
    
    /** Reports an error with a cause */
    public void reportError(String pMessage, Throwable pCause);
    
    /** Reports a fatal error with a cause */
    public void reportFatalError(String pMessage, Throwable pCause);
    
    /** Reports a warning with a cause*/
    public void reportWarning(String pMessage, Throwable pCause, int pPos);
    
    /** Reports an error with a cause */
    public void reportError(String pMessage, Throwable pCause, int pPos);
    
    /** Reports a fatal error with a cause */
    public void reportFatalError(String pMessage, Throwable pCause, int pPos);
    
    /** A simple implementation of Context */
    static public class Simple implements CompilationContext {
        
        public Simple() {
            this(true);
        }
        
        public Simple(boolean pUseErrorPrintStreamForWarning) {
            this.UseErrorPrintStreamForWarning = pUseErrorPrintStreamForWarning;
        }
        
        boolean UseErrorPrintStreamForWarning = true;
        
        /** Returns the position as a string */
        public String getLocationAsString(int pPos) {
            return String.format("Near [%s]", pPos);
        }
        
        /** Reports a warning with a cause*/
        public void reportWarning(String pMessage, Throwable pCause) {
            if (this.UseErrorPrintStreamForWarning)
                System.err.println("WARNING: " + pMessage);
            else
                System.out.println("WARNING: " + pMessage);
            if (pCause != null)
                pCause.printStackTrace(this.UseErrorPrintStreamForWarning ? System.err : System.out);
        }
        
        /** Reports an error with a cause */
        public void reportError(String pMessage, Throwable pCause) {
            System.err.println("ERROR: " + pMessage);
            if (pCause != null)
                pCause.printStackTrace(System.err);
        }
        
        /** Reports a fatal error with a cause */
        public void reportFatalError(String pMessage, Throwable pCause) {
            System.err.println("FATAL ERROR: " + pMessage);
            if (pCause != null)
                pCause.printStackTrace(System.err);
        }
        
        /** Reports a warning with a cause*/
        public void reportWarning(String pMessage, Throwable pCause, int pPos) {
            String Msg = String.format("WARNING: %s%s", pMessage,
                    (pPos < 0) ? "" : String.format(" near %s", this.getLocationAsString(pPos)));
            ((this.UseErrorPrintStreamForWarning) ? System.err : System.out).println(Msg);
            if (pCause != null)
                pCause.printStackTrace(this.UseErrorPrintStreamForWarning ? System.err : System.out);
        }
        
        /** Reports an error with a cause */
        public void reportError(String pMessage, Throwable pCause, int pPos) {
            String Msg = String.format("ERROR: %s%s", pMessage,
                    (pPos < 0) ? "" : String.format(" near %s", this.getLocationAsString(pPos)));
            System.err.println(Msg);
            if (pCause != null)
                pCause.printStackTrace(System.err);
        }
        
        /** Reports a fatal error with a cause */
        public void reportFatalError(String pMessage, Throwable pCause, int pPos) {
            String Msg = String.format("FATAL ERROR: %s%s", pMessage,
                    (pPos < 0) ? "" : String.format(" near %s", this.getLocationAsString(pPos)));
            System.err.println(Msg);
            if (pCause != null)
                pCause.printStackTrace(System.err);
        }
        
        /** Returns the string representation of this compilation context */
        @Override
        public String toString() {
            return "CompilationContext$Simple:" + this.hashCode();
        }
    }
}
