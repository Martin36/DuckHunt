import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

class Player {
		HMM hmm;
		double probOfEmissionSeq;
		boolean trainModel;
    public Player() {
    	hmm = new HMM(3,3);
    	probOfEmissionSeq = 0;
    	trainModel = true;
    }

    /**
     * Shoot!
     *
     * This is the function where you start your work.
     *
     * You will receive a variable pState, which contains information about all
     * birds, both dead and alive. Each bird contains all past moves.
     *
     * The state also contains the scores for all players and the number of
     * time steps elapsed since the last time this function was called.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return the prediction of a bird we want to shoot at, or cDontShoot to pass
     */
    public Action shoot(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to get the best action.
         * This skeleton never shoots.
         */
    	int noObs = pState.getBird(0).getSeqLength();
    	//Doesn't do anything if the nr of observations
    	//ain't large enough. 
    	if(noObs < 10){
    		return cDontShoot;
    	}
    	int noBirds = pState.getNumBirds();
    	int[][] observations = new int[noBirds][noObs];
    	for(int i = 0; i < noBirds; i++){
    		for(int j = 0; j < noObs; j++){
    			observations[i][j] = pState.getBird(i).getObservation(j)-3;
    		}
      	//Check if training the model will improve it.
    		if(trainModel){
      		hmm.estimateModel(observations[i]);
      		double estProb = hmm.estimateProbabilityOfEmissionSequence(observations[i]);
      		if(estProb > probOfEmissionSeq){
      			probOfEmissionSeq = estProb;
      		}else{
      			trainModel = false;
      		}
    		}
    		
    	}
    	//Now we want to check which bird to shoot
    	int [] lastMoves = new int[noBirds];
    	double [] currentStateDist = new double[hmm.getNrOfEmissions()];
    	//First we calculate how many birds at each position
    	for(int i = 0; i < noBirds; i++){
    		lastMoves[i] = pState.getBird(i).getLastObservation();
    		currentStateDist[lastMoves[i]] += 1;
    	}
    	//Then we make the list to hold the probability
    	//of a bird being in position i
    	for(int i = 0; i < currentStateDist.length; i++){
    		currentStateDist[i] = currentStateDist[i]/noBirds;
    	}
    	//Here we estimate the probabilities of the birds positions
    	//in the next state
    	double [] estimatedDistOfNextState = new double[currentStateDist.length];
    	estimatedDistOfNextState = hmm.estimateProbabilityDistributionOfNextEmission(currentStateDist);
    	//Now we want to check which of these probabilities are the largest
    	int nextMove = 0;
    	double maxDistOfNextState = 0;
    	for(int i = 0; i < estimatedDistOfNextState.length; i++){
    		double maxTemp = estimatedDistOfNextState[i];
    		if (maxTemp > maxDistOfNextState){
    			maxDistOfNextState = maxTemp;
    			nextMove = i;
    		}
    	}
    	//Now we need to check which bird has the highest probability
    	//to move in the direction which was the maximum
    	double [] emiSeqProb = new double[noBirds];
    	for(int i = 0; i < noBirds; i++){
    		//First we make the emission sequence
    		int [] emiSeq = new int[2];
    		emiSeq[0] = pState.getBird(i).getLastObservation();
    		emiSeq[1] = nextMove;
    		emiSeqProb[i] = hmm.estimateProbabilityOfEmissionSequence(emiSeq);
    	}
    	//We need to find the maximized sequence probability
    	double maxProb = 0;
    	int birdIndex = 0;
    	for(int i = 0; i < emiSeqProb.length; i++){
    		double maxProbTemp = emiSeqProb[i];
    		if(maxProbTemp > maxProb){
    			maxProb = maxProbTemp;
    			birdIndex = i;
    		}
    	}
    	//There has to be a limit of probability so
    	//we don't shoot if the probability ain't high enough
    	double minProb = 0.7;
    	if(maxProb >= minProb){
    		return new Action(birdIndex,nextMove);
    	}
    	
/*    	FileWriter fr = null;
    	try {
			fr = new FileWriter(new File("observations.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	int o;
    	//Writes down all observations in a file.
    	for(int i = 0; i < pState.getNumBirds(); i++){
        	o = pState.getBird(i).getLastObservation();
        	try {
				fr.write(o);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
    	}
*/		
        // This line chooses not to shoot.
        return cDontShoot;

        // This line would predict that bird 0 will move right and shoot at it.
        // return Action(0, MOVE_RIGHT);
    }

    /**
     * Guess the species!
     * This function will be called at the end of each round, to give you
     * a chance to identify the species of the birds for extra points.
     *
     * Fill the vector with guesses for the all birds.
     * Use SPECIES_UNKNOWN to avoid guessing.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return a vector with guesses for all the birds
     */
    public int[] guess(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to guess the species of
         * each bird. This skeleton makes no guesses, better safe than sorry!
         */

        int[] lGuess = new int[pState.getNumBirds()];
        for (int i = 0; i < pState.getNumBirds(); ++i)
            lGuess[i] = Constants.SPECIES_UNKNOWN;
        return lGuess;
    }

    /**
     * If you hit the bird you were trying to shoot, you will be notified
     * through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pBird the bird you hit
     * @param pDue time before which we must have returned
     */
    public void hit(GameState pState, int pBird, Deadline pDue) {
        System.err.println("HIT BIRD!!!");
    }

    /**
     * If you made any guesses, you will find out the true species of those
     * birds through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pSpecies the vector with species
     * @param pDue time before which we must have returned
     */
    public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {
    }

    public static final Action cDontShoot = new Action(-1, -1);
}
