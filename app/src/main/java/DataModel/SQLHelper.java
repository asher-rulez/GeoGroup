package DataModel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Asher on 09.08.2016.
 */
public class SQLHelper extends SQLiteOpenHelper {
    private static final String MY_TAG = "geog_sqlHelper";

    public static final String GEOG_DB_NAME = "GeoGroup.db";
    public static final int GEOG_DB_VERSION = 1;

    public SQLHelper(Context context) {
        super(context, GEOG_DB_NAME, null, GEOG_DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        LocationReport.onCreate(sqLiteDatabase);
        MyGeoGroup.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        LocationReport.onUpgrade(sqLiteDatabase);
        MyGeoGroup.onUpgrade(sqLiteDatabase);
    }
}
