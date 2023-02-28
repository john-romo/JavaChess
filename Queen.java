public class Queen extends Piece {
    public Queen(int rank, int file, boolean turn){
        pos[0] = rank;
        pos[1] = file;
        name = "Q";
        color = turn;
        if(!color){
            FEN = "q";
            image = "blackQueen.png";
        }
        else{
            FEN = "Q";
            image = "whiteQueen.png";
        }
    }

    public void addMoves(){
        rookMove();
        bishopMove();
    }
}