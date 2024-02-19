package net.nawaman.regparser.result;

import static net.nawaman.regparser.TestUtils.validate;
import static net.nawaman.regparser.result.Location.detailLocationOf;
import static net.nawaman.regparser.result.Location.locationOf;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

public class LocationTest {
    
    private static String text = null;
    
    private int idx = 0;
    
    @BeforeClass
    public static void load() throws IOException {
        text = new String(Coordinate.class.getResourceAsStream("Code1.txt").readAllBytes());
        
        System.out.println(text);
    }
    
    @Test
    public void testNull() {
        assertNull(Location.of(null, idx));
    }
    
    @Test
    public void testIndexOutOfBound() {
        assertNull(Location.of("", -5));
        assertNull(Location.of("",  5));
    }
    
    @Test
    public void testFirstLine() {
        validate("A : (0,0)\n"
                + "\n"
                + "	-|A23456789a123456789b123456789c\n"
                + "	-|^\n"
                + "", detailLocationOf(text, 0));
        validate("2 : (0,1)\n"
                + "\n"
                + "	-|A23456789a123456789b123456789c\n"
                + "	-| ^\n"
                + "", detailLocationOf(text, 1));
        validate("3 : (0,2)\n"
                + "\n"
                + "	-|A23456789a123456789b123456789c\n"
                + "	-|  ^\n"
                + "", detailLocationOf(text, 2));
        validate("4 : (0,3)\n"
                + "\n"
                + "	-|A23456789a123456789b123456789c\n"
                + "	-|   ^\n"
                + "", detailLocationOf(text, 3));
    }
    
    @Test
    public void testSecondLine() {
        validate("B : (1,0)\n"
                + "\n"
                + "	-|A23456789a123456789b123456789c\n"
                + "	-|B23456789a123456789b123456789c\n"
                + "	-|^\n"
                + "", detailLocationOf(text, 31));
        validate("2 : (1,1)\n"
                + "\n"
                + "	-|A23456789a123456789b123456789c\n"
                + "	-|B23456789a123456789b123456789c\n"
                + "	-| ^\n"
                + "", detailLocationOf(text, 32));
        validate("3 : (1,2)\n"
                + "\n"
                + "	-|A23456789a123456789b123456789c\n"
                + "	-|B23456789a123456789b123456789c\n"
                + "	-|  ^\n"
                + "", detailLocationOf(text, 33));
        validate("4 : (1,3)\n"
                + "\n"
                + "	-|A23456789a123456789b123456789c\n"
                + "	-|B23456789a123456789b123456789c\n"
                + "	-|   ^\n"
                + "", detailLocationOf(text, 34));
    }
    
    @Test
    public void testThirdLine() {
        validate("C : (2,0)\n"
                + "\n"
                + "	-|B23456789a123456789b123456789c\n"
                + "	-|C23456789a123456789b123456789c\n"
                + "	-|^\n"
                + "", detailLocationOf(text, 62));
        validate("2 : (2,1)\n"
                + "\n"
                + "	-|B23456789a123456789b123456789c\n"
                + "	-|C23456789a123456789b123456789c\n"
                + "	-| ^\n"
                + "", detailLocationOf(text, 63));
        validate("3 : (2,2)\n"
                + "\n"
                + "	-|B23456789a123456789b123456789c\n"
                + "	-|C23456789a123456789b123456789c\n"
                + "	-|  ^\n"
                + "", detailLocationOf(text, 64));
        validate("4 : (2,3)\n"
                + "\n"
                + "	-|B23456789a123456789b123456789c\n"
                + "	-|C23456789a123456789b123456789c\n"
                + "	-|   ^\n"
                + "", detailLocationOf(text, 65));
    }
    
    @Test
    public void testBetweenFirstAndSecondLine() {
        validate( "9 : (0,28)\n"
                + "\n"
                + "	-|A23456789a123456789b123456789c\n"
                + "	-|                            ^\n"
                + "", detailLocationOf(text, 28));
        validate( "c : (0,29)\n"
                + "\n"
                + "	-|A23456789a123456789b123456789c\n"
                + "	-|                             ^\n"
                + "", detailLocationOf(text, 29));
        validate("\n"
                + " : (0,30)\n"
                + "\n"
                + "	-|A23456789a123456789b123456789c\n"
                + "	-|                              ^-- At the end of the line\n"
                + "", detailLocationOf(text, 30));
        validate( "B : (1,0)\n"
                + "\n"
                + "	-|A23456789a123456789b123456789c\n"
                + "	-|B23456789a123456789b123456789c\n"
                + "	-|^\n"
                + "", detailLocationOf(text, 31));
    }
    
