public class studentAI extends Player {
	private int maxDepth;

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public void move(BoardState state) {
			move = alphabetaSearch(state, maxDepth);
		
	}

	public int alphabetaSearch(BoardState state, int maxDepth) {
		
		int action = -1;
		int v = Integer.MIN_VALUE;
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		for (int i = 0; i < 6; i++) {
			BoardState current = new BoardState(state);
			if (state.isLegalMove(1, i)) {
				
				v = Math.max(v, minValue(current.applyMove(1, i), maxDepth, 1 , alpha, beta));
				
				if (alpha < v) {
					action = i;
					alpha = v;
				}
				if(alpha>=beta){
					break;
				}
			}
		}
		return action;
	}

	public int maxValue(BoardState state, int maxDepth, int currentDepth, int alpha, int beta) {
		
		if (currentDepth == maxDepth || state.status(2) != Integer.MIN_VALUE) {
			return sbe(state);
		}

		int v = Integer.MIN_VALUE;
		for (int i = 0; i < 6; i++) {
			BoardState current = new BoardState(state);
			if (current.isLegalMove(1, i)) {
				v = Math.max(v, minValue(current.applyMove(1, i), maxDepth, currentDepth + 1, alpha, beta));
			}
			
			if (v >= beta) {
				return v;
			}
			alpha = Math.max(alpha, v);
			if(beta<= alpha){
				break;
			}
		}
		return v;

	}

	public int minValue(BoardState state, int maxDepth, int currentDepth, int alpha, int beta) {
		if (currentDepth == maxDepth || state.status(1) != Integer.MIN_VALUE) {
			return sbe(state);
		}
		int v = Integer.MAX_VALUE;
		for (int i = 7; i < 13; i++) {
			BoardState current = new BoardState(state);
			if (state.isLegalMove(2, i)) {
				v = Math.min(v, maxValue(current.applyMove(2, i), maxDepth, currentDepth + 1, alpha, beta));
			}
			if (v <= alpha) {
				return v;
			}
			beta = Math.min(beta, v);
			if(beta<=alpha){
				break;
			}
		}
		return v;
	}

	public int sbe(BoardState state) {
		return state.score[0] - state.score[1];
	}

}