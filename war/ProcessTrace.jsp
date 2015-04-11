<%@page import="com.pankaj.parsetrace.ParseSqlTrace"%>
<%@ page language="java" contentType="text/plain; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="java.io.*, com.google.appengine.api.blobstore.*, java.util.*,
    org.apache.commons.fileupload.*,
    org.apache.commons.fileupload.servlet.*"%>
<%!
//Your upload handle would look like
public static void doProcessingBlob(HttpServletRequest req, HttpServletResponse res) throws IOException {
	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
    List<BlobKey> blobKeys = blobs.get("tracefile");

    if (blobKeys == null || blobKeys.isEmpty()) {
        res.sendRedirect("ProcessTrace.jsp?message=No files found.");
        return;
    } else {
        res.sendRedirect("ProcessTrace.jsp??blob-key=" + blobKeys.get(0).getKeyString());
    }
}

public static void doProcessing(HttpServletRequest req, HttpServletResponse res, JspWriter out) 
		throws IOException, ServletException {
    try {
        ServletFileUpload upload = new ServletFileUpload();
        res.setContentType("text/plain");

        FileItemIterator iterator = upload.getItemIterator(req);
        while (iterator.hasNext()) {
          FileItemStream item = iterator.next();
          InputStream stream = item.openStream();

          if (item.isFormField()) {
           System.out.println("Got a form field: " + item.getFieldName());
          } else {
        	  System.out.println("Got an uploaded file: " + item.getFieldName() +
                        ", name = " + item.getName());

            // You now have the filename (item.getName() and the
            // contents (which you can read from stream). Here we just
            // print them back out to the servlet output stream, but you
            // will probably want to do something more interesting (for
            // example, wrap them in a Blob and commit them to the
            // datastore).
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(baos));

            //int len;
            //byte[] buffer = new byte[8192];
            //while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
            //  System.out.write(buffer, 0, len);
            //}
            System.out.println("*********** START Printing parsed trace ************************");
            ParseSqlTrace.parseTrace(br, bw);
            System.out.println("Size : " + baos.size());
            System.out.println("Byte Size : " + baos.toByteArray().length);
            //System.out.println("String : " + baos.toString());
            out.write(baos.toString());
            System.out.println("*********** END Printing parsed trace ************************");
            
          }
        }
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
}
%>
<% doProcessing(request, response, out); %>
