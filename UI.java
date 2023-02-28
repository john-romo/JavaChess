// Static Class (why instantiate?) handling all things UI.  I know most people make their components classes
// and instatiate. But why?  Why not just make functions that build and return components?  I'm sure there's
// a reason that I just don't know, but I don't want to copy other people's work. Anyway, the Class is
// largely self explanitory.

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class UI {
    private static JFrame gameFrame; // <-- main frame
    private static JPanel board; // <-- chess board
    private static JPanel variationDisplay;
    private static JPanel infoBar;
    private static Popup infoPop; // <-- made after promote pop, so it was easy
    private static Popup promotePop; // <-- took forever
    private static int flipBoard = 0;
    private static final String saveFolder = "src/saves/";
    private static final String startSaveFolder = "src/startSaves/";
    private static final String imagesFolder = "src/ChessPieces/";
    private static final String aboutTXTFolder = "src/InfoMenuTXTFiles/";
    private static boolean firstClick = true; // <-- for checking if user is grabbing start/end move coordinates

    public static void ui() throws IOException {
        gameFrame = new JFrame("Chess");
        gameFrame.setResizable(false);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setVisible(true);

        setBoard();
        variationDisplay();
        menuBar();
        toMoveIcon();
        gameStateBar();
        keyInput();

        gameFrame.pack();
    }

    private static void refreshGameFrame(){
        gameFrame.validate();
        gameFrame.repaint();
    }

    private static void refreshBoard(){
        gameFrame.remove(board);
        setBoard();
        refreshGameStateBar();
        firstClick = true;
    }

    private static void refreshVariationDisplay() throws IOException {
        gameFrame.remove(variationDisplay);
        variationDisplay();
        refreshGameFrame();
    }

    public static void refreshBoardAndVariationDisplay() {
        refreshBoard();
        try{
        refreshVariationDisplay();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static void refreshMenuBar(){
        gameFrame.setMenuBar(null);
        menuBar();
        refreshGameFrame();
    }

    public static void refreshGameStateBar(){
        gameFrame.remove(infoBar);
        gameStateBar();
        refreshGameFrame();
        Game.gameState = "";
    }

    private static void setBoard(){
        board = new JPanel(new GridLayout(8,8));
        Color color;

        for(int rank = 0; rank < 8; rank++){
            for(int file = 0; file < 8; file++){
                if((rank+file)%2 == 0) color = Color.WHITE;
                else color = Color.DARK_GRAY;
                JPanel tile = new JPanel();
                tile.setPreferredSize(new Dimension(64,64));

                if(!Board.board[Math.abs(Math.abs(rank+flipBoard))][Math.abs(file+flipBoard)].square){
                    ImageIcon piece = new ImageIcon(imagesFolder
                            + Board.board[Math.abs(rank+flipBoard)][Math.abs(file+flipBoard)].image);
                    JLabel pic = new JLabel(piece);
                    tile.add(pic);
                }
                tile.setBackground(color);
                tile.setName(rank + "" + file);
                board.add(tile);
            }
        }
        board.setPreferredSize(new Dimension(512,512));
        gameFrame.getContentPane().add(board, BorderLayout.CENTER);
        board.addMouseListener(MouseInputs.boardInput);
    }

    private static void variationDisplay() throws IOException {
        JEditorPane display = new JEditorPane("text/html",Game.lines.printNames());
        Font font = new Font("Serif", Font.PLAIN, 15);
        display.setFont(font);
        display.setEditable(false);

        JViewport view = new JViewport();
        view.setView(display);

        JScrollPane scroll = new JScrollPane(view);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(20,20));
        Runnable getScrollPos = () -> {
            double unit = (double)scroll.getVerticalScrollBar().getMaximum()/Game.lines.totalPositionNum;
            int scrollbarPosition = (int)Math.ceil(Game.lines.getCursorPosNum()*unit-(25*unit));

            scroll.getVerticalScrollBar().setValue(scrollbarPosition);
        };

        SwingUtilities.invokeLater(getScrollPos);
        scroll.setPreferredSize(new Dimension(250,482));

        variationDisplay = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 7;
        c.gridy = 1;
        variationDisplay.add(scroll,c);
        evalPanel();
        addButtons();
        gameFrame.getContentPane().add(variationDisplay, BorderLayout.EAST);
    }

    private static void evalPanel() throws IOException {
        Engine.eval();
        JPanel evalPanel = new JPanel();
        evalPanel.setPreferredSize(new Dimension(250,30));
        evalPanel.setBackground(Color.lightGray);
        evalPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        JLabel text = new JLabel(Engine.eval);

        evalPanel.add(text);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NORTH;
        c.gridwidth = 7;
        c.gridy = 0;
        variationDisplay.add(evalPanel, c);
    }

    private static void rewindButton(){
        JButton variationStart = new JButton("|");
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.SOUTH;
        c.gridx = 0;
        c.gridy = 2;
        variationStart.addActionListener(e -> {
            Game.lines.rewind();
            refreshBoardAndVariationDisplay();
        });

        variationDisplay.add(variationStart, c);
    }

    private static void variationStartButton(){
        JButton variationStart = new JButton("<<");
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.SOUTH;
        c.gridx = 1;
        c.gridy = 2;
        variationStart.addActionListener(e -> {
            Game.lines.variationBack();
            refreshBoardAndVariationDisplay();
        });

        variationDisplay.add(variationStart, c);
    }

    private static void backButton(){
        JButton back = new JButton("<");
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.SOUTH;
        c.gridx = 2;
        c.gridy = 2;
        back.addActionListener(e -> {
            Game.lines.back();
            refreshBoardAndVariationDisplay();
        });

        variationDisplay.add(back, c);
    }

    private static void switchVariationButton(){
        JButton switchVariation = new JButton("/");
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.SOUTH;
        c.gridx = 3;
        c.gridy = 2;
        switchVariation.addActionListener(e -> {
            Game.lines.switchVariation();
            refreshBoardAndVariationDisplay();
        });
        variationDisplay.add(switchVariation, c);

    }

    private static void forwardButton(){
        JButton forward = new JButton(">");
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.SOUTH;
        c.gridx = 4;
        c.gridy = 2;
        forward.addActionListener(e -> {
            Game.lines.forward();
            refreshBoardAndVariationDisplay();
        });
        variationDisplay.add(forward, c);
    }

    private static void variationEndButton(){
        JButton variationEnd = new JButton(">>");
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.SOUTH;
        c.gridx = 5;
        c.gridy = 2;
        variationEnd.addActionListener(e -> {
            Game.lines.variationForward();
            refreshBoardAndVariationDisplay();
        });

        variationDisplay.add(variationEnd, c);
    }

    private static void fastForwardButton(){
        JButton variationStart = new JButton("|");
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.SOUTH;
        c.gridx = 6;
        c.gridy = 2;
        variationStart.addActionListener(e -> {
            Game.lines.fastForward();
            refreshBoardAndVariationDisplay();
        });

        variationDisplay.add(variationStart, c);
    }

    private static void addButtons(){
        rewindButton();
        variationStartButton();
        backButton();
        switchVariationButton();
        forwardButton();
        variationEndButton();
        fastForwardButton();
    }

    private static void menuBar(){
        JMenuBar menu = new JMenuBar();
        menu.setPreferredSize(new Dimension(50,30));

        menu.add(toMoveIcon());

        menu.add(new JSeparator(SwingConstants.VERTICAL));
        menu.add(fileMenu());
        menu.add(new JSeparator(SwingConstants.VERTICAL));
        menu.add(variationMenu());
        menu.add(new JSeparator(SwingConstants.VERTICAL));
        menu.add(engineMenu());
        menu.add(new JSeparator(SwingConstants.VERTICAL));
        menu.add(infoMenu());


        JSeparator allignment = new JSeparator(javax.swing.SwingConstants.VERTICAL);
        allignment.setPreferredSize(new java.awt.Dimension(425, 1));
        menu.add(allignment);

        menu.add(flipBoardButton());

        gameFrame.setJMenuBar(menu);
    }

    private static JMenuItem toMoveIcon(){
        String picName;
        if(Game.turn) picName = imagesFolder + "whiteToMove.png";
        else picName = imagesFolder + "blackToMove.png";
        ImageIcon toMovePic = new ImageIcon(picName);
        JMenuItem label = new JMenuItem();
        label.setIcon(toMovePic);
        return label;
    }

    private static JMenu fileMenu(){
        JMenu file = new JMenu("    File    ");
        file.setBackground(Color.DARK_GRAY);
        JMenuItem newGame = new JMenuItem("New Game");
        newGame.addActionListener(e -> newGame());
        JMenuItem save = new JMenuItem("Save Game");
        save.addActionListener(e -> saveGame(saveFolder));
        JMenuItem saveStart = new JMenuItem("Save As New Start Position");
        saveStart.addActionListener(e -> saveGame(startSaveFolder));
        JMenu load = new JMenu("Load Game");
        JMenu loadStart = new JMenu("Load Start Position");

        file.add(newGame);
        file.addSeparator();
        file.add(save);
        file.add(saveStart);
        file.addSeparator();
        loadSubMenu(load, saveFolder);
        loadSubMenu(loadStart, startSaveFolder);
        file.add(load);
        file.add(loadStart);

        return file;
    }

    private static void newGame(){
        Game.lines.newGame();
        gameFrame.setFocusable(true);
        gameFrame.requestFocus();
        refreshBoardAndVariationDisplay();
    }

    private static void saveGame(String folder){
        if(Game.lines.savable()){
            String filename = savePop();
            if(filename != null){
                try {
                    Game.lines.saveGame(folder + filename + ".txt");
                } catch (IOException ex) {
                    // add pop (on bedroom PC, old history: no file warning pop is perfect)
                    ex.printStackTrace();
                }
            }
        }
    }

    private static String savePop(){
        String send = JOptionPane.showInputDialog("Enter save file name: ");
        char[] removes = new char[] {'#','<','>','$','+','%','!','*','\'','|',
                '{','}','/','\\','?','\"','=','@'};

        for(char remove: removes){
            send = send.replace(remove, '\u0000');
        }

        return send;
    }

    private static int saveRequestPop(){
        return JOptionPane.showOptionDialog(null,
                "Current Game progress will be lost.  Save Game?",
                "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                null, null, null);
    }

    private static void loadGame(String name, String folder){
        int choice = 1;
        if(Game.lines.savable()) choice = saveRequestPop();

        if(choice == 0 || choice == 1){
            if (choice == 0) saveGame(folder);
            Game.lines.newGame();
            try {
                Game.lines.loadGame(folder + name + ".txt");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            gameFrame.setFocusable(true);
            gameFrame.requestFocus();
            refreshBoardAndVariationDisplay();
            refreshMenuBar();
        }
    }

    private static void loadSubMenu(JMenu load, String folder) {
        String[] lgsn = loadSubNames(folder);
        if (lgsn != null) {
            for (String name : lgsn) {
                JMenuItem mname = new JMenuItem(name);
                mname.addActionListener(e -> loadGame(name, folder));
                load.add(mname);
            }
        }
    }

    private static String[] loadSubNames(String folder){
        File save = new File(folder);
        String[] files = save.list();

        if (files != null){
            String[] filenames = new String[files.length];
            for(int i = 0; i < files.length; i++){
                filenames[i] = files[i].substring(0,files[i].length()-4);
            }
            return filenames;
        }
        return null;
    }

    private static JMenu engineMenu(){
        JMenu engineMenu = new JMenu("   Engine   ");

        engineMenu.add(engineModeMenu());
        engineMenu.add(engineMoveTimeMenu());
        engineMenu.add(engineELOMenu());
        engineMenu.addSeparator();
        engineMenu.add(engineTurnMenuItem());

        return engineMenu;
    }

    private static JMenu engineELOMenu(){
        JMenu engineSub = new JMenu("Set Engine ELO");

        JMenuItem minimum = new JMenuItem("Minimum");
        minimum.addActionListener(e -> setEngineELO("1350"));
        if(Engine.ELO.equals("1350")) minimum.setBackground(Color.LIGHT_GRAY);

        JMenuItem fifteen = new JMenuItem("1500");
        fifteen.addActionListener(e -> setEngineELO("1500"));
        if(Engine.ELO.equals("1500")) fifteen.setBackground(Color.LIGHT_GRAY);

        JMenuItem seventeen = new JMenuItem("1700");
        seventeen.addActionListener(e -> setEngineELO("1700"));
        if(Engine.ELO.equals("1700")) seventeen.setBackground(Color.LIGHT_GRAY);

        JMenuItem nineteen = new JMenuItem("1900");
        nineteen.addActionListener(e -> setEngineELO("1900"));
        if(Engine.ELO.equals("1800")) nineteen.setBackground(Color.LIGHT_GRAY);

        JMenuItem twentytwo = new JMenuItem("2200");
        twentytwo.addActionListener(e -> setEngineELO("2200"));
        if(Engine.ELO.equals("2200")) twentytwo.setBackground(Color.LIGHT_GRAY);

        JMenuItem twentyfive = new JMenuItem("2500");
        twentyfive.addActionListener(e -> setEngineELO("2500"));
        if(Engine.ELO.equals("2500")) twentyfive.setBackground(Color.LIGHT_GRAY);

        JMenuItem maximum = new JMenuItem("Maximum");
        maximum.addActionListener(e -> setEngineELO("max"));
        if(Engine.ELO.equals("max")) maximum.setBackground(Color.LIGHT_GRAY);

        engineSub.add(minimum);
        engineSub.add(fifteen);
        engineSub.add(seventeen);
        engineSub.add(nineteen);
        engineSub.add(twentytwo);
        engineSub.add(twentyfive);
        engineSub.add(maximum);

        return engineSub;
    }

    private static JMenuItem engineTurnMenuItem(){
        String blackWhite;
        if(Engine.turn) blackWhite = "Switch Engine Turn to Black";
        else blackWhite = "Switch Engine Turn to White";

        JMenuItem turn = new JMenuItem(blackWhite);
        turn.addActionListener(e -> {
            Engine.turn = !Engine.turn;
            tryEngineMakeMove();
            refreshMenuBar();
        });
        return turn;
    }

    // trys to make a move if possible
    private static void tryEngineMakeMove(){
        if(Game.turn == Engine.turn && Engine.engaged && Engine.playMode) {
            try {
                Engine.move();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // choose play mode (makes moves) and analyses mode (updates analyses)
    private static JMenu engineModeMenu(){
        JMenu mode = new JMenu("Engine Mode");
        JCheckBoxMenuItem playMode = new JCheckBoxMenuItem("Play Mode");
        playMode.setSelected(Engine.playMode);
        playMode.addItemListener(i-> {
                if(i.getStateChange() == ItemEvent.SELECTED){
                    Engine.playMode = true;
                    tryEngineShouldEngage();
                    tryEngineMakeMove();
                }
                if(i.getStateChange() == ItemEvent.DESELECTED){
                    Engine.playMode = false;
                    tryEngineShouldEngage();
                }
            }
        );
        JCheckBoxMenuItem analysesMode = new JCheckBoxMenuItem("Analyses Mode");
        analysesMode.setSelected(Engine.analysesMode);
        analysesMode.addItemListener(i-> {
                if(i.getStateChange() == ItemEvent.SELECTED){
                    Engine.analysesMode = true;
                    tryEngineShouldEngage();
                }
                if(i.getStateChange() == ItemEvent.DESELECTED){
                    Engine.analysesMode = false;
                    tryEngineShouldEngage();
                }
            }
        );
        mode.add(playMode);
        mode.add(analysesMode);

        return mode;
    }

    // trys to engage engine if possible
    private static void tryEngineShouldEngage(){
        try {
            Engine.shouldEngage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JMenu engineMoveTimeMenu(){
        JMenu engineSub = new JMenu("Set Engine Move Time");

        JMenuItem one = new JMenuItem("One Second");
        one.addActionListener(e -> setEngineMovetime(1000));
        if(Engine.moveTime == 1000) one.setBackground(Color.LIGHT_GRAY);

        JMenuItem five = new JMenuItem("Five Seconds");
        five.addActionListener(e -> setEngineMovetime(5000));
        if(Engine.moveTime == 5000) five.setBackground(Color.LIGHT_GRAY);

        JMenuItem thirty = new JMenuItem("Thirty Seconds");
        thirty.addActionListener(e -> setEngineMovetime(30000));
        if(Engine.moveTime == 30000) thirty.setBackground(Color.LIGHT_GRAY);

        JMenuItem minute = new JMenuItem("One Minute");
        minute.addActionListener(e -> setEngineMovetime(60000));
        if(Engine.moveTime == 60000) minute.setBackground(Color.LIGHT_GRAY);

        engineSub.add(one);
        engineSub.add(five);
        engineSub.add(thirty);
        engineSub.add(minute);

        return engineSub;
    }

    private static void setEngineMovetime(int time){
        Engine.moveTime = time;
        refreshMenuBar();
    }

    private static void setEngineELO(String ELO) {
        try{
            Engine.ELO = ELO;
            Engine.setELO();
            refreshMenuBar();
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    private static JMenu variationMenu(){
        JMenu variationMenu = new JMenu(" Variations ");

        JMenuItem deleteMainLineVariation = new JMenuItem("Delete Main Line Variations");
        deleteMainLineVariation.addActionListener(e -> {
            Game.lines.deleteMainLineVariations();
            refreshBoardAndVariationDisplay();
        });

        JMenuItem deleteCurrentVariation = new JMenuItem("Delete Current Variation");
        deleteCurrentVariation.addActionListener(e-> {
            Game.lines.deleteCurrentVariation();
            refreshBoardAndVariationDisplay();
        });

        variationMenu.add(deleteMainLineVariation);
        variationMenu.add(deleteCurrentVariation);

        return variationMenu;
    }

    private static JMenu infoMenu() { // about / contros / hotkeys -> need text panel popup actions
        JMenu infoMenu = new JMenu("     Info     ");

        File files = new File(String.valueOf(Path.of(aboutTXTFolder)));

        String[] fileNames = files.list();

        for(int i = 0; i < Objects.requireNonNull(fileNames).length; i++){
            String name = fileNames[i];
            JMenuItem about = new JMenuItem(name.replace(".txt",""));
            about.addActionListener(e-> {
                try {
                    createAndShowInfoPop(name);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            infoMenu.add(about);
            if(i == 0) infoMenu.addSeparator();
        }
        return infoMenu;
    }

    // sets infoBar to display Game.info
    private static void setInfoPop(String filename){
        JEditorPane info = new JEditorPane("text/html",filename);
        info.setEditable(false);
        Font font = new Font("Serif", Font.PLAIN, 15);
        info.setFont(font);
        info.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        info.setBackground(Color.lightGray.brighter());

        JButton close = new JButton("Close");
        close.addActionListener(e-> infoPop.hide());

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.DARK_GRAY);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 1;
        GridBagConstraints cc = new GridBagConstraints();
        cc.gridy = 2;
        infoPanel.add(info, cc);
        infoPanel.add(close,c);

        PopupFactory thisPop = PopupFactory.getSharedInstance();
        infoPop = thisPop.getPopup(gameFrame, infoPanel, 250,25);
    }

    private static void createAndShowInfoPop(String filename) throws IOException {
        List<String> about = Files.readAllLines(Path.of(aboutTXTFolder + filename));
        StringBuilder sendStr = new StringBuilder();
        for(String string: about){
            sendStr.append(string);
        }
        setInfoPop(sendStr.toString());
        infoPop.show();
    }

    private static JButton flipBoardButton(){
        JButton flipBoardButton = new JButton("Flip Board");
        flipBoardButton.addActionListener(e -> {
            if(flipBoard == 0) flipBoard = -7;
            else flipBoard = 0;
            refreshBoard();
        });
        return flipBoardButton;
    }

    private static void gameStateBar(){
        JPanel gameState = new JPanel();
        gameState.setPreferredSize(new Dimension(512,30));
        gameState.setBackground(Color.lightGray);
        gameState.setBorder(BorderFactory.createLineBorder(Color.black));

        JLabel text = new JLabel(Game.gameState);

        gameState.add(text);

        infoBar = gameState;
        gameFrame.add(gameState, BorderLayout.SOUTH);
    }

    private static void setPromotePop(int[] start, int[] end, Piece piece){
        Game.inPromotion = true;
        JPanel promotePieceChoices = new JPanel();
        String[] names = new String[]{"Queen","Rook","Bishop","Knight"};
        JPanel pieceChoiceTile = new JPanel();
        for(String name: names){
            String color;
            if(Game.turn) color = "white";
            else color = "black";
            ImageIcon pieceIcon = new ImageIcon(imagesFolder + color + "Mini" + name + ".png");
            JButton pieceButton = new JButton();
            pieceButton.setIcon(pieceIcon);
            pieceButton.addActionListener(e-> {
                Board.promote(start, end, color+"_"+name);
                try {
                    Game.promotionMove(piece);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Game.inPromotion = false;
                firstClick = true;
                promotePop.hide();
                refreshBoardAndVariationDisplay();
            });
            pieceChoiceTile.add(pieceButton);
            pieceChoiceTile.setName(color + "_" + name);
            promotePieceChoices.add(pieceChoiceTile);
        }

        PopupFactory pop = PopupFactory.getSharedInstance();
        promotePop = pop.getPopup(gameFrame, promotePieceChoices, 250,250);
    }

    public static void createAndShowPromotePop(int[] start, int[] end, Piece piece){
        setPromotePop(start, end, piece);
        promotePop.show();
    }

    private static class MouseInputs {
        static Color color;
        static Component c;

        static MouseListener boardInput = new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }
            public void mousePressed(MouseEvent e) {
                if(Game.lines.notGameOverPos()){
                    if(!Engine.playMode || Game.turn != Engine.turn){
                        c = board.getComponentAt(e.getPoint());
                        int rank = Math.abs(((c.getName().charAt(0))-48)+flipBoard);
                        int file = Math.abs(((c.getName().charAt(1))-48)+flipBoard);
                        if(firstClick){
                            color = c.getBackground();
                            c.setBackground(Color.YELLOW);
                            Game.startCoords = new int[] {rank, file};
                            firstClick = false;
                        }
                        else{
                            c.setBackground(color);
                            Game.endCoords = new int[] {rank,file};
                            try {
                                Game.turn();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            refreshBoardAndVariationDisplay();
                        }
                    }
                    if(Engine.engaged && Game.turn == Engine.turn){
                        Runnable getMove = () -> {
                            try {
                                Engine.move();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        };
                        SwingUtilities.invokeLater(getMove);
                    }
                }
            }
            public void mouseReleased(MouseEvent e) {
            }
            public void mouseEntered(MouseEvent e) {
            }
            public void mouseExited(MouseEvent e) {
            }
        };
    }

    private static void keyInput(){
        gameFrame.setFocusable(true);
        gameFrame.requestFocus();
        gameFrame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_Q){
                    Game.lines.rewind();
                    refreshBoardAndVariationDisplay();
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    Game.lines.back();
                    refreshBoardAndVariationDisplay();
                }
                if (e.getKeyCode() == KeyEvent.VK_A) {
                    Game.lines.back();
                    refreshBoardAndVariationDisplay();
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    Game.lines.forward();
                    refreshBoardAndVariationDisplay();
                }
                if (e.getKeyCode() == KeyEvent.VK_D) {
                    Game.lines.forward();
                    refreshBoardAndVariationDisplay();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    Game.lines.variationBack();
                    refreshBoardAndVariationDisplay();
                }
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    Game.lines.variationBack();
                    refreshBoardAndVariationDisplay();
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    Game.lines.variationForward();
                    refreshBoardAndVariationDisplay();
                }
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    Game.lines.variationForward();
                    refreshBoardAndVariationDisplay();
                }
                if(e.getKeyCode() == KeyEvent.VK_E){
                    Game.lines.fastForward();
                    refreshBoardAndVariationDisplay();
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    Game.lines.switchVariation();
                    refreshBoardAndVariationDisplay();
                }
                if (e.getKeyCode() == KeyEvent.VK_F) {
                    if(flipBoard == 0) flipBoard = -7;
                    else flipBoard = 0;
                    refreshBoardAndVariationDisplay();
                }
                if (e.getKeyCode() == KeyEvent.VK_F1) {
                    newGame();
                    refreshBoardAndVariationDisplay();
                }
                if (e.getKeyCode() == KeyEvent.VK_F2) {
                    saveGame(saveFolder);
                }
                if (e.getKeyCode() == KeyEvent.VK_F3) {
                    saveGame(startSaveFolder);
                }
                if (e.getKeyCode() == KeyEvent.VK_T) {
                    if(Engine.engaged){
                        try {
                            Engine.disengage();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    else{
                        tryEngineShouldEngage();
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_G) {
                    Engine.turn = !Engine.turn;
                    refreshMenuBar();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }
}
