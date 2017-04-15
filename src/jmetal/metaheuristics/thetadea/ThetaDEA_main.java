package jmetal.metaheuristics.ThetaDEA;

import java.util.HashMap;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.metaheuristics.Config;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.util.JMException;

public class ThetaDEA_main {
	public static void main(String args[]) throws JMException, ClassNotFoundException{
		Problem problem; // The problem to solve
		Algorithm algorithm; // The algorithm to use
		Operator crossover; // Crossover operator
		Operator mutation; // Mutation operator
		
		HashMap parameters; // Operator parameters
		//总共有8个不同的目标函数：{"DTLZ1","DTLZ2","DTLZ3","DTLZ4","SDTLZ1","SDTLZ2","WFG6","WFG7"}
		String[] probNames={"DTLZ3"};
		int[] objs={5};

		String probName = "DTLZ1";
		int nobj =3;
        double theta=5.0;

		long batchStart=System.currentTimeMillis();
		for(String pro :probNames) {
			for (int currentObj : objs) {

				probName = pro;
				nobj = currentObj;
				problem = Config.setProblem(probName, nobj);

				int[] divs = Config.setDivs(nobj);
				int maxGenerations = Config.setMaxGenerations(probName, nobj);

				algorithm = new ThetaDEA(problem);


				algorithm.setInputParameter("normalize", true);


				algorithm.setInputParameter("theta", theta);
				algorithm.setInputParameter("div1", divs[0]);
				algorithm.setInputParameter("div2", divs[1]);


				algorithm.setInputParameter("maxGenerations", maxGenerations);

				// Mutation and Crossover for Real codification
				parameters = new HashMap();
				parameters.put("probability", 1.0);
				parameters.put("distributionIndex", 30.0);
				crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover",
						parameters);

				parameters = new HashMap();
				parameters.put("probability", 1.0 / problem.getNumberOfVariables());
				parameters.put("distributionIndex", 20.0);
				mutation = MutationFactory.getMutationOperator("PolynomialMutation",
						parameters);

				// Add the operators to the algorithm
				algorithm.addOperator("crossover", crossover);
				algorithm.addOperator("mutation", mutation);
				StringBuilder builder = new StringBuilder();
				long starttime = System.currentTimeMillis();
				builder.append("开始时间戳：").append(starttime).append(" ms,");

				algorithm.execute();
				// wait for execute finish;
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
