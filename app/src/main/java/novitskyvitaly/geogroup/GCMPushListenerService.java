package novitskyvitaly.geogroup;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.gcm.GcmListenerService;

public class GCMPushListenerService extends GcmListenerService {
    private static final String MY_TAG = "geog_gcm_listener";

    @Override
    public void onMessageReceived(String s, Bundle bundle) {
        super.onMessageReceived(s, bundle);
    }
}
