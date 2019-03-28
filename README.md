# VND-UPMSSD
Project destinated to the paper "Analysis of Variable Neighborhood Descent as a Local Search Operator for Total Weighted Tardiness Problem on Unrelated Parallel Machines"

#EXPERIMENTS CONFIGURATIONS

Every configuration of the experiments, e.g. the directory of instances, number of executions without improvement, etc., must be done in the file ExperimentConfig.java;

#COMPILE AND EXECUTE

To compile goes to ./src directory and execute in command line javac -cp lib/tools.jar -encoding ISO-8859-1 @sources.txt

To execute the algorithm goes to ./src directory and execute in command line java -cp lib/tools.jar:. ExperimentRunner The path and name of the result files must be configurated in ExperimentConfig.java

#RESULTS

The analytical results showed in the paper to the benchmark instances are avaliable in the directory ./results.

#INSTANCES

The instances used in the paper are not property of the authors. In reason to that and to make possible tests in the algorithm only one instance of the studied case is avaliable in the directory ./instances.

To get the complete set of instances, contact the authors of the original papers.

The Total Weighted Tardiness contact Yang-Kuei Lin - yklin@mail.fcu.edu.tw Paper: Lin, Y.-K., Hsieh, F.-Y., 2014. Unrelated parallel machine scheduling with setup times and ready times. International Journal of Production Research Vol. 52 (4), P. 1200 – 1214.

#CONTACT

Any doubt may be emailed to rodneyoliveira@dppg.cefetmg.br or sergio@dppg.cefetmg.br
