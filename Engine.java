import java.io.*;

public class Engine {
    static String engineLocation =
           "Paste the path to your stockfish exe here. Remember to edit engineActive in Game.java";
    static ProcessBuilder engine;
    static Process run;
    static BufferedReader istream;
    static BufferedWriter ostream;
    static boolean engaged;
    static boolean analysesMode = true;
    static boolean playMode = false;
    static boolean turn = false;
    static int moveTime = 1000;
    static String ELO = "";
    static String eval;

    public static void shouldEngage() throws IOException {
        if((analysesMode || playMode) && !engaged && Game.engineActive){ engage(); }
        if(!analysesMode && !playMode && engaged){ disengage(); }
    }

    public static void engage() throws IOException {		
        engaged = true;

	engine = new ProcessBuilder(engineLocation);

        run = engine.start();

        istream = new BufferedReader(new
                InputStreamReader(run.getInputStream()));

        ostream = new BufferedWriter(new
                OutputStreamWriter(run.getOutputStream()));

        if(playMode && Game.turn == Engine.turn) move();
    }

    public static void disengage() throws IOException {
        engaged = false;
        run.destroy();
        istream.close();
        ostream.close();
        Game.lines.setBoard();
    }

    public static void move() throws IOException {
        if(playMode){
            ostream.write("position fen "  + Game.lines.getFen() + "\n");
            ostream.write("go movetime " + moveTime + "\n");
            ostream.flush();

            String bestMove;

            while(true){
                String line = istream.readLine();
                if(line.startsWith("bestmove")) {
                    bestMove = line;
                    break;
                }
            }

            String stringMove = bestMove.split(" ")[1];
            int[][] move = new int[2][2];
            move[0] = new int[]{Math.abs(stringMove.charAt(1)-56), stringMove.charAt(0)-97};
            move[1] = new int[]{Math.abs(stringMove.charAt(3)-56), stringMove.charAt(2)-97};
            Game.startCoords = move[0];
            Game.endCoords = move[1];

            if(stringMove.length() == 4){
                Game.turn();
            }
            else{
                promotionMove(stringMove.charAt(4), move);
            }
            Game.lines.setBoard();
            UI.refreshBoardAndVariationDisplay();
        }

    }

    public static void promotionMove(char c, int[][] move) throws IOException {
        String name = switch (c) {
            case 'q' -> "Queen";
            case 'r' -> "Rook";
            case 'b' -> "Bishop";
            default -> "Knight";
        };
        String color;
        if(Game.turn) color = "white";
        else color = "black";
        Board.promote(move[0], move[1], color + "_" + name);
        Game.promotionMove(new Pawn(2, 2, true));
    }

    public static void eval() throws IOException {
        if(analysesMode && !Game.lines.checkEndGame()){
            if(engaged){
                ostream.write("position fen "  + Game.lines.getFen() + "\n");
                ostream.write("go movetime 20\n");
                ostream.flush();

                String line = "";
                String tempLine;

                do {
                    tempLine = istream.readLine();
                    if(tempLine.startsWith("info")) {
                        line = tempLine;
                    }
                } while (!tempLine.startsWith("bestmove"));

                String[] split = line.split(" ");

                String evalString;

                if(split[8].equals("cp")){
                    float evalFloat = Integer.parseInt(split[9])/100.f;
                    if(!Game.turn) evalFloat = -evalFloat;
                    evalString = String.format("%.2f", evalFloat);
                    if(evalFloat > 0) evalString = "+" + evalString;
                }

                else evalString = "Mate in " + Math.abs(Integer.parseInt(split[9]));
                eval = evalString;
            }
        }
    }

    public static void setELO() throws IOException {
        if(engaged) {
            if(ELO.equals("max")) ostream.write("setoption name UCI_LimitStrength value false\n");
            else {
                ostream.write("setoption name UCI_LimitStrength value true\n");
                ostream.write("setoption name UCI_Elo value " + ELO + "\n");
            }
            ostream.flush();
        }
    }
}
