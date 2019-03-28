import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import algorithms.Algorithm;
import exception.BestObjectFunctionException;
import instance.loader.TargetSol;
import instance.loader.LoaderInstance;
import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import operators.function.evaluator.ObjectFunctionEvaluator;

/**
 * Class resnponsable to execute an experiment given a configuration in ExperimentConfig class
 * @author Rodney Oliveira Marinho Diana
 *
 */
public class ExperimentRunner {
	
	public static final int STOPPING_CRITERIA_IT_WITHOUT_IMPROVEMENT = 1;
	public static final int STOPPING_CRITERIA_TIME = 2;
	public static final int STOPPING_CRITERIA_TARGET = 3;
	
	public static final int EXECUTION_TYPE_STANDARD = 1;
	public static final int EXECUTION_TYPE_DESIGN = 2;
	public static final int EXECUTION_TYPE_COMPONENT_WISE = 3;
	public static final int EXECUTION_TYPE_DESIGN_BY_INSTANCE = 4;
	
	private int[][] processTimes;
	private int[][][] setupTimes;
	
	private HashMap<String, TargetSol> targetSolutions;
		
	private Random rand;
	
	private Algorithm algorithm;
	private LoaderInstance loader;
	private ObjectFunctionEvaluator evaluator;	
	
	/*Paths*/
	private String solPath;
	private String mainDir;
	private String writeSolDir;
	private String resultArqName;
	private String[] fileDir;
	private String FILE_TYPE;
	private boolean isToWritePartialSolutions;
	
	private String[] levels;
	private String design_conf;
	
	private int STOPPING_CRITERIA;
	private int EXECUTION_TYPE;
	private int RUNS_NUMBER;
	private double PERCENTUAL_TARGET;
	private int IT_MAX;
	private int[] millis;
	
	
	public ExperimentRunner() {
		
		super();
		this.algorithm = ExperimentConfig.algoritmo;
		this.loader = ExperimentConfig.loader;
		this.rand = ExperimentConfig.rand;
		this.STOPPING_CRITERIA = ExperimentConfig.stopping_criteria;
		this.mainDir = ExperimentConfig.main_dir;
		this.solPath = ExperimentConfig.main_dir + ExperimentConfig.file_of_target_value_of_instance;
		this.writeSolDir = ExperimentConfig.dir_to_write_the_best_solutions;
		this.resultArqName = ExperimentConfig.result_file_name;
		this.fileDir = ExperimentConfig.dir_instances;
		this.FILE_TYPE = ExperimentConfig.file_instance_type;
		this.RUNS_NUMBER = ExperimentConfig.number_of_experiments_per_instance;
		this.PERCENTUAL_TARGET = ExperimentConfig.percentual_target;
		this.IT_MAX = ExperimentConfig.iterations_without_improvement;
		this.millis = ExperimentConfig.tempo_millis;
		this.levels = ExperimentConfig.levels_parameters;
		this.design_conf = ExperimentConfig.parameter_calibration_design_conf;
		this.EXECUTION_TYPE = ExperimentConfig.execution_type;
		this.isToWritePartialSolutions = ExperimentConfig.is_to_write_partial_solutions;
		
	}
	
	public ExperimentRunner(Random rand) {
		
		this();
		this.rand = rand;
		
	}

	private void loadArq(String path, HashMap<String, String> params){
		
		try{
			
			processTimes = loader.loadProcessTime(path);
			setupTimes = loader.loadSetupTime(path);
			targetSolutions = loader.loadTargetSolutions(solPath);
			SchedulingInstancesConfig config = new SchedulingInstancesConfig(setupTimes, processTimes, loader.loadDueDate(path));
			this.evaluator = loader.getEvaluator(path);
			this.evaluator.setSchedulingConfig(config);
			algorithm.setInstanceValues(config,
										evaluator,
										rand, 
										params);
			
        }
        catch(FileNotFoundException e){
        	e.printStackTrace();
            System.out.println("Arquivo não encontrado.");
        }
        catch(IOException o){
        	o.printStackTrace();
            System.out.println("ERRO");
        }
	}
	
	private long getProcessTime(int milli) {
		
		return milli;
		
	}
	
