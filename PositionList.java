// A list of all positions and variations

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class PositionList {
    private final Position start;
    private Position end;
    private Position cursor; // <-- the position displayed by the UI
    public int totalPositionNum;

    public PositionList(String FEN, String name){
        start = new Position(FEN, name);
        cursor = start;
    }

    // all the necesary information for each board position is stored in the instantiations of this class
    private static class Position{
        private int ID; // <-- helps with loading a saved game
        private Position next;
        private Position prev;
        private Position root; // <-- position that is head of variation "fork." null if !part of variation
        private ArrayList<VariationList> variations; // <-- makes sure position can have multiple variations
        private VariationList variationList; // <-- variation list which position belongs to. null if none.
        private final String FEN;
        private String printName; // <-- the string that shows up on the variation display
        private final String name; // <-- position chess notation
        private int depth; // <-- variation depth (eg: mainline has 0 depth, variation from mainline has 1 depth)
        private int variationNumber = -1; // <-- for mainline position = -1, numbers a position's variations
        private int turnNumber = 0;
        int positionInList = 0; // <-- part of how I solved dynamic scrollbar positioning
        boolean endGamePos = false; // <-- true if the position resulted in a game-over

        private Position(String FEN, String name){
            Random rand = new Random();
            ID = rand.nextInt(999999);
            this.FEN = FEN;
            this.name = name;
            printName = name;
        }
    }

    // class for holding a position's variations
    private static class VariationList {
        private Position root;
        private Position head;
        private Position tail;
    }

    // adds position / variation list to the position list
    public void append(String FEN, String name, int ID){
        Position pos = new Position(FEN,name);
        if(ID != -1) pos.ID = ID;
        if(notAVariation(FEN)){
            if(cursor == start){
                end = pos;
                start.next = end;
                end.prev = start;
                cursor = end;
            }
            else if(cursor == end){
                pos.prev = end;
                end.next = pos;
                end = pos;
                cursor = end;
                cursor.depth = cursor.prev.depth;
            }
            // adding to a variationList
            else if(cursor.variationList != null && (cursor.next == null || !cursor.next.FEN.equals(FEN))){
                cursor.next = pos;
                pos.prev = cursor;
                pos.root = cursor.root;
                cursor = pos;
                cursor.variationList = cursor.prev.variationList;
                cursor.variationNumber = cursor.variationList.head.variationNumber;
                cursor.variationList.tail = pos;
                cursor.depth = cursor.prev.depth;
            }
            else cursor = cursor.next;
            setTurnNumber(cursor);
            totalPositionNum++;
            setPositionInList();
        }
        else if(!alreadyAVariation(FEN)) addVariation(FEN, name, -1);
    }

    // adds a variation to a move's variation list (create variation list if none)
    private void addVariation(String FEN, String name, int ID){
        if(cursor.variations == null) {
            cursor.variations = new ArrayList<>();
        }
        VariationList vars = new VariationList();
        cursor.variations.add(vars);

        Position pos = new Position(FEN, name);
        if(ID != -1) pos.ID = ID;

        vars.root = cursor;
        pos.root = cursor;
        vars.head = pos;
        vars.tail = pos;
        pos.prev = cursor;
        pos.variationList = vars;
        pos.variationNumber = pos.prev.variations.size()-1;
        pos.depth = cursor.depth + 1;

        setTurnNumber(pos);
        cursor = pos;
        totalPositionNum++;
        setPositionInList();
    }

    private void setPositionInList(){
        Stack<Position> stack = searchList();
        int posNum = 0;
        while(!stack.empty()){
            stack.pop().positionInList = posNum;
            posNum++;
        }
    }

    // for chess notation, important because chess notation turn isn't the same as FEN turn
    private void setTurnNumber(Position pos){
        if(pos.prev != null && pos.prev.turnNumber == 0){
            pos.turnNumber = pos.prev.turnNumber + 1;
        }
        if(pos.prev != null && pos.prev.prev != null){
            if((pos.prev.turnNumber + pos.prev.prev.turnNumber)%2 == 1){
                pos.turnNumber = pos.prev.turnNumber;
            }
            else {
                pos.turnNumber = pos.prev.turnNumber + 1;
            }
        }
    }

    private boolean notAVariation(String FEN){
        return cursor.next == null || cursor.next.FEN.equals(FEN);
    }

    private boolean alreadyAVariation(String FEN){
        if(cursor.variations != null){
            for(VariationList vars: cursor.variations){
                if(vars.head.FEN.equals(FEN)){
                    cursor = vars.head;
                    return true;
                }
            }
        }
        return false;
    }

    // takes the moves name (eg: Nf6) and changes it to its chess notation version (eg: 1.Nf6)
    private void setPrintName(Position check){
        check.printName = check.name;
        String tab = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;".repeat(check.depth);
        // ^^^ makes sure variations are easily identifiable as variations

        if(check.prev.turnNumber != check.turnNumber) {
            check.printName = check.turnNumber + "." + check.printName;
            check.printName = tab + check.printName;
        }
        else {
            if(check.prev.variations != null) {
                check.printName = check.turnNumber + "..." + check.printName;
                check.printName = tab + check.printName;
            }
            else check.printName = " " + check.printName;
        }

        if(( check.turnNumber == check.prev.turnNumber || check.variations != null) ||
                (check.variationList != null && check == check.variationList.tail) ){
            check.printName = check.printName + "<br>";
        }

        if(check == cursor) check.printName = "<b>" + check.printName + "</b>";
    }

    // creates and returns string of combined variation's notations
    public String printNames(){
        StringBuilder send = new StringBuilder();
        Stack<Position> positions = searchList();
        String whiteSpace = "<style='white-space:pre; white-space:nowrap'";

        send.append(whiteSpace);
        send.append("<span>");

        while(!positions.empty()){
            Position check = positions.pop();
            setPrintName(check);
            send.append(check.printName);
            if(check.root != null && check.variationNumber != check.root.variations.size()-1){
                if(check == check.variationList.tail){
                    send.append("<br>");
                }
            }
        }
        send.append("</span>");

        return send.toString();
    }

    // interesting work, imo
    public void saveGame(String filename) throws IOException {
        Path path = Path.of(filename);
        List<String> saveList = new ArrayList<>();
        Stack<Position> saveStack = searchList();

        while(!saveStack.empty()){
            Position check = saveStack.pop();

            int prev;
            if(check.prev == null) prev = -1;
            else prev = check.prev.ID;
            int root;
            if(check.root == null) root = -1;
            else root = check.root.ID;
            String saveString = check.name + "_" + check.ID + "_" + prev + "_" + root + "_" + check.FEN;
            // ^^^ figuring out the best information to store took a while.
            saveList.add(saveString);
        }
        Files.write(path, saveList);
    }

    // this took forever.  the answer was obvious, once I thought of it.  This is the reason
    // the positions have unique ID numbers.
    public void loadGame(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filename));
        for(String line: lines){
            String[] split = line.split("_");

            String name = split[0];
            int ID = Integer.parseInt(split[1]);
            int prev = Integer.parseInt(split[2]);
            int root = Integer.parseInt(split[3]);
            String FEN = split[4];

            if(cursor.name.equals("START")){
               append(FEN, name, ID);
            }
            else if(root != -1 && root == prev){
                cursor = getPosByID(root);
                addVariation(FEN, name, ID);
            }
            else if (prev != -1){
                cursor = getPosByID(prev);
                append(FEN, name, ID);
            }
        }
        cursor = end;
        setBoard();
    }

    public void newGame(){
        Game.lines = new PositionList(Game.startPos, "START");
        cursor = start;
        setBoard();
    }

    // the next seven functions move the cursor around (see 'Controls.txt' or 'Buttons.txt' in src)
    public void rewind(){
        if(cursor.variationList == null && start.next != null) cursor = start.next;
        else if(cursor.root != null) cursor = cursor.root;
        setBoard();
        checkEndGame();
    }

    public void variationBack() {
        if(cursor.prev != null && cursor.prev != start) cursor = cursor.prev;
        setBoard();
        checkEndGame();
    }

    public void back(){
        if(cursor.prev != null && cursor.prev != start){
            if(cursor.prev.variations != null){
                if(cursor.prev != cursor.root){
                    cursor = cursor.prev.variations.get(cursor.prev.variations.size()-1).tail;
                }
                else if(cursor.variationNumber != 0){
                    cursor = cursor.prev.variations.get(cursor.variationNumber-1).tail;
                }
                else cursor = cursor.prev;
            }
            else cursor = cursor.prev;
            setBoard();
            checkEndGame();
        }
    }

    public void switchVariation(){
        if(cursor.depth != 0){
            int varNum = cursor.variationNumber;
            varNum += 1;
            if(varNum > cursor.root.variations.size()-1) varNum = 0;
            cursor = cursor.root.variations.get(varNum).head;
        }
        setBoard();
        checkEndGame();
    }

    public void forward(){
        if(cursor.next != null){
            if(cursor.variations != null){
                cursor = cursor.variations.get(0).head;
            }
            else cursor = cursor.next;
        }
        else if(cursor != end){
            if(cursor.root != null && cursor.variationNumber == cursor.root.variations.size()-1){
                cursor = cursor.root.next;
            }
            else {
                assert cursor.root != null;
                cursor = cursor.root.variations.get(cursor.variationNumber + 1).head;
            }
        }
        setBoard();
        checkEndGame();
    }

    public void variationForward(){
        if(cursor.next != null){
           cursor = cursor.next;
        }
        setBoard();
        checkEndGame();
    }

    public void fastForward(){
        if(cursor.variationList == null) cursor = end;
        else if(cursor.root != null) cursor = cursor.root.next;
        setBoard();
        checkEndGame();
    }

    // necisary for loading, at least the way I'm doing it
    private Position getPosByID(int ID){
        Stack<Position> stack = searchList();
        while(!stack.empty()){
            Position pos = stack.pop();
            if(pos.ID == ID) return pos;
        }
        return null;
    }

    // great function.  good to be able to use a stack for once.
    private Stack<Position> searchList(){
        Stack<Position> stack = new Stack<>();
        Position check = end;
        while(check != null){
            if(check.variations != null){
                searchVars(stack,check);
            }
            if(!check.printName.equals("START")) stack.push(check);
            check = check.prev;
        }
        return stack;
    }

    // searchList's recursive brother.  A lot of fun to write.
    private void searchVars(Stack<Position> stack, Position check){
        Position checkAgain;
        int counter = check.variations.size();
        for(int i = counter-1; i >= 0; i--){
            VariationList vars = check.variations.get(i);
            checkAgain = vars.tail;
            while(checkAgain != vars.root){
                if(checkAgain.variations != null) {
                    searchVars(stack,checkAgain);
                }
                stack.push(checkAgain);
                checkAgain = checkAgain.prev;
            }
        }
    }

    public void deleteMainLineVariations(){
        cursor = start;
        while(cursor != null){
            cursor.variations = null;
            cursor = cursor.next;
        }
        cursor = end;
    }

    public void deleteCurrentVariation(){
        if(cursor.variations != null){
            cursor.variations = null;
        }
    }

    // next four functions check endgame conditions
    public boolean threefold(){
        Position check = cursor;
        int count = 0;

        while(check.prev != null && count < 2){
            check = check.prev;
            if(check.FEN.split(" ")[0].equals(cursor.FEN.split(" ")[0])) count++;
        }
        if(count >= 2){
            Game.gameState = "Game drawn by threefold repetition.";
            return true;
        }
        return false;
    }

    public static boolean insufficient(){
        int wbCount = 0;
        int bbCount = 0;
        int wnCount = 0;
        int bnCount = 0;

        for(Piece piece: Board.pieces){
            if(piece instanceof Queen || piece instanceof Pawn || piece instanceof Rook) return false;
            if (piece instanceof Bishop){
                if(piece.color) wbCount++;
                else bbCount++;
            }
            if (piece instanceof Knight){
                if(piece.color) wnCount++;
                else bnCount++;
            }
        }
        if(!(wbCount+wnCount <= 1) || !(bbCount+bnCount <= 1)) return false;
        Game.gameState = "Game drawn by Insufficient Material.";
        return true;
    }

    public static boolean fiftyMove(){
        if(Game.fiftyMoveCount != 100) return false;
        Game.gameState = "Game drawn by Fifty Move Rule.";
        return true;
    }

    public static boolean stalemate(){
        if((Game.turn && !Board.whiteMoves) || (!Game.turn && !Board.blackMoves)){
            Game.gameState = "Stalemate!";
            return true;
        }
        else return false;
    }

    // next two functions deal with making sure no moves can be played on an ended variation
    public boolean checkEndGame(){
        if(cursor == end || (cursor.root != null && cursor.variationList.tail == cursor)){
            if (insufficient() || stalemate() || threefold() || fiftyMove() || Game.checkMate()) {
                cursor.endGamePos = true;
                return true;
            }
        }
        return false;
    }

    public boolean notGameOverPos(){
        return !cursor.endGamePos;
    }

    // a big part of solving dynamic scrollbar positioning.
    public int getCursorPosNum(){ return cursor.positionInList; }

    // sets up the board
    public void setBoard(){
        Board.readFEN(cursor.FEN);
        Board.getMoves();
        Game.setKings();
    }

    // no reason to save a game after 1.e4
    public boolean savable(){ return searchList().size() >= 2; }

    // literally used once to tell engine what board to analyse.
    public String getFen(){ return cursor.FEN; }
}
