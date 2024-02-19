package net.nawaman.regparser.newway;

public interface ExtensibleCharSequence extends CharSequence {
    
    public CharSequence originalText();
    
    @Override
    public default int length() {
        return originalText()
                .length();
    }
    
    @Override
    public default char charAt(int index) {
        return originalText()
                .charAt(index);
    }
    
    @Override
    public default CharSequence subSequence(int start, int end) {
        return originalText()
                .subSequence(start, end);
    }
    
}
