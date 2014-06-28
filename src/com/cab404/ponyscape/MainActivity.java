package com.cab404.ponyscape;

import android.animation.Animator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cab404.acli.base.ACLIList;
import com.cab404.jconsol.CommandManager;
import com.cab404.jconsol.CommandNotFoundException;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.ponyscape.commands.Core;
import com.cab404.ponyscape.commands.Load;
import com.cab404.ponyscape.events.Events;
import com.cab404.ponyscape.utils.Bus;

public class MainActivity extends Activity {
    private TextView line;
    private Handler handler;
    private float dpi;

    private static final int TOKEN_REQUEST_CODE = 42;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        dpi = getResources().getDisplayMetrics().density;

        Static.list = new ACLIList((ViewGroup) findViewById(R.id.data));
        Static.cm = new CommandManager();
        Static.user = new TabunAccessProfile();
        Static.app_context = getApplicationContext();
        Static.handler = new Handler(getMainLooper());

        line = (TextView) findViewById(R.id.input);

        line.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView textView, int ime, KeyEvent kE) {
                if (kE == null || kE.getAction() == KeyEvent.ACTION_UP)
                    execute();
                return true;
            }
        });

        Static.cm.register(Core.class);
        Static.cm.register(Load.class);


        Bus.register(this);

        hideHelp(0);

    }


    /* Invoked on button press */
    public void execute() {
        CharSequence data = line.getText();
        try {
            line.setError(null);
            Static.cm.run(data.toString());
        } catch (CommandNotFoundException e) {
            Log.e("Command execution", "Error while evaluating '" + data + "' — command not found.");
            line.setError("Command not found");
        }
    }


    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /* Получили что-то от профилей. */
        if (requestCode == TOKEN_REQUEST_CODE)
            if (resultCode == RESULT_OK) {
                Bus.send(new Events.LoginSuccess());
                Static.user = TabunAccessProfile.parseString(data.getStringExtra("everypony.tabun.cookie"));
            } else {
                Bus.send(new Events.LoginFailure());
            }

    }

    @Bus.Handler
    public void login(Events.LoginRequested event) {
        try {
            startActivityForResult(new Intent("everypony.tabun.auth.TOKEN_REQUEST"), TOKEN_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Intent download = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=everypony.tabun.auth")
            );
            startActivity(download);
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Bus.Handler
    public void runCommand(final Events.RunCommand event) {
        handler.post(new Runnable() {
            @Override public void run() {
                line.setText(event.command);
                execute();
            }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        Bus.unregister(this);
    }

    private boolean bg_active = false;

    public void activateBG(View view) {
        if (bg_active = !bg_active)
            showHelp(50);
        else
            hideHelp(50);

    }

    private void hideHelp(final int delay_per_item) {
        final LinearLayout items = (LinearLayout) findViewById(R.id.commands_root);
        findViewById(R.id.command_bg).animate().scaleX(1).scaleY(1)
                .setDuration(items.getChildCount() * delay_per_item);

        for (int i = 0; i < items.getChildCount(); i++) {
            final View anim = items.getChildAt(i);

            Static.handler.postDelayed(
                    new Runnable() {
                        public void run() {
                            anim.animate()
                                    .x(items.getWidth() * 2);
                        }
                    },
                    i * delay_per_item
            );

        }

        findViewById(R.id.fade_bg).animate()
                .setDuration(items.getChildCount() * delay_per_item)
                .alpha(0).setListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {}
            @Override public void onAnimationEnd(Animator animator) {
                findViewById(R.id.fade_bg).setVisibility(View.GONE);
            }
            @Override public void onAnimationCancel(Animator animator) {}
            @Override public void onAnimationRepeat(Animator animator) {}
        });

    }

    /**
     * Вызывается, если пользователь нажал на затемненный фон.
     */
    public void onFadePresseed(View view) {
        hideHelp(50);
    }

    private void showHelp(final int delay_per_item) {
        final LinearLayout items = (LinearLayout) findViewById(R.id.commands_root);
        findViewById(R.id.command_bg).animate()
                .scaleX(items.getChildCount() * 3)
                .scaleY(items.getChildCount() * 3)
                .setDuration(items.getChildCount() * delay_per_item);

        for (int i = 0; i < items.getChildCount(); i++) {
            final View anim = items.getChildAt(i);
            /** Передвигаем элементы в начальное положение только первый раз. */
            if (items.getVisibility() == View.INVISIBLE)
                anim.setX(items.getWidth() * 2);

            Static.handler.postDelayed(
                    new Runnable() {
                        public void run() {
                            anim.animate()
                                    .x(items.getWidth() - anim.getWidth());
                        }
                    },
                    i * delay_per_item
            );

        }

        findViewById(R.id.fade_bg).animate()
                .setDuration(items.getChildCount() * delay_per_item)
                .alpha(1).setListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {
                findViewById(R.id.fade_bg).setVisibility(View.VISIBLE);
            }
            @Override public void onAnimationEnd(Animator animator) {}
            @Override public void onAnimationCancel(Animator animator) {}
            @Override public void onAnimationRepeat(Animator animator) {}
        });

        items.setVisibility(View.VISIBLE);
    }

    public void login(View view) {

    }

}
