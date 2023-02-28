public class Square extends Piece{
    public Square(int rank, int file){
        pos[0] = rank;
        pos[1] = file;
        name = rank + "" + file + "square";
        square = true;
        color = (rank + file) % 2 == 0;
    }
}
