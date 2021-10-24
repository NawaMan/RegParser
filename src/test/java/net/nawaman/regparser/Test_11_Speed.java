package net.nawaman.regparser;

import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Test_11_Speed {
    
    static public void main(String... Args) {
        
        boolean IsQuiet = ((Args != null) && (Args.length > 0) && (Tests.ToQuiet.equals(Args[0])));
        
        if (IsQuiet)
            System.out.println("Speed testing ...");
        
        long      S  = System.currentTimeMillis();
        RegParser RP = RegParser.newRegParser("[^0-9]*");
        long      E  = System.currentTimeMillis();
        if (!IsQuiet)
            System.out.println("RegExpr   :" + (E - S));
        
        S = System.currentTimeMillis();
        Pattern P = Pattern.compile("[^0-9]*");
        E = System.currentTimeMillis();
        if (!IsQuiet)
            System.out.println("RegParser :" + (E - S));
        
        Random Rd = new Random();
        
        StringBuffer SB = new StringBuffer();
        for (int i = 0; i < 100000; i++) {
            int I = Rd.nextInt(300) % 2;
            switch (I) {
            //case 0: SB.append((char)(Rd.nextInt('9' - '0') + '0')); break;
            case 0:
                SB.append((char) (Rd.nextInt('Z' - 'A') + 'A'));
                break;
            case 1:
                SB.append((char) (Rd.nextInt('z' - 'a') + 'a'));
                break;
            }
        }
        
        String T = SB.toString();
        
        if (!IsQuiet)
            System.out.println(T);
        
        Vector<Integer> RETs = new Vector<Integer>();
        long            SRE  = System.currentTimeMillis();
        Matcher         M    = P.matcher(T);
        M.matches();
        long ERE = System.currentTimeMillis();
        RETs.add((int) (ERE - SRE));
        
        Vector<Integer> RPTs = new Vector<Integer>();
        long            SRP  = System.currentTimeMillis();
        ParseResult     R    = RP.parse(T);
        long            ERP  = System.currentTimeMillis();
        RPTs.add((int) (ERP - SRP));
        
        for (int i = 0; i < 50; i++) {
            SB = new StringBuffer();
            for (int j = 0; j < 100000; j++) {
                int I = Rd.nextInt(300) % 2;
                switch (I) {
                //case 0: SB.append((char)(Rd.nextInt('9' - '0') + '0')); break;
                case 0:
                    SB.append((char) (Rd.nextInt('Z' - 'A') + 'A'));
                    break;
                case 1:
                    SB.append((char) (Rd.nextInt('z' - 'a') + 'a'));
                    break;
                }
            }
            
            T = SB.toString();
            
            SRE = System.currentTimeMillis();
            M   = P.matcher(T);
            M.matches();
            ERE = System.currentTimeMillis();
            RETs.add((int) (ERE - SRE));
            
            SRP = System.currentTimeMillis();
            R   = RP.parse(T);
            ERP = System.currentTimeMillis();
            RPTs.add((int) (ERP - SRP));
        }
        
        if (!IsQuiet)
            System.out.println("RegExpr   :" + RETs.toString());
        if (!IsQuiet)
            System.out.println("RegParser :" + RPTs.toString());
        if (!IsQuiet)
            System.out.println(M.end());
        if (!IsQuiet)
            System.out.println(R.getEndPosition());
        
//        if(!IsQuiet) System.out.println(R);
//        if(!IsQuiet) System.out.println(M);
            
    }
    
}
