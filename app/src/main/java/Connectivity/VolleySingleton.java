package Connectivity;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import novitskyvitaly.geogroup.R;

/**
 * Created by Asher on 06.08.2016.
 */
public class VolleySingleton {

    private static VolleySingleton mInstance;
    private RequestQueue requestQueue;
    private static Context ctx;
    private static String serverApiKey;

    private VolleySingleton(Context context){
        ctx = context;
        serverApiKey = ctx.getString(R.string.server_api_key);
        requestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue(){
        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext(), new CustomHurlStack());
        return requestQueue;
    }

    public static synchronized VolleySingleton getmInstance(Context context){
        if(mInstance == null)
            mInstance = new VolleySingleton(context);
        return mInstance;
    }

    public<T> void addToRequestQueue(Request<T> request){
        requestQueue.add(request);
    }

    private class CustomHurlStack extends HurlStack{
        @Override
        protected HttpURLConnection createConnection(URL url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Authorization", "key=" + serverApiKey);
            return connection;
        }
    }
}
