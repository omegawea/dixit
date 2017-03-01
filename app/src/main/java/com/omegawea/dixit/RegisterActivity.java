package com.omegawea.dixit;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.Response;
import com.omegawea.dixit.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;


public class RegisterActivity extends AppCompatActivity {
    //FOR TEST PURPOSE
    private static final String TAG = "RegisterActivity";
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("message");

    String fnUsername = "Username";
    String fnPassword = "Password";
    FileIOStream fileIOStream = new FileIOStream(RegisterActivity.this);

    private static RegisterRequest registerRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final Spinner spLanguage1 = (Spinner) findViewById(R.id.spLanguage1);
        final Spinner spLanguage2 = (Spinner) findViewById(R.id.spLanguage2);
        final Button bRegister = (Button) findViewById(R.id.bRegister);

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();
                final String language1 = spLanguage1.getSelectedItem().toString();
                final String language2 = spLanguage2.getSelectedItem().toString();
                Response.Listener<String> registerListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {

                                // Save to Internal Memory
                                try {
                                    fileIOStream.InputStream(fnUsername, username);
                                    fileIOStream.InputStream(fnPassword, password);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                RegisterActivity.this.startActivity(intent);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                builder.setMessage("Register Failed")
                                        .setNegativeButton("Retry", null)
                                        .create()
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                registerRequest = new RegisterRequest(username, password, language1, language2, registerListener);
                SingletonRequest.getInstance(RegisterActivity.this).addToRequestQueue(registerRequest);
            }
        });
    }
}