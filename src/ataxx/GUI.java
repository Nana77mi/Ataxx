package ataxx;

// Optional Task: The GUI for the Ataxx Game

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import static ataxx.PieceState.*;

class GUI extends JFrame implements View, CommandSource, Reporter {

    // Complete the codes here
    GUI(String ataxx) {

        // Frame basic settings
        setTitle(ataxx);
        Dimension dimension = new Dimension(PIECE_NUM * PIECE_SIZE + LEFT_MARGIN, PIECE_NUM * PIECE_SIZE + UP_MARGIN);
        setSize(dimension);

        // Center the frame
        setLocationRelativeTo(null);
        // Exit event
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the icon
        redIcon = getIcon(RED_PATH);
        blueIcon = getIcon(BLUE_PATH);
        emptyIcon = getIcon(EMPTY_PATH);
        blockIcon = getIcon(BLOCK_PATH);

        // Add system buttons
        buttonFont = new Font( "Dialog", Font.BOLD, 14 );
        int sysButtonNum = Math.min(sysButtonTitles.size(), sysButtonCommands.size());
        initSysButtons(sysButtonNum);

        // Initialize the labels and buttons
        initLabels();
        initPieces();

        // Set panel
        JPanel chessBoard = new JPanel();
        getContentPane().add(chessBoard);
//        chessBoard.setBackground(Color.white);
        chessBoard.setPreferredSize(dimension);

    }

    // Add some codes here

    private class SystemOperation extends JButton {

        public SystemOperation(int x, int y, String title, String operation) {

            setBackground(Color.white);
            setLocation(x, y);
            setSize(BUTTON_WIDTH, 50);
            setText(title);
            setMargin(new Insets(0, 0, 0, 0));
            setFont(buttonFont);
            addActionListener((actionEvent->{
                message("%s", operation);
                showMessage(title);
            }));

        }

    }


    private class Piece extends JButton {
        private final char column;
        private final char row;
        public Piece(int x, int y) {
            setSize(PIECE_SIZE, PIECE_SIZE);
            setLocation(x * PIECE_SIZE, y * PIECE_SIZE + UP_MARGIN);
            column = (char)('a' + x);
            row = (char)('7' - y);
            addActionListener((actionEvent->{
                pieceClick();
            }));
        }

        private void pieceClick () {
            if (packBoard.getWinner() != null) {
                return;
            }
            String place = String.valueOf(column)+String.valueOf(row);
            if (selectPiece == null) {
                selectPiece = place;
                showMessage("select:" + place);
            } else {
                String moveStr = selectPiece+"-"+place;
                Move userMove = Move.move(moveStr);
                if (packBoard.moveLegal(userMove)) {
                    message("%s-%s", selectPiece, place);
                    selectPiece = null;
                } else if (packBoard.getContent(Board.index(column, row)).equals(packBoard.nextMove())) {
                    selectPiece = place;
                    showMessage("select:" + place);
                } else {
                    showMessage("ILLEGAL MOVE!  Please choose another place");
                }

            }
        }

    }

    private void refreshPieces() {

        for (List<Piece> row : pieces) {
            for (Piece piece : row) {
                PieceState state = packBoard.getContent(piece.column, piece.row);
                if (RED.equals(state)) {
                    piece.setIcon(redIcon);
                } else if (BLUE.equals(state)) {
                    piece.setIcon(blueIcon);
                } else if (EMPTY.equals(state)) {
                    piece.setIcon(emptyIcon);
                } else {
                    piece.setIcon(blockIcon);
                }
            }
        }

    }

    private void initPieces() {
        for (int i = 0 ; i < PIECE_NUM; ++i) {
            List<Piece> row = new ArrayList<>();
            for (int j = 0; j < PIECE_NUM; ++j) {
                Piece piece = new Piece(j, i);
                row.add(piece);
                add(piece);
            }
            pieces.add(row);
        }
    }

