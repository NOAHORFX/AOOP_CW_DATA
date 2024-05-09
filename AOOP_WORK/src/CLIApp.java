import Controller.*;
import Model.*;

import java.time.temporal.ChronoField;
import java.util.*;


public class CLIApp {
    public static void main(String[] args) {
        System.out.println("===========================================================================");
        INumberleModel model = new NumberleModel();
        NumberleController controller = new NumberleController(model);
        HashMap<String, String> colorMap = new HashMap<>();
        char[] allChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', '*', '/'};                       //Stores all character types
        ArrayList<Character> Green = new ArrayList<>();                                                                 //Stores the colour of the selected button.
        ArrayList<Character> Orange = new ArrayList<>();
        ArrayList<Character> Gray = new ArrayList<>();
        ArrayList<Character> White = new ArrayList<>();
        Set<Character> allUsedChars = new HashSet<>();

        model.setViewStatus(true);                                                                                      //Set it to true to output an error message on the console.

        colorMap.put("0", "Green");
        colorMap.put("1", "Orange");
        colorMap.put("2", "Gray");
        colorMap.put("3", "White");

        try (Scanner scanner = new Scanner(System.in)) {
            controller.startNewGame();
            System.out.println("Welcome to Numberle - CLI Version");
            System.out.println("You have " + controller.getRemainingAttempts() + " attempts to guess the right equation");
            System.out.println("===========================================================================");
            System.out.print("Please initialise the settings: \n1. Display Error Message(Default True). " +
                             "\n2. Display Target Equation(Default False).\n3. Set Equation Randomly(Default True)." +
                             "\nPlease select settings('1','2','3' to choose; 'start' to start game): ");

            while (true) {                                                                                              //Three FLAGs are set here and the user can select the corresponding option to initialise the game settings
                String inputFlag = scanner.nextLine().trim().toLowerCase();
                if (inputFlag.equals("start")) {
                    System.out.println("Initialization complete");
                    System.out.println();
                    break;
                } else if (inputFlag.equals("1") || inputFlag.equals("2") || inputFlag.equals("3")) {
                    int index = Integer.parseInt(inputFlag) - 1;
                    model.toggleFlag(index);
                    System.out.println("Setting " + inputFlag + " is now set to " + model.getFlag().get(index));
                    switch (inputFlag) {
                        case "1":
                            break;
                        case "2":
                            System.out.println("The targeted equation is: " + model.getTargetNumber());
                            break;
                        case "3":
                            model.startNewGame();
                            break;
                    }
                } else {
                    System.out.println("Invalid input. Please enter 1, 2, 3, or 'start'.");
                }
            }


            System.out.println("===========================================================================");

            while (!controller.isGameOver()) {
                System.out.println("===========================================================================");
                System.out.print("\nEnter your guess (7 characters long equation): ");
                String input = scanner.nextLine();

                model.setInputLegal(false);
                controller.processInput(input);

                if (controller.isGameWon()) {                                                   //The setting here is for the case where the game succeeds on the first play, and the corresponding button state is initialised directly
                    Orange.clear();
                    Green.clear();
                    for (int i = 0; i < 7; i++) {
                        char c = input.charAt(i);
                        Green.add(c);
                    }
                    for (char c : allChars) {
                        if (!Green.contains(c)) {
                            White.add(c);
                        }
                    }
                    System.out.println("Green(Confirmed) " + removeDuplicates(Green));
                    System.out.println("Orange(Wrong Position): " + removeDuplicates(Orange));
                    System.out.println("Gray(Not Exist): " + removeDuplicates(Gray));
                    System.out.println("White(Not Used): " + removeDuplicates(White));
                } else if (model.getInputLegal()) {                                                                     //Enter Legal to add the current state of the button to a list showing which are Green, Orange, Gray, and White.
                    for (int i = 0; i < 7; i++) {
                        System.out.print(model.getInputEquation().get(i) + "(" + colorMap.get(model.getColors().get(i)) + ")   ");
                    }
                    System.out.println();

                    Set<Character> gray = model.getMap().get("Gray");
                    Set<Character> orange = model.getMap().get("Orange");
                    Set<Character> green = model.getMap().get("Green");
                    if (gray != null) {
                        Gray.addAll(model.getMap().get("Gray"));
                    }
                    if (orange != null) {
                        Orange.addAll(model.getMap().get("Orange"));
                    }
                    if (green != null) {
                        Green.addAll(model.getMap().get("Green"));
                    }
                    Orange.removeAll(Green);
                    allUsedChars.addAll(Gray);
                    allUsedChars.addAll(Orange);
                    allUsedChars.addAll(Green);
                    for (char c : allChars) {
                        if (!allUsedChars.contains(c)) {
                            White.add(c);
                        }
                    }

                    System.out.println("Green(Confirmed) " + removeDuplicates(Green));
                    System.out.println("Orange(Wrong Position): " + removeDuplicates(Orange));
                    System.out.println("Gray(Not Exist): " + removeDuplicates(Gray));
                    System.out.println("White(Not Used): " + removeDuplicates(White));

                }

                System.out.println();
                if (controller.isGameOver()) {                                                                          //Game over. End the loop.
                    if (controller.isGameWon()) {
                        System.out.println("Congratulations! You've guessed the equation correctly.");
                    } else {
                        System.out.println("Game Over! The correct equation was: " + controller.getTargetWord());
                    }
                } else {
                    System.out.println("Try again. You have " + controller.getRemainingAttempts() + " attempts left.");
                    System.out.println("===========================================================================");
                    System.out.println();
                }
            }

        }
    }
    public static ArrayList<Character> removeDuplicates(ArrayList<Character> list) {
        LinkedHashSet<Character> set = new LinkedHashSet<>(list);
        return new ArrayList<>(set);
    }
}
