package scal.io.liger.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import scal.io.liger.MainActivity;
import scal.io.liger.sample.R;

public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("lang", "en");
        i.putExtra("photo_essay_slide_duration", 5000);
        startActivity(i);
    }
}
