package Model;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public interface INumberleModel {
    int MAX_ATTEMPTS = 6;
    void initialize();
    void processInput(String input);
    boolean isGameOver();
    boolean isGameWon();
    String getTargetNumber();
    StringBuilder getCurrentGuess();
    int getRemainingAttempts();
    void startNewGame();
    ArrayList<String> getColors();
    Map<String, Set<Character>> getMap();
    ArrayList<Character> getInputEquation();
    Boolean getInputLegal();
    void setInputLegal(Boolean inputLegal);
    void toggleFlag(int index);
    boolean evaluateExpression(String expression);
    ArrayList<Boolean> getFlag();
    void setFlag(Boolean Flag1, Boolean Flag2, Boolean Flag3);
    void setViewStatus(boolean status);
}