package dalbers.com.places;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.Manifest;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import timber.log.Timber;

/**
 * Created by davidalbers on 1/20/17.
 * Gets location updates and handles associated setup/tear-down
 */
class LocationWrapper implements LocationContract.LocationModel {

    private LocationRequest locationRequest;
    private LocationContract.Presenter presenter;

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            presenter.updateLocation(location);
        }
    };

    public void setPresenter(@NonNull LocationContract.Presenter presenter) {
        this.presenter = presenter;
    }

    private ResultCallback<LocationSettingsResult> resultCallback =
            new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        presenter.onLocationSettingsOkay();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        presenter.askToTurnOnNavigation(status);
                        break;
                    default:
                        presenter.onNoLocationSettings();
                        break;
                }
            }
        };


    @Override
    public void startLocationUpdates(Activity activity) {
        if (activity == null ||
                (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)) {
            Timber.w("Cannot start location updates");
            return;
        }
        if(checkApiClient()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    presenter.getApiClient(), locationRequest, locationListener);
        }
    }

    @Override
    public void endLocationUpdates() {
        if(checkApiClient()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    presenter.getApiClient(), locationListener);
        }
    }

    @Override
    public void checkLocationSettings() {
        if(checkApiClient()) {
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1000);
            locationRequest.setMaxWaitTime(5000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(presenter.getApiClient(),
                            builder.build());

            result.setResultCallback(resultCallback);
        }
    }

    private boolean checkApiClient() {
        if (presenter.getApiClient() != null)
            return true;
        else {
            Timber.w("Api client is null!");
            //TODO: show dialog
            return false;
        }
    }
}
