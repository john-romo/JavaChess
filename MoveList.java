// this was basically the first thing I wrote.  Why didn't I just use a normal array?  I actually
// can't remember...

import java.util.ArrayList;
import java.util.Arrays;

public class MoveList {
    public ArrayList<int[]> moves = new ArrayList<>();

    public boolean legal(int[] pos){
        for(int[] arr: moves){
            if (arr[0] == pos[0] && arr[1] == pos[1]) return true;
        }
        return false;
    }

    public void add(int[] pos){ moves.add(pos); }

    public int[] get(int i){ return moves.get(i); }

    public int size(){ return moves.size(); }

    public void clear(){ moves.clear(); }

    public boolean notContains(int rank, int file){
        for(int i = 0; i < this.size(); i++){
            if(this.get(i)[0] == rank && this.get(i)[1] == file) return false;
        }
        return true;
    }

    public void printList(){
        if(moves == null || moves.size() == 0) System.out.println("None");
        for(int[] moves: moves){
            System.out.println(Arrays.toString(moves));
        }
    }

    public static void printCheckingLine(){
        if(Game.allCheckingLines == null || Game.allCheckingLines.size()==0) System.out.println("NONE");
        if(Game.allCheckingLines != null){
            for(int[] move: Game.allCheckingLines){
                System.out.println("Checking line: " + Arrays.toString(move));
            }
        }
    }

    public static void printPieceCheckingLine(Piece piece){
        if(piece.pieceCheckingLine == null || piece.pieceCheckingLine.size()==0) System.out.println("NONE");
        if(piece.pieceCheckingLine != null){
            for(int[] move: piece.pieceCheckingLine){
                System.out.println("Checking line: " + Arrays.toString(move));
            }
        }
    }
}
