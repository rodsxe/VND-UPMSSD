package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import algorithms.vnd.BasicVND;
import algorithms.vnd.VND;
import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import model.Job;
import model.MachineContainer;
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
 * Artificial Bee Colony algorithm proposed by Ying & Lin (2012) in:
 * 
 * Ying, K.-C., and Lin, S. W. (2012). Unrelated parallel machines scheduling with
 * sequence and machine dependent setups times and due dates constraints.
 * International Journal of Innovative Computing, Information and Control, 8, 3279 – 3297.
 *
 * @author Rodney Oliveira Marinho Diana
 *
 */
public class ABC extends BaseAlgorithm {
	
	private Random random;
	private RandomSolutionGenerator solGenerator = new RandomSolutionGenerator();
	
	private ATCSR_Rm atcsr_Rm;
	
	private SchedulingInstancesConfig config;
	private ObjectFunctionEvaluator evaluator;
	
	private LocalSearch ls3;
	private long localSearchTime;
	
	private int N = 6;
	private int MAX_MUT_RATE = 5;
	private float THRESHOLT = 5.f;
	private float LIMIT = 150.f;
	
	
	private Solution bestFoodSource;
	
	public ABC() {
		// TODO Auto-generated constructor stub
	}
	
	public long getLocalSearchTime() {
		return localSearchTime;
	}

	@Override
	public void setInstanceValues(SchedulingInstancesConfig config, ObjectFunctionEvaluator evaluator, Random rand, HashMap<String, String> params) {
		
		bestFoodSource =  null;
		atcsr_Rm = new ATCSR_Rm(evaluator);
		
		this.random = rand;
		this.config = config;
		this.evaluator = evaluator;
		this.localSearchTime = 0;
		Solution.CONCENTRATION_DEC_RATE = 1.f/LIMIT;
		
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
		
		VND vnd = new BasicVND();
		vnd.addLocalSearch(new IGSInternalLocalSearch(evaluator, 3));
		vnd.addLocalSearch(new InternalInsertionLocalSearch(evaluator));
		vnd.addLocalSearch(new ExternalSwapLocalSearch(evaluator));
		vnd.addLocalSearch(new ExternalInsertionLocalSearch(evaluator));
		
		//this.ls3 = DoEVNDFactory.getVND(evaluator, params);
		
		//this.ls3 = new ABCExchangeLocalSearch(evaluator);	
		
		return vnd;
	}
	
	@Override
	public List<Solution> loadInitialSolutions() {
		
		List<Solution> pop = new ArrayList<Solution>(N);
		
		Solution attractionCell = atcsr_Rm.new_solution(random, config, getTotalTime(), getInitialTime());
		pop.add(attractionCell);
		
		for (int i = 1; i < N; i++){
		
			Solution s = newFoodSource();
			pop.add(s);
		
		}
		
		updateTheBestFoodSource(pop);
		
		return pop;
		
	}
	
	public Solution newFoodSource() {
		
		Solution s = solGenerator.newRandomCell(random, this.evaluator, this.config);
		calcFitness(s);
		return s;
	
	}
	
	private float calcFitness(Solution bee) {
		
		float fitness = 1.f /(1.f + bee.getObjectiveFunction());
		bee.setFitness(fitness);
		return fitness;
	
	}
	
	private Job[] linkedListToArray(MachineContainer[] machines) {
		
		int i = 0;
		Job[] aux = new Job[config.getNumberOfJobs() + config.getNumberOfMachines() - 1];
		List<Job> jobs;
		
		for (int j = 0; j < machines.length; j++) {
			
			MachineContainer machine = machines[j];
			jobs = machine.getJobs();
			for (Job job : jobs) aux[i++] = job;
			if (j < machines.length - 1) aux[i++] = new Job(-1, -1, false, -1, -1, -1, -1);
			
		}
		
		return aux;
		
	}
	
