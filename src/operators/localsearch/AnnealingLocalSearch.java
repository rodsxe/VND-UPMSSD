package operators.localsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;
import model.Solution;
import model.Job;

public class AnnealingLocalSearch implements LocalSearch{

	private ObjectFunctionEvaluator evaluator;
	
	private float beta;
	private float gama;
	
	public AnnealingLocalSearch(ObjectFunctionEvaluator evaluator, float beta, float gama) {
		
		super();
		this.evaluator = evaluator;
		this.beta = beta;
		this.gama = gama;
		
	}
	
	
	public boolean run(Random rand, Solution s, long totalTime, long initialTime) {
		
		s.setObjectiveFunction();
		Solution best = s;
		Solution x = s;
				
		int M = evaluator.getSchedulingConfig().getNumberOfMachines(); 
		int N = evaluator.getSchedulingConfig().getNumberOfJobs();
		
		int iterMax = 30;
				
		float alpha = 0.97f;
		float Tr = 350.f;
		
		long time  = System.currentTimeMillis() - initialTime;
		
		/*float beta = 0.005f;
		float gama = 0.05f;*/
				
		int J = (int)(gama * N * M);
		int I = (int)(beta * N * M);
		
		
		boolean improvement;
		int itWithoutImprovement = 0;
		
		while (iterMax > itWithoutImprovement && time <= totalTime) {
		
			x = best;
			improvement = false;
			int i = 0, j = 0;
			
			while (i <= I && j <= J) {
				
				j++;
				
				Solution clone = generateNewSol(rand, x);
				float delta = clone.getObjectiveFunction() - best.getObjectiveFunction();
				
				if (delta < 0) {
					
					best = clone;
					x = clone;
					i++;
					improvement = true;
					
				} else if (delta >= 0 && rand.nextFloat() <  Math.exp(-delta/Tr)) {
					
					x = clone;
					i++;
					
				}
			
			}
			
			if (!improvement) itWithoutImprovement++;
			else itWithoutImprovement = 0;
				
			Tr = alpha * Tr;
			
			time = System.currentTimeMillis() - initialTime;
		
		}
		
		s.setMachines(best.getMachines());
		s.setObjectiveFunction();
		
		return false;
	}
	
	private List<Integer> getEligiblyJobs(MachineContainer machine) {
		
		boolean previousJobInList = true;
		Job job;
		List<Integer> jobsPositions = new ArrayList<Integer>();
		
		for (int i =0; i < machine.getJobs().size(); i++) {
			
			job = machine.getJob(i);
			
			if (job.isHaAtraso()) {
				
				jobsPositions.add(i);
				
				if (!previousJobInList) jobsPositions.add(i - 1);
				
				previousJobInList = true;
				
			} else previousJobInList = false;
			
		}
		
		return jobsPositions;
		
	}
	
	private void swapJobs(Random rand, MachineContainer lower, MachineContainer higher) {
		
		int indiceTM1, indiceTM2;
		Job t1, t2;
		List<Integer> fromEligiblyJobs;
		
		if (this.evaluator.getObjectFunctionValue(null, higher) > 0) {
			
			fromEligiblyJobs =  getEligiblyJobs(higher);
					
			indiceTM1 = fromEligiblyJobs.get(rand.nextInt(fromEligiblyJobs.size()));
			indiceTM2 = rand.nextInt(lower.getJobs().size());
			
			t1 = higher.getJob(indiceTM1);
			t2 = lower.getJob(indiceTM2);
			higher.replaceJob(indiceTM1, t2);
			lower.replaceJob(indiceTM2, t1);
			
		}
		
	}
	
	private void realocateJobs(Random rand, MachineContainer fromMachine, MachineContainer toMachine) {
	
		if (this.evaluator.getObjectFunctionValue(null, fromMachine) > 0) {
			
			List<Integer> fromEligiblyJobs =  getEligiblyJobs(fromMachine);
			Job j = fromMachine.removeJob(fromEligiblyJobs.get(rand.nextInt(fromEligiblyJobs.size())));
			toMachine.addJob(j, rand.nextInt(toMachine.getJobs().size() + 1));
			
		}
		
	}
	
	private Solution generateNewSol(Random rand, Solution s) {
		
		Solution clone = s.clone();
		
		MachineContainer[] machines = ordena(clone);
		
		MachineContainer lower = machines[0];
		
		MachineContainer intermatiate = machines[rand.nextInt(machines.length - 2) + 1];
		
		MachineContainer higher = machines[machines.length - 1];
		
		realocateJobs(rand, intermatiate, lower);
		realocateJobs(rand, higher, intermatiate);
						
		if (clone.setObjectiveFunction() > s.getObjectiveFunction()) clone = s.clone();
		
		machines = ordena(clone);
		
		lower = machines[0];
		
		higher = machines[machines.length - 1];
		
		swapJobs(rand, lower, higher);
		//clone.setTardiness(clone.calcTardiness());
		
		clone.setObjectiveFunction();
		
		return clone;
		
	}


	
	
	private MachineContainer[] ordena(Solution solution) {
		
		MachineContainer[] machines = solution.getMachines();
		MachineContainer[] result = new MachineContainer[machines.length];
		for(int i = 0; i < machines.length; i++){
			result[i] = machines[i];
			result[i].setMachineObjValue((long)this.evaluator.getObjectFunctionValue(solution, result[i]));
		}
		
		Arrays.sort(result, new Comparator<MachineContainer>() {
			
			@Override
			public int compare(MachineContainer machine, MachineContainer machine2) {
				
				long objFuncM1 = machine.getMachineObjValue();
				long objFuncM2 = machine2.getMachineObjValue();
				if(objFuncM2 < objFuncM1)return 1;
				else if(objFuncM2 > objFuncM1) return -1;
				else return 0;
				
			}
		});
		
		return result;
		
	}
}
