package com.pankaj.parsetrace;

public class SqlCursor {
    
    private String cursorId;
    private String query;
    private int numOfBindVars;
    private int numOfTimesVarBound = 0;
    
    public SqlCursor(String cursorId, String query) {
        super();
        this.cursorId = cursorId;
        this.query = query;
        this.numOfBindVars = findNumOfBindVars(query);
    }


    public String getCursorId() {
        return cursorId;
    }

    public String getQuery() {
        return query;
    }

    private int findNumOfBindVars(String query) {
        //TODO: This is quick way to do, need to fine tune further to count colon in string. 
        //Code also needs to be optimized
        int count = 0;
        for(int i=0; i<query.length(); i++) {
            if (query.charAt(i)==':') {
                boolean isThisBindVar = false;
                while (i<(query.length()-1)) {
                    char currChar = query.charAt(++i);
                    if (Character.isLetterOrDigit(currChar)) {
                        isThisBindVar = true;
                    }else{
                        break;
                    }
                }
                if (isThisBindVar) {
                    count++;
                }
            }
        }
        return count;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SqlCursor[").append("CursorId=").append(cursorId).
            append(", Query=").append(query).append(", BindVariableCount=").append(numOfBindVars).append("]");
        return sb.toString();
    }

    public int getNumOfBindVars() {
        return numOfBindVars;
    }

    public String getQueryWithBindings(String[] bindValues) {
        if (bindValues==null) {
            throw new NullPointerException("Value array must not be null for cursor=" + this);
        }
        if (bindValues.length!=numOfBindVars) {
            throw new RuntimeException("Bound value array length=" + bindValues.length + " does not match for bound value for cursor=" + this);
        }
        numOfTimesVarBound++;
        int currBindIndex = 0;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<query.length(); i++) {
            char currChar = query.charAt(i);
            if (currChar==':') {
                boolean isThisBindVar = false;
                //while (i<(query.length()-1)) {
                for(int j=i+1; j<query.length(); j++) {
                    char ch = query.charAt(j);
                    if (Character.isLetterOrDigit(ch)) {
                        isThisBindVar = true;
                        break;
                    }else{
                        break;
                    }
                }
                if (isThisBindVar) {
                    if (bindValues[currBindIndex]==null) {
                        sb.append(currChar);
                        while (i<(query.length()-1)) {
                            currChar = query.charAt(++i);
                            sb.append(currChar);
                            if (!Character.isLetterOrDigit(currChar)) {
                                break;
                            }
                        }
                    }else{
                        sb.append(bindValues[currBindIndex]);//.append(" ");
                        while (i<(query.length()-1)) {
                            currChar = query.charAt(++i);
                            if (!Character.isLetterOrDigit(currChar)) {
                                break;
                            }
                        }
                        sb.append(currChar);
                    }
                    currBindIndex++;
                }else{
                    sb.append(currChar);
                }
            }else{
                sb.append(currChar);
            }
        }
        ParseSqlTrace.debug("Query with Binds=" + sb.toString() + " for cursor=" + this + " with Bind values=" + bindValues);
        return sb.toString();
    }
}
