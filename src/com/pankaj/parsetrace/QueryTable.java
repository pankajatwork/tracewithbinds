package com.pankaj.parsetrace;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

import java.text.SimpleDateFormat;

import java.util.Calendar;


public class QueryTable {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

  private static final String HTML_BLANK_SPACE = "&nbsp;";
  
  private String tableFormatStart = "<table border=1 cellpadding=3 cellspacing=0>";
  private String tableHeaderRowStart = "<tr>";
  private String tableDataRowStart = "<tr>";
  private String tableHeaderColStart = "<th><b>";
  private String tableDataColStart = "<td>";
  private String nullDataField = "-";
  private String noRecordFoundMessage = "No Records Found.";
  
  private String tableFormatEnd ="</table>";
  private String tableHeaderRowEnd = "</tr>";
  private String tableDataRowEnd = "</tr>";
  private String tableHeaderColEnd = "</b></th>";
  private String tableDataColEnd = "</td>";
  
  private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");
  
  
  private boolean doColumnHeadingFormatting = true;
  
  //if not null, uses given table header row instead of preparing from column names from database
  private String columnHeadingRow = null;
  
  //used to add serial number column in table, if not null, this column will be added with given title
  private String rowNumberColumnTitle = null;
  
  public QueryTable() {
    
  }
  
  public String getTableDataRowStart() {
    return tableDataRowStart;
  }

  public void setTableDataRowStart(String tableDataRowStart) {
    this.tableDataRowStart = tableDataRowStart;
  }

  public String getTableFormatStart() {
    return tableFormatStart;
  }

  public void setTableFormatStart(String tableFormatStart) {
    this.tableFormatStart = tableFormatStart;
  }

  public String getTableHeaderRowStart() {
    return tableHeaderRowStart;
  }

  public String getTableDataColStart() {
    return tableDataColStart;
  }

  public void setTableDataColStart(String tableDataColStart) {
    this.tableDataColStart = tableDataColStart;
  }

  public String getTableHeaderColStart() {
    return tableHeaderColStart;
  }

  public void setTableHeaderColStart(String tableHeaderColStart) {
    this.tableHeaderColStart = tableHeaderColStart;
  }

  public void setTableHeaderRowStart(String tableHeadingRowStart) {
    this.tableHeaderRowStart = tableHeadingRowStart;
  }

  public String getTableDataColEnd() {
    return tableDataColEnd;
  }

  public void setTableDataColEnd(String tableDataColEnd) {
    this.tableDataColEnd = tableDataColEnd;
  }

  public String getTableDataRowEnd() {
    return tableDataRowEnd;
  }

  public void setTableDataRowEnd(String tableDataRowEnd) {
    this.tableDataRowEnd = tableDataRowEnd;
  }

  public String getTableFormatEnd() {
    return tableFormatEnd;
  }

  public void setTableFormatEnd(String tableFormatEnd) {
    this.tableFormatEnd = tableFormatEnd;
  }

  public String getTableHeaderColEnd() {
    return tableHeaderColEnd;
  }

  public void setTableHeaderColEnd(String tableHeaderColEnd) {
    this.tableHeaderColEnd = tableHeaderColEnd;
  }

  public String getTableHeaderRowEnd() {
    return tableHeaderRowEnd;
  }

  public void setTableHeaderRowEnd(String tableHeaderRowEnd) {
    this.tableHeaderRowEnd = tableHeaderRowEnd;
  }

  public String getNullDataField() {
    return nullDataField;
  }

  public void setNullDataField(String nullDataField) {
    this.nullDataField = nullDataField;
  }

  public String getNoRecordFoundMessage() {
    return noRecordFoundMessage;
  }

  public void setNoRecordFoundMessage(String noRecordFoundMessage) {
    this.noRecordFoundMessage = noRecordFoundMessage;
  }

  public boolean isDoColumnHeadingFormatting() {
    return doColumnHeadingFormatting;
  }

  public void setDoColumnHeadingFormatting(boolean doColumnHeadingFormatting) {
    this.doColumnHeadingFormatting = doColumnHeadingFormatting;
  }

  public String getColumnHeadingRow() {
    return columnHeadingRow;
  }

  public void setColumnHeadingRow(String columnHeadingRow) {
    this.columnHeadingRow = columnHeadingRow;
  }

  public String getRowNumberColumnTitle() {
    return rowNumberColumnTitle;
  }

  public void setRowNumberColumnTitle(String rowNumberColumnTitle) {
    this.rowNumberColumnTitle = rowNumberColumnTitle;
  }

