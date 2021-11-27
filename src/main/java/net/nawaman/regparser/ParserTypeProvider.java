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

import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.utils.Util.loadObjectsFromStream;
import static net.nawaman.regparser.utils.Util.saveObjectsToStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.nawaman.regparser.types.PTComposable;
import net.nawaman.regparser.types.PTJavaChecker;
import net.nawaman.regparser.types.PTTextCI;

/**
 * Supply types to the parser
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public interface ParserTypeProvider extends Serializable {
	
	/** Returns type from name */
	public ParserType type(String name);
	
	/** Returns the names of all types in this provider */
	public Set<String> typeNames();
	
	/** Get an error message  */
	public String errorMessage(String errorName);
	
	/** Returns the names of all types in this provider */
	public Set<String> errorMessageNames();
	
	// Sub classes -----------------------------------------------------------------------------------------------------
	
	@SuppressWarnings("serial")
	static public class Simple implements ParserTypeProvider {
		
		/** Include the type to exclusively be a member of the provider */
		public static boolean exclusivelyInclude(ParserTypeProvider provider, ParserType type) {
			if ((type == null)
			 || (type.typeProvider() != null))
				return false;
			
			type.setTypeProvider(provider);
			return true;
		}
		
		private static ParserTypeProvider.Extensible defaultProvider = null;
		
		public static final ParserTypeProvider.Extensible defaultProvider() {
			if (defaultProvider != null)
				return defaultProvider;
			
			defaultProvider = new ParserTypeProvider.Extensible();
			defaultProvider.addType(new PTTextCI());
			defaultProvider.addType(new ParserTypeBackRef());
			defaultProvider.addType(new ParserTypeBackRefCI());
			defaultProvider.addType(new PTJavaChecker());
			return defaultProvider;
		}
		
		private final Map<String, ParserType> types;
		private final Map<String, String>     errorMessages;
		
		/** Constructs an empty type provider */
		protected Simple() {
			this(new ParserType[0]);
		}
		
		/** Constructs a type provider with the types */
		public Simple(ParserType... types) {
			this.types         = new HashMap<String, ParserType>();
			this.errorMessages = new HashMap<String, String>();
			if (types == null) {
				return;
			}
			for (var type : types) {
				if (type == null)
					continue;
				
				this.types.put(type.name(), type);
			}
		}
		
		// Services ----------------------------------------------------------------------------------------------------
		
		// Type --------------------------------------------------------------------------
		
		public Set<String> typeNames() {
			return types.keySet();
		}
		
		protected boolean addType(ParserType type) {
			if (type == null)
				return false;
			
			var name = type.name();
			if (types.containsKey(name))
				return false;
			
			types.put(name, type);
			return true;
		}
		
		protected boolean removeType(ParserType type) {
			if (type == null)
				return false;
			
			var name = type.name();
			if (types.containsKey(name))
				return false;
			
			types.remove(name);
			return true;
		}
		
		protected boolean removeType(String name) {
			if (name == null)
				return false;
			
			if (types.containsKey(name))
				return false;
			
			types.remove(name);
			return true;
		}
		
		public ParserType type(String name) {
			if (name == null)
				return null;
			
			return types.get(name);
		}
		
		// Error -------------------------------------------------------------------------
		
		/** Returns the names of all types in this provider */
		public Set<String> errorMessageNames() {
			return errorMessages.keySet();
		}
		
		protected boolean addErrorMessage(String errorName, String errorMessage) {
			if (errorName == null)
				return false;
			
			if (errorMessage == null)
				return false;
			
			if (errorMessages.containsKey(errorName))
				return false;
			
			errorMessages.put(errorName, errorMessage);
			return true;
		}
		
		protected boolean removeError(String errorName) {
			if (errorName == null)
				return false;
			
			var previous = errorMessages.remove(errorName);
			return (previous != null);
		}
		
		/** Get an error message  */
		public String errorMessage(String name) {
			if (name == null)
				return null;
			
			if (errorMessages == null)
				return null;
			
			return errorMessages.get(name);
		}
		
		// ToString ----------------------------------------------------------------------------------------------------
		
		@Override
		public String toString() {
			return this.types.isEmpty() ? "{=}" : types.toString();
		}
		
		// Load and Save -----------------------------------------------------------------------------------------------
		
		/** Load type provider from a stream */
		static public ParserTypeProvider loadTypeProviderFromStream(InputStream inputStream) throws IOException {
			var types = loadTypesFromStream(inputStream);
			return new Simple(types);
		}
		
		/** Load types into a type provider from a stream */
		static public int loadTypeProviderFromStream(
							InputStream        inputStream,
							ParserTypeProvider typeProvider,
							boolean            isToReplace)
								throws IOException {
			var types = loadTypesFromStream(inputStream);
			if (types == null)
				return 0;
			
			if (typeProvider == null)
				return -1;
			
			int t = 0;
			for (int i = types.length; --i >= 0;) {
				var type = types[i];
				if (type == null)
					continue;
				
				var name = type.name();
				if (!isToReplace
				 && ((Simple)typeProvider).types.containsKey(name)) {
					((Simple)typeProvider).types.put(name, type);
				}
				
				t++;
			}
			return t;
		}
		
		/** Load types a stream */
		static public ParserType[] loadTypesFromStream(InputStream inputStream) throws IOException {
			var object = loadObjectsFromStream(inputStream);
			if (!((object instanceof Serializable[])
			 && (((Serializable[])object).length != 0)))
				throw new IOException("The selected file is mal-formed.");
			
			var objects   = (Serializable[])object;
			var types     = new ArrayList<ParserType>();
			var providers = new ArrayList<ParserTypeProvider>();
			for (var serializable : objects) {
				if (!(serializable instanceof ParserType)
				 && !(serializable instanceof ParserTypeProvider))
					continue;
				
				if (serializable instanceof ParserType) {
					types.add((ParserType)serializable);
				} else {
					providers.add((ParserTypeProvider)serializable);
				}
			}
			
			for (var provider : providers) {
				var typeNames = provider.typeNames();
				if (typeNames == null)
					continue;
				
				for (var typeName : typeNames) {
					var type = provider.type(typeName);
					if (type == null)
						continue;
					
					types.add(type);
				}
			}
			
			return types.toArray(ParserType.EmptyTypeArray);
		}
		
		/** Save a type provider to the stream */
		static public void saveTypeProviderToStream(
							OutputStream       outputStream,
							ParserTypeProvider typeProvider)
								throws IOException {
			saveObjectsToStream(outputStream, new Serializable[] { typeProvider });
		}
		
		/** Save a type provider to the stream */
		static public void saveTypesToStream(OutputStream outputStream, ParserType[] types) throws IOException {
			saveObjectsToStream(outputStream, types);
		}
	}
	
	@SuppressWarnings("serial")
	static public class Extensible extends Simple {
		/** Constructs an empty type provider */
		public Extensible() {
		}
		
		/** Constructs a type provider with the types */
		public Extensible(ParserType... types) {
			super(types);
		}
		
		// Add Type exclusively ----------------------------------------------------------------------------------------
		
		@Override
		public boolean addType(ParserType type) {
			if ((type != null)
			 && (type.typeProvider() == null))
				type.setTypeProvider(this);
			
			return super.addType(type);
		}
		
		public boolean addType(String name, Checker checker) {
			return addType(name, checker, null, null);
		}
		
		public boolean addType(String name, Checker checker, ResultVerifier vertifier) {
			return addType(name, checker, vertifier, null);
		}
		
		public boolean addType(String name, Checker checker, ResultCompiler compiler) {
			return addType(name, checker, null, compiler);
		}
		
		public boolean addType(String name, Checker checker, ResultVerifier vertifier, ResultCompiler compiler) {
			if (checker == null)
				return false;
			
			checker = (checker instanceof RegParser)
			        ? RegParser.WithDefaultTypeProvider.attachDefaultTypeProvider((RegParser)checker, this)
			        : RegParser.newRegParser(this, checker);
			
			return addType(new PTComposable(name, checker, vertifier, compiler));
		}

		public boolean addType(String name, String regParser) {
			return addType(name, regParser, null, null);
		}
		
		public boolean addType(String name, String regParser, ResultVerifier vertifier) {
			return addType(name, regParser, vertifier, null);
		}
		
		public boolean addType(String name, String regParser, ResultCompiler compiler) {
			return addType(name, regParser, null, compiler);
		}
		
		public boolean addType(String name, String regParser, ResultVerifier vertifier, ResultCompiler compiler) {
			var parser = newRegParser(this, regParser);
			return addType(new PTComposable(name, parser, vertifier, compiler));
		}
		
		public boolean addType(String name, RegParser parser) {
			return addType(name, parser, null, null);
		}
		
		public boolean addType(String name, RegParser parser, ResultVerifier vertifier) {
			return addType(name, parser, vertifier, null);
		}
		
		public boolean addType(String name, RegParser parser, ResultCompiler compiler) {
			return addType(name, parser, null, compiler);
		}
		
		public boolean addType(String name, RegParser parser, ResultVerifier vertifier, ResultCompiler compiler) {
			var type = new PTComposable(name, parser, vertifier, compiler);
			return addType(type);
		}
	}
	
	@SuppressWarnings("serial")
	static public class Library extends Simple {
		
		/** Returns the TypeProvider that include either providers */
		static public ParserTypeProvider either(ParserTypeProvider firstProvider, ParserTypeProvider secondProvider) {
			if (firstProvider == null)
				return secondProvider;
			
			if (secondProvider == null)
				return firstProvider;
			
			return new Library(firstProvider, secondProvider);
		}
		
		private final List<ParserTypeProvider> providers = new ArrayList<ParserTypeProvider>();
		
		public Library(ParserType[] types, ParserTypeProvider... typeProviders) {
			super(types);
			
			if (typeProviders == null)
				return;
			
			for (var typeProvider : typeProviders) {
				if (typeProvider == null)
					continue;
				
				providers.add(typeProvider);
			}
		}
		
		public Library(ParserTypeProvider... typeProviders) {
			this(null, typeProviders);
		}
		
		public void addProvider(ParserTypeProvider typeProvider) {
			if (typeProvider == null)
				return;
			
			for (var provider : providers) {
				if (provider == typeProvider)
					return;
			}
			
			providers.add(typeProvider);
		}
		
		public void removeProvider(ParserTypeProvider typeProvider) {
			if (typeProvider == null)
				return;
			
			providers.remove(typeProvider);
		}
		
		// Override of the services -----------------------------------------------------------------------------------
		
		@Override
		public Set<String> typeNames() {
			var names = new HashSet<String>();
			names.addAll(typeNames());
			
			for (var provider : providers) {
				var typeNames = provider.typeNames();
				if (typeNames != null) {
					names.addAll(typeNames);
				}
			}
			return names;
		}
		
		@Override
		public ParserType type(String name) {
			var type = super.type(name);
			if (type != null)
				return type;
			
			for (var provider : providers) {
				type = provider.type(name);
				if (type != null)
					return type;
			}
			return null;
		}
		
		@Override
		public Set<String> errorMessageNames() {
			var names = new HashSet<String>();
			names.addAll(errorMessageNames());
			
			for (var provider : providers) {
				var moreErrorNames = provider.errorMessageNames();
				if (moreErrorNames != null) {
					names.addAll(moreErrorNames);
				}
			}
			return names;
		}
		
		@Override
		public String errorMessage(String errorName) {
			var errorMessage = errorMessage(errorName);
			if (errorMessage != null)
				return errorMessage;
			
			for (var provider : providers) {
				errorMessage = provider.errorMessage(errorName);
				if (errorMessage != null)
					return errorMessage;
			}
			return null;
		}
		
		@Override
		public String toString() {
			var buffer = new StringBuffer();
			buffer.append("{");
			buffer.append(super.toString());
			for (var provider : providers) {
				if (provider == null)
					continue;
				
				buffer.append("; ");
				buffer.append(provider.toString());
			}
			buffer.append("}");
			return buffer.toString();
		}
	}
	
}
