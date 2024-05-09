package Model;

import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class NumberleModelTest {
    private NumberleModel model;

    @org.junit.Before
    public void setUp() throws Exception {
        this.model = new NumberleModel();
        model.initialize();
    }

    @org.junit.After
    public void tearDown() throws Exception {
        this.model = null;
    }


    /*@ This test scenario focuses on verifying how the game performs under various conditions of successful completion.
      @ The state of the game after initialisation was checked to ensure that the target number and the number of attempts remaining were set correctly.
      @ The tests also verified that the game correctly determines victory when the player enters the correct answer, and ensured that the game state (e.g., the colour array and the end-of-game flag) is correctly updated after victory.
      @ The consistency of the game logic and the effectiveness of the observer notification mechanism were confirmed through multiple rounds of game restarts and observer messages.
      @ requires model != null;
      @ requires model.getTargetNumber() != null;
      @ requires model.getRemainingAttempts() == NumberleModel.MAX_ATTEMPTS;
      @ requires !model.isGameWon();
      @ ensures model.isGameWon();
      @ ensures model.isGameOver();
      @ ensures model.getColors().stream().allMatch(color -> color.equals("0"));
      @ ensures model.getRemainingAttempts() == NumberleModel.MAX_ATTEMPTS;
      @*/
    @org.junit.Test
    public void testGameSuccess() {

        /**
         * @pre: game not initialised.
         * @post: target number set, number of attempts maxed out, game not finished.*/
        /*Checking initialisation conditions*/
        model.startNewGame();
        assertNotNull("Target number should not be null after initialization", model.getTargetNumber());
        assertEquals("Initial remaining attempts should be MAX_ATTEMPTS", NumberleModel.MAX_ATTEMPTS, model.getRemainingAttempts());
        assertFalse("Game should not be won at initialization", model.isGameWon());

        /**
         * @pre: The game is initialised.
         * @post: After correct input, the game is marked as a win and the game is over.*/
        /*Checking for correct input*/
        String correctInput1 = model.getTargetNumber();
        model.processInput(correctInput1);
        assertTrue("Input correct, Game won!", model.isGameWon());
        assertTrue("Game should be over after winning", model.isGameOver());

        /**
         * @pre: Game has been marked as won.
         * @post: All entries in the color array are "0".*/
        /*Check the colour array*/
        ArrayList<String> colors = model.getColors();
        assertTrue("All colors should be '0' indicating correct positions", colors.stream().allMatch(color -> color.equals("0")));

        /**
         * @pre: Check immediately after winning.
         * @post: Remaining attempts unchanged after winning.*/
        /*Check remaining attempts*/
        assertEquals("Remaining attempts should not change after game won", NumberleModel.MAX_ATTEMPTS, model.getRemainingAttempts());

        /**
         * @pre: At least one input has been made.
         * @post: Input legal flag is true.*/
        /*Verify the legality of input formulas*/
        assertTrue("Input legal flag should be true", model.getInputLegal());

        /**
         * @pre: Game can be reset.
         * @post: Each game starts with a non-null target number, maximum attempts, and not won.*/
        /*Multiple rounds of playtesting*/
        for (int i = 0; i < 5; i++) {
            model.startNewGame();
            assertNotNull("Target number should not be null after game start", model.getTargetNumber());
            assertEquals("Remaining attempts should be MAX_ATTEMPTS at start of game", NumberleModel.MAX_ATTEMPTS, model.getRemainingAttempts());
            assertFalse("Game should not be won at start", model.isGameWon());

            String correctInput2 = model.getTargetNumber();
            model.processInput(correctInput2);
            assertTrue("Game should be won after correct input", model.isGameWon());
            model.startNewGame();
        }

        /**
         * @pre: Observer added and new game started.
         * @post: "Game Won" notification received by the observer upon winning.*/
        /*Observer message testing*/
        NumberleModelObserverTest numberleModelObserverTest = new NumberleModelObserverTest();
        model.addObserver(numberleModelObserverTest);

        model.startNewGame();
        String correctInput3 = model.getTargetNumber();
        model.processInput(correctInput3);
        assertTrue("Observer should have received 'Game Won' notification", numberleModelObserverTest.getNotifications().contains("Game Won"));
    }



    /*@ This test scenario focuses on examining the game over logic.
      @ Simulating Wrong Inputs: The test simulates a player making consecutive wrong inputs until the number of attempts runs out, verifying that the game ends correctly.
      @ Verify that the game correctly determines victory after the player enters the correct answer in the last attempt.
      @ Checks that the number of attempts and the game over flag are reset after restarting the game.
      @ Test that the correct notification message is sent at the end of the game through the observer pattern.
      @ requires model != null;
      @ requires model.startNewGame() == true;
      @ requires model.getRemainingAttempts() == NumberleModel.MAX_ATTEMPTS;
      @ ensures model.getRemainingAttempts() == 0;
      @ ensures model.isGameOver();
      @*/
    @org.junit.Test
    public void testGameOver() {

        /**
         * @pre: A new game has started.
         * @post: After consecutive incorrect inputs, remaining attempts drop to zero, and the game is marked as over.*/
        /*Wrong six times*/
        model.startNewGame();
        for (int i = 0; i < NumberleModel.MAX_ATTEMPTS; i++) {
            model.processInput("1+1+1=3");
        }
        assertEquals("Remaining attempts should be zero", 0, model.getRemainingAttempts());
        assertTrue("No attempt left, Game over!", model.isGameOver());

        /**
         * @pre: The game has started with almost all attempts used incorrectly.
         * @post: Correct input on the last attempt leads to a win and ends the game.*/
        /*Wrong the first five times, right the sixth time*/
        model.startNewGame();
        for (int i = 0; i < NumberleModel.MAX_ATTEMPTS - 1; i++) {
            model.processInput("wrong input");
        }
        model.processInput(model.getTargetNumber());
        assertTrue("Game should be won on the last attempt", model.isGameWon());
        assertTrue("Game should be over after winning on the last attempt", model.isGameOver());

        /**
         * @pre: The game has ended.
         * @post: Upon starting a new game, it is not in a 'game over' state, and attempts are reset to maximum.*/
        /*Restart new game*/
        model.startNewGame();
        assertFalse("New game should not be in 'game over' state", model.isGameOver());
        assertEquals("New game should have full attempts", NumberleModel.MAX_ATTEMPTS, model.getRemainingAttempts());

        /**
         * @pre: A new game has started without any inputs processed.
         * @post: Character mapping should be empty if no inputs have been made.*/
        /*Checking Character Mapping*/
        assertTrue("All characters should be in 'Green' set", model.getMap().isEmpty());

        /**
         * @pre: An observer has been added, and a new game has started.
         * @post: After exhausting all attempts without a win, a 'Game Over' notification should be received.*/
        /*Observer Message Testing*/
        NumberleModelObserverTest numberleModelObserverTest = new NumberleModelObserverTest();
        model.addObserver(numberleModelObserverTest);
        model.startNewGame();
        for (int i = 0; i < NumberleModel.MAX_ATTEMPTS; i++) {
            model.processInput("1+1+1=3");
        }
        assertTrue("Game should be over", model.isGameOver());
        assertTrue("Observer should have received 'Game Over' notification", numberleModelObserverTest.getNotifications().contains("Game Over"));
    }



    /*@ This test scenario, testProcessInputValid, examines how the game handles various input validations.
      @ testProcessInputValid tests whether inputs that do not meet the length requirement are correctly invalidated.
      @ Checks that the game handles consecutive symbols in the input (e.g. "++" or "--"), which are expected to be invalidated.
      @ Verify the handling of input for mathematically unsound equations (unequal sides), which should be marked as invalid.
      @ Testing the response to input that contains only the equals sign and no operator should be considered invalid.
      @ Check the processing of inputs that do not contain the equals sign, which is expected to be invalid.
      @ Verify that the game accepts valid input that begins with a plus or minus sign.
      @After the game restarts, verify that the correct mathematical expression is accepted and check that the colour array is correctly populated.
      @ requires model != null;
      @ requires model.startNewGame() == true;
      @ ensures (!model.getInputLegal()) implies (model.getColors().size() == 0);
      @ ensures (model.getInputLegal()) implies (model.getColors().size() > 0);
      @*/
    @org.junit.Test
    public void testProcessInputValid() {
        model.startNewGame();

        /**
         * @pre: A new game has started.
         * @post: Short inputs should be marked as invalid, and the colors list should remain empty.*/
        /*Input too short*/
        String invalidInput = "1+2";
        model.processInput(invalidInput);
        assertFalse("Input should be illegal for short invalid input", model.getInputLegal());
        assertEquals("Colors list should be empty after invalid input", 0, model.getColors().size());

        /**
         * @pre: Game has been restarted or continues after an invalid input.
         * @post: Inputs with overlapping symbols like "++" or "--" should be marked as invalid.*/
        /*Input formula symbols overlap*/
        String inputWithDoublePlus = "1++9=10";
        model.processInput(inputWithDoublePlus);
        assertFalse("Input with consecutive pluses should be illegal", model.getInputLegal());

        String inputWithDoubleMinus = "10--2=8";
        model.processInput(inputWithDoubleMinus);
        assertFalse("Input with consecutive minuses should be illegal", model.getInputLegal());

        /**
         * @pre: The game is ongoing with inputs being processed.
         * @post: Inputs where the equation sides do not mathematically balance should be marked as invalid.*/
        /*Enter that the two sides of the equation are not equal*/
        String unequalInput = "1+2+3=4";
        model.processInput(unequalInput);
        assertFalse("Input where sides are not equal should be illegal", model.getInputLegal());

        /**
         * @pre: Game is active with various inputs tested.
         * @post: Inputs that only contain an equals sign without arithmetic operations should be marked as invalid*/
        /*Missing addition, subtraction, multiplication and division symbols*/
        String singleEqualsInput = "111=111";
        model.processInput(singleEqualsInput);
        assertFalse("Input with a single equals should be legal if correctly balanced", model.getInputLegal());

        /**
         * @pre: A new round of input testing has begun.
         * @post: Inputs lacking an equals sign should be identified as invalid.*/
        /*Missing equals sign*/
        String noEqualsInput = "1111111";
        model.processInput(noEqualsInput);
        assertFalse("Input without an equals sign should be illegal", model.getInputLegal());

        /**
         * @pre: Game continues, checking for various input scenarios.
         * @post: Inputs that start with an operator like '+' should be marked as valid if they are otherwise correct.*/
        /*The first part of the equation contains a plus or minus sign*/
        String startsWithOperator = "+1+9=10";
        model.processInput(startsWithOperator);
        assertTrue("Input starting with a plus should be legal", model.getInputLegal());

        /**
         * @pre: The game has been reset for a new round.
         * @post: Correctly balanced and structured inputs should be marked as valid, and the colors list should be populated accordingly.*/
        /*Correct input*/
        model.startNewGame();
        String validInput = "1+2+3=6";
        model.processInput(validInput);
        assertTrue("Input should be legal after valid input", model.getInputLegal());
        assertTrue("Colors array should be filled with colors", model.getColors().size() > 0);
    }
}
