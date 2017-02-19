package dalbers.com.places;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dalbers.com.places.realm.Event;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * Created by davidalbers on 1/23/17.
 */

public class EventParser {

    private static final int MINIMUM_DWELL_TIME_MINUTES = 15;

    private enum State {
        WAITING_ENTRY,
        INSIDE,
        WAITING_NEXT
    }

    static String rawEnterExitsToTimeSpent(@NonNull RealmResults<Event> events) {
        StringBuilder eventsAsString = new StringBuilder(events.size() * 10);

        if (events.isEmpty())
            return "";

        Event entryEvent = events.get(0);
        Event previousEvent = events.get(0);
        State state = State.WAITING_ENTRY;
        for (Event event : events) {
            Timber.v("Event %s @%s",event.getName(), event.getDate().toString());
            switch (state) {
                case WAITING_ENTRY:
                    Timber.v(" waiting entry");
                    if (event.isEntered()) {
                        state = State.INSIDE;
                        entryEvent = event;
                    }
                    break;
                case INSIDE:
                    if (event.equals(entryEvent)) {
                        Map<TimeUnit, Long> timeDwelled =
                                computeDiff(entryEvent.getDate(), event.getDate());
                        if (isLongEnough(MINIMUM_DWELL_TIME_MINUTES, timeDwelled)) {
                            if (event.isEntered()) {
                                Timber.v(" inside %s, waited %s, staying", event.getName(), timeSpentToHMS(timeDwelled));
                                entryEvent = event;
                            }
                            else {
                                Timber.v(" inside %s, waited %s, moving to next", event.getName(), timeSpentToHMS(timeDwelled));
                                state = State.WAITING_NEXT;
                            }
                        }
                        else {
                            Timber.v(" inside %s, waited %s, not long enough so staying", event.getName(), timeSpentToHMS(timeDwelled));
                        }
                    }
                    else {
                        Timber.v(" inside %s but found %s, resetting to new place", entryEvent.getName(), event.getName());
                        //reset, different place
                        entryEvent = event;
                    }
                    break;
                case WAITING_NEXT:
                    Map<TimeUnit, Long> timeDwelled =
                            computeDiff(previousEvent.getDate(), event.getDate());
                    if (!entryEvent.equals(event)
                           || isLongEnough(MINIMUM_DWELL_TIME_MINUTES, timeDwelled)) {
                            Timber.v(" %s waiting, found %s, going to new event, waited %s", entryEvent.getName(), event.getName(), timeSpentToHMS(timeDwelled));
                            if (entryEvent.equals(event) && !event.isEntered()) {
                                Timber.v(" Printing events for %s, entry @%s exit @%s",entryEvent.getName(), entryEvent.getDate().toString(), previousEvent.getDate().toString());
                                appendEvent(eventsAsString, entryEvent, event);
                            }
                            else {
                                Timber.v(" Printing events for %s, entry @%s exit @%s",entryEvent.getName(), entryEvent.getDate().toString(), event.getDate().toString());
                                appendEvent(eventsAsString, entryEvent, previousEvent);
                            }
                            entryEvent = event;
                            state = State.INSIDE;
                    }
                    else {
                        Timber.v(" Staying in waiting for new event");
                    }
                    break;
            }
            previousEvent = event;
        }
        return eventsAsString.toString();
    }

    private static void appendEvent(@NonNull StringBuilder eventsAsString, @NonNull Event entryEvent,
                                    @NonNull Event event) {
        Map<TimeUnit, Long> timeDwelled =
                computeDiff(entryEvent.getDate(), event.getDate());
        Timber.v("Diffing %s & %s", entryEvent.getDate(), event.getDate());
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, H:mm", Locale.US);
        eventsAsString.append(event.getName());
        eventsAsString.append(" @");
        eventsAsString.append(formatter.format(entryEvent.getDate()));
        eventsAsString.append(": ");
        eventsAsString.append(timeSpentToHMS(timeDwelled));
        eventsAsString.append("\n");
    }

    private static StringBuilder timeSpentToHMS(@NonNull Map<TimeUnit, Long> timeDwelled) {
        StringBuilder timeAsString = new StringBuilder();
        Timber.v("timeSpentToHMS %s",timeDwelled.values().toString());
        if (timeDwelled.get(TimeUnit.DAYS) > 0) {
            timeAsString.append(timeDwelled.get(TimeUnit.DAYS));
            timeAsString.append("d ");
        }
        if (timeDwelled.get(TimeUnit.HOURS) > 0) {
            timeAsString.append(timeDwelled.get(TimeUnit.HOURS));
            timeAsString.append("h ");
        }
        if (timeDwelled.get(TimeUnit.MINUTES) > 0) {
            timeAsString.append(timeDwelled.get(TimeUnit.MINUTES));
            timeAsString.append("m ");
        }
        return timeAsString;
    }

    private static boolean isLongEnough(int minDwellTimeMinutes, @NonNull Map<TimeUnit, Long> timeDwelled) {
        return timeDwelled.get(TimeUnit.DAYS) != 0 || timeDwelled.get(TimeUnit.HOURS) != 0
                || timeDwelled.get(TimeUnit.MINUTES) >= minDwellTimeMinutes;
    }

    private static Map<TimeUnit,Long> computeDiff(@NonNull Date date1, @NonNull Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        List<TimeUnit> units = new ArrayList<>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);
        Map<TimeUnit, Long> result = new EnumMap<>(TimeUnit.class);
        long milliesRest = diffInMillies;
        for ( TimeUnit unit : units ) {
            long diff = unit.convert(milliesRest,TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;
            result.put(unit,diff);
        }
        return result;
    }
}
