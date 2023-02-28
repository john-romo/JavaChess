// handles all things shared by piece subclasses, plus a few thing only shared by a few.
// remember, a queen's move is just a rook's move plus a bishop's move.

import java.util.ArrayList;

public class Piece {
    String image;
    String name;
    String FEN;
    boolean color; // <-- boolean works way better than string for black/white
    boolean square = false; // <-- so I don't have to keep typing 'instanceof'
    int[] pos = {0,0};
    MoveList moves =  new MoveList(); // <-- list of all legal moves
    ArrayList<int[]> pieceCheckingLine = new ArrayList<>(); // <-- if ranging piece attacks king...
    boolean firstMove; // <-- for pawn double-hop
    boolean enP; // <-- en passant
    boolean castle;
    boolean check;
    boolean attWhite; // <-- if piece/square attacked by white, attWhite = true;
    boolean attBlack;

    // maybe could be split into multiple functions? maybe.  returns true if move is legal/makes the move
    // starts making the chess notation for the turn.
    public boolean move(int[] start, int[] end){
        boolean castled = false;
        String castledStr = ""; // <-- O-O, O-O-O
        boolean enPCap = false; // <-- remember, enP captures are unique

        Game.turnNotation = "";

        if(!moves.legal(end)){
            return false;
        }
        else{
            if (this instanceof Pawn) {
                if (firstMove && (end[0] == start[0] + 2 || end[0] == start[0] - 2)) enP = true;
                firstMove = false;
                if (Board.board[end[0]][end[1]].square && end[1] != pos[1]) {

                    enPCap = true;
                    int mod;
                    if (!color) mod = -1;
                    else mod = 1;
                    Board.board[end[0] + mod][end[1]] = new Square(end[0] + mod, end[1]);
                }
                if(end[0] == 0 || end[0] == 7) {
                    Game.inPromotion = true;
                    UI.createAndShowPromotePop(start, end, Board.board[start[0]][start[1]]);
                    return true;
                }
            }
            // add castling notiation (eg: O-O)
            else if(this instanceof King){
                 if(castle){
                     if(end[1] == start[1]+2) {
                         castled = true;
                         castledStr = "O-O";
                         Board.board[start[0]][start[1] + 1] = new Rook(start[0], start[1] + 1, color);
                         Board.board[start[0]][start[1] + 1].castle = false;
                         Board.board[start[0]][start[1] + 3] = new Square(start[0], start[1]+3);
                     }
                     else if(end[1] == start[1]-2) {
                         castled = true;
                         castledStr = "O-O-O";
                         Board.board[start[0]][start[1] - 1] = new Rook(start[0], start[1] - 1, color);
                         Board.board[start[0]][start[1] - 1].castle = false;
                         Board.board[start[0]][start[1] - 4] = new Square(start[0], start[1]-4);
                     }
                 }
                 castle = false;
            }
            else if(this instanceof Rook) castle = false;
            if(!Game.inPromotion) normalTurnEnd(start, end, enPCap, castled, castledStr);
            return true;
        }
    }

    // ends non-promotion move, adds capture notation (eg: x)
    public void normalTurnEnd(int[] start, int[] end, boolean enPCap, boolean castled, String castledStr){
        boolean capture = !(Board.board[end[0]][end[1]].square) || enPCap ;

        if(!castled){
            if(!(this instanceof Pawn) || capture)Game.turnNotation += name;
            if(capture) Game.turnNotation += 'x';
            Game.turnNotation += ((char)(end[1] + 97));
            Game.turnNotation +=  -(end[0]-8);
        }
        else Game.turnNotation += castledStr;

        Board.board[end[0]][end[1]] = this;
        if(this instanceof Pawn) name = String.valueOf((char)((end[1]) + 97));
        Board.board[start[0]][start[1]] = new Square(start[0],start[1]);
        pos = end;
    }

    // check out them mods.  great idea, I must say.  when I tried this on python the functions
    // were stupid long because I didn't think of using mods.
    public void rookMove(){
        int[][] mods = {{1,0},{0,1}};
        int[] directions = {-1,1};

        for(int[] mod: mods){
            for(Integer dir: directions){
                int rank = pos[0];
                int file = pos[1];
                boolean cont = true;

                while(cont){
                    int v = dir*mod[0];
                    int h = dir*mod[1];

                    if(rank+v < 8 && rank+v > -1) rank = rank+v;
                    else cont = false;
                    if(file+h < 8 && file+h > -1) file = file+h;
                    else cont = false;

                    if(cont){
                        cont = bishopRookAttack(rank, file);
                    }

                }
            }
        }
    }

    // those sweet, sweet mods...
    public void bishopMove(){
        int[][] mods = {{1,-1},{-1,1},{1,1},{-1,-1}};

        for(int[] mod: mods){
            int rank = pos[0];
            int file = pos[1];
            boolean cont = true;
            while(cont){
                int v = mod[0];
                int h = mod[1];

                if(rank+v < 8 && rank+v > -1) rank = rank+v;
                else cont = false;
                if(file+h < 8 && file+h > -1) file = file+h;
                else cont = false;

                if(cont){
                    cont = bishopRookAttack(rank, file);
                }
            }
        }
    }

    // the moves are different, but gathering the attacks is exactly the same...
    public boolean bishopRookAttack(int rank, int file){
        Piece select = Board.board[rank][file];
        if(color) select.attWhite = true;
        else select.attBlack = true;

        if(!(select.square)){
            if(select.color != color){
                moves.add(select.pos);
                if(select instanceof King){
                    select.check = true;
                    Game.allCheckingLines.addAll(pieceCheckingLine);
                }
            }
            pieceCheckingLine.clear();
            return false;
        }
        else if(select.pos[0] == 0 || select.pos[1]==0 || select.pos[0] == 7 || select.pos[1] == 7){
            moves.add(new int[] {rank,file});
            pieceCheckingLine.clear();
        }
        else {
            moves.add(select.pos);
            pieceCheckingLine.add(new int[]{rank, file});

        }
        return true;
    }

    public void addMoves(){/*Overide by subclasses*/}
}
