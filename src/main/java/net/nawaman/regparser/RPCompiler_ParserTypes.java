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

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Vector;

import net.nawaman.regparser.checkers.CharChecker;
import net.nawaman.regparser.checkers.CharSet;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.CheckerFirstFound;
import net.nawaman.regparser.checkers.CheckerNot;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.compiler.RPCommentParserType;
import net.nawaman.regparser.compiler.RPQuantifierParserType;
import net.nawaman.regparser.compiler.RPRegParserItemParserType;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.utils.Util;

/**
 * Compiler of RegParser language.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class RPCompiler_ParserTypes {
    
    RPCompiler_ParserTypes() {}
    
    static final public String Escapable = ".?*-+^&'{}()[]|\\~ \t\n\r\f\u000B\u000C";
    
    public static String escapeOfRegParser(String pWord) {
        if(pWord == null) return null;
        pWord = Util.escapeText(pWord).toString();
        
        StringBuffer SB = new StringBuffer();
        for(int i = 0; i < pWord.length(); i++)  {
            char C = pWord.charAt(i);
            if((C != '\\') && (Escapable.indexOf(C) != -1)) SB.append('\\'); 
            SB.append(C);
        }
        return SB.toString();
    }
    
    // Types -----------------------------------------------------------------------------------------------------------
    
    static public final String CharClassName = "#CharClass";
    
    // NOTE - This is not very performance effective, but I have no time to refactor now
    // TODOLATER - Reorder this (to take advantage of CheckerFirstFound)
    static public final CheckerAlternative PredefinedCheckers = new CheckerAlternative(
        RegParser.newRegParser("#Any", new CharSingle('.')),
        RegParser.newRegParser(CharClassName, 
            RegParser.newRegParser(
                new CharSingle('\\'),
                new CheckerFirstFound(    // ~\[dDdDwWoOxX]~
                    RegParser.newRegParser("$Digit",               new CharSingle('d')),
                    RegParser.newRegParser("$NonDigit",            new CharSingle('D')),
                    RegParser.newRegParser("$WhiteSpace",          new CharSingle('s')),
                    RegParser.newRegParser("$NonWhiteSpace",       new CharSingle('S')),
                    RegParser.newRegParser("$Blank",               new CharSingle('b')),
                    RegParser.newRegParser("$NonBlank",            new CharSingle('B')),
                    RegParser.newRegParser("$Word",                new CharSingle('w')),
                    RegParser.newRegParser("$NonWord",             new CharSingle('W')),
                    RegParser.newRegParser("$OctalDigit",          new CharSingle('o')),
                    RegParser.newRegParser("$NonOctalDigit",       new CharSingle('O')),
                    RegParser.newRegParser("$HexadecimalDigit",    new CharSingle('x')),
                    RegParser.newRegParser("$NonHexadecimalDigit", new CharSingle('X')),
                    RegParser.newRegParser(
                        new WordChecker("p{"),    // \p{Lower}
                        new CheckerFirstFound(
                            RegParser.newRegParser("$LowerCaseAlphabet", new WordChecker("Lower")),
                            RegParser.newRegParser("$UpperCaseAlphabet", new WordChecker("Upper")),
                            RegParser.newRegParser("$ASCII",             new WordChecker("ASCII")),
                            RegParser.newRegParser("$AlphabetAndDigit",  new WordChecker("Alnum")),    // A&D first
                            RegParser.newRegParser("$Alphabet",          new WordChecker("Alpha")),
                            RegParser.newRegParser("$Punctuation",       new WordChecker("Punct")),
                            RegParser.newRegParser("$Visible",           new WordChecker("Graph")),
                            RegParser.newRegParser("$Printable",         new WordChecker("Print")),
                            RegParser.newRegParser("$Blank",             new WordChecker("Blank")),
                            RegParser.newRegParser("$OctalDigit",        new WordChecker("ODigit")),
                            RegParser.newRegParser("$HexadecimalDigit",  new WordChecker("XDigit"))
                        ),
                        new CharSingle('}')
                    ),
                    RegParser.newRegParser(
                        new CharSingle('j'),
                        new CheckerFirstFound(
                            RegParser.newRegParser("$JDigit",         new CharSingle('d')),
                            RegParser.newRegParser("$JNonDigit",      new CharSingle('D')),
                            RegParser.newRegParser("$JWhiteSpace",    new CharSingle('s')),
                            RegParser.newRegParser("$JNonWhiteSpace", new CharSingle('S')),
                            RegParser.newRegParser("$JWord",          new CharSingle('w')),
                            RegParser.newRegParser("$JNonWord",       new CharSingle('W')),
                            RegParser.newRegParser(
                                new WordChecker("p{"),    // \jp{}
                                new CheckerFirstFound(
                                    RegParser.newRegParser("$JLowerCaseAlphabet", new WordChecker("Lower")),
                                    RegParser.newRegParser("$JUpperCaseAlphabet", new WordChecker("Upper")),
                                    RegParser.newRegParser("$JASCII",             new WordChecker("ASCII")),
                                    RegParser.newRegParser("$JAlphabetAndDigit",  new WordChecker("Alnum")),    // A&D First
                                    RegParser.newRegParser("$JAlphabet",          new WordChecker("Alpha")),
                                    RegParser.newRegParser("$JPunctuation",       new WordChecker("Punct")),
                                    RegParser.newRegParser("$JVisible",           new WordChecker("Graph")),
                                    RegParser.newRegParser("$JPrintable",         new WordChecker("Print")),
                                    RegParser.newRegParser("$JBlank",             new WordChecker("Blank")),
                                    RegParser.newRegParser("$JControlCharacter",  new WordChecker("Cntrl")),
                                    RegParser.newRegParser("$JHexadecimalDigit",  new WordChecker("XDigit")),
                                    RegParser.newRegParser("$JGreek",             new WordChecker("InGreek")),
                                    RegParser.newRegParser("$JCurrencySimbol",    new WordChecker("Sc"))
                                ),
                                new CharSingle('}')
                            )
                        )
                    )
                )
            )
        ),
        RegParser.newRegParser(CharClassName,
            RegParser.newRegParser(
                new WordChecker("[:"),
                new CheckerFirstFound(
                    // Single Char for better escape
                    RegParser.newRegParser("$Tilde",    new WordChecker("Tilde")),
                    RegParser.newRegParser("$Tilde",    new CharSingle('~')),
                    RegParser.newRegParser("$Stress",   new WordChecker("Stress")),
                    RegParser.newRegParser("$Stress",   new CharSingle('`')),
                    RegParser.newRegParser("$XMark",    new WordChecker("XMark")),
                    RegParser.newRegParser("$XMark",    new CharSingle('!')),
                    RegParser.newRegParser("$AtSign",   new WordChecker("AtSign")),
                    RegParser.newRegParser("$AtSign",   new CharSingle('@')),
                    RegParser.newRegParser("$Hash",     new WordChecker("Hash")),
                    RegParser.newRegParser("$Hash",     new CharSingle('#')),
                    RegParser.newRegParser("$Dollar",   new WordChecker("Dollar")),
                    RegParser.newRegParser("$Dollar",   new CharSingle('$')),
                    RegParser.newRegParser("$Percent",  new WordChecker("Percent")),
                    RegParser.newRegParser("$Percent",  new CharSingle('%')),
                    RegParser.newRegParser("$Caret",    new WordChecker("Caret")),
                    RegParser.newRegParser("$Caret",    new CharSingle('^')),
                    RegParser.newRegParser("$AndSign",  new WordChecker("AndSign")),
                    RegParser.newRegParser("$AndSign",  new CharSingle('&')),
                    RegParser.newRegParser("$Asterisk", new WordChecker("Asterisk")),
                    RegParser.newRegParser("$Asterisk", new CharSingle('*')),
                    RegParser.newRegParser("$ORound",   new WordChecker("ORound")),
                    RegParser.newRegParser("$ORound",   new CharSingle('(')),
                    RegParser.newRegParser("$CRound",   new WordChecker("CRound")),
                    RegParser.newRegParser("$CRound",   new CharSingle(')')),
                    RegParser.newRegParser("$OSquare",  new WordChecker("OSquare")),
                    RegParser.newRegParser("$OSquare",  new CharSingle('[')),
                    RegParser.newRegParser("$CSquare",  new WordChecker("CSquare")),
                    RegParser.newRegParser("$CSquare",  new CharSingle(']')),
                    RegParser.newRegParser("$OCurl",    new WordChecker("OCurl")),
                    RegParser.newRegParser("$OCurl",    new CharSingle('{')),
                    RegParser.newRegParser("$CCurl",    new WordChecker("CCurl")),
                    RegParser.newRegParser("$CCurl",    new CharSingle('}')),
                    RegParser.newRegParser("$OAngle",   new WordChecker("OAngle")),
                    RegParser.newRegParser("$OAngle",   new CharSingle('<')),
                    RegParser.newRegParser("$CAngle",   new WordChecker("CAngle")),
                    RegParser.newRegParser("$CAngle",   new CharSingle('>')),
                    RegParser.newRegParser("$UScore",   new WordChecker("UScore")),
                    RegParser.newRegParser("$UScore",   new CharSingle('_')),
                    RegParser.newRegParser("$Minus",    new WordChecker("Minus")),
                    RegParser.newRegParser("$Minus",    new CharSingle('-')),
                    RegParser.newRegParser("$Plus",     new WordChecker("Plus")),
                    RegParser.newRegParser("$Plus",     new CharSingle('+')),
                    RegParser.newRegParser("$Equal",    new WordChecker("Equal")),
                    RegParser.newRegParser("$Equal",    new CharSingle('=')),
                    RegParser.newRegParser("$Pipe",     new WordChecker("Pipe")),
                    RegParser.newRegParser("$Pipe",     new CharSingle('|')),
                    RegParser.newRegParser("$BSlash",   new WordChecker("BSlash")),
                    RegParser.newRegParser("$BSlash",   new CharSingle('\\')),
                    RegParser.newRegParser("$Colon",    new WordChecker("Colon")),
                    RegParser.newRegParser("$Colon",    new CharSingle(':')),
                    RegParser.newRegParser("$SColon",   new WordChecker("SColon")),
                    RegParser.newRegParser("$SColon",   new CharSingle(';')),
                    RegParser.newRegParser("$SQoute",   new WordChecker("SQoute")),
                    RegParser.newRegParser("$SQoute",   new CharSingle('\'')),
                    RegParser.newRegParser("$DQoute",   new WordChecker("DQoute")),
                    RegParser.newRegParser("$DQoute",   new CharSingle('\"')),
                    RegParser.newRegParser("$Comma",    new WordChecker("Comma")),
                    RegParser.newRegParser("$Comma",    new CharSingle(',')),
                    RegParser.newRegParser("$Dot",      new WordChecker("Dot")),
                    RegParser.newRegParser("$Dot",      new CharSingle('.')),
                    RegParser.newRegParser("$QMark",    new WordChecker("QMark")),
                    RegParser.newRegParser("$QMark",    new CharSingle('?')),
                    RegParser.newRegParser("$Slash",    new WordChecker("Slash")),
                    RegParser.newRegParser("$Slash",    new CharSingle('/')),
                    
                    RegParser.newRegParser("$NewLine", new WordChecker("NewLine")),
                    RegParser.newRegParser("$NewLine", new CharSingle('\n')),
                    RegParser.newRegParser("$Return",  new WordChecker("Return")),
                    RegParser.newRegParser("$Return",  new CharSingle('\r')),
                    RegParser.newRegParser("$Tab",     new WordChecker("Tab")),
                    RegParser.newRegParser("$Tab",     new CharSingle('\t')),
                    RegParser.newRegParser("$Space",   new WordChecker("Space")),
                    RegParser.newRegParser("$Space",   new CharSingle(' ')),
                    
                    RegParser.newRegParser("$Any",                 new WordChecker("Any")),
                    RegParser.newRegParser("$Digit",               new WordChecker("Digit")),
                    RegParser.newRegParser("$NonDigit",            new WordChecker("NonDigit")),
                    RegParser.newRegParser("$WhiteSpace",          new WordChecker("WhiteSpace")),
                    RegParser.newRegParser("$NonWhiteSpace",       new WordChecker("NonWhiteSpace")),
                    RegParser.newRegParser("$WhiteSpaceNoNewLine", new WordChecker("WhiteSpaceNoNewLine")),
                    RegParser.newRegParser("$Word",                new WordChecker("Word")),
                    RegParser.newRegParser("$NonWord",             new WordChecker("NonWord")),
                    RegParser.newRegParser("$Blank",               new WordChecker("Blank")),
                    RegParser.newRegParser("$NonBlank",            new WordChecker("NonBlank")),
                    RegParser.newRegParser("$OctalDigit",          new WordChecker("OctDigit")),
                    RegParser.newRegParser("$NonOctalDigit",       new WordChecker("NonOctDigit")),
                    RegParser.newRegParser("$HexadecimalDigit",    new WordChecker("HexDigit")),
                    RegParser.newRegParser("$NonHexadecimalDigit", new WordChecker("NonHexDigit")),
                    
                    RegParser.newRegParser("$LowerCaseAlphabet", new WordChecker("LowerCaseAlphabet")),
                    RegParser.newRegParser("$UpperCaseAlphabet", new WordChecker("UpperCaseAlphabet")),
                    RegParser.newRegParser("$ASCII",             new WordChecker("ASCII")),
                    RegParser.newRegParser("$AlphabetAndDigit",  new WordChecker("AlphabetAndDigit")),    // A&D First
                    RegParser.newRegParser("$Alphabet",          new WordChecker("Alphabet")),
                    RegParser.newRegParser("$Punctuation",       new WordChecker("Punctuation")),
                    RegParser.newRegParser("$Visible",           new WordChecker("Visible")),
                    RegParser.newRegParser("$Printable",         new WordChecker("Printable")),
                    
                    RegParser.newRegParser("$JAny",           new WordChecker("JAny")),
                    RegParser.newRegParser("$JDigit",         new WordChecker("JDigit")),
                    RegParser.newRegParser("$JNonDigit",      new WordChecker("JNonDigit")),
                    RegParser.newRegParser("$JWhiteSpace",    new WordChecker("JWhiteSpace")),
                    RegParser.newRegParser("$JNonWhiteSpace", new WordChecker("JNonWhiteSpace")),
                    RegParser.newRegParser("$JWord",          new WordChecker("JWord")),
                    RegParser.newRegParser("$JNonWord",       new WordChecker("JNonWord")),
                    
                    RegParser.newRegParser("$JLowerCaseAlphabet", new WordChecker("JLowerCaseAlphabet")),
                    RegParser.newRegParser("$JUpperCaseAlphabet", new WordChecker("JUpperCaseAlphabet")),
                    RegParser.newRegParser("$JASCII",             new WordChecker("JASCII")),
                    RegParser.newRegParser("$JAlphabetAndDigit",  new WordChecker("JAlphabetAndDigit")),    // A&D First
                    RegParser.newRegParser("$JAlphabet",          new WordChecker("JAlphabet")),
                    RegParser.newRegParser("$JPunctuation",       new WordChecker("JPunctuation")),
                    RegParser.newRegParser("$JVisible",           new WordChecker("JVisible")),
                    RegParser.newRegParser("$JPrintable",         new WordChecker("JPrintable")),
                    RegParser.newRegParser("$JBlank",             new WordChecker("JBlank")),
                    RegParser.newRegParser("$JControlCharacter",  new WordChecker("JControlCharacter")),
                    RegParser.newRegParser("$JHexadecimalDigit",  new WordChecker("JHexadecimalDigit")),
                    RegParser.newRegParser("$JGreek",             new WordChecker("JGreek")),
                    RegParser.newRegParser("$JCurrencySimbol",    new WordChecker("JCurrencySimbol"))
                ),
                new WordChecker(":]")
            )
        )
    );
    
    static Hashtable<String, CharChecker> CachedPredefineds = new Hashtable<String, CharChecker>();
    
    static public CharChecker getCharClass(ParseResult pPR) {
        return getCharClass(pPR, 0);
    }
    static public CharChecker getCharClass(ParseResult pThisResult, int pEntryIndex) {
        // Any
        if(".".equals(pThisResult.textOf(pEntryIndex))) return PredefinedCharClasses.Any;
        
        // Ensure type
        if(!CharClassName.equals(pThisResult.nameOf(pEntryIndex)))
            throw new CompilationException("Mal-formed RegParser character class near \""
                    + pThisResult.originalText().substring(pThisResult.startPosition()) + "\".");
        
        // CharClass
        String N = pThisResult.nameOf(pEntryIndex, 1).substring(1); // Remove the '$'
        if(N.startsWith("J")) N = "Java_" + N.substring(1);
        
        CharChecker CC;
        if((CC = CachedPredefineds.get(N)) != null) return CC;
        
        Field[] Fs = PredefinedCharClasses.class.getFields();
        for(Field F : Fs) {
            if(!N.equals(F.getName())) continue;
            try { CC = (CharChecker)F.get(null); CachedPredefineds.put(N, CC); return CC; } catch(Exception E) {}
        }
        return null;
    }
    
    @SuppressWarnings("serial")
    static public class RPTRegParser extends ParserType {
        static public String Name = "RegParser";
        
        Checker Checker;
        
        @Override public String name() { return Name; }
        public RPTRegParser() {
        	Checker = RegParser.newRegParser(
	            new CheckerAlternative(true,
	                RegParser.newRegParser(
	                    "#ItemQuantifier", 
	                    RegParser.newRegParser(
	                        RPRegParserItemParserType.typeRef,
	                        RegParser.newRegParser("#Ignored[]", PredefinedCharClasses.WhiteSpace, Quantifier.ZeroOrMore),
	                        RPQuantifierParserType.typeRef
	                    )
	                ),
	                RegParser.newRegParser("#Comment", RPCommentParserType.typeRef),
	                new CheckerAlternative(true,
	                    RegParser.newRegParser("#Item[]",    RPRegParserItemParserType.typeRef),
	                    RegParser.newRegParser("#Ignored[]", new CharSet(" \t\n\r\f"))
	                )
	            ), Quantifier.OneOrMore
	        );
        }
        @Override public Checker checker(ParseResult pHostResult, String pParam, ParserTypeProvider pProvider) { return this.Checker; }
        @Override public Object  doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
                ParserTypeProvider pProvider) {
            
            if((pThisResult.entryAt(pEntryIndex) == null) ||
                (pThisResult.entryAt(pEntryIndex).subResult() == null)) {
                throw new CompilationException("Mal-formed RegParser Type near \""
                            + pThisResult.originalText().substring(pThisResult.startPositionOf(0)) + "\".");
            }
            pThisResult = pThisResult.entryAt(pEntryIndex).subResult();
            
            Vector<RegParser> RPPs = new Vector<RegParser>();
            Vector<RegParserEntry>   RPs  = new Vector<RegParserEntry>();
            boolean IsNot     = false;
            boolean IsOR      = false;
            boolean IsDefault = false;
            int Count = pThisResult.rawEntryCount();
            for(int i = 0; i < Count; i++) {
                var PSE   = pThisResult.entryAt(i);
                String            PName = PSE.name();
                
                if("#Ignored[]".equals(PName)) continue;
                if("#Comment"  .equals(PName))   continue;
                
                if("#Error[]".equals(PName)) {
                    System.out.println(pThisResult.toString());
                    throw new CompilationException("Mal-formed RegParser Type near \""
                            + pThisResult.originalText().substring(pThisResult.startPositionOf(i)) + "\".");
                }
                
                boolean HasSub = PSE.hasSubResult();
                
                if(!HasSub) {
                    String PText = pThisResult.textOf(i);
                    if((PName == null) && (PText.equals("("))) continue;
                    
                    if("#NOT".equals(PName)) {    // Process not
                        IsNot= true;
                        
                    } else if("#Default".equals(PName)) {    // Process Default
                        if(!IsDefault) {
                            IsOR      = true;
                            IsDefault = true;
                        } else {
                            if(RPs.size() > 0) {
                                Checker NewRP = RegParser.newRegParser((Object[])RPs.toArray(RegParserEntry.EmptyRPEntryArray));
                                RPPs.add((NewRP instanceof RegParser)?(RegParser)NewRP:RegParser.newRegParser(NewRP));
                                
                                RPs.clear();
                            }
                            CheckerAlternative CA = new CheckerAlternative(true, RPPs.toArray(new RegParser[RPPs.size()]));
                            RPPs.clear();
                            RPPs.add(RegParser.newRegParser(CA));
                            
                            //IsOR      = false;
                            //IsDefault = false;
                        }
                        
                    } else if("#OR".equals(PName)) {    // Process OR
                        IsOR = true;
                        
                    } else if(PText.equals(")")) {
                    
                    } else {    // A Word
                        RPs.add(RegParserEntry.newParserEntry(new WordChecker(pThisResult.textOf(i))));
                        
                    }
                } else {
                	
                    RegParserEntry RPI = null;
                    if(RPTRegParser.Name.equals(PSE.typeName())) {
                        RPI = RegParserEntry.newParserEntry((Checker)this.compile(pThisResult, i, null, pContext, pProvider));
                        
                        if(RPI.checker() instanceof RegParser) {
                            var parserEntries = ((RegParser)(RPI.checker())).entries();
                            RegParserEntry[] Entries = (parserEntries == null) ? null : parserEntries.toArray(RegParserEntry[]::new);
                            if(Entries == null) 
                                RPI = null;
                            else if(Entries.length == 1)
                                RPI = Entries[0];
                        }
                        
                    } else if("#Item[]".equals(PName)) {    // Process not
                        RPI = (RegParserEntry)pProvider.type(RPRegParserItemParserType.name).compile(pThisResult, i, null, pContext, pProvider);
                        
                    } else {
                        ParseResult Sub = PSE.subResult();
                        
                        RPI = (RegParserEntry)pProvider.type(RPRegParserItemParserType.name).compile(Sub, 0, null, pContext, pProvider);
                        
                        Quantifier Q = Quantifier.One;
                        if(Sub.rawEntryCount() == 2) {
                            Q = (Quantifier)pProvider.type(RPQuantifierParserType.name).compile(Sub, 1, null, pContext, pProvider); 
                        }
                        if(Q != Quantifier.One) {
                            if(RPI.checker() != null) RPI = RegParserEntry.newParserEntry(RPI.name(), RPI.checker(), Q, RPI.secondStage());
                            if(RPI.typeRef() != null) RPI = RegParserEntry.newParserEntry(RPI.name(), RPI.typeRef(), Q, RPI.secondStage());
                            if(RPI.type()    != null) RPI = RegParserEntry.newParserEntry(RPI.name(), RPI.type(),    Q, RPI.secondStage());
                        }
                        
                    }
                    
                    if(RPI != null) RPs.add(RPI);
                }
                
                // Ending
                if(IsOR) {
                    Checker NewRP = RegParser.newRegParser((Object[])RPs.toArray(RegParserEntry.EmptyRPEntryArray));
                    RPPs.add((NewRP instanceof RegParser)?(RegParser)NewRP:RegParser.newRegParser(NewRP));
                    RPs = new Vector<RegParserEntry>();
                    IsOR  = false;
                }
            }
            
            // Ending
            if(RPs.size() > 0) {
                Checker NewRP = RegParser.newRegParser((Object[])RPs.toArray(RegParserEntry.EmptyRPEntryArray));
                RPPs.add((NewRP instanceof RegParser)?(RegParser)NewRP:RegParser.newRegParser(NewRP));
            }
            
            if(RPPs.size() == 1) {
                if(IsNot) return new CheckerNot(RPPs.get(0));
                return RPPs.get(0);
            } else {
                Checker[] NewRPEs = RPPs.toArray(net.nawaman.regparser.Checker.EMPTY_CHECKER_ARRAY);
                Checker C = null;
                if(IsDefault) C = new CheckerAlternative(true, NewRPEs);
                else          C = new CheckerAlternative(      NewRPEs);
                if(IsNot) C = new CheckerNot(C);
                return C;
            }
        }
    }
}