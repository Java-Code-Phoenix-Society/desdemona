package dev.jcps;

/*
*****************************************************************************

 Desdemona: The Java Othello game.
 Copyright (C) 2001 Kenneth D. Huffman.

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License (gnu.org/copyleft/gpl.html) for more details.

 The author can be reached at www.huffmancoding.com.

 Thanks to:
 - The original IOCCC winner that wrote the logic in Obfuscated C.
 *****************************************************************************
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.LinkedList;

/* Original source
#define D define
#D Y return
#D R for
#D e while
#D I printf
#D l int
#D C y=v+111;H(x,v)*y++= *x
#D H(a,b)R(a=b+11;a<b+89;a++)
#D s(a)t=scanf("%d",&a)
l V[1100],u,r[]={-1,-11,-10,-9,1,11,10,9},h[]={11,18,81,88},ih[]={22,27,72,77},
bz,lv=60,*x,*y,m,t;S(d,v,f,a,b)l*v;{l c=0,*n=v+100,bw=d<u-1?a:-9000,w,z,i,zb,q=
3-f;if(d>u){R(w=i=0;i<4;i++)w+=(m=v[h[i]])==f?300:m==q?-300:(t=v[ih[i]])==f?-50
:t==q?50:0;return w;}H(z,0){if(GZ(v,z,f,100)){c++;w= -S(d+1,n,q,-b,-bw);if(w>bw
){zb=z;bw=w;if(w>=b||w>=8003)Y w;}}}if(!c){bz=0;C;Y-S(d+1,n,q,-b,-bw);}bz=zb;Y
d>=u-1?bw+(c<<3):bw;}main(){R(;t<1100;t+=100)R(m=0;m<100;m++)V[t+m]=m<11||m>88
||(m+1)%10<2?3:0;V[44]=V[55]=1;V[45]=V[54]=2;I("Level:");s(u);e(lv>0){do{I("Yo\
u:");s(m);}e(!GZ(V,m,2,0)&&m!=99);if(m!=99)lv--;if(lv<15&&u<10)u+=2;I("Wait\n")
;I("Value:%d\n",S(0,V,1,-9000,9000));I("move: %d\n",(lv-=GZ(V,bz,1,0),bz));}}GZ
(v,z,f,o)l*v;{l*j,q=3-f,g=0,i,h,*k=v+z;if(*k==0)R(i=7;i>=0;i--){j=k+(h=r[i]);e(
*j==q)j+=h;if(*j==f&&j-h!=k){if(!g){g=1;C;}e(j!=k)*((j-=h)+o)=f;}}Y g;}
*/

/**
 * This class plays the game Othello.  The original version of this program
 * was written in C by Roemer B. Lievaart and won the International Obfuscated
 * C Code Contest in 1987.  I translated it into slightly more readable code.
 * I used all inner classes because I was in a creative mood.
 *
 * @author Ken Huffman
 */
public class Desdemona {
    /**
     * the Square property for occupier changes
     */
    public static final String OCCUPIER_PROPERTY = "Occupier";
    /**
     * the number of Squares on each side of the board
     */
    public static final int BOARD_SIZE = 8;
    /**
     * the X offsets for each of 8 directions of flipitude
     */
    public static final int[] deltaX = {-1, 0, 1, -1, 1, -1, 0, 1};
    /**
     * the Y offsets for each of 8 directions of flipitude
     */
    public static final int[] deltaY = {-1, -1, -1, 0, 0, 1, 1, 1};
    /**
     * the background color of the board
     */
    private static final Color BOARD_COLOR = Color.green.darker().darker();
    /**
     * the main board being displayed
     */
    private final BoardView itsBoardView;
    /**
     * the player of the white pieces
     */
    private Player itsWhitePlayer;
    /**
     * the player of the black pieces
     */
    private Player itsBlackPlayer;
    /**
     * the current player taking his turn
     */
    private Player itsCurrentPlayer;
    /**
     * the consecutive number of times a player has not been able to play
     */
    private int itsConsecutivePasses;

