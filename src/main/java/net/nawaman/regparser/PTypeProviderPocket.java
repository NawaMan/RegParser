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
public interface PTypeProviderPocket extends Serializable {
	
	public PTypeProvider getTProvider();
	
	public boolean isRepackagingNeeded();
	
	/** Simple implementation of PTypeProviderPocket */
	@SuppressWarnings("serial")
	static public class Simple implements PTypeProviderPocket {
		
		public Simple(PTypeProvider pTProvider) {
			this.TProvider = pTProvider;
		}
		
		transient boolean       isRepackagingNeeded = false;
		transient PTypeProvider TProvider           = null;
		
		public PTypeProvider getTProvider() {
			return this.TProvider;
		}
		
		public boolean isRepackagingNeeded() {
			return this.isRepackagingNeeded;
		}
		
		/** Custom deserialization is needed. */
		private void readObject(ObjectInputStream aStream) throws IOException, ClassNotFoundException {
			// Save the rest
			aStream.defaultReadObject();
			
			try {
				ObjectInputStream IOS = new ObjectInputStream(new ByteArrayInputStream((byte[])aStream.readObject()));
				// Load the body
				this.TProvider = (PTypeProvider)IOS.readObject();
			} catch(Exception E) {
				this.isRepackagingNeeded = true;
			}
		}
		
		/** Custom serialization is needed. */
		private void writeObject(ObjectOutputStream aStream) throws IOException {
			// Save the rest
			aStream.defaultWriteObject();
			
			// Save the Kinds and Types
			ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
			ObjectOutputStream    OOS  = new ObjectOutputStream(BAOS);

			// Save the body function
			try {
				OOS.writeObject(this.TProvider);
				OOS.close();
				aStream.writeObject(BAOS.toByteArray());
			} catch(Exception E) {
			} finally { OOS.close(); }
		}
		
		// Utilities ---------------------------------------------------------------------------------------------------

		// File ----------------------------------------------------------------------------------
		
		/** Load a type package as text from a file named FN */
		static public PTypeProviderPocket loadPocketFromFile(String FN) throws IOException {
			return loadPocketFromFile(new File(FN));
		}

		/** Load a type package as text from a file F */
		static public PTypeProviderPocket loadPocketFromFile(File F) throws IOException {
			try { return loadPocketFromStream(new FileInputStream(F)); }
			catch(Exception E) { return null; }
		}

		/** Load a type package as text from a stream IS */
		static public PTypeProviderPocket loadPocketFromStream(InputStream IS) throws IOException {
			Serializable S = Util.loadObjectsFromStream(IS);
			if(!(S instanceof Serializable[]) ||
				(((Serializable[])S).length != 1) ||
				!(((Serializable[])S)[0] instanceof PTypeProviderPocket))
				throw new RuntimeException("Mal-fomred parser TypeProvider Pocket file.");
				
			return (PTypeProviderPocket)((Serializable[])S)[0];
		}
		
		/** Save this type package as text to a file named FN */
		static public void savePocketToFile(String FN, PTypeProviderPocket pPTPocket) throws IOException {
			savePocketToFile(new File(FN), pPTPocket);
		}

		/** Save this type package as text to a file F */
		static public void savePocketToFile(File F, PTypeProviderPocket pPTPocket) throws IOException {
			savePocketToStream(new FileOutputStream(F), pPTPocket);
		}

		/** Save this type package as text to a stream OS */
		static public void savePocketToStream(OutputStream OS, PTypeProviderPocket pPTPocket) throws IOException {
			Util.saveObjectsToStream(OS, new Serializable[] { pPTPocket });
		}

		// TypeProvider ----------------------------------------------------------------------------
		
		/** Load a type package as text from a file named FN */
		static public PTypeProvider loadAsPocketFromFile(String FN) throws IOException {
			PTypeProviderPocket PTPP = loadPocketFromFile(new File(FN));
			return (PTPP == null)?null:PTPP.getTProvider();
		}

		/** Load a type package as text from a file F */
		static public PTypeProvider loadAsPocketFromFile(File F) throws IOException {
			PTypeProviderPocket PTPP = loadPocketFromFile(F);
			return (PTPP == null)?null:PTPP.getTProvider();
		}

		/** Load a type package as text from a stream IS */
		static public PTypeProvider loadAsPocketFromStream(InputStream IS) throws IOException {
			PTypeProviderPocket PTPP = loadPocketFromStream(IS);
			return (PTPP == null)?null:PTPP.getTProvider();
		}
		
		/** Save this type package as text to a file named FN */
		public void saveAsPocketToFile(String FN, PTypeProvider pPTProvider) throws IOException {
			savePocketToFile(FN, new PTypeProviderPocket.Simple(pPTProvider));
		}

		/** Save this type package as text to a file F */
		public void saveAsPocketToFile(File F, PTypeProvider pPTProvider) throws IOException {
			savePocketToFile(F, new PTypeProviderPocket.Simple(pPTProvider));
		}

		/** Save this type package as text to a stream OS */
		public void saveAsPocketToStream(OutputStream OS, PTypeProvider pPTProvider) throws IOException {
			savePocketToStream(OS, new PTypeProviderPocket.Simple(pPTProvider));
		}
	}
}
