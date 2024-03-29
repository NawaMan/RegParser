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

package net.nawaman.regparser.checkers;

import static java.util.Objects.requireNonNull;
import static net.nawaman.regparser.EscapeHelpers.escapeOfRegParser;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.utils.Util;

/**
 * Checker for checking a sequence of character or a word
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public final class WordChecker implements Checker {
    
    private static final long serialVersionUID = -6856120712978724955L;
    
    static public final WordChecker EmptyWord = new WordChecker();
    
    private final String word;
    
    private WordChecker() {
        this.word = "";
    }
    
    public WordChecker(String word) {
        this.word = requireNonNull(word);
        if (word.length() == 0)
            throw new IllegalArgumentException();
    }
    
    public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider) {
        return startLengthOf(text, offset, typeProvider, null);
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider, ParseResult parseResult) {
        return Util.startsWith(text, word, offset)
                ? word.length()
                : -1;
    }
    
    @Override
    public final Boolean isDeterministic() {
        return true;
    }
    
    @Override
    public String toString() {
        return escapeOfRegParser(word);
    }
    
    @Override
    public int hashCode() {
        return word.hashCode();
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this)
            return true;
        
        if (!(O instanceof WordChecker))
            return false;
        
        return word.equals(((WordChecker)O).word);
    }
    
    @Override
    public Checker optimize() {
        return this;
    }
    
}