    @Test
    public void testLastLine() {
        validate("8 : (3,27)\n"
                + "\n"
                + "	-|C23456789a123456789b123456789c\n"
                + "	-|D23456789a123456789b123456789c\n"
                + "	-|                           ^\n"
                + "", detailLocationOf(text, 120));
        validate("9 : (3,28)\n"
                + "\n"
                + "	-|C23456789a123456789b123456789c\n"
                + "	-|D23456789a123456789b123456789c\n"
                + "	-|                            ^\n"
                + "", detailLocationOf(text, 121));
        validate("c : (3,29)\n"
                + "\n"
                + "	-|C23456789a123456789b123456789c\n"
                + "	-|D23456789a123456789b123456789c\n"
                + "	-|                             ^\n"
                + "", detailLocationOf(text, 122));
    }
    
    @Test
    public void testBeyondTextLength() {
        validate("✖ : (3,29)\n"
                + "\n"
                + "	-|C23456789a123456789b123456789c\n"
                + "	-|D23456789a123456789b123456789c\n"
                + "	-|                              ^-- At the end of the line\n"
                + "", detailLocationOf(text, 123));
        
        validate("✖ : (3,29)\n"
                + "\n"
                + "", detailLocationOf(text, 124));
        
        validate("✖ : (3,29)\n"
                + "\n"
                + "", detailLocationOf(text, 125));
        
        validate(null, locationOf(text, 124));
        validate(null, locationOf(text, 125));
    }
    
    @Test
    public void testLinuxText() {
        var text = "First Line\n"
                 + "Second Line\n";
        validate("e : (0,9)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-|         ^\n"
                + "", detailLocationOf(text, 9));
        validate("\n"
                + " : (0,10)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-|          ^-- At the end of the line\n"
                + "", detailLocationOf(text, 10));
        validate("S : (1,0)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-|Second Line\n"
                + "	-|^\n"
                + "", detailLocationOf(text, 11));
        
        validate("e : (1,10)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-|Second Line\n"
                + "	-|          ^\n"
                + "", detailLocationOf(text, 21));
        validate("\n"
                + " : (1,11)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-|Second Line\n"
                + "	-|           ^-- At the end of the line\n"
                + "", detailLocationOf(text, 22));
        
        validate("✖ : (1,11)\n"
                + "\n"
                + "	-|Second Line\n"
                + "	-|\n"
                + "	-| ^-- At the end of the line\n"
                + "", detailLocationOf(text, 23));
    }
    
    @Test
    public void testWindowsText() {
        var text = "First Line\r\n"
                 + "Second Line\r\n";
        validate("e : (0,9)\n"
                + "\n"
                + "	-|First Line\r\n"
                + "	-|         ^\n"
                + "", detailLocationOf(text, 9));
        validate("\r"
                + " : (0,10)\n"
                + "\n"
                + "	-|First Line\r\n"
                + "	-|          ^\n"
                + "", detailLocationOf(text, 10));
        validate("\n"
                + " : (0,11)\n"
                + "\n"
                + "	-|First Line\r\n"
                + "	-|           ^-- At the end of the line\n"
                + "", detailLocationOf(text, 11));
        validate("S : (1,0)\n"
                + "\n"
                + "	-|First Line\r\n"
                + "	-|Second Line\r\n"
                + "	-|^\n"
                + "", detailLocationOf(text, 12));
        
        validate("e : (1,10)\n"
                + "\n"
                + "	-|First Line\r\n"
                + "	-|Second Line\r\n"
                + "	-|          ^\n"
                + "", detailLocationOf(text, 22));
        validate("\r"
                + " : (1,11)\n"
                + "\n"
                + "	-|First Line\r\n"
                + "	-|Second Line\r\n"
                + "	-|           ^\n"
                + "", detailLocationOf(text, 23));
        validate("\n"
                + " : (1,12)\n"
                + "\n"
                + "	-|First Line\r\n"
                + "	-|Second Line\r\n"
                + "	-|            ^-- At the end of the line\n"
                + "", detailLocationOf(text, 24));
        
        validate("✖ : (1,12)\n"
                + "\n"
                + "	-|Second Line\r\n"
                + "	-|\n"
                + "	-| ^-- At the end of the line\n"
                + "", detailLocationOf(text, 25));
    }
    
