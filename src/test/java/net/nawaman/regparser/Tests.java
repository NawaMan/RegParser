package net.nawaman.regparser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;

public class Tests {
    
    static public final String ToQuiet        = "Quiet";
    static final String[]      ToQuietAsArray = new String[] { ToQuiet };
    
    static public int getCurrentRevisionNumber() {
        
        Process P = null;
        try {
            P = Runtime.getRuntime().exec("/usr/bin/svn info file:///home/svn/RegParser/");
            //P = Runtime.getRuntime().exec("ls");
            InputStream       In   = P.getInputStream();
            InputStreamReader InR  = new InputStreamReader(In);
            BufferedReader    InBR = new BufferedReader(InR);
            String            Line = null;
            while ((Line = InBR.readLine()) != null) {
                if (Line.startsWith("Revision: "))
                    break;
            }
            
            if (P.waitFor() == 0) {
                RegParser   RP = RegParser.newRegParser("Revision:[:WhiteSpace:]*($Revision:~[0-9]+~)");
                ParseResult PR = RP.parse(Line);
                return Integer.parseInt(PR.textOf("$Revision"));
            }
        } catch (Exception E) {
        }
        
        return Integer.MAX_VALUE;
    }
    
    @SuppressWarnings("unchecked")
    static public void main(String... pArgs) {
        
        Vector<Long> ExecTime = new Vector<Long>();
        
        long StartTime = System.currentTimeMillis();
//        Test_01_Char.main(ToQuietAsArray);
//        ExecTime.add(System.currentTimeMillis() - StartTime);
//        
//        StartTime = System.currentTimeMillis();
//        Test_02_Word.main(ToQuietAsArray);
//        ExecTime.add(System.currentTimeMillis() - StartTime);
//        
//        StartTime = System.currentTimeMillis();
//        Test_03_Alternative.main(ToQuietAsArray);
//        ExecTime.add(System.currentTimeMillis() - StartTime);
//        
//        StartTime = System.currentTimeMillis();
//        Test_04_RegParser.main(ToQuietAsArray);
//        ExecTime.add(System.currentTimeMillis() - StartTime);
//        
//        StartTime = System.currentTimeMillis();
//        Test_05_Greediness.main(ToQuietAsArray);
//        ExecTime.add(System.currentTimeMillis() - StartTime);
//        
//        StartTime = System.currentTimeMillis();
//        Test_06_Name.main(ToQuietAsArray);
//        ExecTime.add(System.currentTimeMillis() - StartTime);
//        
//        StartTime = System.currentTimeMillis();
//        Test_07_Type.main(ToQuietAsArray);
//        ExecTime.add(System.currentTimeMillis() - StartTime);
//        
//        StartTime = System.currentTimeMillis();
//        Test_08_SelfContain.main(ToQuietAsArray);
//        ExecTime.add(System.currentTimeMillis() - StartTime);
//        
//        StartTime = System.currentTimeMillis();
//        Test_09_RegParserCompiler_1.main(ToQuietAsArray);
//        ExecTime.add(System.currentTimeMillis() - StartTime);
//        
//        StartTime = System.currentTimeMillis();
//        Test_10_RegParserCompiler_2.main(ToQuietAsArray);
//        ExecTime.add(System.currentTimeMillis() - StartTime);
//        
//        StartTime = System.currentTimeMillis();
//        Test_11_Speed.main(ToQuietAsArray);
//        ExecTime.add(System.currentTimeMillis() - StartTime);
        
        StartTime = System.currentTimeMillis();
        Test_12_Chagnable_ParseResult.main(ToQuietAsArray);
        ExecTime.add(System.currentTimeMillis() - StartTime);
        
        System.out.println("Tests  Times: " + Arrays.toString(ExecTime.toArray(new Long[0])));
        
        Vector<Vector<Long>> History   = null;
        Vector<Integer>      Revisions = null;
        Vector<Integer>      Times     = null;
        try {
            Serializable[] Datas = Util.loadObjectsFromFile("TimeRecord.data");
            History   = (Vector<Vector<Long>>) (Datas[0]);
            Revisions = (Vector<Integer>) (Datas[1]);
            Times     = (Vector<Integer>) (Datas[2]);
        } catch (Exception E) {
        }
        
        if ((History == null) || (Times == null) || (Revisions == null)) {
            History   = new Vector<Vector<Long>>();
            Revisions = new Vector<Integer>();
            Times     = new Vector<Integer>();
            
            History.add(new Vector<Long>());
            Revisions.add(getCurrentRevisionNumber());
            Times.add(1);
        }
        
        // Get the new revision
        int LastesRevision  = Revisions.get(Revisions.size() - 1);
        int CurrentRevision = getCurrentRevisionNumber();
        
        Vector<Long> RecordTimes = null;
        int          Count       = -1;
        
        if (LastesRevision == CurrentRevision) {
            RecordTimes = History.get(History.size() - 1);
            Count       = Times.get(Times.size() - 1);
        } else {    // This is a new Revision
            RecordTimes = new Vector<Long>();
            Count       = 1;
            History.add(RecordTimes);
            Revisions.add(CurrentRevision);
            Times.add(Count);
        }
        
        // Adjust the size - In case a new test was added
        for (int i = Math.min(ExecTime.size(), RecordTimes.size()); i < Math.max(ExecTime.size(),
                RecordTimes.size()); i++) {
            if (ExecTime.size() < RecordTimes.size())
                ExecTime.add(RecordTimes.get(i));
            else
                if (ExecTime.size() > RecordTimes.size())
                    RecordTimes.add(ExecTime.get(i));
        }
        
        // Only Record when the time is not so much different
        if (RecordTimes.get(0) * 3 >= ExecTime.get(0)) {
            // Add it to the average
            for (int i = RecordTimes.size(); --i >= 0;) {
                RecordTimes.set(i, (RecordTimes.get(i) * Count + ExecTime.get(i)) / (Count + 1));
            }
            
            // Save the record
            try {
                Times.set(Times.size() - 1, Count + 1);
                Util.saveObjectsToFile("TimeRecord.data", new Serializable[] { History, Revisions, Times });
            } catch (Exception E) {
            }
            
            System.out.println("Record Times: " + Arrays.toString(RecordTimes.toArray(new Long[0])));
            System.out.println("Out of " + Count + " times in the Revision " + CurrentRevision + ".");
        }
        
        System.out.println();
        for (int i = 0; i < History.size(); i++) {
            RecordTimes = History.get(i);
            System.out.println("Record Times #" + i + " of " + "Revision#" + Revisions.get(i) + " " + "(" + Times.get(i)
                    + " times): " + Arrays.toString(RecordTimes.toArray(new Long[0])));
        }
        
        System.out.println();
        System.out.println("All tests are passed");
        
        System.exit(0);
    }
    
}
