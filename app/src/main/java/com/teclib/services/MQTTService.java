package com.teclib.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.teclib.data.DataStorage;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

/**
 * Created by rafaelhernandez on 09/05/2016.
 */
public class MQTTService extends Service implements MqttCallback {

    private String TAG = "MQTT";
    private MqttAndroidClient client;
    private DataStorage cache;

    private String mBroker = "";
    private String mPort = "";
    private String mUser = "";
    private String mPassword = "";
    private String mTopic = "";

    public MQTTService(Context applicationContext) {
        super();
        Log.i("START", "SERVICE MQTT");
    }

    public MQTTService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        connect();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("com.teclib.RestartMQTT");
        sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void connect() {

        cache = new DataStorage(this.getApplicationContext());

        mBroker = cache.getVariablePermanente("broker");
        mPort = cache.getVariablePermanente("port");
        mUser = cache.getVariablePermanente("agent_id");
        mPassword = cache.getVariablePermanente("mqttpasswd");
        mTopic = cache.getVariablePermanente("topic");

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://" + mBroker + ":" + mPort,
                clientId);

        client.setCallback( this );

        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setPassword(mPassword.toCharArray());
            options.setUserName(mUser);
            options.setCleanSession(true);
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setConnectionTimeout(0);

            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    //Toast.makeText( getApplicationContext(), "CONNECTION OK!", Toast.LENGTH_SHORT);
                    suscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    //Toast.makeText( getApplicationContext(), "CONNECTION FAIL", Toast.LENGTH_SHORT);
                }
            });
        }
        catch (MqttException ex) {
            ex.printStackTrace();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "Connection fail " + cause.getMessage());
        //Toast.makeText( getApplicationContext(), "Connection fail " + cause.getMessage(), Toast.LENGTH_SHORT);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(TAG, "Message " + message);
        //Toast.makeText( getApplicationContext(), "Message " + message, Toast.LENGTH_SHORT);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "deliveryComplete ");
        //Toast.makeText( getApplicationContext(), "deliveryComplete " + token, Toast.LENGTH_SHORT);
    }

    private void sendPayloadTest() {

        String topic = mTopic;
        String payload = "the payload test from " + mUser;
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            Log.d(TAG, "payload sended");
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR: " + e.getMessage());
        }

    }

    private void suscribe() {

        String topic = mTopic;
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Log.d(TAG, "suscribed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Log.d(TAG, "ERROR: " + exception.getMessage());

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

//    private void createNotification(String message)
//    {
//
//        Intent intent = new Intent(this, MQTTService.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        PendingIntent pendingIntent = PendingIntent.getService(this, 0 /* Request code */, intent,
//                0);
//
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("Flyve MDM")
//                .setContentText(message)
//                .setAutoCancel(true)
//                .setOngoing(true)
//                .setWhen(System.currentTimeMillis())
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(121 , notificationBuilder.build());
//
//    }
}