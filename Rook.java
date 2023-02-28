public class Rook extends Piece{
    public Rook(int rank, int file, boolean turn){
        pos[0] = rank;
        pos[1] = file;
        name = "R";
        castle = true;
        color = turn;
        if(!color){
            FEN = "r";
            image = "blackRook.png";
        }
        else{
            FEN = "R";
            image = "whiteRook.png";
        }
    }

    public void addMoves(){
        rookMove();
    }
}