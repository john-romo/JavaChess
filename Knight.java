public class Knight extends Piece{
    public Knight(int rank, int file, boolean turn){
        pos[0] = rank;
        pos[1] = file;
        name = "N";
        color = turn;
        if(!color){
            FEN = "n";
            image = "blackKnight.png";
        }
        else{
            FEN = "N";
            image = "whiteKnight.png";
        }
    }

    public void addMoves(){
        int[] vmod = {-2,-1,1,2};
        int[] hmod = {-2,-1,1,2};

        for(Integer v: vmod) {
            for (Integer h : hmod) {

                int rank = pos[0];
                int file = pos[1];

                if(Math.abs(v) != Math.abs(h)){

                    int count = 0;

                    if (rank+v < 8 && rank+v > -1){
                        rank = rank+v;
                        count++;
                    }
                    if (file+h < 8 && file+h > -1){
                        file = file+h;
                        count++;
                    }

                    if(moves.notContains(rank, file) && count == 2){
                        Piece select = Board.board[rank][file];
                        if(select instanceof King && select.color != color){
                            select.check = true;
                        }
                        if (color) {
                            select.attWhite = true;
                            if (select.square || !select.color)
                                moves.add(new int[]{select.pos[0], select.pos[1]});
                            Board.whiteMoves = true;
                        } else {
                            select.attBlack = true;
                            if (select.square || select.color)
                                moves.add(new int[]{select.pos[0], select.pos[1]});
                            Board.blackMoves = true;
                        }
                    }
                }
            }
        }
    }
}
