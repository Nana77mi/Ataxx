package ataxx;

import java.util.ArrayList;
import java.util.List;

import static ataxx.PieceState.EMPTY;

// Final Project Part A.2 Ataxx AI Player (A group project)

/** A Player that computes its own moves. */
class AIPlayer extends Player {

    
    /** A new AIPlayer for GAME that will play MYCOLOR.
     *  SEED is used to initialize a random-number generator,
	 *  increase the value of SEED would make the AIPlayer move automatically.
     *  Identical seeds produce identical behaviour. */
    AIPlayer(Game game, PieceState myColor, long seed) {
        super(game, myColor);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getAtaxxMove() {
        Move move = findMove();
        getAtaxxGame().reportMove(move, getMyState());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(getAtaxxBoard());
        lastFoundMove = null;

        // Here we just have the simple AI to randomly move.
        // However, it does not meet with the requirements of Part A.2.
        // Therefore, the following codes should be modified
        // in order to meet with the requirements of Part A.2.
        // You can create add your own method and put your method here.

        minmax(b, DEEP_LIMIT, getMyState(), -BOUND, BOUND);

        // Please do not change the codes below
        if (lastFoundMove == null) {
            lastFoundMove = Move.pass();
        }
        return lastFoundMove;
    }


    /** The move found by the last call to the findMove method above. */
    private Move lastFoundMove;
    private List<Move> lastFoundMoveList;


    /** Return all possible moves for a color.
     * @param board the current board.
     * @param myColor the specified color.
     * @return an ArrayList of all possible moves for the specified color. */
    private ArrayList<Move> possibleMoves(Board board, PieceState myColor) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        for (char row = '7'; row >= '1'; row--) {
            for (char col = 'a'; col <= 'g'; col++) {
                int index = Board.index(col, row);
                if (board.getContent(index) == myColor) {
                    ArrayList<Move> addMoves
                            = assistPossibleMoves(board, row, col);
                    possibleMoves.addAll(addMoves);
                }
            }
        }
        return possibleMoves;
    }

    /** Returns an Arraylist of legal moves.
     * @param board the board for testing
     * @param row the row coordinate of the center
     * @param col the col coordinate of the center */
    private ArrayList<Move>
        assistPossibleMoves(Board board, char row, char col) {
        ArrayList<Move> assistPossibleMoves = new ArrayList<>();
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                if (i != 0 || j != 0) {
                    char row2 = (char) (row + j);
                    char col2 = (char) (col + i);
                    Move currMove = Move.move(col, row, col2, row2);
                    if (board.moveLegal(currMove)) {
                        assistPossibleMoves.add(currMove);
                    }
                }
            }
        }
        return assistPossibleMoves;
    }

    /** Choose the difference between the number of your own pieces and
     *  the number of your opponent's pieces as the scoring criteria*/
    private int getScore(Board board) {
        PieceState winner = board.getWinner();
        if(winner != null){
            if (winner.equals(EMPTY)) {
                return 0;
            } else if (winner.equals(getMyState())) {
                return BOUND;
            } else {
                return -BOUND;
            }
        }
        return board.getColorNums(board.nextMove()) - board.getColorNums(board.nextMove().opposite());
    }

    // The default minmax value of the board.
    // The aim is to find the maximum and minimum values by comparison.
    private static final int BOUND = 9999;

    // The upper limit of search depth, minmax algorithm complexity is n!
    // Each increment of depth is costly
    public static final int DEEP_LIMIT = 4;

    // alpha beta is the bound of search
    /** Depth-first searching all the possible cases of the future steps of the game,
     * constantly updating the lastFoundMove.
     * @param board the current board.
     * @param depth the current depth in the decision tree, the default value for which is the DEEP_LIMIT
     * @param currentState the state of current player.
     * @param alpha lower bound of the search.
     * @param beta upper bound of the search.
     * @return currScore */
    private int minmax(Board board, int depth, PieceState currentState, int alpha, int beta) {

        // When depth is reached or the game is over, return to score immediately.
        if (depth == 0 || board.getWinner() != null) {
            return getScore(board);
        }

        // Record the maximum or minimum score so far.
        // The initial value of (solo and remain the best one) to find the maximum or minimum value.
        int currScore = getMyState().equals(currentState) ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // Get all possible moves and analyze them.
        ArrayList<Move> listOfMoves = possibleMoves(board, board.nextMove());
        for (Move move : listOfMoves) {
            // Generate the board after this move.
            Board currBoard = new Board(board);
            currBoard.createMove(move);
            // dfs get the best score for this move
            int newScore = minmax(currBoard, depth - 1, currentState.opposite(), alpha, beta);

            // For our turn, the maximum value is required,
            if (getMyState().equals(currentState)) {
                // Only the first move is recorded, and
                // subsequent moves are only used to build the minmax tree
                if (depth == DEEP_LIMIT && newScore > currScore) {
                    // record the move
                    lastFoundMove = move;
                }
                // update score
                currScore = Math.max(newScore, currScore);
                // update bound
                alpha = Math.max(alpha, currScore);

            } else { // For opposite's turn, the minimum value is required.
                currScore = Math.min(newScore, currScore);
                beta = Math.min(beta, currScore);
            }
            // alpha-beta pruning algorithm
            if (beta <= alpha) {
                break;
            }
        }
        if (Math.abs(currScore) > BOUND) return 0;
        return currScore;

    }

}
