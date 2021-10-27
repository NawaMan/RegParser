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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import net.nawaman.regparser.types.PTComposable;
import net.nawaman.regparser.types.PTJavaChecker;
import net.nawaman.regparser.types.PTTextCI;

/**
 * Supply types to the parser
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public interface PTypeProvider extends Serializable {
    
    /** Returns type from name */
    public PType getType(String pName);
    
    /** Returns the names of all types in this provider */
    public Set<String> getAllTypeNames();
    
    /** Returns the names of all types in this provider */
    public Set<String> getAllErrorMessageNames();
    
    /** Get an error message  */
    public String getErrorMessage(String pErrName);
    
    // Sub classes -----------------------------------------------------------------------------------------------------
    
    @SuppressWarnings("serial")
    static public class Simple implements PTypeProvider {
        
        /** Include the type pPT to exclusively be a member of the provider pTP */
        static public boolean exclusivelyInclude(PTypeProvider pTP, PType pPT) {
            if ((pPT == null) || (pPT.TProvider != null))
                return false;
            pPT.TProvider = pTP;
            return true;
        }
        
        static private PTypeProvider.Extensible Default = null;
        
        static public final PTypeProvider.Extensible getDefault() {
            if (Default != null)
                return Default;
            Default = new PTypeProvider.Extensible();
            Default.addRPType(new PTTextCI());
            Default.addRPType(new PTBackRef());
            Default.addRPType(new PTBackRefCI());
            Default.addRPType(new PTJavaChecker());
            return Default;
        }
        
        /** Constructs an empty type provider */
        protected Simple() {
        }
        
        /** Constructs a type provider with the types */
        public Simple(PType... pTypes) {
            if (pTypes == null)
                return;
            this.RPTypes = new Hashtable<String, PType>();
            for (PType T : pTypes) {
                if (T == null)
                    continue;
                this.RPTypes.put(T.name(), T);
            }
        }
        
        Hashtable<String, PType>  RPTypes = null;
        Hashtable<String, String> ErrMsgs = null;
        
        // Services --------------------------------------------------------------------------------------------------------
        
        // Type --------------------------------------------------------------------------
        
        public Set<String> getAllTypeNames() {
            if (this.RPTypes == null)
                return null;
            return this.RPTypes.keySet();
        }
        
        protected boolean addRPType(PType pRPT) {
            if (pRPT == null)
                return false;
            if ((this.RPTypes != null) && (this.RPTypes.containsKey(pRPT.name())))
                return false;
            
            if (this.RPTypes == null)
                this.RPTypes = new Hashtable<String, PType>();
            this.RPTypes.put(pRPT.name(), pRPT);
            return true;
        }
        
        protected boolean removeRPType(PType pRPT) {
            if (pRPT == null)
                return false;
            if ((this.RPTypes != null) && (this.RPTypes.containsKey(pRPT.name())))
                return false;
            
            if (this.RPTypes == null)
                this.RPTypes = new Hashtable<String, PType>();
            this.RPTypes.remove(pRPT.name());
            return true;
        }
        
        public PType getType(String pName) {
            if (pName == null)
                return null;
            if (this.RPTypes == null)
                return null;
            return this.RPTypes.get(pName);
        }
        
        // Error -------------------------------------------------------------------------
        
        /** Returns the names of all types in this provider */
        public Set<String> getAllErrorMessageNames() {
            if (this.ErrMsgs == null)
                return null;
            return this.ErrMsgs.keySet();
        }
        
        protected boolean addErrorMessage(String pErrName, String pErrMsg) {
            if (pErrName == null)
                return false;
            if (pErrMsg == null)
                return false;
            if ((this.ErrMsgs != null) && (this.ErrMsgs.containsKey(pErrName)))
                return false;
            
            if (this.ErrMsgs == null)
                this.ErrMsgs = new Hashtable<String, String>();
            this.ErrMsgs.put(pErrName, pErrMsg);
            return true;
        }
        
        protected boolean removeRPType(String pErrName) {
            if (pErrName == null)
                return false;
            if ((this.ErrMsgs != null) && (this.ErrMsgs.containsKey(pErrName)))
                return false;
            
            if (this.ErrMsgs == null)
                this.ErrMsgs = new Hashtable<String, String>();
            this.ErrMsgs.remove(pErrName);
            return true;
        }
        
        /** Get an error message  */
        public String getErrorMessage(String pName) {
            if (pName == null)
                return null;
            if (this.ErrMsgs == null)
                return null;
            return this.ErrMsgs.get(pName);
        }
        
        // ToString ----------------------------------------------------------------------------------------------------
        
        @Override
        public String toString() {
            return (this.RPTypes == null) ? "{=}" : RPTypes.toString();
        }
        
        // Load and Save -----------------------------------------------------------------------------------------------
        
        /** Load type provider from a stream */
        static public PTypeProvider loadTypeProviderFromStream(InputStream pIS) throws IOException {
            PType[] Ts = loadTypesFromStream(pIS);
            return new Simple(Ts);
        }
        
        /** Load types into a type provider from a stream */
        static public int loadTypeProviderFromStream(InputStream pIS, PTypeProvider pProvider, boolean isToReplace)
                throws IOException {
            PType[] Ts = loadTypesFromStream(pIS);
            if (Ts == null)
                return 0;
            if (pProvider == null)
                return -1;
            
            if ((Ts.length != 0) && (((Simple) pProvider).RPTypes == null))
                ((Simple) pProvider).RPTypes = new Hashtable<String, PType>();
            
            int t = 0;
            for (int i = Ts.length; --i >= 0;) {
                PType T = Ts[i];
                if (T == null)
                    continue;
                String N = T.name();
                
                if (!isToReplace && ((Simple) pProvider).RPTypes.containsKey(N))
                    ((Simple) pProvider).RPTypes.put(N, T);
                t++;
            }
            return t;
        }
        
        /** Load types a stream */
        static public PType[] loadTypesFromStream(InputStream pIS) throws IOException {
            Object O = Util.loadObjectsFromStream(pIS);
            if (!((O instanceof Serializable[]) && (((Serializable[]) O).length != 0)))
                throw new IOException("The selected file is mal-formed.");
            
            Serializable[]        Ss        = (Serializable[]) O;
            Vector<PType>         Types     = new Vector<PType>();
            Vector<PTypeProvider> Providers = new Vector<PTypeProvider>();
            for (Serializable S : Ss) {
                if (!(S instanceof PType) && !(S instanceof PTypeProvider))
                    continue;
                if (S instanceof PType)
                    Types.add((PType) S);
                else
                    Providers.add((PTypeProvider) S);
            }
            
            for (PTypeProvider Provider : Providers) {
                Set<String> TNames = Provider.getAllTypeNames();
                if (TNames != null) {
                    for (String TName : TNames) {
                        PType T = Provider.getType(TName);
                        if (T == null)
                            continue;
                        Types.add(T);
                    }
                }
            }
            
            return Types.toArray(PType.EmptyTypeArray);
        }
        
        /** Save a type provider to the stream */
        static public void saveRPTypeProviderToStream(OutputStream pOS, PTypeProvider pProvider) throws IOException {
            Util.saveObjectsToStream(pOS, new Serializable[] { pProvider });
        }
        
        /** Save a type provider to the stream */
        static public void saveRPTypesToStream(OutputStream pOS, PType[] pTypes) throws IOException {
            Util.saveObjectsToStream(pOS, pTypes);
        }
    }
    
    @SuppressWarnings("serial")
    static public class Extensible extends Simple {
        /** Constructs an empty type provider */
        public Extensible() {
        }
        
        /** Constructs a type provider with the types */
        public Extensible(PType... pTypes) {
            super(pTypes);
        }
        
        @Override
        public boolean addRPType(PType pRPT) {
            return super.addRPType(pRPT);
        }
        
        @Override
        public boolean addErrorMessage(String pErrName, String pErrMsg) {
            return super.addErrorMessage(pErrName, pErrMsg);
        }
        
        // Add Type exclusively ----------------------------------------------------------------------------------------
        
        public boolean addType(PType pRPT) {
            if ((pRPT != null) && (pRPT.TProvider == null))
                pRPT.TProvider = this;
            return super.addRPType(pRPT);
        }
        
        public boolean addType(String pName, Checker pChecker) {
            return this.addType(pName, pChecker, null, null);
        }
        
        public boolean addType(String pName, Checker pChecker, ResultVerifier pVertifier) {
            return this.addType(pName, pChecker, pVertifier, null);
        }
        
        public boolean addType(String pName, Checker pChecker, RPCompiler pCompiler) {
            return this.addType(pName, pChecker, null, pCompiler);
        }
        
        public boolean addType(String pName, Checker pChecker, ResultVerifier pVertifier, RPCompiler pCompiler) {
            if (pChecker == null)
                return false;
            if (pChecker instanceof RegParser)
                pChecker = RegParser.WithDefaultTypeProvider.attachDefaultTypeProvider((RegParser) pChecker, this);
            else
                pChecker = RegParser.newRegParser(this, pChecker);
            return this.addType(new PTComposable(pName, pChecker, pVertifier, pCompiler));
        }
        
        
        public boolean addType(String pName, String pRegParser) {
            return this.addType(pName, pRegParser, null, null);
        }
        
        public boolean addType(String pName, String pRegParser, ResultVerifier pVertifier) {
            return this.addType(pName, pRegParser, pVertifier, null);
        }
        
        public boolean addType(String pName, String pRegParser, RPCompiler pCompiler) {
            return this.addType(pName, pRegParser, null, pCompiler);
        }
        
        public boolean addType(String pName, String pRegParser, ResultVerifier pVertifier, RPCompiler pCompiler) {
            RegParser RP = RegParser.newRegParser(this, pRegParser);
            if (RP == null)
                return false;
            return this.addType(new PTComposable(pName, RP, pVertifier, pCompiler));
        }
    }
    
    @SuppressWarnings("serial")
    static public class Library extends Simple {
        
        /** Returns the TypeProvider that include either providers */
        static public PTypeProvider getEither(PTypeProvider First, PTypeProvider Second) {
            if (First == null)
                return Second;
            if (Second == null)
                return First;
            return new Library(First, Second);
        }
        
        public Library(PType[] pTypes, PTypeProvider... pProviders) {
            super(pTypes);
            if (pProviders == null)
                return;
            this.Providers = new Vector<PTypeProvider>();
            for (int i = 0; i < pProviders.length; i++) {
                if (pProviders[i] == null)
                    continue;
                this.Providers.add(pProviders[i]);
            }
        }
        
        public Library(PTypeProvider... pProviders) {
            this(null, pProviders);
        }
        
        Vector<PTypeProvider> Providers;
        
        public void addProvider(PTypeProvider pProvider) {
            if (pProvider == null)
                return;
            if (this.Providers == null)
                this.Providers = new Vector<PTypeProvider>();
            for (int i = 0; i < this.Providers.size(); i++) {
                if (this.Providers.get(i) == pProvider)
                    return;
            }
            this.Providers.add(pProvider);
        }
        
        public void removeProvider(PTypeProvider pProvider) {
            if (pProvider == null)
                return;
            if (this.Providers == null)
                return;
            for (int i = 0; i < this.Providers.size(); i++) {
                if (this.Providers.get(i) == pProvider) {
                    this.Providers.remove(i);
                    return;
                }
            }
        }
        
        // Override of the services -----------------------------------------------------------------------------------
        
        @Override
        public Set<String> getAllTypeNames() {
            HashSet<String> Names = new HashSet<String>();
            if (this.RPTypes != null)
                Names.addAll(this.RPTypes.keySet());
            if (this.Providers != null) {
                for (int i = 0; i < this.Providers.size(); i++) {
                    Set<String> Ns = this.Providers.get(i).getAllTypeNames();
                    if (Ns != null)
                        Names.addAll(Ns);
                }
            }
            return Names;
        }
        
        @Override
        public PType getType(String pName) {
            if (this.RPTypes != null) {
                PType RPT = this.RPTypes.get(pName);
                if (RPT != null)
                    return RPT;
            }
            if (this.Providers != null) {
                for (int i = 0; i < this.Providers.size(); i++) {
                    PType RPT = this.Providers.get(i).getType(pName);
                    if (RPT != null)
                        return RPT;
                }
            }
            return null;
        }
        
        @Override
        public Set<String> getAllErrorMessageNames() {
            HashSet<String> Names = new HashSet<String>();
            if (this.ErrMsgs != null)
                Names.addAll(this.ErrMsgs.keySet());
            if (this.Providers != null) {
                for (int i = 0; i < this.Providers.size(); i++) {
                    Set<String> Ns = this.Providers.get(i).getAllErrorMessageNames();
                    if (Ns != null)
                        Names.addAll(Ns);
                }
            }
            return Names;
        }
        
        @Override
        public String getErrorMessage(String pErrName) {
            if (this.ErrMsgs != null) {
                String ErrMsg = this.ErrMsgs.get(pErrName);
                if (ErrMsg != null)
                    return ErrMsg;
            }
            if (this.Providers != null) {
                for (int i = 0; i < this.Providers.size(); i++) {
                    String ErrMsg = this.Providers.get(i).getErrorMessage(pErrName);
                    if (ErrMsg != null)
                        return ErrMsg;
                }
            }
            return null;
        }
        
        @Override
        public String toString() {
            StringBuffer SB = new StringBuffer();
            SB.append("{");
            SB.append((this.RPTypes == null) ? "{=}" : RPTypes.toString());
            if (this.Providers != null) {
                for (int i = 0; i < this.Providers.size(); i++) {
                    PTypeProvider TP = this.Providers.get(i);
                    if (TP == null)
                        continue;
                    SB.append("; ");
                    SB.append(TP.toString());
                }
            }
            SB.append("}");
            return SB.toString();
        }
    }
    
}
