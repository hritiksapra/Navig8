package com.example.disaster3;

import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class TransferActivity extends ConnectionsActivity {

    private ConnectionsClient mConnectionsClient;
    Set<ConnectionsActivity.Endpoint> endIDs = ConnectionsActivity.getConnectedEndpoints();
    private String mName;
    private static final String SERVICE_ID =
            "com.google.location.nearby.apps.walkietalkie.manual.SERVICE_ID";

    private static final Strategy STRATEGY = Strategy.P2P_STAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        Intent intent = getIntent();
        String path = intent.getStringExtra("directory");

        mConnectionsClient = Nearby.getConnectionsClient(this);
        mName = generateRandomName();
        if (path != null)
            sendVideoFile(path);
        Intent intent1 = new Intent(this, MainActivity.class);
        startActivity(intent1);
    }

    private void sendVideoFile(String path) {

        File fileToSend = new File(path);

        try {
            Payload filePayload = Payload.fromFile(fileToSend);
            send(filePayload);
         } catch (FileNotFoundException e) {
            Log.e("MyApp", "File not found", e);
        }

    }

    /** Callbacks for payloads (bytes of data) sent from another device to us. */
    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {

                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                }
            };


    @Override
    protected String getName() {
        return mName;
    }

    @Override
    protected String getServiceId() {
        return SERVICE_ID;
    }

    @Override
    protected Strategy getStrategy() {
        return STRATEGY;
    }

    private static String generateRandomName() {
        String name = "";
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            name += random.nextInt(10);
        }
        return name;
    }

    /** {@see ConnectionsActivity#onReceive(Endpoint, Payload)} */
    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
        if (payload.getType() == Payload.Type.BYTES) {
            Toast.makeText(
                    this, "Received File From Distressed, Please Check Downloads Folder", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        // We found an advertiser!
        if (!isConnecting()) {
            connectToEndpoint(endpoint);
        }
    }


    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        // A connection to another device has been initiated! We'll accept the connection immediately.
        acceptConnection(endpoint);
    }

    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_connected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
    }
    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_disconnected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();

        // If we lost all our endpoints, then we should reset the state of our app and go back
        // to our initial state (discovering).
        if (getConnectedEndpoints().isEmpty()) {
        }
    }

}
