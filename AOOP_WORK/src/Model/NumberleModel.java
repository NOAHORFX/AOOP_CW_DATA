package Model;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;

public class NumberleModel extends Observable implements INumberleModel {
    private String targetNumber;
    private StringBuilder currentGuess;
    private int remainingAttempts;
    private boolean gameWon;
    private boolean inputLegal = false;
    private final ArrayList<String> colors = new ArrayList<>();    //Saves the colour data corresponding to each symbol in each segment of the entered equation.
    private final ArrayList<Character> inputEquation = new ArrayList<Character>();
    private final Map<String, Set<Character>> map = new HashMap<>();  //Save the name of the button and the state of the button
    private final ArrayList<Boolean> Flag = new ArrayList<>();  // I initialised three flags here to use as settings at the start of the game
    {
        Flag.add(true);
        Flag.add(false);
        Flag.add(true);
    }
    private boolean viewStatus = false;

    @Override
    public void setViewStatus(boolean status) {
        this.viewStatus = true;
    }

    /*@ ensures \result == Flag;
      @*/
    @Override
    public ArrayList<Boolean> getFlag() {  //This method ensures that the GUI moderator does not display an error message at runtime
        return Flag;
    }

    /*@ invariant invariant();
      @ invariant Flag != null && Flag.size() == 3;
      @ requires Flag1 != null && Flag2 != null && Flag3 != null;
      @ ensures Flag.get(0) == Flag1 && Flag.get(1) == Flag2 && Flag.get(2) == Flag3;
      @*/
    @Override
    public void setFlag(Boolean Flag1, Boolean Flag2, Boolean Flag3) {    //The View sets the FLAG via the set method.

        /**@pre The boolean parameters Flag1, Flag2, and Flag3 must not be null.
         * @pre The Flag list must be initialized and must have exactly three elements.
         * @post The Flag list will have its first, second, and third elements set to Flag1, Flag2, and Flag3 respectively.
         */

        assert invariant();
        assert Flag1 != null && Flag2 != null && Flag3 != null : "Flags cannot be null";
        Flag.set(0, Flag1);
        Flag.set(1, Flag2);
        Flag.set(2, Flag3);
        assert Flag.get(0) == Flag1 && Flag.get(1) == Flag2 && Flag.get(2) == Flag3 : "Flag list cannot be null";
    }

    /*@
      @ invariant invariant();
      @ requires index >= 0 && index < Flag.size();
      @ assignable Flag;
      @ ensures Flag.get(index) != oldValue;
      @*/
    @Override
    public void toggleFlag(int index) {  //Reverse the status of the FLAG
        assert invariant();
        assert index >= 0 && index < Flag.size() : "Index out of bounds";
        boolean oldValue = Flag.get(index);

        Flag.set(index, !Flag.get(index));

        assert Flag.get(index) != oldValue : "Flag value should have been toggled";
    }

    /*@ invariant invariant();
      @ requires Flag != null;
      @ ensures result != null && !result.isEmpty();
      @*/
    private String generateTargetEquation(Boolean Flag) {    //Determine if the equation is randomly generated based on FLAG
        /**@pre Flag must not be null.
         * @post Return result required non-null and non-empty string.
         */
        assert invariant();
        assert Flag != null : "Flag cannot be null";

        List<String> equations = new ArrayList<>();
        String fileName = "equations.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                equations.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result;
        if (!equations.isEmpty() && Flag) {
            Random rand = new Random();
            result = equations.get(rand.nextInt(equations.size()));
        } else {
            result = "1+1+1=3";
        }
        assert result != null && !result.isEmpty() : "Resulting equation must not be null or empty";
        return result;
    }

    /*@ invariant invariant();
      @ requires Flag.get(2)
      @ assignable targetNumber, currentGuess, remainingAttempts, gameWon;
      @ ensures remainingAttempts == MAX_ATTEMPTS;
      @ ensures !gameWon;
      @ ensures targetNumber != null && !targetNumber.isEmpty();
      @*/
    @Override
    public void initialize() {
        assert invariant();
        assert Flag.get(2) != null : "Initialization flag must not be null";
        Random rand = new Random();
        targetNumber = Integer.toString(rand.nextInt(10000000));
        currentGuess = new StringBuilder("");
        remainingAttempts = MAX_ATTEMPTS;
        gameWon = false;
        setChanged();
        notifyObservers();
        targetNumber = generateTargetEquation(Flag.get(2));
        assert remainingAttempts == MAX_ATTEMPTS : "RemainingAttempts need set with MAX_ATTEMPTS";
        assert !gameWon : "Game success flag need to be set with false";
        assert targetNumber != null && !targetNumber.isEmpty() : "Target number must not be null or empty";
    }


