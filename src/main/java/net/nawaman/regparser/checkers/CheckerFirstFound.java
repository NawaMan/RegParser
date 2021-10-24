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

import net.nawaman.regparser.Checker;

/**
 * This checker simplify the creation of CheckerAlternative with default.
 * 
 * With this checker, all checkers are check from the first to the last. If a match is found, the search stop.
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class CheckerFirstFound extends CheckerAlternative {
    
    static private final long serialVersionUID = 4464247859465463549L;
    
    static Checker[] getLaters(Checker... pLaters) {
        Checker[] Cs = new Checker[pLaters.length - 1];
        System.arraycopy(pLaters, 1, Cs, 0, Cs.length);
        return Cs;
    }
    
    /** Constructs a char set */
    public CheckerFirstFound(Checker... pLaters) {
        super(true, pLaters[0],
                (pLaters.length == 1) ? null
                        : (pLaters.length == 2) ? pLaters[1]
                                : (pLaters.length == 3) ? new CheckerAlternative(true, getLaters(pLaters))
                                        : new CheckerFirstFound(getLaters(pLaters)));
    }
    
}