	private MachineContainer[] arrayTolinkedList(Job[] list) {
		
		int m = 0;
		MachineContainer[] machines = new MachineContainer[config.getNumberOfMachines()];
		machines[m] = new MachineContainer(m);
		for (Job job : list) {
			
			if (job.getId() ==-1) {m++; machines[m] = new MachineContainer(m); }
			else machines[m].addJobInFinal(job);
			
		}
		
		return machines;
		
	}
	
	private Solution crossOver(Solution p1, Solution p2){
		
		int pos;
		Job job;
		boolean foundTheJob;
		
		Job[] sol = new Job[config.getNumberOfJobs() + config.getNumberOfMachines() - 1];
		Job[] parent1 = linkedListToArray(p1.getMachines());
		Job[] parent2 = linkedListToArray(p2.getMachines());
		
		int tempPoint1 = random.nextInt(parent1.length);
		int tempPoint2 = random.nextInt(parent1.length);
		
		int point1 = Math.min(tempPoint1, tempPoint2);
		int point2 = Math.max(tempPoint1, tempPoint2) + 1;
	
		for (int i = point1; i < point2; i++) sol[i] = parent1[i];
		
		for (int i = 0; i < point1; i++) if (parent1[i].getId() == -1) sol[i] = parent1[i];
		
		for (int i = point2; i < parent1.length; i++) if (parent1[i].getId() == -1) sol[i] = parent1[i];
		
		pos = 0;
		if (sol[pos] != null) while (++pos < sol.length && sol[pos] != null);
		
		for (int i = 0; i < parent1.length; i++) {
			
			job = parent2[i];
			if (job.getId() > -1) {
				
				foundTheJob = false;
				for (int j = point1; j < point2 && !foundTheJob; j++) 
				
					if (sol[j].getId() == job.getId()) foundTheJob = true;
				
				if (!foundTheJob) {
					
					sol[pos] = job;
					while (++pos < sol.length && sol[pos] != null);
										
				}
			
			}
			
		}
		
		
		Solution newBee = new Solution(evaluator, random, this.arrayTolinkedList(sol));
		newBee.setPreviusObjectiveFunction(p2.getObjectiveFunction());
		return newBee;
		
	}
	
	private void employeeBeePhase(List<Solution> pop, double bestFitness){
		
		Solution bestFoodSourceInPop = getBestFoodSourceInPop(pop);
		Solution newFoodSource;
		int mut_rate = random.nextInt(MAX_MUT_RATE) + 1;
		
		for (int i = 0; i < pop.size(); i++) {
			
			Solution foodsource = pop.get(i);
			if (foodsource.getFitness() == bestFitness) {
				
				newFoodSource = foodsource.clone(); 
				newFoodSource.setPreviusObjectiveFunction(foodsource.getObjectiveFunction());
				newFoodSource.mutIGS(mut_rate);
				
			}			
			else newFoodSource = this.crossOver(bestFoodSourceInPop, foodsource);
			
			newFoodSource.setObjectiveFunction();
			calcFitness(newFoodSource);
			
			
			if ((newFoodSource.getFitness() - bestFitness)/bestFitness < THRESHOLT || newFoodSource.getFitness() >= bestFitness)
				localSearch(newFoodSource);
			
			calcFitness(newFoodSource);
						
			if (newFoodSource.getFitness() >= foodsource.getFitness()) pop.set(i, newFoodSource);
			
		}
		
	}

	private void localSearch(Solution newFoodSource) {
		
		long time = System.currentTimeMillis();
		
		this.ls3.run(random, newFoodSource, getTotalTime(), getInitialTime());
		
		this.localSearchTime += System.currentTimeMillis() - time;
		
	}
	
