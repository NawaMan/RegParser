package net.nawaman.regparser.newway;

import net.nawaman.regparser.AsChecker;

public class RPNodeText extends RPMatchText {
	
	final RPRootText root;
	final RPText     parent;
	final int        offset;
	final AsChecker  asChecker;
	
	public RPNodeText(RPText parent, int offset, AsChecker asChecker) {
		this.parent    = parent;
		this.root      = parent.root();
		this.offset    = offset;
		this.asChecker = asChecker;
	}
	
	@Override
	public RPRootText root() {
		return root;
	}
	
	public RPText parent() {
		return parent;
	}
	
	public int offset() {
		return offset;
	}
	
	public AsChecker asChecker() {
		return asChecker;
	}
	
	@Override
	public CharSequence originalText() {
		return root.originalText();
	}
	
	@Override
	public String toString() {
		return parent.toString() + "\n"
				+ "offset: " + offset + ", checker: " + asChecker;
	}
	
}
