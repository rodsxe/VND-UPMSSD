package algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import instance.loader.SchedulingInstancesConfig;
import model.Job;
import model.MachineContainer;
import model.Solution;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class RandomSolutionGenerator {

	public RandomSolutionGenerator() {}
	
	public Solution newRandomCell(Random random, ObjectFunctionEvaluator evaluator,SchedulingInstancesConfig config){
		
		MachineContainer[] machines = new MachineContainer[config.getNumberOfMachines()];
		for(int i = 0; i < machines.length; i++){
			machines[i] = new MachineContainer(i);
		}
		Solution s = newRandomCell(random, machines, evaluator, config);
		s.setInitialObjFunction(s.getObjectiveFunction());
		return s;
		
	}
	
	private Solution newRandomCell(Random random, MachineContainer[] machines, ObjectFunctionEvaluator evaluator, SchedulingInstancesConfig config){
		
		List<Job> list = new ArrayList<Job>();
		
		for(int job = 0; job < config.getNumberOfJobs(); job++) 
			list.add(new Job(job, config.getDueDate(job), config.isRestrict(job), config.getReleaseDate(job), config.getTardinessWeight(job), config.getEarliestWeight(job), config.getDueDateEarly(job)));
				
		for (int i = 0; i < config.getNumberOfJobs(); i++) {
			
			MachineContainer machine = machines[random.nextInt(machines.length)];
			int indice = 0;
			if (list.size() > 1) indice = random.nextInt(list.size() - 1);
			machine.addJobInFinal(list.get(indice));
			list.remove(indice);
			
		}
		
		return newInstance(evaluator, random, machines);
		
	}
 
	protected Solution newInstance(ObjectFunctionEvaluator evaluator, Random random, MachineContainer[] machines) {
		return new Solution(evaluator, random, machines);
	}
	
}
