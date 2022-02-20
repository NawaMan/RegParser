package net.nawaman.regparser.newway;

import net.nawaman.regparser.AsChecker;

public class RPNodeText extends RPText {
	
	private final RPRootText root;
	private final RPText     parent;
	private final int        offset;
	private final AsChecker  asChecker;
	
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
	
	@Override
	public CharSequence originalText() {
		return root.originalText();
	}

	@Override
	public String toString() {
		return parent.toString() + "\n"
				+ "offset: " + offset + ", asChecker: " + asChecker;
	}
	
}
