package com.pankaj.parsetrace;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class JavaTest {
	
	public static void main(String args[]) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(baos));
    	bw.write("This is my test");
    	bw.flush();
    	baos.write("directly writing in data".getBytes());
    	System.out.println("Size : "+ baos.size());
    	System.out.println("String : "+ baos.toString());
	}

}
