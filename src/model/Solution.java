package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import operators.function.evaluator.ObjectFunctionEvaluator;

public class Solution {
	
	private ObjectFunctionEvaluator evaluator;
	private Random rand;
	
	private MachineContainer[] machines;
	private Integer nJobs;
		
	private float maturationFactor;
	private float objectiveFunction;
	private float previusObjectiveFunction;
	private double fitness;
	private float initialObjectiveFunction;
	
	public float mutationRate;
	
	public static float CONCENTRATION_DEC_RATE = 0.035f;
	
	private Edge[] edges;
	
	private long time;
	
	
	public Solution(ObjectFunctionEvaluator evaluator, Random rand, MachineContainer[] machines){
		
		this.evaluator = evaluator;
		this.machines = machines;
		this.objectiveFunction = calcObjectiveFunction();
		this.previusObjectiveFunction = objectiveFunction;
		this.maturationFactor = 0;
		this.rand = rand;
		mutationRate = rand.nextFloat();
		
	}
	
	private Solution(ObjectFunctionEvaluator evaluator, 
				Random rand, 
				MachineContainer[] machines, 
				float objectiveFunction, 
				float previusObjectiveFunction,
				float maturationFactor,
				double fitness,
				float initialSolution, float parentMutationRate){
		
		this.rand = rand;
		this.evaluator = evaluator;
		this.machines = machines;
		this.objectiveFunction = objectiveFunction;
		this.previusObjectiveFunction = previusObjectiveFunction;
		this.maturationFactor = maturationFactor;
		this.fitness = fitness;
		this.initialObjectiveFunction = initialSolution;
		this.edges = null;
		
		mutationRate = (this.getFmat() * parentMutationRate) + (1 - this.getFmat()) * rand.nextFloat(); 
		
	}
	
		
	public Integer getNumberOfJobs() {
		
		if(nJobs != null) return nJobs;
		
		nJobs = 0;
		for (MachineContainer machine : machines) {
			nJobs += machine.size();
		}
		
		return nJobs;
	}
	
