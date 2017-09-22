package minesweeper;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Random;
import java.awt.event.*;
import javax.imageio.ImageIO;

public class game extends JFrame {
    
    private JButton[][] buttons;  // The Grid buttons
    private JPanel topPanel;  // Top panel containing labels and a smile button
    private JPanel bottomPanel;  // Bottom panel containing the grid of buttons
    private JLabel numberOfRemainedFlags;  // Number of flags remaining to be used
    private JButton minerSmilingFace;  // The smile button ;-)
    private JLabel timeLabel;  // Label showing time elapsed

    private int noOfMines = 0;  // The no. of mines in the field
    private int[][] mineLand;  // 2-D array containing info for each block
    private boolean[][] revealed;  // Whether the button has been clicked
    private int noOfRevealed;  // How many of them?
    private boolean[][] flagged;  // Or the got flagged?
    
    private Image smiley;
    private Image newSmiley;
    private Image flag;
    private Image newFlag;
    private Image mine;
    private Image newMine;
    private Image dead;
    private Image newDead;
    
    private boolean smiling;  // Is he? Or is he not?

    public static final int MAGIC_SIZE = 30;

    
    public game(int size, int toughness) {
        noOfMines = size*(1 + toughness/2);
        this.setSize(size*MAGIC_SIZE, size*MAGIC_SIZE + 50);
        this.setTitle("Minesweeper");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    }

    private void setMines(int size) {
        Random rand = new Random();
        
        mineLand = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                mineLand[i][j] = 0;
            }
        }

        int count = 0;
        int xPoint;
        int yPoint;
        while(count<noOfMines) {
            xPoint = rand.nextInt(size);
            yPoint = rand.nextInt(size);
            if (mineLand[xPoint][yPoint]!=-1) {
                mineLand[xPoint][yPoint]=-1;  // -1 represents bomb
                count++;
            }
        }
        
        // Fill boxes adjacent to mines with numbers
        for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
            if (mineLand[i][j]==-1) {
                    for (int k = -1; k <= 1 ; k++) {
                    for (int l = -1; l <= 1; l++) {
                        // In boundary cases
                        try {
                            if (mineLand[i+k][j+l]!=-1) {
                                mineLand[i+k][j+l] += 1;
                            }
                        }
                        catch (Exception e) {
                            // Do nothing
                        }
                    }
                    }
            }
        }
        }
    }

    public void main(game frame, int size) {

        // Some instantiation
        GameEngine gameEngine = new GameEngine(frame);
        MyMouseListener myMouseListener = new MyMouseListener(frame);
        JPanel mainPanel = new JPanel();

        topPanel = new JPanel();
        bottomPanel = new JPanel();

        this.noOfRevealed = 0;

        revealed = new boolean[size][size];
        flagged = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                revealed[i][j] = false;
                flagged[i][j] = false;
            }
        }

        // Images
        try {
            smiley = ImageIO.read(getClass().getResource("images/Smiley.png"));
            newSmiley = smiley.getScaledInstance(MAGIC_SIZE, MAGIC_SIZE, java.awt.Image.SCALE_SMOOTH);

            dead = ImageIO.read(getClass().getResource("images/dead.png"));
            newDead = dead.getScaledInstance(MAGIC_SIZE, MAGIC_SIZE, java.awt.Image.SCALE_SMOOTH);

            flag = ImageIO.read(getClass().getResource("images/flag.png"));
            newFlag = flag.getScaledInstance(MAGIC_SIZE, MAGIC_SIZE, java.awt.Image.SCALE_SMOOTH);

            mine = ImageIO.read(getClass().getResource("images/mine.png"));
            newMine = mine.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
        }
        catch (Exception e){
        }

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        BoxLayout g1 = new BoxLayout(topPanel, BoxLayout.X_AXIS);
        topPanel.setLayout(g1);

        JLabel jLabel1 = new JLabel(" Flags = ");
        jLabel1.setAlignmentX(Component.LEFT_ALIGNMENT);
        jLabel1.setHorizontalAlignment(JLabel.LEFT);
       numberOfRemainedFlags = new JLabel(""+this.noOfMines);

        smiling = true;
        minerSmilingFace = new JButton(new ImageIcon(newSmiley));
        minerSmilingFace.setPreferredSize(new Dimension(MAGIC_SIZE, MAGIC_SIZE));
        minerSmilingFace.setMaximumSize(new Dimension(MAGIC_SIZE, MAGIC_SIZE));
        minerSmilingFace.setBorderPainted(true);
        minerSmilingFace.setName("smileButton");
        minerSmilingFace.addActionListener(gameEngine);

        JLabel jLabel2 = new JLabel(" Time :");
        timeLabel = new JLabel("0");
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        timeLabel.setHorizontalAlignment(JLabel.RIGHT);

        topPanel.add(jLabel1);
        topPanel.add(numberOfRemainedFlags);
        topPanel.add(Box.createRigidArea(new Dimension((size-1)*15 - 80,50)));
        topPanel.add(minerSmilingFace, BorderLayout.PAGE_START);
        topPanel.add(Box.createRigidArea(new Dimension((size-1)*15 - 85,50)));
        topPanel.add(jLabel2);
        topPanel.add(timeLabel);
        
        GridLayout g2 = new GridLayout(size, size);
       bottomPanel.setLayout(g2);

        buttons = new JButton[size][size];

        for (int i=0; i<size; i++) {
            for (int j=0; j<size ; j++ ) {
                buttons[i][j] = new JButton();
                buttons[i][j].setPreferredSize(new Dimension(12, 12));
                buttons[i][j].setBorder(new LineBorder(Color.BLACK));
                buttons[i][j].setBorderPainted(true);
                buttons[i][j].setName(i + " " + j);
                buttons[i][j].addActionListener(gameEngine);
                buttons[i][j].addMouseListener(myMouseListener);
               bottomPanel.add(buttons[i][j]);
            }
        }

        // Both panels done

        mainPanel.add(topPanel);
        mainPanel.add(bottomPanel);
        frame.setContentPane(mainPanel);
        this.setVisible(true);
        
        // Algorithms
        setMines(size);

        // The timer
        timeThread timer = new timeThread(this);
        timer.start();

    }

    // Increase timer every second
    public void timer() {
        String[] time = this.timeLabel.getText().split(" ");
        int time0 = Integer.parseInt(time[0]);
        ++time0;
        this.timeLabel.setText(Integer.toString(time0) + " s");
    }

    // Change icon upon clicking smile Button
    public void changeSmile() {
        if (smiling) {
            smiling=false;
            minerSmilingFace.setIcon(new ImageIcon(newDead));
        } else {
            smiling=true;
            minerSmilingFace.setIcon(new ImageIcon(newSmiley));
        }
    }

    // If a block is right clicked
    public void buttonRightClicked(int x, int y) {
        if(!revealed[x][y]) {
            if (flagged[x][y]) {
                buttons[x][y].setIcon(null);
                flagged[x][y] = false;
                int old = Integer.parseInt(this.numberOfRemainedFlags.getText());
                ++old;
                this.numberOfRemainedFlags.setText(""+old);
            }
            else {
                if (Integer.parseInt(this.numberOfRemainedFlags.getText())>0) {
                    buttons[x][y].setIcon(new ImageIcon(newFlag));
                    flagged[x][y] = true;
                    int old = Integer.parseInt(this.numberOfRemainedFlags.getText());
                    --old;
                    this.numberOfRemainedFlags.setText(""+old);
                }
            }
        }
    }

    private boolean gameWon() {
        // noOfRevealed + noOfMines must be equal to the total no. of boxes
        return (this.noOfRevealed) ==
                        (Math.pow(this.mineLand.length, 2) - this.noOfMines);
    }

    // When a block is clicked
    public void buttonClicked(int x, int y) {
        if(!revealed[x][y] && !flagged[x][y]) {
            revealed[x][y] = true;

            switch (mineLand[x][y]) {
                case -1:
                    try {
                        buttons[x][y].setIcon(new ImageIcon(newMine));
                    } catch (Exception e1) {
                    }
                    buttons[x][y].setBackground(Color.RED);
                    try {
                        minerSmilingFace.setIcon(new ImageIcon(newDead));
                    } catch (Exception e2) {
                    }

                    JOptionPane.showMessageDialog(this, "Game Over !", null,
                            JOptionPane.ERROR_MESSAGE);


                    System.exit(0);

                    break;

                case 0:
                    buttons[x][y].setBackground(Color.lightGray);
                    ++this.noOfRevealed;

                    if (gameWon()) {
                        JOptionPane.showMessageDialog(rootPane,
                            "Congratulations! You've Won");

                        System.exit(0);
                    } // Winning condition

                    // Else simply recurse around
                    for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        try {
                            buttonClicked(x + i, y + j);
                        }
                        catch (Exception e3) {
                            // Do nothing
                        }
                    }
                    }

                    break;

                default:
                    buttons[x][y].setText(Integer.toString(mineLand[x][y]));
                    buttons[x][y].setBackground(Color.LIGHT_GRAY);
                    ++this.noOfRevealed;
                    if (gameWon()) {
                        JOptionPane.showMessageDialog(rootPane, "You Won !");

                        System.exit(0);
                    }

                    break;
            }
        }
        
    }

    
}

