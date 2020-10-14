package seedu.duke.command;

import seedu.duke.command.repeatExceptions.InvalidEventListTypeException;
import seedu.duke.command.repeatExceptions.InvalidTypeException;
import seedu.duke.command.repeatExceptions.MissingDeadlineRepeatException;
import seedu.duke.command.repeatExceptions.WrongNumberOfArgumentsException;
import seedu.duke.data.UserData;
import seedu.duke.event.Event;
import seedu.duke.event.EventList;
import seedu.duke.event.Repeat;
import seedu.duke.storage.Storage;
import seedu.duke.ui.Ui;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command to repeat task.
 */
public class RepeatCommand extends Command {
    private static final String COMMANDTYPE_LIST = "list";
    private static final String COMMANDTYPE_ADD = "add";
    private static final String COMMANDTYPE_ERROR = "error";
    private String commandType;

    /**
     * Constructor for the repeat command.
     *
     * @param command user input with the format eventIndex; eventType; timeInterval; NumberofIterations
     */
    public RepeatCommand(String command, String commandType) {
        this.isExit = false;
        this.command = command;
        this.commandType = commandType;
    }

    @Override
    public void execute(UserData data, Ui ui, Storage storage) {
        try {
            switch (commandType) {
            case COMMANDTYPE_ADD:
                executeAdd(data, ui, storage);
                break;
            case COMMANDTYPE_LIST:
                executeList(data, ui);
                break;
            case COMMANDTYPE_ERROR:
                executeNull(data, ui, storage);
            default:
                //do nothing
            }
        } catch (IndexOutOfBoundsException e) {
            this.command = "Error! Index out of bounds!";
            this.commandType = COMMANDTYPE_ERROR;
            executeNull(data, ui, storage);
        } catch (Exception e) {
            this.command = e.getMessage();
            this.commandType = COMMANDTYPE_ERROR;
            executeNull(data, ui, storage);
        }

    }

    /**
     * Static parser for repeat command creation. Distinguish between adding repeated dates or listing current repeats.
     *
     * @param input String containing user inputs
     * @return RepeatCommand set to either add additional dates or set to list out current dates in event
     */
    public static Command parse(String input) {
        String[] words = input.split(" ");
        try {

            switch (words.length) {
            case 2:
                words[0] = formatListName(words[0]);
                isValidNumber(words[1]);
                input = String.join(" ", words);
                return new RepeatCommand(input, COMMANDTYPE_LIST);
            case 4:
                words[0] = formatListName(words[0]);
                isValidNumber(words[1]);
                words[2] = words[2].toUpperCase();
                isValidNumber(words[3]);
                input = String.join(" ", words);
                return new RepeatCommand(input, COMMANDTYPE_ADD);
            default:
                String errorMessage = "Wrong number of arguments provided";
                throw new WrongNumberOfArgumentsException(errorMessage);

            }
        } catch (WrongNumberOfArgumentsException e) {
            String errorMessage = e.getMessage();
            return new RepeatCommand(errorMessage, COMMANDTYPE_ERROR);
        } catch (NumberFormatException e) {
            String errorMessage =  "Numbers are not in numeric form";
            return new RepeatCommand(errorMessage, COMMANDTYPE_ERROR);
        }

        

    }

    /**
     * Change the name of the list to ensure the first character is capitalised.
     *
     * @param name String containing name of list for the first character to be capitalised
     * @return String with first character capitalised
     */
    private static String formatListName(String name) {
        name = name.toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Checks if the string can be converted to an integer.
     *
     * @param number String containing the String form of an integer
     */
    private static void isValidNumber(String number) throws NumberFormatException{
        Integer.parseInt(number);
    }

    /**
     * List command. Used to show dates of repeated events.
     *
     * @param data location where all user event information is stored
     * @param ui User Interface class for printing on screens
     */
    private void executeList(UserData data, Ui ui) {
        String[] words = command.split(" ");
        EventList eventList = data.getEventList(words[0]);
        int index = Integer.parseInt(words[1]) - 1;
        Event repeatEvent = eventList.getEventByIndex(index);
        ui.printRepeatList(repeatEvent);
    }

    /**
     * Add command. Used to add repeated dates to an event.
     *
     * @param data location where all user event information is stored
     * @param ui User Interface class for printing on screens
     * @param storage File storage location on computer
     */
    private void executeAdd(UserData data, Ui ui, Storage storage)
            throws MissingDeadlineRepeatException, InvalidTypeException, IndexOutOfBoundsException {
        String[] words = command.split(" ");
        EventList eventList = data.getEventList(words[0]);

        if (eventList == null) {
            throw new InvalidEventListTypeException(words[0]);
        }
        int index = Integer.parseInt(words[1]) - 1;
        Event eventToRepeat = eventList.getEventByIndex(index);
        LocalDate startDate = eventToRepeat.getDate();
        if (startDate == null) {
            throw new MissingDeadlineRepeatException();
        }
        LocalTime startTime = eventToRepeat.getTime();
        int count = Integer.parseInt(words[3]);
        Repeat repeat = new Repeat(startDate, startTime, words[2], count);
        eventToRepeat.setRepeat(repeat);
        ui.printRepeatAdd(eventToRepeat);
    }

    private void executeNull(UserData data, Ui ui, Storage storage) {
        //print the error message of the command
        ui.printExceptionMessage(this.command);
    }
}
