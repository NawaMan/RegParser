package net.nawaman.regparser.result;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import net.nawaman.regparser.result.entry.PREntry;

/**
 * Temporary parse result.
 * 
 * @author nawa
 */
public class PRTemp extends ParseResult {
    
    static private final long serialVersionUID = 3255656565625655652L;
    
    private final ParseResult first;
    
    PRTemp(ParseResult first) {
        this(first, null);
    }
    
    private PRTemp(ParseResult first, List<PREntry> resultEntries) {
        super(null);
        this.first = first;
    }
    
    public ParseResult first() {
        return first;
    }
    
    @Override
    public int entryCount() {
        int count = super.entryCount();
        var first = this.first;
        while (first instanceof PRTemp) {
            count += first.rawEntryCount();
            first =  ((PRTemp) first).first;
        }
        return first.entryCount() + count;
    }
    
    @Override
    public PREntry entryAt(int index) {
        if ((index < 0) || index >= entryCount()) {
            return null;
        }
        if (index < first.entryCount()) {
            var temp = this;
            while (index < temp.first.entryCount()) {
                if (!(temp.first instanceof PRTemp)) {
                    return temp.first.entryAt(index);
                }
                
                temp = (PRTemp)temp.first;
            }
            return temp.entryAt(index);
        }
        return entries()
                .skip(index - first.entryCount())
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public int startPosition() {
        return first.startPosition();
    }
    
    @Override
    public CharSequence originalText() {
        return first.originalText();
    }
    
    @Override
    public ParseResult duplicate() {
        // This was initially implement using recursive but it was too slow.
        // The optimization is done by going to the root or the first 'First' part that is not a Temp and then all
        //     all entries from then down to the current Temp Result.
        if (!(first instanceof PRTemp)) {
            var resultEntries  = entries().collect(toList());
            var duplicateFirst = first.duplicate();
            return new PRTemp(duplicateFirst, resultEntries);
        }
        
        var firsts = new ArrayList<PRTemp>();
        firsts.add(this);
        
        var first = (PRTemp) this.first;
        firsts.add(first);
        
        while (first.first instanceof PRTemp) {
            first = (PRTemp)first.first;
            firsts.add(first);
        }
        
        var resultEntries = new ArrayList<PREntry>();
        for (int i = firsts.size(); --i >= 0;) {
            var firstEntry = firsts.get(i);
            if (firstEntry.entryCount() != 0) {
                var entryList = firstEntry.entryList();
                resultEntries.addAll(entryList);
            }
        }
        var duplicate = first.first.duplicate();
        return new PRTemp(duplicate, resultEntries);
    }
    
}