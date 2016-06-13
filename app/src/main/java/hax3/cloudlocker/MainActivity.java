package hax3.cloudlocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity {
    private String url = "http://ec2-52-39-206-204.us-west-2.compute.amazonaws.com/getdata.php";
    private String myJSON;
    private ImageButton apply;
    private ImageButton turnIn;
    private ImageButton share;
    private ImageButton logout;
    private ToggleButton lockerButton;
    private TextView lockerNumView;
    private int lockerNum;
    private JSONArray results;
    private String userId;
    private BackPressCloseHandler backPressCloseHandler;

    SharedPreferences setting;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apply = (ImageButton) findViewById(R.id.apply_button);
        share = (ImageButton) findViewById(R.id.share_button);
        turnIn = (ImageButton) findViewById(R.id.return_button);
        logout = (ImageButton) findViewById(R.id.logout_button);
        lockerButton = (ToggleButton) findViewById(R.id.ButtonLocker);
        lockerNumView = (TextView) findViewById(R.id.LockerNum);

        backPressCloseHandler = new BackPressCloseHandler(this);

        setting = getSharedPreferences("setting", 0);
        editor= setting.edit();

        Intent intent = getIntent();
        userId = (String) intent.getSerializableExtra("USERID");

        onResume();

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lockerNum == 0) {
                    Intent intent = new Intent(MainActivity.this, Apply.class);
                    intent.putExtra("USERID", userId);
                    startActivity(intent);
                    finish();
                }
                Toast.makeText(MainActivity.this, "You are already using a Locker", Toast.LENGTH_SHORT);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lockerNum != 0) {
                    Intent intent = new Intent(MainActivity.this, Share.class);
                    intent.putExtra("USERID", userId);
                    startActivity(intent);
                    finish();
                }
                Toast.makeText(MainActivity.this, "you are using no locker", Toast.LENGTH_SHORT);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
                alert_confirm.setMessage("진정 로그아웃 하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                Toast.makeText(MainActivity.this, "안녕히가세영", Toast.LENGTH_SHORT).show();
                                editor.remove("ID");
                                editor.remove("PW");
                                editor.remove("Auto_Login_enabled");
                                editor.clear();
                                editor.commit();
                                Intent intent = new Intent(MainActivity.this, Login.class);
                                startActivity(intent);
                                finish();
                            }
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                                Toast.makeText(MainActivity.this, "canceled", Toast.LENGTH_SHORT).show();
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();
            }
        });

        lockerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lockerButton.isChecked()) {
                    Toast.makeText(MainActivity.this, "Locked", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "UnLocked", Toast.LENGTH_SHORT).show();
                }
            }
        });

        turnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lockerNum != 0) {
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
                    alert_confirm.setMessage(lockerNum + "번 사물함을 반납 하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'YES'
                                    Toast.makeText(MainActivity.this, "반납완료", Toast.LENGTH_SHORT).show();
                                    turnInLocker();
                                    onResume();
                                }
                            }).setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'No'
                                    Toast.makeText(MainActivity.this, "canceled", Toast.LENGTH_SHORT).show();

                                }
                            });
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                } else {
                    Toast.makeText(MainActivity.this, "you are using no locker", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getLockerNum(String url) {
        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                InputStream is = null;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("userid", userId));

                String result = null;
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(uri);
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    is = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }
            @Override
            protected void onPostExecute(String result) {
                myJSON = result;
                try {
                    JSONObject jsonObj = new JSONObject(myJSON);
                    results = jsonObj.getJSONArray("result");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject c = results.getJSONObject(i);
                        lockerNum = c.getInt("lockerNum");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                lockerNumView.setText("your Locker is : " + lockerNum);
                if (lockerNum == 0) {
                    lockerButton.setVisibility(View.INVISIBLE);
                }else{
                    lockerButton.setVisibility(View.VISIBLE);
                }
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }

    private void turnInLocker() {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
            }

            @Override
            protected String doInBackground(String... params) {
                InputStream is = null;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("userid", userId));
                nameValuePairs.add(new BasicNameValuePair("lockerid", Integer.toString(lockerNum)));
                nameValuePairs.add(new BasicNameValuePair("limit", "0"));
                String result = null;

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://ec2-52-39-206-204.us-west-2.compute.amazonaws.com/turnin.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    is = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }
        }
        InsertData task = new InsertData();
        task.execute();
    }


    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLockerNum(url);
    }
}
