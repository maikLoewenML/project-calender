import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventDateTime
import org.joda.time.DateTimeConstants
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.security.GeneralSecurityException
import java.util.*
import org.joda.time.Days
import org.joda.time.LocalDate
import kotlin.math.*
import org.joda.time.DateTime as DateTime1


class CalenderApi {
    /**
     * Application name.
     */
    private val APPLICATION_NAME = "Google Calendar API Java Quickstart"

    /**
     * Global instance of the JSON factory.
     */
    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()

    /**
     * Directory to store authorization tokens for this application.
     */
    private val TOKENS_DIRECTORY_PATH = "tokens"

    /**
     * Global instance of the scopes.
     * The tokens/ folder has to be deleted if modified.
     */
    private val SCOPES = listOf<String>(CalendarScopes.CALENDAR)
    private val CREDENTIALS_FILE_PATH = "/credentials.json"

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Throws(IOException::class)
    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential? {
        // Load client secrets.
        val `in` =
            this::class.java.getResourceAsStream(this.CREDENTIALS_FILE_PATH)
                ?: throw FileNotFoundException("Resource not found: " + this.CREDENTIALS_FILE_PATH)
        val clientSecrets =
            GoogleClientSecrets.load(this.JSON_FACTORY, InputStreamReader(`in`))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, this.JSON_FACTORY, clientSecrets, this.SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(File(this.TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        //returns an authorized Credential object.
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun activate() {
        // Build a new authorized API client service.
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        val service = Calendar.Builder(
                HTTP_TRANSPORT,
                this.JSON_FACTORY,
                getCredentials(HTTP_TRANSPORT)
        )
            .setApplicationName(this.APPLICATION_NAME)
            .build()

        displayFutureEvents()
    }

    fun displayFutureEvents() {
        // Build a new authorized API client service.
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        val service = Calendar.Builder(HTTP_TRANSPORT, this.JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(this.APPLICATION_NAME)
                .build()

        // List the next 20 events from the primary calendar.
        val now = com.google.api.client.util.DateTime(System.currentTimeMillis())
        val events = service.events().list("primary")
                .setMaxResults(20)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
        val items = events.items
        if (items.isEmpty()) {
            println("No upcoming events found.")
        } else {
            println("Upcoming events")
            for (event in items) {
                var start = event.start.dateTime
                if (start == null) {
                    start = event.start.date
                }
                println(event.summary+" "+event.id+" "+event.recurringEventId+" "+start)
            }
        }
    }

    /**
     * starker Meetingabfall --> ein Zeitraum (Dip)
     * normaler Meetingabfall --> 3 Zeiträume
     * -> Zeitraum 1 und 3 geringere regelmäßige Meetinghäufigkeit
     * → Zeitraum 2 geringste angegebene Meetinghäufigkeit
     * schwacher Meetingabfall --> 5 Zeiträume
     * -> Zeitraum 1 und 5 geringere Meetinghäufigkeit
     * → Zeitraum 2 und 4 noch geringere Meetinghäufigkeit
     * → Zeitraum 3 geringste Meetinghäufigkeit
     */

    /**
     * TODO: Kommazahlen sollen funktionieren
     *
     */

    /**
     * Creates a project with a specified name, start and end date, meeting duration, frequency factor, participants,
     * description, and location. The function prompts the user for additional information regarding meeting frequency
     * and decay rate to calculate the optimal meeting schedule for the project.
     * @param name the name of the project
     * @param start the start date of the project in string format (yyyy-MM-dd)
     * @param ende the end date of the project in string format (yyyy-MM-dd)
     * @param teilnehmer the participants in the project
     * @param beschreibung the description of the project
     * @param ort the location of the project
     */
    fun createProject(name: String, start: String, ende: String, teilnehmer: String, beschreibung: String, ort: String) {
        val startDate = DateTime1.parse(start)
        val endDate = DateTime1.parse(ende)
        println("Wie oft sollen Meetings stattfinden? (pro Woche): ")
        val meetingFrequency = readlnOrNull()?.toDoubleOrNull() ?: 1
        println("Wie oft sollen Meetings in der Mitte des Projekts stattfinden? (pro Woche)")
        //printItalic("Falls Sie ein Meeting bspw. alle zwei Wochen durchführen, dann geben Sie 0.5 ein: ")
        val meetingMinimumFrequency = readlnOrNull()?.toDoubleOrNull() ?: meetingFrequency
        println("Wie stark soll der Häufigkeitsabfall sein? (stark, normal, schwach): ")
        val decay = when(readlnOrNull()?.lowercase()) {
            "stark" -> DecayRate.STRONG
            "normal" -> DecayRate.NORMAL
            "schwach" -> DecayRate.WEAK
            else -> DecayRate.NORMAL
        }
        val daysBetween = calculateDaysBetween(startDate, endDate)
        val weeksTotal = daysToWeeks(daysBetween)
        val totalMeetings = meetingFrequency.toInt() * weeksTotal
        val durationBetweenMeetings = daysBetween / totalMeetings
        var currentDate = startDate
        var weeksToReachTheDip = 2
        if (decay == DecayRate.STRONG) weeksToReachTheDip = 1
        else if (decay == DecayRate.WEAK) weeksToReachTheDip = 3
        val middleDay = calculateMiddleDay(startDate, endDate, daysBetween)
        var stepsBetweenList = generateSteps(meetingFrequency as Double, meetingMinimumFrequency as Double, weeksToReachTheDip - 1)
        val startDateMeetingFrequencyGap = middleDay.minusWeeks(weeksToReachTheDip)
        val endDateFrequencyGap = middleDay.plusWeeks(weeksToReachTheDip)
        createMeetingEventsInDip(name, teilnehmer, beschreibung, ort, startDateMeetingFrequencyGap, endDateFrequencyGap, decay, meetingMinimumFrequency, stepsBetweenList)
        createMeetingEventsBeforeDip(currentDate, startDateMeetingFrequencyGap, name, teilnehmer, beschreibung, ort, durationBetweenMeetings)
        createMeetingEventsAfterDip(endDateFrequencyGap, endDate, name, teilnehmer, beschreibung, ort, durationBetweenMeetings)
    }

    /**
     * Creates meeting events after the end of the frequency gap until the given end date.
     * Each meeting event will have a duration of 12.5 hours and the specified duration between meetings.
     * @param endDateFrequencyGap the end date of the frequency gap period
     * @param endDate the end date until which meeting events should be created
     * @param name the name of the meeting events
     * @param teilnehmer the participants of the meeting events
     * @param beschreibung the description of the meeting events
     * @param ort the location of the meeting events
     * @param durationBetweenMeetings the duration between two consecutive meeting events
     */
    private fun createMeetingEventsAfterDip(endDateFrequencyGap: DateTime1, endDate: DateTime1, name: String, teilnehmer: String, beschreibung: String, ort: String, durationBetweenMeetings: Int) {
        var currentDate = endDateFrequencyGap
        while(currentDate < endDate) {
            createEvent(name, currentDate.plusHours(11).toString(), currentDate.plusMinutes(750).toString(), teilnehmer, beschreibung,
                ort, arrayOf()
            )
            currentDate = currentDate.plusDays(durationBetweenMeetings)
        }
    }

    /**
     * Creates meeting events before the dip period.
     * This function creates a series of events with the specified name, description, location, and participants, occurring at
     * regular intervals before the start date of the dip period, with a duration between meetings specified in minutes.
     * @param pCurrentDate The starting date from which to create the meeting events.
     * @param startDateMeetingFrequencyGap The start date of the dip period, before which the meeting events are to be created.
     * @param name The name of the meeting event.
     * @param teilnehmer The participants of the meeting event.
     * @param beschreibung The description of the meeting event.
     * @param ort The location of the meeting event.
     * @param durationBetweenMeetings The duration in days between successive meetings.
     */
    private fun createMeetingEventsBeforeDip(pCurrentDate: DateTime1, startDateMeetingFrequencyGap: DateTime1, name: String, teilnehmer: String, beschreibung: String, ort: String, durationBetweenMeetings: Int) {
        var currentDate = pCurrentDate
        while(currentDate < startDateMeetingFrequencyGap) {
            createEvent(name, currentDate.plusHours(11).toString(), currentDate.plusMinutes(750).toString(), teilnehmer, beschreibung,
                ort, arrayOf()
            )
            currentDate = currentDate.plusDays(durationBetweenMeetings)
        }
    }

    /**
     * Generates meeting events according to a decay rate for a specific time period with even or varied meeting frequency.
     * @param name the name of the meeting
     * @param teilnehmer the attendees of the meeting
     * @param beschreibung the description of the meeting
     * @param ort the location of the meeting
     * @param startDateMeetingFrequencyGap the start date of the time period
     * @param endDateFrequencyGap the end date of the time period
     * @param decay the decay rate to determine the meeting frequency
     * @param meetingMinimumFrequency the minimum meeting frequency in days
     * @param stepsBetweenList a list of steps between each meeting
     * @throws IllegalArgumentException if the meetingMinimumFrequency or the stepsBetweenList is negative or the stepsBetweenList size is not valid
     */
    private fun createMeetingEventsInDip(name: String, teilnehmer: String, beschreibung: String, ort: String, startDateMeetingFrequencyGap: DateTime1, endDateFrequencyGap: DateTime1, decay: DecayRate, meetingMinimumFrequency: Double, stepsBetweenList: List<Double>) {
        if (decay == DecayRate.STRONG) {
            val meetingListInDip = evenMeetingsBetweenTwoDates(startDateMeetingFrequencyGap, endDateFrequencyGap, meetingMinimumFrequency.toInt())
            createEvents(name, teilnehmer, beschreibung, ort, meetingListInDip)
        } else if (decay == DecayRate.WEAK) {
            val equalTimePeriods = findEqualTimePeriods(startDateMeetingFrequencyGap, endDateFrequencyGap, 5)

            //first and fifth meetingPeriod
            val meetingListInFirstTimePeriodDip = evenMeetingsBetweenTwoDates(equalTimePeriods[0].first, equalTimePeriods[0].second, stepsBetweenList[0].toInt())
            createEvents(name, teilnehmer, beschreibung, ort, meetingListInFirstTimePeriodDip)
            val meetingListInFifthTimePeriodDip = evenMeetingsBetweenTwoDates(equalTimePeriods[4].first, equalTimePeriods[4].second, stepsBetweenList[0].toInt())
            createEvents(name, teilnehmer, beschreibung, ort, meetingListInFifthTimePeriodDip)

            //second and fourth meetingPeriod
            val meetingListInSecondTimePeriodDip = evenMeetingsBetweenTwoDates(equalTimePeriods[1].first, equalTimePeriods[1].second, stepsBetweenList[1].toInt())
            createEvents(name, teilnehmer, beschreibung, ort, meetingListInSecondTimePeriodDip)
            val meetingListInFourthTimePeriodDip = evenMeetingsBetweenTwoDates(equalTimePeriods[4].first, equalTimePeriods[4].second, stepsBetweenList[1].toInt())
            createEvents(name, teilnehmer, beschreibung, ort, meetingListInFourthTimePeriodDip)

            //third meetingPeriod
            val meetingListInThirdDip = evenMeetingsBetweenTwoDates(equalTimePeriods[2].first, equalTimePeriods[2].second, meetingMinimumFrequency.toInt())
            createEvents(name, teilnehmer, beschreibung, ort, meetingListInThirdDip)
        } else {
            val equalTimePeriods = findEqualTimePeriods(startDateMeetingFrequencyGap, endDateFrequencyGap, 3)

            //first and third meetingPeriod
            val meetingListInFirstTimePeriodDip = evenMeetingsBetweenTwoDates(equalTimePeriods[0].first, equalTimePeriods[0].second, stepsBetweenList[0].toInt())
            createEvents(name, teilnehmer, beschreibung, ort, meetingListInFirstTimePeriodDip)
            val meetingListInThirdDip = evenMeetingsBetweenTwoDates(equalTimePeriods[2].first, equalTimePeriods[2].second, stepsBetweenList[0].toInt())
            createEvents(name, teilnehmer, beschreibung, ort, meetingListInThirdDip)

            //second meetingPeriod
            val meetingListInSecondDip = evenMeetingsBetweenTwoDates(equalTimePeriods[1].first, equalTimePeriods[1].second, meetingMinimumFrequency.toInt())
            createEvents(name, teilnehmer, beschreibung, ort, meetingListInSecondDip)
        }
    }

    /**
     * Creates events with the given name, participant, description, location, and meeting dates.
     * @param name The name of the event to create.
     * @param teilnehmer The participant of the event.
     * @param beschreibung The description of the event.
     * @param ort The location of the event.
     * @param meetings The list of DateTime1 objects representing the meeting dates.
     */
    private fun createEvents(name: String, teilnehmer: String, beschreibung: String, ort: String, meetings: List<DateTime1>) {
        for (meeting in meetings) {
            /**
            val dayOfWeek = meeting.dayOfWeek().get()
            if(dayOfWeek == DateTimeConstants.SATURDAY) createEvent(name, meeting.plusDays(2).plusHours(11).toString(), meeting.plusDays(2).plusMinutes(750).toString(), teilnehmer, beschreibung, ort, arrayOf())
            else if(dayOfWeek == DateTimeConstants.SUNDAY) createEvent(name, meeting.plusDays(1).plusHours(11).toString(), meeting.plusDays(1).plusMinutes(750).toString(), teilnehmer, beschreibung, ort, arrayOf())
            else
            */
            createEvent(name, meeting.plusHours(11).toString(), meeting.plusMinutes(750).toString(), teilnehmer, beschreibung, ort, arrayOf())
        }
    }

    /**
     * Finds and returns a list of equal time periods between two date-time objects.
     * @param startDate the starting date-time object
     * @param endDate the ending date-time object
     * @param numberOfPeriods the number of equal time periods to find
     * @return a list of pairs of date-time objects representing the equal time periods between the specified start and end dates
    */
    private fun findEqualTimePeriods(startDate: DateTime1, endDate: DateTime1, numberOfPeriods: Int): List<Pair<DateTime1, DateTime1>> {
        val startLocalDate = LocalDate(startDate)
        val endLocalDate = LocalDate(endDate)
        val totalDays = Days.daysBetween(startLocalDate, endLocalDate).days + 1
        val daysPerPeriod = totalDays / numberOfPeriods
        val periods = mutableListOf<Pair<DateTime1, DateTime1>>()
        var currentStartDate = startDate
        var currentEndDate = startDate.plusDays(daysPerPeriod - 1)
        for (i in 1..numberOfPeriods) {
            periods.add(Pair(currentStartDate, currentEndDate))
            currentStartDate = currentEndDate.plusDays(1)
            currentEndDate = currentStartDate.plusDays(daysPerPeriod - 1)
            if (i == numberOfPeriods - 1) {
                currentEndDate = endDate
            }
        }
        return periods
    }

    /**
     * Returns a list of dates representing even spaced meetings between the start and end dates.
     * @param startDate The starting date for the meetings.
     * @param endDate The ending date for the meetings.
     * @param numberOfMeetingsPerWeek The number of evenly spaced meetings to schedule between the start and end dates.
     * @return A list of dates representing the evenly spaced meetings between the start and end dates.
     */
    private fun evenMeetingsBetweenTwoDates(startDate: DateTime1, endDate: DateTime1, numberOfMeetingsPerWeek: Int): List<DateTime1> {
        var listOfDates = mutableListOf<DateTime1>()
        val days = calculateDaysBetween(startDate, endDate)
        val weeks = daysToWeeks(days)
        val totalMeetings = weeks * numberOfMeetingsPerWeek
        val gapBetweenMeetings = days / totalMeetings - 1
        var currentDate = startDate.plusDays(gapBetweenMeetings)
        listOfDates.add(currentDate)
        while (currentDate < endDate) {
            if(endDate.minusDays(gapBetweenMeetings) > currentDate) listOfDates.add(currentDate.plusDays(gapBetweenMeetings))
            currentDate = currentDate.plusDays(gapBetweenMeetings)
        }
        return listOfDates
    }

    /**
     * Calculates the middle day between two date-time objects.
     * @param startDate the starting date-time object
     * @param endDate the ending date-time object
     * @param days the number of days between the two date-time objects
     * @return the date-time object representing the middle day between the two specified date-time objects
     */
    private fun calculateMiddleDay(startDate: DateTime1, endDate: DateTime1, days: Int): DateTime1 {
        return if (days % 2 == 0) {
            startDate.plusDays(days / 2)
        } else {
            startDate.plusDays(days / 2 + 1)
        }
    }

    /**
     * Generates a list of doubles with a specified number of steps between a start and end value.
     * @param start the starting value of the range
     * @param end the ending value of the range
     * @param steps the number of steps between the start and end values
     * @return a list of doubles with the specified number of steps between the start and end values
     */
    private fun generateSteps(start: Double, end: Double, steps: Int): List<Double> {
        val stepSize = (end - start) / (steps + 1)
        return (1..steps).map { start + it * stepSize }
    }

    enum class DecayRate {
        STRONG,
        NORMAL,
        WEAK
    }

    /**
     * Calculates the number of days between two date-time objects.
     * @param startDate the starting date-time object
     * @param endDate the ending date-time object
     * @return the number of days between the two specified date-time objects, as a positive integer
     */
    private fun calculateDaysBetween(startDate: DateTime1, endDate: DateTime1): Int {
        val daysBetween = Days.daysBetween(startDate.toLocalDate(), endDate.toLocalDate()).days
        return abs(daysBetween)
    }

    /**
     * Converts the specified number of days to weeks.
     * @param days the number of days to be converted to weeks
     * @return the number of weeks equivalent to the specified number of days
     * @throws ArithmeticException if the input parameter is negative, as the function only supports positive values
     */
    private fun daysToWeeks(days: Int): Int {
        return days / 7
    }

    /**
     * Prints the specified text in italic style to the console.
     * @param text the text to be printed in italic style
     */
    private fun printItalic(text: String) {
        val escape = "\u001B[3m"
        val reset = "\u001B[0m"
        print("$escape$text$reset")
    }


    fun createEvent(name: String, start: String, ende: String, teilnehmer: String, beschreibung: String, ort: String, rule: Array<String>): String? {
        // Build a new authorized API client service.
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        val service = Calendar.Builder(
                HTTP_TRANSPORT,
                this.JSON_FACTORY,
                getCredentials(HTTP_TRANSPORT)
        )
            .setApplicationName(this.APPLICATION_NAME)
            .build()

        var event: Event = Event()
            .setSummary(name)
            .setLocation(ort)
            .setDescription(beschreibung)

        val start = EventDateTime()
            .setDateTime(DateTime(start))
            .setTimeZone("Europe/Berlin")
        event.setStart(start)

        val end = EventDateTime()
            .setDateTime(DateTime(ende))
            .setTimeZone("Europe/Berlin")
        event.setEnd(end)

        if (!rule.isNullOrEmpty()) {
            event.setRecurrence(Arrays.asList(*rule))
        }

        var teilnehmerListe = teilnehmer.split(",").map { it.trim() }
        var attendees = mutableListOf<EventAttendee>()
        for (i in teilnehmerListe) {
            attendees.add(EventAttendee().setEmail(i))
        }
        event.setAttendees(attendees)

        val calendarId = "primary"
        event = service.events().insert(calendarId, event).execute()
        System.out.printf("Event created: %s\n", event.getHtmlLink())
        return event.id
    }
    //TODO METHODS: deleteAllEvents, deleteAllProjects, deleteEvent, updateEvent , updateProject
}
