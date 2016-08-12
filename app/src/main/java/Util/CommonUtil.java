package Util;

import android.content.Context;
import android.content.SharedPreferences;

import novitskyvitaly.geogroup.R;

/**
 * Created by Asher on 12.08.2016.
 */
public class CommonUtil {

    public static void SetIsApplicationRunningInForeground(Context ctx, boolean isRunning){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.is_app_running_in_foreground_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(ctx.getString(R.string.is_app_running_in_foreground_key), isRunning);
        editor.commit();
    }

    public static boolean GetIsApplicationRunningInForeground(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.is_app_running_in_foreground_token), Context.MODE_PRIVATE);
        return sp.getBoolean(ctx.getString(R.string.is_app_running_in_foreground_key), false);
    }


}
