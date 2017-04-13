import jmetal.core.Algorithm;
import jmetal.util.JMException;

/**
 * Created by Rudy Steiner on 2017/4/9.
 */
public class AlgorithmRunner extends Thread{
    private Algorithm algorithm;
    public AlgorithmRunner(String name, Algorithm algorithm){
        this.setName(name);
        this.algorithm=algorithm;
    }
    @Override
    public void run() {
        try {
            algorithm.execute();
        }catch (ClassNotFoundException e){
             e.printStackTrace();
        }catch (JMException e){
             e.printStackTrace();
        }
    }
}