    /**
     * Constructs an Othello game.
     *
     * @param exitOnClose whether the JVM should exit when the frame is closed.
     */
    public Desdemona(boolean exitOnClose) {
        itsBoardView = new BoardView(exitOnClose);

        startGame();
    }

    /**
     * Plays a game and exits when the user is done.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        new Desdemona(true);
    }

    /**
     * Starts a game by choosing two players, reseting the board, and starts
     * the first player.
     */
    public void startGame() {
        itsWhitePlayer = new HumanPlayer();
        itsBlackPlayer = new ComputerPlayer();

        itsWhitePlayer.setOpponent(itsBlackPlayer);
        itsBlackPlayer.setOpponent(itsWhitePlayer);

        itsBoardView.getBoard().reset(itsWhitePlayer, itsBlackPlayer);

        itsCurrentPlayer = itsWhitePlayer;
        itsConsecutivePasses = 0;
        takeTurn();
    }

    /**
     * Instructs the current player to take a turn.
     */
    public void takeTurn() {
        itsCurrentPlayer.requestMove(itsBoardView);
    }

    /**
     * Updates a turn on the board. Called when itsCurrentPlayer has made up its
     * "mind."
     *
     * @param x the horizontal position of the empty Square to fill
     * @param y the vertical position of the empty Square to fill
     */
    public void turnTaken(int x, int y) {
        Board b = itsBoardView.getBoard();
        b.flip(itsCurrentPlayer, x, y, b); // could assert that it returns true
        itsConsecutivePasses = 0;
        nextPlayer();
    }

    /**
     * Skips the current player's turn.  Should only be called when
     * itsCurrentPlayer cannot take any turn.
     */
    public void turnPassed() {
        // could determine if itsCurrentPlayer is passing legally.
        ++itsConsecutivePasses;
        nextPlayer();
    }

    /**
     * Changes itsCurrentPlayer to start the next turn.  If neither player can
     * play, the game is ended and the result is displayed.
     */
    public void nextPlayer() {
        if (itsConsecutivePasses < 2) {
            itsCurrentPlayer = itsCurrentPlayer.getOpponent();
            takeTurn();
        } else {
            itsBoardView.setStatus("Game over.");

            Player opponent = itsCurrentPlayer.getOpponent();
            int currentPlayersCount = 0;
            int opponentPlayersCount = 0;
            Board b = itsBoardView.getBoard();
            for (int x = 0; x < BOARD_SIZE; ++x) {
                for (int y = 0; y < BOARD_SIZE; ++y) {
                    Player occupier = b.getSquare(x, y).getOccupier();
                    if (occupier == itsCurrentPlayer) {
                        ++currentPlayersCount;
                    } else if (occupier == opponent) {
                        ++opponentPlayersCount;
                    }
                }
            }

            String result;
            if (currentPlayersCount == opponentPlayersCount) {
                result = "The game ended in a tie.";
            } else {
                result = (currentPlayersCount > opponentPlayersCount ?
                        itsCurrentPlayer : opponent).getName() + " won the game.";
            }

            if (JOptionPane.showConfirmDialog(itsBoardView,
                    result + "\nPlay again?",
                    "Game over",
                    JOptionPane.YES_NO_OPTION) ==
                    JOptionPane.YES_OPTION) {
                startGame();
            }
        }
    }

    /**
     * This base class implements the minimal functions required of a player.
     * This abstract class does not implement any logic for knowing how to play
     * the game.
     */
    abstract public class Player {
        /**
         * The opposing player.
         */
        private Player itsOpponent;

        /**
         * This function must be defined to return the name of player for
         * display purposes.
         *
         * @return the name of the user
         */
        abstract public String getName();

        /**
         * This function should start the "thinking" process for taking a turn.
         * It should return without blocking while the "thinking" is going on,
         * but eventually the player should call to Desdemona.turnTaken or,
         * if no valid move is possible, call Desdemona.turnPassed.
         *
         * @param bv the view of the board to play.
         */
        abstract public void requestMove(BoardView bv);

