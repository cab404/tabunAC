package com.cab404.ponyscape.android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.AppContextExecutor;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
public class AbstractActivity extends Activity {

	static final boolean THEMES_ENABLED = Static.cfg.ensure("main.themes_enabled", false);

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Static.bus.register(this);
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		Static.bus.unregister(this);
	}

	@Override protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(
				THEMES_ENABLED ?
						Static.theme.getContext(newBase) : newBase
		);
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
			@Override public void run() {
				Static.bus.send(new E.Commands.Finished());
			}
		}, 5000);
	}

	public void luna_quote(String quote) {
		if (findViewById(R.id.princess_Luna).getVisibility() == View.VISIBLE) return;
		Static.bus.send(new E.Commands.Run("luna"));
		Static.bus.send(new E.Status(quote));

		Static.handler.postDelayed(new Runnable() {
			@Override public void run() {
				Static.bus.send(new E.Commands.Finished());
			}
		}, 5000);
	}
}