    private void initSysButtons(int sysButtonNum) {
        for (int i = 0; i < sysButtonNum; ++i) {
            SystemOperation sysButton = new SystemOperation(i * BUTTON_WIDTH, 100, sysButtonTitles.get(i), sysButtonCommands.get(i));
            add(sysButton);
        }

        // Handle the block button separately
        JButton block = new JButton();
        block.setBackground(Color.white);
        block.setLocation(sysButtonNum * BUTTON_WIDTH, 100);
        block.setSize(100, 50);
        block.setText("Block");
        block.setMargin(new Insets(0, 0, 0, 0));
        block.setFont(buttonFont);
        block.addActionListener((actionEvent->{
            message("%s %s", "block", selectPiece);
            if (selectPiece == null) {
                showMessage("choose a piece to block");
            } else {
                message("%s %s", "block", selectPiece);
                showMessage("block: " + selectPiece);
                selectPiece = null;
            }
        }));
        add(block);
    }

    private void initLabels() {
        msgBox = new JLabel();
        msgBox.setFont(new Font("Times New Roman", Font.BOLD, 30));
        msgBox.setHorizontalAlignment(JLabel.CENTER);
        msgBox.setBackground(Color.white);
        msgBox.setLocation(0, 50);
        msgBox.setSize(PIECE_NUM * PIECE_SIZE, 50);
        msgBox.setText("Welcome to ataxx");
        add(msgBox);

        score = new JLabel();
        score.setFont(new Font("Black", Font.BOLD, 30));
        score.setHorizontalAlignment(JLabel.CENTER);
        score.setBackground(Color.white);
        score.setLocation(0, 0);
        score.setSize(PIECE_NUM * PIECE_SIZE, 50);
        score.setText("Please choose player type to start");
        add(score);
    }

    private ImageIcon getIcon(String path) {
        ImageIcon newIcon = new ImageIcon(path);
        Image iconFilter = newIcon.getImage().getScaledInstance(PIECE_SIZE, PIECE_SIZE, newIcon.getImage().SCALE_DEFAULT);
        return new ImageIcon(iconFilter);
    }

    private void showMessage(String message) {
        msgBox.setText(message);
    }


    // These methods could be modified
	
    @Override
    public void update(Board board) {
        packBoard = board;
        refreshPieces();
        score.setText(board.getScore() + " -- Next move: " + board.nextMove().toString());

    }

    @Override
    public String getCommand(String prompt) {
        try {
            return userCommands.take();
        } catch (InterruptedException excp) {
            throw new Error("unexpected interrupt");
        }
    }

    @Override
    public void announceWinner(PieceState state) {
        if (state == EMPTY) {
            showMessage("Tie game.");
        } else {
            showMessage(state.toString() + " wins");
        }
    }

    @Override
    public void announceMove(Move move, PieceState player) {
        showMessage(player.toString() + ": " + move.toString());
    }

    @Override
    public void message(String format, Object... args) {
        userCommands.offer(String.format(format, args));
    }

    @Override
    public void error(String format, Object... args) {
        showMessage(String.format(format, args));
    }

    public void setVisible(boolean b) {
        super.setVisible(true);
    }

    private final ArrayBlockingQueue<String> userCommands = new ArrayBlockingQueue<>(1);
    private final List<List<Piece>> pieces = new ArrayList<List<Piece>>();
    private String selectPiece;
    private Board packBoard;

    private JLabel msgBox;
    private JLabel score;

    private final Font buttonFont;

    private final ImageIcon redIcon;
    private final ImageIcon blueIcon;
    private final ImageIcon emptyIcon;
    private final ImageIcon blockIcon;
    /**
     * If you want to add or modify a button, just edit the two lists below
     * */
    private final List<String> sysButtonTitles = Arrays.asList(
            "New Game",
            "AI Red",
            "Manual Red",
            "AI Blue",
            "Manual Blue",
            "Pass"
    );

    private final List<String> sysButtonCommands = Arrays.asList(
            "new",
            "ai red",
            "manual red",
            "ai blue",
            "manual blue",
            "-"
    );

    private final String RED_PATH = "src/img/red.jpg";
    private final String BLUE_PATH = "src/img/blue.jpg";
    private final String EMPTY_PATH = "src/img/empty.png";
    private final String BLOCK_PATH = "src/img/block.png";

    private final int LEFT_MARGIN = 0;
    private final int UP_MARGIN = 150;
    private final int BUTTON_WIDTH = 100;
    private final int PIECE_SIZE = 100;
    private final int PIECE_NUM = 7;

}