        /**
         * Returns the player's opponent.
         *
         * @return the opposing player.
         */
        public Player getOpponent() {
            return itsOpponent;
        }

        /**
         * Records the player's opponent.
         *
         * @param opp the opposing player.
         */
        public void setOpponent(Player opp) {
            itsOpponent = opp;
        }
    }

    /**
     * This class implements a player taking turns by interacting with the Views
     * and listening for mouse clicks.
     */
    public class HumanPlayer extends Player implements MouseListener {
        /**
         * The list of possible moves that the player can take for this turn.
         */
        private ArrayList<SquareView> itsPossibleMoves;

        /**
         * Returns a gender-neutral name of the human player.
         *
         * @return the string "You".
         */
        public String getName() {
            return "You";
        }

        /**
         * Starts a human's turn by highlighting playable squares and turning on
         * the mouse listener.
         *
         * @param bv the view of the board to be played.
         */
        public void requestMove(BoardView bv) {
            itsPossibleMoves = new ArrayList<>();

            Board b = bv.getBoard();

            // add a mouse listener to the playable squares
            for (int x = 0; x < BOARD_SIZE; ++x) {
                for (int y = 0; y < BOARD_SIZE; ++y) {
                    if (b.flip(this, x, y, null)) {
                        SquareView sv = bv.getSquareView(b.getSquare(x, y));
                        sv.addMouseListener(this);
                        itsPossibleMoves.add(sv); // remember for cleanup
                    }
                }
            }

            // display message if he can play, otherwise complete his turn now.
            if (!itsPossibleMoves.isEmpty()) {
                bv.setStatus("Click on a square that highlights.");
            } else {
                itsPossibleMoves = null;
                turnPassed();
            }
        }

        /**
         * Completes a human's turn when he clicks on a playable square.
         *
         * @param e event describing the SquareView he clicked on.
         */
        public void mouseClicked(MouseEvent e) {
            // turn off the mouse listener to the squares we were listening to.
            for (SquareView sv : itsPossibleMoves) {
                sv.removeMouseListener(this);
                sv.normal();
            }
            itsPossibleMoves = null;

            // find the square he clicked on, and complete the turn
            Square s = ((SquareView) e.getComponent()).getSquare();
            turnTaken(s.getX(), s.getY());
        }

        /**
         * Turns on highlighting as the mouse enters the playable square.
         *
         * @param e event describing the SquareView he entered.
         */
        public void mouseEntered(MouseEvent e) {
            ((SquareView) e.getComponent()).highlight();
        }

        /**
         * Turns off highlighting as the mouse exits the playable square.
         *
         * @param e event describing the SquareView he exited.
         */
        public void mouseExited(MouseEvent e) {
            ((SquareView) e.getComponent()).normal();
        }

        /**
         * Does nothing.
         *
         * @param e ignored.
         */
        public void mousePressed(MouseEvent e) {
        }

        /**
         * Does nothing.
         *
         * @param e ignored.
         */
        public void mouseReleased(MouseEvent e) {
        }
    }

    /**
     * This class implements a player by ranking each possible move and choosing
     * the highest scoring one.  It determines the value of each possible move
     * by recursively determining and scoring each of its successive moves.
     */
    public class ComputerPlayer extends Player {
        /**
         * bigger than the best possible score
         */
        private final int BIG_SCORE = 9000;
        /**
         * The score for grabbing a corner
         */
        private final int CORNER_SCORE = 300;
        /**
         * The score for forcing the opponent to grab an inside corner
         */
        private final int INSIDE_SCORE = 50;
        /**
         * The best score possible
         */
        private final int PERFECT_SCORE = 8003;
        /**
         * X array indexes of the corner positions we want
         */
        private final int[] cornerX = {0, BOARD_SIZE - 1, 0, BOARD_SIZE - 1};
        /**
         * Y array indexes of the corner positions we want
         */
        private final int[] cornerY = {0, 0, BOARD_SIZE - 1, BOARD_SIZE - 1};
        /**
         * X array indexes of the adjacent inner corner positions we don't want
         */
        private final int[] insideX = {1, BOARD_SIZE - 2, 1, BOARD_SIZE - 2};
        /**
         * Y array indexes of the adjacent inner corner positions we don't want
         */
        private final int[] insideY = {1, 1, BOARD_SIZE - 2, BOARD_SIZE - 2};
        /**
         * How deep the computer wants us to think
         */
        private final LinkedList<Board> itsFreeBoards = new LinkedList<>();
        /**
         * How deep the computer wants us to think
         */
        private int itsLookAheadLevel;
        /**
         * move with the highest score
         */
        private Square itsBestMove;

