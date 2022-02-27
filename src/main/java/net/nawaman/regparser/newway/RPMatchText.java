package net.nawaman.regparser.newway;

import net.nawaman.regparser.AsChecker;

public abstract class RPMatchText extends RPText {
	
	public abstract RPRootText root();
	
	public abstract RPText parent();
	
	public abstract int offset();
	
	public abstract CharSequence originalText();
	
	public abstract AsChecker asChecker();
	
	public abstract int level();
	
}
