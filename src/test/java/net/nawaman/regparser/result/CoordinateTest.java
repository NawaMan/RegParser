package net.nawaman.regparser.result;

import static net.nawaman.regparser.TestUtils.validate;
import static net.nawaman.regparser.result.Coordinate.coordinateOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

public class CoordinateTest {
	
	private static String text = null;
	
	private int idx = 0;
	
	@BeforeClass
	public static void load() throws IOException {
		text = new String(Coordinate.class.getResourceAsStream("Code1.txt").readAllBytes());
	}
	
	@Test
	public void testNull() {
		assertNull(coordinateOf(null, idx));
		assertEquals(-1, Coordinate.colOf(null));
		assertEquals(-1, Coordinate.rowOf(null));
	}
	
	@Test
	public void testCreate() {
		validate("[1, 50]", Coordinate.of(1, 50)   .toArray());
		validate("[19, 1]", Coordinate.of(text, 50).toArray());
	}
	
	@Test
	public void testCreateArray() {
		validate(null,      Coordinate.of(null));
		validate(null,      Coordinate.of(new int[] {1}));
		validate("[1, 50]", Coordinate.of(new int[] {1, 50})     .toArray());
		validate("[1, 50]", Coordinate.of(new int[] {1, 50, 100}).toArray());
	}
	
	@Test
	public void testColRow() {
		assertEquals( 1, Coordinate.of(1, 50).col());
		assertEquals(50, Coordinate.of(1, 50).row());
		assertEquals( 1, Coordinate.colOf(Coordinate.of(1, 50)));
		assertEquals(50, Coordinate.rowOf(Coordinate.of(1, 50)));
	}
	
	@Test
	public void testFirstLine() {
		validate("A : Position [col=0, row=0]", text.charAt(idx = 0) + " : " + coordinateOf(text, idx));
		validate("2 : Position [col=1, row=0]", text.charAt(idx = 1) + " : " + coordinateOf(text, idx));
		validate("3 : Position [col=2, row=0]", text.charAt(idx = 2) + " : " + coordinateOf(text, idx));
		
		validate("8 : Position [col=27, row=0]", text.charAt(idx = 27) + " : " + coordinateOf(text, idx));
		validate("9 : Position [col=28, row=0]", text.charAt(idx = 28) + " : " + coordinateOf(text, idx));
		validate("c : Position [col=29, row=0]", text.charAt(idx = 29) + " : " + coordinateOf(text, idx));
	}
	
	@Test
	public void testSecondLine() {
		validate("B : Position [col=0, row=1]", text.charAt(idx = 31) + " : " + coordinateOf(text, idx));
		validate("2 : Position [col=1, row=1]", text.charAt(idx = 32) + " : " + coordinateOf(text, idx));
		validate("3 : Position [col=2, row=1]", text.charAt(idx = 33) + " : " + coordinateOf(text, idx));
		
		validate("8 : Position [col=27, row=1]", text.charAt(idx = 58) + " : " + coordinateOf(text, idx));
		validate("9 : Position [col=28, row=1]", text.charAt(idx = 59) + " : " + coordinateOf(text, idx));
		validate("c : Position [col=29, row=1]", text.charAt(idx = 60) + " : " + coordinateOf(text, idx));
	}
	
	@Test
	public void testBetweenFirstAndSecondLine() {
		validate( "9 : Position [col=28, row=0]", text.charAt(idx = 28) + " : " + coordinateOf(text, idx));
		validate( "c : Position [col=29, row=0]", text.charAt(idx = 29) + " : " + coordinateOf(text, idx));
		validate("\n : Position [col=30, row=0]", text.charAt(idx = 30) + " : " + coordinateOf(text, idx));
		validate( "B : Position [col=0, row=1]",  text.charAt(idx = 31) + " : " + coordinateOf(text, idx));
	}
	
	@Test
	public void testLastLine() {
		validate("8 : Position [col=27, row=3]", text.charAt(idx = 120) + " : " + coordinateOf(text, idx));
		validate("9 : Position [col=28, row=3]", text.charAt(idx = 121) + " : " + coordinateOf(text, idx));
		validate("c : Position [col=29, row=3]", text.charAt(idx = 122) + " : " + coordinateOf(text, idx));
	}
	