        /**
         * Returns a gender-neutral name of the computer player.
         *
         * @return the string "Computer".
         */
        public String getName() {
            return "Computer";
        }

        /**
         * Starts a computer's turn by starting a background thread to determine
         * the best move, then taking it in the AWT Event thread.
         *
         * @param bv the view of the board to be played.
         */
        public void requestMove(BoardView bv) {
            bv.setStatus("Computer is cogitating...");

            final Board b = bv.getBoard();
            itsLookAheadLevel = bv.getLevel();

            new Thread(() -> {
                play(0, b, ComputerPlayer.this, -BIG_SCORE, BIG_SCORE);

                // update the board view in the foreground thread
                SwingUtilities.invokeLater(
                        () -> {
                            if (itsBestMove != null) {
                                turnTaken(itsBestMove.getX(),
                                        itsBestMove.getY());
                            } else {
                                turnPassed();
                            }
                        }
                );
            }).start();
        }

        /**
         * Sets itsBestMove and returns the best possible score for the board.
         *
         * @param nest          the current level of recursion nesting
         * @param b             the board to be played and scored
         * @param me            the player taking the turn
         * @param previousScore the previous best score
         * @param goodScore     the score to beat
         */
        public int play(int nest, Board b, Player me, int previousScore, int goodScore) {
            Player opponent = me.getOpponent();

            if (nest > itsLookAheadLevel) {
                // at the deepest level of recursion, the score is determined by
                // who has the corners and who is likely to give up the empty
                // corners by occupying the squares diagonally adjacent to the
                // corners
                int score = 0;
                for (int i = 0; i < cornerX.length; ++i) {
                    Player occupier = b.getSquare(cornerX[i], cornerY[i]).
                            getOccupier();
                    if (occupier == me) score += CORNER_SCORE;
                    else if (occupier == opponent) score -= CORNER_SCORE;
                    else {
                        occupier = b.getSquare(insideX[i], insideY[i]).
                                getOccupier();
                        if (occupier == me) score -= INSIDE_SCORE;
                        else if (occupier == opponent) score += INSIDE_SCORE;
                    }
                }

                return score;
            }

            int bestScore = (nest < itsLookAheadLevel - 1) ?
                    previousScore : -BIG_SCORE;
            Square tempMove = null;
            int possibleMoves = 0;

            // Since we not at the lowest level of recursion, our best move is
            // the square that yields our opponent's lowest scoring best move on
            // his successive turn.

            // to avoid messing up the original board, we'll create a temporary.
            // clone()ing new boards is not as efficient as reusing old boards
            Board scratchBoard;
            if (itsFreeBoards.isEmpty()) {
                scratchBoard = new Board();
            } else {
                scratchBoard = itsFreeBoards.removeLast();
            }
            scratchBoard.copyFrom(b);

            for (int x = 0; x < BOARD_SIZE; ++x) {
                for (int y = 0; y < BOARD_SIZE; ++y) {
                    if (b.flip(me, x, y, scratchBoard)) {
                        ++possibleMoves;
                        // our score is the opposite of the next player's best
                        int score = -play(nest + 1, scratchBoard, opponent,
                                -goodScore, -bestScore);
                        if (score > bestScore) {
                            tempMove = b.getSquare(x, y);
                            bestScore = score;
                            if (score >= goodScore || score >= PERFECT_SCORE) {
                                itsFreeBoards.addLast(scratchBoard);

                                return score;
                            }
                        }

                        // need new scratch board, cause flip() used the old one
                        scratchBoard.copyFrom(b);
                    }
                }
            }

            if (possibleMoves == 0) {
                itsBestMove = null;
                // since we can't move, the score is just based on his best
                bestScore = -play(nest + 1, scratchBoard, opponent, -goodScore,
                        -bestScore);
            } else {
                // save off our best move, and bump up our bestScore according to
                // the number of possible moves we could make.
                itsBestMove = tempMove;
                if (nest >= itsLookAheadLevel - 1) {
                    bestScore += (possibleMoves << 3);
                }
            }

            itsFreeBoards.addLast(scratchBoard);

            return bestScore;
        }
    }

