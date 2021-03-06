package com.salesianostriana.dam.di.apptalkme.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.salesianostriana.dam.di.apptalkme.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class IniciarActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn_iniciar;
    EditText usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar);getSupportActionBar().hide();
        usuario = (EditText) findViewById(R.id.iniciar_et_nick);
        btn_iniciar = (Button) findViewById(R.id.inicio_btn_iniciar);
        btn_iniciar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(!usuario.getText().toString().isEmpty()){
            new GcmRegistrationAsyncTask(IniciarActivity.this).execute(usuario.getText().toString());
        } else {
            Snackbar.make(v, "Introduce un usuario", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    public class GcmRegistrationAsyncTask extends AsyncTask<String, Void, String> {
        private GoogleCloudMessaging gcm;
        private Context context;
        private String registrationID = "";
        JSONArray response = new JSONArray();
        String nick;

        private static final String SENDER_ID = "447898288023";

        public GcmRegistrationAsyncTask(Context context) {
            this.context = context;
        }


        @Override
        protected String doInBackground(String... params) {
            String msg = "";
            nick = params[0];
            try {
                if (gcm == null){
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                registrationID = gcm.register(SENDER_ID);

                URL url = new URL("http://rest.miguelcr.com/talkme/register?regId=" + registrationID + "&nickname=" + nick);
                sendRegistrationIdToBackend();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {
            SharedPreferences prefs = context.getSharedPreferences("NuevasPreferencias", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("recordar", true);
            editor.putString("nick", nick);
            editor.putString("clave", registrationID);
            Log.i("registro", registrationID);
            editor.commit();
            Log.i("registro", prefs.getString("clave", ""));
            Toast.makeText(IniciarActivity.this, "Registro concluido", Toast.LENGTH_SHORT).show();

            Intent i = null;
            i = new Intent(IniciarActivity.this, TabsActivity.class);
            startActivity(i);
            finish();
        }

        private void sendRegistrationIdToBackend() {
            URL url = null;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("http://miguelcr.hol.es/talkme/register?regId=" + registrationID + "&nickname=" + nick);
                Log.i("registro", url.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                urlConnection = (HttpURLConnection) url.openConnection();

                String responseString = readStream(urlConnection.getInputStream());
                try {
                    response = new JSONArray(responseString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        }
        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuffer response = new StringBuffer();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response.toString();
        }
    }

}
