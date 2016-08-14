package novitskyvitaly.geogroup;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import Util.CommonUtil;

public class GCMInstanceIDListenerService extends IntentService {

    private static final String MY_TAG = "geog_IdService";

    private static final String ACTION_REGISTER_TO_GCM = "registerGCM";

    public static void StartRegisterToGCM(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), GCMInstanceIDListenerService.class);
        intent.setAction(ACTION_REGISTER_TO_GCM);
        context.getApplicationContext().startService(intent);
    }

    public GCMInstanceIDListenerService() {
        super("GCMInstanceIDListenerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();
        switch (action){
            case ACTION_REGISTER_TO_GCM:
                RegisterToGCM();
                break;
            default:
                break;
        }
    }

    private void RegisterToGCM(){
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = "";
        try {
            token = instanceID.getToken(getString(R.string.sender_id), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            CommonUtil.SaveGCMTokenToSharedPreferences(this, token);

            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/topic1", null);

            Log.i(MY_TAG, "got token: " + token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
