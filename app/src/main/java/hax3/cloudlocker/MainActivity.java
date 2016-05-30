package hax3.cloudlocker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MainActivity extends Activity {

    private ImageButton apply;
    private ImageButton turnIn;
    private ImageButton share;
    private ImageButton option;
    private ToggleButton lockerButton;
    private TextView lockerNumView;
    private int lockerNum;
    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apply = (ImageButton) findViewById(R.id.apply_button);
        share = (ImageButton) findViewById(R.id.share_button);
        turnIn = (ImageButton) findViewById(R.id.return_button);
        option = (ImageButton) findViewById(R.id.setting_button);
        lockerButton = (ToggleButton) findViewById(R.id.ButtonLocker);
        lockerNumView = (TextView) findViewById(R.id.LockerNum);
        backPressCloseHandler = new BackPressCloseHandler(this);

        getLockerNum();

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lockerNum==0){
                    Intent intent = new Intent(MainActivity.this, Apply.class);
                    startActivity(intent);
                    finish();
                }
                Toast.makeText(MainActivity.this, "You are already using a Locker", Toast.LENGTH_SHORT);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lockerNum!=0){
                    Intent intent = new Intent(MainActivity.this, Share.class);
                    finish();
                    startActivity(intent);
                }
                Toast.makeText(MainActivity.this, "you are using no locker", Toast.LENGTH_SHORT);
            }
        });
        lockerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lockerButton.isChecked()){
                    Toast.makeText(MainActivity.this , "Locked", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this , "UnLocked", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getLockerNum(){
        Intent intent = getIntent();
        lockerNum = (int) intent.getSerializableExtra("LOCKERNUM");
        lockerNumView.setText("your Locker is : " + lockerNum);
    }
    public void onBackPressed(){
        backPressCloseHandler.onBackPressed();
    }
}