    /*@ invariant invariant();
      @ requires input != null;
      @ requires input.length() == 7;
      @ assignable remainingAttempts, gameWon, colors, map, inputLegal, inputEquation;
      @ ensures input.equals(targetNumber) == gameWon;
      @ ensures gameWon == colors.stream().allMatch("0"::equals);
      @ ensures !isGameOver() || map.isEmpty();
    @*/
    @Override
    public void processInput(String input) {
        /**@pre The input string must not be null.
         * @pre The input string must be exactly 7 characters long.
         * @post If the input string matches the target number, the "gameWon" state will be true.
         * @post If the game won, all values in the colors list will be "0".
         * @post If the game over, the map must be empty.
         */
        assert invariant();
        assert input != null : "Input cannot be null";
        colors.clear();                                      //On each input, clear the list of colours generated by the previous input
        if (input.length() != 7) {
            setChanged();
            notifyObservers("Invalid Input");
            if (getFlag().get(0) && viewStatus) {
                System.out.println("Invalid Input");
            }
            return;
        }

        if (!evaluateExpression(input)) {                    //This expression evaluates to include most cases where the equation is not legal
            return;
        } else {
            for (int i = 0; i < input.length(); i++) {
                inputEquation.add(input.charAt(i));
            }
            inputLegal = true;
        }

        remainingAttempts--;

        if (input.equals(targetNumber)) {                    //If equal, Game win, and add set all color to green
            gameWon = true;
            for(int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                colors.add("0");
                map.computeIfAbsent("Green", k -> new HashSet<>()).add(c);
            }
            System.out.println();
        } else {                                             //If not equal, set the corresponding state to the corresponding colour
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (i < targetNumber.length() && c == targetNumber.charAt(i)) {
                    colors.add("0");
                    map.computeIfAbsent("Green", k -> new HashSet<>()).add(c);
                } else if (targetNumber.contains(String.valueOf(c))) {
                    colors.add("1");
                    map.computeIfAbsent("Orange", k -> new HashSet<>()).add(c);
                } else {
                    colors.add("2");
                    map.computeIfAbsent("Gray", k -> new HashSet<>()).add(c);
                }
            }
        }

        assert input.equals(targetNumber) == gameWon : "Need to match the target number to win the game";
        assert gameWon == colors.stream().allMatch("0"::equals) : "Not all elements are zero strings";

