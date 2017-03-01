package com.omegawea.dixit;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SF on 2/14/2017.
 */

public class RWRequest extends StringRequest {
    //private static final String CREATE_REQUEST_URL = "http://192.168.0.105/";
    private static final String CREATE_REQUEST_URL = "http://dixitgame.000webhostapp.com/RW.php";
    private Map<String, String> params;

    public RWRequest(String name, int room, String story, int face, int guess, Response.Listener<String> listener) {
        super(Request.Method.POST, CREATE_REQUEST_URL, listener, null);
        if(story == null)           // NULL Control
            story = "";
        params = new HashMap<>();
        params.put("name", name);
        //if(room != 0)
        params.put("room", room + "");
        params.put("story", story);
        //if(face != 0)
        params.put("face", face + "");
        //if(guess != 0)
        params.put("guess", guess + "");
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}