    /**
     * This class implements a single square on the board.  It knows its
     * position in life and whether a piece is occupying it.  It does not
     * know what it looks like nor anything about its neighbors.  It supports
     * property change listeners for its occupier.
     */
    public class Square {
        /**
         * the x position of the square
         */
        private final int itsX;
        /**
         * the y position of the square
         */
        private final int itsY;
        /**
         * the player whose piece is this square
         */
        private Player itsOccupier = null;
        /**
         * a holder for listener(s)
         */
        private PropertyChangeSupport itsBeanSupport = null;

        /**
         * Constructor.
         *
         * @param x the horizontal position on the board.
         * @param y the vertical position on the board.
         */
        public Square(int x, int y) {
            itsX = x;
            itsY = y;
        }

        /**
         * Adds a listener to when the square changes occupiers.
         *
         * @param listener the listener for occupation changes.
         */
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            if (itsBeanSupport == null) {
                itsBeanSupport = new PropertyChangeSupport(this);
            }

            itsBeanSupport.addPropertyChangeListener(listener);
        }

        /**
         * Returns the horizontal position of the square.
         *
         * @return the x position
         */
        public int getX() {
            return itsX;
        }

        /**
         * Returns the vertical position of the square.
         *
         * @return the y position
         */
        public int getY() {
            return itsY;
        }

        /**
         * Returns the player whose piece is currently occupying this square.
         *
         * @return the occupying player, or null if it is empty
         */
        public Player getOccupier() {
            return itsOccupier;
        }

