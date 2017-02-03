package dalbers.com.places;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import dalbers.com.places.realm.Event;
import dalbers.com.places.realm.Place;
import dalbers.com.places.realm.PlaceRealm;
import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * Created by davidalbers on 1/21/17.
 */

class LocationPresenter implements LocationContract.Presenter {

    private static final int REQUEST_CHECK_SETTINGS = 1;

    private LocationContract.View view;
    private LocationContract.LocationModel locationModel;
    private LocationContract.GeofenceModel geofenceModel;
    private Location lastLocation;

    LocationPresenter(@NonNull LocationContract.View view,
                      @NonNull LocationContract.LocationModel locationModel,
                      @NonNull LocationContract.GeofenceModel geofenceModel) {
        this.view = view;
        this.locationModel = locationModel;
        this.geofenceModel = geofenceModel;
        locationModel.setPresenter(this);
        geofenceModel.setPresenter(this);
    }

    private void startLocationUpdates() {
        Timber.v("Starting location updates");
        locationModel.startLocationUpdates(getActivity());
    }

    @Override
    public void endLocationUpdates() {
        locationModel.endLocationUpdates();
    }

    @Override
    public void updateLocation(Location location) {
        if (lastLocation == null || location.getAccuracy() < lastLocation.getAccuracy()) {
            lastLocation = location;
            view.updateLocation(location);
        }
    }

    @Override
    public void askToTurnOnNavigation(@NonNull Status status) {
        if (getActivity() != null) {
            try {
                status.startResolutionForResult(
                        getActivity(),
                        REQUEST_CHECK_SETTINGS);
            } catch (IntentSender.SendIntentException e) {
                Timber.e(e, "Error asking user to turn on navigation");
            }
        }
    }

    @Override
    public void checkLocationSettings() {
        Timber.v("checking location settings");
        locationModel.checkLocationSettings();
    }

    @Override
    public void onNoLocationSettings() {
        view.showNoLocationSettings();
    }

    @Override
    public void onLocationSettingsOkay() {
        Timber.v("Location settings okay");
        startLocationUpdates();
    }

    @Nullable
    @Override
    public GoogleApiClient getApiClient() {
        return view.getApiClient();
    }

    @Override
    public void addGeofenceCurrentLocation(@NonNull String name) {
        if (getActivity() != null && lastLocation != null) {
            Timber.v("Adding geofence %s, %s", lastLocation.getLatitude(),
                    lastLocation.getLongitude());
            geofenceModel.addGeofence(getActivity(), lastLocation, name);
            Realm realm = Realm.getDefaultInstance();
            PlaceRealm.addPlace(realm, new Place(name, lastLocation.getLatitude(),
                    lastLocation.getLongitude()));
            realm.close();
        }
    }

    @Override
    public void removeGeofence() {
        if (getActivity() != null && lastLocation != null) {
            Timber.v("Removing geofence ");
            geofenceModel.removeGeofence("1");
        }
    }

    @Nullable private Activity getActivity() {
        if (view instanceof Context)
            return (Activity)view;
        Timber.w("No activity!");
        return null;
    }

    @NonNull
    public String getEventsAsString() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Event> events = PlaceRealm.getAllEvents(realm);
        String eventsAsString = EventParser.rawEnterExitsToTimeSpent(events);
        realm.close();
        return eventsAsString;
    }

}
