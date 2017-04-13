package jmetal.operators.crossover;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PRNG;
import jmetal.util.wrapper.XReal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rudy Steiner on 2016/10/22.
 */
public class PNXCrossover extends Crossover{

    private Double crossoverProbability_;
    private  double eta=2;
    /**
     * Valid solution types to apply this operator
     */
    private static final List VALID_TYPES = Arrays.asList(RealSolutionType.class,
            ArrayRealSolutionType.class) ;

    public PNXCrossover(HashMap<String, Object> parameters) {

        super(parameters);
        if (parameters.get("probability") != null)
            crossoverProbability_ = (Double) parameters.get("probability") ;
    }
  public Solution[] doCrossover(double probability,
                                Solution parent1,
                                Solution parent2) throws JMException{
      double c1,c2;
      double upperValue ;
      double lowerValue ;
      Solution [] offSpring = new Solution[2];

      offSpring[0] = new Solution(parent1);
      offSpring[1] = new Solution(parent2);

      XReal x1 = new XReal(parent1) ;
      XReal x2 = new XReal(parent2) ;
      XReal offs1 = new XReal(offSpring[0]) ;
      XReal offs2 = new XReal(offSpring[1]) ;
      int numberOfVariables =x1.getNumberOfDecisionVariables();

      if(PRNG.nextDouble()<crossoverProbability_) {
          for (int i = 0; i < numberOfVariables; i++) {
              if (PRNG.nextDouble() <=0.3) {
                  upperValue = x1.getUpperBound(i);
                  lowerValue = x1.getLowerBound(i);
                  c1 = PRNG.nextGaussian(x1.getValue(i), Math.abs(x1.getValue(i) - x2.getValue(i)) / eta);
                  c2 = PRNG.nextGaussian(x2.getValue(i), Math.abs(x1.getValue(i) - x2.getValue(i)) / eta);
                  if (c1 < lowerValue)
                      offs1.setValue(i, lowerValue);
                  else if (c1 > upperValue)
                      offs1.setValue(i, upperValue);
                  else
                      offs1.setValue(i, c1);

                  if (c2 < lowerValue)
                      offs2.setValue(i, lowerValue);
                  else if (c2 > upperValue)
                      offs2.setValue(i, upperValue);
                  else
                      offs2.setValue(i, c2);

              }
          }
      }
      return offSpring;
  }
    @Override
    public Object execute(Object object) throws JMException {

        Solution[] parents = (Solution [])object;
        if (parents.length != 2) {
            Configuration.logger_.severe("PNXCrossover.execute: operator needs two " +
                    "parents");
            Class cls = String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()") ;
        } // if

        if (!(VALID_TYPES.contains(parents[0].getType().getClass())  &&
                VALID_TYPES.contains(parents[1].getType().getClass())) ) {
            Configuration.logger_.severe(" PNXCrossover.execute: the solutions " +
                    "type " + parents[0].getType() + " is not allowed with this operator");

            Class cls = String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()") ;
        } // if
        Solution [] offSpring;
        offSpring = doCrossover(crossoverProbability_,
                parents[0],
                parents[1]);
        return offSpring;
    }
}
