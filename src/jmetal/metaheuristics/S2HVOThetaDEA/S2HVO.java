package jmetal.metaheuristics.S2HVOThetaDEA;
import jmetal.core.*;
import jmetal.metaheuristics.AbstractThetaDEA;
import jmetal.operators.utils.CrossoverWrapper;
import jmetal.operators.utils.MutationWrapper;
import jmetal.util.*;
/**
 *  create by rudy2steiner
 *  @email rudy_steiner@163.com
 *  implement 2SHVOThetaDEA  based on thetaDEA framework
 **/
public class S2HVO extends AbstractThetaDEA {
	private Operator SBXCrossover_; // crossover
	private Operator DECrossover_; // crossover
	private Operator nonuniformMutation_;   // mutation operator
	private Operator DESelection_;
	public S2HVO(Problem problem,String pfRoot,String pfSuffix,int curRun) {
		super(problem,pfRoot,pfSuffix,curRun);
	} // S2HVO
	@Override
	public void initOperators() {
		SBXCrossover_ = operators_.get("SBX"); // set the crossover operator
		nonuniformMutation_ = operators_.get("NUM");  // set the mutation operator
		DECrossover_ = operators_.get("DE");
		DESelection_ = operators_.get("DS");
	}

	@Override
	public void createOffSpringPopulation(SolutionSet parentPop, SolutionSet offSpringPop,
										  Problem problem,int curGeneration,int maxGeneration)  {
	    int popSize=parentPop.size();
		int midGeneration=maxGeneration/2;
		for (int i = 0; i < popSize; i++)
		{
			int current = i;
			if(curGeneration<midGeneration) {
				int k = PseudoRandom.randInt(1, 3);
				if (k == 1) {
					SBXCrossover(parentPop,offSpringPop,current);
				} else if (k == 2) {
					DEOperator(parentPop,offSpringPop,current);
				} else {
					NonUniformMutation(parentPop,offSpringPop,current,curGeneration);
				}
			}else{
				int k = PseudoRandom.randInt(1,5);
				if(k<=4) SBXCrossover(parentPop,offSpringPop,current);
				else     DEOperator(parentPop,offSpringPop,current);
			}
		}
	}
	public void NonUniformMutation(SolutionSet parentPop,SolutionSet offSpringPop,
								    int current,int curGeneration){
		MutationWrapper.NonUniformMutation(parentPop,offSpringPop,nonuniformMutation_,problem_,current,curGeneration);

	}
	private void SBXCrossover(SolutionSet parentPop,SolutionSet offSpringPop,int current) {
		CrossoverWrapper.SBXCrossover(parentPop,offSpringPop,SBXCrossover_,problem_,current);
	}

	private void DEOperator(SolutionSet parentPop,SolutionSet offSpringPop,int current) {
		CrossoverWrapper.DECrossover(parentPop,offSpringPop,DECrossover_,DESelection_,problem_,current);
	}
}
