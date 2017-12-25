package schemax.iot;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class Network_connection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_connection);

        String xx = String.valueOf(isNetworkAvailable());
        Log.d("connected ",xx);

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /*
    public void timecondition() {
        Calendar cc = Calendar.getInstance();
        cc.add(Calendar.MINUTE, 0);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        String Datetime = df.format(cc.getTime());
        Date current_time = null;
        try {
            current_time = df.parse(Datetime);

            Log.d("datetime", String.valueOf(current_time.getTime()));

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            Date d2 = format.parse("03/12/2017 10:05 AM ");
            Double hoursDifference = Double.valueOf((current_time.getTime() - d2.getTime()) / 60000);
            //int hoursDifference = (Double)((current_time.getTime() - d2.getTime()) / 3600000L);
            Log.d("difference in time", String.valueOf(hoursDifference));
            if (hoursDifference < 0) {
                Log.d("date2 greater", "date2");
            } else {
                Log.d("date1 greater", "date1");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
*/

}