	private boolean stoppingCriteria(int itWithoutImprovement, long processTime, long totalTime, long target, float cost){
		
		if(STOPPING_CRITERIA == STOPPING_CRITERIA_TIME)
			return (totalTime > processTime)?false:true;
		else if(STOPPING_CRITERIA == STOPPING_CRITERIA_TARGET)
			return (cost > target && totalTime > processTime)?false:true;
		else if(STOPPING_CRITERIA == STOPPING_CRITERIA_IT_WITHOUT_IMPROVEMENT)
			return (itWithoutImprovement < IT_MAX)?false:true;
		return true;
		
	}
	
	public Long getTarget(String instance){
		
		String instanceName = instance.substring(0, instance.indexOf(FILE_TYPE));
		if (targetSolutions == null || !targetSolutions.containsKey(instanceName)) return new Long(0);
		TargetSol bestSol = targetSolutions.get(instanceName);
		return (long)(bestSol.getBestSol() * PERCENTUAL_TARGET);
	}
	
	private List<Solution> sortObjFunction(List<Solution> Ab){
		Collections.sort(Ab, new Comparator<Solution>() {
			@Override
			public int compare(Solution s1, Solution s2) {
				
				if(s1.getObjectiveFunction() < s2.getObjectiveFunction())return -1;
				else if(s1.getObjectiveFunction() > s2.getObjectiveFunction())return 1;
				else return 0;
				
			}
		});
		return Ab;
	}
	
	public Solution getBestValidSolution(List<Solution> solutions){
		
		solutions = sortObjFunction(solutions);
		for (Solution solution : solutions) {
			if(evaluator.isValidSolution(solution)) return solution;
		}
		
		return null;
		
	}
	
	public Solution execOneTime(String arqPath, String instance, PrintWriter printWriter, HashMap<String, String> params) throws BestObjectFunctionException, IOException, ParseException{
		
		this.loadArq(arqPath, params);
		long totalTime = getProcessTime(millis[millis.length - 1]);
		
		float lastChangeMakespan = Float.MAX_VALUE;
		long target = (instance != null)? getTarget(instance): 0;
		int itWithoutImprovement = 0;
		long initialTime = System.currentTimeMillis();
		long processTime = System.currentTimeMillis() - initialTime;
		long targetTime = -1;
		
		List<Solution> solutions = algorithm.loadInitialSolutions();
		
		if (printWriter != null && isToWritePartialSolutions) writeResultInstance(printWriter, instance, algorithm.getBest().getObjectiveFunction(), System.currentTimeMillis() - initialTime);
				
		while(!stoppingCriteria(itWithoutImprovement, processTime, totalTime, target, lastChangeMakespan)){
			
			solutions = algorithm.executeOneIteration(solutions);
			
			Solution bestSol = getBestValidSolution(solutions);
			Solution bestSol2 = algorithm.getBest();
			if (bestSol == null && bestSol2 != null) bestSol = bestSol2;
			else if(bestSol == null && bestSol2 == null) bestSol = null;
			else if(bestSol2 != null && bestSol!= null) bestSol = (bestSol.getObjectiveFunction() < bestSol2.getObjectiveFunction())?bestSol:bestSol2;
			
			processTime = System.currentTimeMillis() - initialTime;
			
			if(bestSol == null || lastChangeMakespan <= bestSol.getObjectiveFunction()){
				
				itWithoutImprovement++;
				if (bestSol != null && bestSol.getObjectiveFunction() == 0) break;
				
			}
			else{
				
				lastChangeMakespan = bestSol.getObjectiveFunction();
				itWithoutImprovement = 0;
				if (printWriter != null && isToWritePartialSolutions) 
					writeResultInstance(printWriter, instance, bestSol.getObjectiveFunction(), processTime);
				
			}		
						
			if (bestSol != null && targetTime < 0 && bestSol.getObjectiveFunction() < target) targetTime = processTime;
	
		}
		
		processTime = System.currentTimeMillis() - initialTime;
		if (targetTime < 0) targetTime = Integer.MAX_VALUE;
		
		solutions = algorithm.updateMemory(solutions);
		
		if (solutions.isEmpty()) return null;
		
		Solution result = solutions.get(0);
		
		if(lastChangeMakespan != result.getObjectiveFunction()) 
			throw new BestObjectFunctionException("Bug in algorithm, best object function return in memory (updateMemory) is not the same showed in rum time. Run Time:"+ lastChangeMakespan + "; Memory:" +  result.getObjectiveFunction());
		
		
		if (printWriter != null) 
				
		result.setTime(processTime);
		
		if (printWriter != null){
			
			if(isToWritePartialSolutions)writeResultInstance(printWriter, instance, algorithm.getBest().getObjectiveFunction(), processTime);
			else writeResultInstance(instance, printWriter, solutions, targetTime);
		
		}
		
		return result;
	}
	
