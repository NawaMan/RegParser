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

import static net.nawaman.regparser.Util.loadObjectsFromStream;
import static net.nawaman.regparser.Util.saveObjectsToStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Pocket holding PTypeProvider
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public interface ParserTypePocket extends Serializable {
	
	public ParserTypeProvider typeProvider();
	
	public boolean isRepackagingNeeded(); 
	
	/** Simple implementation of PTypeProviderPocket */
	@SuppressWarnings("serial")
	static public class Simple implements ParserTypePocket {
		
		public Simple(ParserTypeProvider typeProvider) {
			this.typeProvider = typeProvider;
		}
		
		transient boolean            isRepackagingNeeded = false;
		transient ParserTypeProvider typeProvider        = null;
		
		public ParserTypeProvider typeProvider() {
			return this.typeProvider;
		}
		
		public boolean isRepackagingNeeded() {
			return this.isRepackagingNeeded;
		}
		
		/** Custom deserialization is needed. */
		private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
			// Save the rest
			inputStream.defaultReadObject();
			
			try {
				var objBytes    = (byte[])inputStream.readObject();
				var bytesStream = new ByteArrayInputStream(objBytes);
				var objStream   = new ObjectInputStream(bytesStream);
				// Load the body
				typeProvider = (ParserTypeProvider)objStream.readObject();
			} catch (Exception E) {
				isRepackagingNeeded = true;
			}
		}
		
		/** Custom serialization is needed. */
		private void writeObject(ObjectOutputStream outputStream) throws IOException {
			// Save the rest
			outputStream.defaultWriteObject();
			
			// Save the Kinds and Types
			var bytesStream = new ByteArrayOutputStream();
			try (var objStream   = new ObjectOutputStream(bytesStream)) {
				// Save the body function
				objStream.writeObject(this.typeProvider);
				objStream.close();
				outputStream.writeObject(bytesStream.toByteArray());
			}
		}
		
		// Utilities ---------------------------------------------------------------------------------------------------
		
		// File ----------------------------------------------------------------------------------
		
		/** Load a type package as text from a file named */
		static public ParserTypePocket loadPocketFromFile(String fileName) throws IOException {
			var file = new File(fileName);
			return loadPocketFromFile(file);
		}
		
		/** Load a type package as text from a file F */
		static public ParserTypePocket loadPocketFromFile(File file) throws IOException {
			try {
				var fileStream = new FileInputStream(file);
				return loadPocketFromStream(fileStream);
			} catch (Exception E) {
				return null;
			}
		}
		
		/** Load a type package as text from a stream IS */
		static public ParserTypePocket loadPocketFromStream(InputStream inputStream) throws IOException {
			var serializable = loadObjectsFromStream(inputStream);
			if (!(serializable instanceof Serializable[])
			 || (((Serializable[])serializable).length != 1)
			 || !(((Serializable[])serializable)[0] instanceof ParserTypePocket))
				throw new RuntimeException("Mal-fomred parser TypeProvider Pocket file.");
			
			return (ParserTypePocket)((Serializable[])serializable)[0];
		}
		
		/** Save this type package as text to a file named FN */
		static public void savePocketToFile(String fileName, ParserTypePocket typePocket) throws IOException {
			var file = new File(fileName);
			savePocketToFile(file, typePocket);
		}
		
		/** Save this type package as text to a file F */
		static public void savePocketToFile(File file, ParserTypePocket typePocket) throws IOException {
			var fileStream = new FileOutputStream(file);
			savePocketToStream(fileStream, typePocket);
		}
		
		/** Save this type package as text to a stream OS */
		static public void savePocketToStream(OutputStream outStream, ParserTypePocket typePocket) throws IOException {
			var array = new Serializable[] { typePocket };
			saveObjectsToStream(outStream, array);
		}
		
		// TypeProvider ----------------------------------------------------------------------------
		
		/** Load a type package as text from a file named FN */
		static public ParserTypeProvider loadAsPocketFromFile(String fileName) throws IOException {
			var file   = new File(fileName);
			var pocket = loadPocketFromFile(file);
			return (pocket == null) ? null : pocket.typeProvider();
		}
		
		/** Load a type package as text from a file F */
		static public ParserTypeProvider loadAsPocketFromFile(File file) throws IOException {
			var pocket = loadPocketFromFile(file);
			return (pocket == null) ? null : pocket.typeProvider();
		}
		
		/** Load a type package as text from a stream IS */
		static public ParserTypeProvider loadAsPocketFromStream(InputStream inputStream) throws IOException {
			var pocket = loadPocketFromStream(inputStream);
			return (pocket == null) ? null : pocket.typeProvider();
		}
		
		/** Save this type package as text to a file named FN */
		public void saveAsPocketToFile(String fileName, ParserTypeProvider typeProvider) throws IOException {
			var typePocket = new ParserTypePocket.Simple(typeProvider);
			savePocketToFile(fileName, typePocket);
		}
		
		/** Save this type package as text to a file F */
		public void saveAsPocketToFile(File file, ParserTypeProvider typeProvider) throws IOException {
			var pocket = new ParserTypePocket.Simple(typeProvider);
			savePocketToFile(file, pocket);
		}
		
		/** Save this type package as text to a stream OS */
		public void saveAsPocketToStream(OutputStream outStream, ParserTypeProvider typeProvider) throws IOException {
			var pocket = new ParserTypePocket.Simple(typeProvider);
			savePocketToStream(outStream, pocket);
		}
	}
}
