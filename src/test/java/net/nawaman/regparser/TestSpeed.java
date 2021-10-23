package net.nawaman.regparser;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;

import org.junit.Test;


public class TestSpeed {
    
    @Test
    public void testSpeed() {
        System.out.println("Speed testing ...");
        
        long startTime = System.currentTimeMillis();
        var  parser    = RegParser.newRegParser("[^0-9]*");
        long endTime   = System.currentTimeMillis();
        System.out.println("RegParser:" + (endTime - startTime));
        
        startTime = System.currentTimeMillis();
        var pattern = Pattern.compile("[^0-9]*");
        endTime = System.currentTimeMillis();
        System.out.println("RegExpr:" + (endTime - startTime));
        
        int loop   = 500000;
        var random = new Random();
        
        var buffer = new StringBuffer();
        for (int i = 0; i < loop; i++) {
            int I = random.nextInt(300) % 2;
            switch (I) {
            //case 0: SB.append((char)(Rd.nextInt('9' - '0') + '0')); break;
            case 0:
                buffer.append((char) (random.nextInt('Z' - 'A') + 'A'));
                break;
            case 1:
                buffer.append((char) (random.nextInt('z' - 'a') + 'a'));
                break;
            }
        }
        
        var text = buffer.toString();
        System.out.println(text);
        
        long regexStart = System.currentTimeMillis();
        var  matcher    = pattern.matcher(text);
        matcher.matches();
        long regexEnd   = System.currentTimeMillis();
        System.out.println("RegExpr: " + ((int) (regexEnd - regexStart)));
        
        long parserStart = System.currentTimeMillis();
        var  result      = parser.parse(text);
        long parserEnd   = System.currentTimeMillis();
        System.out.println("RegParser: " + ((int) (parserEnd - parserStart)));
        
        int round = 50;
        
        var  regexTimes  = new int[round];
        var  parserTimes = new int[round];
        for (int i = 0; i < round; i++) {
            buffer = new StringBuffer();
            for (int j = 0; j < loop; j++) {
                int I = random.nextInt(300) % 2;
                switch (I) {
                //case 0: SB.append((char)(Rd.nextInt('9' - '0') + '0')); break;
                case 0:
                    buffer.append((char) (random.nextInt('Z' - 'A') + 'A'));
                    break;
                case 1:
                    buffer.append((char) (random.nextInt('z' - 'a') + 'a'));
                    break;
                }
            }
            
            text = buffer.toString();
            
            regexStart = System.currentTimeMillis();
            matcher   = pattern.matcher(text);
            matcher.matches();
            regexEnd = System.currentTimeMillis();
            regexTimes[i] = ((int) (regexEnd - regexStart));
            
            parserStart = System.currentTimeMillis();
            result   = parser.parse(text);
            parserEnd = System.currentTimeMillis();
            parserTimes[i] = ((int) (parserEnd - parserStart));
        }
        
        System.out.println("RegExpr   :" + Arrays.toString(regexTimes));
        System.out.println("RegParser :" + Arrays.toString(parserTimes));
        System.out.println(matcher.end());
        System.out.println(result.getEndPosition());
        
        var ratios = new int[round];
        for (int i = 0; i < round; i++) {
            ratios[i] = (parserTimes[i]/regexTimes[i]);
        }
        System.out.println("Ratios:" + Arrays.toString(ratios));
        
        int sum = 0;
        for (int i = 0; i < round; i++) {
            sum += ratios[i];
        }
        int ratio = sum/round;
        System.out.println("Ratio: " + ratio);
        assertTrue(ratio < 75);
    }
    
}
