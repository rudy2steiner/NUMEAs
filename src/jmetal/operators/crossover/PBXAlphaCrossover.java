package jmetal.operators.crossover;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.vector.Vector;
import jmetal.util.wrapper.XReal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rudy Steiner on 2016/10/21.
 */
public class PBXAlphaCrossover extends Crossover {

    /**
     * Valid solution types to apply this operator
     */
    private static final List VALID_TYPES = Arrays.asList(RealSolutionType.class,
            ArrayRealSolutionType.class) ;
    private final static double ALPHA_DEFAULT=0.8;
    private double alpha_=ALPHA_DEFAULT;
    public PBXAlphaCrossover(HashMap<String, Object> parameters) {
        super(parameters);

        if (parameters.get("alpha") != null)
            alpha_ = (double) parameters.get("alpha") ;
    }
    public Solution[] doCrossover(Solution parent1,Solution parent2) throws JMException{
        Solution [] offSpring = new Solution[2];

        offSpring[0] = new Solution(parent1);
        offSpring[1] = new Solution(parent2);

        XReal x1 = new XReal(parent1) ;
        XReal x2 = new XReal(parent2) ;
        XReal offs1 = new XReal(offSpring[0]) ;
        XReal offs2 = new XReal(offSpring[1]) ;
        int n= x1.getNumberOfDecisionVariables();
        double[] low=new double[n];
        double[] upper=new double[n];
       //  double randAlpha_= PseudoRandom.randDouble()*alpha_;
           for(int i=0;i<n;i++){
               double I=Math.abs(x1.getValue(i)-x2.getValue(i));
               low[i]= Math.max(x1.getLowerBound(i),x1.getValue(i)-I*alpha_);
               upper[i]=Math.min(x1.getUpperBound(i),x1.getValue(i)+I*alpha_);
               if(PseudoRandom.randDouble()<=0.2)
                      offs1.setValue(i, PseudoRandom.randDouble(low[i],upper[i]));
               else  offs1.setValue(i, x2.getValue(i));
           }

        for(int i=0;i<n;i++){
            double I=Math.abs(x1.getValue(i)-x2.getValue(i));
            low[i]= Math.max(x1.getLowerBound(i),x2.getValue(i)-I*alpha_);
            upper[i]=Math.min(x1.getUpperBound(i),x2.getValue(i)+I*alpha_);
            if(PseudoRandom.randDouble()<=0.5)
                offs2.setValue(i, PseudoRandom.randDouble(low[i],upper[i]));
            else  offs2.setValue(i, x1.getValue(i));
        }
        return offSpring;
    }
    @Override
    public Object execute(Object object) throws JMException {
        Solution [] parents = (Solution [])object;
        if (parents.length != 2) {
            Configuration.logger_.severe("PBXCrossover.execute: operator needs two " +
                    "parents");
            Class cls = String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()") ;
        } // if

        if (!(VALID_TYPES.contains(parents[0].getType().getClass())  &&
                VALID_TYPES.contains(parents[1].getType().getClass())) ) {
            Configuration.logger_.severe("PBXCrossover.execute: the solutions " +
                    "type " + parents[0].getType() + " is not allowed with this operator");

            Class cls = String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()") ;
        } // if
        Solution [] offSpring;
        offSpring = doCrossover(parents[0], parents[1]);

        return offSpring;
    }
}
