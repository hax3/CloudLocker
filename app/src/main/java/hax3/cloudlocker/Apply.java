package hax3.cloudlocker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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


public class Apply extends Activity {

    private String url = "http://ec2-52-39-206-204.us-west-2.compute.amazonaws.com/getlocker.php";
    private String myJSON;
    private ArrayList<HashMap<String, String>> lockerList;
    private JSONArray lockers;
    private ImageButton locker_01;
    private ImageButton locker_02;
    private ArrayList<ImageButton> buttonList;

    boolean flag;
    String userId;
    int applyNum;
    TextView selected;

    Button apply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);
        lockerList = new ArrayList<HashMap<String, String>>();
        locker_01 = (ImageButton) findViewById(R.id.locker_01);
        locker_02 = (ImageButton) findViewById(R.id.locker_02);
        buttonList = new ArrayList<ImageButton>(lockerList.size());
        buttonList.add(locker_01);
        buttonList.add(locker_02);
        selected = (TextView) findViewById(R.id.selected_locker);

        getUserId();
        getData(url);
        flag = false;
        apply = (Button) findViewById(R.id.apply_button);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag) {
                    sendApply();
                } else {
                    Toast.makeText(Apply.this, "사물함을 선택하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        for (int i = 0; i < lockerList.size(); i++) {
            if (!lockerList.get(i).get("usercount").equals("0")) {
            } else {
                buttonList.get(i).setOnClickListener(new OnClick_null(i + 1));
            }
        }
    }

    protected void showLocker() {
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            lockers = jsonObj.getJSONArray("result");
            for (int i = 0; i < lockers.length(); i++) {
                JSONObject c = lockers.getJSONObject(i);
                String id = c.getString("id");
                String limit = c.getString("timelimit");
                String usercount = c.getString("usercount");

                HashMap<String, String> lockers = new HashMap<String, String>();
                lockers.put("id", id);
                lockers.put("limit", limit);
                lockers.put("usercount", usercount);
                lockerList.add(lockers);
            }
            /*사물함 버튼 만들기*/
            for (int i = 0; i < lockerList.size(); i++) {
                if (!lockerList.get(i).get("usercount").equals("0")) {
                    if (i % 2 == 0) {
                        buttonList.get(i).setImageResource(R.drawable.locked_white);
                    } else {
                        buttonList.get(i).setImageResource(R.drawable.locked_black);
                    }
                    buttonList.get(i).setOnClickListener(new OnClick_used(lockerList.get(i).get("limit")));
                } else {
                    buttonList.get(i).setOnClickListener(new OnClick_null(i + 1));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getData(String url) {
        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                BufferedReader bufferedReader = null;
                StringBuilder sb = new StringBuilder();
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    if (con != null) {
                        con.setConnectTimeout(10000);
                        con.setUseCaches(false);
                        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                            for (; ; ) {
                                // 웹상에 보여지는 텍스트를 라인단위로 읽어 저장.
                                String line = br.readLine();
                                if (line == null) break;
                                // 저장된 텍스트 라인을 jsonHtml에 붙여넣음
                                sb.append(line + "\n");
                            }
                            br.close();
                        }
                        con.disconnect();
                    }
                } catch (Exception e) {
                    return null;
                }
                return sb.toString();
            }

            @Override
            protected void onPostExecute(String result) {
                myJSON = result;
                showLocker();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }

    public class OnClick_used implements View.OnClickListener {

        String limit;

        OnClick_used(String limit) {
            this.limit = limit;
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(Apply.this, "해당 사물함은 " + limit + "일 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public class OnClick_null implements View.OnClickListener {
        int i;

        OnClick_null(int i) {
            this.i = i;
        }

        @Override
        public void onClick(View v) {
            selected.setText(Integer.toString(i));
            applyNum = i;
            flag = true;
        }
    }

    private void sendApply() {
        insertToDatabase(Integer.toString(applyNum), Integer.toString(30));
    }


    private void insertToDatabase(String id, String limit) {

        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(Apply.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(), "등록 완료", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Apply.this, MainActivity.class);
                intent.putExtra("USERID", userId);
                startActivity(intent);
                finish();
            }

            @Override
            protected String doInBackground(String... params) {
                String id = (String) params[0];
                String limit = (String) params[1];

                InputStream is = null;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("userid", userId));
                nameValuePairs.add(new BasicNameValuePair("lockerid", id));
                nameValuePairs.add(new BasicNameValuePair("limit", limit));
                String result = null;

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://ec2-52-39-206-204.us-west-2.compute.amazonaws.com/apply.php");
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
        task.execute(id, limit);
    }

    void getUserId(){
        Intent intent = getIntent();
        userId = (String) intent.getSerializableExtra("USERID");
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Apply.this, MainActivity.class);
        intent.putExtra("USERID", userId);
        startActivity(intent);
        finish();
    }
}