	public void setEvaluator(ObjectFunctionEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public MachineContainer[] getMachines() {
		return machines;
	}
	
	public void setMachines(MachineContainer[] machines) {
		
		this.machines = machines;
		this.objectiveFunction = calcObjectiveFunction();
	
	}


	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public float getPreviusObjectiveFunction() {
		return previusObjectiveFunction;
	}

	public void setPreviusObjectiveFunction(float previusObjectiveFunction) {
		this.previusObjectiveFunction = previusObjectiveFunction;
	}

	public void increaseMaturationFactor(){
		maturationFactor = Math.min(1, maturationFactor + CONCENTRATION_DEC_RATE);
	}
	
	public void resetMaturationFactor() {
		this.maturationFactor = 0;
	}
	
	public float getMaturationFactor(){
		return maturationFactor;
	}
	
	public float getFmat() {
		return 1.f - maturationFactor;
	}
	
	public float getInitialObjFunction() {
		return initialObjectiveFunction;
	}
	
	public void setInitialObjFunction(float initialObjFunction) {
		this.initialObjectiveFunction = initialObjFunction;
	}
	
	public MachineContainer getMachine(int index){
		return machines[index];
	}
	
	public float calcObjectiveFunction(){
		
		return evaluator.getObjectFunctionValue(this);
		
	}
	
	public float getObjectiveFunction() {
		
		return objectiveFunction;
	
	}
	
	public float setObjectiveFunction() {
		
		this.objectiveFunction = calcObjectiveFunction();
		return objectiveFunction;
		
	}
	
	public float getMutationRate() {
		return mutationRate;
	}

	
	public Solution clone(){
		
		MachineContainer[] clonedMachines = new MachineContainer[machines.length];
		
		for (int i = 0; i < clonedMachines.length; i++) {
			clonedMachines[i] = machines[i].clone();
		}
		
		Solution s = new Solution(	this.evaluator, 
							this.rand, 
							clonedMachines, 
							this.getObjectiveFunction(), 
							this.getPreviusObjectiveFunction(),
							this.getMaturationFactor(),
							this.getFitness(),
							this.getInitialObjFunction(), this.getMutationRate());
		
		return s;
		
	}
	
	
	public MachineContainer getRandomMachineWithCost(Random random, List<MachineContainer> machines) {
		
		MachineContainer choosed = machines.remove(random.nextInt(machines.size()));
		List<MachineContainer> observed = new ArrayList<MachineContainer>();
		float cost;
		
		while (((cost = evaluator.getObjectFunctionValue(this, choosed)) < 0.01) && machines.size() > 0) {
			
			observed.add(choosed);
			choosed = machines.remove(random.nextInt(machines.size()));
		
		}
		
		if(cost == 0)return null;
		
		machines.addAll(observed);
		return choosed;
	
	}
	
	public int getNumberOfMachines() {
	
		return this.machines.length;
		
	}

	public List<MachineContainer> arrayToList(MachineContainer[] machines) {
		
		List<MachineContainer> listOfmachines = new ArrayList<MachineContainer>();
		int maxIndice = machines.length;
		
		for(int i =0; i < maxIndice; i++ )listOfmachines.add(machines[i]);		
		
		return listOfmachines;
	
	}
	
	public void randomSwapMoviment(){
		
		MachineContainer[] machines = getMachines();
		
		int indexm1 = evaluator.getLSMachineIndex(rand, this, this.arrayToList(machines));
		if (indexm1 == -1) indexm1 = this.rand.nextInt(this.machines.length);
		MachineContainer m1 = machines[indexm1];
		
		int iM2 = this.rand.nextInt(this.machines.length);
		MachineContainer m2 = this.machines[iM2];
		int numTM1 = m1.size();
		int numTM2 = m2.size();
		if(numTM1 > 0 && numTM2 > 0 ){
			
			int indiceTM1 = this.rand.nextInt(numTM1);
			int indiceTM2 = this.rand.nextInt(numTM2);
			
			Job t1 = m1.getJob(indiceTM1);
			Job t2 = m2.getJob(indiceTM2);
			m1.replaceJob(indiceTM1, t2);
			m2.replaceJob(indiceTM2, t1);
		
		}
		
	}
	
	public void randomInsertionMoviment(){
		
		MachineContainer[] machines = getMachines();
		int indexm1 = evaluator.getLSMachineIndex(rand, this, this.arrayToList(machines));
		if (indexm1 == -1) indexm1 = this.rand.nextInt(this.machines.length);
		MachineContainer m1 = machines[indexm1];
		
		int numTM1 = m1.size();
		if(numTM1 > 0){
			
			int indiceTM1 = this.rand.nextInt(numTM1);
			Job job = m1.getJob(indiceTM1);
			m1.removeJob(indiceTM1);
			
			int iM2 = this.rand.nextInt(this.machines.length);
			MachineContainer m2 = this.machines[iM2];
			int numTM2 = m2.size();
			int indiceTM2 = 0;
			if(numTM2 > 1) indiceTM2 = this.rand.nextInt(numTM2);
			m2.addJob(job, indiceTM2);
			
		}
		
	}
	
	public void mutIGS (int N) {
		
		
		MachineContainer sortedMachine;
		
		int bestM = -1;
		int bestJ = -1;
		float newCost, minCost, actualCost;
		
		Job job;
		List<Job> sortedJobs = new ArrayList<Job>();
		
		for (int i = 0; i < N; i++){
			
			sortedMachine = this.machines[rand.nextInt(this.machines.length)];
			if (sortedMachine.getJobs().size() == 0)	i--;
			else sortedJobs.add(sortedMachine.removeJob(rand.nextInt(sortedMachine.getJobs().size())));
		
		}
				
		for (int i = 0; i < N; i++) {
				
			minCost = Float.MAX_VALUE;
			job = sortedJobs.get(i);
			bestM = -1;
			bestJ = -1;
			
			for (int m = 0; m < this.machines.length; m++) {
				
				sortedMachine = this.machines[m];
				actualCost = evaluator.getObjectFunctionValue(this, sortedMachine);
				
				for (int j = 0; j <= sortedMachine.getJobs().size(); j++) {
					
					sortedMachine.addJob(job, j);
					newCost = evaluator.getObjectFunctionValue(this, sortedMachine) - actualCost;
					
					if(newCost < minCost){
						
						minCost = newCost;
						bestJ = j;
						bestM = m;
												
					}
					
					sortedMachine.removeJob(j);
				
				}
				
			}
			
			sortedMachine = this.machines[bestM];
			
			sortedMachine.addJob(job, bestJ);
			
		}
		
	}
	
	public Edge[] getEdges(){
		
		if(this.edges != null)return this.edges;
		Edge[] result = new Edge[getNumberOfJobs()];
		for (MachineContainer machine : machines) {
			List<Job> jobs = machine.getJobs();
			int anterior = -1;
			for (int j = 0; j < jobs.size(); j++) {
				Job job = jobs.get(j);
				result[job.getId()] = new Edge(machine.getId(), job.getId(), anterior);
				anterior = job.getId();
			}
			
		}
		
		this.edges = result;
		
		return result;
		
	}
		
	public float getDistanceFromC2(Solution c2){
		
		Edge[] vetorArrestasS1 = this.getEdges();
		Edge[] vetorArrestasS2 = c2.getEdges();
		int distancia = 0;
		for (int j = 0; j < vetorArrestasS1.length; j++) {
			Edge arresta = vetorArrestasS1[j];
			int i = arresta.getI();
			int k = arresta.getK();
			arresta = vetorArrestasS2[j];
			int w = arresta.getI();
			int z = arresta.getK();
			if(i != w || k != z)distancia++;
		}
		
		return (float)distancia/(float)vetorArrestasS1.length;
	}
	
	public void printJobs(){
		for (int i = 0; i < machines.length; i++) {
			System.out.println("M" + i + ":");
			List<Job> list = machines[i].getJobs();
			for (Job job : list) {
				System.out.print("T" + job.getId() + "  -----> ");
			}
			System.out.println();
		}
	}
}




