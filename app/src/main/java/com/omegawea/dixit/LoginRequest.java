package com.omegawea.dixit;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {
    //private static final String LOGIN_REQUEST_URL = "http://192.168.0.105/Login.php";
    private static final String LOGIN_REQUEST_URL = "http://dixitgame.000webhostapp.com/Login.php";
//    private static final String LOGIN_REQUEST_URL = "http://cherrimon.ddns.net:3306/Login.php";
    private Map<String, String> params;

    public LoginRequest(String username, String password, Response.Listener<String> listener) {
        super(Method.POST, LOGIN_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
