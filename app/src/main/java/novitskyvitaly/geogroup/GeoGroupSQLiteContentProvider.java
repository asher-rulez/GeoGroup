package novitskyvitaly.geogroup;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import DataModel.LocationReport;
import DataModel.MyGeoGroup;
import DataModel.SQLHelper;

public class GeoGroupSQLiteContentProvider extends ContentProvider {
    private final static String MY_TAG = "geog_sqlProvider";

    private SQLHelper dbHelper;

    private static final int CODE_LOCATION_REPORT = 0;
    private static final int CODE_LOCATION_REPORT_BY_ID = 1;
    private static final int CODE_MY_GEOGROUP = 10;

    private static final String AUTHORITY = "geogroup.sqliteprovider";
    private static final String BASE_PATH = "geogroup";

    private static final String EXT_LOCATION_REPORT = "/locReport";
    private static final String EXT_MY_GEOGROUP = "/geogroup";
    public static final String BASE_STRING_FOR_URI = "content://" + AUTHORITY + "/" + BASE_PATH;

    public static final Uri URI_LOCATION_REPORT
            = Uri.parse(BASE_STRING_FOR_URI + EXT_LOCATION_REPORT);

    public static final Uri URI_MY_GEOGROUP
            = Uri.parse(BASE_STRING_FOR_URI + EXT_MY_GEOGROUP);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_LOCATION_REPORT, CODE_LOCATION_REPORT);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_LOCATION_REPORT + "/#", CODE_LOCATION_REPORT_BY_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_MY_GEOGROUP, CODE_MY_GEOGROUP);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int uriType = sURIMatcher.match(uri);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriType){
            case CODE_LOCATION_REPORT:
                queryBuilder.setTables(LocationReport.LOCATION_REPORT_TABLE_NAME);
                break;
//            case CODE_LOCATION_REPORT_BY_ID:
//                queryBuilder.setTables(LocationReport.LOCATION_REPORT_TABLE_NAME);
//                break;
            case CODE_MY_GEOGROUP:
                queryBuilder.setTables(MyGeoGroup.MY_GEOGROUP_TABLE_NAME);
                break;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = 0;
        switch (uriType){
            case CODE_LOCATION_REPORT:
                id = db.insert(LocationReport.LOCATION_REPORT_TABLE_NAME, null, values);
                break;
            case CODE_MY_GEOGROUP:
                id = db.insert(MyGeoGroup.MY_GEOGROUP_TABLE_NAME, null, values);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(EXT_LOCATION_REPORT + "/" + id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType){
            case CODE_LOCATION_REPORT:
                rowsUpdated = db.update(LocationReport.LOCATION_REPORT_TABLE_NAME, values, selection, selectionArgs);
                break;
            case CODE_MY_GEOGROUP:
                rowsUpdated = db.update(MyGeoGroup.MY_GEOGROUP_TABLE_NAME, values, selection, selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType){
            case CODE_LOCATION_REPORT:
                rowsDeleted = db.delete(LocationReport.LOCATION_REPORT_TABLE_NAME, selection, selectionArgs);
                break;
            case CODE_MY_GEOGROUP:
                rowsDeleted = db.delete(MyGeoGroup.MY_GEOGROUP_TABLE_NAME, selection, selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new SQLHelper(getContext());
        return false;
    }
}
