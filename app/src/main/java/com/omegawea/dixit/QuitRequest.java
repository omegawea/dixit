package com.omegawea.dixit;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class QuitRequest extends StringRequest {
//    private static final String CREATE_REQUEST_URL = "http://192.168.0.105/Quit.php";
    private static final String CREATE_REQUEST_URL = "http://dixitgame.000webhostapp.com/Quit.php";
    private Map<String, String> params;

    public QuitRequest(String host, int room, Response.Listener<String> listener) {
        super(Method.POST, CREATE_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("host", host);
        params.put("room", room + "");
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
