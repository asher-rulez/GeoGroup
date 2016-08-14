package Util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import novitskyvitaly.geogroup.R;

/**
 * Created by Asher on 12.08.2016.
 */
public class CommonUtil {

    public static String GetAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

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

    public static void SetLocationRefreshFrequency(Context ctx, int frequencyMillis){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.location_refresh_frequency_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(ctx.getString(R.string.location_refresh_frequency_key), frequencyMillis);
        editor.commit();
    }

    public static int GetLocationRefreshFrequency(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.location_refresh_frequency_token), Context.MODE_PRIVATE);
        int fr = sp.getInt(ctx.getString(R.string.location_refresh_frequency_key), -1);
        if(fr == -1){
            fr = ctx.getResources().getInteger(R.integer.location_refresh_default_frequency_rate_milliseconds);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(ctx.getString(R.string.location_refresh_frequency_key), fr);
            editor.commit();
        }
        return fr;
    }

    public static void SaveLocationInSharedPreferences(Context ctx, double latitude, double longitude, Date date){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.last_location_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(ctx.getString(R.string.last_location_latitude), (float)latitude);
        editor.putFloat(ctx.getString(R.string.last_location_longitude), (float)longitude);
        editor.putLong(ctx.getString(R.string.last_location_datetime), date.getTime());
        editor.commit();
    }

    public static long GetLastLocationSavedDateTimeInMillis(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.last_location_token), Context.MODE_PRIVATE);
        return sp.getLong(ctx.getString(R.string.last_location_datetime), -1);
    }

    public static LatLng GetLastLocationLatLng(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.last_location_token), Context.MODE_PRIVATE);
        float lat = sp.getFloat(ctx.getString(R.string.last_location_latitude), -1);
        float lng = sp.getFloat(ctx.getString(R.string.last_location_longitude), -1);
        return new LatLng((double)lat, (double)lng);
    }

    public static void SaveNicknameInSharedPreferences(Context ctx, String nickname){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.nickname_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(ctx.getString(R.string.nickname_key), nickname);
        editor.commit();
    }

    public static String GetMyNickname(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.nickname_token), Context.MODE_PRIVATE);
        return sp.getString(ctx.getString(R.string.nickname_key), "");
    }

    public static ProgressDialog ShowProgressDialog(Context context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle(message);
        progressDialog.show();
        return progressDialog;
    }

    public static void SetEditTextIsValid(Context context, EditText field, boolean isValid) {
        field.getBackground()
                .setColorFilter(isValid ? context.getResources().getColor(R.color.validation_green_text_color) :
                        context.getResources().getColor(R.color.validation_red_text_color), PorterDuff.Mode.SRC_ATOP);
        Bitmap validationBitmap = CommonUtil.decodeScaledBitmapFromDrawableResource(context.getResources(),
                isValid ? R.drawable.validation_ok : R.drawable.validation_wrong,
                context.getResources().getDimensionPixelSize(R.dimen.edittext_validation_img_size),
                context.getResources().getDimensionPixelSize(R.dimen.edittext_validation_img_size));
        Drawable validationDrawable = new BitmapDrawable(validationBitmap);
        field.setCompoundDrawablesWithIntrinsicBounds(validationDrawable, null, null, null);
        field.setCompoundDrawablePadding(10);
    }

    public static void RemoveValidationFromEditText(Context context, EditText field) {
        field.getBackground().setColorFilter(context.getResources()
                .getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        field.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    public static Bitmap decodeScaledBitmapFromDrawableResource(Resources resources, int drawableID,
                                                                int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, drawableID, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, drawableID, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static void SaveGCMTokenToSharedPreferences(Context ctx, String token){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(R.string.sp_gcm_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ctx.getString(R.string.sp_gcm_token_key), token);
        editor.commit();
    }

    public static String GetGCMToken(Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(R.string.sp_gcm_token), Context.MODE_PRIVATE);
        return sharedPreferences.getString(ctx.getString(R.string.sp_gcm_token_key), "");
    }

}
