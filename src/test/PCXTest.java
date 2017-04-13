package test;

import org.junit.Test;

/**
 * Created by Rudy Steiner on 2016/10/21.
 */
public class PCXTest {
    @Test
    public void  pcxTest(){
        int i=1;
        Double a=new Double(0.3);
        Double b=a;
        b+=0.02;

         System.out.println(a);
        System.out.println(b);
    }
}
