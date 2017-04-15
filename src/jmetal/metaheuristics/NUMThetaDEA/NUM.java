package jmetal.metaheuristics.NUMThetaDEA;

import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.AbstractThetaDEA;
import jmetal.operators.utils.CrossoverWrapper;
import jmetal.operators.utils.MutationWrapper;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 *  create by rudy2steiner
 *  @email rudy_steiner@163.com
 *  implement NUMThetaDEA  based on thetaDEA framework
 **/
public class NUM extends AbstractThetaDEA {
	private Operator SBXCrossover_;           // crossover
	private Operator nonuniformMutation_;   // mutation operator
	public NUM(Problem problem, String pfRoot, String pfSuffix, int curRun) {
		super(problem,pfRoot,pfSuffix,curRun);
	} // NUM
	@Override
	public void initOperators() {
		nonuniformMutation_ = operators_.get("NUM");    // set the mutation operator
		SBXCrossover_ = operators_.get("SBX");    // set the crossover operator
		//guassianMutation_=operators_.get("GUASSIAN");       // set the mutation operator
		//binarySelection_=operators_.get("BS");
	}

	@Override
	public void createOffSpringPopulation(SolutionSet parentPop, SolutionSet offSpringPop,
										  Problem problem,int curGeneration,int maxGeneration) {
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
		    nonuniformMutation_.setParameter("currentIteration", curGeneration);
		    nonuniformMutation_.execute(offSpring[0]);
			problem_.evaluate(offSpring[0]);
			offSpringPop.add(offSpring[0]);
		}catch (JMException e){
			e.printStackTrace();
		}
	}
}
