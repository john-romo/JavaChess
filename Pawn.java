public class Pawn extends Piece{
    public Pawn(int rank, int file, boolean turn){
        pos[0] = rank;
        pos[1] = file;
        name = String.valueOf((char)(pos[1] + 97));
        firstMove = true;
        color = turn;
        if(!color){
            FEN = "p";
            image = "blackPawn.png";
        }
        else{
            FEN = "P";
            image = "whitePawn.png";
        }
    }

    public void addMoves(){
        int rank = pos[0];
        int file = pos[1];

        int vmod;
        int rhmod = 1;
        int lhmod = 1;

        if(!color) vmod = 1;
        else vmod = -1;
        if (file == 0) lhmod = 0;
        if (file == 7) rhmod = 0;

        Piece left = Board.board[rank+vmod][file-lhmod];
        Piece right = Board.board[rank+vmod][file+rhmod];

        if(!color && left != this) left.attBlack = true;
        else if (color && left != this) left.attWhite = true;

        if(!color && right != this) right.attBlack = true;
        else if (color && right != this)right.attWhite = true;

        if(!left.square && left.color != color){
            moves.add(new int[] {left.pos[0], left.pos[1]});
            if(left instanceof King){
                left.check = true;
            }
        }
        if(!right.square && right.color != color){
            moves.add(new int[] {right.pos[0], right.pos[1]});
            if(right instanceof King) {
                right.check = true;
            }

        }

        if(Board.board[rank+vmod][file].square) {
            moves.add(new int[] {rank+vmod,file});
            if (firstMove && Board.board[rank+(vmod*2)][file].square){
                moves.add(new int[] {rank+vmod*2, file});
            }
        }

        left = Board.board[rank][file-lhmod];
        right = Board.board[rank][file+rhmod];

        if (left.enP && !left.color == color){
            moves.add(new int[] {rank+vmod,file-lhmod});
        }
        if (right.enP && !right.color == color){
            moves.add(new int[] {rank+vmod,file+rhmod});
        }
        if(moves.size() != 0 && color) Board.whiteMoves = true;
        if(moves.size() != 0 && !color) Board.blackMoves = true;

    }
}
