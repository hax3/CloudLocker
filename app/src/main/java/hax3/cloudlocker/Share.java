package hax3.cloudlocker;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.List;

public class Share extends AppCompatActivity {


    String userId;
    String friendId;
    EditText editTextFriendId;
    Button shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Intent intent = getIntent();
        userId = (String) intent.getSerializableExtra("USERID");
        shareButton = (Button) findViewById(R.id.button_share);
        editTextFriendId = (EditText) findViewById(R.id.friendId);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendId = editTextFriendId.getText().toString();
                share(friendId);
            }
        });
    }
    private void share(final String userid) {

        class ShareAsync extends AsyncTask<String, Void, String> {

            private Dialog loadingDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loadingDialog = ProgressDialog.show(Share.this, "Please wait", "Loading...");
            }

            @Override
            protected String doInBackground(String... params) {
                String id = params[0];

                InputStream is = null;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("userid", id));
                String result = null;

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://ec2-52-39-206-204.us-west-2.compute.amazonaws.com/share.php");
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
                String k = result.trim();
                String s = "fail";
                loadingDialog.dismiss();
                try {
                    JSONObject jsonObj = new JSONObject(k);
                    JSONArray data = jsonObj.getJSONArray("result");
                    JSONObject rsult = data.getJSONObject(0);
                    s = rsult.getString("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (s.equalsIgnoreCase("success")) {
                    Intent intent = new Intent(Share.this, MainActivity.class);
                    intent.putExtra("USERID", userid);
                    finish();
                    startActivity(intent);
                } else if(s.equalsIgnoreCase("already")) {
                    Toast.makeText(getApplicationContext(), "이미 사물함을 이용중인 사용자입니다.", Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(getApplicationContext(), "사용자가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        ShareAsync la = new ShareAsync();
        la.execute(userid);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Share.this, MainActivity.class);
        intent.putExtra("USERID", userId);
        startActivity(intent);
        finish();
    }
}
