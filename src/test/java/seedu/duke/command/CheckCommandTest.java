package seedu.duke.command;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import seedu.duke.data.UserData;
import seedu.duke.exception.DateErrorException;
import seedu.duke.exception.DukeException;
import seedu.duke.exception.MissingSemicolonException;
import seedu.duke.exception.TimeErrorException;
import seedu.duke.exception.WrongNumberOfArgumentsException;
import seedu.duke.storage.Storage;
import seedu.duke.ui.Ui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckCommandTest {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private final UserData data = new UserData();
    private final Ui ui = new Ui();
    private final Storage storage = new Storage("data", ui);

    @BeforeEach
    void setupEventList() throws DukeException {
        // Add Personal events to data
        String personalInput = "personal; Go out for dinner; 05/05/20; 12:00";
        Command addCommand = new AddCommand(personalInput);
        addCommand.execute(data, ui, storage);

        personalInput = "personal; Stay at home; 04/05/20";
        addCommand = new AddCommand(personalInput);
        addCommand.execute(data, ui, storage);

        // Add Zoom event to data
        String zoomInput = "zoom; CS2113T tutorial; zoom.com/blahblah; 03/10/2020; 1330";
        addCommand = new AddCommand(zoomInput);
        addCommand.execute(data, ui, storage);

        // Repeat Zoom event
        String repeatZoomInput = "zoom; 1; weekly; 1";
        Command repeatCommand = RepeatCommand.parse(repeatZoomInput);
        repeatCommand.execute(data, ui, storage);

        //Add Timetable Event to Data
        String timeTableInput = "timetable; Science class; S17; 4/5/2020; 3 pm";
        addCommand = new AddCommand(timeTableInput);
        addCommand.execute(data, ui, storage);
    }

    @Test
    void execute_someEventsInTimeRange_printEventsInTimeRange() throws DukeException {
        // Execute check command
        String inputString = "04/05/20; 13:15; 05/05/20; 14:30";
        System.setOut(new PrintStream(outputStreamCaptor));

        Command checkCommand  = new CheckCommand(inputString);
        checkCommand.execute(data, ui, storage);

        String expectedString = "Here is a list of your coinciding events:" + System.lineSeparator()
                + "1. [P][X] Go out for dinner on 2020-05-05, 12:00" + System.lineSeparator()
                + "2. [P][X] Stay at home on 2020-05-04" + System.lineSeparator()
                + "3. [T][X] Science class, Location: S17 on 2020-05-04, 15:00";
        assertEquals(expectedString, outputStreamCaptor.toString().trim());
    }

    @Test
    void execute_repeatedEventInsideTimeRange_printEventInTimeRange() throws DukeException {
        // Execute check command
        String inputString = "10/10/2020; 12 pm; 10/10/20; 5 pm";
        System.setOut(new PrintStream(outputStreamCaptor));

        Command checkCommand  = new CheckCommand(inputString);
        checkCommand.execute(data, ui, storage);

        String expectedString = "Here is a list of your coinciding events:" + System.lineSeparator()
                + "1. [Z][X] CS2113T tutorial, Link: zoom.com/blahblah on 2020-10-10, 13:30";
        assertEquals(expectedString, outputStreamCaptor.toString().trim());
    }

    @Test
    void execute_eventsOutsideTimeRange_printEventsInTimeRange() throws DukeException {
        // Execute check command
        String inputString = "20/10/20; 13:00; ; ";
        System.setOut(new PrintStream(outputStreamCaptor));

        Command checkCommand  = new CheckCommand(inputString);
        checkCommand.execute(data, ui, storage);

        String expectedString = "You have no coinciding events!";
        assertEquals(expectedString, outputStreamCaptor.toString().trim());
    }

    @Test
    void execute_semicolonsNotUsedToSeparateFields_missingSemicolonExceptionThrown() {
        // Execute check command
        String inputString = "9/10/2020 3 pm 10/10/20 5 pm";
        System.setOut(new PrintStream(outputStreamCaptor));

        assertThrows(MissingSemicolonException.class, () -> {
            Command checkCommand  = new CheckCommand(inputString);
            checkCommand.execute(data, ui, storage);
        });
    }

    @Test
    void execute_notEnoughFieldsFilled_WrongNumberOfArgumentsExceptionThrown() {
        // Execute check command
        String inputString = "9/10/2020; 3 pm";
        System.setOut(new PrintStream(outputStreamCaptor));

        assertThrows(WrongNumberOfArgumentsException.class, () -> {
            Command checkCommand  = new CheckCommand(inputString);
            checkCommand.execute(data, ui, storage);
        });
    }

    @Test
    void execute_invalidDateFormatGiven_DateErrorExceptionThrown() {
        // Execute check command
        String inputStringOne = "9.10.2020; 3 pm; 10.10.2020; 5 pm";
        System.setOut(new PrintStream(outputStreamCaptor));

        assertThrows(DateErrorException.class, () -> {
            Command checkCommand  = new CheckCommand(inputStringOne);
            checkCommand.execute(data, ui, storage);
        });

        String inputStringTwo = "9,10,2020; 3 pm; 10,10,2020; 5 pm";
        System.setOut(new PrintStream(outputStreamCaptor));

        assertThrows(DateErrorException.class, () -> {
            Command checkCommand  = new CheckCommand(inputStringTwo);
            checkCommand.execute(data, ui, storage);
        });
    }

    @Test
    void execute_invalidTimeFormatGiven_DateErrorExceptionThrown() {
        // Execute check command
        String inputStringOne = "9/10/2020; 3.00 pm; 10/10/2020; 5.00 pm";
        System.setOut(new PrintStream(outputStreamCaptor));

        assertThrows(TimeErrorException.class, () -> {
            Command checkCommand  = new CheckCommand(inputStringOne);
            checkCommand.execute(data, ui, storage);
        });
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }
}