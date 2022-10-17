package edu.unal.mywebservicesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Spinner mDepartmentSpinner;
    private ArrayList<String> mDepartment;
    private ArrayList<String> mMunicipality;
    private ArrayAdapter<String> mDepartmentAdapter;
    private ListView mListView;
    private ArrayList<String> mData;
    private ArrayAdapter<String> mDataAdapter;
    private Context context = this;
    private String url;
    private RequestQueue mRequestQueue;
    private String stringDepartment;
    private String stringMunicipality;
    private String stringInformation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDepartment = new ArrayList<>();
        mMunicipality = new ArrayList<>();
        mData = new ArrayList<>();
        mRequestQueue = Volley.newRequestQueue(this);
        url = "https://www.datos.gov.co/resource/xdk5-pm3f.json?$select=distinct%20departamento&$order=departamento%20ASC";
        mDepartmentSpinner = (Spinner) findViewById(R.id.spinnerDepartment);
        mListView = findViewById(R.id.listMunicipality);

        mDepartment.clear();
        JsonArrayRequest department = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject tmp = null;
                            try {
                                tmp = response.getJSONObject(i);
                                mDepartment.add(tmp.getString("departamento"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        mDepartmentAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, mDepartment);
                        mDepartmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mDepartmentSpinner.setAdapter(mDepartmentAdapter);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        mDepartmentSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mMunicipality.clear();
                stringDepartment = (String) parent.getItemAtPosition(pos);
                url = "https://www.datos.gov.co/resource/xdk5-pm3f.json?$select=distinct%20municipio&departamento="+ stringDepartment + "&$order=municipio%20ASC";
                JsonArrayRequest municipality = new JsonArrayRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject tmp = null;
                                    try {
                                        tmp = response.getJSONObject(i);
                                        mMunicipality.add(tmp.getString("municipio"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                mData.clear();
                                for(int m = 0; m < mMunicipality.size(); m++){
                                    stringMunicipality = mMunicipality.get(m);
                                    url = "https://www.datos.gov.co/resource/xdk5-pm3f.json?municipio=" + stringMunicipality;
                                    JsonArrayRequest codes = new JsonArrayRequest
                                            (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    for (int i = 0; i < response.length(); i++) {
                                                        JSONObject tmp = null;
                                                        try {
                                                            tmp = response.getJSONObject(i);
                                                            if( tmp.getString("departamento").equals(stringDepartment)) {
                                                                String tmp2 = "Municipio: " + tmp.getString("municipio") + "\n";
                                                                tmp2 += "Departamento: " + tmp.getString("departamento") + "\n";
                                                                tmp2 += "Codigo del municipio: " + tmp.getString("c_digo_dane_del_municipio") + "\n";
                                                                tmp2 += "Codigo del departamento: " + tmp.getString("c_digo_dane_del_departamento") + "\n";
                                                                tmp2 += "Region: " + tmp.getString("region") + "\n";
                                                                mData.add(tmp2);
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    mDataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mData);
                                                    mListView.setAdapter(mDataAdapter);
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Log.e("REQ", "bad");
                                                }
                                            });
                                    mRequestQueue.add(codes);
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                            }
                        });
                mRequestQueue.add(municipality);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });



        mRequestQueue.add(department);
    }
}