package negotiator.group4;

import java.util.List;

import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;

/*
 * This is an agent for multi-party negotiation
 */

public class Group4 extends AbstractNegotiationParty{
	
	/*
	 * opponentAction ------ the most recent Action opponents performed.
	 */
	private Action opponentAction;
	/*
	 * MINIMUM_BID_UTILITY ------ the minimum acceptable utility of a bid, decrease over time
	 */
	private double MINIMUM_BID_UTILITY;
	
	public Group4(){
		MINIMUM_BID_UTILITY = 1.0;
	}
	
	/*
	 * initiate the agent
	 * When the negotiation start, assume an agent want to reach its highest utility
	 * So set the MINIMUM_BID_UTILITY to 1
	 */
	public void init(){
		MINIMUM_BID_UTILITY = 1.0;
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see negotiator.parties.AbstractNegotiationParty#getDescription()
	 * Return the description of the agent
	 */
    public String getDescription() {
        return "Group4MultiPartyNegotiationAgent";
    }
	

	@Override
	/*
	 * (non-Javadoc)
	 * @see negotiator.parties.NegotiationParty#chooseAction(java.util.List)
	 * When the validActions list doesn't contain "Accept", which means there is no bid currently,
	 * offer a bid that is higher than the MINIMUN_BID_UTILITY
	 * When the validActions list contains "Accept" then 
	 * accept this bid if it was acceptable, otherwise offer a new bid; 
	 */
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		for(int i = 0;i < validActions.size();i ++){
			System.out.println("Valid Actions" + i + ":" + validActions.get(i).toString());
		}
		if(!validActions.contains(Accept.class)){
			return offerBid();
		}
		else{
			if(isAcceptable(opponentAction))
				return new Accept();
			else
				return offerBid();
		}
	}
	
	/*
	 * Determine whether a bid is acceptable
	 * 1. return true when the utility is higher or equal to than the MINIMUM_BID_UTILITY
	 * 2. return false When the utility is lower than the MINIMUM_BID_UTILITY
	 */
	public boolean isAcceptable(Action action){
		Bid opponentBid = Action.getBidFromAction(opponentAction);
		if(opponentAction != null && getUtility(opponentBid) >= MINIMUM_BID_UTILITY){
			return true;
		}
		else{
			//System.out.println("Current time:" + timeline.getCurrentTime() + "  Reservation value:" + utilitySpace.getReservationValue());
			/*
			 * The MINIMUM_BID_UTILITY decrease over time. Set MINIMUM_BID_UTILITY to 1 - time spent
			 * The lower limit of MINIMUM_BID_UTILITY is 0.7
			 */
			if(timeline.getType().toString().equals("Rounds")){
				MINIMUM_BID_UTILITY = Math.max((1 - timeline.getCurrentTime()/timeline.getTotalTime()), 0.7);
			}
			else{
				MINIMUM_BID_UTILITY = Math.max((1 - timeline.getCurrentTime()),0.7) ;
			}
			for(int i = 0; i < utilitySpace.getDomain().getObjectives().size(); i ++){
				System.out.println(utilitySpace.getDomain().getObjective(i).toString());
			}
			return false;
		}
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see negotiator.parties.AbstractNegotiationParty#receiveMessage(java.lang.Object, negotiator.actions.Action)
	 * receive opponents' last action
	 */
	public void receiveMessage(Object sender, Action action) {
		super.receiveMessage(sender, action);
		this.opponentAction = action;
		System.out.println(opponentAction.toString());
	}
	
	/*
	 * Use Random Walker to choose a bid
	 * the utility of the chosen bid should be higher than the MINIMUM_BID_UTILITY
	 * (agent should offer a bid that is acceptable to itself)
	 */
	public Action offerBid(){
		System.out.println("Minmum Utility = " + MINIMUM_BID_UTILITY);
		Bid bid = utilitySpace.getDomain().getRandomBid();
		int loop = 0;
		while(loop <= 10000 && getUtility(bid) < MINIMUM_BID_UTILITY){
			bid = utilitySpace.getDomain().getRandomBid();
			loop ++;
		}
		if(loop < 10000){
			System.out.println(this.getDescription() + ":Found bid in 10000 loops, Utility =" + getUtility(bid));
			return new Offer(bid);
		}
		else{
			try {
				System.out.println(this.getDescription() + ": Return maxUtilityBid, Utility =" + getUtility(bid));
				return new Offer(utilitySpace.getMaxUtilityBid());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	}
}
