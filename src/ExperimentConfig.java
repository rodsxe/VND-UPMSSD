import java.util.Random;

import algorithms.ABC;
import algorithms.Algorithm;
import algorithms.GA;
import algorithms.IGS;
import algorithms.vnd.DoEVNDFactory;
import instance.loader.LoaderInstance;
import instance.loader.LoaderLinInstance;

/**
 * Configuration file of an experimental run of the algorithms
 * @author Rodney Oliveira Marinho Diana
 *
 */
public class ExperimentConfig {
	
	/**The class used to load the instances.*/
	public static final LoaderInstance loader = new LoaderLinInstance();
	
	/**The algorithm used*/
	public static final Algorithm algoritmo = new GA();
	
	/**The execution type, it may be default, or calibration of the parameters.*/
	public static final int execution_type = ExperimentRunner.EXECUTION_TYPE_STANDARD;
	
	/**Number of experiments will be runned by instance.*/
	public static final int number_of_experiments_per_instance = 1;
		
	/**Type of stopping criteria used*/
	public static final int stopping_criteria = ExperimentRunner.STOPPING_CRITERIA_TIME;
	
	/**if used stopping criteria iterations without improvement, 
	 * this parameter represents the number of iterations without improvement.*/
	public static final int iterations_without_improvement = 50;
	
	/**if used stopping criteria of time or target, this parameter represents the maximum time expend in milliseconds.*/
	public static final int[] tempo_millis = {60000};
	
	/**The main directory, in which the other files and directories must be subdirectory of it.*/
	public static final String main_dir = "/home/rodney/workspace/vnd-upmssd/instances/";
	
	/**The list of directories of the instances processed in the experiment. This directories must be subdirectories of main_dir.*/
	public static final String[] dir_instances = {"TWT"}; 
	
	/**Type of the files to read in dir_instances.*/
	public static final String file_instance_type = ".txt";
		
	/**Directory where the complete solution will be saved. 
	 * This solution is saved with the complete structure of the scheduling, the order of each job and the machine in which their are assigned.*/
	public static final String dir_to_write_the_best_solutions = "sol";
	
	/**The file where is saved only the cpu time and the value of the object function to each instance.*/
	public static final String result_file_name = "result_file";
		
	/**if the stoppping criteria is target, it file contains the target fo each instance.*/
	public static final String file_of_target_value_of_instance = "target.txt";
	
	/**if the stoppping criteria is target, the percent of the target used.*/
	public static final double percentual_target = 1.00;
	
	/**True if write a solution in each improviment; False otherwise.*/
	public static final boolean is_to_write_partial_solutions = false;
	
	/**if used the execution_type to calibrate the parameters, it is the file which contains the of the design of experiments.*/
	public static final String parameter_calibration_design_conf = "taguchi.conf"; 
	
	/**Name of each parameter used in the calibration experiment*/
	public static final String[] levels_parameters = DoEVNDFactory.levels_parameters_l_18_design; 
	
	/**The random variable used by the experiments.*/
	public static final Random rand = new Random();
	
}


