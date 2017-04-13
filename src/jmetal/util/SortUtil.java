package jmetal.util;

import jmetal.core.Solution;
import jmetal.core.Variable;

/**
 * Created by Rudy Steiner on 2016/10/2.
 */
public class SortUtil {

    public static void minFastSort(double x[], int idx[], int n, int m) {
        for (int i = 0; i < m; i++) {
            for (int j = i + 1; j < n; j++) {
                if (x[i] > x[j]) {
                    double temp = x[i];
                    x[i] = x[j];
                    x[j] = temp;
                    int id = idx[i];
                    idx[i] = idx[j];
                    idx[j] = id;
                }
            }
        }
    }
    public static void maxFastSort(double x[], int idx[], int n, int m) {
        for (int i = 0; i < m; i++) {
            for (int j = i + 1; j < n; j++) {
                if (x[i] <x[j]) {
                    double temp = x[i];
                    x[i] = x[j];
                    x[j] = temp;
                    int id = idx[i];
                    idx[i] = idx[j];
                    idx[j] = id;
                }
            }
        }
    }
    public static double distVector(double[] vector1, double[] vector2) {
        int dim = vector1.length;
        double sum = 0;
        for (int n = 0; n < dim; n++) {
            sum += (vector1[n] - vector2[n]) * (vector1[n] - vector2[n]);
        }
        return Math.sqrt(sum);
    }
    public static double distVector(Variable[] vector1, Variable[] vector2) {
        int dim = vector1.length;
        double sum = 0;
        try {
            for (int n = 0; n < dim; n++) {
                sum += (vector1[n].getValue() - vector2[n].getValue()) * (vector1[n].getValue() - vector2[n].getValue());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return Math.sqrt(sum);
    }
    public static double distBetweenNormalizedSolution(Solution sol1, Solution sol2){
       int dim=sol1.getNumberOfObjectives();
        double sum=0;
        for(int i=0;i<dim;i++){
            sum+=(sol1.getNormalizedObjective(i)-sol2.getNormalizedObjective(i))*(sol1.getNormalizedObjective(i)-sol2.getNormalizedObjective(i));
        }
        return Math.sqrt(sum);
    }
}
