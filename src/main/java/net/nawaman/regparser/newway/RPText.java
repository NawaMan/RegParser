package net.nawaman.regparser.newway;

import net.nawaman.regparser.AsChecker;
import net.nawaman.regparser.ParserTypeProvider;

public abstract class RPText implements ExtensibleCharSequence {
	
	public abstract RPRootText root();
	
	public abstract RPText previous();
	
	@Override
	public abstract CharSequence originalText();
	
	int match(int offset, AsChecker asChecker, ParserTypeProvider typeProvider) {
		var matches = root().matches;
		return matches.match(this, offset, asChecker, typeProvider);
	}
	
}
