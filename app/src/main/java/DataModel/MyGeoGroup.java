package DataModel;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Asher on 13.08.2016.
 */
public class MyGeoGroup {
    private static final String MY_TAG = "geog_myGroup";

    public static final String MY_GEOGROUP_TABLE_NAME = "GEOGROUP";

    public static final String MY_GEOGROUP_ID_KEY = "_id";
    public static final String MY_GEOGROUP_NAME_KEY = "name";
    public static final String MY_GEOGROUP_IS_MY_KEY = "is_mine";
    public static final String MY_GEOGROUP_IS_ACTIVE_KEY = "is_active";

    private int _id;
    private String _name;
    private boolean _is_mine;
    private boolean _is_active;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public boolean is_mine() {
        return _is_mine;
    }

    public void set_is_mine(boolean _is_mine) {
        this._is_mine = _is_mine;
    }

    public boolean is_active() {
        return _is_active;
    }

    public void set_is_active(boolean _is_active) {
        this._is_active = _is_active;
    }

    public static String[] GetColumnNamesArray(){
        return new String[]{
                MY_GEOGROUP_ID_KEY,
                MY_GEOGROUP_NAME_KEY,
                MY_GEOGROUP_IS_MY_KEY,
                MY_GEOGROUP_IS_ACTIVE_KEY
        };
    }

    public ContentValues GetContentValuesRow(){
        ContentValues cv = new ContentValues();
        //cv.put(MY_GEOGROUP_ID_KEY, get_id());
        cv.put(MY_GEOGROUP_NAME_KEY, get_name());
        cv.put(MY_GEOGROUP_IS_MY_KEY, is_mine());
        cv.put(MY_GEOGROUP_IS_ACTIVE_KEY, is_active());
        return cv;
    }

    public static ArrayList<MyGeoGroup> GetMyGeoGroupsListFromCursor(Cursor cursor){
        ArrayList<MyGeoGroup> result = new ArrayList<>();
        if(cursor != null && cursor.moveToFirst()){
            do {
                MyGeoGroup group = new MyGeoGroup();
                group.set_id(cursor.getInt(cursor.getColumnIndex(MY_GEOGROUP_ID_KEY)));
                group.set_name(cursor.getString(cursor.getColumnIndex(MY_GEOGROUP_NAME_KEY)));
                group.set_is_mine(cursor.getInt(cursor.getColumnIndex(MY_GEOGROUP_IS_MY_KEY)) == 1);
                group.set_is_active(cursor.getInt(cursor.getColumnIndex(MY_GEOGROUP_IS_ACTIVE_KEY)) == 1);
                result.add(group);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return result;
    }

    private static String GetCreateTableCommandText(){
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(MY_GEOGROUP_TABLE_NAME);
        sb.append(" (");
        sb.append(MY_GEOGROUP_ID_KEY);
        sb.append(" integer primary key AUTOINCREMENT not null, ");
        sb.append(MY_GEOGROUP_NAME_KEY);
        sb.append(" text not null, ");
        sb.append(MY_GEOGROUP_IS_MY_KEY);
        sb.append(" integer not null, ");
        sb.append(MY_GEOGROUP_IS_ACTIVE_KEY);
        sb.append(" integer not null);");
        return sb.toString();
    }

    public static void onCreate(SQLiteDatabase db) { db.execSQL(GetCreateTableCommandText()); }

    public static void onUpgrade(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + MY_GEOGROUP_TABLE_NAME);
        onCreate(db);
    }
}