	public void run()throws Exception{
		
		switch(EXECUTION_TYPE) {
			
		case EXECUTION_TYPE_DESIGN:
			
			this.runDesignExp();
			break;
			
		case EXECUTION_TYPE_COMPONENT_WISE:
			
			this.runComponentWise();
			break;
					
		case EXECUTION_TYPE_DESIGN_BY_INSTANCE:
			
			runDesignExpByInstance();
			break;

		case EXECUTION_TYPE_STANDARD:
			
			runStandard();
			break;

		}
		
	}
	
	public void runStandard() throws Exception{
		
		for (int j = 0; j < fileDir.length; j++) {
			String dirName = mainDir + fileDir[j];
			
			try {
				
				FileWriter fileWriter = new FileWriter(mainDir + resultArqName + "_" + fileDir[j] + ".txt", true);
				PrintWriter printWriter = new PrintWriter(fileWriter);
				
				ArrayList<String> instances = loadInstancesNames(dirName); 
				for (String instance : instances) {
					
					float melhorCusto = 100000000;
					float media = 0;
					
					Solution solucao = null;
					int it = 0;
					
					while(it < RUNS_NUMBER){
						
						Solution s = execOneTime(dirName + "/" + instance, fileDir[j] + "_" +instance, printWriter, null);
						float custo = s.getObjectiveFunction();
						
						if(custo < melhorCusto){
							
							melhorCusto = custo;
							solucao = s;
						
						}
						media = media + custo;
						
						it ++;
					}
				
					if(STOPPING_CRITERIA != STOPPING_CRITERIA_TARGET){
						SolutionWriter vs = new SolutionWriter(solucao, evaluator, processTimes, setupTimes);
						vs.writeSolInArq(mainDir + "/" + writeSolDir + "/" + fileDir[j] + "/" + instance, instance, FILE_TYPE);
					}
				
				}
				
				printWriter.close();
				fileWriter.close();
				
			} catch (IOException e) { e.printStackTrace();}
		
		}
		
	}
	
