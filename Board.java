// is there any reason to instatiate a class that is just there, like it doesn't have multiple instatiations?

import java.util.ArrayList;

public class Board {
    static Piece[][] board = new Piece[8][8];
    static ArrayList<Piece> pieces = new ArrayList<>(); // <-- used for insufficient (eg: theres only a bishop...)
    static boolean whiteMoves; // <-- used for stalemate (eg: blackMoves = false, so black can't move)
    static boolean blackMoves;

    // gets all legal moves
    public static void getMoves(){
        newPos();
        pieces.clear();
        Game.allCheckingLines.clear();
        whiteMoves = false;
        blackMoves = false;
        for(Piece[] file: board){
            for(Piece piece: file) {
                piece.moves.clear();
                if(!piece.square && !(piece instanceof King)) {
                    pieces.add(piece);
                    piece.addMoves();
                }
            }
        }
        kingAttack();
    }

    // at first this didn't exist.  then I tried to implement stalemate and saw that the kings
    // must set their attacked squares AFTER their move squares, so that one king can't move
    // into a square attacked by the other king / capture a piece defended by the other king.
    private static void kingAttack(){
        for(King king: Game.kings){
            int[] vmod = {-1,0,1}; // <-- using these mods for getting legal moves is choice imo
            int[] hmod = {-1,0,1};

            for(Integer v: vmod) {
                for (Integer h : hmod) {
                    int rank = king.pos[0];
                    int file = king.pos[1];

                    if (rank + v < 8 && rank + v > -1) rank = rank + v;
                    if (file + h < 8 && file + h > -1) file = file + h;

                    if ((rank != king.pos[0] || file != king.pos[1]) && king.moves.notContains(rank, file)) {
                        Piece dest = Board.board[rank][file];
                        if(king.color) dest.attWhite = true;
                        else dest.attBlack = true;
                    }
                }
            }
        }
        for(King king: Game.kings){
            king.addMoves();
        }
    }

    // readFEN and getFEN were probably the two most fun functions to write.
    public static void readFEN(String FEN){
        int rank = 0;
        int file = 0;
        String[] split = FEN.split(" ");

        for(char c: split[0].toCharArray()){
            boolean cont = true;

            if(c == '/') rank++;
            else if((int)c > 56 ){
                switch (c) {
                    case 'P' ->{
                        board[rank][file] = new Pawn(rank,file,true);
                        if(rank != 6) board[rank][file].firstMove = false;
                    }

                    case 'p' ->{
                        board[rank][file] = new Pawn(rank,file,false);
                        if(rank != 1) board[rank][file].firstMove = false;
                    }
                    case 'N' -> board[rank][file] = new Knight(rank,file,true);
                    case 'n' -> board[rank][file] = new Knight(rank,file,false);
                    case 'B' -> board[rank][file] = new Bishop(rank, file, true);
                    case 'b' -> board[rank][file] = new Bishop(rank, file, false);
                    case 'Q' -> board[rank][file] = new Queen(rank,file, true);
                    case 'q' -> board[rank][file] = new Queen(rank,file, false);
                    case 'R' -> {
                        board[rank][file] = new Rook(rank,file,true);
                        board[rank][file].castle = false;
                    }
                    case 'r' -> {
                        board[rank][file] = new Rook(rank,file,false);
                        board[rank][file].castle = false;
                    }
                    case 'K' -> {
                        board[rank][file] = new King(rank,file,true);
                        board[rank][file].castle = false;
                    }
                    case 'k' -> {
                        board[rank][file] = new King(rank,file,false);
                        board[rank][file].castle = false;
                    }
                }
            }
            else if ((int)c > 48){
                for(int i = 0; i < (int)c-48; i++){
                    cont = false;
                    board[rank][file] = new Square(rank,file);
                    file++;
                    if(file == 8) file = 0;
                }
            }
            if(c!= '/' && cont) file++;
            if(file == 8) file = 0;
        }
        Game.turn = split[1].equals("w");

        if(!split[2].equals("-")){
            for(char c: split[2].toCharArray()){
                switch (c){
                    case 'K' -> {
                        board[7][4].castle = true;
                        board[7][7].castle = true;
                    }
                    case 'Q' -> {
                        board[7][4].castle = true;
                        board[7][0].castle = true;
                    }
                    case 'k' -> {
                        board[0][4].castle = true;
                        board[0][7].castle = true;
                    }
                    case 'q' -> {
                        board[0][4].castle=true;
                        board[0][0].castle = true;
                    }
                }
            }
        }
        if(!split[3].equals("-")){
            file = Math.abs(split[3].charAt(0)-97);
            rank = Math.abs(split[3].charAt(1)-56);

            if(rank == 5){
                board[rank-1][file].enP = true;
            }
            else{
                board[rank+1][file].enP = true;
            }
        }
        int size = split[4].length();
        int fiftyMove = 0;
        for(int i = 0; i < split[4].length(); i++){
            fiftyMove += (split[4].charAt(i)-48)*Math.pow(10, size-1);
            size--;
        }
        size = split[5].length();
        int fullMoves = 0;
        for(int i = 0; i < split[5].length(); i++){
            fullMoves += (split[5].charAt(i)-48)*Math.pow(10, size-1);
            size--;
        }
        Game.fiftyMoveCount = fiftyMove;
        Game.fullMove = fullMoves;
    }