    @Test
    public void testMacText() {
        var text = "First Line\r"
                 + "Second Line\r";
        validate("e : (0,9)\n"
                + "\n"
                + "	-|First Line\r"
                + "Second Line\r\n"
                + "	-|         ^\n"
                + "", detailLocationOf(text, 9));
        validate("\r"
                + " : (0,10)\n"
                + "\n"
                + "	-|First Line\r"
                + "Second Line\r\n"
                + "	-|          ^\n"
                + "", detailLocationOf(text, 10));
        validate("S : (1,0)\n"
                + "\n"
                + "	-|First Line\r"
                + "Second Line\r\n"
                + "	-|           ^\n"
                + "", detailLocationOf(text, 11));
        
        validate("e : (1,10)\n"
                + "\n"
                + "	-|First Line\r"
                + "Second Line\r\n"
                + "	-|                     ^\n"
                + "", detailLocationOf(text, 21));
        validate("\r"
                + " : (1,11)\n"
                + "\n"
                + "	-|First Line\r"
                + "Second Line\r\n"
                + "	-|                      ^\n"
                + "", detailLocationOf(text, 22));
        
        idx = 24;
        validate(null, locationOf(text, idx));
    }
    
    @Test
    public void testTabFirstLine() {
        var text = "\tFirst Line\n"
                 + "Second Line\n";
        validate("	 : (0,0)\n"
                + "\n"
                + "	-|	First Line\n"
                + "	-|^\n"
                + "", detailLocationOf(text, 0));
        validate("F : (0,1)\n"
                + "\n"
                + "	-|	First Line\n"
                + "	-|	^\n"
                + "", detailLocationOf(text, 1));
        validate("i : (0,2)\n"
                + "\n"
                + "	-|	First Line\n"
                + "	-|	 ^\n"
                + "", detailLocationOf(text, 2));
        
        validate("n : (0,9)\n"
                + "\n"
                + "	-|	First Line\n"
                + "	-|	        ^\n"
                + "", detailLocationOf(text, 9));
        validate("e : (0,10)\n"
                + "\n"
                + "	-|	First Line\n"
                + "	-|	         ^\n"
                + "", detailLocationOf(text, 10));
        validate("\n"
                + " : (0,11)\n"
                + "\n"
                + "	-|	First Line\n"
                + "	-|	          ^-- At the end of the line\n"
                + "", detailLocationOf(text, 11));
        validate("S : (1,0)\n"
                + "\n"
                + "	-|\tFirst Line\n"
                + "	-|Second Line\n"
                + "	-|^\n"
                + "", detailLocationOf(text, 12));
        
        validate("e : (1,10)\n"
                + "\n"
                + "	-|\tFirst Line\n"
                + "	-|Second Line\n"
                + "	-|          ^\n"
                + "", detailLocationOf(text, 22));
        validate("\n"
                + " : (1,11)\n"
                + "\n"
                + "	-|	First Line\n"
                + "	-|Second Line\n"
                + "	-|           ^-- At the end of the line\n"
                + "", detailLocationOf(text, 23));
        
        validate("\n"
                + " : (1,11)\n"
                + "\n"
                + "	-|	First Line\n"
                + "	-|Second Line\n"
                + "	-|           ^-- At the end of the line\n"
                + "", detailLocationOf(text, 23));
    }
    
    @Test
    public void testTabSecondLine() {
        var text = "First Line\n"
                 + "\tSecond Line\n";
        validate("F : (0,0)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-|^\n"
                + "", detailLocationOf(text, 0));
        validate("i : (0,1)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-| ^\n"
                + "", detailLocationOf(text, 1));
        
        validate("e : (0,9)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-|         ^\n"
                + "", detailLocationOf(text, 9));
        validate("\n"
                + " : (0,10)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-|          ^-- At the end of the line\n"
                + "", detailLocationOf(text, 10));
        validate("	 : (1,0)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-|	Second Line\n"
                + "	-|^\n"
                + "", detailLocationOf(text, 11));
        validate("S : (1,1)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-|	Second Line\n"
                + "	-|	^\n"
                + "", detailLocationOf(text, 12));
        
        validate("e : (1,11)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-|	Second Line\n"
                + "	-|	          ^\n"
                + "", detailLocationOf(text, 22));
        validate("\n"
                + " : (1,12)\n"
                + "\n"
                + "	-|First Line\n"
                + "	-|	Second Line\n"
                + "	-|	           ^-- At the end of the line\n"
                + "", detailLocationOf(text, 23));
        
        validate("✖ : (1,12)\n"
                + "\n"
                + "	-|	Second Line\n"
                + "	-|\n"
                + "	-| ^-- At the end of the line\n"
                + "", detailLocationOf(text, 24));
    }
    
}
