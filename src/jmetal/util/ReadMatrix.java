package jmetal.util;

/**
 * Created by Rudy Steiner on 2016/9/18.
 */
import Jama.Matrix;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReadMatrix {

    public final static Matrix readMatrix(String path,int column){

        InputStream input =ReadMatrix.class.getResourceAsStream(path);
        BufferedReader reader = null;
        List<String> data = new ArrayList<String>();
        //
        try {
            reader = new BufferedReader(new InputStreamReader(input));

            String temp = null;
            while((temp = reader.readLine()) != null){
                data.add(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
       if (data.size()>0){
           Matrix  matrix=new Matrix(data.size(),column);
           int j=0;
           for  (String str:data){
               String[] array=str.split(" ");
               Double[] row=new Double[array.length];
               for(int i=0;i<array.length;i++){
                   matrix.set(j,i,  Double.parseDouble(array[i]));
               }
               j++;
           }
           return matrix;
       }else return new Matrix(0,0);

    }
}
