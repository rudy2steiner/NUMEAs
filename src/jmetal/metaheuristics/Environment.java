package jmetal.metaheuristics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static jmetal.util.StringUtils.combine;


/**
 * Created by Rudy Steiner on 2017/4/10.
 */
public class Environment {
    public  static final String CAUCHY_ALGORITHM="jmetal.metaheuristics.CauchyThetaDEA.Cauchy";
    public  static final String GAUSSIAN_ALGORITHM="jmetal.metaheuristics.GuassianThetaDEA.Gaussian";
    public  static final String PLM_ALGORITHM="jmetal.metaheuristics.ThetaDEA.PLM";
    public  static final String NUM_ALGORITHM="jmetal.metaheuristics.NUMThetaDEA.NUM";
    public  static final String S2HVO_ALGORITHM="jmetal.metaheuristics.S2HVOThetaDEA.S2HVO";
    public  static final String DEFAULT_PF_ROOT="/resources/pf/";
    public static final String DEFAULT_PF_SUFFIX=".dat";
    public static final String DEFAULT_PF_OBJECTIVE_SOLUTION="src/resources/pf_solution/";
    public static final String DEFAULT_IDG_ROOT="src/resources/igd/";
    public static final String DEFAULT_LOG_FILE_ROOT="src/resources/logs/";
    public static final String DEFAULT_PERFORMANCE_NAME="IGD";
    public static final int  DEFAULT_INDEPENDENT_RUN=20;
    public static final int  DEFAULT_THREAD_POOL=4;

    public static  String DEFAULT_TODAY_LOG_FILE_NAME=null;
    public  static String DEFAULT_ALGORITHM_TO_RUN=PLM_ALGORITHM;
    public static String getTodayLogFileName(){
              if(DEFAULT_TODAY_LOG_FILE_NAME==null){
                  DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
                  DEFAULT_TODAY_LOG_FILE_NAME= combine(DEFAULT_LOG_FILE_ROOT,df.format(new Date()),"_log.txt");
              }
              return DEFAULT_TODAY_LOG_FILE_NAME;
    }
}
