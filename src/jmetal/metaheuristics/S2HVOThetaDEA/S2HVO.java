package jmetal.metaheuristics.S2HVOThetaDEA;
import Jama.Matrix;
import jmetal.core.*;
import jmetal.metaheuristics.AbstractThetaDEA;
import jmetal.metaheuristics.Environment;
import jmetal.quality.IGD;
import jmetal.util.*;
import jmetal.util.ranking.NondominatedRanking;
import jmetal.util.ranking.Ranking;
import jmetal.util.ranking.ThetaRanking;
import jmetal.util.vector.TwoLevelWeightVectorGenerator;
import jmetal.util.vector.VectorGenerator;
import java.util.ArrayList;
import java.util.List;

import static jmetal.util.StringUtils.combine;

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
		DESelection_ = operators_.get("DES");
	}

	@Override
	public void createOffSpringPopulation(SolutionSet parentPop, SolutionSet offSpringPop, Problem problem,int curGeneration,int maxGeneration)  {
	    int popSize=parentPop.size();
		for (int i = 0; i < popSize; i++)
		{
			int current = i;
			if(curGeneration<maxGeneration/2) {
				int k = PseudoRandom.randInt(1, 3);
				if (k == 1) {
					SBXCrossover(parentPop,offSpringPop,current, false,curGeneration);
				} else if (k == 2) {
					DEOperator(parentPop,offSpringPop,current, false,curGeneration);
				} else {
					NonUniformMutation(parentPop,offSpringPop,current,curGeneration);
				}
			}else{
				int k = PseudoRandom.randInt(1,5);
				if(k<=4) SBXCrossover(parentPop,offSpringPop,current, false,curGeneration);
				else     DEOperator(parentPop,offSpringPop,current, false,curGeneration);
			}
		}
	}

	public void NonUniformMutation(SolutionSet parentPop,SolutionSet offSpringPop,int current,int curGeneration){
		Solution sol = new Solution(parentPop.get(current));
		nonuniformMutation_.setParameter("currentIteration",curGeneration);
		try {
			nonuniformMutation_.execute(sol);
			problem_.evaluate(sol);
			offSpringPop.add(sol);
		}catch (JMException e){
			e.printStackTrace();
		}
	}
	private void SBXCrossover(SolutionSet parentPop,SolutionSet offSpringPop,int current,boolean mutation,int curGeneration) {
		int r;
		int popSize=parentPop.size();
		do {
			r = PseudoRandom.randInt(0, popSize - 1);
		} while (r == current);
		Solution[] parents = new Solution[2];
		parents[0] = parentPop.get(current);
		parents[1] = parentPop.get(r);
		try {
			Solution[] offSprings = (Solution[]) SBXCrossover_.execute(parents);
			if (mutation) {
				nonuniformMutation_.setParameter("currentIteration", curGeneration);
				nonuniformMutation_.execute(offSprings[0]);
			}
			problem_.evaluate(offSprings[0]);
			offSpringPop.add(offSprings[0]);
		}catch (JMException e){
			e.printStackTrace();
		}
	}

	private void DEOperator(SolutionSet parentPop,SolutionSet offSpringPop,int current,boolean mutation,int curGeneration) {
		//DE
		Object[] parameters = new Object[2];
		parameters[0] = parentPop;
		parameters[1] = new Integer(current);
		try {
			Solution[] parents = (Solution[]) DESelection_.execute(parameters);
			parameters[0] = parentPop.get(current);
			parameters[1] = parents;
			Solution offSpring = (Solution) DECrossover_
					.execute(parameters);
			if (mutation) {
				nonuniformMutation_.setParameter("currentIteration", curGeneration);
				nonuniformMutation_.execute(offSpring);
			}
			problem_.evaluate(offSpring);
			offSpringPop.add(offSpring);
		}catch (JMException e){
			e.printStackTrace();
		}

	}
}
