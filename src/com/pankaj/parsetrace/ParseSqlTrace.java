package com.pankaj.parsetrace;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;


public class ParseSqlTrace {
    //private static final String TRACE_FILE="C:\\Users\\Pankaj\\temp\\VIS1213_ora_17740_OPERATIONS.trc";
    private static final String TRACE_FILE="C:\\Users\\Pankaj\\Downloads\\CFG_ora_26414_JANGAMASHETT.trc";
    
    private File traceFile = null;
    private File traceWithBindValuesFile = null;
    
    private static final String PARSING_IN_CURSOR = "PARSING IN CURSOR #";
    private static final String END_OF_STMT = "END OF STMT";
    private static final String PARSE = "PARSE ";
    private static final String WAIT = "WAIT ";
    private static final String FETCH = "FETCH ";
    private static final String STAT = "STAT ";
    private static final String BINDS = "BINDS #";
    private static final String BIND_NUM = " Bind#";
    private static final String CLOSE = "CLOSE ";
    private static final String EXEC = "EXEC ";
    private static final String BIND_VALUE = "  value=";

	private static final boolean debugEnabled = false;

    private HashMap<String, SqlCursor> cursorMap = new HashMap<String, SqlCursor>();    
    
    public ParseSqlTrace(String filePath) {
        super();
        traceFile = new File(filePath);
        traceWithBindValuesFile = new File(filePath + ".bindvalues");
        try {
            traceWithBindValuesFile.createNewFile();
        } catch (IOException e) {
            debug("Error while creating new file " + filePath +".bindvalues : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public ParseSqlTrace() {
        super();
    }
    
    public static void parseTrace(BufferedReader br, BufferedWriter bw) throws IOException {
    	ParseSqlTrace pt = new ParseSqlTrace();
    	pt.processTrace(br, bw);    	
    }
    
//    public void process() {
//        FileReader fr = null;
//        BufferedReader br = null;
//        BufferedWriter bw = null;
//        try {
//            fr = new FileReader(traceFile);
//            br = new BufferedReader(fr);
//            bw = new BufferedWriter(new FileWriter(traceWithBindValuesFile));
//            processTrace(br, bw);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            if (br!=null)             {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if (fr!=null)             {
//                try {
//                    fr.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if (bw!=null)             {
//                try {
//                    bw.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//    }
    
    private void test() {
    	InputStream is = null;
    	BufferedReader br = new BufferedReader(new InputStreamReader(is));
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(baos));
    	
    }

    private void processTrace(BufferedReader br, BufferedWriter bw) throws java.io.IOException {
        String line = null;
        while ((line=br.readLine())!=null) {
            //processLine(line);
            debug("process() - Processing line=" + line);
            if (line.startsWith(PARSING_IN_CURSOR)) {
                String curNum = parseString(PARSING_IN_CURSOR, line);
                String query = "";
                while ((line = br.readLine())!=null) {
                    if (line.equals(END_OF_STMT)) {
                        break;
                    }else{
                        query = query + line;
                    }
                }
                if (line==null) {
                    break;
                }
                if (query.length()>0) {
                    SqlCursor sc = new SqlCursor(curNum, query);
                    cursorMap.put(curNum, sc);
                    debug("New Cursor = " + sc);
                }else{
                    throw new RuntimeException("Could not fetch query from " + PARSING_IN_CURSOR + " line=" + line);
                }
                
            }else if (line.startsWith(BINDS)) {
                //Here starts binding
                String curId = parseString(BINDS, line);
                SqlCursor sc = cursorMap.get(curId);
                if (sc==null) {
                    debug("Could not find cursor for id=" + curId + ", for bind line=" + line);
                    continue;
                }
                String query = sc.getQuery();
                debug("Found cursor=" + sc + " for bind line="+ line);
                String[] bindValues = new String[sc.getNumOfBindVars()];
                long bindVarIndex = -1;
                while ((line = br.readLine())!=null) {
                    if (line.startsWith(BIND_NUM)) {
                        bindVarIndex = parseId(BIND_NUM, line);
                    }else if (line.startsWith(BIND_VALUE)){
                        String value = parseValue(BIND_VALUE, line);
                        try {
                            if (bindVarIndex > Integer.MAX_VALUE) {
                                throw new RuntimeException("Index out of range : " + bindVarIndex);
                            }
                            bindValues[(int)bindVarIndex] = value;
                        } catch (Exception e) {
                            // TODO: Add catch code
                            e.printStackTrace();
                        }
                    }else if (!line.startsWith("  ")){
                        break;
                    }
                }
                String queryWithBind = sc.getQueryWithBindings(bindValues);
                debug("Final query = " + queryWithBind);
                bw.write(queryWithBind + " ;\n\n");
                if (line==null) {
                    break;
                }
            }
        }
        bw.flush();
    }
    

    private long parseId(String preFix, String line) {
//        PARSING IN CURSOR #21 len=93 dep=1 uid=65 oct=3 lid=65 tim=1396265093480092 hv=277371595 ad='8dcce490' sqlid='btamp5s88hqqb'
//        SELECT PATH_DOCID, PATH_TYPE FROM JDR_PATHS WHERE PATH_NAME = :B1 AND PATH_OWNER_DOCID = :B2 
//        END OF STMT
        String idNumStr = parseString(preFix, line);
        long idNum = Long.parseLong(idNumStr);
        //debug("CurNum = " + curNum);
        return idNum;
    }
    
    private String parseString(String preFix, String line) {
        //TODO: Improve what if line ends with new line after parse value
        debug("parseId() - prefix=" + preFix + ", line=" + line);
        String remLine = line.substring(preFix.length());
        int commaIndex = remLine.indexOf(",");
        int spaceIndex = remLine.indexOf(" ");
        int braceIndex = remLine.indexOf(")");
        int semicolonIndex = remLine.indexOf(":");
        if (commaIndex<0) {
            commaIndex = remLine.length();
        }
        if (spaceIndex<0) {
            spaceIndex = remLine.length();
        }
        if (braceIndex<0) {
            braceIndex = remLine.length();
        }
        if (semicolonIndex<0) {
            semicolonIndex = remLine.length();
        }
        int index = Math.min(Math.min(commaIndex, semicolonIndex), Math.min(spaceIndex, braceIndex));
        if (index<0) {
            index = remLine.length();
        }
        String finalStr = remLine.substring(0, index);
        return finalStr;
    }
    
    private String parseValue(String preFix, String line) {
        //TODO: Improve what if line ends with new line after parse value
        debug("parseValue() - prefix=" + preFix + ", line=" + line);
        String remLine = line.substring(preFix.length());
        int index = remLine.length();
        debug("remLine=" + remLine + ", index=" + index);
        String finalStr = null;
        if (index==0) {
            //assuming value is null in this case
            finalStr = "null";
        }else {
            finalStr = remLine.substring(0, index);
            if (finalStr.startsWith("\"")) {
                finalStr = finalStr.substring(1, finalStr.length()-1);
                finalStr = "'" + finalStr + "'";
            }
        }
        return finalStr;
    }
    
    public static void main(String[] args) {
        ParseSqlTrace pst = new ParseSqlTrace(TRACE_FILE);
        String absolutePath = pst.traceFile.getAbsolutePath();
        debug("File Path = " + absolutePath);
        pst.parseId(PARSING_IN_CURSOR, "PARSING IN CURSOR #21 len=93 dep=1 uid=65 oct=3 lid=65 tim=1396265093480092 hv=277371595 ad='8dcce490' sqlid='btamp5s88hqqb'");
        //pst.process();
        
//        SqlCursor sqlCur = new SqlCursor(29, "BEGIN  :1 := FND_HELP.GET_URL(:2, :3,true,:4, true);END;");
//        String[] bindValues = {null, "'CZ'", "'CZ_MODELUIPG'", "'TARGET'"};
//        String finalQuery = sqlCur.getQueryWithBindings(bindValues);
//        debug("Final Query = " + finalQuery);
        
    }
    
    public static void mainTest(String[] args) {
        String value="value=\"http://ebs.config-consultants.com:8080/OA_HTML/OA.jsp?OAFunc=OAHOMEPAGE\"";
        ParseSqlTrace pst = new ParseSqlTrace(TRACE_FILE);
        String parseString = pst.parseValue("value", value);
        debug(parseString);
    }

	static void debug(String message) {
		if (debugEnabled) {
			System.out.println(message);
		}
		
	}
    
    
    
}
