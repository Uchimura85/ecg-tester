package ecg.android.tool.ecg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import no.nordicsemi.android.nrftoolbox.R;

public class MenuActivity extends AppCompatActivity {

    public Button captureButton;
    public Button analyzeButton;
    public Button mitbitButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        captureButton = (Button)findViewById(R.id.captureButton);
//        analyzeButton = (Button)findViewById(R.id.analyzeButton);
        mitbitButton = (Button)findViewById(R.id.mitbitButton);

        captureButton.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 final Intent newIntent = new Intent(MenuActivity.this,ECGActivity.class);
                                                 newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                 startActivity(newIntent);
                                                      }
                                         }

        );

//        analyzeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final Intent newIntent = new Intent(MenuActivity.this,AnalyzeActivity.class);
//                newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                startActivity(newIntent);
//            }
//        });

        mitbitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent newIntent = new Intent(MenuActivity.this,MitBitActivity.class);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(newIntent);
            }
        });
     }
}
