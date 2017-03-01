package com.omegawea.dixit;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {
//    private static final String REGISTER_REQUEST_URL = "http://192.168.0.105/Register.php";
    private static final String REGISTER_REQUEST_URL = "http://dixitgame.000webhostapp.com/Register.php";
    private Map<String, String> params;

    public RegisterRequest(String username, String password, String language1, String language2, Response.Listener<String> listener) {
        super(Method.POST, REGISTER_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        params.put("language1", language1);
        params.put("language2", language2);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
