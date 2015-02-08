package com.cab404.ponyscape.android;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.AppContextExecutor;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Keys;
import com.cab404.ponyscape.utils.Static;
import com.cab404.sjbus.Bus;

import java.util.Locale;

/**
 * @author cab404
 */
public class AbstractActivity extends Activity {

    /* Темы. ДА. */
    public final static String[] locales_to_theme = {"en", "ru"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Static.bus.register(this);
        applyTheme();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        applyTheme();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyTheme();
    }

    protected void applyTheme() {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();

        Integer theme_index = Static.cfg.ensure(Keys.MAIN_THEME, 0);
        if (theme_index >= locales_to_theme.length) theme_index = locales_to_theme.length - 1;
        if (theme_index < 0) theme_index = 0;

        conf.locale = new Locale(locales_to_theme[theme_index]);

        res.updateConfiguration(conf, dm);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Static.bus.unregister(this);
    }

    private void setLunaTalk(String msg) {
        if (findViewById(R.id.princess_Luna).getVisibility() == View.VISIBLE || msg.isEmpty()) {
            TextView talk = (TextView) findViewById(R.id.load_msg);
            talk.setVisibility(msg.isEmpty() ? View.GONE : View.VISIBLE);
            talk.setText(msg);
        }
    }

    @Bus.Handler(executor = AppContextExecutor.class)
    public void lunaTalk_finishListener(E.Commands.Finished f) {
        setLunaTalk("");
    }


    @Bus.Handler(executor = AppContextExecutor.class)
    public void lunaTalk_msg(E.Status msg) {
        setLunaTalk(msg.status);
    }


    @Bus.Handler(executor = AppContextExecutor.class)
    public void onStart(E.Commands.Run unused) {
        if (!unused.command.replace(";", "").isEmpty())
            findViewById(R.id.princess_Luna).setVisibility(View.VISIBLE);
    }

    @Bus.Handler(executor = AppContextExecutor.class)
    public void onFinish(E.Commands.Finished unused) {
        findViewById(R.id.princess_Luna).setVisibility(View.GONE);
    }

    public void luna_quote() {
        if (findViewById(R.id.princess_Luna).getVisibility() == View.VISIBLE) return;
        String[] phrases = getResources().getStringArray(R.array.luna_phrases);
        String quote = phrases[((int) (Math.random() * phrases.length))];

        Static.bus.send(new E.Commands.Run("luna"));
        Static.bus.send(new E.Status(quote));

        Static.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Static.bus.send(new E.Commands.Finished());
            }
        }, 5000);
    }

    public void luna_quote(String quote) {
        if (findViewById(R.id.princess_Luna).getVisibility() == View.VISIBLE) return;
        Static.bus.send(new E.Commands.Run("luna"));
        Static.bus.send(new E.Status(quote));

        Static.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Static.bus.send(new E.Commands.Finished());
            }
        }, 5000);
    }
}
