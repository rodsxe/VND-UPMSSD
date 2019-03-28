package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree;

import algorithms.vnd.BasicVND;
import algorithms.vnd.CyclicVND;
import algorithms.vnd.DoEVNDFactory;
import algorithms.vnd.UnionVND;
import algorithms.vnd.VND;
import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import model.Job;
import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;
import operators.localsearch.ABCExchangeLocalSearch;
import operators.localsearch.AnnealingLocalSearch;
import operators.localsearch.ExternalInsertionLocalSearch;
import operators.localsearch.InternalInsertionLocalSearch;
import operators.localsearch.ExternalSwapLocalSearch;
import operators.localsearch.InternalSwapLocalSearch;
import operators.localsearch.IGSInternalLocalSearch;
import operators.localsearch.IGSSwapLocalSearch;
import operators.localsearch.LocalSearch;


/**
 * Genetic Algorithm proposed in:
 * 
 * Zeidi, J. R., & MohammadHosseini, S. (2015). Scheduling unrelated 
 * parallel machines with sequence-dependent setup times.
 * The International Journal of Advanced Manufacturing Technology, 81, 1487–1496. 
 * DOI:10.1007/s00170-015-7215-y.
 *
 * @author Rodney Oliveira Marinho Diana
 *
 */
public class GA extends BaseAlgorithm {
	
	private Random random;
	private RandomSolutionGenerator solGenerator = new RandomSolutionGenerator();
	
	private SchedulingInstancesConfig config;
	private ObjectFunctionEvaluator evaluator;
	
	private LocalSearch ls3;
	
	private ATCSR_Rm atcsr_Rm;
	
	private int N = 80;
	private float Pc = 0.6f;
	private float Pmut = 0.12f;
	private float Ps = 0.25f;
	
	private Solution best;
	
	public long getLocalSearchTime() {return 0;}
	
	@Override
	public void setInstanceValues(SchedulingInstancesConfig config, ObjectFunctionEvaluator evaluator, Random rand,
			HashMap<String, String> params) {
		
		atcsr_Rm = new ATCSR_Rm(evaluator);
		best = null;
		this.random = rand;
		this.config = config;
		this.evaluator = evaluator;
		
		this.ls3 = getLocalSearch();
		
	
	}
	

	/**
	 * This method return the local search used for the metaheuristic.
	 * For calibration the VND use DoEVNDFactory.getVND(evaluator, params);
	 * For original Localsearch use new AnnealingLocalSearch(evaluator, beta, gama);
	 * For the calibrated VND in paper use the VND configured.
	 * @return
	 */
	private LocalSearch getLocalSearch() {
		
		VND vnd = new BasicVND();
		vnd.addLocalSearch(new InternalInsertionLocalSearch(evaluator));
		vnd.addLocalSearch(new InternalSwapLocalSearch(evaluator).setFirstImprovement(true));
		vnd.addLocalSearch(new ExternalInsertionLocalSearch(evaluator));
		vnd.addLocalSearch(new ExternalSwapLocalSearch(evaluator).setFirstImprovement(true));
		vnd.addLocalSearch(new IGSInternalLocalSearch(evaluator, 3));
		vnd.addLocalSearch(new IGSSwapLocalSearch(evaluator, 1));
		
		this.ls3 = vnd;
		
		/*float beta = new Float(params.get("--beta"));
		float gama = new Float(params.get("--gama"));
		
		this.ls3 = new AnnealingLocalSearch(evaluator, beta, gama);
		*/
		//this.ls3 = DoEVNDFactory.getVND(evaluator, params);
		
		return vnd;
	}
	
	public Solution newChromosome() {
		
		Solution s = solGenerator.newRandomCell(random, this.evaluator, this.config);
		calcFitness(s);
		return s;
	
	}
	
	private float calcFitness(Solution chromosome) {
		
		float fitness = 1.f /(1.f + chromosome.getObjectiveFunction());
		chromosome.setFitness(fitness);
		return fitness;
	
	}
	
	@Override
	public List<Solution> loadInitialSolutions() {
		
		List<Solution> pop = new ArrayList<Solution>(N);
		
		Solution attractionCell = atcsr_Rm.new_solution(random, config, getTotalTime(), getInitialTime());
		pop.add(attractionCell);
		
		for (int i = 1; i < N; i++){
		
			Solution s = newChromosome();
			pop.add(s);
		
		}
		
		sortByObjectiveFunction(pop);
		
		this.best = pop.get(0);
		
		return pop;
	}
	
	private void updateFitness(List<Solution> pop) {
		
		for (Solution chromosome : pop) calcFitness(chromosome);
			
	}
	
	private void localSearch(List<Solution> pop) {
		
		for (Solution chromosome : pop) ls3.run(random, chromosome, getTotalTime(), getInitialTime());
			
	}
	
	private Solution selectionChromossome(List<Solution> pop, double[] rouletteWheel) {
		
		double rand = this.random.nextDouble();
		
		for (int i = 0; i < rouletteWheel.length; i++) {
			
			double pi = rouletteWheel[i];
			if (pi > rand) return pop.get(i);
					
		}
        
        return null;
		
	
	}
	