class GameEngine implements ActionListener {
    game parent;
    
    GameEngine(game parent) {
        this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object eventSource = e.getSource();
        JButton clickedButton = (JButton) eventSource;
        String name = clickedButton.getName();
        if (name.equals("smileButton")) {
            parent.changeSmile();
        }
        else {
            String[] xy = clickedButton.getName().split(" ", 2);
            int x = Integer.parseInt(xy[0]);
            int y = Integer.parseInt(xy[1]);
            parent.buttonClicked(x, y);

        }
    }
}

class MyMouseListener implements MouseListener {
    game parent;

    MyMouseListener(game parent) {
        this.parent = parent;
    }

    public void mouseExited(MouseEvent arg0){
    }
    public void mouseEntered(MouseEvent arg0){
    }
    public void mousePressed(MouseEvent arg0){
    }
    public void mouseClicked(MouseEvent arg0){
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        if(SwingUtilities.isRightMouseButton(arg0)){
            Object eventSource = arg0.getSource();
            JButton clickedButton = (JButton) eventSource;
            String[] xy = clickedButton.getName().split(" ", 2);
            int x = Integer.parseInt(xy[0]);
            int y = Integer.parseInt(xy[1]);
            parent.buttonRightClicked(x, y);
        }
    }
}

class timeThread implements Runnable {
    private Thread t;
    private game newGame;

    timeThread(game newGame) {
        this.newGame = newGame;
    }

    public void run() {
        while(true) {
            try {
                Thread.sleep(1000);
                newGame.timer();
            }
            catch (InterruptedException e) {
                System.exit(0);
            }
        }
    }

    public void start() {
        if (t==null) {
            t = new Thread(this);
            t.start();
        }
    }
}

