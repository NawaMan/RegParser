package net.nawaman.regparser.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import net.nawaman.regparser.result.entry.PREntry;

public class PRTemp extends ParseResult {
    
    static private final long serialVersionUID = 3255656565625655652L;
    
    public PRTemp(ParseResult pFirst) {
        this(pFirst, null);
    }
    
    public PRTemp(ParseResult pFirst, List<PREntry> resultEntries) {
        super(resultEntries);
        this.First = pFirst;
    }
    
    ParseResult First = null;
    
    public ParseResult first() {
        return First;
    }
    
    @Override
    public int entryCount() {
        int         Count = super.entryCount();
        ParseResult F     = this.First;
        while (F instanceof PRTemp) {
            Count += F.resultEntrySize();
            F      = ((PRTemp) F).First;
        }
        return F.entryCount() + Count;
    }
    
    @Override
    public PREntry entryAt(int pIndex) {
        if ((pIndex < 0) || pIndex >= this.entryCount())
            return null;
        if (pIndex < this.First.entryCount()) {
            PRTemp T = this;
            while (pIndex < T.First.entryCount()) {
                if (!(T.First instanceof PRTemp))
                    return T.First.entryAt(pIndex);
                T = (PRTemp) T.First;
            }
            return T.entryAt(pIndex);
        }
        return this.entries().skip(pIndex - this.First.entryCount()).findFirst().orElse(null);
    }
    
    @Override
    public int startPosition() {
        return this.First.startPosition();
    }
    
    @Override
    public CharSequence originalText() {
        return this.First.originalText();
    }
    
    @Override
    public ParseResult getDuplicate() {
        // This was initially implement using recursive but it was too slow.
        // The optimization is done by going to the root or the first 'First' part that is not a Temp and then all
        //     all entries from then down to the current Temp Result.
        if (!(this.First instanceof PRTemp)) {
            var resultEntries = this.entries().collect(Collectors.toList());
            PRTemp T = new PRTemp(this.First.getDuplicate(), resultEntries);
            return T;
        } else {
            Vector<PRTemp> Firsts = new Vector<PRTemp>();
            Firsts.add(this);
            PRTemp F = (PRTemp) this.First;
            Firsts.add(F);
            while (F.First instanceof PRTemp) {
                F = (PRTemp) F.First;
                Firsts.add(F);
            }
            
            var resultEntries = new ArrayList<PREntry>();
            for (int i = Firsts.size(); --i >= 0;) {
                if (Firsts.get(i).entries().count() == 0) {
                    continue;
                }
                resultEntries.addAll(Firsts.get(i).entryList());
            }
            PRTemp T = new PRTemp(F.First.getDuplicate(), resultEntries);
            return T;
        }
    }
}