package jmetal.metaheuristics.NUMTheta;

import java.util.HashMap;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.Config;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.CauchyMutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;

public class NUMThetaDEA_main {
	public static void main(String args[]) throws JMException, ClassNotFoundException{
		Problem problem; // The problem to solve
		Algorithm algorithm; // The algorithm to use
		Operator crossover; // Crossover operator
		Operator polyMutation; // Mutation operator
		Operator nonuniformMutation; // Mutation operator
		Operator cauchyMutation;
		Operator BinarySelection;
		HashMap parameters; // Operator parameters
		//总共有8个不同的目标函数：{"DTLZ1","DTLZ2","DTLZ3","DTLZ4","SDTLZ1","SDTLZ2","WFG6","WFG7"}
		String[] probNames={"DTLZ1"};
		int[] objs={3};

		String probName = "DTLZ1";
		int nobj =3;
		long batchStart=System.currentTimeMillis();
		for(String pro :probNames) {
			for (int currentObj : objs) {

				probName = pro;
				nobj = currentObj;
				problem = Config.setProblem(probName, nobj);

				int[] divs = Config.setDivs(nobj);
				int maxGenerations = Config.setMaxGenerations(probName, nobj);

				algorithm = new NUMThetaDEA(problem);


				algorithm.setInputParameter("normalize", true);


				algorithm.setInputParameter("theta", 5.0);
				algorithm.setInputParameter("div1", divs[0]);
				algorithm.setInputParameter("div2", divs[1]);


				algorithm.setInputParameter("maxGenerations", maxGenerations);

				// Mutation and Crossover for Real codification
				parameters = new HashMap();
				parameters.put("probability", 1.0);
				parameters.put("distributionIndex", 30.0);
				crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover",
						parameters);
                //polynomial mutation
				parameters = new HashMap();
				parameters.put("probability", 1.0 / problem.getNumberOfVariables());
				parameters.put("distributionIndex", 20.0);
				polyMutation = MutationFactory.getMutationOperator("PolynomialMutation",
						parameters);
                // non-uniform mutation
				parameters = new HashMap();
				parameters.put("probability", 1.0 / problem.getNumberOfVariables());
				parameters.put("perturbation", 6.0);

				System.out.println("perturbation:" + (Double) parameters.get("perturbation"));
				parameters.put("maxIterations", algorithm.getInputParameter("maxGenerations"));
				nonuniformMutation = MutationFactory.getMutationOperator("NonUniformMutation",
						parameters);
				//cauchy mutation
				parameters=new HashMap();
				parameters.put("probability", 1.0 / problem.getNumberOfVariables());
				parameters.put("segma",0.3);
				cauchyMutation=new CauchyMutation(parameters);

				parameters = new HashMap();
				BinarySelection = SelectionFactory.getSelectionOperator("BinaryTournament3",
						parameters);

				// Add the operators to the algorithm
				algorithm.addOperator("crossover", crossover);
				algorithm.addOperator("PM", polyMutation);
				algorithm.addOperator("NUM", nonuniformMutation);
				algorithm.addOperator("CAUCHY",cauchyMutation);
				algorithm.addOperator("BS", BinarySelection);
				StringBuilder builder = new StringBuilder();
				long starttime = System.currentTimeMillis();
				builder.append("开始时间戳：").append(starttime).append(" ms,");
				SolutionSet population = algorithm.execute();
				long endtime = System.currentTimeMillis();
				builder.append("结束时间戳：").append(endtime).append(" ms").append("\r\n");
				builder.append("持续时间：").append(endtime - starttime).append(" ms").append("\r\n--------------------");
				System.out.println(builder);
			}
		}
		long batchEnd=System.currentTimeMillis();
		StringBuilder builder=new StringBuilder();
		builder.append("----------------------\r\n");
		builder.append("批操作开始时间戳：").append(batchStart).append(" ms,");
		builder.append("批操作结束时间戳：").append(batchEnd).append(" ms").append("\r\n");
		builder.append("持续时间：").append(batchEnd-batchStart).append(" ms").append("\r\n--------------------");
		System.out.println(builder);
	}
}
