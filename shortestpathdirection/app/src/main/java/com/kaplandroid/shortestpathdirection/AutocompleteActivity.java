package com.kaplandroid.shortestpathdirection;

/**
 * Created by rub on 3/7/2018.
 */

        import java.io.UnsupportedEncodingException;
        import java.net.URLEncoder;
        import java.util.ArrayList;
        import org.json.JSONArray;
        import org.json.JSONObject;
        import android.app.Activity;
        import android.graphics.Color;
        import android.os.Bundle;
        import android.text.Editable;
        import android.text.TextWatcher;
        import android.util.Log;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.AutoCompleteTextView;
        import android.widget.TextView;
        import android.widget.Toast;
        import com.android.volley.Request.Method;
        import com.android.volley.Response;
        import com.android.volley.VolleyError;
        import com.android.volley.toolbox.JsonObjectRequest;

public class AutocompleteActivity extends Activity {
    private String LOG_TAG = AutocompleteActivity.class.getSimpleName();
    String url;
    private static final String TAG_RESULT = "predictions";
    JSONObject json;

    AutoCompleteTextView auto_tv;
    ArrayList<String> names;
    ArrayAdapter<String> adapter;
    String browserKey = "AIzaSyBXPMrwbesvaYxbaJTUr_W5Onchs_FZ0yA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_complete);
        auto_tv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        auto_tv.setThreshold(0);

        names = new ArrayList<String>();

        auto_tv.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                if (s.toString().length() <= 3) {
                    names = new ArrayList<String>();
                    Log.d(LOG_TAG, "in OnTextChanged. String: " + s.toString());
                    updateList(s.toString());
                }

            }
        });

    }

    public void updateList(String place) {
        String input = "";
        Log.d(LOG_TAG, "in updateList. place: " + place.toString());
        try {
            input = "input=" + URLEncoder.encode(place, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        String output = "json";
        String parameter = input + "&types=geocode&sensor=true&key="
        + browserKey;

        url = "https://maps.googleapis.com/maps/api/place/autocomplete/"
        + output + "?" + parameter;
        Log.d(LOG_TAG, "in updateList. url: " + url.toString());
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(LOG_TAG, "onResponse. url: " + response.toString());
                try {

                    JSONArray ja = response.getJSONArray(TAG_RESULT);

                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject c = ja.getJSONObject(i);
                        String description = c.getString("description");
                        Log.d(LOG_TAG,"description " + description);
                        names.add(description);
                    }

                    adapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            android.R.layout.simple_list_item_1, names) {

                        @Override
                        public View getView(int position,
                                            View convertView, ViewGroup parent) {
                            View view = super.getView(position,
                                    convertView, parent);
                            TextView text = (TextView) view
                                    .findViewById(android.R.id.text1);
                            Log.d(LOG_TAG,"inGet View " );
                            text.setTextColor(Color.BLACK);
                            return view;
                        }
                    };
                    auto_tv.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        MyApplication.getInstance().addToReqQueue(jsonObjReq, "jreq");
    }

}