        System.out.println();
        if (isGameOver()) {
            setChanged();
            notifyObservers(gameWon ? "Game Won" : "Game Over");

            map.clear();
            if (gameWon) {
                remainingAttempts = INumberleModel.MAX_ATTEMPTS;
            }
        } else {
            setChanged();
            notifyObservers("Try Again");
        }
        assert !isGameOver() || map.isEmpty();
    }

    @Override
    public boolean isGameOver() {
        return remainingAttempts <= 0 || gameWon;
    }

    /*@ invariant invariant();
      @ ensures \result == gameWon;
      @*/
    @Override
    public boolean isGameWon() {
        assert invariant();
        return gameWon;
    }

    /*@ invariant invariant();
      @ ensures targetNumber != null;
      @*/
    @Override
    public String getTargetNumber() {
        assert invariant();
        assert targetNumber != null : "Target number should not be null";
        return targetNumber;
    }

    /*@
      @ invariant invariant();
      @ ensures currentGuess != null;
      @*/
    @Override
    public StringBuilder getCurrentGuess() {
        assert invariant();
        assert currentGuess != null : "Current guess should not be null";
        return currentGuess;
    }

    @Override
    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    @Override
    public void startNewGame() {
        initialize();
    }

    @Override
    public ArrayList<String> getColors(){
        return colors;
    }

    @Override
    public Map<String, Set<Character>> getMap() {
        return map;
    }

    @Override
    public ArrayList<Character> getInputEquation() {
        return inputEquation;
    }

    @Override
    public Boolean getInputLegal() {
        return inputLegal;
    }

    /*@
      @ invariant invariant();
      @ ensures inputLegal != null;
      @*/
    @Override
    public void setInputLegal(Boolean inputLegal) {
        assert invariant();
        assert inputLegal != null : "Input legal flag should not be null";
        this.inputLegal = inputLegal;
    }

    /*@
      @ invariant invariant();
      @ requires expression != null && !expression.isEmpty();
      @ ensures expression.indexOf('=') == -1;
      @ ensures isInputValid(expression);
      @ ensures isExpressionValid(expression);
    @*/
    @Override
    public boolean evaluateExpression(String expression) {
        /**@pre The expression must not be null or empty.
         * @post The expression must contain exactly one equals sign, indicating a legal equation.
         * @post The input of the expression must be valid, follow the specific format and content rules.
         * @post The structure of the expression must be valid, containing required arithmetic operators and structure.
         */
        assert invariant();
        assert expression != null && !expression.isEmpty() : "Expression cannot be null or empty";

        String[] parts = expression.split("=");      //If there are two parts, it means that the equal sign exists
        if (parts.length != 2) {
            setChanged();
            notifyObservers("No Equal");
            if (getFlag().get(0) && viewStatus) {          //Here it will be determined whether or not to display an error message based on the flag, and indeed whether or not it is the GUI version that is running
                System.out.println("No equal '=' sign.");
            }
            return false;
        }

        assert expression.indexOf('=') != -1 : "Expression must contain exactly one '=' sign";

        if (!isInputValid(expression)) {                   //Here I used a regular expression to initially determine if the first part of the equation is a + or -, and if there are any repeating characters in the equation.
            setChanged();
            notifyObservers("Invalid Input");
            if (getFlag().get(0) && viewStatus) {          //Same as above
                System.out.println("Must contain numbers and symbols cannot be consecutive.");
            }
            return false;
        }
        assert isInputValid(expression) : "Input must be valid with correct numerical and symbolic content";

        if (!isExpressionValid(expression)) {              //Here at least one addition, subtraction, multiplication and division symbol is detected in the equation
            setChanged();
            notifyObservers("Missing Symbols");
            if (getFlag().get(0) && viewStatus) {
                System.out.println("There must be at least one '+-*/'.");
            }
            return false;
        }
        assert isExpressionValid(expression) : "Expression must contain at least one arithmetic operator (+, -, *, /)";

        if (Math.abs(evaluateSide(parts[0]) - evaluateSide(parts[1])) < 0.0001) {    //Here the equality of the two sides of the equal sign is tested
            return true;
        } else {
            setChanged();
            notifyObservers("Not Equal");
            if (getFlag().get(0) && viewStatus) {
                System.out.println("The left side is not equal to the right.");
            }
            return false;
        }
    }


    private static boolean isInputValid(String expression) {
        String regex = "^[-+]?\\d+(?:[-+*/]\\d+)*(?:=([-+]?\\d+(?:[-+*/]\\d+)*))?$";
        return Pattern.matches(regex, expression) && !expression.contains("++") && !expression.contains("--") && !expression.contains("**") && !expression.contains("//");
    }


    private static boolean isExpressionValid(String expression) {
        String regex = "^[0-9=]*[+\\-*/][0-9=+\\-*/]*$";
        return Pattern.matches(regex, expression);
    }

    private static double evaluateSide(String side) {
        int start = 0;
        if (side.startsWith("-") || side.startsWith("+")) {
            start = 1;
        }

        List<Double> numbers = new ArrayList<>();                                     //Stores the number parsed from the string
        List<Character> operators = new ArrayList<>();                                //Storing operators parsed from strings
        StringBuilder tempNum = new StringBuilder(side.substring(0, start));

        for (int i = start; i < side.length(); i++) {
            char ch = side.charAt(i);
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/') {                   //When an operator is encountered, the parsed number is added to the list
                numbers.add(Double.parseDouble(tempNum.toString()));
                tempNum = new StringBuilder();
                operators.add(ch);
            } else {
                tempNum.append(ch);
            }
        }
        numbers.add(Double.parseDouble(tempNum.toString()));

        for (int i = 0; i < operators.size(); i++) {                                  //Multiply and divide first
            if (operators.get(i) == '*' || operators.get(i) == '/') {
                double result = operators.get(i) == '*' ? numbers.get(i) * numbers.get(i + 1) : numbers.get(i) / numbers.get(i + 1);
                numbers.set(i, result);
                numbers.remove(i + 1);
                operators.remove(i);
                i--;
            }
        }

        double result = numbers.get(0);                                               //Then add and subtract
        for (int i = 0; i < operators.size(); i++) {
            double number = numbers.get(i + 1);
            if (operators.get(i) == '+') {
                result += number;
            } else if (operators.get(i) == '-') {
                result -= number;
            }
        }

        return result;
    }

    public boolean invariant() {
        return remainingAttempts >= 0 && remainingAttempts <= MAX_ATTEMPTS;
    }


}

