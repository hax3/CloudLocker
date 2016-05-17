package hax3.cloudlocker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {

    ImageView apply;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apply = (ImageView) findViewById(R.id.applyButton);

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Apply.class);
                finish();
                startActivity(intent);
            }
        });
    }
}
