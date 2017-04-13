package jmetal.util.vector;

import jmetal.encodings.variable.Int;
import jmetal.util.SortUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rudy Steiner on 2016/10/7.
 */
public class NeighbourGenerator {
    public static int[][]  generateNeighboursVector(double[][] lambda,int niche){
        int len=lambda.length;
        int [][] neighbours =new int[len][niche];
        double[] x = new double[len];
        int[] idx = new int[len];

        for (int i = 0; i < len; i++) {
            // calculate the distances based on weight vectors
            for (int j = 0; j < len; j++) {
                x[j] = SortUtil.distVector(lambda[i], lambda[j]);
                idx[j] = j;
            }

            // find 'niche' nearest neighboring subproblems
            SortUtil.minFastSort(x, idx, len, niche);
            System.arraycopy(idx, 0, neighbours[i], 0, niche);
        }
        return neighbours;
    }
    public static int[][]  generateNeighboursByConstantDistance(double[][] lambda,int h){
        int len=lambda.length;
        //double segma=Math.sqrt(2)/len*h;
        double segma=Math.sqrt(2)*0.5;
        System.out.println("neighbor distance:"+segma);
        int [][] neighbours =new int[len][];
        double[] x = new double[len];
        int[] idx = new int[len];
        List<Integer> list=new LinkedList<Integer>();
        for (int i = 0; i < len; i++) {

            // calculate the distances based on weight vectors
            for (int j = 0; j < len; j++) {
                x[j] = SortUtil.distVector(lambda[i], lambda[j]);
                idx[j] = j;
                if(x[j]<=segma)
                     list.add(new Integer(j));

            }
           // SortUtil.minFastSort(x, idx, len, len);
            int nbSize=list.size();
            System.out.print(nbSize+" ");
            neighbours[i]=new int[nbSize];
            for(int k=0;k<nbSize;k++)
                neighbours[i][k]=list.get(k).intValue();
            list.clear();
        }

        return neighbours;
    }
}
