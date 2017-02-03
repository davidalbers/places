package dalbers.com.places;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dalbers.com.places.realm.Event;
import io.realm.RealmResults;

/**
 * Created by davidalbers on 1/23/17.
 */

public class EventParser {

    private static final int MINIMUM_DWELL_TIME_MINUTES = 5;

    static String rawEnterExitsToTimeSpent(RealmResults<Event> events) {
        StringBuilder eventsAsString = new StringBuilder(events.size() * 10);
        boolean insidePlace = false;
        Event entryEvent = null;
        for (Event event : events) {
            if (event.isEntered() && !insidePlace) {
                //entered a new place
                insidePlace = true;
                entryEvent = event;
            }
            else if (!event.isEntered() && insidePlace
                    && event.getName().equals(entryEvent.getName())) {
                //exiting the place we previously entered
                Map<TimeUnit, Long> timeDwelled =
                        computeDiff(entryEvent.getDate(), event.getDate());
                if (isLongEnough(MINIMUM_DWELL_TIME_MINUTES, timeDwelled)) {
                    SimpleDateFormat formatter = new SimpleDateFormat("MMM d, H:mm");
                    eventsAsString.append(event.getName());
                    eventsAsString.append(" @");
                    eventsAsString.append(formatter.format(entryEvent.getDate()));
                    eventsAsString.append(": ");
                    eventsAsString.append(timeSpentToHMS(timeDwelled));
                    eventsAsString.append("\n");
                    insidePlace = false;
                }
            }
        }
        return eventsAsString.toString();
    }

    private static StringBuilder timeSpentToHMS(@NonNull Map<TimeUnit, Long> timeDwelled) {
        StringBuilder timeAsString = new StringBuilder();
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
