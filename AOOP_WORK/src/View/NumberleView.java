package View;
import Controller.NumberleController;

import Model.INumberleModel;
import Model.NumberleModel;
import overrideMethodOfSwing.RoundedTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class NumberleView implements Observer {
    private final INumberleModel model;
    private final NumberleController controller;
    private final JFrame frame = new JFrame("Numberle");
    private final JTextField inputTextField = new JTextField(3);;
    private final StringBuilder input;
    private final JTextField[][] fields = new JTextField[INumberleModel.MAX_ATTEMPTS][7];                               //Creates a two-dimensional array that makes it easy to locate the position of the text field
    private final Map<String, JButton> buttonMap = new HashMap<>();

    private int remainingAttempts;
    private int currentPosition = 0;                                                                                    //Record current position
    Boolean showAnswer = false;
    Boolean showError = true;


    public NumberleView(INumberleModel model, NumberleController controller) {
        this.controller = controller;
        this.model = model;
        this.controller.startNewGame();
        ((NumberleModel)this.model).addObserver(this);
        initializeFrame();
        this.controller.setView(this);
        update((NumberleModel)this.model, null);
        input = controller.getCurrentGuess();
    }

    public void initializeFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(756, 725);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setOpaque(true);

        ImageIcon scaledLogoIcon = new ImageIcon(new ImageIcon("logo.png").getImage().getScaledInstance(140, 32, Image.SCALE_SMOOTH));
        JButton iconButton = new JButton(scaledLogoIcon);
        iconButton.setBorderPainted(false);
        iconButton.setContentAreaFilled(false);
        iconButton.setFocusPainted(false);
        iconButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {                                                                //Here I've set up a button, controlled by controller, that will restart the game when clicked
                if (controller.getENABLE()) {
                    model.setFlag(true, false, true);
                    restartGame();
                }
            }
        });
        menuBar.add(iconButton);
        menuBar.add(Box.createHorizontalGlue());

        ImageIcon scaledSetIcon = new ImageIcon(new ImageIcon("setting.png").getImage().getScaledInstance(33, 28, Image.SCALE_SMOOTH));
        JButton iconSetButton = new JButton(scaledSetIcon);                                                             //I've added a settings button where the user can initialise the game FLAG
        iconSetButton.setBorderPainted(false);
        iconSetButton.setContentAreaFilled(false);
        iconSetButton.setFocusPainted(false);
        iconSetButton.addActionListener(e -> showFlagDialog(frame));
        menuBar.add(iconSetButton);
        frame.setJMenuBar(menuBar);

        initializeFields();
        initializeButtons();

        frame.setVisible(true);

    }

    private void initializeFields() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel displayPanel = new JPanel();
        displayPanel.setBackground(Color.white);
        displayPanel.setLayout(new GridLayout(6, 7, 6, 6));
        displayPanel.setBorder(BorderFactory.createEmptyBorder(10, 100, 10, 100));
        Font font = new Font("Montserrat Bold", Font.PLAIN, 30);

        for (int i = 0; i < INumberleModel.MAX_ATTEMPTS; i++) {                                                         //Add each reconstructed text field object to the corresponding position by traversing the two-dimensional array.
            for (int j = 0; j < 7; j++) {
                fields[i][j] = new RoundedTextField(15, new Color(189, 199, 199), 2);
                fields[i][j].setBackground(new Color(251, 252, 255));
                fields[i][j].setEditable(false);
                fields[i][j].setHorizontalAlignment(JTextField.CENTER);
                fields[i][j].setFont(font);
                fields[i][j].setPreferredSize(new Dimension(50, 50));
                displayPanel.add(fields[i][j]);
            }
        }
        center.add(displayPanel);

        Font fontAns = new Font("Montserrat Bold", Font.PLAIN, 20);
        JPanel displayAnswer = new JPanel();
        displayAnswer.setBackground(Color.WHITE);
        displayAnswer.setLayout(new GridLayout(1, 1, 0, 0));
        displayAnswer.setBorder(BorderFactory.createEmptyBorder(-60, 100, -50, 100));
        JLabel label;
        if (showAnswer) {                                                                                               //If Show Answer is set in FLAG, it will be shown in the view
            label = new JLabel("Target Equation: " + controller.getTargetWord().replace("*", "×").replace("/", "÷"));
        } else {
            label = new JLabel("                 ");
        }
        label.setFont(fontAns);
        label.setHorizontalAlignment(JLabel.CENTER);
        displayAnswer.add(label);
        center.add(displayAnswer);
        center.add(Box.createVerticalStrut(-1), BorderLayout.SOUTH);

        frame.add(center, BorderLayout.CENTER);
    }

    private void initializeButtons() {                                                     //This is the way to initialise the buttons, I set up two JPanels, one for numbers and one for text for a better layout
        JPanel keyboardPanel = new JPanel(new BorderLayout());
        keyboardPanel.setBackground(Color.white);
        keyboardPanel.setLayout(new GridLayout(2, INumberleModel.MAX_ATTEMPTS, 5, 5));
        keyboardPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JPanel numberPanel = new JPanel(new GridLayout(1, 10, 5, 5));
        numberPanel.setBackground(Color.white);
        String[] numberKeys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};

        for (String key : numberKeys) {
            JButton button = new JButton(key);
            button.setFocusPainted(false);
            button.setFont(new Font("Montserrat Bold", Font.PLAIN, 20));
            button.setForeground(new Color(90, 99, 118));
            button.setBorderPainted(false);
            button.setBackground(new Color(220, 225, 237));
            button.setPreferredSize(new Dimension(50, 65));
            mouseClick(button);
            button.addActionListener(e -> {
                if (currentPosition < 7) {
                    fields[remainingAttempts][currentPosition].setText(key);                                            //Setting text to a text field after clicking a button
                    currentPosition++;
                }
            });
            buttonMap.put(key, button);
            numberPanel.add(button);
        }


        JPanel operationPanel = new JPanel(null);
        operationPanel.setBackground(Color.white);
        String[] operationKeys = {"Back", "+", "-", "×", "÷", "=", "Enter"};

        int x = 1; // Initial x position
        int y = 0; // y position of all buttons

        for (String key : operationKeys) {
            JButton button = new JButton(key);
            if (key.equals("Back")) {
                button.setText("");
                ImageIcon originalIcon  = new ImageIcon("back.png");
                Image originalImage = originalIcon.getImage();
                Image scaledImage = originalImage.getScaledInstance(40, 25, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);

                button.setIcon(scaledIcon);
                button.setHorizontalTextPosition(JButton.CENTER);
                button.setVerticalTextPosition(JButton.CENTER);
            }
            button.setFocusPainted(false);
            button.setFont(new Font("Montserrat Bold", Font.BOLD, 20));
            button.setForeground(new Color(90, 99, 118));
            button.setBorderPainted(false);
            button.setBackground(new Color(220, 225, 237));

            int width = key.equals("Back") || key.equals("Enter") ? 160 : 76;
            button.setBounds(x, y, width, 65);
            x += width + 5;


            mouseClick(button);
            button.addActionListener(e -> {                                              //This is the operator logic that adds the operator to the Stringbuilder after clicking on the corresponding operator
                if (currentPosition <= 7) {
                    switch (key) {
                        case "Back":
                            if (currentPosition > 0) {
                                fields[remainingAttempts][currentPosition - 1].setText("");
                                currentPosition--;
                            }
                            break;
                        case "Enter":
                            for (int i = 0; i < currentPosition; i++) {
                                input.append(fields[remainingAttempts][i].getText());
                            }
                            controller.processInput(input.toString().replace("×", "*").replace("÷", "/"));
                            break;
                        case "+":
                        case "-":
                        case "×":
                        case "÷":
                        case "=":
                            if (currentPosition < 7) {
                                fields[remainingAttempts][currentPosition].setText(key);
                                currentPosition++;
                            }
                            break;
                    }
                }
            });
            buttonMap.put(key, button);
            operationPanel.add(button);
        }


        keyboardPanel.add(numberPanel, BorderLayout.NORTH);
        keyboardPanel.add(operationPanel, BorderLayout.SOUTH);
        frame.add(keyboardPanel, BorderLayout.SOUTH);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            String message = (String) arg;
            switch (message) {
                case "Invalid Input":
                    if (showError) {
                        showAutoCloseDialog(frame, message);
                    }
//                    JOptionPane.showMessageDialog(frame, message, "Invalid Input", JOptionPane.INFORMATION_MESSAGE);
                    currentPosition = input.length();
                    remainingAttempts = INumberleModel.MAX_ATTEMPTS - model.getRemainingAttempts();
                    input.setLength(0);
                    break;
                case "Game Won":                                                                                        //All game states are initialised after a win
                    controller.setDISABLE(true);
                    showColor();
                    setButtonColors();
                    showCustomDialog(frame, "Congratulations! You won the game!");
//                    JOptionPane.showMessageDialog(frame, "Congratulations! You won the game!", "Game Won", JOptionPane.INFORMATION_MESSAGE);
                    clearAllContent();
                    currentPosition = 0;
                    remainingAttempts = 0;
//                    remainingAttempts = INumberleModel.MAX_ATTEMPTS - model.getRemainingAttempts();
                    input.setLength(0);
                    showAnswer = false;
                    frame.getContentPane().removeAll();
                    initializeFrame();
                    model.setFlag(true, false, true);
                    controller.startNewGame();
                    break;
                case "Game Over":                                                                                       //Same as above
                    showColor();
                    setButtonColors();
                    showCustomDialog(frame, message + "! \nNo Attempts, The correct equation was: " + controller.getTargetWord());
//                    JOptionPane.showMessageDialog(frame, message + "! No Attempts, The correct equation was: " + controller.getTargetWord(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    clearAllContent();
                    currentPosition = 0;
                    remainingAttempts = 0;
                    input.setLength(0);
                    showAnswer = false;
                    frame.getContentPane().removeAll();
                    initializeFrame();
                    model.setFlag(true, false, true);
                    controller.startNewGame();
                    break;
                case "Try Again":
                    controller.setDISABLE(true);
//                    System.out.println(controller.getENABLE());
                    showColor();
                    setButtonColors();
                    if (showError) {                                                                                    //If this FLAG is set, an error message is displayed
                        showAutoCloseDialog(frame, message + ", Attempts remaining: " + model.getRemainingAttempts());
                    }
//                    JOptionPane.showMessageDialog(frame, message + ", Attempts remaining: " + model.getRemainingAttempts(), "Try Again", JOptionPane.INFORMATION_MESSAGE);
                    currentPosition = 0;
                    remainingAttempts = INumberleModel.MAX_ATTEMPTS - model.getRemainingAttempts();
                    input.setLength(0);
                    break;
                case "No Equal":
                    if (showError) {                                                                                    //If this FLAG is set, an error message is displayed
                        showAutoCloseDialog(frame,  "No equal '=' sign.");
                    }
//                    JOptionPane.showMessageDialog(frame,  "No equal '=' sign.", message, JOptionPane.INFORMATION_MESSAGE);
                    currentPosition = input.length();
                    remainingAttempts = INumberleModel.MAX_ATTEMPTS - model.getRemainingAttempts();
                    input.setLength(0);
                    break;
                case "Missing Symbols":
                    if (showError) {
                        showAutoCloseDialog(frame,  "There must be at least one '+-*/'.");
                    }
//                    JOptionPane.showMessageDialog(frame,  "There must be at least one '+-*/'.", message, JOptionPane.INFORMATION_MESSAGE);
                    currentPosition = input.length();
                    remainingAttempts = INumberleModel.MAX_ATTEMPTS - model.getRemainingAttempts();
                    input.setLength(0);
                    break;
                case "Not Equal":
                    if (showError) {
                        showAutoCloseDialog(frame,  "The left side is not equal to the right.");
                    }
//                    JOptionPane.showMessageDialog(frame,  "The left side is not equal to the right.", message, JOptionPane.INFORMATION_MESSAGE);
                    currentPosition = input.length();
                    remainingAttempts = INumberleModel.MAX_ATTEMPTS - model.getRemainingAttempts();
                    input.setLength(0);
                    break;
            }
        }
    }

    private void showColor() {                                                                                          //This is the code that displays the colour after a line has been entered
        for (int i = 0; i < model.getColors().size(); i++) {
            switch (model.getColors().get(i)) {
                case "0":
                    fields[remainingAttempts][i].setBackground(new Color(47, 193, 165));
                    break;
                case "1":
                    fields[remainingAttempts][i].setBackground(new Color(247, 154, 111));
                    break;
                case "2":
                    fields[remainingAttempts][i].setBackground(new Color(164, 174, 196));
                    break;
            }
        }
    }

    private static void mouseClick(JButton button) {                                                                     //In order to improve the operability of the user and the aesthetics of the interface, I added three mouse click effects: click, hover, and release.
        button.addMouseListener(new MouseAdapter() {
            final Color originalColor = button.getBackground();
            final Color hoverColor = new Color(196, 203, 221);
            final Color pressedColor = new Color(159, 168, 192);
            final List<Color> noChangeColors = Arrays.asList(
                    new Color(47, 193, 165),
                    new Color(247, 154, 111),
                    new Color(164, 174, 196)
            );

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!noChangeColors.contains(button.getBackground())) {
                    button.setBackground(hoverColor);
                }
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!noChangeColors.contains(button.getBackground())) {
                    button.setBackground(originalColor);
                }
                button.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException classNotFoundException) {
                    classNotFoundException.printStackTrace();
                }
                if (!noChangeColors.contains(button.getBackground())) {
                    button.setBackground(pressedColor);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.getBounds().contains(e.getPoint()) && !noChangeColors.contains(button.getBackground())) {
                    button.setBackground(hoverColor);
                } else if (!noChangeColors.contains(button.getBackground())) {
                    button.setBackground(originalColor);
                }
            }
        });
    }

    private void clearAllContent() {                                                                                    //This is done at the end of the game or after resetting the game, and clears all data and colours shown in the text field.
        for (int i = 0; i < INumberleModel.MAX_ATTEMPTS; i++) {
            for (int j = 0; j < 7; j++) {
                fields[i][j].setText("");
                fields[i][j].setBackground(new Color(251, 252, 255));
            }
        }
        for (JButton button : buttonMap.values()) {
            button.setBackground(new Color(220, 225, 237));
        }
    }

    private void setButtonColors() {                                             //This is used to set the colour of the button, by getting the map passed from the model
        Map<String, Color> colorDefinitions = new HashMap<>();
        colorDefinitions.put("Green", new Color(47, 193, 165));
        colorDefinitions.put("Orange", new Color(247, 154, 111));
        colorDefinitions.put("Gray", new Color(164, 174, 196));

        Map<String, Set<Character>> map = model.getMap();

        Set<Character> greenSet = map.get("Green");
        Set<Character> orangeSet = map.get("Orange");
        if (greenSet != null && orangeSet != null) {
            orangeSet.removeAll(greenSet);
        }

        for (Map.Entry<String, Set<Character>> entry : model.getMap().entrySet()) {
            String colorName = entry.getKey();
            Set<Character> characters = entry.getValue();

            Color color = colorDefinitions.get(colorName);
            if (color == null) continue;

            for (Character character : characters) {
//                "×", "÷"
                JButton button = buttonMap.get(character.toString().replace("*", "×").replace("/", "÷"));
                if (button != null) {
                    button.setBackground(color);
                }
            }
        }
    }

    public static void showAutoCloseDialog(JFrame frame, String message) {                //Here, to ensure UI uniformity, a custom dialogue box is set up which automatically closes the
        JDialog dialog = new JDialog(frame, true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.setModal(false);

        Font font = new Font("Montserrat Bold", Font.PLAIN, 12);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(255, 255, 255, 100));
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 30, 30);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        dialog.add(panel);

        JLabel contentLabel = new JLabel(message, SwingConstants.CENTER);
        contentLabel.setFont(font);
        panel.add(contentLabel, BorderLayout.CENTER);

        dialog.pack();
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(frame);

        dialog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
            }
        });

        dialog.setShape(new RoundRectangle2D.Double(0, 0, dialog.getWidth() - 10, dialog.getHeight() - 10, 30, 30));

        new Timer(1000, e -> dialog.dispose()).start();

        dialog.setVisible(true);
    }

    public static void showCustomDialog(JFrame frame, String message) {                               //Same as above, but it can be turned off after clicking restart the game
        JDialog dialog = new JDialog(frame, "Message", true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.setModal(true);

        Font font = new Font("Montserrat Bold", Font.PLAIN, 12);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(255, 255, 255, 100));
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 30, 30);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        dialog.add(panel);

        JLabel contentLabel = new JLabel(message, SwingConstants.CENTER);
        contentLabel.setFont(font);
        panel.add(contentLabel, BorderLayout.CENTER);

        JButton okButton = new JButton("Restart The Game");
        okButton.setFocusPainted(false);
        okButton.setForeground(new Color(90, 99, 118));
        okButton.setBorderPainted(false);
        okButton.setBackground(new Color(220, 225, 237));
        mouseClick(okButton);

        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        panel.add(buttonPanel, BorderLayout.PAGE_END);

        dialog.pack();
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(frame);
        dialog.setShape(new RoundRectangle2D.Double(0, 0, dialog.getWidth() - 10, dialog.getHeight() - 10, 30, 30));

        dialog.setVisible(true);
    }

    public void showFlagDialog(JFrame frame) {                                           //This is a dialogue box showing the three FLAG settings. The user sets the initial state of the game by checking the corresponding FLAGs
        JDialog dialog = new JDialog(frame, "Settings", true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.setModal(true);

        Font font = new Font("Montserrat Bold", Font.PLAIN, 13);

        JPanel panel = new JPanel(new GridLayout(0, 1, 50, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        panel.setOpaque(false);

        JPanel checkBoxPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JCheckBox checkBox1 = new JCheckBox("Display Error Message   ");
        checkBox1.setFont(font);
        checkBox1.setSelected(model.getFlag().get(0));
        checkBox1.setFocusPainted(false);
        checkBoxPanel1.add(checkBox1);
        panel.add(checkBoxPanel1);

        JPanel checkBoxPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JCheckBox checkBox2 = new JCheckBox("Display Target Equation");
        checkBox2.setFont(font);
        checkBox2.setSelected(model.getFlag().get(1));
        checkBox2.setFocusPainted(false);
        checkBoxPanel2.add(checkBox2);
        panel.add(checkBoxPanel2);

        JPanel checkBoxPanel3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JCheckBox checkBox3 = new JCheckBox("Set Equation Randomly ");
        checkBox3.setFont(font);
        checkBox3.setSelected(model.getFlag().get(2));
        checkBox3.setFocusPainted(false);
        checkBoxPanel3.add(checkBox3);
        panel.add(checkBoxPanel3);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("Apply");
        okButton.setFocusPainted(false);
        okButton.setForeground(new Color(90, 99, 118));
        okButton.setBorderPainted(false);
        okButton.setBackground(new Color(220, 225, 237));
        okButton.addActionListener(e -> {
            boolean flag1Changed = (checkBox1.isSelected() != model.getFlag().get(0));
            boolean flag2Changed = (checkBox2.isSelected() != model.getFlag().get(1));
            boolean flag3Changed = (checkBox3.isSelected() != model.getFlag().get(2));

            if (flag1Changed || flag2Changed || flag3Changed) {
                model.setFlag(checkBox1.isSelected(), checkBox2.isSelected(), checkBox3.isSelected());
            }

            if (flag1Changed) {
                if (!checkBox1.isSelected()) {
                    showError = false;
//                    System.out.println("a");
                } else if (checkBox1.isSelected()) {
                    showError = true;
//                    System.out.println("b");
                }
            }

            if (flag2Changed) {
                showAnswer = checkBox2.isSelected() && !(currentPosition != 0 || remainingAttempts != 0);
                if (showAnswer) {
                    frame.getContentPane().removeAll();
                    initializeFrame();
                } else {
                    checkBox2.setSelected(false);
                    model.setFlag(checkBox1.isSelected(), checkBox2.isSelected(), checkBox3.isSelected());
                }
            }

            if (flag3Changed) {
                checkBox2.setSelected(false);
                model.setFlag(checkBox1.isSelected(), checkBox2.isSelected(), checkBox3.isSelected());
                restartGame();
            }
            dialog.dispose();
        });
        mouseClick(okButton);
        buttonPanel.add(okButton);
        panel.add(buttonPanel);

        dialog.add(panel);

        dialog.pack();
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(frame);
        dialog.setShape(new RoundRectangle2D.Double(0, 0, dialog.getWidth(), dialog.getHeight(), 30, 30));

        dialog.setVisible(true);
    }

    private void restartGame() {                                                       //This is the method to restart the game
        controller.setDISABLE(true);
        showError = true;
        clearAllContent();
        currentPosition = 0;
        remainingAttempts = 0;
        input.setLength(0);
        showAnswer = false;
        frame.getContentPane().removeAll();
        initializeFrame();
        controller.startNewGame();
    }
}