	private Solution[] parrentSelection(List<Solution> pop) {
	
		Solution[] selectedCells = new Solution[pop.size()*2];
		double totalObjFunction = 0;
		double pi;
		double[] rouletteWheel = new double[pop.size()];
		
		for (int i = 0; i < pop.size(); i++) {
			
			Solution chromossome = pop.get(i);
			totalObjFunction += chromossome.getFitness();
			
		}
		
		for (int i = 0; i < pop.size(); i++) {
			
			Solution chromossome = pop.get(i);
			pi = chromossome.getFitness() / totalObjFunction;
			rouletteWheel[i] = ((i == 0)? 0: rouletteWheel[i - 1]) + pi;
			
		}
		
		for (int i = 0; i < selectedCells.length; i += 2) {
		
			selectedCells[i] = selectionChromossome(pop, rouletteWheel);
			selectedCells[i + 1] = selectionChromossome(pop, rouletteWheel);
			
		}
		
		
		return selectedCells;
		
	}
	
	private Solution crossover(Solution parent1, Solution parent2, int[] points) {
		
		TreeMap<Integer, Integer> jobsScheduling = new TreeMap<Integer, Integer>();
		Job job;
		MachineContainer[] machines = new MachineContainer[points.length];
				
		for (int i = 0; i < machines.length; i++) {
			
			MachineContainer machineP1 = parent1.getMachine(i);
			MachineContainer machinesOffspring = new MachineContainer(i);
			
			for (int j = 0; j < points[i]; j++) {
				
				job = machineP1.getJob(j);
				machinesOffspring.addJobInFinal(job);				
				jobsScheduling.put(job.getId(), job.getId());
				
			}
			
			machines[i] = machinesOffspring;
			
		}
		
		for (int i = 0; i < machines.length; i++) {
			
			MachineContainer machineP2 = parent2.getMachine(i);
			MachineContainer machinesOffspring = machines[i];
						
			for (int j = points[i]; j < machineP2.getJobs().size(); j++) {
				
				job = machineP2.getJob(j);
				if (!jobsScheduling.containsKey(job.getId())) {
					
					machinesOffspring.addJobInFinal(job);
					jobsScheduling.put(job.getId(), job.getId());
					
				} 
				
			}
			
		}
		
		for (int i = 0; i < machines.length; i++) {
			
			MachineContainer machineP1 = parent1.getMachine(i);
			
			for (int j = points[i]; j < machineP1.getJobs().size(); j++) {
				
				job = machineP1.getJob(j);
				if (!jobsScheduling.containsKey(job.getId())) 
					
					machines[random.nextInt(machines.length)].addJobInFinal(job);;
			
			}
			
		}
		
		return new Solution(evaluator, random, machines);
		
	}
	
	private List<Solution> crossover(Solution[] parents) {
		
		List<Solution> offsprings = new ArrayList<Solution>();
		
		for (int i = 0; i < parents.length; i += 2) {
			
			if (this.random.nextDouble() < Pc) {
				
				Solution parent1 = parents[i];
				Solution parent2 = parents[i + 1];
				int[] points = new int[parent1.getMachines().length];
				
				for (int j = 0; j < points.length; j++) {
					
					int ni = Math.min(parent1.getMachine(j).getJobs().size(), parent2.getMachine(j).getJobs().size());
					
					points[j] = (ni > 0)? random.nextInt(ni) : 0; 
					
				}
				offsprings.add(crossover(parent1, parent2, points));
				offsprings.add(crossover(parent2, parent1, points));
				
			}
			
		}
		
		return offsprings;
			
	}
	
	private void mutation(List<Solution> offsprings) {
	
		for (Solution chromossome : offsprings) {
			
			if (this.random.nextDouble() < Pmut) chromossome.randomInsertionMoviment();
			
		}
	
	}
	
	private void selection(List<Solution> pop, List<Solution> offsprings) {
		
		int replaceSols = (int)(offsprings.size() * Ps);
		
		for (int i = pop.size() - replaceSols; i < pop.size(); i++) {
			
			Solution chromossome = offsprings.remove(random.nextInt(offsprings.size()));
			//ls3.run(random, chromossome, null);
			
			pop.set(i, chromossome);
			
		}
		
	}
	
	private List<Solution> sortByObjectiveFunction(List<Solution> Ab){
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

	@Override
	public List<Solution> executeOneIteration(List<Solution> pop) {
		
		updateFitness(pop);
		
		Solution[] parents = parrentSelection(pop);
		
		List<Solution> offsprings = crossover(parents);
		
		mutation(offsprings);
		
		selection(pop, offsprings);
				
		localSearch(pop);
		
		sortByObjectiveFunction(pop);
		
		this.best = pop.get(0);
		
		return pop;
	}

	@Override
	public String getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Solution> updateMemory(List<Solution> Ab) {
		
		return Ab;
	
	}

	@Override
	public Solution getBest() {
		return best;
	}

}
