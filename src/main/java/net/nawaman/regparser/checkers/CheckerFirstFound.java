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
    
    private static final long serialVersionUID = 4464247859465463549L;
    
    private static Checker[] getLaters(Checker... laters) {
        var checkers = new Checker[laters.length - 1];
        System.arraycopy(laters, 1, checkers, 0, checkers.length);
        return checkers;
    }
    
    /** Constructs a char set */
    public CheckerFirstFound(Checker... laters) {
        super(true, laters[0],
                (laters.length == 1)
                    ? null
                    : (laters.length == 2) 
                        ? laters[1]
                        : (laters.length == 3) 
                            ? new CheckerAlternative(true, getLaters(laters))
                            : new CheckerFirstFound(getLaters(laters)));
    }
    
}