	public void runComponentWise()throws Exception{
		
		int it, round, n_leves = levels.length;
		String line, componentKey;
		String dirName = mainDir + fileDir[0];
		
		ArrayList<String> instances;
		BufferedReader in;
		FileWriter fileWriter;
		PrintWriter printWriter;
		StringTokenizer st;
		
		HashMap<String, Float> bestByInstance = new HashMap<String, Float>();
		HashMap<String, Float> worstByInstance = new HashMap<String, Float>();
		Float theBest, worst;
		HashMap<String, Float> executionResultByInstance = new HashMap<String, Float>();
		
		HashMap<String, String> component, selectedComponent;
		List<HashMap<String, String>> listOfComponents = new ArrayList<HashMap<String, String>>();
				
		try {
			
			fileWriter = new FileWriter(mainDir + resultArqName + "_" + fileDir[0] + ".txt", true);
			printWriter = new PrintWriter(fileWriter);
			instances = loadInstancesNames(dirName); 
			
			in = new BufferedReader(new FileReader(mainDir + design_conf));
			
			while ((line = in.readLine()) != null) {
				
				st = new StringTokenizer(line.trim(), " ");
				component = new HashMap<String, String>();
				
				componentKey = st.nextToken().trim();
				
				component.put("COMPONENT_KEY", componentKey);
				
				for (int i = 0; i < n_leves; i++) component.put(levels[i], st.nextToken().trim());
				
				listOfComponents.add(component);
			
			}					
			
			round = 1;
			
			while (!listOfComponents.isEmpty()) {
			
				for (int i = 0; i < listOfComponents.size(); i++) {
					
					component = listOfComponents.get(i);
					componentKey = component.get("COMPONENT_KEY");
					
					for (String instance : instances) {
						
						if (bestByInstance.containsKey(instance)) theBest = bestByInstance.get(instance);
						else theBest = Float.MAX_VALUE;
						
						if (worstByInstance.containsKey(instance)) worst = worstByInstance.get(instance);
						else worst = Float.MIN_VALUE;
						
						it = 0;
											
						while (it < RUNS_NUMBER) {
							
							Solution s = execOneTime(dirName + "/" + instance, instance, null, component);
							if (s.setObjectiveFunction() < theBest) theBest = s.getObjectiveFunction();
							if (s.getObjectiveFunction() > worst) worst = s.getObjectiveFunction();
							
							executionResultByInstance.put(componentKey + "_" + instance + "_" + it, s.getObjectiveFunction());
							
							printWriter.println(round + ";" + componentKey + ";" + instance + ";" + s.getObjectiveFunction());
							printWriter.flush();
							it++;
							
						}
						
						bestByInstance.put(instance, theBest);
						worstByInstance.put(instance, worst);
					
					}
					
				}
				
				float value, sum, rpd, rpdSelected = Float.MAX_VALUE;
				int N;
				int componenetSelected = -1;
				
				for (int i = 0; i < listOfComponents.size(); i++) {
					
					component = listOfComponents.get(i);
					componentKey = component.get("COMPONENT_KEY");
					N = 0; sum = 0.f;
					
					for (String instance : instances) {
						
						theBest = bestByInstance.get(instance);
						worst = worstByInstance.get(instance);
						
						it = 0;
						
						while (it < RUNS_NUMBER) {
							
							value = executionResultByInstance.get(componentKey + "_" + instance + "_" + it);
							sum += (value - theBest)/(worst - theBest);
							it++; N++;
							
						}
						
					}
					
					rpd = sum/N;
					
					if (rpd < rpdSelected) {
						
						rpdSelected = rpd;
						componenetSelected = i;
						
					}
					
				}
				
				selectedComponent = listOfComponents.remove(componenetSelected);
				
				for (int i = 0; i < listOfComponents.size(); i++) {
					
					component = listOfComponents.get(i);
					Set<String> levels = selectedComponent.keySet();
					
					for (String level : levels) {
						
						if (!level.equals("COMPONENT_KEY")) 
							
							if (!selectedComponent.get(level).equals("1")) 
								
								component.put(level, selectedComponent.get(level));
											
					}
								
				}
				
				round++;
			
			}
			
			printWriter.close();
			fileWriter.close();
			in.close();
					
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	public void runDesignExpByInstance()throws Exception{
		
		int n_leves = levels.length;
		String line;
		StringTokenizer st;
		String dirName = mainDir + fileDir[0];
		BufferedReader in;
		ArrayList<String> instances;
		FileWriter fileWriter;
		PrintWriter printWriter;
		HashMap<String, String> params = new HashMap<String, String>();
		
		try {
						
			instances = loadInstancesNames(dirName); 
			
			for (String instance : instances) {
				
				fileWriter = new FileWriter(mainDir + resultArqName + "_" + instance + "_" + fileDir[0] + ".txt", true);
				printWriter = new PrintWriter(fileWriter);
				
				in = new BufferedReader(new FileReader(mainDir + design_conf));
							
				while ((line = in.readLine()) != null) {
					
					st = new StringTokenizer(line.trim(), " ");
					String number = st.nextToken().trim();
					st.nextToken();
					st.nextToken();
					st.nextToken();
					//instance = instances.get(new Integer(st.nextToken().trim()) - 1);
					
					for (int i = 0; i < n_leves; i++) params.put(levels[i], st.nextToken().trim());
					
					int it = 0;
					while(it < RUNS_NUMBER){
						
						Solution s = execOneTime(dirName + "/" + instance, instance, null, params);
						printWriter.println(number + ";" + instance + ";" + s.setObjectiveFunction());
						printWriter.flush();
						it++;
						
					}
					
				}
				
				printWriter.close();
				fileWriter.close();
				in.close();
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	public void runDesignExp()throws Exception{
		
		int n_leves = levels.length;
		String line, instance;
		StringTokenizer st;
		String dirName = mainDir + fileDir[0];
		BufferedReader in;
		ArrayList<String> instances;
		FileWriter fileWriter;
		PrintWriter printWriter;
		HashMap<String, String> params = new HashMap<String, String>();
		
		try {
			
			fileWriter = new FileWriter(mainDir + resultArqName + "_" + fileDir[0] + ".txt", true);
			printWriter = new PrintWriter(fileWriter);
			//instances = loadInstancesNames(dirName); 
			
			instances = new ArrayList<String>();
			instances.add("10X100_13.txt");
			instances.add("10X100_10.txt");
			instances.add("10X100_8.txt");
			instances.add("10X100_11.txt");
			instances.add("10X100_5.txt");
			instances.add("10X100_12.txt");
			instances.add("10X100_4.txt");
			instances.add("10X100_2.txt");
			instances.add("10X100_9.txt");
			instances.add("10X100_20.txt");
			instances.add("10X100_14.txt");
			instances.add("10X100_17.txt");
			instances.add("10X100_6.txt");
			instances.add("10X100_19.txt");
			instances.add("10X100_7.txt");
			instances.add("10X100_1.txt");
			
			
			in = new BufferedReader(new FileReader(mainDir + design_conf));
						
			while ((line = in.readLine()) != null) {
				
				st = new StringTokenizer(line.trim(), " ");
				String number = st.nextToken().trim();
				st.nextToken().trim();
				st.nextToken().trim();
				
				instance = instances.get(new Integer(st.nextToken().trim()) - 1);
				
				for (int i = 0; i < n_leves; i++) params.put(levels[i], st.nextToken().trim());
				
				int it = 0;
				while(it < RUNS_NUMBER){
					
					Solution s = execOneTime(dirName + "/" + instance, instance, null, params);
					printWriter.println(number + ";" + instance + ";" + s.setObjectiveFunction());
					printWriter.flush();
					it++;
					
				}
			}
			
			printWriter.close();
			fileWriter.close();
			in.close();
					
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	private void writeResultInstance(String instance, PrintWriter printWriter,	List<Solution> memory, long tempoAlvo) throws ParseException {
		
		String instanceName = instance.substring(0, instance.indexOf(FILE_TYPE));
		Solution solution = memory.get(0);
		
		printWriter.print(instanceName +";" + solution.getNumberOfJobs()+ ";"+ solution.getNumberOfMachines() + ";");
		printWriter.print(";" + solution.getTime());
		printWriter.print(";" + tempoAlvo);
		printWriter.print(";" + solution.getObjectiveFunction());
		printWriter.println();		
		printWriter.flush();
		
	}
	
	private void writeResultInstance(PrintWriter printWriter, String instance, float cost, long time) throws ParseException {
		
		String instanceName = instance.substring(0, instance.indexOf(FILE_TYPE));
		printWriter.println(instanceName + ";" + time + ";" + cost);
		printWriter.flush();
		
	}

	private ArrayList<String> loadInstancesNames(String dirName) {
		ArrayList<String> instances = new ArrayList<String>();
		File dir = new File(dirName);  
		
		String[] children = dir.list();  
		if (children == null);
		else {  
		    for (int i=0; i < children.length; i++) {  
		        // Get filename of file or directory  
		        String filename = children[i]; 
		        if(!filename.contains(FILE_TYPE)) continue;
		        instances.add(filename);
		    }  
		}
		return instances;
	}
	
	public static void main(String[] args) {

		if (args.length >= 4){
			
			Random rand = new Random(new Long(args[2]));
			String instance = args[3];
			HashMap<String, String> params = new HashMap<String, String>();
			
			for (int i = 4; i < args.length; i+=2)
				params.put(args[i], args[i + 1]);
						
			ExperimentRunner experiment = new ExperimentRunner(rand);
			try {
				
				Solution s = experiment.execOneTime(instance, null, null, params);
				System.out.println("Best " + ((s == null)? Float.MAX_VALUE : s.getObjectiveFunction()) + "0");
				
			} catch (Exception e) {e.printStackTrace();}
			
		} else {
			
			ExperimentRunner experiment = new ExperimentRunner();
			try {
				experiment.run();
			} catch (Exception e) { e.printStackTrace(); }
			
		}
	}
	
	
}
