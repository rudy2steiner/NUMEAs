import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.metaheuristics.Config;
import jmetal.metaheuristics.Environment;
import jmetal.metaheuristics.NUMThetaDEA.NUM;
import jmetal.metaheuristics.S2HVOThetaDEA.S2HVO;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.CauchyMutation;
import jmetal.operators.mutation.GuassianMutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.Configuration;
import jmetal.util.JMException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import static jmetal.util.StringUtils.combine;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException,JMException {
        Problem problem; // The problem to solve
        Algorithm algorithm; // The algorithm to use
        ExecutorService executors;
        String pfRoot=Environment.DEFAULT_PF_ROOT;
        String pfSuffix=Environment.DEFAULT_PF_SUFFIX;
        int    maxRun=Environment.DEFAULT_INDEPENDENT_RUN;
        String probName;
        int   nobj;
        //总共有8个不同的目标函数：{"DTLZ1","DTLZ2","DTLZ3","DTLZ4","SDTLZ1","SDTLZ2","WFG6","WFG7"}
        String[] probNames={"DTLZ1"};
        int[]   objs={3};
        try {
            FileHandler fh= new FileHandler(Environment.getTodayLogFileName(),true);
            fh.setFormatter(new SimpleFormatter());
            Configuration.logger_.addHandler(fh);
        }catch (IOException e){
            e.printStackTrace();
        }
        executors= Executors.newFixedThreadPool(Environment.DEFAULT_THREAD_POOL);

        long batchStart=System.currentTimeMillis();
        try {
        for(String pro :probNames) {
            for (int currentObj : objs) {
                probName = pro;
                nobj = currentObj;
                problem = Config.setProblem(probName,nobj);
                int[] divs = Config.setDivs(nobj);
                int maxGenerations = Config.setMaxGenerations(probName, nobj);
                for(int i=1;i<=maxRun;i++) {
                    algorithm=initByAlgName(Environment.DEFAULT_ALGORITHM_TO_RUN,problem, divs, maxGenerations, pfRoot, pfSuffix, i);
                    //algorithm=initNUM(problem, divs, maxGenerations, pfRoot, pfSuffix, i);
                    //algorithm = initS2HVO(problem, divs, maxGenerations, pfRoot, pfSuffix, i);
                    executors.execute(new AlgorithmRunner("##"+i,algorithm));
                    //algorithm.execute();
                    //executors.submit(new AlgorithmRunner("##"+i,algorithm));
                }
            }
        }
            executors.shutdown();
            executors.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }catch (InterruptedException ie){
            ie.printStackTrace();
        }finally {
            if(!executors.isTerminated()) executors.shutdown();
        }
        long batchEnd=System.currentTimeMillis();
        StringBuilder builder=new StringBuilder();
        builder.append("----------------------\r\n");
        builder.append("problems:").append(Arrays.toString(probNames)).append("\r\n");
        builder.append("objective:").append(Arrays.toString(objs)).append("\r\n");
        builder.append("开始时间戳：").append(batchStart).append("ms,");
        builder.append("结束时间戳：").append(batchEnd).append(" ms").append("\r\n");
        builder.append("持续时间：").append(batchEnd-batchStart).append("ms").append("\r\n--------------------");
        Configuration.logger_.info(builder.toString());
    }
    private static Algorithm initS2HVO(Problem problem, int[] divs, int maxGenerations,
                                           String pfRoot,String pfSuffix,int run) throws JMException {
        Algorithm algorithm;
        HashMap parameters;
        Operator SBXCrossover;
        Operator DECrossover;
        Operator polyMutation;
        Operator nonuniformMutation;
        Operator DESelection;
        Operator BinarySelection;
        algorithm = new S2HVO(problem,pfRoot,pfSuffix,run);
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
        parameters.put("CR", 0.1);   //CR pool
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
        //System.out.println("perturbation:" + (Double) parameters.get("perturbation"));
        Configuration.logger_.info(combine("perturbation:" ,((Double)parameters.get("perturbation")).toString()));
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
        algorithm.addOperator("NUM", nonuniformMutation);
        algorithm.addOperator("BS", BinarySelection);
        algorithm.addOperator("DES", DESelection);
        return algorithm;
    }
    private static Algorithm initNUM(Problem problem, int[] divs, int maxGenerations,
                                       String pfRoot,String pfSuffix,int run) throws JMException {
        Algorithm algorithm;
        HashMap parameters;
        Operator crossover;
        Operator polyMutation;
        Operator nonuniformMutation;
        Operator cauchyMutation;
        Operator gaussianMutation;
        Operator BinarySelection;
        Operator DESelection;
        algorithm = new NUM(problem,pfRoot,pfSuffix,run);
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
        parameters.put("perturbation",6.0);

        System.out.println("perturbation:" + (Double) parameters.get("perturbation"));
        parameters.put("maxIterations", algorithm.getInputParameter("maxGenerations"));
        nonuniformMutation = MutationFactory.getMutationOperator("NonUniformMutation",
                parameters);
        //cauchy mutation
        parameters=new HashMap();
        parameters.put("probability", 1.0 / problem.getNumberOfVariables());
        parameters.put("gamma",0.05);
        cauchyMutation=new CauchyMutation(parameters);

        //guassian mutation
        parameters = new HashMap();
        parameters.put("probability", 1.0 / problem.getNumberOfVariables());
        parameters.put("sigmma",0.1);
        gaussianMutation=new GuassianMutation(parameters);

        parameters = new HashMap();
        BinarySelection = SelectionFactory.getSelectionOperator("BinaryTournament3",
                parameters);

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("PM", polyMutation);
        algorithm.addOperator("NUM", nonuniformMutation);
        algorithm.addOperator("CAUCHY",cauchyMutation);
        algorithm.addOperator("GUASSIAN",gaussianMutation);
        algorithm.addOperator("BS", BinarySelection);
        return algorithm;
    }
    public static Algorithm initByAlgName(String algorithmName,Problem problem,int[] divs, int maxGenerations,
                              String pfRoot,String pfSuffix,int run) throws JMException{
        Algorithm algorithm=null;
        HashMap parameters;
        Operator SBXCrossover;
        Operator DECrossover;
        Operator polyMutation;
        Operator nonuniformMutation;
        Operator cauchyMutation;
        Operator gaussianMutation;
        Operator DESelection;
        Operator BinarySelection;
        try {
            //反射实例化对应的算法
            Class cs = Class.forName(algorithmName);
            Constructor cr=cs.getConstructor(Problem.class,String.class,String.class,int.class);
            algorithm=(Algorithm)cr.newInstance(problem,pfRoot,pfSuffix,run);
            algorithm.setInputParameter("normalize", true);
            algorithm.setInputParameter("theta", 5.0);
            algorithm.setInputParameter("div1", divs[0]);
            algorithm.setInputParameter("div2", divs[1]);
            algorithm.setInputParameter("maxGenerations", maxGenerations);

            // Mutation and Crossover for Real codification
            // simulated binary crossover
            parameters = new HashMap();
            parameters.put("probability", 1.0);
            parameters.put("distributionIndex", 30.0);
            SBXCrossover = CrossoverFactory.getCrossoverOperator("SBXCrossover",
                    parameters);
            //DE crossover
            parameters = new HashMap();
            parameters.put("F", 0.5);
            parameters.put("CR", 0.1);   //CR pool
            parameters.put("DE_VARIANT", "rand/1/bin");
            DECrossover = CrossoverFactory.getCrossoverOperator("DifferentialEvolutionCrossover",
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
            parameters.put("perturbation", 6.0); // 2SHVO b=4 is better,but 6 for non-uniform mutation
            Configuration.logger_.info("perturbation:" + (Double) parameters.get("perturbation"));

            parameters.put("maxIterations", algorithm.getInputParameter("maxGenerations"));
            nonuniformMutation = MutationFactory.getMutationOperator("NonUniformMutation",
                    parameters);
            //cauchy mutation
            parameters=new HashMap();
            parameters.put("probability", 1.0 / problem.getNumberOfVariables());
            parameters.put("gamma",0.05);
            cauchyMutation=new CauchyMutation(parameters);
            //guassian mutation
            parameters = new HashMap();
            parameters.put("probability", 1.0 / problem.getNumberOfVariables());
            parameters.put("sigmma",0.1);
            gaussianMutation=new GuassianMutation(parameters);

            parameters = new HashMap();
            BinarySelection = SelectionFactory.getSelectionOperator("BinaryTournament3",
                    parameters);
            parameters = null;
            DESelection = SelectionFactory.getSelectionOperator("DifferentialEvolutionSelection",
                    parameters);
            // Add the operators to the algorithm
            algorithm.addOperator("SBX", SBXCrossover);
            algorithm.addOperator("DE", DECrossover);
            algorithm.addOperator("PLM", polyMutation);
            algorithm.addOperator("NUM", nonuniformMutation);
            algorithm.addOperator("CAUCHY",cauchyMutation);
            algorithm.addOperator("GAUSSIAN",gaussianMutation);
            algorithm.addOperator("BS", BinarySelection);
            algorithm.addOperator("DS", DESelection);
        }catch (ClassNotFoundException e){
                   e.printStackTrace();
        }catch (NoSuchMethodException e){
            e.printStackTrace();
        }catch(InvocationTargetException e){
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }catch (InstantiationException e){
            e.printStackTrace();
        }
        return algorithm;
    }
}
