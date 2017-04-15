package jmetal.operators.utils;

import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * Created by Rudy Steiner on 2017/4/14.
 */
public class CrossoverWrapper {
    public static void SBXCrossover(SolutionSet parentPop, SolutionSet offSpringPop,
                                    Operator sbx,Problem pro,int current) {
        int r;
        int popSize=parentPop.size();
        do {
            r = PseudoRandom.randInt(0, popSize - 1);
        } while (r == current);
        Solution[] parents = new Solution[2];
        parents[0] = parentPop.get(current);
        parents[1] = parentPop.get(r);
        try {
            Solution[] offSprings = (Solution[]) sbx.execute(parents);
            pro.evaluate(offSprings[0]);
            offSpringPop.add(offSprings[0]);
        }catch (JMException e){
            e.printStackTrace();
        }
    }
    public static void DECrossover(SolutionSet parentPop,SolutionSet offSpringPop,
                              Operator de,Operator deSelection ,Problem pro,int current) {
        Object[] parameters = new Object[2];
        parameters[0] = parentPop;
        parameters[1] = new Integer(current);
        try {
            Solution[] parents = (Solution[]) deSelection.execute(parameters);
            parameters[0] = parentPop.get(current);
            parameters[1] = parents;
            Solution offSpring = (Solution) de
                    .execute(parameters);
            pro.evaluate(offSpring);
            offSpringPop.add(offSpring);
        }catch (JMException e){
            e.printStackTrace();
        }
    }
}