	@Test
	public void testBeyondTextLength() {
		idx = 123;
		validate(" : Position [col=29, row=3]", " : " + coordinateOf(text, idx));
		idx = 124;
		validate(" : Position [col=29, row=3]", " : " + coordinateOf(text, idx));
		idx = 125;
		validate(" : Position [col=29, row=3]", " : " + coordinateOf(text, idx));
	}
	
	@Test
	public void testLinuxText() {
		var text = "First Line\n"
				 + "Second Line\n";
		validate( "e : Position [col=9, row=0]",  text.charAt(idx =  9) + " : " + coordinateOf(text, idx));
		validate("\n : Position [col=10, row=0]", text.charAt(idx = 10) + " : " + coordinateOf(text, idx));
		validate( "S : Position [col=0, row=1]",  text.charAt(idx = 11) + " : " + coordinateOf(text, idx));
		
		validate( "e : Position [col=10, row=1]", text.charAt(idx = 21) + " : " + coordinateOf(text, idx));
		validate("\n : Position [col=11, row=1]", text.charAt(idx = 22) + " : " + coordinateOf(text, idx));
		
		idx = 23;
		validate(  " : Position [col=11, row=1]", " : " + coordinateOf(text, idx));
	}
	
	@Test
	public void testWindowsText() {
		var text = "First Line\r\n"
				 + "Second Line\r\n";
		validate( "e : Position [col=9, row=0]",  text.charAt(idx =  9) + " : " + coordinateOf(text, idx));
		validate("\r : Position [col=10, row=0]", text.charAt(idx = 10) + " : " + coordinateOf(text, idx));
		validate("\n : Position [col=11, row=0]", text.charAt(idx = 11) + " : " + coordinateOf(text, idx));
		validate( "S : Position [col=0, row=1]",  text.charAt(idx = 12) + " : " + coordinateOf(text, idx));
		
		validate( "e : Position [col=10, row=1]", text.charAt(idx = 22) + " : " + coordinateOf(text, idx));
		validate("\r : Position [col=11, row=1]", text.charAt(idx = 23) + " : " + coordinateOf(text, idx));
		validate("\n : Position [col=12, row=1]", text.charAt(idx = 24) + " : " + coordinateOf(text, idx));
		
		idx = 25;
		validate(  " : Position [col=12, row=1]", " : " + coordinateOf(text, idx));
	}
	
	@Test
	public void testMacText() {
		var text = "First Line\r"
				 + "Second Line\r";
		validate( "e : Position [col=9, row=0]",  text.charAt(idx =  9) + " : " + coordinateOf(text, idx));
		validate("\r : Position [col=10, row=0]", text.charAt(idx = 10) + " : " + coordinateOf(text, idx));
		validate( "S : Position [col=0, row=1]",  text.charAt(idx = 11) + " : " + coordinateOf(text, idx));
		
		validate( "e : Position [col=10, row=1]", text.charAt(idx = 21) + " : " + coordinateOf(text, idx));
		validate("\r : Position [col=11, row=1]", text.charAt(idx = 22) + " : " + coordinateOf(text, idx));
		
		idx = 24;
		validate( " : Position [col=11, row=1]", " : " + coordinateOf(text, idx));
	}
	
	@Test
	public void testHashCode() {
		var coordinate1 = Coordinate.of(1, 2);
		var coordinate2 = Coordinate.of(1, 2);
		var coordinate3 = Coordinate.of(4, 5);
		assertTrue(coordinate1.hashCode() == coordinate2.hashCode());
		assertTrue(coordinate1.hashCode() != coordinate3.hashCode());
		assertTrue(coordinate2.hashCode() != coordinate3.hashCode());
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals() {
		var coordinate1 = Coordinate.of(1, 2);
		var coordinate2 = Coordinate.of(1, 2);
		var coordinate3 = Coordinate.of(4, 5);
		var coordinate4 = Coordinate.of(4, 7);
		assertTrue (coordinate1.equals(coordinate1));
		assertTrue (coordinate1.equals(coordinate2));
		assertFalse(coordinate1.equals(coordinate3));
		assertFalse(coordinate1.equals(coordinate4));
		assertFalse(coordinate2.equals(coordinate3));
		assertFalse(coordinate2.equals(coordinate4));
		assertFalse(coordinate3.equals(coordinate4));
		assertFalse(coordinate1.equals(null));
		assertFalse(coordinate2.equals(null));
		assertFalse(coordinate3.equals(null));
		
		assertFalse(coordinate1.equals("Hello"));
	}
	
}
