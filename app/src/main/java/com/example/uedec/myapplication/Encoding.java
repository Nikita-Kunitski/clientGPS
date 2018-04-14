package com.example.uedec.myapplication;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by uedec on 04.04.2018.
 */

public class Encoding {
    public static SecretKeySpec generate() throws NoSuchAlgorithmException {
        SecretKeySpec sks = null;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed("any data used as random seed".getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(256, sr);
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
        } catch (Exception e) {
            Log.e("KEY GEN", "AES secret key spec error");
        }
        return sks;
    }

    public static boolean sendHTTP(String ID, String encodedMessage) throws IOException {

        if(!encodedMessage.equals(""))
        {
        URL url=new URL("https://secure-waters-60346.herokuapp.com/api/stuff?id="+ID+"&encdata="+encodedMessage);
        HttpURLConnection con=(HttpURLConnection)url.openConnection();
        if(con.getResponseCode()==HttpURLConnection.HTTP_OK)
        {
            return true;
        }
        else{
          return false;
        }
        }
        else {
            return false;
        }

    }
}
