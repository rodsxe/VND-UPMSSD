package instance.loader;

public class TargetSol {
	
	private String name;
	private int machines;
	private int jobs;
	private int bestSol;
	private int setup;
	
	public TargetSol(String name, int machines, int jobs, int bestSol, int setup) {
		super();
		this.name = name;
		this.machines = machines;
		this.jobs = jobs;
		this.bestSol = bestSol;
		this.setup = setup;
	}
	
	public TargetSol() {
		super();
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getMachines() {
		return machines;
	}
	public void setMachines(int machines) {
		this.machines = machines;
	}
	public int getJobs() {
		return jobs;
	}
	public void setTarefas(int jobs) {
		this.jobs = jobs;
	}
	public int getBestSol() {
		return bestSol;
	}
	public TargetSol setBestSol(int bestSol) {
		this.bestSol = bestSol;
		return this;
	}
	public int getSetup() {
		return setup;
	}
	public void setSetup(int setup) {
		this.setup = setup;
	}
	
	
	
}
