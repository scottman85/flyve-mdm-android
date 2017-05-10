package com.teclib.flyvemdm;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.teclib.data.DataStorage;
import com.teclib.services.MQTTService;

public class MainActivity extends AppCompatActivity {

    Intent mServiceIntent;
    private MQTTService mMQTTService;

    private DataStorage cache;

    private EditText edtBroker;
    private EditText edtPort;
    private EditText edtUser;
    private EditText edtPassword;
    private EditText edtTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ------------------
        // MQTT SERVICE
        // ------------------
        mMQTTService = new MQTTService(this);
        mServiceIntent = new Intent(MainActivity.this, mMQTTService.getClass());
        if (!isMyServiceRunning(mMQTTService.getClass())) {
            startService(mServiceIntent);
        }

        cache = new DataStorage(getApplicationContext());

        String mBroker = cache.getVariablePermanente("broker");
        String mPort = cache.getVariablePermanente("port");
        String mUser = cache.getVariablePermanente("agent_id");
        String mPassword = cache.getVariablePermanente("mqttpasswd");
        String mTopic = cache.getVariablePermanente("topic");

        edtBroker = (EditText) findViewById(R.id.edtbroker);
        edtBroker.setText(mBroker);

        edtPort = (EditText) findViewById(R.id.edtport);
        edtPort.setText(mPort);

        edtUser = (EditText) findViewById(R.id.edtUser);
        edtUser.setText(mUser);

        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtPassword.setText(mPassword);

        edtTopic = (EditText) findViewById(R.id.edtTopic);
        edtTopic.setText(mTopic);

        Button btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cache.setVariablePermanente("broker", edtBroker.getText().toString());
                cache.setVariablePermanente("port", edtPort.getText().toString());
                cache.setVariablePermanente("agent_id", edtUser.getText().toString());
                cache.setVariablePermanente("mqttpasswd", edtPassword.getText().toString());
                cache.setVariablePermanente("topic", edtTopic.getText().toString());

                if (!isMyServiceRunning(mMQTTService.getClass())) {
                    startService(mServiceIntent);
                } else {
                    stopService(mServiceIntent);
                    startService(mServiceIntent);
                }
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }


    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }
}