  public void setDateFormat(SimpleDateFormat simpleDateFormat) {
    if (simpleDateFormat==null)
      throw new NullPointerException("DateFormat Cannot be set to null. It is used to format the date field of the query");
    this.dateFormat = simpleDateFormat;
  }

  public SimpleDateFormat getDateFormat() {
    return dateFormat;
  }

  public String getQueryTableData(Connection conn, String query) {
    debug("QueryTable.getQueryTableData => query: " + query);
    String retStr = null;
    Statement stmt = null;
    ResultSet rs = null;
    
    try {
      stmt = conn.createStatement();
      boolean hasResultSet = stmt.execute(query);
      if (hasResultSet) {
        rs = stmt.getResultSet();
        retStr = getQueryTableData(rs);
      } else { //its execute query, get updateCount
        int updateCount = stmt.getUpdateCount();
        if (updateCount==-1) { 
          //actually this is some other kind of result, not update  query result, but query executed successfully
          retStr = getQuerySuccessfulTable();
        }else{
          //got the update  count, return it
          retStr = getUpdateCountTable(updateCount);
        }
      }
    }catch (SQLException e) {
      error("There might be errors in preparing html table code successfully.", e);
      retStr = e.getMessage();
    }finally {
      if (stmt!=null){
        try {
          stmt.close();
        } catch (SQLException e) {
        }
      }
      if (rs!=null){
        try {
          rs.close();
        } catch (SQLException e) {
        }
      }
    }
    return retStr;
  }
  
  private String getUpdateCountTable(int updateCount) {

    try {
      StringBuffer buffer = new StringBuffer();
      // Start table
      buffer.append(getTableFormatStart()).append(LINE_SEPARATOR);
      
      if (columnHeadingRow!=null && columnHeadingRow.trim().length()==0) {
        // column heading row given, so set it
        buffer.append(columnHeadingRow);
        debug("QueryTable : ColumnHeadingRow set as given.");
      }else{
        // Prepare Header Row
        buffer.append(tableHeaderRowStart).append(LINE_SEPARATOR);
        //add row number column if title is set for it
        buffer.append(tableHeaderColStart).append("Rows Updated");
        buffer.append(tableHeaderColEnd).append(LINE_SEPARATOR);
        buffer.append(tableHeaderRowEnd).append(LINE_SEPARATOR);
      }
        
      buffer.append(tableDataRowStart);
      buffer.append(tableDataColStart).append(updateCount).append(tableDataColEnd).append(LINE_SEPARATOR);
      buffer.append(tableDataRowEnd).append(LINE_SEPARATOR);
      // End table
      buffer.append(tableFormatEnd);
      return buffer.toString();
    }catch(Exception e){
      error("QueryTable: Exception while preparing html table from query", e);
      return "No Processing Done. Internal Error while getting updateCountTable : " + e.getMessage();
    }
  }

  private String getQuerySuccessfulTable() {
    try {
      StringBuffer buffer = new StringBuffer();
      // Start table
      buffer.append(getTableFormatStart()).append(LINE_SEPARATOR);
      
      // Prepare Header Row
      buffer.append(tableHeaderRowStart).append(LINE_SEPARATOR);
      buffer.append(tableHeaderColStart).append("Query Executed Successfully, but returned no result and did not update any rows.");
      buffer.append(tableHeaderColEnd).append(LINE_SEPARATOR);
      buffer.append(tableHeaderRowEnd).append(LINE_SEPARATOR);
      
      // End table
      buffer.append(tableFormatEnd);
      return buffer.toString();
    }catch(Exception e){
      error("QueryTable: Exception while preparing html table from query", e);
      return "No Processing Done. Internal Error while returning QuerySuccessfulTable : " + e.getMessage();
    }
  }

