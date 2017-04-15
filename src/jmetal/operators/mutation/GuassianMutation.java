package jmetal.operators.mutation;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.wrapper.XReal;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rudy Steiner on 2017/4/13.
 */
public class GuassianMutation extends Mutation{

    /**
     * Valid solution types to apply this operator
     */
    private static final List VALID_TYPES = Arrays.asList(RealSolutionType.class,
            ArrayRealSolutionType.class) ;
    private RandomEngine generator;
    private Normal normal;
    private Double mutationProbability_ = null;
    private static final double DEFAULT_SIGMMA =0.1;
    private static final double DEFAULT_U =0.0;
    private double sigmma = DEFAULT_SIGMMA;
    private double u = DEFAULT_U;
    public GuassianMutation(HashMap<String, Object> parameters) {
        super(parameters);
        if (parameters.get("probability") != null)
            mutationProbability_ = (Double) parameters.get("probability");
        if (parameters.get("sigmma") != null)
            sigmma = (Double) parameters.get("sigmma");
        generator =new MersenneTwister(new Date());
        normal=new Normal(u,sigmma,generator);
    }

    @Override
    public Object execute(Object object) throws JMException {
        Solution solution = (Solution)object;
        if (!VALID_TYPES.contains(solution.getType().getClass())) {
            Configuration.logger_.severe("NonUniformMutation.execute: the solution " + solution.getType() + "is not of the right type");
            Class cls = String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()") ;
        }
        doMutation(mutationProbability_,solution);
        return solution;
    }
    public void doMutation(double probability,Solution solution) throws JMException {
        XReal x = new XReal(solution) ;
        for (int var = 0; var < solution.getDecisionVariables().length; var++) {
            if (PseudoRandom.randDouble() < probability) {
                double rand = PseudoRandom.randDouble();
                double tmp;
                if (rand <= 0.5) {
                    tmp = delta(x.getUpperBound(var) - x.getValue(var));
                }
                else {
                    tmp = delta(x.getLowerBound(var) - x.getValue(var));
                }
                tmp += x.getValue(var);
                if (tmp < x.getLowerBound(var))
                    tmp = x.getLowerBound(var);
                else if (tmp > x.getUpperBound(var))
                    tmp = x.getUpperBound(var);
                x.setValue(var, tmp) ;
            }
        }
    } // doMutation
    /**
     * Calculates the delta value used in NonUniform mutation operator
     */
    private double delta(double y) {
        double rand;
        do {
            rand = normal.nextDouble();
        }while(rand>1.0);
        return (y * rand);
    } // delta

}
