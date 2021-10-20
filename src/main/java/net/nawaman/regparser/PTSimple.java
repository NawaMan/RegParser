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

/**
 * A simple type (type without a compiler).
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 **/
@SuppressWarnings("serial")
public class PTSimple extends PType {
    
    protected PTSimple(String pTheName) {
        this.TheName    = pTheName;
    }
    public PTSimple(String pTheName, Checker pTheChecker) {
        this(pTheName);
        this.TheChecker = pTheChecker;
    }
    public PTSimple(String pTheName, RPGetChecker pTheGetChecker) {
        this(pTheName);
        this.TheChecker = pTheGetChecker;
    }
    
    String TheName;
    Object TheChecker;
    
    /**{@inheritDoc}*/ @Override
    final public String getName() { 
        return this.TheName;
    }

    /**{@inheritDoc}*/ @Override
    final public Checker getChecker(ParseResult pHostResult, String pParam,
            PTypeProvider pProvider) {
        
        if(this.TheChecker instanceof RPGetChecker)
            return ((RPGetChecker)TheChecker).getChecker(pHostResult, pParam, pProvider);
        
        return (Checker)TheChecker;
    }
}
