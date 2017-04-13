package jmetal.util;

import jmetal.core.Solution;

import java.io.*;
import java.util.List;

public class FileUtils {
  static public void appendObjectToFile(String fileName, Object object) {
  	FileOutputStream fos;
    try {
	    fos = new FileOutputStream(fileName, true);
	    OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
	    BufferedWriter bw      = new BufferedWriter(osw)        ;
	                      
	    bw.write(object.toString());
	    bw.newLine();
	    bw.close();
    } catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
  }
  
  static public void createEmtpyFile(String fileName) {
  	FileOutputStream fos;
    try {
	    fos = new FileOutputStream(fileName, false);
	    OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
	    BufferedWriter bw      = new BufferedWriter(osw)        ;
	                      
	    bw.close();
    } catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
  }

  static public void saveArrayToFile(Class clazz,String path,String filename,Double[] value){

  	   if(value.length<=0) return;
	  try {
		  OutputStream fos = new FileOutputStream(path+filename);
		  OutputStreamWriter osw = new OutputStreamWriter(fos);
		  BufferedWriter bw = new BufferedWriter(osw);

		  for(int i=0;i<value.length;i++) {
			  bw.write(value[i].toString());
			  bw.newLine();
		  }
		  bw.close();
	  } catch (IOException e) {
		  Configuration.logger_.severe("Error acceding to the file");
		  e.printStackTrace();
	  }
  }
}
