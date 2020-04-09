package cardibuddy.model.testsession;

import java.util.HashMap;
import java.util.LinkedList;

import cardibuddy.model.deck.Deck;
import cardibuddy.model.flashcard.Answer;
import cardibuddy.model.flashcard.Card;
import cardibuddy.model.flashcard.Question;
import cardibuddy.model.testsession.exceptions.AlreadyCorrectException;
import cardibuddy.model.testsession.exceptions.EmptyDeckException;
import cardibuddy.model.testsession.exceptions.UnansweredQuestionException;

/**
 * Test Session class.
 * The test session works as follows:
 * 1. User initiates a test session by the command "test [DECK INDEX]"<br>
 * 2. TestParser parsers this command, and
 * TestSession object testSession is created with the given deck<br>
 * (in future implementations, user can also set test settings,
 * which can mean this class will be a parent class for different kinds of tests)<br>
 * 3. testSession calls nextFlashcard(), which returns the next flashcard in the queue.<br>
 * 4. User inputs their answer to the question, and the parser parses it into an Answer object.
 * testSession calls test() and creates a new TestResult object that stores the user's Answer
 * and the calculated Result by comparing the user's answer with the Flashcard's.<br>
 * 5. testSession returns a {@code TestResult} object.<br>
 * 6. User presses next.
 * (Note in future implementations, user can use force correct
 * to first mark the flashcard as correct, before going to the next question)<br>
 * 7. testSession calls nextFlashcard(), and displays the next flashcard to the user.
 */
public class TestSession {

    private Deck deck;
    private Card current;
    private boolean isOngoing;
    private boolean hasAnswered;
    // public Statistic statistics; // for recording statistics

    // space to set deck settings


    // hashmap to store the user's answer history
    private HashMap<Card, TestResult> testResults;

    // test queue
    private LinkedList<Card> testQueue;

    /**
     * Constructor for test session. Initiates the test session.
     *
     * @param deck the deck that is being tested
     */
    public TestSession(Deck deck) throws EmptyDeckException {
        // initialise variables
        this.deck = deck;
        testQueue = new LinkedList<>(deck.getFlashcardList());
        testResults = new HashMap<>();
        if (deck.getFlashcardList().isEmpty()) {
            throw new EmptyDeckException();
        }
    }

    /**
     * Called when the test session is first started.
     *
     * @return the first question in the {@code testQueue}
     */
    public Question getFirstQuestion() {
        current = testQueue.removeFirst();
        hasAnswered = false;
        return current.getQuestion();
    }

    /**
     * Moves on to the next flashcard in the queue, called when the user inputs the command for 'next'.<br>
     * Sets the {@code current} flashcard to this next flashcard.<br>
     * Checks to see if the card should be added to the back of the queue again. (Prioritising)
     *
     * @return the {@code Question} for the next flashcard.
     */
    public Question getNextQuestion() throws UnansweredQuestionException {
        if (!hasAnswered) { // cannot go to the next question without answering the question
            throw new UnansweredQuestionException();
        }
        current = testQueue.removeFirst();
        hasAnswered = false; // reset the boolean as the user has not answered this new question

        return current.getQuestion();
    }

    /**
     * Skips this question by removing the question from the testQueue without marking it.
     * Updates the TestResult by incrementing the variable {@code timesSkipped}
     */
    public Question skipQuestion() throws AlreadyCorrectException {
        if (testResults.containsKey(current)) {
            // if the user has already answered this question before, update the result in the testResults hashmap
            TestResult prevTestResult = testResults.get(current);
            if (prevTestResult.getResult() == Result.CORRECT) {
                // user should not be allowed to skip the question if they already got it correct
                throw new AlreadyCorrectException();
            }
            TestResult newTestResult = new TestResult(prevTestResult);
            // call this other TestResult constructor to remember the number of attempts
            testResults.put(current, newTestResult);
        } else {
            testResults.put(current, new TestResult(Result.SKIPPED));
        }

        if (hasAnswered) {
            testQueue.removeLast(); // remove the prioritised flashcard
        }

        current = testQueue.removeFirst();
        hasAnswered = false;

        return current.getQuestion();
    }

    /**
     * Shows the answer for the {@code current} flashcard.
     */
    public Answer getAnswer() {
        assert !testQueue.isEmpty() && current != null;
        return current.getAnswer();
    }


    /**
     * Gets the number of flashcards left in the {@code testQueue}.
     * This method is used for the countdown.
     */
    public int getTestQueueSize() {
        return testQueue.size();
    }

    /**
     * Performs the force correct option.
     * Allows the user to manually mark their answer as correct, if it was initially marked wrong by {@code TestResult}.
     * Force correct should be performed before the user gets the next question.
     */
    public void forceCorrect() throws UnansweredQuestionException, AlreadyCorrectException {
        if (!hasAnswered) { // cannot force correct a question you have not even answered
            throw new UnansweredQuestionException();
        }
        testQueue.removeLast(); // reverse the prioritisation
        testResults.get(current).forceCorrect();
    }

    /**
     * Takes the user's answer for the current flashcard, and tests it against the current flashcard.
     * If the user got the answer wrong, the current flashcard is automatically added to the back of the test queue.
     *
     * @param userAnswer the String representation of the user's answer
     * @return the TestResult of the test
     */
    public TestResult submitAnswer(String userAnswer) {
        // compare the userAnswer, and get the result of the test
        TestResult testResult = new TestResult(current.getAnswer(), userAnswer);
        // if flashcard was already tested before, update the number of tries.
        // There no need to update the number of tries if this is the first time testing the flashcard.
        // By default, numTries is set to 1.
        if (testResults.containsKey(current)) {
            TestResult prevResult = testResults.get(current);
            int numTries = prevResult.getNumTries();
            testResult.setNumTries(numTries + 1); // increase numTries by 1
        }
        testResults.put(current, testResult); // store the result

        // add the current flashcard back into the test queue
        if (testResult.getResult() == Result.WRONG) {
            testQueue.addLast(current);
        }

        hasAnswered = true; // note down that the user has answered the question
        return testResult;
    }

    /**
     * Returns the session's results.
     * @return a Hashmap of TestResults corresponding to each question
     */
    public HashMap<Card, TestResult> getTestResults() {
        return testResults;
    }

    public Deck getDeck() {
        return deck;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof TestSession)
                && this.deck.equals(((TestSession) other).deck)
                && this.testResults.equals(((TestSession) other).testResults);
    }
}
