package com.omegawea.dixit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.omegawea.dixit.R;

import org.json.JSONException;
import org.json.JSONObject;

import static com.omegawea.dixit.R.id.bSignIn;

public class LoginActivity extends AppCompatActivity {
//    FOR TEST PURPOSE
    private static final String TAG = "LoginActivity";
    private static final String MYSQLERROR = "MySQL server has gone away";
    //MYSQL
    private static JoinRequest joinRequest;
    private static LoginRequest loginRequest;

    //Internal Memory
    private String fnUsername = "Username";
    private String fnPassword = "Password";
    private FileIOStream fileIOStream = new FileIOStream(LoginActivity.this);

    //Layout
    private EditText etUsername;
    private EditText etPassword;
    private TextView tvRegisterLink;
    private Button bLogin;

    //General
    private String language1;
    private String language2;
    private MediaPlayer mpBackground;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        // Layout Parameter
        etUsername = (EditText) findViewById(R.id.etPassword);
        etPassword = (EditText) findViewById(R.id.etUsername);
        tvRegisterLink = (TextView) findViewById(R.id.tvRegisterLink);
        bLogin = (Button) findViewById(bSignIn);

        // Button Interaction
        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (null != activeNetwork) {
                    if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                        Toast.makeText(getApplicationContext(), " connected via WiFi Network", Toast.LENGTH_LONG).show();
                    if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                        Toast.makeText(getApplicationContext(), "connected via Mobile Network", Toast.LENGTH_LONG).show();

                    // Response received from the server
                    Response.Listener<String> loginListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.i(TAG, response);
                                if(response.contains(MYSQLERROR)) {
                                    Log.d(TAG, "OMG!! MYSQLERROR!!!");                      // ignore that message
                                    Toast.makeText(getApplicationContext(), "(Server Error, Please Retry)", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    boolean success = jsonResponse.getBoolean("success");
                                    /*  Member ID is found
                                        Data returns from Server via php,
                                        php data can be retrieved by JSON
                                    * */
                                    if (success) {
                                        language1 = jsonResponse.getString("language1");
                                        language2 = jsonResponse.getString("language2");
                                        //Create Room
                                        // Response received from the server
                                        Response.Listener<String> joinListener = new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                try {
                                                    JSONObject jsonResponse = new JSONObject(response);
                                                    boolean success = jsonResponse.getBoolean("success");
                                                    if (success) {
                                                        // Save to Internal Memory
                                                        fileIOStream.InputStream(fnUsername, username);
                                                        fileIOStream.InputStream(fnPassword, password);
                                                        //int room = jsonResponse.optInt("room", -1);
                                                        final int room = jsonResponse.optInt("room", -1);
                                                        //  Prepare Data transmit to next Activity (UserAreaActivity.java)
//                                            mpBackground.stop();
                                                        Intent intent = new Intent(LoginActivity.this, UserAreaActivity.class);
                                                        intent.putExtra("username", username);
                                                        intent.putExtra("room", room);
                                                        LoginActivity.this.startActivity(intent);

                                                    } else {
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                                        builder.setMessage("Join Failed")
                                                                .setNegativeButton("Retry", null)
                                                                .create()
                                                                .show();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        };
                                        joinRequest = new JoinRequest(username, language1, language2, joinListener);
                                        SingletonRequest.getInstance(LoginActivity.this).addToRequestQueue(joinRequest);
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                        builder.setMessage("Login Failed")
                                                .setNegativeButton("Retry", null)
                                                .create()
                                                .show();
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    loginRequest = new LoginRequest(username, password, loginListener);
                    SingletonRequest.getInstance(LoginActivity.this).addToRequestQueue(loginRequest);

                }
                else
                    Toast.makeText(getApplicationContext(), "No internet Connectivity", Toast.LENGTH_LONG).show();


            }
        });
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPause() {
        super.onPause();
//        Log.i(TAG, "onPause");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.i(TAG, "onStop");
//        mpBackground.pause();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onRestart() {
        super.onRestart();
//        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.i(TAG, "onStart");
//        mpBackground.start();
        // Read ID PW from internal memory
        etUsername.setText(fileIOStream.OutputStream(fnUsername));
        etPassword.setText(fileIOStream.OutputStream(fnPassword));
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.i(TAG, "onResume");
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        Log.i(TAG, "onRestoreInstanceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.i(TAG, "onDestroy");
    }
}
