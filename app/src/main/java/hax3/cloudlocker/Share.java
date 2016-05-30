package hax3.cloudlocker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Share extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
    }

    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(Share.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