  public String getQueryTableData(ResultSet rs) {
    try {
      ResultSetMetaData metaData = rs.getMetaData();
      int totalColumns = metaData.getColumnCount();
      StringBuffer buffer = new StringBuffer();
      String columnDisplayName = null;
      String columnValue = null;
      int[] columnType = new int[totalColumns+1];
      
      // Start table
      buffer.append(getTableFormatStart()).append(LINE_SEPARATOR);
      
      if (columnHeadingRow!=null && columnHeadingRow.trim().length()==0) {
        // column heading row given, so set it
        buffer.append(columnHeadingRow);
        debug("QueryTable : ColumnHeadingRow set as given.");
      }else{
        // Prepare Header Row
        buffer.append(tableHeaderRowStart).append(LINE_SEPARATOR);
        //add row number column if title is set for it
        if (rowNumberColumnTitle!=null) {
          buffer.append(tableHeaderColStart).append(rowNumberColumnTitle);
          buffer.append(tableHeaderColEnd).append(LINE_SEPARATOR);
        }
        for(int i = 1; i<=totalColumns; i++) {
          buffer.append(tableHeaderColStart);
          columnType[i] = metaData.getColumnType(i);
          if (doColumnHeadingFormatting) {
            columnDisplayName = formatColumnHeading(metaData.getColumnLabel(i));
          }else{
            columnDisplayName = metaData.getColumnLabel(i);
          }
          buffer.append(columnDisplayName);
          buffer.append(tableHeaderColEnd).append(LINE_SEPARATOR);
        }
        buffer.append(tableHeaderRowEnd).append(LINE_SEPARATOR);
      }
        
      
      // Prepare Data Rows
      int count = 1;
      if (rs.next()) {
        do {
          buffer.append(tableDataRowStart);
          if (rowNumberColumnTitle!=null) {
            buffer.append(tableDataColStart).append(count++).append(tableDataColEnd);
          }
          for (int i = 1; i <= totalColumns; i++) {
            buffer.append(tableDataColStart);
            // buffer.append("*" + columnType[i] + "*");
            if (columnType[i] == Types.TIMESTAMP) {
              // this is date
              Timestamp timestamp = rs.getTimestamp(i);
              if (timestamp != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(timestamp);
                columnValue = dateFormat.format(cal.getTime());
              }
            } else {
              columnValue = rs.getString(i);
            }
            buffer.append(columnValue != null ? 
                (columnValue=columnValue.trim()).length()>0 ? columnValue : HTML_BLANK_SPACE
                : nullDataField);
            buffer.append(tableDataColEnd);
          }
          buffer.append(tableDataRowEnd).append(LINE_SEPARATOR);
        }while (rs.next());
      }else {
        // Handle no data found in query case
        buffer.append(tableDataRowStart);
        buffer.append("<td colspan=" + totalColumns +" align=center>");
        buffer.append(noRecordFoundMessage);
        buffer.append("</td>");
        buffer.append(tableDataRowEnd).append(LINE_SEPARATOR);
      }
      
      // End table
      buffer.append(tableFormatEnd);
      return buffer.toString();
    }catch(Exception e){
      error("QueryTable: Exception while preparing html table from query", e);
    }
    debug("QueryTable: No processing was done, returning Internal Server Error.");
    return "No Processing Done. Internal Error";
  }
  
  public static void debug(String message) {
    System.out.println(message);
  }
  public static void error(String message, Throwable t) {
    debug(message);
    t.printStackTrace();
  }
  
  private String formatColumnHeading(String columnLabel) {
    char[] label = null;
    try {
      //formattedLabel = columnLabel.replace('_',' ');
      label = columnLabel.toCharArray();
      boolean previousCharSpace = true;
      for (int i=0; i<label.length; i++) {
        if (label[i]=='_') {
          label[i] = ' ';
          previousCharSpace = true;
        }else if (previousCharSpace){
          label[i] = Character.toUpperCase(label[i]);
          previousCharSpace = false;
        }else{
          label[i] = Character.toLowerCase(label[i]);
        }
      }
    }catch(Exception e){
      error("Error in processing column heading : " + columnLabel, e);
      return "UNKNOWN";
    }
    return new String(label);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    //String query = "select department_id as ID, dept_name as Department_Name, description as Text_DESC FROM tbldepartment where 1=2";
    //String query = "select * from tblbugfix";
    System.out.println("This is start");
    QueryTable queryTable = new QueryTable();
    //System.out.println(queryTable.formatColumnHeading(null));
    System.out.println(queryTable.formatColumnHeading("null"));
    System.out.println(queryTable.formatColumnHeading("!#@$@#$!@#$"));
    System.out.println(queryTable.formatColumnHeading("HELLO_WORLD"));
    System.out.println(queryTable.formatColumnHeading("__this_is_for_test"));
    System.out.println(queryTable.formatColumnHeading("ThiS__Way_"));
    System.out.println(queryTable.formatColumnHeading("You_must_bE___happy_to_sEE_This"));
    
    String query = "SELECT customer_id, firstname, lastname, email, mobile from customers";
    QueryTable qTable = new QueryTable();
    qTable.setRowNumberColumnTitle("Row Number");
    Connection conn = null;
    System.out.println(qTable.getQueryTableData(conn, query));
  }

}
