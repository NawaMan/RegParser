package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.validate;
import static net.nawaman.regparser.utils.Util.indexOf;
import static net.nawaman.regparser.utils.Util.lastIndexOf;
import static net.nawaman.regparser.utils.Util.startsWith;

import org.junit.Assert;
import org.junit.Test;

public class UtilTest {
    
    @Test
    public void testIndexOf() {
        validate("abcdefghi".indexOf("de", 0), indexOf("abcdefghi", "de", 0));
        validate("abcdefghi".indexOf("de", 4), indexOf("abcdefghi", "de", 4));
    }
    
    @Test
    public void testLastIndexOf() {
        validate("abcdefghi".lastIndexOf("de", 10), lastIndexOf("abcdefghi", "de", 10));
        validate("abcdefghi".lastIndexOf("de",  7), lastIndexOf("abcdefghi", "de",  7));
    }
    
    @Test
    public void tesStartsWith() {
        Assert.assertTrue("abcdefghi".startsWith("ab"));
        Assert.assertTrue(startsWith("abcdefghi", "ab"));
        
        Assert.assertTrue("abcdefghi".startsWith("de", 3));
        Assert.assertTrue(startsWith("abcdefghi", "de", 3));
        
        Assert.assertFalse("int.type".startsWith("typeref", 4));
        Assert.assertFalse(startsWith("int.type", "typeref", 4));
    }
    
}
