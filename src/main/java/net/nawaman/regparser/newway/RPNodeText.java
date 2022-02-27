package net.nawaman.regparser.newway;

import net.nawaman.regparser.AsChecker;

public class RPNodeText extends RPMatchText {
	
	final RPRootText root;
	final RPText     parent;
	final int        offset;
	final AsChecker  asChecker;
	final int        level;
	
	public RPNodeText(RPText parent, int offset, AsChecker asChecker, int level) {
		this.parent    = parent;
		this.root      = parent.root();
		this.offset    = offset;
		this.asChecker = asChecker;
		this.level     = level;
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
	
	public int level() {
		return level;
	}
	
	@Override
	public CharSequence originalText() {
		return root.originalText();
	}
	
	@Override
	public String toString() {
		return parent.toString() + "\n"
				+ "offset: " + offset + ", level: " + level + ", checker: " + asChecker;
	}
	
}
