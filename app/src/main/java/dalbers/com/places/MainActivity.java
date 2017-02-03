package dalbers.com.places;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements LocationContract.View {

    private GoogleApiClient apiClient;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int PERMISSIONS_KEY = 1;
    private LocationContract.Presenter presenter;
    @BindView(R.id.main_location_text_view)
    TextView locationTextView;
    @BindView(R.id.main_place_name_edit_text)
    EditText placeNameEditText;
    @BindView(R.id.main_log_text_view)
    TextView logView;

    GoogleApiClient.ConnectionCallbacks connectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    Timber.v("Api connection established");
                    presenter.checkLocationSettings();
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Timber.v("Api connection suspended");
                }
            };

    GoogleApiClient.OnConnectionFailedListener connectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    Timber.v("Api connection failed");
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkPermissions();
        LocationWrapper model = new LocationWrapper();
        GeofenceWrapper geofenceWrapper = new GeofenceWrapper();
        presenter = new LocationPresenter(this, model, geofenceWrapper);
        logView.setMovementMethod(new ScrollingMovementMethod());
        logView.setText(presenter.getEventsAsString());
    }

    @OnClick (R.id.main_add_location_button)
    public void addLocation() {
        presenter.addGeofenceCurrentLocation(placeNameEditText.getText().toString());
        presenter.endLocationUpdates();
    }

    @Override @Nullable
    public GoogleApiClient getApiClient() {
        return apiClient;
    }

    private void checkPermissions() {
        //request permissions on Android 6+, not needed on less than <6
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS, PERMISSIONS_KEY);
            }
        else {
            //we already have permissions
            setupApiClient();
        }
    }

    private void setupApiClient() {
        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(connectionCallbacks)
                    .addOnConnectionFailedListener(connectionFailedListener)
                    .addApi(LocationServices.API)
                    .build();
        }
        apiClient.connect();
    }

    @Override
    protected void onStop() {
        if (apiClient.isConnected())
            presenter.endLocationUpdates();
        apiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_KEY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        //TODO: show a dialog
                        finish();
                    }
                }
                setupApiClient();
                apiClient.connect();

            } else {
                //TODO: show a dialog
                finish();
            }
        }
    }

    @Override
    public void updateLocation(Location location) {
        Timber.v("Location update: %f, %f, %f", location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy());
        locationTextView.setText(location.getLatitude() + "," + location.getLongitude());
    }

    @Override
    public void showNoLocationSettings() {
        //TODO: handle this
        Timber.w("No location settings!");
    }
}
