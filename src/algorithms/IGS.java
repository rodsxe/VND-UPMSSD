package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import algorithms.vnd.BasicVND;
import algorithms.vnd.CyclicVND;
import algorithms.vnd.DoEVNDFactory;
import algorithms.vnd.VND;
import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import operators.function.evaluator.ObjectFunctionEvaluator;
import operators.localsearch.ABCExchangeLocalSearch;
import operators.localsearch.ExternalInsertionLocalSearch;
import operators.localsearch.InternalInsertionLocalSearch;
import operators.localsearch.ExternalSwapLocalSearch;
import operators.localsearch.InternalSwapLocalSearch;
import operators.localsearch.IGSInternalLocalSearch;
import operators.localsearch.IGSSwapLocalSearch;
import operators.localsearch.LocalSearch;


/**
 * Iterated Greedy Algorithm proposed in:
 * 
 * Lin, S.-W., Lu, C.-C., & Ying, K.-C. (2011). Minimization of total 
 * tardiness on unrelated parallel machines with sequence- and machine 
 * dependent setup times under due date constraints. 
 * The International Journal of Advanced Manufacturing Technology, 53, 353 – 361. 
 * DOI:10.1007/s00170-010-2824-y.
 * 
 * @author Rodney Oliveira Marinho Diana
 *
 */
public class IGS extends BaseAlgorithm {
	
	private Solution best;
	
	private Random random;
	private RandomSolutionGenerator solGenerator = new RandomSolutionGenerator();
	
	private ATCSR_Rm atcsr_Rm;
	
	private SchedulingInstancesConfig config;
	private ObjectFunctionEvaluator evaluator;
	
	private LocalSearch ls3;
	
	private float BETA = 0.9f;
	private float TEMPERATURE = 0.9f;
	private int IT_MAX = 200;
	private int IGS_D = 3;
	
	long tempoInicial;
	
	public float getTemperature(SchedulingInstancesConfig config){
		
		float pTotal = 0;
		int[][] processTime = config.processTime;
		float N = config.getNumberOfJobs();
		float M = config.getNumberOfMachines();
		
		for (int i = 0; i < M; i++) 
			
			for (int j = 0; j < N; j++)  pTotal += processTime[i][j];
				
		
		return pTotal/(N * M * 10);
		
	}
	
	@Override
	public void setInstanceValues(SchedulingInstancesConfig config, ObjectFunctionEvaluator evaluator, Random rand,
			HashMap<String, String> params) {
		
		atcsr_Rm = new ATCSR_Rm(evaluator);
		best = null;
		this.random = rand;
		this.config = config;
		this.evaluator = evaluator;
		
		
		this.TEMPERATURE = BETA * (getTemperature(config));
			
		float N = config.getNumberOfJobs();
		float M = config.getNumberOfMachines();
		
		IT_MAX = (int)((N+M-1));
		
		this.ls3 = getLocalSearch();		
		
	}
	
	/**
	 * This method return the local search used for the metaheuristic.
	 * For calibration the VND use DoEVNDFactory.getVND(evaluator, params);
	 * For original Localsearch use new ABCExchangeLocalSearch(evaluator);
	 * For the calibrated VND in paper use the VND configured.
	 * @return
	 */
	private LocalSearch getLocalSearch() {
		
		//this.ls3 = DoEVNDFactory.getVND(evaluator, params);
		//this.ls3 = new ABCExchangeLocalSearch(evaluator);	
		
		VND vnd = new BasicVND();
		
		vnd.addLocalSearch(new InternalInsertionLocalSearch(evaluator));
		vnd.addLocalSearch(new InternalSwapLocalSearch(evaluator).setFirstImprovement(true));
		vnd.addLocalSearch(new ExternalInsertionLocalSearch(evaluator));
		vnd.addLocalSearch(new ExternalSwapLocalSearch(evaluator));	
		vnd.addLocalSearch(new IGSInternalLocalSearch(evaluator, 3));
		vnd.addLocalSearch(new IGSSwapLocalSearch(evaluator, 1));
		
		return vnd;
	}

	@Override
	public List<Solution> loadInitialSolutions() {
		
		tempoInicial = System.currentTimeMillis();
		
		List<Solution> pop = new ArrayList<Solution>(1);
		
		Solution attractionCell = atcsr_Rm.new_solution(random, config, getTotalTime(), getInitialTime());
		this.ls3.run(this.random, attractionCell, getTotalTime(), getInitialTime());
		pop.add(0, attractionCell);
		pop.add(1, attractionCell);
		
		best = attractionCell;
		
		return pop;
		
	}

	public long getLocalSearchTime() {return 0;}
	
	
	@Override
	public List<Solution> executeOneIteration(List<Solution> solucoes) {
		
		
		long processTime = System.currentTimeMillis() - tempoInicial;
		
		int IT_S = 0;		
		while (IT_S < IT_MAX && processTime <  60000){
			
			Solution solution = solucoes.get(1);
			
			Solution newCell = solution.clone();
			
			newCell.mutIGS(IGS_D);
			newCell.setObjectiveFunction();
			
			this.ls3.run(this.random, newCell, getTotalTime(), getInitialTime());
			
			if(newCell.getObjectiveFunction() < solution.getObjectiveFunction()) {
				
				solucoes.set(1, newCell);			
				if(newCell.getObjectiveFunction() < best.getObjectiveFunction()) {
					
					best = newCell;
					solucoes.set(0, newCell);			
					
				}
				
				IT_S = 0;
				
			} else{
				
				IT_S++;
				if (this.random.nextFloat() < Math.exp(-(newCell.getObjectiveFunction() - solution.getObjectiveFunction())/(TEMPERATURE) )) 
					solucoes.set(1, newCell);			
				
			}
			
			processTime = System.currentTimeMillis() - tempoInicial;
		}
		
		Solution newCell = new_solution();
		solucoes.set(1, newCell);
		
		if(newCell.getObjectiveFunction() < best.getObjectiveFunction()) {
			
			best = newCell;
			solucoes.set(0, newCell);			
			
		}
		
		
		return solucoes;
	}

	public Solution new_solution() {
		
		Solution s = solGenerator.newRandomCell(random, this.evaluator, this.config);
		this.ls3.run(this.random, s, getTotalTime(), getInitialTime());
		return s;
	
	}
	
	@Override
	public String getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Solution> updateMemory(List<Solution> Ab) {
		Ab.set(0, best);
		return Ab;
	}

	@Override
	public Solution getBest() {
		return best;
	}

}
