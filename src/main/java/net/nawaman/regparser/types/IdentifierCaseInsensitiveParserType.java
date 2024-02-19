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

package net.nawaman.regparser.types;

import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.result.ParseResult;

/**
 * Parser for detecting C-like identifier (for case-insensitive)
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class IdentifierCaseInsensitiveParserType extends IdentifierParserType {
    
    private static final long serialVersionUID = 1841177503836598215L;

    
    public static String               name     = "$IdentifierCI";
    public static IdentifierParserType instance = new IdentifierParserType();
    public static ParserTypeRef        typeRef  = instance.typeRef();
    
    @Override
    public String name() {
        return name;
    }
    
    @Override
    public boolean doValidate(
                    ParseResult        hostResult,
                    ParseResult        thisResult,
                    String             parameter,
                    ParserTypeProvider typeProvider) {
        var text = thisResult.text();
        if (text == parameter)
            return true;
        
        if ((text == null) || (parameter == null))
            return false;
        
        return text.toLowerCase().equals(parameter.toLowerCase());
    }
    
}