	private void onlookerPhase(List<Solution> pop, double bestFitness){
		
		Solution newFoodSource;
		int mut_rate = random.nextInt(MAX_MUT_RATE) + 1;
		double totalFitness = 0;
		double pi;
		
		for (int i = 0; i < pop.size(); i++) {
			
			Solution foodsource = pop.get(i);
			totalFitness += foodsource.getFitness();
			
		}
		
		for (int i = 0; i < pop.size(); i++) {
			
			Solution foodsource = pop.get(i);
			pi = foodsource.getFitness()/totalFitness;
			
			
			if (random.nextDouble() < pi) {
				
				newFoodSource = foodsource.clone(); 
				newFoodSource.setPreviusObjectiveFunction(foodsource.getObjectiveFunction());
				newFoodSource.mutIGS(mut_rate);
			
				newFoodSource.setObjectiveFunction();
				calcFitness(newFoodSource);
				
				if ((newFoodSource.getFitness() - bestFitness)/bestFitness < THRESHOLT || newFoodSource.getFitness() >= bestFitness)
					localSearch(newFoodSource);
				
				newFoodSource.setObjectiveFunction();
				calcFitness(newFoodSource);
							
				if (newFoodSource.getFitness() >= foodsource.getFitness()) pop.set(i, newFoodSource);
				
			}
			
		}
		
	}
	
	private void scoutPhase(List<Solution> pop){
	
		for (int i = 0; i < pop.size(); i++) {
			
			Solution foodSource = pop.get(i);
			
			if (foodSource.getObjectiveFunction() >= (foodSource.getPreviusObjectiveFunction())) {
				
				foodSource.increaseMaturationFactor();
				if (foodSource.getFmat() < 0.1) {
					if(foodSource.getObjectiveFunction() < bestFoodSource.getObjectiveFunction()) 
						bestFoodSource = foodSource;
					
					pop.set(i, newFoodSource());
					
				}
				
			} else {
				
				foodSource.resetMaturationFactor();
				foodSource.setPreviusObjectiveFunction(foodSource.getObjectiveFunction());
			
			}
			
		}
		
	}
	
	private Solution getBestFoodSourceInPop(List<Solution> pop) {
	
		Solution best = pop.get(0);
		
		for (int i = 1; i < pop.size(); i++) {
			
			Solution foodSource = pop.get(i);
			if (best.getFitness() < foodSource.getFitness()) best = foodSource;
			
		}
		
		return best;
		
	}
	
	private void updateTheBestFoodSource(List<Solution> pop) {
		
		if(this.bestFoodSource == null) this.bestFoodSource = pop.get(0);
			
		for (int i = 0; i < pop.size(); i++) {
			
			Solution foodSource = pop.get(i);
			if (this.bestFoodSource.getFitness() < foodSource.getFitness()) this.bestFoodSource = foodSource;
			
		}
	
	}
	
	@Override
	public List<Solution> executeOneIteration(List<Solution> pop) {
		
		employeeBeePhase(pop, this.bestFoodSource.getFitness());
		
		onlookerPhase(pop, this.bestFoodSource.getFitness());
		
		scoutPhase(pop);
		
		updateTheBestFoodSource(pop);
		
		return pop;
	}
	
	private List<Solution> sortByObjectiveFunction(List<Solution> Ab){
		Collections.sort(Ab, new Comparator<Solution>() {
			@Override
			public int compare(Solution s1, Solution s2) {
				
				if(s1.getObjectiveFunction() < s2.getObjectiveFunction())return -1;
				else if(s1.getObjectiveFunction() > s2.getObjectiveFunction())return 1;
				else if(s1.getFmat() < s2.getFmat())return 1; 
				else if(s1.getFmat() > s2.getFmat())return -1; 
				else return 0;
				
			}
		});
		return Ab;
	}
	
	@Override
	public String getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Solution> updateMemory(List<Solution> Ab) {
		Ab.add(0, bestFoodSource);
		
		return sortByObjectiveFunction(Ab);
	}

	@Override
	public Solution getBest() {
		
		return this.bestFoodSource;
		
	}

}
