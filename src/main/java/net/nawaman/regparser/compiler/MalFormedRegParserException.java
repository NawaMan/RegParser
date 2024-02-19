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
package net.nawaman.regparser.compiler;

public class MalFormedRegParserException extends RuntimeException {
    
    private static final long serialVersionUID = -9160277386650840043L;
    
    public MalFormedRegParserException() {
        super();
    }
    
    public MalFormedRegParserException(String message) {
        super(message);
    }
    
    public MalFormedRegParserException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public MalFormedRegParserException(Throwable cause) {
        super(cause);
    }
    
}
