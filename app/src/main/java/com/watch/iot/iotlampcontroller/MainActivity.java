package com.watch.iot.iotlampcontroller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;



public class MainActivity extends WearableActivity
        implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{


    private BoxInsetLayout mContainerView;

    public static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private static final String COUNT_KEY = "com.example.key.count";
    private int count = 0;
    private int id = -1;
    //public DataLayerListenerService mService;
    private boolean mBound = false;
//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        super.onSaveInstanceState(savedInstanceState);
//
//        if (savedInstanceState == null) {
//            Log.d("Intent","savedInstanceState == null");
//            Bundle extras = getIntent().getExtras();
//            if(extras == null) {
//                Log.d("Intent","idextras == null");
//            } else {
//                Log.d("Intent","savedInstanceState != null");
//                id= extras.getInt("id");
//            }
//        } else {
//            Log.d("Intent","savedInstanceState != null");
//            id= (int) savedInstanceState.getInt("id");
//        }
//
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        ImageView img = (ImageView) findViewById(R.id.big_lightbulb);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                increaseCounter();
//                increaseCounter();
                toggleLampLight();

            }
        });



//        Google API klient som behövs för att skicka data till det speciella molnet.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

//        Intent i = new Intent(MainActivity.this, DataLayerListenerService.class);
////        Bundle b = new Bundle();
////        Integer integer = new Integer(0);
////        b.putSerializable("int", integer);
////        i.putExtras(b);
//        ServiceConnection mServiceConn = new ServiceConnection() {
//
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                // We've bound to LocalService, cast the IBinder and get LocalService instance
//                DataLayerListenerService.LocalBinder binder = (DataLayerListenerService.LocalBinder) service;
//                mService = binder.getService();
//                mBound = true;
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//                mBound = false;
//            }
//        };
//
//        this.bindService(i,mServiceConn, Context.BIND_AUTO_CREATE);
//        mService.increaseCounter();


        Intent intent= getIntent();
        Bundle b = intent.getExtras();

        if(b!=null){
            id =(int) b.get("id");

        }
        TextView tv = (TextView) findViewById(R.id.text);
        switch (id){
            case 0: tv.setText("Lampa golv");
            case 1: tv.setText("lampa fönster");

        }

        //se till att lampans bild reflekterar nuvarande status.
    }
    //Ändrar lampa med det aktiva id:t till på/av
    private void toggleLampLight(){
        //ändra count så att den passar nya värdet i 0..3
        //denna 2^id get oss binära platsen(2 för id = 1 och 1 för id=0) och ^ gör xor på count för att flippa den biten
        int tempCount = count^((int) Math.pow(2,id));


        //skicka detta värdet till molnet
        sendLampState(tempCount);
        //uppdatera lampan
        //bitwise and är == 0 om lampan skall vara av;
        iconPwrOn((tempCount & ((int) Math.pow(2,id))) != 0);




    }


private void sendLampState(int tempCount) {
    PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count");
    putDataMapReq.setUrgent();
    putDataMapReq.getDataMap().putInt(COUNT_KEY, tempCount);
    PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
    PendingResult<DataApi.DataItemResult> pendingResult =
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
}

//    private void increaseCounter() {
//        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count");
//        putDataMapReq.setUrgent();
//        putDataMapReq.getDataMap().putInt(COUNT_KEY, count++);
//        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
//        PendingResult<DataApi.DataItemResult> pendingResult =
//                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
//    }
    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }




    @Override
    public void onExitAmbient() {
        updateDisplay();
        mGoogleApiClient.connect();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));

        } else {
            mContainerView.setBackground(null);

        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                if (dataItems.getCount() != 0) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(0));

                    // This should read the correct value.
                    int value = dataMapItem.getDataMap().getInt(COUNT_KEY);
                    updateCount(value);
                    //Toast.makeText(MainActivity.this, "hämtat värde:" + count, Toast.LENGTH_SHORT).show();
                }

                dataItems.release();
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/count") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    updateCount(dataMap.getInt(COUNT_KEY));
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    //method to update count
    private void updateCount(int c){
        count = c;
//        TextView tv = (TextView) findViewById(R.id.text);
//        tv.setText(""+count);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void iconPwrOn(boolean b) {
        ImageView img = (ImageView) findViewById(R.id.big_lightbulb);
        if (b == true) img.setImageResource(R.drawable.big_lightbulb_on);
        else img.setImageResource(R.drawable.big_lightbulb_off);
    }


}
