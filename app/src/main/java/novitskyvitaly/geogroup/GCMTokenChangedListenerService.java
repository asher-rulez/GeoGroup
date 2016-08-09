package novitskyvitaly.geogroup;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.iid.InstanceIDListenerService;

public class GCMTokenChangedListenerService extends InstanceIDListenerService {
    public GCMTokenChangedListenerService() {
    }

    @Override
    public void onTokenRefresh() {
        GCMInstanceIDListenerService.StartRegisterToGCM(this);
    }
}
