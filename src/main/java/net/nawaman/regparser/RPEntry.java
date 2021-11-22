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

import java.io.Serializable;

import net.nawaman.regparser.checkers.WordChecker;

/**
 * Regular Parser Entry
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
abstract public class RPEntry implements Serializable {
    
    static private final long serialVersionUID = 2457845545454544122L;
    
    
    RPEntry() {
    }
    
    static public final RPEntry[] EmptyRPEntryArray = new RPEntry[0];
    
    // Factory -------------------------------------------------------------------------------------
    
    static public RPEntry newParserEntry(Checker pChecker) {
        return new Direct(pChecker);
    }
    
    static public RPEntry newEntry(Checker pChecker) {
        return new Direct(pChecker);
    }
    
    static public RPEntry newParserEntry(Checker pChecker, Quantifier pQuantifier) {
    	return newEntry(pChecker, pQuantifier);
    }
    
    static public RPEntry newEntry(Checker pChecker, Quantifier pQuantifier) {
        if (pQuantifier == null)
            return new Direct(pChecker);
        else
            return new Direct_WithQ(pChecker, pQuantifier);
    }
    
    static public RPEntry _new(String pName, Checker pChecker) {
        if (pName == null)
            return new Direct(pChecker);
        else
            return new NamedDirect(pName, pChecker);
    }
    
    static public RPEntry _new(String pName, Checker pChecker, Quantifier pQuantifier) {
        if (pName == null) {
            if (pQuantifier == null)
                return new Direct(pChecker);
            else
                return new Direct_WithQ(pChecker, pQuantifier);
        } else {
            if (pQuantifier == null)
                return new NamedDirect(pName, pChecker);
            else
                return new NamedDirect_WithQ(pName, pChecker, pQuantifier);
        }
    }
    
    static public RPEntry _new(String pName, Checker pChecker, Quantifier pQuantifier, Checker pSecondStage) {
        if (pSecondStage == null)
            return _new(pName, pChecker, pQuantifier);
        if (pName == null) {
            if (pQuantifier == null)
                return new TwoStage(new Direct(pChecker), pSecondStage);
            else
                return new TwoStage(new Direct_WithQ(pChecker, pQuantifier), pSecondStage);
        } else {
            if (pQuantifier == null)
                return new TwoStage(new NamedDirect(pName, pChecker), pSecondStage);
            else
                return new TwoStage(new NamedDirect_WithQ(pName, pChecker, pQuantifier), pSecondStage);
        }
    }
    
    // TypeRef -------------------------------------------------------------------------------------
    
    static public RPEntry _new(PTypeRef pRPTypeRef) {
        return new TypeRef(pRPTypeRef);
    }
    
    static public RPEntry _new(PTypeRef pRPTypeRef, Quantifier pQuantifier) {
        if (pQuantifier == null)
            return new TypeRef(pRPTypeRef);
        else
            return new TypeRef_WithQ(pRPTypeRef, pQuantifier);
    }
    
    static public RPEntry _new(String pName, PTypeRef pRPTypeRef) {
        if (pName == null)
            return new TypeRef(pRPTypeRef);
        else
            return new NamedTypeRef(pName, pRPTypeRef);
    }
    
    static public RPEntry _new(String pName, PTypeRef pRPTypeRef, Quantifier pQuantifier) {
        if (pName == null) {
            if (pQuantifier == null)
                return new TypeRef(pRPTypeRef);
            else
                return new TypeRef_WithQ(pRPTypeRef, pQuantifier);
        } else {
            if (pQuantifier == null)
                return new NamedTypeRef(pName, pRPTypeRef);
            else
                return new NamedTypeRef_WithQ(pName, pRPTypeRef, pQuantifier);
        }
    }
    
    static public RPEntry _new(String pName, PTypeRef pRPTypeRef, Quantifier pQuantifier, Checker pSecondStage) {
        if (pSecondStage == null)
            return _new(pName, pRPTypeRef, pQuantifier);
        if (pName == null) {
            if (pQuantifier == null)
                return new TwoStage(new TypeRef(pRPTypeRef), pSecondStage);
            else
                return new TwoStage(new TypeRef_WithQ(pRPTypeRef, pQuantifier), pSecondStage);
        } else {
            if (pQuantifier == null)
                return new TwoStage(new NamedTypeRef(pName, pRPTypeRef), pSecondStage);
            else
                return new TwoStage(new NamedTypeRef_WithQ(pName, pRPTypeRef, pQuantifier), pSecondStage);
        }
    }
    
    // Type ----------------------------------------------------------------------------------------
    
    static public RPEntry _new(PType pRPType) {
        return new Typed(pRPType);
    }
    
    static public RPEntry _new(PType pRPType, Quantifier pQuantifier) {
        if (pQuantifier == null)
            return new Typed(pRPType);
        else
            return new Typed_WithQ(pRPType, pQuantifier);
    }
    
    static public RPEntry _new(String pName, PType pRPType) {
        if (pName == null)
            return new Typed(pRPType);
        else
            return new NamedTyped(pName, pRPType);
    }
    
    static public RPEntry _new(String pName, PType pRPType, Quantifier pQuantifier) {
        if (pName == null) {
            if (pQuantifier == null)
                return new Typed(pRPType);
            else
                return new Typed_WithQ(pRPType, pQuantifier);
        } else {
            if (pQuantifier == null)
                return new NamedTyped(pName, pRPType);
            else
                return new NamedTyped_WithQ(pName, pRPType, pQuantifier);
        }
    }
    
    static public RPEntry _new(String pName, PType pRPType, Quantifier pQuantifier, RegParser pSecondStage) {
        if (pSecondStage == null)
            return _new(pName, pRPType, pQuantifier);
        if (pName == null) {
            if (pQuantifier == null)
                return new TwoStage(new Typed(pRPType), pSecondStage);
            else
                return new TwoStage(new Typed_WithQ(pRPType, pQuantifier), pSecondStage);
        } else {
            if (pQuantifier == null)
                return new TwoStage(new NamedTyped(pName, pRPType), pSecondStage);
            else
                return new TwoStage(new NamedTyped_WithQ(pName, pRPType, pQuantifier), pSecondStage);
        }
    }
    
    // Constructor ---------------------------------------------------------------------------------
    
    public String name() {
        return null;
    }
    
    public Checker getChecker() {
        return null;
    }
    
    public PTypeRef typeRef() {
        return null;
    }
    
    public PType type() {
        return null;
    }
    
    public Quantifier getQuantifier() {
        return Quantifier.One;
    }
    
    public RegParser secondStage() {
        return null;
    }
    
    // Object --------------------------------------------------------------------------------------
    
    @Override
    public String toString() {
        StringBuffer SB = new StringBuffer();
        String       N  = this.name();
        Checker      C  = this.getChecker();
        PTypeRef     TR = this.typeRef();
        PType        T  = this.type();
        Quantifier   Q  = this.getQuantifier();
        
        if (T != null) {
            SB.append("(");
            if (N != null)
                SB.append(N).append(":");
            SB.append(T);
            SB.append(")");
            SB.append(Quantifier.toString(Q));
            
        } else
            if (TR != null) {
                SB.append("(");
                if (N != null)
                    SB.append(N).append(":");
                SB.append(TR);
                SB.append(")");
                SB.append(Quantifier.toString(Q));
                
            } else
                if (C instanceof RegParser) {
                    SB.append("(");
                    if (N != null)
                        SB.append(N).append(":~");
                    if ((C instanceof WordChecker) && (!Quantifier.One.equals(Q))) {
                        SB.append("(");
                        SB.append(C);
                        SB.append(")");
                    } else
                        SB.append(C);
                    if (N != null)
                        SB.append("~)");
                    else
                        SB.append(")");
                    SB.append(Quantifier.toString(Q));
                    
                } else
                    if (N != null) {
                        SB.append("(").append(N).append(":~");
                        SB.append(C);
                        SB.append("~)");
                        SB.append(Quantifier.toString(Q));
                        
                    } else {
                        if ((C instanceof WordChecker) && (!Quantifier.One.equals(Q))) {
                            SB.append("(");
                            SB.append(C);
                            SB.append(")");
                        } else
                            SB.append(C);
                        SB.append(Quantifier.toString(Q));
                        
                    }
        return SB.toString();
    }
    
    // Sub Class -----------------------------------------------------------------------------------
    
    // Non-Typed & Non-Named -------------------------------------------------------------
    
    static private class Direct extends RPEntry {
        
        static private final long serialVersionUID = 6546356543546354612L;
        
        protected Direct(Checker pChecker) {
            this.TheChecker = (pChecker instanceof RegParser) ? ((RegParser) pChecker).optimize() : pChecker;
        }
        
        protected Checker TheChecker = null;
        
        @Override
        public Checker getChecker() {
            return this.TheChecker;
        }
    }
    
    static private class Direct_WithQ extends Direct {
        
        static private final long serialVersionUID = 8351352113651352625L;
        
        protected Direct_WithQ(Checker pChecker, Quantifier pQuantifier) {
            super(pChecker);
            this.TheQuantifier = pQuantifier;
        }
        
        protected Quantifier TheQuantifier = null;
        
        @Override
        public Quantifier getQuantifier() {
            return this.TheQuantifier;
        }
    }
    
    // Named -----------------------------------------------------------------------------
    
    static private class NamedDirect extends Direct {
        
        static private final long serialVersionUID = 2245241556456325624L;
        
        protected NamedDirect(String pName, Checker pChecker) {
            super(pChecker);
            this.Name = pName;
        }
        
        String Name = null;
        
        @Override
        public String name() {
            return this.Name;
        }
    }
    
    static private class NamedDirect_WithQ extends NamedDirect {
        
        static private final long serialVersionUID = 1354655635656936565L;
        
        protected NamedDirect_WithQ(String pName, Checker pChecker, Quantifier pQuantifier) {
            super(pName, pChecker);
            this.TheQuantifier = pQuantifier;
        }
        
        protected Quantifier TheQuantifier = null;
        
        @Override
        public Quantifier getQuantifier() {
            return this.TheQuantifier;
        }
    }
    
    // Typed -----------------------------------------------------------------------------
    
    static private class TypeRef extends RPEntry {
        
        static private final long serialVersionUID = 3565652656351262366L;
        
        protected TypeRef(PTypeRef pRPTypeRef) {
            this.TheTypeRef = pRPTypeRef;
            if (this.TheTypeRef == null)
                throw new NullPointerException();
        }
        
        PTypeRef TheTypeRef = null;
        
        @Override
        public PTypeRef typeRef() {
            return this.TheTypeRef;
        }
    }
    
    static private class TypeRef_WithQ extends TypeRef {
        
        static private final long serialVersionUID = 4123563534562456523L;
        
        protected TypeRef_WithQ(PTypeRef pRPTypeRef, Quantifier pQuantifier) {
            super(pRPTypeRef);
            this.TheQuantifier = pQuantifier;
        }
        
        protected Quantifier TheQuantifier = null;
        
        @Override
        public Quantifier getQuantifier() {
            return this.TheQuantifier;
        }
    }
    
    static private class NamedTypeRef extends TypeRef {
        
        static private final long serialVersionUID = 3456412356352456523L;
        
        protected NamedTypeRef(String pName, PTypeRef pRPTypeRef) {
            super(pRPTypeRef);
            this.Name = pName;
        }
        
        String Name = null;
        
        @Override
        public String name() {
            return this.Name;
        }
    }
    
    static private class NamedTypeRef_WithQ extends NamedTypeRef {
        
        static private final long serialVersionUID = 6312456522334564535L;
        
        protected NamedTypeRef_WithQ(String pName, PTypeRef pRPTypeRef, Quantifier pQuantifier) {
            super(pName, pRPTypeRef);
            this.TheQuantifier = pQuantifier;
        }
        
        protected Quantifier TheQuantifier = null;
        
        @Override
        public Quantifier getQuantifier() {
            return this.TheQuantifier;
        }
    }
    
    // Typed -----------------------------------------------------------------------------
    
    static private class Typed extends RPEntry {
        
        static private final long serialVersionUID = 4566522331246354535L;
        
        protected Typed(PType pRPType) {
            this.TheType = pRPType;
            if (this.TheType == null)
                throw new NullPointerException();
        }
        
        PType TheType = null;
        
        @Override
        public PType type() {
            return this.TheType;
        }
    }
    
    static private class Typed_WithQ extends Typed {
        
        static private final long serialVersionUID = 3125454566463522335L;
        
        protected Typed_WithQ(PType pRPType, Quantifier pQuantifier) {
            super(pRPType);
            this.TheQuantifier = pQuantifier;
        }
        
        protected Quantifier TheQuantifier = null;
        
        @Override
        public Quantifier getQuantifier() {
            return this.TheQuantifier;
        }
    }
    
    static private class NamedTyped extends Typed {
        
        static private final long serialVersionUID = 6312352354545266435L;
        
        protected NamedTyped(String pName, PType pRPType) {
            super(pRPType);
            this.Name = pName;
        }
        
        String Name = null;
        
        @Override
        public String name() {
            return this.Name;
        }
    }
    
    static private class NamedTyped_WithQ extends NamedTyped {
        
        static private final long serialVersionUID = 2613235452356436455L;
        
        protected NamedTyped_WithQ(String pName, PType pRPType, Quantifier pQuantifier) {
            super(pName, pRPType);
            this.TheQuantifier = pQuantifier;
        }
        
        protected Quantifier TheQuantifier = null;
        
        @Override
        public Quantifier getQuantifier() {
            return this.TheQuantifier;
        }
    }
    
    // TwoStage --------------------------------------------------------------------------------------------------------
    
    static private class TwoStage extends RPEntry {
        
        static private final long serialVersionUID = 2636435413256452355L;
        
        TwoStage(RPEntry pDelegate, Checker pChecker) {
            this.Delegate = pDelegate;
            this.Parser   = (pChecker instanceof RegParser) ? (RegParser) pChecker : RegParser.newRegParser(pChecker);
        }
        
        RPEntry   Delegate;
        RegParser Parser;
        
        @Override
        public String name() {
            return this.Delegate.name();
        }
        
        @Override
        public Checker getChecker() {
            return this.Delegate.getChecker();
        }
        
        @Override
        public PTypeRef typeRef() {
            return this.Delegate.typeRef();
        }
        
        @Override
        public PType type() {
            return this.Delegate.type();
        }
        
        @Override
        public Quantifier getQuantifier() {
            return this.Delegate.getQuantifier();
        }
        
        @Override
        public RegParser secondStage() {
            return this.Parser;
        }
    }
}
