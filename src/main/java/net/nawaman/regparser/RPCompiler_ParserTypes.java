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

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Vector;

import net.nawaman.regparser.checkers.CharChecker;
import net.nawaman.regparser.checkers.CharIntersect;
import net.nawaman.regparser.checkers.CharNot;
import net.nawaman.regparser.checkers.CharRange;
import net.nawaman.regparser.checkers.CharSet;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CharUnion;
import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.CheckerFirstFound;
import net.nawaman.regparser.checkers.CheckerNot;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.types.PTIdentifier;
import net.nawaman.regparser.types.PTStrLiteral;
import net.nawaman.regparser.types.PTTextCI;

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
    
    @SuppressWarnings("serial")
    static public class RPTComment extends PType {
        static public String Name = "Comment";
        @Override public String name() { return Name; }
        Checker Checker = null;
        @Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
            if(this.Checker == null) {
                this.Checker = new CheckerAlternative(
                    RegParser.newRegParser(new WordChecker("/*"), new CheckerNot(new WordChecker("*/")), Quantifier.ZeroOrMore, new WordChecker("*/")),
                    RegParser.newRegParser(new WordChecker("(*"), new CheckerNot(new WordChecker("*)")), Quantifier.ZeroOrMore, new WordChecker("*)")),
                    RegParser.newRegParser(
                        new WordChecker("//"),
                        new CheckerNot(new CheckerAlternative(new WordChecker("\n"), RegParser.newRegParser(PredefinedCharClasses.Any, Quantifier.Zero))), Quantifier.ZeroOrMore,
                        new CheckerAlternative(new WordChecker("\n"), RegParser.newRegParser(PredefinedCharClasses.Any, Quantifier.Zero))
                    )
                );
            }
            return this.Checker;
        }
    }
    
    @SuppressWarnings("serial")
    static public class RPTEscape extends PType {
        static public String Name = "Escape";
        @Override public String name() { return Name; }
        Checker Checker = RegParser.newRegParser(new CharSingle('\\'), new CharSet(RPCompiler_ParserTypes.Escapable));
        @Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
        @Override public Object  doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
                PTypeProvider pProvider) {
            
            // Ensure type
            if(!Name.equals(pThisResult.typeNameAt(pEntryIndex)))
                throw new RPCompilationException("Mal-formed RegParser Escape near \""
                        + pThisResult.originalText().substring(pThisResult.startPosition()) + "\".");
            
            return pThisResult.textOf(pEntryIndex).charAt(1);
        }
    }
    
    @SuppressWarnings("serial")
    static public class RPTEscapeOct extends PType {
        static public String Name = "EscapeOct";
        @Override public String name() { return Name; }
        Checker Checker = RegParser.newRegParser(    // ~\\0[0-3]?[0-7]?[0-7]~
                new WordChecker("\\0"),
                new CharRange('0', '3'), Quantifier.ZeroOrOne,
                new CharRange('0', '7'), new Quantifier(1, 2)
            );
        static final String OCT = "01234567";
        @Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
        @Override public Object  doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
                PTypeProvider pProvider) {
            
            // Ensure type
            if(!Name.equals(pThisResult.typeNameAt(pEntryIndex)))
                throw new RPCompilationException("Mal-formed RegParser Escape near \""
                        + pThisResult.originalText().substring(pThisResult.startPosition()) + "\".");
            
            String Text = pThisResult.textOf(pEntryIndex).substring(2);
            while(Text.length() < 3) Text = "0" + Text;
            return (char)(OCT.indexOf(Text.charAt(0))*8*8 + OCT.indexOf(Text.charAt(1))*8 + OCT.indexOf(Text.charAt(2)));
        }
    }
    
    @SuppressWarnings("serial")
    static public class RPTEscapeHex extends PType {
        static public String Name = "EscapeHex";
        @Override public String name() { return Name; }
        Checker Checker = RegParser.newRegParser(    // ~\\x[0-9A-Fa-f][0-9A-Fa-f]~
                new WordChecker("\\x"),
                PredefinedCharClasses.HexadecimalDigit,
                PredefinedCharClasses.HexadecimalDigit
            );
        static final public String HEX = "0123456789ABCDEF";
        @Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
        @Override public Object  doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
                PTypeProvider pProvider) {
            
            // Ensure type
            if(!Name.equals(pThisResult.typeNameAt(pEntryIndex)))
                throw new RPCompilationException("Mal-formed RegParser Escape near \""
                        + pThisResult.originalText().substring(pThisResult.startPosition()) + "\".");
            
            String Text = pThisResult.textOf(pEntryIndex).toUpperCase();
            return (char)(HEX.indexOf(Text.charAt(2))*16 + HEX.indexOf(Text.charAt(3)));
        }
    }
    
    @SuppressWarnings("serial")
    static public class RPTEscapeUnicode extends PType {
        static public String Name = "EscapeUnicode";
        @Override public String name() { return Name; }
        Checker Checker = RegParser.newRegParser(    // ~\\u[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]~
                new WordChecker("\\u"),
                PredefinedCharClasses.HexadecimalDigit,
                PredefinedCharClasses.HexadecimalDigit,
                PredefinedCharClasses.HexadecimalDigit,
                PredefinedCharClasses.HexadecimalDigit
            );
        static final public String HEX = "0123456789ABCDEF";
        @Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
        @Override public Object  doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
                PTypeProvider pProvider) {
            
            // Ensure type
            if(!Name.equals(pThisResult.typeNameAt(pEntryIndex)))
                throw new RPCompilationException("Mal-formed RegParser Escape near \""
                        + pThisResult.originalText().substring(pThisResult.startPosition()) + "\".");
            
            String Text = pThisResult.textOf(pEntryIndex).toUpperCase();
            return (char)(HEX.indexOf(Text.charAt(2))*16*16*16 + HEX.indexOf(Text.charAt(3))*16*16
                        + HEX.indexOf(Text.charAt(4))*16       + HEX.indexOf(Text.charAt(5)));
        }
    }
    
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
            throw new RPCompilationException("Mal-formed RegParser character class near \""
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
    static public class RPTType extends PType {
        static public String Name = "Type";
        @Override public String name() { return Name; }
        Checker Checker = RegParser.newRegParser(
            new CharSingle('!'),
            new CheckerAlternative(
                true,
                RegParser.newRegParser(
                    "#AsText",     new CharSingle('$'),   Quantifier.ZeroOrOne,
                    "#TypeName",   new PTypeRef.Simple(PTIdentifier.Name),
                    "#TypeOption", new CharSet("*+"),     Quantifier.ZeroOrOne,
                    "#Validate",   new CharSet("~?"),     Quantifier.ZeroOrOne,
                    "#Collective", new WordChecker("[]"), Quantifier.ZeroOrOne,
                    "#Param",      RegParser.newRegParser(
                        new CharSingle('('),
                        "#ParamValue", new PTypeRef.Simple(PTStrLiteral.Name), Quantifier.ZeroOrOne,
                        new CharSingle(')')
                    ), Quantifier.ZeroOrOne,
                    new CharSingle('!')
                ),
                RegParser.newRegParser(
                    "#Error[]", new CharNot(new CharSingle('!')), Quantifier.ZeroOrMore,
                    new CharSingle('!')
                )
            )
        );
        @Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
        @Override public Object  doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
                PTypeProvider pProvider) {
            pThisResult = pThisResult.entryAt(pEntryIndex).subResult();

            String N = pThisResult.textOf("#TypeName");
            if(N == null)
                throw new RPCompilationException("Mal-formed RegParser Type near \""
                        + pThisResult.originalText().substring(pThisResult.startPosition()) + "\".");
            
            String T = pThisResult.textOf("#AsText");
            String O = pThisResult.textOf("#TypeOption");
            String V = pThisResult.textOf("#Validate");
            String C = pThisResult.textOf("#Collective");
            String TName  = ((T == null)?"":T) + N + ((O == null)?"":O) + ((V == null)?"":V) + ((C == null)?"":C);
            
            String Param = null;
            var PE = pThisResult.lastEntryOf("#Param");
            if((PE != null) && PE.hasSubResult()) {
                Param = pThisResult.lastEntryOf("#Param").subResult().textOf("#ParamValue");
                if(Param != null) Param = Util.unescapeText(Param.substring(1, Param.length() - 1)).toString();
                
            }
            return new PTypeRef.Simple(TName, Param);
        }
    }
    
    @SuppressWarnings("serial")
    static public class RPTQuantifier extends PType {
        static public String Name = "Quantifier";
        @Override public String name() { return Name; }
        Checker Checker = RegParser.newRegParser(    // ((?|*|+|{\s[\d]*\s}|{\s[\d]*\s,\s}|{\s,\s[\d]*\s}|{\s[\d]*\s,\s[\d]*\s})(*|+)?)?
                "#Quantifier", new CheckerAlternative(
                    true,
                    new CharSet("+*?"),
                    RegParser.newRegParser(
                        new CharSingle('{'),
                        PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                        RegParser.newRegParser("#BothBound", RegParser.newRegParser(PredefinedCharClasses.Digit, Quantifier.OneOrMore)),
                        PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                        new CharSingle('}')
                    ),
                    RegParser.newRegParser(
                        new CharSingle('{'),
                        PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                        new CharSingle(','),
                        PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                        RegParser.newRegParser("#UpperBound",RegParser.newRegParser(PredefinedCharClasses.Digit, Quantifier.OneOrMore)),
                        PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                        new CharSingle('}')
                    ),
                    RegParser.newRegParser(
                        new CharSingle('{'),         Quantifier.One,
                        PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                        RegParser.newRegParser("#LowerBound", RegParser.newRegParser(PredefinedCharClasses.Digit, Quantifier.OneOrMore)),
                        PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                        new CharSingle(','),
                        PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                        new CharSingle('}')
                    ),
                    RegParser.newRegParser(
                        new CharSingle('{'),
                        PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                        RegParser.newRegParser("#LowerBound",RegParser.newRegParser(PredefinedCharClasses.Digit, Quantifier.OneOrMore)),
                        PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                        new CharSingle(','),
                        PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                        RegParser.newRegParser("#UpperBound",RegParser.newRegParser(PredefinedCharClasses.Digit, Quantifier.OneOrMore)),
                        PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                        new CharSingle('}')
                    ),
                    RegParser.newRegParser(
                        new CharSingle('{'),
                        RegParser.newRegParser("#Error[]",RegParser.newRegParser(new CharNot(new CharSingle('}')), Quantifier.ZeroOrMore)),
                        new CharSingle('}')
                    )
                ),
                "#Greediness", new CharSet("+*"), Quantifier.ZeroOrOne
            );
        @Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
        @Override public Object  doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
                PTypeProvider pProvider) {

            // Ensure type
            if(!Name.equals(pThisResult.typeNameAt(pEntryIndex)))
                throw new RPCompilationException("Mal-formed RegParser quatifier near \""
                        + pThisResult.originalText().substring(pThisResult.startPosition()) + "\".");
            
            pThisResult = pThisResult.entryAt(pEntryIndex).subResult();
            
            String Q = pThisResult.textOf("#Quantifier");
            String G = pThisResult.textOf("#Greediness");
            
            switch(Q.charAt(0)) {
                case '?': {
                    if(G           == null)                             return Quantifier.ZeroOrOne;
                    if(G.charAt(0) == Greediness.MaximumSign.charAt(0)) return Quantifier.ZeroOrOne_Maximum;
                    if(G.charAt(0) == Greediness.MinimumSign.charAt(0)) return Quantifier.ZeroOrOne_Minimum;
                    break;
                }
                case '*': {
                    if(G           == null)                             return Quantifier.ZeroOrMore;
                    if(G.charAt(0) == Greediness.MaximumSign.charAt(0)) return Quantifier.ZeroOrMore_Maximum;
                    if(G.charAt(0) == Greediness.MinimumSign.charAt(0)) return Quantifier.ZeroOrMore_Minimum;
                    break;
                }
                case '+': {
                    if(G           == null)                             return Quantifier.OneOrMore;
                    if(G.charAt(0) == Greediness.MaximumSign.charAt(0)) return Quantifier.OneOrMore_Maximum;
                    if(G.charAt(0) == Greediness.MinimumSign.charAt(0)) return Quantifier.OneOrMore_Minimum;
                    break;
                }
                case '{': {
                    pThisResult = pThisResult.entryAt(0).subResult(); 

                    String E = pThisResult.textOf("#Error[]");
                    if(E != null) break;
                    
                    String BS = pThisResult.textOf("#BothBound");
                    int B = -1;
                    if(BS == null) {
                        String US = pThisResult.textOf("#UpperBound");
                        String LS = pThisResult.textOf("#LowerBound");
                        int U = (US == null)?-1:Integer.parseInt(US);
                        int L = (LS == null)? 0:Integer.parseInt(LS);
                        if((U != -1) && (U < L))
                            throw new RPCompilationException("Upper bound must not be lower than its lower bound "
                                    + "near \"" + pThisResult.originalText().substring(pThisResult.startPosition())
                                    + "\".");
                        if(U != L) {
                            if((L == 0) && (L == 1)) {
                                if(G           == null)                             return Quantifier.ZeroOrOne;
                                if(G.charAt(0) == Greediness.MaximumSign.charAt(0)) return Quantifier.ZeroOrOne_Maximum;
                                if(G.charAt(0) == Greediness.MinimumSign.charAt(0)) return Quantifier.ZeroOrOne_Minimum;
                                break;
                            }
                            if((L == 0) && (U == -1)) {
                                if(G           == null)                             return Quantifier.ZeroOrMore;
                                if(G.charAt(0) == Greediness.MaximumSign.charAt(0)) return Quantifier.ZeroOrMore_Maximum;
                                if(G.charAt(0) == Greediness.MinimumSign.charAt(0)) return Quantifier.ZeroOrMore_Minimum;
                                break;
                            }
                            if((L == 1) && (U == -1)) {
                                if(G           == null)                             return Quantifier.OneOrMore;
                                if(G.charAt(0) == Greediness.MaximumSign.charAt(0)) return Quantifier.OneOrMore_Maximum;
                                if(G.charAt(0) == Greediness.MinimumSign.charAt(0)) return Quantifier.OneOrMore_Minimum;
                                break;
                            }
                            if(G           == null)                             return new Quantifier(L, U);
                            if(G.charAt(0) == Greediness.MaximumSign.charAt(0)) return new Quantifier(L, U, Greediness.Maximum);
                            if(G.charAt(0) == Greediness.MinimumSign.charAt(0)) return new Quantifier(L, U, Greediness.Minimum);
                            break;
                        }
                        B = L;
                    } else
                        B = Integer.parseInt(BS);
                    
                    if(G == null)
                    if(B == 0) return Quantifier.Zero;
                    if(B == 1) return Quantifier.One;
                    
                    if(G           == null)                             return new Quantifier(B, B, Greediness.Possessive);
                    if(G.charAt(0) == Greediness.MaximumSign.charAt(0)) return new Quantifier(B, B, Greediness.Maximum);
                    if(G.charAt(0) == Greediness.MinimumSign.charAt(0)) return new Quantifier(B, B, Greediness.Minimum);
                    break;
                }
            }
            throw new RPCompilationException("Mal-formed RegParser Type near \""
                    + pThisResult.originalText().substring(pThisResult.startPosition()) + "\".");
        }
    }
    
    @SuppressWarnings("serial")
    static public class RPTRange extends PType {
        static public String Name = "Range";
        @Override public String name() { return Name; }
        Checker TheChecker = null;
        @Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
            if(this.TheChecker == null) {
                Vector<Checker> Cs = new Vector<Checker>();
                Cs.add(RegParser.newRegParser(new CharNot(new CharSet(RPCompiler_ParserTypes.Escapable + "-"))));
                Cs.add(RegParser.newRegParser(new PTypeRef.Simple(RPTEscape.Name)));
                Cs.add(RegParser.newRegParser(new PTypeRef.Simple(RPTEscapeOct.Name)));
                Cs.add(RegParser.newRegParser(new PTypeRef.Simple(RPTEscapeHex.Name)));
                Cs.add(RegParser.newRegParser(new PTypeRef.Simple(RPTEscapeUnicode.Name)));
                
                // Last
                Cs.add(RegParser.newRegParser("#Error[]", new CharNot(new CharSingle(']'))));
                
                // Create the checker
                this.TheChecker = RegParser.newRegParser(
                    PredefinedCheckers, Quantifier.Zero,
                    "#Start", new CheckerAlternative(true, Cs.toArray(Checker.EMPTY_CHECKER_ARRAY)),
                    PredefinedCharClasses.WhiteSpace, Quantifier.ZeroOrMore,
                    RegParser.newRegParser(
                        new CharSingle('-'),
                        PredefinedCheckers, Quantifier.Zero,
                        PredefinedCharClasses.WhiteSpace, Quantifier.ZeroOrMore,
                        "#End", new CheckerAlternative(true, Cs.toArray(Checker.EMPTY_CHECKER_ARRAY))
                    ), Quantifier.ZeroOrOne
                );
            }
            return this.TheChecker;
        }
        @Override public Object doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
                PTypeProvider pProvider) {

            // Ensure type
            if(!Name.equals(pThisResult.typeNameAt(pEntryIndex)))
                throw new RPCompilationException("Mal-formed RegParser character range near \""
                        + pThisResult.originalText().substring(pThisResult.startPosition()) + "\".");
            
            pThisResult = pThisResult.entryAt(pEntryIndex).subResult();
            
            if(pThisResult.lastEntryOf("#Start").hasSubResult()) {
                if(pThisResult.lastEntryOf("#Start").subResult().hasName("#Error[]")) {
                    throw new RPCompilationException("There is an invalid character near \""
                        + pThisResult.originalText().substring(pThisResult.startPosition()) + "\".");
                }
            }
            
            String S  = pThisResult.textOf("#Start");
            char   SC = S.charAt(0);
            if(S.length() > 1) {
                // Only possibility is that it is an escape
                ParseResult PS = pThisResult.lastEntryOf("#Start").subResult();
                SC = (Character)(pProvider.getType(RPTEscape.Name).compile(PS, pProvider));
            }
            
            String E = pThisResult.textOf("#End");
            if(E == null) return new CharSingle(SC);
            else {
                if(pThisResult.lastEntryOf("#End").hasSubResult()) {
                    if(pThisResult.lastEntryOf("#End").subResult().hasName("#Error[]")) {
                        throw new RPCompilationException("There is an invalid character near \""
                        + pThisResult.originalText().substring(pThisResult.startPosition()) + "\".");
                    }
                }
                
                char EC = E.charAt(0);
                if(E.length() > 1) {
                    // Only possibility is that it is an escape
                    ParseResult PS = pThisResult.lastEntryOf("#End").subResult();
                    EC = (Character)(pProvider.getType(RPTEscape.Name).compile(PS, pProvider));
                }
                if(SC > EC)
                    throw new RPCompilationException("Range starter must not be greater than its ender - near \""
                        + pThisResult.originalText().substring(pThisResult.startPosition()) + "\".");
                return new CharRange(SC, EC);
            }
        }
    }
    
    @SuppressWarnings("serial")
    static public class RPTCharSetItem extends PType {
        static public String Name = "CharSetItem";
        @Override public String name() { return Name; }
        Checker TheChecker = null;
        @Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
            if(this.TheChecker == null) {
                Vector<Checker> Cs = new Vector<Checker>();
                Cs.add(RPCompiler_ParserTypes.PredefinedCheckers);
                Cs.add(
                    RegParser.newRegParser(
                        new PTypeRef.Simple(RPTCharSetItem.Name),
                        RegParser.newRegParser(
                            "#Intersect", new WordChecker("&&"),
                            new PTypeRef.Simple(RPTCharSetItem.Name)
                        ), Quantifier.ZeroOrMore
                    )
                );

                Cs.add(RegParser.newRegParser("#Ignored[]", PredefinedCharClasses.WhiteSpace, Quantifier.OneOrMore));
                
                // The last item
                Cs.add(RegParser.newRegParser("#Range", new PTypeRef.Simple(RPTRange.Name)));
                
                // Create the checker
                this.TheChecker = RegParser.newRegParser(
                        RegParser.newRegParser(
                            new CharSingle('['),
                            "#NOT", new CharSingle('^'), Quantifier.ZeroOrOne,
                            new CharSingle(':'), Quantifier.Zero,
                            //"#Content", 
                            new CheckerAlternative(true, Cs.toArray(Checker.EMPTY_CHECKER_ARRAY)), Quantifier.OneOrMore,
                            new CharSingle(']')
                        ),
                        RegParser.newRegParser(
                            "#Intersect", new WordChecker("&&"),
                            "#Set", RegParser.newRegParser(
                                new CharSingle('['),
                                "#NOT", new CharSingle('^'), Quantifier.ZeroOrOne,
                                new CharSingle(':'), Quantifier.Zero,
                                //"#Content", 
                                new CheckerAlternative(Cs.toArray(Checker.EMPTY_CHECKER_ARRAY)), Quantifier.OneOrMore,
                                new CharSingle(']')
                            )
                        ), Quantifier.ZeroOrMore
                    );
            }
            return this.TheChecker;
        }
        @Override public Object doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
                PTypeProvider pProvider) {            
            pThisResult = pThisResult.entryAt(pEntryIndex).subResult();
            
            Vector<CharChecker> CCCs = new Vector<CharChecker>();
            Vector<CharChecker> CCs  = new Vector<CharChecker>();
            boolean IsNot = false;
            for(int i = 0; i < pThisResult.rawEntryCount(); i++) {
                var PSE = pThisResult.entryAt(i);
                String PName = PSE.name();
                String PText = pThisResult.textOf(i);
                String PType = PSE.typeName();
                if((PName == null) && (PText.equals("["))) continue;
                
                if("#NOT".equals(PName)) {    // Process not
                    IsNot= true;
                    
                } else if(CharClassName.equals(PName) || "#Any".equals(PName)) {    // Extract CharClass
                    CCs.add(getCharClass(pThisResult, i));
                    
                } else if("#Range".equals(PName)) {    // Extract Range
                    CharChecker CC = (CharChecker)pProvider.getType(RPTRange.Name ).compile(pThisResult, i, null,
                            pContext, pProvider);
                    
                    if((CC instanceof CharSingle) && (CCs.size() > 0)) {
                        CharChecker PCC = CCs.get(CCs.size() - 1);
                        // Append the previous one if able
                        if(PCC instanceof CharSingle) {
                            CC = new CharSet("" + ((CharSingle)PCC).ch + ((CharSingle)CC).ch);
                            CCs.remove(CCs.size() - 1);
                        } else if(PCC instanceof CharSet) {
                            CC = new CharSet("" + ((CharSet)PCC).set + ((CharSingle)CC).ch);
                            CCs.remove(CCs.size() - 1);
                        }
                    }
                    CCs.add(CC);
                    
                } else if("#Set".equals(PName) || RPTCharSetItem.Name.equals(PType)) {    // Extract Nested
                    CCs.add((CharChecker)this.compile(pThisResult, i, null, pContext, pProvider));
                    
                }
                
                // Ending and intersect
                if("#Intersect".equals(PName)) {
                    CharChecker NewCC = (CCs.size() == 1)?CCs.get(0):new CharUnion(CCs.toArray(CharChecker.EMPTY_CHAR_CHECKER_ARRAY));
                    if(IsNot) NewCC = new CharNot(NewCC);
                    CCCs.add(NewCC);
                    CCs = new Vector<CharChecker>();
                    IsNot= false;
                }
            }
            
            if(CCs.size() > 0) {
                CharChecker NewCC = (CCs.size() == 1)?CCs.get(0):new CharUnion(CCs.toArray(CharChecker.EMPTY_CHAR_CHECKER_ARRAY));
                if(IsNot) NewCC = new CharNot(NewCC);
                CCCs.add(NewCC);
            }
            
            if(CCCs.size() == 1) return CCCs.get(0);
            else                 return new CharIntersect(CCCs.toArray(CharChecker.EMPTY_CHAR_CHECKER_ARRAY));
        }
    }
    
    @SuppressWarnings("serial")
    static public class RPTRegParserItem extends PType {
        static public String Name = "RegParserItem[]";
        @Override public String name() { return Name; }
        Checker TheChecker = null;
        //@Override public boolean isText() { return false; }
        @Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
            if(this.TheChecker == null) {
                Vector<Checker> Cs = new Vector<Checker>();
                Cs.add(RPCompiler_ParserTypes.PredefinedCheckers);
                // Escape
                Cs.add(RegParser.newRegParser(new PTypeRef.Simple(RPTEscape.Name)));
                Cs.add(RegParser.newRegParser(new PTypeRef.Simple(RPTEscapeOct.Name)));
                Cs.add(RegParser.newRegParser(new PTypeRef.Simple(RPTEscapeHex.Name)));
                Cs.add(RegParser.newRegParser(new PTypeRef.Simple(RPTEscapeUnicode.Name)));
                // CharSet
                Cs.add(RegParser.newRegParser(new PTypeRef.Simple(RPTType.Name)));
                // Type
                Cs.add(RegParser.newRegParser(new PTypeRef.Simple(RPTCharSetItem.Name)));
                
                Cs.add(
                    RegParser.newRegParser(
                        "#Group", 
                        RegParser.newRegParser(
                            new CharSingle('('),
                            new CharSingle('*'), Quantifier.Zero,
                            new CheckerAlternative(
                                RegParser.newRegParser(
                                    new CharSet("#$"), Quantifier.Zero,
                                    new CheckerAlternative(
                                        true,
                                        RegParser.newRegParser(
                                            "#NOT", new CharSingle('^'), Quantifier.ZeroOrOne,
                                            new PTypeRef.Simple("RegParser"), Quantifier.OneOrMore,
                                            RegParser.newRegParser(
                                                "#OR", new CharSingle('|'),
                                                new PTypeRef.Simple("RegParser"), Quantifier.OneOrMore
                                            ), Quantifier.ZeroOrMore,
                                            RegParser.newRegParser(
                                                "#Default", new WordChecker("||"),
                                                new PTypeRef.Simple("RegParser"), Quantifier.OneOrMore
                                            ), Quantifier.ZeroOrMore,
                                            new CharSingle(')')
                                        ),
                                        RegParser.newRegParser(
                                            "#Error[]", new CharNot(new CharSet(")")), Quantifier.ZeroOrMore,
                                            new CharSingle(')')
                                        )
                                    )
                                ),
                                RegParser.newRegParser(
                                    "#Name", new CharSet("#$"),
                                    new CheckerAlternative(
                                        true,
                                        RegParser.newRegParser(
                                            "#Group-Name",   new PTypeRef.Simple(PTIdentifier.Name),
                                            "#Group-Option", new CharSet("*+"),     Quantifier.ZeroOrOne,
                                            "#Multiple",     new WordChecker("[]"), Quantifier.ZeroOrOne,
                                            PredefinedCharClasses.WhiteSpace, Quantifier.ZeroOrMore,
                                            new CheckerAlternative(
                                                true,
                                                RegParser.newRegParser(
                                                    "#Defined", new CharSingle(':'),
                                                    PredefinedCharClasses.WhiteSpace, Quantifier.ZeroOrMore,
                                                    new CheckerAlternative(
                                                        // Type
                                                        RegParser.newRegParser(
                                                            "#Type", new PTypeRef.Simple(RPTType.Name)
                                                        ),
                                                        // Error of Type
                                                        RegParser.newRegParser(
                                                            "#Error[]",
                                                            RegParser.newRegParser(
                                                                new CharSingle('!'),
                                                                new CharNot(new CharSet("!)")), Quantifier.ZeroOrMore
                                                            )
                                                        ),
                                                        // Nested-RegParser
                                                        RegParser.newRegParser(
                                                            new CharSingle('~'),
                                                            "#GroupRegParser", new PTypeRef.Simple("RegParser"),
                                                            new CharSingle('~')
                                                        ),
                                                        RegParser.newRegParser(
                                                            "#Error[]",
                                                            RegParser.newRegParser(
                                                                new CharNot(new CharSet(":!)~")), Quantifier.ZeroOrMore
                                                            )
                                                        )
                                                    ),
                                                    PredefinedCharClasses.WhiteSpace, Quantifier.ZeroOrMore,
                                                    // Second set
                                                    "#Second", RegParser.newRegParser(
                                                        new CharSingle(':'),
                                                        PredefinedCharClasses.WhiteSpace, Quantifier.ZeroOrMore,
                                                        new CheckerAlternative(
                                                            // Type
                                                            RegParser.newRegParser(
                                                                "#Type", new PTypeRef.Simple(RPTType.Name)
                                                            ),
                                                            // Error of Type
                                                            RegParser.newRegParser(
                                                                "#Error[]",
                                                                RegParser.newRegParser(
                                                                    new CharSingle('!'),
                                                                    new CharNot(new CharSet("!)")), Quantifier.ZeroOrMore
                                                                )
                                                            ),
                                                            // Nested-RegParser
                                                            RegParser.newRegParser(
                                                                new CharSingle('~'),
                                                                "#GroupRegParser", new PTypeRef.Simple("RegParser"),
                                                                new CharSingle('~')
                                                            ),
                                                            RegParser.newRegParser(
                                                                "#Error[]",
                                                                RegParser.newRegParser(
                                                                    new CharNot(new CharSet(":!)~")), Quantifier.ZeroOrMore
                                                                )
                                                            )
                                                        ),
                                                        PredefinedCharClasses.WhiteSpace, Quantifier.ZeroOrMore
                                                    ), Quantifier.ZeroOrOne,
                                                    new CharSingle(')')
                                                ),
                                                // BackRef
                                                RegParser.newRegParser(
                                                    "#BackRefCI", new CharSingle('\''), Quantifier.ZeroOrOne,
                                                    "#BackRef",   new CharSingle(';'),
                                                    PredefinedCharClasses.WhiteSpace, Quantifier.ZeroOrMore,
                                                    new CheckerAlternative(
                                                        new CharSingle(')'),
                                                        RegParser.newRegParser(
                                                            "#Error[]", new CharNot(new CharSet(")")), Quantifier.ZeroOrOne,
                                                            new CharSingle(')')
                                                        )
                                                    )
                                                ),
                                                RegParser.newRegParser(
                                                    "#Error[]", new CharNot(new CharSet(")")), Quantifier.ZeroOrMore,
                                                    new CharSingle(')')
                                                )
                                            )
                                        ),
                                        RegParser.newRegParser(
                                            "#Error[]", new CharNot(new CharSet(")")), Quantifier.ZeroOrMore,
                                            new CharSingle(')')
                                        )
                                    )
                                )
                            )
                        )
                    )
                );
                
                Cs.add(RegParser.newRegParser(
                        "$TextCI",
                        RegParser.newRegParser(
                            new CharSet("'"),
                            new CharNot(new CharSingle('\'')), Quantifier.ZeroOrMore,
                            new WordChecker("'")
                        )
                    ));
                

                // Other char
                Cs.add(RegParser.newRegParser(new CharNot(new CharSet(RPCompiler_ParserTypes.Escapable)), Quantifier.OneOrMore_Minimum));
                
                // Create the checker
                this.TheChecker = RegParser.newRegParser(new CheckerAlternative(true, Cs.toArray(Checker.EMPTY_CHECKER_ARRAY)));
            }
            return this.TheChecker;
        }
        @Override public Object doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
                PTypeProvider pProvider) {
            
            var PSE = pThisResult.entryAt(pEntryIndex);

            boolean HasSub = PSE.hasSubResult();
            
            if(!HasSub) {    // A word
                String Text = pThisResult.textOf(pEntryIndex);
                return RPEntry._new((Text.length() == 0)?new CharSingle(Text.charAt(0)):new WordChecker(Text));
            }
            
            // Go into the sub
            pThisResult = PSE.subResult();
            PSE         = pThisResult.entryAt(0);
            
            String PName = PSE.name();
            
            if("#Any".equals(PName))        return RPEntry._new(PredefinedCharClasses.Any);
            if(CharClassName.equals(PName)) return RPEntry._new(getCharClass(pThisResult, 0));
            
            String PType = PSE.typeName();
            if(RPTEscape.Name.equals(PType))
                return RPEntry._new(new CharSingle((Character)pProvider.getType(RPTEscape.Name       ).compile(pThisResult, 0, null, pContext, pProvider)));
            if(RPTEscapeOct.Name.equals(PType))
                return RPEntry._new(new CharSingle((Character)pProvider.getType(RPTEscapeOct.Name    ).compile(pThisResult, 0, null, pContext, pProvider)));
            if(RPTEscapeHex.Name.equals(PType))
                return RPEntry._new(new CharSingle((Character)pProvider.getType(RPTEscapeHex.Name    ).compile(pThisResult, 0, null, pContext, pProvider)));
            if(RPTEscapeUnicode.Name.equals(PType))
                return RPEntry._new(new CharSingle((Character)pProvider.getType(RPTEscapeUnicode.Name).compile(pThisResult, 0, null, pContext, pProvider)));
            
            if(RPTCharSetItem.Name.equals(PType))
                return RPEntry._new((Checker)pProvider.getType(RPTCharSetItem.Name  ).compile(pThisResult, 0, null, pContext, pProvider));
            
            if("$TextCI".equals(PName)) {
                String Text = pThisResult.textOf(0);
                // Return as Word if its lower case and upper case is the same
                if(Text.toUpperCase().equals(Text.toLowerCase())) return RPEntry._new(new WordChecker(Text));
                return RPEntry._new(new PTypeRef.Simple(PTTextCI.Name, Text.substring(1, Text.length() - 1)));
            }
            
            if(RPTType.Name.equals(PType))
                return RPEntry._new((PTypeRef)pProvider.getType(RPTType.Name   ).compile(pThisResult, 0, null, pContext, pProvider));
            
            if("#Group".equals(PName)) {
                
                String N = PSE.subResult().textOf("#Name");
                if(N == null) return RPEntry._new((Checker)pProvider.getType(RPTRegParser.Name).compile(pThisResult, 0, null, pContext, pProvider));
                
                pThisResult = PSE.subResult();
                
                String GN = pThisResult.textOf("#Group-Name");
                String O  = pThisResult.textOf("#Group-Option"); O = (O == null)?"":O;
                String M  = pThisResult.textOf("#Multiple");     M = (M == null)?"":M;
                
                String B = pThisResult.textOf("#BackRef");
                if(B != null) {
                    if(pThisResult.textOf("#BackRefCI") != null)
                        return RPEntry._new(new PTypeRef.Simple(PTBackRefCI.BackRefCI_Instance.name(), N+GN+M));
                    else
                        return RPEntry._new(new PTypeRef.Simple(PTBackRef.BackRef_Instance.name(), N+GN+M));
                }
                
                RegParser Second = null;
                var PRE = pThisResult.lastEntryOf("#Second");
                if((PRE != null) && PRE.hasSubResult()) {
                    ParseResult Sub_Second = PRE.subResult();
                    
                    int IT = Sub_Second.lastIndexOf("#Type");
                    if(IT != -1) {    // TypeRef with Name
                        Second = RegParser.newRegParser((PTypeRef)pProvider.getType(RPTType .Name).compile(Sub_Second, IT, null, pContext, pProvider));
                    } else {
                        int IE = Sub_Second.lastIndexOf("#GroupRegParser");
                        // Named Group
                        Second = RegParser.newRegParser((Checker)pProvider.getType(RPTRegParser.Name).compile(Sub_Second, IE, null, pContext, pProvider));
                    }
                }
                
                int IT = pThisResult.lastIndexOf("#Type");
                if(IT != -1) {    // TypeRef with Name
                    return RPEntry._new(
                            N+GN+O+M,
                            (PTypeRef)pProvider.getType(RPTType .Name).compile(pThisResult, IT, null, pContext, pProvider),
                            null,
                            Second);
                }
                
                int IE = pThisResult.lastIndexOf("#GroupRegParser");
                // Named Group
                return RPEntry._new(
                        N+GN+O+M,
                        (Checker)pProvider.getType(RPTRegParser.Name).compile(pThisResult, IE, null, pContext, pProvider),
                        null,
                        Second);
            }
            return super.compile(pThisResult, pParam, pContext, pProvider);
        }
    }
    
    @SuppressWarnings("serial")
    static public class RPTRegParser extends PType {
        static public String Name = "RegParser";
        @Override public String name() { return Name; }
        Checker Checker = RegParser.newRegParser(
            new CheckerAlternative(true,
                RegParser.newRegParser(
                    "#ItemQuantifier", 
                    RegParser.newRegParser(
                        new PTypeRef.Simple(RPTRegParserItem.Name),
                        RegParser.newRegParser("#Ignored[]", PredefinedCharClasses.WhiteSpace, Quantifier.ZeroOrMore),
                        new PTypeRef.Simple(RPTQuantifier.Name)
                    )
                ),
                RegParser.newRegParser("#Comment",   new PTypeRef.Simple(RPTComment.Name)),
                new CheckerAlternative(true,
                    RegParser.newRegParser("#Item[]",    new PTypeRef.Simple(RPTRegParserItem.Name)),
                    RegParser.newRegParser("#Ignored[]", new CharSet(" \t\n\r\f"))
                )
            ), Quantifier.OneOrMore
        );
        @Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
        @Override public Object  doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
                PTypeProvider pProvider) {
            
            if((pThisResult.entryAt(pEntryIndex) == null) ||
                (pThisResult.entryAt(pEntryIndex).subResult() == null)) {
                throw new RPCompilationException("Mal-formed RegParser Type near \""
                            + pThisResult.originalText().substring(pThisResult.startPositionAt(0)) + "\".");
            }
            pThisResult = pThisResult.entryAt(pEntryIndex).subResult();
            
            Vector<RegParser> RPPs = new Vector<RegParser>();
            Vector<RPEntry>   RPs  = new Vector<RPEntry>();
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
                    throw new RPCompilationException("Mal-formed RegParser Type near \""
                            + pThisResult.originalText().substring(pThisResult.startPositionAt(i)) + "\".");
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
                                Checker NewRP = RegParser.newRegParser((Object[])RPs.toArray(RPEntry.EmptyRPEntryArray));
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
                        RPs.add(RPEntry._new(new WordChecker(pThisResult.textOf(i))));
                        
                    }
                } else {

                    RPEntry RPI = null;
                    if(RPTRegParser.Name.equals(PSE.typeName())) {
                        RPI = RPEntry._new((Checker)this.compile(pThisResult, i, null, pContext, pProvider));
                        
                        if(RPI.getChecker() instanceof RegParser) {
                            var parserEntries = ((RegParser)(RPI.getChecker())).entries();
                            RPEntry[] Entries = (parserEntries == null) ? null : parserEntries.toArray(RPEntry[]::new);
                            if(Entries == null) 
                                RPI = null;
                            else if(Entries.length == 1)
                                RPI = Entries[0];
                        }
                        
                    } else if("#Item[]".equals(PName)) {    // Process not
                        RPI = (RPEntry)pProvider.getType(RPTRegParserItem.Name).compile(pThisResult, i, null, pContext, pProvider);
                        
                    } else {
                        ParseResult Sub = PSE.subResult();
        
                        RPI = (RPEntry)pProvider.getType(RPTRegParserItem.Name).compile(Sub, 0, null, pContext, pProvider);
    
                        Quantifier Q = Quantifier.One;
                        if(Sub.rawEntryCount() == 2) {
                            Q = (Quantifier)pProvider.getType(RPTQuantifier.Name).compile(Sub, 1, null, pContext, pProvider); 
                        }
                        if(Q != Quantifier.One) {
                            if(RPI.getChecker() != null) RPI = RPEntry._new(RPI.name(), RPI.getChecker(), Q, RPI.secondStage());
                            if(RPI.typeRef() != null) RPI = RPEntry._new(RPI.name(), RPI.typeRef(), Q, RPI.secondStage());
                            if(RPI.type()    != null) RPI = RPEntry._new(RPI.name(), RPI.type(),    Q, RPI.secondStage());
                        }
                        
                    }
                    
                    if(RPI != null) RPs.add(RPI);
                }
                
                // Ending
                if(IsOR) {
                    Checker NewRP = RegParser.newRegParser((Object[])RPs.toArray(RPEntry.EmptyRPEntryArray));
                    RPPs.add((NewRP instanceof RegParser)?(RegParser)NewRP:RegParser.newRegParser(NewRP));
                    RPs = new Vector<RPEntry>();
                    IsOR  = false;
                }
            }
            
            // Ending
            if(RPs.size() > 0) {
                Checker NewRP = RegParser.newRegParser((Object[])RPs.toArray(RPEntry.EmptyRPEntryArray));
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