package osiac.ase.ro.medcom.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import osiac.ase.ro.medcom.R;

public class AboutActivity extends AppCompatActivity {

    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_about);

        text = findViewById(R.id.tvInfo);

        text.setText(R.string.resUsageInfoText);

    }
}