        /**
         * Set the player who is to occupy this square.
         *
         * @param newOccupier the occupying player to occupy the square
         */
        public void setOccupier(
                Player newOccupier
        ) {
            if (newOccupier != itsOccupier) {
                Player previousOccupier = itsOccupier;
                itsOccupier = newOccupier;

                if (itsBeanSupport != null) {
                    itsBeanSupport.firePropertyChange(OCCUPIER_PROPERTY,
                            previousOccupier, itsOccupier);
                }
            }
        }
    }

    /**
     * This class implements a grid of squares.  It knows how a move can cause
     * flips, but it does not have an inherent strategy on how to play well.
     * It does not know what it looks like.
     */
    public class Board {
        /**
         * the doubly-indexed array of squares
         */
        private final Square[][] itsSquares = new Square[BOARD_SIZE][BOARD_SIZE];

        /**
         * Constructor initializes the doubly-indexed array to empty squares.
         */
        public Board() {
            for (int x = 0; x < BOARD_SIZE; ++x) {
                itsSquares[x] = new Square[BOARD_SIZE];
                for (int y = 0; y < BOARD_SIZE; ++y) {
                    itsSquares[x][y] = new Square(x, y);
                }
            }
        }

        /**
         * Resets the board to the opening position with only the four middle
         * squares occupied.
         *
         * @param player1 first player to place pieces on the board
         * @param player2 second player to place pieces on the board
         */
        public void reset(Player player1, Player player2) {
            for (int x = 0; x < BOARD_SIZE; ++x) {
                for (int y = 0; y < BOARD_SIZE; ++y) {
                    itsSquares[x][y].setOccupier(null);
                }
            }

            itsSquares[3][3].setOccupier(player1);
            itsSquares[3][4].setOccupier(player2);
            itsSquares[4][3].setOccupier(player2);
            itsSquares[4][4].setOccupier(player1);
        }

        /**
         * Clones a board object with its own squares and the current occupiers.
         */
        public void copyFrom(Board srcBoard) {
            for (int x = 0; x < BOARD_SIZE; ++x) {
                Square[] srcColumn = srcBoard.itsSquares[x];
                Square[] destColumn = itsSquares[x];

                for (int y = 0; y < BOARD_SIZE; ++y) {
                    destColumn[y].setOccupier(srcColumn[y].getOccupier());
                }
            }
        }

        /**
         * Determines if any pieces would flip if a player occupied an empty
         * spot.  It will return false if the square is already occupied.
         *
         * @param player        the player taking the turn
         * @param x             the horizontal index to place the piece
         * @param y             the vertical index to place the piece
         * @param boardToUpdate "this" board (or a clone) to reflect flip
         *                      changes, can be null if the caller merely wants to know if the
         *                      move was legal.
         * @return true, if the move would cause flips and therefore be legal
         */
        public boolean flip(Player player, int x, int y, Board boardToUpdate) {
            boolean canFlip = false;
            Square s = itsSquares[x][y];
            if (s.getOccupier() == null) {
                for (int dir = 0; dir < deltaX.length; ++dir) {
                    int xDelta = deltaX[dir];
                    int yDelta = deltaY[dir];
                    int flipped = flipCountInDirection(player, x, y,
                            xDelta, yDelta);
                    if (flipped > 0) {
                        canFlip = true;
                        if (boardToUpdate == null) {
                            break; // don't need to update, just detect
                        }
                        while (flipped >= 0) {
                            boardToUpdate.itsSquares
                                    [x + flipped * xDelta][y + flipped * yDelta].
                                    setOccupier(player);
                            --flipped;
                        }
                    }
                }
            }

            return canFlip;
        }

        /**
         * Determines if any pieces would flip in a particular direction if a
         * player occupied an empty spot.
         *
         * @param p      the player taking the turn
         * @param x      the horizontal index to place the piece
         * @param y      the vertical index to place the piece
         * @param xDelta the x increment for the direction.
         * @param yDelta the y increment for the direction.
         * @return the number of opponents flipped.
         */
        private int flipCountInDirection(Player p, int x, int y, int xDelta, int yDelta) {
            int flipped = 0;
            x += xDelta;
            y += yDelta;
            if (x < 0 || y < 0 || x == BOARD_SIZE || y == BOARD_SIZE) {
                return 0;  // this direction is off the board
            }

            Player occupier;
            while ((occupier = itsSquares[x][y].getOccupier()) == p.getOpponent()) {
                ++flipped;
                x += xDelta;
                y += yDelta;

                if (x < 0 || y < 0 || x == BOARD_SIZE || y == BOARD_SIZE) {
                    return 0;  // we have walked off the edge of the board
                }
            }

            return (occupier == p) ? flipped : 0;
        }

        /**
         * Returns the square at a particular location.
         *
         * @param x the horizontal index to the square
         * @param y the vertical index to the square
         * @return the Square of the board.
         */
        public Square getSquare(int x, int y) {
            return itsSquares[x][y];
        }
    }

    /**
     * This class will display a square.  It listens to the property changes
     * of a square and responds to anyone who wants to highlight it.
     */
    public class SquareView
            extends JLabel
            implements PropertyChangeListener {
        /**
         * the square it is displaying
         */
        private final Square itsSquare;

        /**
         * Constructor.
         *
         * @param square the square it is to display
         */
        public SquareView(Square square) {
            itsSquare = square;

            setBorder(BorderFactory.createLoweredBevelBorder());
            setOpaque(true);
            itsSquare.addPropertyChangeListener(this);

            normal();
        }

        /**
         * Highlights the square.
         */
        public void highlight() {
            setBackground(BOARD_COLOR.brighter());
        }

        /**
         * Un-highlights the square.
         */
        public void normal() {
            setBackground(BOARD_COLOR);
        }

        /**
         * Returns the square it is displaying.
         *
         * @return the square being viewed
         */
        public Square getSquare() {
            return itsSquare;
        }

        /**
         * Responds to a square property change by repainting itself.
         *
         * @param e ignored
         */
        public void propertyChange(PropertyChangeEvent e) {
            repaint();
        }

        /**
         * Paints itself by calling super then drawing the occupying piece.
         *
         * @param g the graphics context
         */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Player p = itsSquare.getOccupier();
            if (p != null) {
                if (p == itsWhitePlayer) {
                    g.setColor(Color.white);
                } else {
                    g.setColor(Color.black);
                }

                g.fillOval(getWidth() / 5, getHeight() / 5,
                        getWidth() * 3 / 5, getHeight() * 3 / 5);
            }
        }
    }

    /**
     * This JFrame class own and display a board.  It can exit on close if
     * desired.
     */
    public class BoardView
            extends JFrame {
        /**
         * the board for playing
         */
        private final Board itsBoard = new Board();
        /**
         * panel containing the SquareViews
         */
        private final JPanel itsBoardPanel =
                new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        /**
         * the slider for controlling the level
         */
        private final JSlider itsLevelSlider = new JSlider(1, 10, 3);
        /**
         * the status text label
         */
        private final JLabel itsStatus = new JLabel(" ");

        /**
         * Constructor.
         *
         * @param exitOnClose whether the program should exit when the frame is
         *                    closed.
         */
        public BoardView(boolean exitOnClose) {
            super("Desdemona");

            setSize(296, 394);
            getContentPane().setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(10, 10, 0, 10);

            itsBoardPanel.setBorder(BorderFactory.createRaisedBevelBorder());
            itsBoardPanel.setBackground(BOARD_COLOR);

            for (int y = 0; y < BOARD_SIZE; ++y) {
                for (int x = 0; x < BOARD_SIZE; ++x) {
                    itsBoardPanel.add(new SquareView(itsBoard.getSquare(x, y)));
                }
            }

            c.gridx = 0;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.BOTH;
            getContentPane().add(itsBoardPanel, c);

            itsLevelSlider.setLabelTable(itsLevelSlider.createStandardLabels(1));
            itsLevelSlider.setPaintLabels(true);
            itsLevelSlider.setMajorTickSpacing(1);
            itsLevelSlider.setPaintTicks(true);
            itsLevelSlider.setSnapToTicks(true);

            c.gridwidth = 1;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            getContentPane().add(new JLabel("Easy"), c);

            c.gridx = GridBagConstraints.RELATIVE;
            c.weightx = 1.0;
            getContentPane().add(itsLevelSlider, c);

            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 0.0;
            getContentPane().add(new JLabel("Hard"), c);

            c.gridx = 0;
            c.weightx = 1.0;
            getContentPane().add(itsStatus, c);

            Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameDim = getSize();
            setLocation((screenDim.width - frameDim.width) / 2,
                    (screenDim.height - frameDim.height) / 2);
            setVisible(true);

            if (exitOnClose) {
                addWindowListener(
                        new WindowAdapter() {
                            public void windowClosing(
                                    WindowEvent e
                            ) {
                                System.exit(0);
                            }
                        }
                );
            }
        }

        /**
         * Returns the board owned by the view.
         *
         * @return the board being displayed by this view.
         */
        public Board getBoard() {
            return itsBoard;
        }

        /**
         * Returns the level dictated by the user.
         *
         * @return the recursion level for the computer.
         */
        public int getLevel() {
            return itsLevelSlider.getValue();
        }

        /**
         * Sets the status text on the view.
         *
         * @param text the text for the bottom of the frame.
         */
        public void setStatus(String text) {
            itsStatus.setText(text);
        }

        /**
         * Returns the view of the square on the board.
         *
         * @param s the square to get the view of.
         */
        public SquareView getSquareView(Square s) {
            return (SquareView) itsBoardPanel.getComponent(s.getY() * BOARD_SIZE + s.getX());
        }
    }
}
