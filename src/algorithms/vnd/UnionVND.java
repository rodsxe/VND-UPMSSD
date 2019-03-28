package algorithms.vnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Solution;
import operators.localsearch.LocalSearch;

public class UnionVND implements VND {

	private List<LocalSearch> seekers = new ArrayList<LocalSearch>();
	
	public void addLocalSearch(LocalSearch localSearch) { 
		seekers.add(localSearch);
	}
	
	public boolean run(Random rand, Solution s, long totalTime, long initialTime){
		
		boolean improvement = true;
		s.setObjectiveFunction();
		Solution best = s;
		Solution current;
		
		while (improvement) {
			
			improvement = false;
			
			current = best.clone();
			
			for (LocalSearch ls : seekers) {
											
				Solution clone = current.clone();
				
				if (ls.run(rand, clone, totalTime, initialTime)) {
					
					if (clone.setObjectiveFunction() < best.getObjectiveFunction()) best = clone;
					
					improvement = true;
					
				}
				
			}
			
		}
		
		if (best.getObjectiveFunction() < s.getObjectiveFunction()) {
			
			s.setMachines(best.getMachines());
			s.setObjectiveFunction();
			
		}
		
		return improvement;
	}

}
