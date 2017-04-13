package test;

import jmetal.util.PseudoRandom;
import jmetal.util.ReadMatrix;
import org.junit.Test;

/**
 * Created by Rudy Steiner on 2016/9/19.
 */
public class ReadMatrixTest {
    @Test
    public void  readMatrixTest(){
       // ReadMatrix.readMatrix("/resources/DTLZ1(3).dat",3);

        int[] ints={1,2,3,4,5,6,7};
        int[] shorts=new int[3];

        System.arraycopy(ints,0,shorts,0,3);
        System.out.println(0.00333);
        while (true)
           System.out.println(PseudoRandom.randInt(0,10));
    }
}
