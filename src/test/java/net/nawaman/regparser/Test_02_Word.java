package net.nawaman.regparser;

public class Test_02_Word {
	
	static public void Assert(Object pValue, Object pCorrectValue) {
		if(!Util.equal(pValue, pCorrectValue)) {
			System.out.println("It's " + Util.toString(pValue) + " but it should be "
					+ Util.toString(pCorrectValue) + ".");
			
			if((pValue instanceof String) && (pCorrectValue instanceof String)) {
				String S1 = (String)pValue;
				String S2 = (String)pCorrectValue;
				System.out.println(S1.length() + " : " + S2.length());
			}
			
			throw new AssertionError();
		}
	}
	
	static public void main(String ... Arvs) {
		System.out.println("Word ---------------------------------------------------------");
		Checker CW = new WordChecker("555");
		Assert(CW.getStartLengthOf("555222",   0, null),  3);
		Assert(CW.getStartLengthOf("555222",   2, null), -1);
		Assert(CW.getStartLengthOf("00555222", 0, null), -1);
		Assert(CW.getStartLengthOf("5505222",  0, null), -1);

		System.out.println("All Success.");
	}

}
