public class Bishop extends Piece{
    public Bishop(int rank, int file, boolean turn){
        pos[0] = rank;
        pos[1] = file;
        name = "B";
        color = turn;
        if(!color){
            FEN = "b";
            image = "blackBishop.png";
        }
        else{
            FEN = "B";
            image = "whiteBishop.png";
        }
    }

    public void addMoves(){
        bishopMove();
    }
}