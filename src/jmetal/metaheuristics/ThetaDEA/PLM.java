package jmetal.metaheuristics.ThetaDEA;

import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.AbstractThetaDEA;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * Created by Rudy Steiner on 2017/4/14.
 */
public class PLM extends AbstractThetaDEA{

    private Operator SBXCrossover_;           // crossover
    private Operator polyMutation_;   // mutation operator
    public PLM(Problem problem, String pfRoot, String pfSuffix, int curRun) {
        super(problem,pfRoot,pfSuffix,curRun);
    } // PLM
    @Override
    public void initOperators() {
        polyMutation_ = operators_.get("PLM");    // set the mutation operator
        SBXCrossover_ = operators_.get("SBX");    // set the crossover operator
    }

    @Override
    public void createOffSpringPopulation(SolutionSet parentPop, SolutionSet offSpringPop,
                                          Problem problem, int curGeneration, int maxGeneration) {
        int popSize=parentPop.size();
        for (int i = 0; i < popSize; i++)
            doCrossover(parentPop,offSpringPop,popSize,i,curGeneration);
    }
    void doCrossover(SolutionSet parentPop,SolutionSet offSpringPop,int popSize,int i,int curGeneration) {
        int r;
        do {
            r = PseudoRandom.randInt(0,popSize - 1);
        } while (r == i);
        Solution[] parents = new Solution[2];
        parents[0] = parentPop.get(i);
        parents[1] = parentPop.get(r);
        try {
            Solution[] offSpring = (Solution[]) SBXCrossover_.execute(parents);
            polyMutation_.execute(offSpring[0]);
            problem_.evaluate(offSpring[0]);
            offSpringPop.add(offSpring[0]);
        }catch (JMException e){
            e.printStackTrace();
        }
    }
}
