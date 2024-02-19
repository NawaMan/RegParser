package net.nawaman.regparser;

import static net.nawaman.regparser.RegParserEntry.newParserEntry;

import java.util.concurrent.atomic.AtomicBoolean;

class RegParserOptimizer {
    
    static Checker optimize(RegParser regParser) {
        RegParserEntry[] entries = regParser.entries().toArray(RegParserEntry[]::new);
        var newEntries = new RegParserEntry[entries.length];
        var isChanged  = new AtomicBoolean(false);
        
        regParser
        .entries()
        .map(entry -> optimizeEntry(isChanged, entry))
        .toArray(RegParserEntry[]::new);
        
        if (!isChanged.get())
            return regParser;
        
        if (regParser instanceof RegParserWithDefaultTypeProvider) {
            var typeProvider = ((RegParserWithDefaultTypeProvider)regParser).getDefaultTypeProvider();
            return new RegParserWithDefaultTypeProvider(true, entries, typeProvider);
        }
        
        return new RegParser(true, (RegParserEntry[]) newEntries);
    }
    
    private static RegParserEntry optimizeEntry(AtomicBoolean isChanged, RegParserEntry entry) {
        var name       = entry.name();
        var typeRef    = entry.typeRef();
        var quantifier = entry.quantifier();
        if (typeRef != null)
            return newParserEntry(name, typeRef, quantifier);
        
        var type = entry.type();
        if (type != null)
            return newParserEntry(name, type, quantifier);
        
        var checker = entry.checker();
        if (checker == null)
            throw new NullPointerException("`checker` is null.");

        if (!(checker instanceof RegParser))
            return newParserEntry(name, checker, quantifier);
        
        var newParser = ((RegParser) checker).optimize();
        if (newParser == checker)
            return newParserEntry(name, checker, quantifier);
        
        isChanged.set(true);
        return RegParserEntry.newParserEntry(name, newParser, quantifier);
    }
}
