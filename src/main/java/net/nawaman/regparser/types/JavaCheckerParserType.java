/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2022 Nawapunth Manusitthipol.
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

import static java.lang.String.format;
import static net.nawaman.regparser.utils.Util.getClassByName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Objects;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
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
public class JavaCheckerParserType extends ParserType {
	
	private static final long serialVersionUID = 2084235385677789297L;

	private static Hashtable<String, Checker> checkers = new Hashtable<String, Checker>();
	
	private static Class<?>[] emptyClassArray          = new Class<?>[0];
	private static Class<?>[] checkerClassArrayInt     = new Class<?>[] { CharSequence.class, int.class,     ParserTypeProvider.class, ParseResult.class };
	private static Class<?>[] checkerClassArrayInteger = new Class<?>[] { CharSequence.class, Integer.class, ParserTypeProvider.class, ParseResult.class };
	
	public static String name = "javaChecker";
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public Checker checker(ParseResult hostResult, String parameter, ParserTypeProvider typeProvider) {
		return JavaCheckerParserType.checkers.computeIfAbsent(parameter, __ -> {
			try {
				int index;
				if ((index = parameter.indexOf("::")) != -1) {
					return checkerFromStaticFieldOrMethod(parameter, index);
					
				} else if ((index = parameter.indexOf("->")) != -1) {
					return checkerFromInstanceFieldOrMethod(parameter, index);
					
				} else {
					return checkerConstructor(parameter);
				}
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				var errorMessage = format("Unable to obtain the checker from the parameter:\"%s\"", parameter);
				throw new RuntimeException(errorMessage);
			}
		});
	}
	
	@Override
	public final Boolean isDeterministic() {
		return false;
	}
	
	private Checker checkerFromStaticFieldOrMethod(String parameter, int Index) {
		var className  = parameter.substring(0, Index);
		var accessName = parameter.substring(Index + "::".length());
		try {
			var clazz = getClassByName(className, null);
			if (accessName.indexOf("(") != -1) {
				return checkerFromStaticMethod(accessName, clazz);
				
			} else {
				return checkerFromStaticField(accessName, clazz);
				
			}
		} catch (Exception E) {
			throw new RuntimeException(E);
		}
	}
	
	private Checker checkerFromStaticField(String accessName, Class<?> clazz)
						throws NoSuchFieldException, IllegalAccessException {
		var field  = clazz.getField(accessName);
		var object = field.get(null);
		if (!(object instanceof Checker))
			throw new NoSuchFieldException(accessName + ":Checker");
		
		return (Checker)object;
	}
	
	private Checker checkerFromStaticMethod(String accessName, Class<?> clazz)
						throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		accessName = accessName.substring(0, accessName.indexOf("("));
		var method = clazz.getMethod(accessName, JavaCheckerParserType.emptyClassArray);
		var object = method.invoke(null);
		if (!(object instanceof Checker))
			throw new NoSuchMethodException(accessName + "():Checker");
		
		return (Checker)object;
	}
	
	private Checker checkerFromInstanceFieldOrMethod(String parameter, int index) {
		var className  = parameter.substring(0, index);
		var clazz      = getClassByName(className, null);
		var accessName = parameter.substring(index + "->".length());
		
		try {
			var method = clazz.getMethod(accessName, JavaCheckerParserType.checkerClassArrayInt);
			if ((method.getReturnType() == int.class)
			 || (method.getReturnType() == Integer.class))
				return new CheckerFromMethod(method);
			
		} catch (Exception E) {
		}
		try {
			var method = clazz.getMethod(accessName, JavaCheckerParserType.checkerClassArrayInteger);
			if ((method.getReturnType() == int.class)
			 || (method.getReturnType() == Integer.class))
				return new CheckerFromMethod(method);
			
		} catch (Exception E) {
		}
		
		var cause = new NoSuchMethodException(accessName + "(CharSequence,int,PTypeProvider):int");
		throw new RuntimeException(cause);
	}
	
	private Checker checkerConstructor(String parameter) {
		var className = parameter;
		var clazz     = getClassByName(className, null);
		try {
			if (!Checker.class.isAssignableFrom(clazz))
				return null;
			
			var constructor = clazz.getConstructor();
			return (Checker)(constructor.newInstance());
			
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
	
	static class CheckerFromMethod implements Checker {
		
		private static final long serialVersionUID = -3591658191507860064L;
		
		private final Method method;
		
		public CheckerFromMethod(Method method) {
			this.method = method;
		}
		
		/**{@inherDoc}*/
		@Override
		public int startLengthOf(CharSequence S, int pOffset, ParserTypeProvider pTProvider) {
			return startLengthOf(S, pOffset, pTProvider, null);
		}
		
		/**{@inherDoc}*/
		@Override
		public int startLengthOf(
					CharSequence       charSequence,
					int                offset,
					ParserTypeProvider typeProvider,
					ParseResult        parseResult) {
			try {
				var result = method.invoke(null, charSequence, offset, typeProvider, parseResult);
				return ((Integer)result).intValue();
			} catch (Exception E) {
				throw new RuntimeException(E);
			}
		}
		
		@Override
		public final Boolean isDeterministic() {
			return false;
		}
		
		/**{@inherDoc}*/
		@Override
		public Checker optimize() {
			return this;
		}
		
		private int hashCode = 0;
		
		@Override
		public int hashCode() {
			if (hashCode != 0) {
				return hashCode;
			}
			
			hashCode = Objects.hash(CheckerFromMethod.class, method);
			return hashCode;
		}
		
	}
	
}
