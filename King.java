public class King extends Piece{
    public King(int rank, int file, boolean turn){
        pos[0] = rank;
        pos[1] = file;
        castle = true;
        name = "K";
        color = turn;
        if(!color){
            FEN = "k";
            Game.kings[0] = this;
            image = "blackKing.png";
        }
        else{
            FEN = "K";
            Game.kings[1] = this;
            image = "whiteKing.png";
        }
    }

    public void addMoves(){
        int[] vmod = {-1,0,1};
        int[] hmod = {-1,0,1};

        for(Integer v: vmod) {
            for (Integer h : hmod) {
                int rank = pos[0];
                int file = pos[1];

                if (rank + v < 8 && rank + v > -1) rank = rank + v;
                if (file + h < 8 && file + h > -1) file = file + h;

                if ((rank != pos[0] || file != pos[1]) && moves.notContains(rank, file)) {
                    Piece dest = Board.board[rank][file];
                    if(color && !dest.attBlack) {
                        if(dest.square || !dest.color)
                            moves.add(new int[]{dest.pos[0], dest.pos[1]});
                        Board.whiteMoves = true;
                    }
                    else {
                        if (!dest.attWhite) {
                            if (dest.square || dest.color)
                                moves.add(new int[]{dest.pos[0], dest.pos[1]});
                            Board.blackMoves = true;
                        }
                    }
                }
            }
        }
        if(castle){
            boolean cont = true;
            for(int i = 1; i < 4; i++){
                if(cont){
                    Piece select = Board.board[pos[0]][pos[1] + i];
                    if(!select.square || select instanceof Rook) cont = false;
                    if(select.square){
                        if (!color && select.attWhite) cont = false;
                        else if(color && select.attBlack) cont = false;
                    }
                    if(select.castle) moves.add(new int[] {pos[0], pos[1]+2});
                }
            }
            cont = true;

            for(int i = -1; i > -5; i--){
                if(cont){
                    Piece select = Board.board[pos[0]][pos[1] + i];
                    if(!select.square || select instanceof Rook) cont = false;
                    if(select.square && i != -3){
                        if(!color && select.attWhite) cont = false;
                        else if(color && select.attBlack) cont = false;
                    }
                    if(select.castle) moves.add(new int[] {pos[0], pos[1]-2});
                }
            }
        }
    }
}