/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2024 Nawapunth Manusitthipol.
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

/**
 * Classes implementing this interface can be seen as a RegParserEntry.
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public interface AsRegParserEntry extends AsRegParser {
    
    /** @return  a {@link RegParserEntry} that this object represents. */
    public RegParserEntry asRegParserEntry();
    
    /** @return  a {@link RegParser} that this object represents. */
    public default RegParser asRegParser() {
        return RegParser.newRegParser(asRegParserEntry());
    }
    
    
}
