package DataModel;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Asher on 09.08.2016.
 */
public class LocationReport {
    private static final String MY_TAG = "geog_locReport";

    public static final String LOCATION_REPORT_ID_KEY = "_id";
    public static final String LOCATION_REPORT_USER_ID_KEY = "user_id";
    public static final String LOCATION_REPORT_LATITUDE_KEY = "latitude";
    public static final String LOCATION_REPORT_LONGITUDE_KEY = "longitude";
    public static final String LOCATION_REPORT_DATE_KEY = "date";

    public LocationReport(String userID, double lat, double lon){
        set_user_id(userID);
        set_latitude(lat);
        set_longitude(lon);
        set_date(new Date());
    }

    public LocationReport(){}

    private int _id;
    private String _user_id;
    private double _latitude;
    private double _longitude;
    private Date _date;

    public void set_id(int _id) {
        this._id = _id;
    }

    public int get_id() {
        return _id;
    }

    public String get_user_id() {
        return _user_id;
    }

    public void set_user_id(String _user_id) {
        this._user_id = _user_id;
    }

    public double get_latitude() {
        return _latitude;
    }

    public void set_latitude(double _latitude) {
        this._latitude = _latitude;
    }

    public double get_longitude() {
        return _longitude;
    }

    public void set_longitude(double _longitude) {
        this._longitude = _longitude;
    }

    public Date get_date() {
        return _date;
    }

    public long get_date_unix_time(){
        return _date.getTime() / 1000;
    }

    public void set_date(Date _date) {
        this._date = _date;
    }

    public void set_date(long unix_time){
        this._date = new Date(unix_time * 1000);
    }

    public static String[] GetColumnNamesArray(){
        return new String[]{
                LOCATION_REPORT_ID_KEY,
                LOCATION_REPORT_USER_ID_KEY,
                LOCATION_REPORT_LATITUDE_KEY,
                LOCATION_REPORT_LONGITUDE_KEY,
                LOCATION_REPORT_DATE_KEY
        };
    }

    public ContentValues GetContentValuesRow(){
        ContentValues cv = new ContentValues();
        cv.put(LOCATION_REPORT_ID_KEY, get_id());
        cv.put(LOCATION_REPORT_USER_ID_KEY, get_user_id());
        cv.put(LOCATION_REPORT_LATITUDE_KEY, get_latitude());
        cv.put(LOCATION_REPORT_LONGITUDE_KEY, get_longitude());
        cv.put(LOCATION_REPORT_DATE_KEY, get_date_unix_time());
        return cv;
    }

    public static ArrayList<LocationReport> GetArrayListOfLocationReports(Cursor cursor){
        ArrayList<LocationReport> result = new ArrayList<>();
        if(cursor != null && cursor.moveToFirst()){
            do{
                LocationReport lr = new LocationReport();
                lr.set_id(cursor.getInt(cursor.getColumnIndex(LOCATION_REPORT_ID_KEY)));
                lr.set_user_id(cursor.getString(cursor.getColumnIndex(LOCATION_REPORT_USER_ID_KEY)));
                lr.set_latitude(cursor.getDouble(cursor.getColumnIndex(LOCATION_REPORT_LATITUDE_KEY)));
                lr.set_longitude(cursor.getDouble(cursor.getColumnIndex(LOCATION_REPORT_LONGITUDE_KEY)));
                lr.set_date(cursor.getLong(cursor.getColumnIndex(LOCATION_REPORT_DATE_KEY)));
                result.add(lr);
            }while (cursor.moveToNext());
        }
        return result;
    }

    public static LocationReport ParseSingleLocationReportFromJSON(JSONObject jsonObject){
        if(jsonObject == null) return null;
        LocationReport locationReport = new LocationReport();
        try {
            locationReport.set_user_id(jsonObject.getString(LOCATION_REPORT_USER_ID_KEY));
            locationReport.set_latitude(jsonObject.getDouble(LOCATION_REPORT_LATITUDE_KEY));
            locationReport.set_longitude(jsonObject.getDouble(LOCATION_REPORT_LONGITUDE_KEY));
            locationReport.set_date(new Date());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return locationReport;
    }

    public static JSONObject GetJSONObjectForPush(String userID, double latitude, double longitude){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(LOCATION_REPORT_USER_ID_KEY, userID);
            jsonObject.put(LOCATION_REPORT_LATITUDE_KEY, latitude);
            jsonObject.put(LOCATION_REPORT_LONGITUDE_KEY, longitude);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    //region SQL

    public static final String LOCATION_REPORT_TABLE_NAME = "LOCATION_REPORT";

    private static String GetCreateTableCommandText(){
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(LOCATION_REPORT_TABLE_NAME);
        sb.append(" (");
        sb.append(LOCATION_REPORT_ID_KEY);
        sb.append(" integer primary key AUTOINCREMENT not null, ");
        sb.append(LOCATION_REPORT_USER_ID_KEY);
        sb.append(" text not null, ");
        sb.append(LOCATION_REPORT_LATITUDE_KEY);
        sb.append(" real not null, ");
        sb.append(LOCATION_REPORT_LONGITUDE_KEY);
        sb.append(" real not null, ");
        sb.append(LOCATION_REPORT_DATE_KEY);
        sb.append(" long not null);");
        return sb.toString();
    }

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(GetCreateTableCommandText());
    }

    public static void onUpgrade(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_REPORT_TABLE_NAME);
        onCreate(db);
    }

    //endregion SQL
}
