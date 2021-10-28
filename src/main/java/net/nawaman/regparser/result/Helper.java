package net.nawaman.regparser.result;

class Helper {
    
    /** Returns the string representation of the starting of the given text at the position (character number) */
    public static int[] getLocationAsColRow(CharSequence pOrgText, int pPosition) {
        if (pOrgText == null)
            return null;
        if (pPosition >= pOrgText.length())
            pPosition = pOrgText.length() - 1;
        if (pPosition <= 0)
            return new int[] { 0, 0 };
        int PrevLine   = 0;
        int LineCount  = 0;
        int TextLength = pOrgText.length();
        
        int[] CR = null;
        for (int i = 0; i < TextLength; i++) {
            if (pOrgText.charAt(i) == '\r') {
                if ((TextLength > i) && (pOrgText.charAt(i + 1) == '\n'))
                    i++;
            } else
                if (pOrgText.charAt(i) == '\n') {
                    if (pPosition <= i) {
                        // Found it
                        if (PrevLine != 0)
                            PrevLine++;
                        CR = new int[] { pPosition - PrevLine, LineCount };
                        break;
                    } else
                        PrevLine = i;
                    LineCount++;
                } else
                    continue;
        }
        
        // Not found yet, so it is the last line
        if (CR == null) {
            if (PrevLine != 0)
                PrevLine++;
            CR = new int[] { pPosition - PrevLine, LineCount };
        }
        
        return CR;
    }
    
    /** Returns the string representation of the starting of the given text at the position (character number) */
    public static String getLocationAsString(CharSequence pOrgText, int pPosition) {
        if (pOrgText == null)
            return null;
        if ((pPosition < 0) || (pPosition > pOrgText.length()))
            return null;
        
        boolean IsShift = false;
        if ((pPosition == pOrgText.length()) || (pOrgText.charAt(pPosition) == '\n')) {
            // Shift to avoid point at NewLine
            IsShift = true;
            pPosition--;
        }
        
        String OrgText       = pOrgText.toString();
        int    ThisLineBegin = OrgText.lastIndexOf("\n", pPosition);
        int    ThisLineEnd   = OrgText.indexOf("\n", pPosition);
        if (ThisLineEnd == -1)
            ThisLineEnd = OrgText.length();
        
        String ThisLine = OrgText.substring((ThisLineBegin + 1 <= ThisLineEnd) ? (ThisLineBegin + 1) : ThisLineEnd,
                ThisLineEnd);
        ThisLineBegin++;
        
        StringBuffer SB = new StringBuffer();
        
        // Print (Row, Col)
        int[] RC = getLocationAsColRow(pOrgText, pPosition);
        if (RC != null)
            SB.append("(").append(RC[1]).append(",").append(RC[0]).append(")\n");
        
        if (ThisLineBegin > 1) {
            int PrevLineBegin = OrgText.lastIndexOf("\n", ThisLineBegin - 2);
            if (PrevLineBegin == -1)
                PrevLineBegin = 0;
            String PrevLine = OrgText.substring(PrevLineBegin, ThisLineBegin - 1);
            SB.append("\n");
            SB.append("\t-|");
            for (int i = 0; i < PrevLine.length(); i++) {
                if (PrevLine.charAt(i) == '\t')
                    SB.append("    ");
                else
                    if (PrevLine.charAt(i) == '\n')
                        continue;
                    else
                        SB.append(PrevLine.charAt(i));
            }
        }
        
        SB.append("\n");
        SB.append("\t-|");
        SB.append((ThisLine.endsWith("\n")) ? ThisLine.substring(0, ThisLine.length() - 1) : ThisLine);
        
        pPosition -= ThisLineBegin;
        SB.append("\n");
        SB.append("\t-|");
        for (int i = 0; i < pPosition; i++) {
            if (ThisLine.charAt(i) != '\t')
                SB.append(' ');
            else
                SB.append("    ");
        }
        if (IsShift)
            SB.append(' ');
        SB.append('^');
        if (IsShift)
            SB.append("-- At the end of the line");
        SB.append('\n');
        return SB.toString();
    }
    
}
