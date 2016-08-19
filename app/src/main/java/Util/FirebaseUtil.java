package Util;

import android.content.Context;

import com.firebase.client.Firebase;

import novitskyvitaly.geogroup.R;

/**
 * Created by Asher on 16.08.2016.
 */
public class FirebaseUtil {

    public static Firebase GetFirebaseGroupsReference(Context ctx){
        return new Firebase(ctx.getString(R.string.firebase_connection_string)
                + "/" + ctx.getString(R.string.firebase_fieldname_groups));
    }

    public static Firebase GetFirebaseSingleGroupReference(Context ctx, String groupID){
        return GetFirebaseGroupsReference(ctx).child(groupID);
    }

    public static Firebase GetFirebaseGroupCommonEventsReference(Context ctx, String groupID){
        return GetFirebaseSingleGroupReference(ctx, groupID).child(ctx.getString(R.string.firebase_fieldname_common_events));
    }

    public static Firebase GetFirebaseUsersOfGroupReference(Context ctx, String groupID){
        return GetFirebaseGroupsReference(ctx).child(groupID).child(ctx.getString(R.string.firebase_fieldname_users));
    }

    public static Firebase GetFirebaseLocationReportsByGroupAndUserReference(Context ctx, String groupID, String userID){
        return GetFirebaseUsersOfGroupReference(ctx, groupID).child(userID).child(ctx.getString(R.string.firebase_fieldname_reports));
    }

    public static Firebase GetFirebaseStatusUpdateByGroupAndUserReference(Context ctx, String groupID, String userID){
        return GetFirebaseUsersOfGroupReference(ctx, groupID).child(userID).child(ctx.getString(R.string.firebase_fieldname_status_updates));
    }

    public static Firebase GetFirebaseUsersReference(Context ctx){
        return new Firebase(ctx.getString(R.string.firebase_connection_string)
                + "/" + ctx.getString(R.string.firebase_fieldname_users));
    }

    public static Firebase GetFirebaseUserReference(Context ctx, String userID){
        return GetFirebaseUsersReference(ctx).child(userID);
    }
}
