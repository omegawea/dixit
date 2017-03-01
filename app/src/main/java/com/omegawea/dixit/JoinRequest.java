package com.omegawea.dixit;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class JoinRequest extends StringRequest {
    //private static final String CREATE_REQUEST_URL = "http://192.168.0.105/Join.php";
    private static final String CREATE_REQUEST_URL = "http://dixitgame.000webhostapp.com/Join.php";
    private Map<String, String> params;

    public JoinRequest(String host, String language1, String language2, Response.Listener<String> listener) {
        super(Method.POST, CREATE_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("host", host);
        params.put("language1", language1);
        params.put("language2", language2);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
