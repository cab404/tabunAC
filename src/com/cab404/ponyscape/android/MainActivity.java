package com.cab404.ponyscape.android;

import android.animation.Animator;
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
import com.cab404.acli.ACLIList;
import com.cab404.acli.Part;
import com.cab404.jconsol.CommandManager;
import com.cab404.jconsol.CommandNotFoundException;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.commands.Core;
import com.cab404.ponyscape.commands.Page;
import com.cab404.ponyscape.commands.Post;
import com.cab404.ponyscape.events.Commands;
import com.cab404.ponyscape.events.Login;
import com.cab404.ponyscape.events.Parts;
import com.cab404.ponyscape.utils.Bus;
import com.cab404.ponyscape.utils.Static;

import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AbstractActivity {
	private TextView line;

	private static final int TOKEN_REQUEST_CODE = 42;
	private ACLIList list;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		list = new ACLIList((ViewGroup) findViewById(R.id.data));

        /* Инициализируем статику. */
		Static.cm = new CommandManager();
		Static.user = new TabunAccessProfile();
		Static.app_context = getApplicationContext();
		Static.handler = new Handler(getMainLooper());

        /* Привязываем локальные переменные */
		line = (TextView) findViewById(R.id.input);
		line.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override public boolean onEditorAction(TextView textView, int ime, KeyEvent kE) {
				if (kE == null || kE.getAction() == KeyEvent.ACTION_UP)
					execute();
				return true;
			}
		});

        /* Регестрируем обработчики команд */
		Static.cm.register(Core.class);
		Static.cm.register(Post.class);
		Static.cm.register(Page.class);

        /* Убираем меню помощи */
		hideMenu(0);

		final Runnable adder = new Runnable() {
			@Override public void run() {
				if (!MainActivity.this.isDestroyed()) {
					Static.handler.postDelayed(this, 20);
					if (!addQueue.isEmpty())
						list.add(addQueue.remove());
				}
			}
		};
		adder.run();

	}

	@Bus.Handler
	public void clear(Parts.Clear clear) {
		addQueue.clear();
		while (list.size() > 0)
			list.remove(list.partAt(0));
	}


	private Queue<Part> addQueue = new LinkedList<>();

	@Bus.Handler
	public void add(Parts.Add add) {
		addQueue.add(add.part);
	}

	@Bus.Handler
	public void remove(Parts.Remove remove) {
		if (addQueue.contains(remove.part))
			addQueue.remove(remove.part);
		else
			list.add(remove.part);
	}

	/**
	 * Запускает команду в окошке и блокирует его изменение
	 */
	public void execute() {
		CharSequence data = line.getText();
		line.setError(null);

		if (data.length() != 0)
			try {
				findViewById(R.id.execution).setVisibility(View.VISIBLE);
				line.setEnabled(false);
				Static.cm.run(data.toString());
			} catch (CommandNotFoundException e) {
				Log.e("Command execution", "Error while evaluating '" + data + "' — command not found.");
				line.setError("Команда не найдена");

				Bus.send(new Commands.Finished());
				Bus.send(new Commands.Clear());
			}

	}


	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/* Получили что-то от профилей. */
		if (requestCode == TOKEN_REQUEST_CODE)
			if (resultCode == RESULT_OK) {
				Bus.send(new Login.Success());
				Static.user = TabunAccessProfile.parseString(data.getStringExtra("everypony.tabun.cookie"));
			} else {
				Bus.send(new Login.Failure());
			}

	}

	@Override protected void onDestroy() {
		super.onDestroy();
		Bus.unregister(this);
	}

    /*    / / / BUS
     * / / /
     */

	/* Вызывает Tabun.Auth */
	@Bus.Handler
	public void login(Login.Requested event) {
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
	public void unlock(Commands.Finished event) {
		line.setEnabled(true);
		findViewById(R.id.execution).setVisibility(View.GONE);
	}

	@Bus.Handler
	public void clear(Commands.Clear event) {
		line.setText("");
	}

	/* Выставляет в командную строку выданную команду и запускает её. */
	@Bus.Handler
	public void runCommand(final Commands.Run event) {
		Static.handler.post(new Runnable() {
			@Override public void run() {
				line.setText(event.command);
				execute();
			}
		});
	}

	/*    / / / UI
	* / / /
	*/
	private boolean menu_active = false;

	public void toggleMenu(View view) {
		hideBar();
//		if (menu_active)
//			hideMenu(30);
//		else
//			showMenu(30);
	}

	private void hideMenu(final int delay_per_item) {

		final LinearLayout items = (LinearLayout) findViewById(R.id.commands_root);
		findViewById(R.id.command_bg).animate().scaleX(1).scaleY(1)
				.setDuration(items.getChildCount() * delay_per_item);

		for (int i = 0; i < items.getChildCount(); i++) {
			final View anim = items.getChildAt(i);

			Static.handler.postDelayed(
					new Runnable() {
						public void run() {
							anim.animate()
									.setDuration(delay_per_item * 2)
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
				findViewById(R.id.menu_scroll_pane).setVisibility(View.GONE);
				findViewById(R.id.fade_bg).setVisibility(View.GONE);
				menu_active = false;
			}
			@Override public void onAnimationCancel(Animator animator) {}
			@Override public void onAnimationRepeat(Animator animator) {}
		});

	}

	/**
	 * Вызывается, если пользователь нажал на затемненный фон.
	 * Скрывает меню.
	 */
	public void closeMenu(View view) {
		hideMenu(50);
	}

	private void showMenu(final int delay_per_item) {
		if (menu_active) return;
		menu_active = true;

		final LinearLayout items = (LinearLayout) findViewById(R.id.commands_root);

		findViewById(R.id.command_bg).animate()
				.scaleX(items.getChildCount() * 3)
				.scaleY(items.getChildCount() * 3)
				.setDuration(items.getChildCount() * delay_per_item);

		for (int i = 0; i < items.getChildCount(); i++) {
			final View anim = items.getChildAt(i);
			anim.setX(items.getWidth() * 2);
			Static.handler.postDelayed(
					new Runnable() {
						public void run() {
							float dpi = getResources().getDisplayMetrics().density;
							anim.animate()
									.setDuration(delay_per_item * 2)
									.x(items.getWidth() - anim.getWidth() + 2 * dpi);
						}
					},
					(items.getChildCount() - i) * delay_per_item
			);

		}

		findViewById(R.id.fade_bg).animate()
				.setDuration(items.getChildCount() * delay_per_item + delay_per_item)
				.alpha(1).setListener(new Animator.AnimatorListener() {
			@Override public void onAnimationStart(Animator animator) {
				findViewById(R.id.fade_bg).setVisibility(View.VISIBLE);
			}
			@Override public void onAnimationEnd(Animator animator) {}
			@Override public void onAnimationCancel(Animator animator) {}
			@Override public void onAnimationRepeat(Animator animator) {}
		});

		findViewById(R.id.menu_scroll_pane).setVisibility(View.VISIBLE);
	}

	protected void hideBar() {
		findViewById(R.id.input).animate().setDuration(100).alpha(0);
		findViewById(R.id.command_bg).animate().setDuration(100).alpha(0);
		findViewById(R.id.command_button).animate().setDuration(100).alpha(0);
		Static.handler.post(new Runnable() {
			@Override public void run() {
				findViewById(R.id.input).setVisibility(View.GONE);
				findViewById(R.id.command_bg).setVisibility(View.GONE);
				findViewById(R.id.command_button).setVisibility(View.GONE);
			}
		});
	}

	protected void showBar() {
		findViewById(R.id.input).setVisibility(View.GONE);
		findViewById(R.id.command_bg).setVisibility(View.GONE);
		findViewById(R.id.command_button).setVisibility(View.GONE);
		findViewById(R.id.input).animate().setDuration(100).alpha(1);
		findViewById(R.id.command_bg).animate().setDuration(100).alpha(1);
		findViewById(R.id.command_button).animate().setDuration(100).alpha(1).setListener(null);
	}
}
