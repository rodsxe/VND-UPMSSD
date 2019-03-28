package operators.localsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Solution;
import model.Job;
import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class IGSInternalLocalSearch implements LocalSearch {
	
	private ObjectFunctionEvaluator evaluator;
	
	private int K;
	
	public IGSInternalLocalSearch(ObjectFunctionEvaluator evaluator, int K) {
		super();
		this.evaluator = evaluator;
		this.K = K;
	}
	
	@Override
	public boolean run(Random rand, Solution s, long totalTime, long initialTime) {
		
		//if(!true) return false; 
		Solution clone = s.clone();
		MachineContainer[] machines = clone.getMachines();
		
		MachineContainer sortedMachine;
		
		int bestM = -1;
		int bestJ = -1;
		float newCost, machineCost, minCost;
		
		Job job;
		Job[] sortedJobs = new Job[K];
		
		for (int i = 0; i < K; i++){
			
			sortedMachine = machines[rand.nextInt(machines.length)];
			while(sortedMachine.getJobs().size() == 0) sortedMachine = machines[rand.nextInt(machines.length)];
			sortedJobs[i] = sortedMachine.removeJob(rand.nextInt(sortedMachine.getJobs().size()));
		
		}
				
		for (int i = 0; i < K; i++) {
				
			minCost = Float.MAX_VALUE;
			job = sortedJobs[i];
			
			for (int m = 0; m < machines.length; m++) {
				
				sortedMachine = machines[m];
				machineCost = evaluator.getObjectFunctionValue(clone, sortedMachine);
				
				for (int j = 0; j <= sortedMachine.getJobs().size(); j++) {
					
					sortedMachine.addJob(job, j);
					newCost = evaluator.getObjectFunctionValue(clone, sortedMachine);
					
					if(newCost - machineCost < minCost){
						
						minCost = newCost - machineCost;
						bestJ = j;
						bestM = m;
						
					}
					
					sortedMachine.removeJob(j);
				
				}
				
			}
			
			machines[bestM].addJob(job, bestJ);
			
		}
		
		if (clone.setObjectiveFunction() < s.setObjectiveFunction()) {
			
			s.setMachines(clone.getMachines());
			s.setObjectiveFunction();
			return true;
			
		}
		
		
		return false;
		
	}

}