    public static String getFEN(){
        StringBuilder FEN = new StringBuilder();
        int count = 0;
        boolean wqc = false;
        boolean wkc = false;
        boolean bqc = false;
        boolean bkc = false;
        String castling = " ";
        StringBuilder enP = new StringBuilder();

        for(Piece[] rank: board){
            for(Piece piece: rank){
                if(piece instanceof Square) count++;
                else{
                    if(piece instanceof Pawn && piece.enP){
                        enP.append((char)(piece.pos[1] + 97));
                        if(piece.color) enP.append(Math.abs(piece.pos[0] - 8) - 1);
                        else enP.append(Math.abs(piece.pos[0] - 8) + 1);
                    }
                    if(piece instanceof Rook && piece.castle){
                        if(piece.pos[1] == 0){
                            if(piece.color) wqc = true;
                            else bqc = true;
                        }
                        else{
                            if(piece.color) wkc = true;
                            else bkc = true;
                        }
                    }

                    if(count != 0) FEN.append(count);
                    count = 0;
                    FEN.append(piece.FEN);
                }
                if(piece.pos[1] == 7 && count!=0) { FEN.append(count); count = 0; }
                if(piece.pos[0] != 7 && piece.pos[1] == 7) FEN.append("/");
            }
        }
        for(Piece piece: Game.kings){
            if(!piece.castle && piece.color){
                wkc = false;
                wqc = false;
            }
            if(!piece.castle && !piece.color){
                bkc = false;
                bqc = false;
            }
        }
        if (Game.turn) FEN.append(" w");
        else FEN.append(" b");

        if(!wkc && !wqc && !bkc && !bqc) castling += "-";
        else{
            if(wkc) castling += "K";
            if(wqc) castling += "Q";
            if(bkc) castling += "k";
            if(bqc) castling += "q";
        }

        FEN.append(castling);

        if(enP.toString().equals("")) FEN.append(" -");
        else FEN.append(" ").append(enP);

        FEN.append(" ").append(Game.fiftyMoveCount).append(" ").append(Game.fullMove);

        return FEN.toString();
    }

    private static void newPos(){
        for(Piece[] rank: board){
            for(Piece piece: rank){
                piece.moves.clear();
                piece.attWhite = false;
                piece.attBlack = false;
            }
        }
    }

    public static void promote(int[] start, int[] end, String colorName){
        String[] split = colorName.split("_");
        String name  = String.valueOf((char)(start[1]+97));

        Piece piece;
        boolean turn = split[0].equals("white");

        piece = switch (split[1]) {
            case "Knight" -> new Knight(end[0], end[1], turn);
            case "Rook" -> new Rook(end[0], end[1], turn);
            case "Bishop" -> new Bishop(end[0], end[1], turn);
            default -> new Queen(end[0], end[1], turn);
        };

        Game.turnNotation += name + Math.abs(end[0] - 8) + "=" + piece.FEN.toUpperCase();
        Board.board[end[0]][end[1]] = piece;
        Board.board[start[0]][start[1]] = new Square(start[0],start[1]);

    }
}
