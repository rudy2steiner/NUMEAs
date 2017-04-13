package jmetal.operators.mutation;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.wrapper.XReal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rudy Steiner on 2017/1/12.
 */
public class NUPLM extends Mutation{
    private static final double ETA_M_DEFAULT_ = 20.0;
    private final double eta_m_=ETA_M_DEFAULT_;

    private Double mutationProbability_ = null ;
    private Double distributionIndex_ = eta_m_;
    private Integer maxIterations_ = null;
    private Double perturbation_ = 5.0;
    /**
     *    Valid solution types to apply this operator
     */

    private static final List VALID_TYPES = Arrays.asList(RealSolutionType.class, ArrayRealSolutionType.class) ;

    /**
     * Constructor
     * Creates a new instance of the polynomial mutation operator
     */
    public NUPLM(HashMap<String, Object> parameters) {
        super(parameters) ;
        if (parameters.get("probability") != null)
            mutationProbability_ = (Double) parameters.get("probability") ;
        if (parameters.get("distributionIndex") != null)
            distributionIndex_ = (Double) parameters.get("distributionIndex") ;
        if (parameters.get("perturbation") != null)
            perturbation_ = (Double) parameters.get("perturbation") ;
        if (parameters.get("maxIterations") != null)
            maxIterations_ = (Integer) parameters.get("maxIterations") ;

    } // PolynomialMutation

    /**
     * Perform the mutation operation
     * @param probability Mutation probability
     * @param solution The solution to mutate
     * @throws JMException
     */
    public void doMutation(double probability, Solution solution) throws JMException {
        double rnd, delta1, delta2, mut_pow, deltaq;
        double y, yl, yu, val, xy;
        double Delta;
        XReal x = new XReal(solution) ;
        for (int var=0; var < solution.numberOfVariables(); var++) {
            if (PseudoRandom.randDouble() <= probability)
            {

                y      = x.getValue(var);
                yl     = x.getLowerBound(var);
                yu     = x.getUpperBound(var);
                delta1 = (y-yl)/(yu-yl);
                delta2 = (yu-y)/(yu-yl);
                rnd = PseudoRandom.randDouble();
                mut_pow = 1.0/(distributionIndex_+1.0);
                if (rnd <= 0.5)
                {
                    xy     = 1.0-delta1;
                    val    = 2.0*rnd+(1.0-2.0*rnd)*(Math.pow(xy,(distributionIndex_+1.0)));
                    deltaq =  Math.pow(val,mut_pow) - 1.0;

                }
                else
                {
                    xy = 1.0-delta2;
                    val = 2.0*(1.0-rnd)+2.0*(rnd-0.5)*(Math.pow(xy,(distributionIndex_+1.0)));
                    deltaq = 1.0 - (Math.pow(val,mut_pow));

                }

                y = y + delta(deltaq*(yu-yl),perturbation_.doubleValue());
                if (y<yl)
                    y = yl;
                if (y>yu)
                    y = yu;
                x.setValue(var, y);
            }
        } // for

    } // doMutation

    private double delta(double y, double bMutationParameter) {
        double rand = PseudoRandom.randDouble();
        int it,maxIt;
        it    = ((Integer)getParameter("currentIteration")).intValue();//currentIteration_.intValue();
        maxIt = maxIterations_.intValue();
        //System.out.println((double)it/maxIt+" :"+y);

        return y*Math.pow((1.0 - it /(double) maxIt),1/bMutationParameter);
//                 (y * (1.0 -
//                Math.pow(rand, Math.pow((1.0 - it /(double) maxIt),bMutationParameter)
//                )));
    } // delta
    /**
     * Executes the operation
     * @param object An object containing a solution
     * @return An object containing the mutated solution
     * @throws JMException
     */
    public Object execute(Object object) throws JMException {
        Solution solution = (Solution)object;

        if (!VALID_TYPES.contains(solution.getType().getClass())) {
            Configuration.logger_.severe("PolynomialMutation.execute: the solution " +
                    "type " + solution.getType() + " is not allowed with this operator");

            Class cls = String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()") ;
        } // if

        doMutation(mutationProbability_, solution);
        return solution;
    } // execute

}
