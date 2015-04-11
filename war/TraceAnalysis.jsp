<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
     import="java.io.*, com.google.appengine.api.blobstore.*, java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Trace Analysis</title>
</head>
<body>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService(); %>
	<!-- <form action="<%= blobstoreService.createUploadUrl("/ProcessTrace.jsp") %>" method="post" enctype="multipart/form-data">
	-->
	<form action="/ProcessTrace.jsp"method="post" enctype="multipart/form-data">
		<h3>Generate SQL trace with bind parameters attached in query from raw trace file</h3>
	    Select trace file for upload : 
	    <input name="tracefile" type="file" size="50"> <br/>
	    <input name="Submit" type="submit" value="Sumbit">
	</form>
</body>
</html>