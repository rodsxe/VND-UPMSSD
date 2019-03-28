package algorithms.vnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Solution;
import operators.localsearch.LocalSearch;

public class PipeVND implements VND {

	private List<LocalSearch> seekers = new ArrayList<LocalSearch>();
	
	public void addLocalSearch(LocalSearch localSearch) { 
		seekers.add(localSearch);
	}
	
	public boolean run(Random rand, Solution s, long totalTime, long initialTime){
		
		boolean improvement = false;
		List<LocalSearch> neighborhoods = new ArrayList<LocalSearch>(seekers); 
		List<LocalSearch> removedLSOp = new ArrayList<LocalSearch>();
		
		while (!neighborhoods.isEmpty()) {
			
			LocalSearch ls = neighborhoods.get(0);
									
			if (ls.run(rand, s, totalTime, initialTime)) {
				
				neighborhoods.addAll(removedLSOp);
				removedLSOp.clear();
				improvement = true;
				
			} else removedLSOp.add(neighborhoods.remove(0));
			
		}
		
		s.setObjectiveFunction();
		
		return improvement;
	}

}
