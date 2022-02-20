package net.nawaman.regparser.newway;

public class RPRootText extends RPText {
	
	private final CharSequence original;
	/*private*/ final MatchCache   matches = new MatchCache();
	
	public RPRootText(CharSequence original) {
		this.original = original;
	}
	
	@Override
	public RPRootText root() {
		return this;
	}
	
	public RPText parent() {
		return null;
	}
	
	@Override
	public CharSequence originalText() {
		return original;
	}
	
	public MatchCache matches(int offset) {
		return matches;
	}

	@Override
	public String toString() {
		return "RPRootText [original=" + original + "]";
	}
	
	public String toDetail() {
		return "RPRootText [original=" + original + ", matches=" + matches + "]";
	}
	
}
