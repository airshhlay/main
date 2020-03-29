package cardibuddy.ui;

import cardibuddy.model.flashcard.Answer;
import cardibuddy.model.flashcard.Flashcard;
import cardibuddy.model.flashcard.Question;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * A class for the Test Session's card view.
 * Displays the question for the user to answer.
 * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
 * As a consequence, UI elements' variable names cannot be set to such keywords
 * or an exception will be thrown by JavaFX during runtime.
 *
 * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
 */
public class TestCard extends UiPart<Region> {

    private static final String FXML = "TestCard.fxml";
    
    public final Question question;
    public final Answer answer;
    
    @FXML
    private HBox cardPane;
    @FXML
    private Label content;
    
    public TestCard(Question question) {
        super(FXML);
        this.question = question;
        content.setText(question.toString());
    }

    public TestCard(Answer answer) {
        super(FXML);
        this.question
    }
    
    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TestCard)) {
            return false;
        }

        // state check
        TestCard card = (TestCard) other;
        return question.equals(card.question)
                && content.getText().equals(card.content.getText());
    }

}
