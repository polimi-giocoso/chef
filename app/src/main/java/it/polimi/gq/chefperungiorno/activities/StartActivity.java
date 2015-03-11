package it.polimi.gq.chefperungiorno.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import it.polimi.gq.chefperungiorno.R;
import it.polimi.gq.chefperungiorno.model.Game;
import it.polimi.gq.chefperungiorno.model.TurnResult;
import it.polimi.gq.chefperungiorno.utils.Commons;
import it.polimi.gq.chefperungiorno.utils.PrefUtils;

public class StartActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        System.out.println("Create");
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_start);
        String s = loadJSONFromAsset(R.raw.data);
        Game.loadData(s);

        String mail=PrefUtils.getFromPrefs(this, PrefUtils.PREFS_MAIL_KEY, null);
        TextView textView = (TextView) this.findViewById(R.id.mail_text_view);
        SpannableString content = null;


        if(mail==null) {
            content=new SpannableString(getResources().getString(R.string.default_mail));
            showAlertForEmailPref();
        }
        else{
            content=new SpannableString(mail);
        }

        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        textView.setText(content);

    }

    protected void onStart(){
        super.onStart();

    }

    protected void onDestroy(){
        super.onDestroy();
    }

    protected void showAlertForEmailPref(){
        final Activity self = this;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.insertEmail);
        alert.setMessage(R.string.whyEmail);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        alert.setView(input);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                PrefUtils.saveToPrefs(self, PrefUtils.PREFS_MAIL_KEY, value);
                TextView textView = (TextView) self.findViewById(R.id.mail_text_view);
                SpannableString content = new SpannableString(value);
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                textView.setText(content);
            }
        });

        alert.setNegativeButton(R.string.cancel, null);

        alert.setCancelable(false);

        alert.show();

    }

    public void changeEmail(View view){
        this.showAlertForEmailPref();
    }


    private String loadJSONFromAsset(int id) {
        String json = null;
        try {

            InputStream is = getResources().openRawResource(id);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void startSingle(View view){
        Commons.reset();
        Intent intent = new Intent(this, ChooseSessionActivity.class);
        intent.putExtra("mode", Commons.SINGLE_MODE);
        startActivity(intent);
    }

    public void startMulti(View view){
        Commons.reset();
        Intent intent = new Intent(this, MultiPlayerConfigActivity.class);
        startActivity(intent);
    }
}
