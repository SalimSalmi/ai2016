package ai2016;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Deadline;
import negotiator.DeadlineType;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.session.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;

import java.util.List;

/**
 * This is your negotiation party.
 */
public class Group14 extends AbstractNegotiationParty {

	private final float MINIMUM_UTILITY = 0.8f;
	private final float FINAL_MODEL_TIME = 0.2f; //Deadline to fix the opponent model
	private final float MEAN_MODEL_TIME = 0.6f; //Deadline to start calculating the mean model
	private final float CONCEDE_TIME = 0.9f; //Deadline to hard concede
	private final int REFRESH_MEAN = 10; //Amount of times the mean model is being refreshed.

	private Bid lastReceivedBid = null;

	private OpponentList opponents = new OpponentList();
	private AcceptanceStrategy acceptanceStrategy;
	private BiddingStrategy biddingStrategy;

	private int roundNo = 0;

	@Override
	public void init(AbstractUtilitySpace utilSpace, Deadline dl,
			TimeLineInfo tl, long randomSeed, AgentID agentId) {

		super.init(utilSpace, dl, tl, randomSeed, agentId);

		DeadlineType type = dl.getType();

		System.out.println("Discount Factor is "
				+ utilSpace.getDiscountFactor());
		System.out.println("Reservation Value is "
				+ utilSpace.getReservationValueUndiscounted());

		// if you need to initialize some variables, please initialize them
		// below

		acceptanceStrategy = new AcceptanceStrategy(utilSpace, MINIMUM_UTILITY, opponents);
		biddingStrategy = new BiddingStrategy(utilSpace, MINIMUM_UTILITY, opponents);

	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {


		// If the utility of the proposed bid is lower than the minimum acceptable
		// utility then counter offer.
		// if we are the first party, also offer.
		if (lastReceivedBid == null || !validActions.contains(Accept.class)
				|| !acceptanceStrategy.accept(lastReceivedBid)) {

			Bid bid;

			roundNo = roundNo +1;

			if(roundNo == 200){

				OpponentModel oppAvg = opponents.getAverageOpponentModel(getUtilitySpace());

				try {
					System.out.println("The max utility bid is :"+ oppAvg.getUtilSpace().getMaxUtilityBid());
				} catch (Exception e) {
					e.printStackTrace();
				}


			}

			do{
				//bid = biddingStrategy.getNextBid();

			bid = generateRandomBid();

			}while(getUtility(bid) < MINIMUM_UTILITY);

			return new Offer(getPartyId(), bid);
		} else {

			return new Accept(getPartyId(), lastReceivedBid);
		}

	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action. Can be null.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);

		if (action instanceof Offer) {
			lastReceivedBid = ((Offer) action).getBid();
		}

		if(sender != null) {
			OpponentModel opponent = opponents.getOpponent(sender, getUtilitySpace());

			if (action instanceof Offer) {
				Bid bid = ((Offer) action).getBid();
				opponent.pushBid(bid);
			}

			if (action instanceof Accept) {
				Bid bid = ((Accept) action).getBid();
				opponent.pushBid(bid);
			}
		}

	}


	@Override
	public String getDescription() {
		return "Party group 14 v0.0.10";
	}

}
