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

package net.nawaman.regparser.types;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.PredefinedCharClasses;
import net.nawaman.regparser.Quantifier;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.Util;
import net.nawaman.regparser.checkers.CharNot;
import net.nawaman.regparser.result.ParseResult;

/**
 * Parser Type for java checker.
 * 
 * This class allows a Parser Type to be implement without actually implementing a Parser Type. This class utilize Java
 *   Reflection mechanism and Parser Type parameter. See Test_14_JavaChecker _CheckerFized for example.
 *   
 * There are four ways of using PTJavaChecker
 * 
 *  "!javaChecker(`<<string reference to a static method of (CharSequence, int, PTypeProvider, ParseResult):int>>`)!"
 *  "!javaChecker(`<<string reference to a class that implements Checker>>`)!"
 *  "!javaChecker(`<<string reference to a field of an instance of a Checker>>`)!"
 *  "!javaChecker(`<<string reference to a method ():Checker to be executed to get an instance of a Checker>>`)!"
 *  
 * Again, See Test_14_JavaChecker _CheckerFized for example.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
public class PTJavaChecker extends ParserType {
    
    static Hashtable<String, Checker> Checkers                  = new Hashtable<String, Checker>();
    static Class<?>[]                 EmptyClassArray           = new Class<?>[0];
    static Class<?>[]                 CheckerClassArray_int     = new Class<?>[] { CharSequence.class, int.class,
            PTypeProvider.class, ParseResult.class };
    static Class<?>[]                 CheckerClassArray_Integer = new Class<?>[] { CharSequence.class, Integer.class,
            PTypeProvider.class, ParseResult.class };
    
    static public String Name = "javaChecker";
    
    @Override
    public String name() {
        return Name;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public Checker checker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
        if ((pParam == null) || (pParam.length() == 0))
            return RegParser.newRegParser(new CharNot(PredefinedCharClasses.Any), Quantifier.Zero);
        
        Checker C = PTJavaChecker.Checkers.get(pParam);
        if (C != null)
            return C;
        
        int Index;
        if ((Index = pParam.indexOf("::")) != -1) {    // Get a checker from a static field or static method 
            String ClassName  = pParam.substring(0, Index);
            String AccessName = pParam.substring(Index + "::".length());
            try {
                Class<?> Cls = Util.getClassByName(ClassName, null);
                if (AccessName.indexOf("(") != -1) {    // Method
                    AccessName = AccessName.substring(0, AccessName.indexOf("("));
                    Method M = Cls.getMethod(AccessName, PTJavaChecker.EmptyClassArray);
                    Object O = M.invoke(null);
                    if (!(O instanceof Checker))
                        throw new NoSuchMethodException(AccessName + "():Checker");
                    C = (Checker) O;
                } else {    // Field
                    Field  F = Cls.getField(AccessName);
                    Object O = F.get(null);
                    if (!(O instanceof Checker))
                        throw new NoSuchFieldException(AccessName + ":Checker");
                    C = (Checker) O;
                }
            } catch (Exception E) {
                throw new RuntimeException(E);
            }
            
        } else
            if ((Index = pParam.indexOf("->")) != -1) {    // The checker method int (CharSequence, int, PTypeProvider, ParseResult pResult) 
                String   ClassName  = pParam.substring(0, Index);
                Class<?> Cls        = Util.getClassByName(ClassName, null);
                String   AccessName = pParam.substring(Index + "->".length());
                
                Method M = null;
                try {
                    M = Cls.getMethod(AccessName, PTJavaChecker.CheckerClassArray_int);
                    if ((M.getReturnType() != int.class) && (M.getReturnType() != Integer.class))
                        M = null;
                } catch (Exception E) {
                }
                try {
                    M = Cls.getMethod(AccessName, PTJavaChecker.CheckerClassArray_Integer);
                    if ((M.getReturnType() != int.class) && (M.getReturnType() != Integer.class))
                        M = null;
                } catch (Exception E) {
                }
                
                if (M == null)
                    throw new RuntimeException(
                            new NoSuchMethodException(AccessName + "(CharSequence,int,PTypeProvider):int"));
                
                final Method TheMethod = M;
                C = new Checker() {
                    /**{@inherDoc}*/
                    @Override
                    public int startLengthOf(CharSequence S, int pOffset, PTypeProvider pTProvider) {
                        return this.startLengthOf(S, pOffset, pTProvider, null);
                    }
                    
                    /**{@inherDoc}*/
                    @Override
                    public int startLengthOf(CharSequence S, int pOffset, PTypeProvider pTProvider,
                            ParseResult pResult) {
                        try {
                            return ((Integer) TheMethod.invoke(null, S, pOffset, pTProvider, pResult)).intValue();
                        } catch (Exception E) {
                            throw new RuntimeException(E);
                        }
                    }
                    
                    /**{@inherDoc}*/
                    @Override
                    public Checker optimize() {
                        return this;
                    }
                };
                
            } else {    // Create a checker from a default constructor of the given class
                String   ClassName = pParam;
                Class<?> Cls       = Util.getClassByName(ClassName, null);
                try {
                    if (Checker.class.isAssignableFrom(Cls))
                        C = (Checker) (Cls.newInstance());
                } catch (Exception E) {
                    throw new RuntimeException(E);
                }
            }
        
        if (C == null)
            throw new RuntimeException("Unable to obtain the checker from the parameter:\"" + pParam + "\"");
        
        PTJavaChecker.Checkers.put(pParam, C);
        return C;
    }
    
}
