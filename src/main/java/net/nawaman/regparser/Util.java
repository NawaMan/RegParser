/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Utilities functions
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class Util {
	
	static String[] EmptyStringArray = new String[0]; 
	
	// Object ----------------------------------------------------------------------------------------------------------
	
	/** Returns string representation of an object value. */
	static public String toString(Object O) {
		return (O == null)?"null":O.toString();
	}
	
	/** Checks if object O1 and O2 equals. */
	static public boolean equal(Object O1, Object O2) {
		if(O1 == O2) return true;
		if((O1 == null) || (O2 == null)) return false;
		return O1.hashCode() == O2.hashCode();
	}
	
	// Exception -------------------------------------------------------------------------------------------------------
	
	static public String getThrowableToString(Throwable T) {
		StringBuffer SB = new StringBuffer();
		ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
		PrintStream PS = new PrintStream(BAOS);
		T.printStackTrace(PS);
		SB.append(BAOS.toString());
		return SB.toString();
	}
	
	// Array -----------------------------------------------------------------------------------------------------------
	
	/** Returns string representation of an object value with customized Open, Close and Separator. */
	static public <T> String toString(T[] O) {
		return toString(O, "[","]",",");
	}
	
	/** Returns string representation of an object value with customized Open, Close and Separator. */
	static public <T> String toString(T[] O, String Open, String Close, String Separator) {
		StringBuffer SB = new StringBuffer();
		SB.append(Open);
		if(O != null) {
			for(int i = 0; i < Array.getLength(O); i++) {
				if(i != 0) SB.append(Separator);
				Object Oi = Array.get(O,i);
				if(     Oi instanceof String)    SB.append(Oi);
				else if(Oi == null)              SB.append("null");
				else if(Oi.getClass().isArray()) SB.append(Arrays.toString((Object[])Oi));
				else                             SB.append(Oi.toString());
			}
		}
		SB.append(Close);
		return SB.toString();
	}
	
	// String ----------------------------------------------------------------------------------------------------------
	
	/**
	 * Converts the CharSequence to a escape text
	 * For example Tab => '\t', Newline => '\n' e.g.
	 */
	static public CharSequence escapeText(CharSequence CS) {
		//if(CS == null) return null;
		StringBuffer SB = new StringBuffer();
		for(int i = 0; i < CS.length(); i++) {
			char c = ' ';
			try {
				c = CS.charAt(i);
			} catch(java.lang.StringIndexOutOfBoundsException E) {
				System.out.println("escapeText: " + E.toString());
			}
			if(c ==     '\t') { SB.append("\\t");  continue; }
			if(c ==     '\n') { SB.append("\\n");  continue; }
			if(c ==     '\r') { SB.append("\\r");  continue; }
			if(c ==     '\f') { SB.append("\\f");  continue; }
			if(c ==     '\b') { SB.append("\\b");  continue; }
			if(c ==     '\'') { SB.append("\\\'"); continue; }
			if(c ==     '\"') { SB.append("\\\""); continue; }
			if(c ==     '\\') { SB.append("\\\\"); continue; }
			SB.append(c);
		}
		return SB;
	}
	
	/**
	 * Converts the CharSequence to a escape text
	 * For example '\t' => Tab, '\n' => Newline e.g.
	 */
	static public CharSequence invertEscapeText(CharSequence CS) {
		StringBuffer SB = new StringBuffer();
		for(int i = 0; i < CS.length(); i++) {
			char c = ' ';
			try {
				c = CS.charAt(i);
			} catch(java.lang.StringIndexOutOfBoundsException E) {
				System.out.println("invertEscapeText: " + E.toString());
			}
			if(c != '\\') { SB.append(c); continue; }
			i++;
			c = CS.charAt(i);
			switch(c) {
				case 't':  { SB.append("\t"); continue; }
				case 'n':  { SB.append("\n"); continue; }
				case 'r':  { SB.append("\r"); continue; }
				case 'f':  { SB.append("\f"); continue; }
				case 'b':  { SB.append("\b"); continue; }
				case '\'': { SB.append("\'"); continue; }
				case '\"': { SB.append("\""); continue; }
				case '\\': { SB.append("\\"); continue; }
			}
			
			if(c == 'u') {
				if(i + 6 >= CS.length()) { SB.append(c); continue; }
				// Found Hexadecimal Unicode Escape
				String UniEsc = CS.subSequence(i + 1, i + 5).toString().toUpperCase();
				// 4-digit hexadecimal
				int C    = 0;
				int Base = 1;
				for(int uc = (UniEsc.length() - 1); uc >= 0; uc--) {
					int I = "0123456789ABCDEF".indexOf(UniEsc.charAt(uc));
					if(I == -1) { SB.append("(*ERROR*)"); break; }
					C += I*Base;
					Base *= 16;
				}
				SB.append("" + C);
				i+= 5;
			}
			if((c >= '0') && (c <= '7')) {
				// Found Octal Unicode Escape
				String UniEsc = CS.subSequence(i, i + 3).toString().toUpperCase();
				// 2-digit octal
				int C    = 0;
				int Base = 1;
				for(int uc = (UniEsc.length() - 1); uc >= 0; uc--) {
					int I = "01234567".indexOf(UniEsc.charAt(uc));
					if(I == -1) { SB.append("(*ERROR*)"); break; }
					C += I*Base;
					Base *= 8;
				}
				SB.append("" + C);
			}
			
			SB.append(c); continue;
		}
		return SB;
	}
	
	// File ------------------------------------------------------------------------------------------------------------
	
	/** Loads objects from a file */
	static public Serializable[] loadObjectsFromFile(String pFileName)  throws IOException {
		return Util.loadObjectsFromFile(new File(pFileName));
	}
	/** Loads objects from a file */
	static public Serializable[] loadObjectsFromFile(File pFile) throws IOException {
		FileInputStream FIS = null;
		try {
			FIS = new FileInputStream(pFile);
			return loadObjectsFromStream(FIS);
		} catch(Exception E) {
			throw new RuntimeException("File Loading Error: There is error while tring to load objects from file '"
					+ pFile.getAbsolutePath() + File.pathSeparator + pFile.getName()+"'.", E);
		} finally {
			if(FIS != null) FIS.close();
		}
	}
	
	/** Loads objects from a file */
	static public Serializable[] loadObjectsFromStream(InputStream IS) throws IOException {
		try {
			Object O = null;
			ObjectInputStream OIS = new ObjectInputStream(IS);
			// Check Signature of the file
			O = OIS.readObject();
			if(!(O instanceof Serializable[])) {
				throw new RuntimeException("File Loading Error: The file does not contain an array of serilizables.");
			}
			
			return (Serializable[])O;
		} catch(IOException IOE) {
			throw IOE;
		} catch(ClassNotFoundException E) {
			throw new RuntimeException("Object Loading Error.", E);
		}
	}
	
	/** Saves objects to a file */
	static public void saveObjectsToFile(String pFileName, Serializable[] pObjects)  throws IOException {
		Util.saveObjectsToFile(new File(pFileName), pObjects);
	}
	
	/** Saves objects to a file */
	static public void saveObjectsToFile(File pFile, Serializable[] pObjects) throws IOException {
		saveObjectsToStream(new FileOutputStream(pFile), pObjects);
	}
	/** Saves objects to a file */
	static public void saveObjectsToStream(OutputStream pOS, Serializable[] pObjects) throws IOException {
		// Save it to the file
		try {
			ObjectOutputStream OOS = new ObjectOutputStream(pOS);
			OOS.writeObject(pObjects);
		} finally {
			if(pOS != null) pOS.close();
		}
	}
	
	// Text file -------------------------------------------------------------------------------------------------------
	
	/** Loads a text from a file */
	static public String loadTextFile(String pTextFileName)  throws IOException {
		return Util.loadTextFile(new File(pTextFileName));
	}
	
	/** Loads a text from a file */
	static public String loadTextFile(File pTextFile) throws IOException {
		FileInputStream FIS = null;
		try {
			FIS = new FileInputStream(pTextFile);
			return Util.loadTextFromStream(FIS);
		} finally {
			if(FIS != null) FIS.close();
		}
	}
	
	/** Loads a text from a file */
	static public String loadTextFromStream(InputStream IS) throws IOException {
		InputStreamReader IDR = new InputStreamReader(IS);
		BufferedReader    BR  = new BufferedReader(IDR);
	        
		StringBuffer      SB  = new StringBuffer();
		String Line;
		while ((Line = BR.readLine()) != null) SB.append(Line).append('\n');
	        
		BR.close();
		
		if(SB.length() == 0) return "";
		return SB.toString().substring(0, SB.length() - 1);
	}
	
	/** Saves a text to a file */
	static public void saveTextToFile(String pTextFileName, String pText)  throws IOException {
		Util.saveTextToFile(new File(pTextFileName), pText);
	}
	
	/** Saves a text to a file */
	static public void saveTextToFile(File pTextFile, String pText)  throws IOException {
		FileOutputStream FOS = null;
		try {
			FOS = new FileOutputStream(pTextFile);
			Util.saveTextToStream(FOS, pText);
		} finally {
			if(FOS != null) FOS.toString();
		}
	}
	
	/** Saves a text to a file */
	static public void saveTextToStream(OutputStream OS, CharSequence pText) throws IOException {
		OutputStreamWriter TextOut = new OutputStreamWriter(OS);
		TextOut.write(pText.toString());
		TextOut.close();
	}

	/**
	 * Returns the class by its name and throws exception if not found.
	 * 
     * @exception LinkageError                 if the linkage fails
     * @exception ExceptionInInitializerError  if the initialization provoked by this method fails
     * @exception ClassNotFoundException       if the class cannot be located
	 **/
	static Class<?> getClassByName(String Name, ClassLoader CL) {
		try {
			String OrgName = Name;
			// Try to extract the class name from the signature
			String SubClassName = "";
			while(true) {
				Class<?> Cls = null;
				try {
					// Get class by the normal mean
					Cls = (CL == null)?Class.forName(Name):Class.forName(Name, true, CL);
				} catch(ClassNotFoundException  E) {
					// When not found, try to see if the class is declared in side other class.
					// By trimming the string after the last "."
					int Ind = Name.lastIndexOf('.');
					// If there is no more ".", the class is not found.
					if(Ind == -1) throw new ClassNotFoundException(OrgName);
					String S = Name.substring(Ind + 1);
					if(SubClassName.length() == 0) SubClassName = S;
					else                           SubClassName = S + "." + SubClassName; 
					Name = Name.substring(0, Ind);
					// The continue, until forName(String) return a class
					continue;
				}
				
				// After get the base class, get the sub class if any
				if(SubClassName.length() == 0) return Cls;
				String[] SNs = SubClassName.split("\\.");
				// Loop to get each level of sub-class.
				for(int i = 0; i < SNs.length; i++) {
					Class<?>[] SubClasses = Cls.getDeclaredClasses();
					boolean IsFound = false;
					for(int j = SubClasses.length; --j >= 0; ) {
						if(SubClasses[j].getCanonicalName().equals(Cls.getCanonicalName() + "." + SNs[i])) {
							IsFound = true;
							Cls = SubClasses[j];
							break;
						}
					}
					// Throw an error if not found
					if(!IsFound) { throw new ClassNotFoundException(OrgName); }
				} 
				return Cls;
			}
		} catch(Exception E) {}
		return null;
	}
}
