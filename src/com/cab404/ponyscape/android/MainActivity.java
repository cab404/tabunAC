package com.cab404.ponyscape.android;

import android.animation.Animator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cab404.acli.ACLIList;
import com.cab404.jconsol.CommandManager;
import com.cab404.jconsol.CommandNotFoundException;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.commands.*;
import com.cab404.ponyscape.events.Commands;
import com.cab404.ponyscape.events.Login;
import com.cab404.ponyscape.events.Parts;
import com.cab404.ponyscape.events.Shortcuts;
import com.cab404.ponyscape.utils.Bus;
import com.cab404.ponyscape.utils.Settings;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.views.Anim;
import com.cab404.ponyscape.utils.views.FollowableScrollView;

import java.io.Serializable;
import java.util.ArrayList;

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
		Static.settings = new Settings(this, "settings.bin");
		Static.settings.load();

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
		Static.cm.register(Vote.class);
		Static.cm.register(Make.class);

		/* Обновляем меню ссылок*/
		updateShortcutList();

        /* Убираем меню ссылок */
		hideMenu(0);

		/* Тут проставлено скрытие/показ бара */
		FollowableScrollView view = (FollowableScrollView) findViewById(R.id.data_root);
		view.setHandler(new FollowableScrollView.ScrollHandler() {
			boolean hidden;
			@Override public void onScrolled(int y, int old_y) {
				if (y > old_y) {
					hidden = true;
					hideBar();
				}
				if (y < old_y) {
					hidden = false;
					showBar();
				}

			}
			@Override public void onOverScrolled(float y, boolean clamped) {}
		});

	}

	@Bus.Handler
	public void clear(Parts.Clear clear) {
		while (list.size() > 0)
			list.remove(list.partAt(0));
	}


	@Bus.Handler
	public void add(Parts.Add add) {
		list.add(add.part);
	}

	@Bus.Handler
	public void remove(Parts.Remove remove) {
		list.remove(remove.part);
	}

	/**
	 * Запускает команду в окошке и блокирует его изменение
	 */

	private boolean command_running = false;
	public void execute() {
		CharSequence data = line.getText();
		line.setError(null);

		if (data.length() != 0)
			try {
				findViewById(R.id.execution).setVisibility(View.VISIBLE);
				command_running = true;
				updateInput();
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
		Static.settings.save();
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
		command_running = false;
		updateInput();
		findViewById(R.id.execution).setVisibility(View.GONE);
	}

	@Bus.Handler
	public void clear(Commands.Clear event) {
		line.setText("");
	}

	/* Выставляет в командную строку выданную команду и запускает её. */
	@Bus.Handler
	public void runCommand(final Commands.Run event) {
		if (!command_running)
			Static.handler.post(new Runnable() {
				@Override public void run() {
					line.setText(event.command);
					execute();
				}
			});
	}

	@Bus.Handler
	public void updateShortcuts(Shortcuts.Update event) {
		updateShortcutList();
		showMenu(0);
		hideMenu(0);
	}

	/*    / / / UI
	* / / /
	*/
	private boolean menu_active = false;

	public void toggleMenu(View view) {
		if (menu_active)
			hideMenu(30);
		else
			showMenu(30);
	}

	private void hideMenu(final int delay_per_item) {
		if (!menu_active) return;

		menu_active = false;
		updateInput();

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
		updateInput();

		final LinearLayout items = (LinearLayout) findViewById(R.id.commands_root);
		final View button = findViewById(R.id.command_bg);

		findViewById(R.id.command_bg).animate()
				/* Размер иконки уможен на 4, т.к лень вручную вбивать размер.*/
				.scaleX(((float) items.getHeight() * 2 + button.getHeight() * 4) / button.getHeight())
				.scaleY(((float) items.getHeight() * 2 + button.getHeight() * 4) / button.getHeight())
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


	private boolean bar_enabled = true;

	protected void hideBar() {
		if (!findViewById(R.id.input).isEnabled()) return;

		bar_enabled = false;
		updateInput();

		Anim.fadeOut(findViewById(R.id.input));
		Anim.fadeOut(findViewById(R.id.command_bg));
		Anim.fadeOut(findViewById(R.id.command_button));

	}

	protected void showBar() {
		if (findViewById(R.id.input).isEnabled()) return;

		bar_enabled = true;
		updateInput();

		Anim.fadeIn(findViewById(R.id.input));
		Anim.fadeIn(findViewById(R.id.command_bg));
		Anim.fadeIn(findViewById(R.id.command_button));

	}

	protected void updateInput() {
		line.setEnabled(!command_running && bar_enabled && !menu_active);
	}

	public static class LaunchShortcut implements Serializable {
		private static final long serialVersionUID = 0L;

		String name;
		String command;

		public LaunchShortcut(String name, String command) {
			this.name = name;
			this.command = command;
		}
	}

	@Bus.Handler
	protected void lockListScroll(Parts.Lock event) {
		((FollowableScrollView) findViewById(R.id.data_root)).setScrollEnabled(false);
	}

	protected void updateShortcutList() {
		ArrayList<LaunchShortcut> shortcuts = Static.settings.get("main.shortcuts");
		if (shortcuts == null) shortcuts = new ArrayList<>();

		LayoutInflater inflater = getLayoutInflater();
		LinearLayout views = (LinearLayout) findViewById(R.id.commands_root);
		views.removeViews(0, views.getChildCount());

		for (final LaunchShortcut shortcut : shortcuts) {
			View view = inflater.inflate(R.layout.shortcut, views, false);
			((TextView) view.findViewById(R.id.label)).setText(shortcut.name);
			view.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View view) {
					Bus.send(new Commands.Run(shortcut.command));
				}
			});
			views.addView(view);
		}

		Static.settings.put("main.shortcuts", shortcuts);
		Static.settings.save();
	}
}
