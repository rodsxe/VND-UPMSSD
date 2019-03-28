package model;

import java.util.ArrayList;
import java.util.List;

import model.base.Container;

public class MachineContainer extends Container{
	
	private long machineObjValue;
	private int id;
	private List<Job> jobs;	
	
	public MachineContainer(int id) {
		
		super();
		this.id = id;
		this.jobs = new ArrayList<Job>();

	}
		
	public int getId() {
		return id;
	}
	
	public long getMachineObjValue() {
		return machineObjValue;
	}

	public void setMachineObjValue(long machineObjValue) {
		this.machineObjValue = machineObjValue;
	}

	public void addJobInFinal(Job job){
		
		jobs.add(job);
	
	}
	
	public void addJob(Job job, int index){
	
		jobs.add(index, job);
	
	}
	
	public void removeJob(Job job){
		
		int removeIdx = -1;
		
		for (int i = 0; i < jobs.size(); i++){
			
			Job element = jobs.get(i);
			
			if(job.getId() == element.getId()){
				removeIdx = i;
				break;
			}
			
		}
		
		if(removeIdx != -1)removeJob(removeIdx);
		
	}
	
	public Job removeLastJob(){
		
		Job job = jobs.remove(jobs.size() - 1);
		return job;
	}
	
	public Job removeJob(int idx){
		
		Job job = jobs.remove(idx);
		return job;
	
	}
	
	public void replaceJob(int idx, Job job){
		
		jobs.set(idx, job);
	
	}
	
	public void swapJobs(int j, int w){
		
		Job t1 = jobs.get(j);
		Job t2 = jobs.get(w);
		jobs.set(j, t2);
		jobs.set(w, t1);
	
	}
	
	public Job getLastJob(){
		return (getJobs().isEmpty())?null:getJobs().get(getJobs().size() - 1);
	}
	
	public Job getJob(int idx){
		return this.jobs.get(idx);
	}
	
	public List<Job> removeLotOfJobs(int initialPosition, int numberOfJobs){
		
		List<Job> result = new ArrayList<Job>();
		
		for(int i = initialPosition; i < initialPosition + numberOfJobs; i++){
			
			result.add(this.removeJob(initialPosition));
		
		}
		
		return result;
		
	}
	public void addLotOfJobs(int initialPosition, List<Job> jobs){
		
		for (int i = 0; i < jobs.size(); i++) {
			
			Job job = jobs.get(i);
			this.addJob(job, initialPosition + i);
			
		}
		
	}
	
	public MachineContainer clone(){
		
		MachineContainer clone = new MachineContainer(this.id);
		cloneJobs(clone);
		return clone;
		
	}
	
	private void cloneJobs(MachineContainer clone){
		
		for (Job job : jobs) 
			clone.addJobInFinal(new Job(job.getId(), job.getDueDate(), job.isRestrict(), job.getReleaseDate(), job.getTardinessWeight(), job.getEarlinessWeight(), job.getDueDateEarly()));
		
	}
	
	public int size(){
		return jobs.size();
	}

	public List<Job> getJobs() {
		return jobs;
	}
	
}
