package dalbers.com.places.realm;

import android.support.annotation.NonNull;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by davidalbers on 1/22/17.
 */

public class PlaceRealm {
    public static void addPlace(@NonNull Realm realm, @NonNull final Place place) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(place);
            }
        });
    }

    public static void addEvent(@NonNull Realm realm, @NonNull final Event event) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(event);
            }
        });
    }

    public static RealmResults<Place> getAllPlaces(@NonNull Realm realm) {
        return realm.where(Place.class).findAll();
    }

    public static RealmResults<Event> getAllEvents(@NonNull Realm realm) {
        return realm.where(Event.class).findAll();
    }

    public static Place getPlace(@NonNull Realm realm, @NonNull String name) {
        return realm.where(Place.class)
                .equalTo(Place.NAME_KEY, name)
                .findFirst();
    }
}
