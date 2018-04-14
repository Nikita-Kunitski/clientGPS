package com.example.uedec.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    TextView coords, hash, tv_encoded;
    SecretKeySpec KEY;
    String LAT,LON,ID;
    StringBuilder STR;
    String encodedMessage;
    Location LOCATION;
    Button send;
    static String SPEC_KEY="qwertyuiopasdfghjklzxcvbnmqwerty";

    private LocationListener listener=new LocationListener(){
        @Override
        public void onLocationChanged(Location location) {
            if(location!=null)
            {
                coords.setText(String.valueOf(location.getLatitude())+" "+String.valueOf(location.getLongitude()));
                LAT=String.valueOf(location.getLatitude());
                LON=String.valueOf(location.getLongitude());
                LOCATION=location;
                try {
                    string_formation(location);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public void string_formation(Location location) throws NoSuchAlgorithmException {

        STR=new StringBuilder();
        String str="time:"+new Date(location.getTime()) +",lat:"+LAT+",lng:"+LON;
        MessageDigest md = MessageDigest.getInstance( "SHA-256" );
        md.update( str.getBytes( StandardCharsets.UTF_8 ) );

        byte[] digest = md.digest();

        String hex = String.format( "%064x", new BigInteger( 1, digest ) );
        hash.setText(hex);
        hex=hex.toUpperCase();
        STR.append(str+",hash:"+hex);

        SecretKeySpec secretKeySpec = new SecretKeySpec(SPEC_KEY.getBytes(), "AES");
        byte[] newIv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
       // KEY=Encoding.generate();
        str=STR.toString();
        byte[] encoded=null;
        try{
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(newIv);
            c.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            encoded = c.doFinal(str.getBytes());
        }catch (Exception e) {
            Log.e("Crypto", "AES encryption error");
        }
        encodedMessage= Base64.encodeToString(encoded,Base64.DEFAULT);
        tv_encoded.setText(encodedMessage);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        STR=new StringBuilder();
       // ID= Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);
        ID="vlad";
        encodedMessage=new String();
        coords=(TextView)findViewById(R.id.cords);
        hash=(TextView)findViewById(R.id.hash);
        tv_encoded=(TextView)findViewById(R.id.encode);
        send=(Button)findViewById(R.id.send);

        LocationManager manager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
            }
        }
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0,listener);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ServerAsync().execute();
            }
        });


    }
    private class ServerAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                new Encoding().sendHTTP(ID,encodedMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
