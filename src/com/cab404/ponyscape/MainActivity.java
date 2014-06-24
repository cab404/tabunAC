package com.cab404.ponyscape;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.cab404.acli.base.ACLIList;
import com.cab404.jconsol.CommandManager;
import com.cab404.ponyscape.parts.Credits;
import com.cab404.ponyscape.parts.Help;
import com.cab404.ponyscape.parts.PostPart;
import com.cab404.ponyscape.utils.Clear;

public class MainActivity extends Activity {
    TextView line;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Static.list = new ACLIList((ViewGroup) findViewById(R.id.data));
        Static.cm = new CommandManager();

        line = (TextView) findViewById(R.id.input);

        line.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView textView, int i, KeyEvent kE) {
                if (kE.getKeyCode() == KeyEvent.KEYCODE_ENTER && kE.getAction() == KeyEvent.ACTION_UP)
                    run(null);
                return true;
            }
        });

        Static.cm.register(Credits.class);
        Static.cm.register(Clear.class);
        Static.cm.register(Help.class);

        Static.cm.register(PostPart.class);

    }


    /* Invoked on button press */
    public void run(View view) {
        CharSequence data = line.getText();
        try {
            Static.cm.run(data.toString());
        } catch (RuntimeException e) {
            Log.w("Command execution", "Error while evaluating '" + data + "'", e);
            Toast.makeText(this, "Command not found", Toast.LENGTH_LONG).show();
        }
    }

}
