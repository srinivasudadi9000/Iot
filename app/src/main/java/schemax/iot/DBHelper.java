package schemax.iot;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;

import schemax.iot.model.Weather;

/**
 * Created by srinivas on 01/12/17.
 */

public class DBHelper {

    static SQLiteDatabase db;
    static Context context;

    public DBHelper(List<Weather> voices, Context context) {
        Log.d("size of voice", String.valueOf(voices.size()));
        this.context = context;
        db = context.openOrCreateDatabase("IOT_schemax", Context.MODE_PRIVATE, null);
       // db.delete("myvoice", null,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS myvoice(voice_id VARCHAR unique,voice_des VARCHAR,voice_date VARCHAR,voice_time VARCHAR,team VARCHAR,flag VARCHAR);");
        for (int i = 0; i < voices.size(); i++) {
            if (!db.isOpen()){
                db = context.openOrCreateDatabase("IOT_schemax", Context.MODE_PRIVATE, null);
            }
            Log.d("datainsert", i + " " + voices.get(i).getDate());
            if (validaterecord(voices.get(i).getId()).equals("notvalidate")){
                db.execSQL("INSERT INTO myvoice VALUES('" + voices.get(i).getId() + "','" + voices.get(i).getVoice_des().replaceAll(",", "")
                        + "','" + voices.get(i).getDate() + "','" + voices.get(i).getTime() + "','" + voices.get(i).getTeam_members().replaceAll(",", "") + "','" + voices.get(i).getFlag() + "');");
                db.close();
            }else {
                updatevoice_Localdb(voices.get(i).getVoice_des(),voices.get(i).getDate(),voices.get(i).getTime(),
                        voices.get(i).getTeam_members(),voices.get(i).getFlag(),voices.get(i).getId(),context,i,voices.size()-1);
            }
        }
       // db.close();
        //viewmydb();
    }

    public String validaterecord(String voice_id) {
        Cursor c = db.rawQuery("SELECT * FROM myvoice WHERE voice_id='" + voice_id + "'", null);
        if (c.moveToFirst()) {
             return "validate";
        } else {
             return "notvalidate";
        }
    }

    public static void updatevoice_Localdb(String voice_des, String voice_date, String voice_time, String team, String flag,
                                             String voice_id , Context mycontext,int i , int size) {


        db.execSQL("UPDATE myvoice SET voice_des='" + voice_des.replaceAll(",", "") + "',voice_date='" + voice_date + "',voice_time='" + voice_time + "',team='" + team.replaceAll(",", "")
                + "',flag='" + flag +"'"+
                 " WHERE voice_id=" + voice_id);

        db.close();

       // db.close();
        Log.d("success", "successfully updated recce");
    }
}
