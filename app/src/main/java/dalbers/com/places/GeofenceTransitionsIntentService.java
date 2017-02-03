package dalbers.com.places;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

import dalbers.com.places.realm.Event;
import dalbers.com.places.realm.PlaceRealm;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by davidalbers on 1/22/17.
 * Handles a Geofence event
 * Taken from https://developer.android.com/samples/Geofencing/Application/src/com.example.android.wearable.geofencing/GeofenceTransitionsIntentService.html
 */
public class GeofenceTransitionsIntentService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Timber.e("Geofencing error, code: %d", geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            boolean entered = geofenceTransition ==  Geofence.GEOFENCE_TRANSITION_ENTER;
            String transition = entered? "enter" : "exit";
            StringBuilder geofences = new StringBuilder();
            Realm realm = Realm.getDefaultInstance();
            //record this event for all matching places
            for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                geofences.append(geofence.getRequestId());
                geofences.append(", ");
                PlaceRealm.addEvent(realm, new Event(geofence.getRequestId(), new Date(), entered));
            }
            realm.close();

            Timber.v("Handled geofence %s for %s", transition, geofences);

        } else {
            Timber.e("Transition invalid");
        }
    }
}

