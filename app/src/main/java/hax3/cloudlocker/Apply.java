package hax3.cloudlocker;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class Apply extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

    }

    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(Apply.this, MainActivity.class);
        intent.putExtra("LOCKERNUM", 0);
        finish();
        startActivity(intent);
    }
}