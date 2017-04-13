package jmetal.metaheuristics._2SHVOThetaDEA;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.Config;
import jmetal.metaheuristics.Environment;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.Configuration;
import jmetal.util.JMException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class S2HVOThetaDEA_main {
	public static void main(String args[]) throws JMException, ClassNotFoundException{
		Problem problem; // The problem to solve
		Algorithm algorithm; // The algorithm to use
		Operator SBXCrossover; // Crossover operator

		Operator DECrossover; // Crossover operator
		Operator polyMutation; // Mutation operator
		Operator nonuniformMutation; // Mutation operator

		Operator DESelection; //Selection operator
		Operator BinarySelection;
		HashMap parameters; // Operator parameters

		//总共有8个不同的目标函数：{"DTLZ1","DTLZ2","DTLZ3","DTLZ4","SDTLZ1","SDTLZ2","WFG6","WFG7"}
		String[] probNames={"DTLZ1"};
		int[] objs={3,5,8,10,15};

		String probName = "DTLZ1";
		int nobj =3;
		try {
			FileHandler fh= new FileHandler(Environment.DEFAULT_LOG_FILE_NAME,true);
			fh.setFormatter(new SimpleFormatter());
			Configuration.logger_.addHandler(fh);
		}catch (IOException e){
			e.printStackTrace();
		}
		long batchStart=System.currentTimeMillis();
		for(String pro :probNames) {
			for (int currentObj : objs) {
				probName = pro;
				nobj = currentObj;
				problem = Config.setProblem(probName, nobj);

				int[] divs = Config.setDivs(nobj);
				int maxGenerations = Config.setMaxGenerations(probName, nobj);
				algorithm = new S2HVOThetaDEA(problem);
				algorithm.setInputParameter("normalize", true);
				algorithm.setInputParameter("theta", 5.0);
				algorithm.setInputParameter("div1", divs[0]);
				algorithm.setInputParameter("div2", divs[1]);
				algorithm.setInputParameter("maxGenerations", maxGenerations);
				// Mutation and Crossover for Real codification
				parameters = new HashMap();
				parameters.put("probability", 1.0);
				parameters.put("distributionIndex", 30.0);
				SBXCrossover = CrossoverFactory.getCrossoverOperator("SBXCrossover",
						parameters);
				// Mutation and Crossover for Real codification
				parameters = new HashMap();
				parameters.put("F", 0.5);
				parameters.put("CR", 0.1);
				parameters.put("DE_VARIANT", "rand/1/bin");
				DECrossover = CrossoverFactory.getCrossoverOperator("DifferentialEvolutionCrossover",
						parameters);
				parameters = new HashMap();
				parameters.put("probability", 1.0 / problem.getNumberOfVariables());
				parameters.put("distributionIndex", 20.0);
				polyMutation = MutationFactory.getMutationOperator("PolynomialMutation",
						parameters);
				parameters = new HashMap();
				parameters.put("probability", 1.0 / problem.getNumberOfVariables());
				parameters.put("perturbation", 4.0);
				System.out.println("perturbation:" + (Double) parameters.get("perturbation"));
				parameters.put("maxIterations", algorithm.getInputParameter("maxGenerations"));
				nonuniformMutation = MutationFactory.getMutationOperator("NonUniformMutation",
						parameters);
				parameters = null;
				DESelection = SelectionFactory.getSelectionOperator("DifferentialEvolutionSelection",
						parameters);
				parameters = new HashMap();
				BinarySelection = SelectionFactory.getSelectionOperator("BinaryTournament3",
						parameters);
				// Add the operators to the algorithm
				algorithm.addOperator("DE", DECrossover);
				algorithm.addOperator("SBX", SBXCrossover);
				algorithm.addOperator("PM", polyMutation);
				algorithm.addOperator("NUMNSGAIII", nonuniformMutation);
				algorithm.addOperator("BS", BinarySelection);
				algorithm.addOperator("DES", DESelection);
				StringBuilder builder = new StringBuilder();
				long starttime = System.currentTimeMillis();
				builder.append("开始时间戳：").append(starttime).append(" ms,");
				algorithm.execute();
				long endtime = System.currentTimeMillis();
				builder.append("结束时间戳：").append(endtime).append(" ms").append("\r\n");
				builder.append("持续时间：").append(endtime - starttime).append(" ms").append("\r\n--------------------");
				Configuration.logger_.info(builder.toString());
				//System.out.println(builder);

			}
		}
		long batchEnd=System.currentTimeMillis();
		StringBuilder builder=new StringBuilder();
		builder.append("----------------------\r\n");
		builder.append("problems:").append(Arrays.toString(probNames)).append("\r\n");
		builder.append("objective:").append(Arrays.toString(objs)).append("\r\n");
		builder.append("开始时间戳：").append(batchStart).append(" ms,");
		builder.append("结束时间戳：").append(batchEnd).append(" ms").append("\r\n");
		builder.append("持续时间：").append(batchEnd-batchStart).append(" ms").append("\r\n--------------------");
		Configuration.logger_.info(builder.toString());
		//System.out.println(builder);

	}
}
