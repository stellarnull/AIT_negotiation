package negotiator.group4;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import negotiator.Bid;
import negotiator.Domain;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;


/**
 * Opponent modeling using frequency analysis heuristic
 *
 */
public class Group4OpponentModel
{
    private Map<IssueDiscrete, Map<ValueDiscrete, Integer>> issueValueCount;
    private Map<IssueDiscrete, Double> weights;

    public Group4OpponentModel(Domain domain) {
        issueValueCount = new HashMap<>();
        weights = new HashMap<>();
        
        for (Issue issue : domain.getIssues()) 
        {
        	
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            issueValueCount.put(issueDiscrete, new HashMap<>());
            
            //let each value be 1 at first
            for (ValueDiscrete value : issueDiscrete.getValues()) 
            {
                issueValueCount.get(issueDiscrete).put(value, 1);
            }
            
            //initiate the weights
            weights.put(issueDiscrete, 1.0 / domain.getIssues().size());
        }
    }

    /**
     * Evaluates the predicted utility of a given bid, using the information
     * provided thus far.
     * 
     * @param bid
     *            the bid to be evaluated
     * @return a value from 0.0 to 1.0 giving the predicted utility of the bid
     */
    public double evaluateBid(Bid bid) {
        double utility = 0.0;
        
        //update the weights
        weights = computeWeights();
        
        try {
            for (Issue issueRaw : bid.getIssues()) 
            {
            	double maxValue = 0;
                ValueDiscrete value = (ValueDiscrete) bid.getValue(issueRaw.getNumber());
                IssueDiscrete issue = (IssueDiscrete) issueRaw;

                double issueWeight = weights.get(issue);
                double issueValue = ((double) issueValueCount.get(issue).get(value));

                //add 1 to the current value
                issueValueCount.get(issue).put(value, (int) ((issueValue + 1)));
             
                //update and normalize the value to make the max of the value equals 1
                for (ValueDiscrete eachValue : issue.getValues()) 
                {
                	double tempValue = ((double) issueValueCount.get(issue).get(eachValue));
                    if (tempValue > maxValue)
                    	maxValue = tempValue;
                }
                
                //update and normalize the value to make the max of the value equals 1
                //to keep track of the history, only the value that is going to be computed
                //in the utility is normalized
                issueValue /= maxValue;
                
                //compute the utility
                utility += issueWeight * issueValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return utility;
    }
    
    /**
     * Given the current data about the opponent, return the predicted weight
     * for each issue. If the opponent suggests many different values for that 
     * issue, we assume that it is unimportant for it.
     * 
     * @return a map of issues to their respective weights, normalized such that
     *         the sum of the weights is 1.0
     */
    private Map<IssueDiscrete, Double> computeWeights() 
    {
        Map<IssueDiscrete, Double> weights = new HashMap<>();

        for (IssueDiscrete issue : issueValueCount.keySet()) 
        {
        	double variance = computeVariance(issue);
            weights.put(issue, 1.0/variance);
        }

        // Normalize weights: make them sum to 1.0
        double sum = weights.values().stream().reduce(0.0, Double::sum);
        weights.forEach((k, v) -> weights.put(k, v / sum));

        return weights;
    }

   
    private double computeVariance(IssueDiscrete issue)
    {
    
        List<Integer> counts = new ArrayList<>(issueValueCount.get(issue).values());
        int max = counts.stream().max(Integer::compare).get();
        List<Double> weightedCounts = counts.stream()
                .mapToDouble(i -> (((double) i) / max)).boxed()
                .collect(Collectors.toList());

        double avg = weightedCounts.stream().reduce(0.0, Double::sum)
                / weightedCounts.size();
        double variance = weightedCounts.stream()
                .mapToDouble(d -> (d - avg) * (d - avg)).sum()
                / weightedCounts.size();
        return variance;
    }
	
}