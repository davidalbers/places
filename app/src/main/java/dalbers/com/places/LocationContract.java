package dalbers.com.places;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

/**
 * Created by davidalbers on 1/21/17.
 * Interaction contract for getting location
 */

class LocationContract {

    interface View {
        void updateLocation(Location location);
        void showNoLocationSettings();
        @Nullable GoogleApiClient getApiClient();
    }

    interface Presenter {
        void endLocationUpdates();
        void updateLocation(Location location);
        void askToTurnOnNavigation(Status status);
        void checkLocationSettings();
        void onNoLocationSettings();
        void onLocationSettingsOkay();
        @Nullable GoogleApiClient getApiClient();
        void addGeofenceCurrentLocation(@NonNull String name);
        void removeGeofence();
        @NonNull String getEventsAsString();
    }

    interface LocationModel {
        void startLocationUpdates(Activity activity);
        void endLocationUpdates();
        void checkLocationSettings();
        void setPresenter(@NonNull LocationContract.Presenter presenter);
    }

    interface GeofenceModel {
        void addGeofence(@NonNull Activity activity, @NonNull Location location,
                         @NonNull String requestId);
        void removeGeofence(@NonNull String requestId);
        void setPresenter(@NonNull LocationContract.Presenter presenter);
    }
}
