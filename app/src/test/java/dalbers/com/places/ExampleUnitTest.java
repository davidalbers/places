package dalbers.com.places;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ExampleUnitTest {

    @Mock
    LocationContract.View view;

    @Mock
    LocationContract.LocationModel locationModel;


    LocationContract.Presenter presenter;

    @Before
    public void setup() {
        presenter = new LocationPresenter(view, locationModel);
    }

    @Test
    public void testSetup() {
        presenter.checkLocationSettings();
        verify(locationModel).checkLocationSettings();
    }

    @Test
    public void testNoLocationSettings() {
        presenter.onNoLocationSettings();
        verify(view).showNoLocationSettings();
    }

    @Test
    public void testOkayLocationSettings() {
        presenter.onLocationSettingsOkay();
        verify(locationModel).startLocationUpdates(null);
    }

    @Test
    public void testStopLocationUpdates() {
        presenter.endLocationUpdates();
        verify(locationModel).endLocationUpdates();
    }

    @Test
    public void testLocationUpdate() {
        presenter.updateLocation(null);
        verify(view).updateLocation(null);
    }

    @Test
    public void testGetApiClient() {
        presenter.getApiClient();
        verify(view).getApiClient();
    }

}