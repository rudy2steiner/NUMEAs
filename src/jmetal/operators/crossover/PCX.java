package jmetal.operators.crossover;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PRNG;
import jmetal.util.vector.Vector;
import jmetal.util.wrapper.XReal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rudy Steiner on 2016/10/21.
 */
public class PCX extends Crossover {

    /**
     * The number of parents required by this operator.
     */
    private  int numberOfParents;

    /**
     * The number of offspring produced by this operator.
     */
    private int numberOfOffspring;

    /**
     * The standard deviation of the normal distribution controlling the spread
     * of solutions in the direction of the selected parent.
     */
    private final double eta=0.1;
    /**
     * Valid solution types to apply this operator
     */
    private static final List VALID_TYPES = Arrays.asList(RealSolutionType.class,
            ArrayRealSolutionType.class) ;
    /**
     * The standard deviation of the normal distribution controlling the spread
     * of solutions in the directions defined by the remaining parents.
     */
    private final double zeta=0.1;

    public PCX(HashMap<String, Object> parameters) {
        super(parameters);
        if (parameters.get("numberOfParents") != null)
            numberOfParents = (int) parameters.get("numberOfParents") ;
        if (parameters.get("numberOfOffspring") != null)
            numberOfOffspring = (int) parameters.get("numberOfOffspring") ;
    }
    public Solution doCrossover(Solution[] parents) throws JMException{

        int k = parents.length;                                           //size of parents
        int n = parents[0].getDecisionVariables().length;
        double[][] x = new double[k][n];
        XReal[] xr=new XReal[k];
        Solution offSpring=new Solution(parents[k-1]);
        XReal    xOffSpring=new XReal(offSpring);

       for(int i=0;i<k;i++){
           xr[i]=new XReal(parents[i]);
       }
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < n; j++) {
                x[i][j] = xr[i].getValue(j);
            }
        }

        double[] g = Vector.mean(x);

        List<double[]> e_eta = new ArrayList<double[]>();

        e_eta.add(Vector.subtract(x[k - 1], g));

        double D = 0.0;

        // basis vectors defined by parents
        for (int i = 0; i < k - 1; i++) {
            double[] d = Vector.subtract(x[i], g);

            if (!Vector.isZero(d)) {
                double[] e = Vector.orthogonalize(d, e_eta);

                if (!Vector.isZero(e)) {
                    D += Vector.magnitude(e);
                    e_eta.add(Vector.normalize(e));
                }
            }
        }

        D /= k - 1;

        // construct the offspring
        double[] variables = x[k - 1];

        variables = Vector.add(variables, Vector.multiply(PRNG.nextGaussian(
                0.0, zeta), e_eta.get(0)));

        double eta = PRNG.nextGaussian(0.0, this.eta);
        for (int i = 1; i < e_eta.size(); i++) {
            variables = Vector.add(variables, Vector.multiply(eta * D, e_eta
                    .get(i)));
        }

        int p=PRNG.nextInt(k-1);
        for (int j = 0; j < n; j++) {

            double value = variables[j];
            if (value < xOffSpring.getLowerBound(j)) {
                value = xOffSpring.getLowerBound(j);
            } else if (value > xOffSpring.getUpperBound(j)) {
                value = xOffSpring.getUpperBound(j);
            }
             if(PRNG.nextDouble()<=0.4)
                  xOffSpring.setValue(j,value);
             else  {
                 xOffSpring.setValue(j,xr[p].getValue(j));
             }
        }

        return offSpring;
    }
    @Override
    public Object execute(Object object) throws JMException {

        Solution[] objs=(Solution[])object;
        Solution[] parents=new Solution[objs.length];         //prevent reordering parents
        for(int i=0;i<objs.length;i++){
            parents[i]=new Solution(objs[i]);
        }
        if (parents.length < 2) {
            Configuration.logger_.severe("PCX Crossover.execute: operator needs at least two " +
                    "parents");
            Class cls = String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()") ;
        } // if
        for(int i=0;i<parents.length;i++) {
            if (!VALID_TYPES.contains(parents[i].getType().getClass())) {
                Configuration.logger_.severe("PCX Crossover.execute: the solutions " +
                        "type " + parents[0].getType() + " is not allowed with this operator");

                Class cls = String.class;
                String name = cls.getName();
                throw new JMException("Exception in " + name + ".execute()");
            } // if
        }//for

        Solution[] offSpring=new Solution[numberOfOffspring];
        for (int i = 0; i < numberOfOffspring; i++) {
            int index = parents.length-1-i;//PRNG.nextInt(parents.length);
            Solution temp = parents[index];
            parents[index] = parents[parents.length - 1];
            parents[parents.length - 1] = temp;
            offSpring[i]=doCrossover(parents);
        }
        return offSpring;
    }


}
