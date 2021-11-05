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

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Utilities functions
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class Util {
    
    public static String[] EMPTY_STRING_ARRAY = new String[0];
    
    private static final String[] indentArray = new String[32];
    private static final String[] tabArray    = new String[32];
    private static final String[] dashArray   = new String[32];
    private static final String[] zeroArray   = new String[32];
    private static final String[] spaceArray  = new String[32];
    
    public static String repeat(int count, String text, String[] cache) {
        if (count == 0) {
            return "";
        }
        
        if (count > cache.length) {
            return repeat(cache.length, text, cache) + repeat(count - cache.length, text, cache);
        }
        
        var cacheText = cache[count];
        if (cacheText != null) {
            return cacheText;
        }
        
        cacheText  = "";
        for (int i = count; --i >= 0;) {
            cacheText += text;
        }
        cache[count] = cacheText;
        return cacheText;
    }
    
    public static String indents(int tab) {
        return repeat(tab, "  ", indentArray);
    }
    
    public static String tabs(int tab) {
        return repeat(tab, ". ", tabArray);
    }
    
    public static String dashes(int dash) {
        return repeat(dash, " -", dashArray);
    }
    
    public static String zeros(int count) {
        return repeat(count, "0", zeroArray);
    }
    
    public static String spaces(int count) {
        return repeat(count, " ", spaceArray);
    }
    
    public static int textWidth(int maxValue) {
        if (maxValue < 10) {
            return 1;
        }
        if (maxValue < 100) {
            return 2;
        }
        if (maxValue < 1000) {
            return 3;
        }
        if (maxValue < 10000) {
            return 4;
        }
        if (maxValue < 100000) {
            return 5;
        }
        if (maxValue < 1000000) {
            return 6;
        }
        if (maxValue < 10000000) {
            return 7;
        }
        if (maxValue < 100000000) {
            return 8;
        }
        return 9;
    }
    
    // Object ----------------------------------------------------------------------------------------------------------
    
    /** Returns string representation of an object value. */
    static public String toString(Object object) {
        if (object == null) {
            return "null";
        }
        
        if (object.getClass().isArray()) {
            if (object instanceof int[]) {
                return Arrays.toString((int[])object);
            }
            if (object instanceof double[]) {
                return Arrays.toString((int[])object);
            }
            if (object instanceof boolean[]) {
                return Arrays.toString((int[])object);
            }
            if (object instanceof Object[]) {
                return Arrays.deepToString((Object[])object);
            }
            if (object instanceof char[]) {
                return Arrays.toString((int[])object);
            }
            if (object instanceof long[]) {
                return Arrays.toString((int[])object);
            }
            if (object instanceof byte[]) {
                return Arrays.toString((int[])object);
            }
            if (object instanceof short[]) {
                return Arrays.toString((int[])object);
            }
        }
        
        return (object == null) ? "null" : object.toString();
    }
    
    // Exception -------------------------------------------------------------------------------------------------------
    
    static public String getThrowableToString(Throwable throwable) {
        var stringBuffer = new StringBuffer();
        var byteStream   = new ByteArrayOutputStream();
        var printStream  = new PrintStream(byteStream);
        throwable.printStackTrace(printStream);
        stringBuffer.append(byteStream.toString());
        return stringBuffer.toString();
    }
    
    // Array -----------------------------------------------------------------------------------------------------------
    
    /** Returns string representation of an object value with customized Open, Close and Separator. */
    static public <T> String toString(T[] array) {
        return toString(array, "[", "]", ",");
    }
    
    /** Returns string representation of an object value with customized prefix, suffix and delimiter. */
    static public String toString(int[] array, String prefix, String suffix, String delimiter) {
        var buffer = new StringBuffer();
        buffer.append(prefix);
        if (array != null) {
            for (int i = 0; i < Array.getLength(array); i++) {
                if (i != 0) {
                    buffer.append(delimiter);
                }
                var element = array[i];
                buffer.append("" + element);
            }
        }
        buffer.append(suffix);
        return buffer.toString();
    }
    
    /** Returns string representation of an object value with customized prefix, suffix and delimiter. */
    static public <T> String toString(T[] array, String prefix, String suffix, String delimiter) {
        var buffer = new StringBuffer();
        buffer.append(prefix);
        if (array != null) {
            for (int i = 0; i < Array.getLength(array); i++) {
                if (i != 0) {
                    buffer.append(delimiter);
                }
                var element = Array.get(array, i);
                if (element instanceof String) {
                    buffer.append(element);
                } else if (element == null) {
                    buffer.append("null");
                } else {
                    if (element.getClass().isArray()) {
                        if (element instanceof int[]) {
                            buffer.append(Arrays.toString((int[])element));
                        } else if (element instanceof int[][]) {
                            buffer.append(Arrays.toString((int[][])element));
                        } else {
                            buffer.append(Arrays.toString((Object[])element));
                        }
                    } else {
                        buffer.append(element.toString());
                    }
                }
            }
        }
        buffer.append(suffix);
        return buffer.toString();
    }
    
    // String ----------------------------------------------------------------------------------------------------------
    
    /**
     * Converts the CharSequence to a escape text
     * For example Tab => '\t', Newline => '\n' e.g.
     */
    static public CharSequence escapeText(CharSequence text) {
        if(text == null) {
            return null;
        }
        
        var stringBuffer = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            char c = ' ';
            try {
                c = text.charAt(i);
            } catch (java.lang.StringIndexOutOfBoundsException E) {
                System.out.println("escapeText: " + E.toString());
            }
            if (c == '\t') { stringBuffer.append("\\t"); continue; }
            if (c == '\n') { stringBuffer.append("\\n"); continue; }
            if (c == '\r') { stringBuffer.append("\\r"); continue; }
            if (c == '\f') { stringBuffer.append("\\f"); continue; }
            if (c == '\b') { stringBuffer.append("\\b"); continue; }
            if (c == '\'') { stringBuffer.append("\\\'"); continue; }
            if (c == '\"') { stringBuffer.append("\\\""); continue; }
            if (c == '\\') { stringBuffer.append("\\\\"); continue; }
            stringBuffer.append(c);
        }
        return stringBuffer;
    }
    
    /**
     * Unescape an escaped text.
     * For example '\t' => Tab, '\n' => Newline e.g.
     */
    static public CharSequence unescapeText(CharSequence text) {
        if(text == null) {
            return null;
        }
        
        var buffer = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            char ch = ' ';
            try {
                ch = text.charAt(i);
            } catch (java.lang.StringIndexOutOfBoundsException E) {
                System.out.println("unescapeText: " + E.toString());
            }
            
            if (ch != '\\') {
                buffer.append(ch);
                continue;
            }
            i++;
            ch = text.charAt(i);
            switch (ch) {
                case 't': {
                    buffer.append("\t");
                    continue;
                }
                case 'n': {
                    buffer.append("\n");
                    continue;
                }
                case 'r': {
                    buffer.append("\r");
                    continue;
                }
                case 'f': {
                    buffer.append("\f");
                    continue;
                }
                case 'b': {
                    buffer.append("\b");
                    continue;
                }
                case '\'': {
                    buffer.append("\'");
                    continue;
                }
                case '\"': {
                    buffer.append("\"");
                    continue;
                }
                case '\\': {
                    buffer.append("\\");
                    continue;
                }
            }
            
            if (ch == 'u') {
                if (i + 6 >= text.length()) {
                    buffer.append(ch);
                    continue;
                }
                // Found Hexadecimal Unicode Escape
                var uniEsc = text.subSequence(i + 1, i + 5).toString().toUpperCase();
                // 4-digit hexadecimal
                int c    = 0;
                int base = 1;
                for (int uc = (uniEsc.length() - 1); uc >= 0; uc--) {
                    int I = "0123456789ABCDEF".indexOf(uniEsc.charAt(uc));
                    if (I == -1) {
                        buffer.append("(*ERROR*)");
                        break;
                    }
                    c    += I * base;
                    base *= 16;
                }
                buffer.append("" + c);
                i += 5;
            }
            if ((ch >= '0') && (ch <= '7')) {
                // Found Octal Unicode Escape
                var uniEsc = text.subSequence(i, i + 3).toString().toUpperCase();
                // 2-digit octal
                int c    = 0;
                int base = 1;
                for (int uc = (uniEsc.length() - 1); uc >= 0; uc--) {
                    int idx = "01234567".indexOf(uniEsc.charAt(uc));
                    if (idx == -1) {
                        buffer.append("(*ERROR*)");
                        break;
                    }
                    c    += idx * base;
                    base *= 8;
                }
                buffer.append("" + c);
            }
            
            buffer.append(ch);
            continue;
        }
        return buffer;
    }
    
    // File ------------------------------------------------------------------------------------------------------------
    
    /** Loads objects from a file */
    static public Serializable[] loadObjectsFromFile(String pFileName) throws IOException {
        return Util.loadObjectsFromFile(new File(pFileName));
    }
    
    /** Loads objects from a file */
    static public Serializable[] loadObjectsFromFile(File file) throws IOException {
        try (var fileInStream = new FileInputStream(file)) {
            return loadObjectsFromStream(fileInStream);
        } catch (Exception E) {
            var errMsg = format("File Loading Error: There is error while tring to load objects from file '%s%s%s'.", 
                    file.getAbsolutePath(), File.pathSeparator, file.getName());
            throw new RuntimeException(errMsg, E);
        }
    }
    
    /** Loads objects from a file */
    static public Serializable[] loadObjectsFromStream(InputStream inputStream) throws IOException {
        try (var objectInStream = new ObjectInputStream(inputStream)) {
            var object = objectInStream.readObject();
            if (!(object instanceof Serializable[])) {
                throw new RuntimeException("File Loading Error: The file does not contain an array of serilizables.");
            }
            
            return (Serializable[])object;
        } catch (IOException IOE) {
            throw IOE;
        } catch (ClassNotFoundException E) {
            throw new RuntimeException("Object Loading Error.", E);
        }
    }
    
    /** Saves objects to a file */
    static public void saveObjectsToFile(String fileName, Serializable[] objects) throws IOException {
        Util.saveObjectsToFile(new File(fileName), objects);
    }
    
    /** Saves objects to a file */
    static public void saveObjectsToFile(File file, Serializable[] objects) throws IOException {
        try (var fileOutStream = new FileOutputStream(file)) {
            saveObjectsToStream(fileOutStream, objects);
        }
    }
    
    /** Saves objects to a file */
    static public void saveObjectsToStream(OutputStream outputStream, Serializable[] objects) throws IOException {
        try {
            var objectStream = new ObjectOutputStream(outputStream);
            objectStream.writeObject(objects);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
    
    // Text file -------------------------------------------------------------------------------------------------------
    
    /** Loads a text from a file */
    static public String loadTextFile(String textFileName)  throws IOException {
        return Util.loadTextFile(new File(textFileName));
    }
    
    /** Loads a text from a file */
    static public String loadTextFile(File textFile) throws IOException {
        try (var fileInputStream = new FileInputStream(textFile)){
            return Util.loadTextFromStream(fileInputStream);
        }
    }
    
    /** Loads a text from a file */
    static public String loadTextFromStream(InputStream inputStream) throws IOException {
        try (var streamReader = new InputStreamReader(inputStream);
             var reader       = new BufferedReader(streamReader)) {
            var stringBuffer  = new StringBuffer();
            
            String Line;
            while ((Line = reader.readLine()) != null) {
                stringBuffer.append(Line).append('\n');
            }
            
            if(stringBuffer.length() == 0) {
                return "";
            }
            
            return stringBuffer.toString()
                    .substring(0, stringBuffer.length() - 1);
        }
    }
    
    /** Saves a text to a file */
    static public void saveTextToFile(String textFileName, String text)  throws IOException {
        Util.saveTextToFile(new File(textFileName), text);
    }
    
    /** Saves a text to a file */
    static public void saveTextToFile(File textFile, String text)  throws IOException {
        try (var fileOutputStream = new FileOutputStream(textFile)) {
            Util.saveTextToStream(fileOutputStream, text);
        }
    }
    
    /** Saves a text to a file */
    static public void saveTextToStream(OutputStream outputStream, CharSequence text) throws IOException {
        try (var writer = new OutputStreamWriter(outputStream)) {
            writer.write(text.toString());
        }
    }
    
    /**
     * Returns the class by its name and throws exception if not found.
     * 
     * @exception LinkageError                 if the linkage fails
     * @exception ExceptionInInitializerError  if the initialization provoked by this method fails
     * @exception ClassNotFoundException       if the class cannot be located
     **/
    public static Class<?> getClassByName(String className, ClassLoader classLoader) {
        try {
            // Try to extract the class name from the signature
            var subClassName = "";
            while (true) {
                Class<?> clazz = null;
                try {
                    // Get class by the normal mean
                    clazz = (classLoader == null)
                          ? Class.forName(className)
                          : Class.forName(className, true, classLoader);
                } catch (ClassNotFoundException exception) {
                    // When not found, try to see if the class is declared in side other class.
                    // By trimming the string after the last "."
                    int idx = className.lastIndexOf('.');
                    // If there is no more ".", the class is not found.
                    if (idx == -1) {
                        throw new ClassNotFoundException(className);
                    }
                    
                    var subString = className.substring(idx + 1);
                    subClassName = subString + ((subClassName.length() == 0) ? "" : ("." + subClassName));
                    className    = className.substring(0, idx);
                    // The continue, until forName(String) return a class
                    continue;
                }
                
                // After get the base class, get the sub class if any
                if (subClassName.length() == 0) {
                    return clazz;
                }
                
                var subNames = subClassName.split("\\.");
                // Loop to get each level of sub-class.
                for (int i = 0; i < subNames.length; i++) {
                    var     subClasses = clazz.getDeclaredClasses();
                    boolean isFound    = false;
                    for (int j = subClasses.length; --j >= 0;) {
                        if (subClasses[j].getCanonicalName().equals(clazz.getCanonicalName() + "." + subNames[i])) {
                            isFound = true;
                            clazz     = subClasses[j];
                            break;
                        }
                    }
                    // Throw an error if not found
                    if (!isFound) {
                        throw new ClassNotFoundException(className);
                    }
                }
                return clazz;
            }
        } catch (Exception exception) {
            System.err.println("Problem getting class by name: " + className);
            exception.printStackTrace();
            return null;
        }
    }
}
