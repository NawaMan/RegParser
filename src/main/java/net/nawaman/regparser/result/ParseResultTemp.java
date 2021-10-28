package net.nawaman.regparser.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import net.nawaman.regparser.result.entry.ParseResultEntry;

public class ParseResultTemp extends ParseResult {
    
    static private final long serialVersionUID = 3255656565625655652L;
    
    public ParseResultTemp(ParseResult pFirst) {
        this(pFirst, null);
    }
    
    public ParseResultTemp(ParseResult pFirst, List<ParseResultEntry> resultEntries) {
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
        while (F instanceof ParseResultTemp) {
            Count += F.resultEntrySize();
            F      = ((ParseResultTemp) F).First;
        }
        return F.entryCount() + Count;
    }
    
    @Override
    public ParseResultEntry entryAt(int pIndex) {
        if ((pIndex < 0) || pIndex >= this.entryCount())
            return null;
        if (pIndex < this.First.entryCount()) {
            ParseResultTemp T = this;
            while (pIndex < T.First.entryCount()) {
                if (!(T.First instanceof ParseResultTemp))
                    return T.First.entryAt(pIndex);
                T = (ParseResultTemp) T.First;
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
        if (!(this.First instanceof ParseResultTemp)) {
            var resultEntries = this.entries().collect(Collectors.toList());
            ParseResultTemp T = new ParseResultTemp(this.First.getDuplicate(), resultEntries);
            return T;
        } else {
            Vector<ParseResultTemp> Firsts = new Vector<ParseResultTemp>();
            Firsts.add(this);
            ParseResultTemp F = (ParseResultTemp) this.First;
            Firsts.add(F);
            while (F.First instanceof ParseResultTemp) {
                F = (ParseResultTemp) F.First;
                Firsts.add(F);
            }
            
            var resultEntries = new ArrayList<ParseResultEntry>();
            for (int i = Firsts.size(); --i >= 0;) {
                if (Firsts.get(i).entries().count() == 0) {
                    continue;
                }
                resultEntries.addAll(Firsts.get(i).entryList());
            }
            ParseResultTemp T = new ParseResultTemp(F.First.getDuplicate(), resultEntries);
            return T;
        }
    }
}