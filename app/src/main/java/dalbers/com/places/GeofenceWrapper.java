package dalbers.com.places;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Collections;

import timber.log.Timber;

/**
 * Created by davidalbers on 1/22/17.
 */

public class GeofenceWrapper implements LocationContract.GeofenceModel, ResultCallback<Status> {
    private static final float GEOFENCE_RADIUS_METERS = 100.0f;
    private LocationContract.Presenter presenter;

    @Override
    public void setPresenter(@NonNull LocationContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void addGeofence(@NonNull Activity activity, @NonNull Location location,
                            @NonNull String requestId) {
        if (presenter.getApiClient() == null
                || ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.GeofencingApi.addGeofences(
                presenter.getApiClient(),
                getGeofencingRequest(location, requestId),
                getGeofencePendingIntent(activity)
        ).setResultCallback(this);

    }

    @Override
    public void removeGeofence(@NonNull String requestId) {
        if (presenter.getApiClient() == null)
            return;
        LocationServices.GeofencingApi.removeGeofences(
                presenter.getApiClient(),
                Collections.singletonList(requestId))
                .setResultCallback(this);
    }

    private GeofencingRequest getGeofencingRequest(@NonNull Location location,
                                                   @NonNull String requestId) {
        return new GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(createGeofence(location,requestId))
            .build();
    }

    private Geofence createGeofence(@NonNull Location location, @NonNull String requestId) {
        return new Geofence.Builder()
                .setRequestId(requestId)
                .setCircularRegion(location.getLatitude(), location.getLongitude(),
                        GEOFENCE_RADIUS_METERS)
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    private PendingIntent getGeofencePendingIntent(@NonNull Activity activity) {
        Intent intent = new Intent(activity, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(activity, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (!status.isSuccess()) {
            Timber.w("Creating/removing geofence was unsucessful");
        }
    }
}
