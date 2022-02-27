package net.nawaman.regparser.newway;

import static java.util.Objects.requireNonNull;

import net.nawaman.regparser.AsChecker;

public class RPNextText extends RPMatchText {
	
	final RPMatchText parent;
	final int         length;
	
	public RPNextText(RPMatchText parent, int length) {
		this.parent = requireNonNull(parent);
		this.length = length;
	}
	
	@Override
	public RPRootText root() {
		return parent.root();
	}
	
	@Override
	public RPText parent() {
		return (RPText)parent;
	}
	
	public int offset() {
		return parent.offset() + length;
	}
	
	@Override
	public CharSequence originalText() {
		return parent.originalText();
	}
	
	public AsChecker asChecker() {
		return parent.asChecker();
	}
	
	public int level() {
		return parent.level();
	}
	
	@Override
	public String toString() {
		int level = ((RPMatchText)parent).level();
		return parent.toString() + "\n"
				+ "offset: " + offset() + ", level: " + level + ", checker: " + asChecker();
	}
}
