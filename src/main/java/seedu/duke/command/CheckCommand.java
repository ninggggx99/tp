package seedu.duke.command;

import seedu.duke.event.Event;
import seedu.duke.event.EventList;
import seedu.duke.exception.DateErrorException;
import seedu.duke.exception.DukeException;
import seedu.duke.exception.TimeErrorException;
import seedu.duke.exception.TryRegularParserException;
import seedu.duke.storage.Storage;
import seedu.duke.ui.Ui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;


import seedu.duke.data.UserData;

import static seedu.duke.parser.DateTimeParser.timeParser;

/**
 * Command to check availability.
 */
public class CheckCommand extends Command {
    /**
     * Constructor for checking availability seedu.duke
     *
     * @param command from user input
     */
    public CheckCommand(String command) {
        this.isExit = false;
        this.command = command;
    }

    @Override
    public void execute(UserData data, Ui ui, Storage storage) throws DukeException {
        String[] datesAndTime = command.split(";");

        LocalDate startDate = getDate(datesAndTime[0].trim());
        LocalDate endDate = getDate(datesAndTime[2].trim());

        LocalTime startTime = getTime(datesAndTime[1].trim());
        LocalTime endTime = getTime(datesAndTime[3].trim());

        ArrayList<Event> eventsInTimeRange = new ArrayList<>();
        String[] eventTypes = new String[]{"Personal", "Timetable", "Zoom"};
        for (String type: eventTypes) {
            EventList eventsList = data.getEventList(type);
            eventsInTimeRange.addAll(checkEventsInTimeRange(eventsList, startDate, endDate, startTime, endTime));
        }
        EventList coinciding = new EventList("coinciding", eventsInTimeRange);

        ui.printList(coinciding);
    }

    private LocalDate getDate(String stringDate) throws DateErrorException {

        String[] dateFields = stringDate.replace("-","/").split("/");

        LocalDate date;
        LocalDate currentDate = LocalDate.now();

        if (stringDate.isBlank()) { // if date is blank, defaults to current date
            return currentDate;
        }

        try {
            switch (dateFields.length) {
            case 1: // only year is given
                DateTimeFormatter yearFormat = DateTimeFormatter.ofPattern("yy");
                Year givenYear = Year.parse(stringDate, yearFormat);
                date = currentDate.with(givenYear);
                return date;
            case 2: // month and year is given
                DateTimeFormatter yearMonthFormat = DateTimeFormatter.ofPattern("M/yy");
                YearMonth givenYearMonth = YearMonth.parse(stringDate, yearMonthFormat);
                date = currentDate.with(givenYearMonth);
                return date;
            case 3: // day, month and year given
                DateTimeFormatter dayMonthYearFormat = DateTimeFormatter.ofPattern("d/M/yy");
                date = LocalDate.parse(stringDate, dayMonthYearFormat);
                return date;
            default:
                throw new DateErrorException("Something is wrong with the date!");
            }
        } catch (DateTimeParseException e) {
            throw new DateErrorException("Something is wrong with the date!");
        }
    }

    private LocalTime getTime(String stringTime) throws TimeErrorException {
        LocalTime time;
        if (stringTime.isBlank()) { // if blank time is provided, default to current time
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:m a");
            String currentTime = LocalTime.now().format(timeFormatter);
            time = LocalTime.parse(currentTime, timeFormatter);
            return time;
        }

        String[] stringTimeArray = stringTime.split(" ");

        try {
            if (stringTimeArray.length == 2) { // 12 hour format hh a
                int givenTwelveHour = Integer.parseInt(stringTimeArray[0]);
                String amPmIndicator = stringTimeArray[1];
                if (givenTwelveHour >= 0 & givenTwelveHour <= 12) {
                    time = timeParser(givenTwelveHour + ":00 " + amPmIndicator); // default to minute 00
                    return time;
                } else {
                    throw new TryRegularParserException("hh a format time requires hours between 1-12.");
                }
            } else if (stringTimeArray.length == 1) { // 24 hour format HH
                int givenTwentyFourHour = Integer.parseInt(stringTimeArray[0]);
                if (givenTwentyFourHour >= 0 & givenTwentyFourHour <= 24) {
                    time = timeParser(givenTwentyFourHour + ":00"); // default to minute 00
                    return time;
                } else {
                    throw new TryRegularParserException("HH format time requires hours between 0-23.");
                }
            } else {
                throw new TimeErrorException("Something is wrong with the time!");
            }
        } catch (NumberFormatException | TryRegularParserException e) {
            // if hh:mm, HH:mm or other invalid non integers is given
            time = timeParser(stringTime); // exception will be thrown if invalid non-integer is given
            return time;
        }
    }

    public ArrayList<Event> checkEventsInTimeRange(EventList eventsList, LocalDate startDate, LocalDate endDate,
                                                   LocalTime startTime, LocalTime endTime) {
        ArrayList<Event> eventsInTimeRange = new ArrayList<>();

        for (Event event : eventsList.getEvents()) {
            boolean eventIsBetweenDate = event.getDate().isAfter(startDate) && event.getDate().isBefore(endDate);

            boolean eventIsBetweenTime;
            if (eventIsBetweenDate) {
                eventIsBetweenTime = true;
            } else if (event.getDate().isEqual(startDate)) {
                eventIsBetweenTime = !(event.getTime().isBefore(startTime));
            } else if (event.getDate().isEqual(endDate)) {
                eventIsBetweenTime = !(event.getTime().isAfter(endTime));
            } else {
                eventIsBetweenTime = false;
            }

            if (eventIsBetweenTime) {
                eventsInTimeRange.add(event);
            }
        }

        return eventsInTimeRange;
    }
}
