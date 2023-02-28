import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Game {
    static final String startPos = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0";
    // static final String startPos = "5k2/8/3K4/8/4P3/8/8/8 b - - 8 36"; // <-- test pos
    static PositionList lines = new PositionList(startPos,"START"); // <-- Linked list of positions
    static King[] kings = new King[2];
    static King enemyKing; // <-- helps with check/checkmate
    static King friendlyKing; // ^^^
    static ArrayList<int[]> allCheckingLines = new ArrayList<>(); // <-- part of the way I implement reveal-check
    static boolean turn = true; // <-- true: white to move, false: black to move
    static int fullMove = 0; //
    static int fiftyMoveCount = 0;
    static String turnNotation = "START"; // <-- chess notation
    static boolean inPromotion = false; // <-- pauses things so user can pick which piece to promote to
    static int[] startCoords;
    static int[] endCoords;
    static String gameState = ""; // <-- various info like "king in check" or "illegal move"
	static boolean engineActive = false;

    // Sets everything up in default start position
    public static void main(String[] args) throws IOException {
        Board.readFEN(startPos);
        Board.getMoves();
        setKings();
        UI.ui();
        if(engineActive) Engine.analysesMode = true;
        if(engineActive) Engine.engage();
    }

    // the function to cary out a trun.
    public static void turn() throws IOException {
        if(!lines.checkEndGame()){
            allCheckingLines.clear();
            Board.getMoves();
            setKings();

            Piece select = Board.board[startCoords[0]][startCoords[1]];
            boolean turnEnd = false;
            friendlyKing.check = false;

            if(!select.square && select.color == turn){
                boolean capture = !Board.board[endCoords[0]][endCoords[1]].square;

                // Piece move function returns a true value of start/end coordinates are legal
                if(select.move(startCoords, endCoords) && !inPromotion){
                    // checkForCheckAndMate returns false if friendly king is in check
                    turnEnd = checkForCheckAndMate(select, capture);
                }
                else if (!Arrays.equals(startCoords, endCoords) && !inPromotion) gameState = "Illegal Move!";
                if(turnEnd){
                    turnEnd();
                }
                // if move is illegal, restart turn from initial position
                else if (!inPromotion) lines.setBoard();
            }
            if(!inPromotion) nullifyStartEnd();
            lines.checkEndGame();
            UI.refreshMenuBar();
        }
        else UI.refreshGameStateBar();
    }

    // bypasses anything in the turn function that isn't a valid concern for a promotion move
    public static void promotionMove(Piece select) throws IOException {
        setKings();
        if(checkForCheckAndMate(select, false)) turnEnd();
        else lines.setBoard();

        nullifyStartEnd();
        lines.checkEndGame();
        UI.refreshMenuBar();
    }

    // sets enemy and friendly kings
    public static void setKings(){
        if(turn){
            enemyKing = kings[0];
            friendlyKing = kings[1];
        }
        else {
            enemyKing = kings[1];
            friendlyKing = kings[0];
        }
    }

    // if friendly king isn't in check, returns true. adjusts chess notation for check/checkmate (eg: e4+)
    public static boolean checkForCheckAndMate(Piece select, boolean capture){
        Board.getMoves();
        setKings();

        if(enemyKing.check && !checkMate()) turnNotation += '+';
        if(!checkMate()){
            if(!friendlyKing.check){
                if(!(select instanceof Pawn) && !capture)  fiftyMoveCount++;
                else fiftyMoveCount = 0;
                return true;
            }
            else gameState = "You're king is in check!";
        }
        else{
            turnNotation += '#';
            lines.append(Board.getFEN(), turnNotation, -1);
        }
        return false;
    }

    // tries to find a move which can block check. only called if king has no moves and is in check
    public static boolean blocksCheck(){
        for(Piece[] rank: Board.board){
            for(Piece file: rank){
                if(!file.square && file.color != turn){
                    file.addMoves();
                    for(int i = 0; i < file.moves.size(); i++){
                        for (int[] move : allCheckingLines) {
                            if (Arrays.equals(file.moves.get(i), move)) return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // switches turn, set all pawn's en passant to false
    public static void changeTurn(){
        for(int rank = 0; rank < 8; rank++){
            for(int file = 0; file < 8; file++){
                Piece pawn = Board.board[rank][file];
                if(pawn instanceof Pawn && pawn.color != turn){
                    pawn.enP = false;
                }
            }
        }
        if (!turn) fullMove++;

        turn = !turn;
    }

    // resets start and end move coordinates
    public static void nullifyStartEnd(){
        startCoords = null;
        endCoords = null;
    }

    // calls changeTurn and adds position to position list
    public static void turnEnd() {
        changeTurn();
        lines.append(Board.getFEN(), turnNotation, -1);
    }

    public static boolean checkMate(){
        if(!enemyKing.check || enemyKing.moves.size() != 0) return false;
        if(blocksCheck()) return false;
        String color;
        if(!enemyKing.color) color = "White";
        else color = "Black";
        int turnNum = fullMove+1;
        gameState = color + " wins by Checkmate in " + turnNum + "! ";
        return true;
    }
}
