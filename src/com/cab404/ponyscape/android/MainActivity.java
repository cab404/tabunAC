package com.cab404.ponyscape.android;

import android.animation.Animator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cab404.acli.ACLIList;
import com.cab404.jconsol.CommandNotFoundException;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.AppContextExecutor;
import com.cab404.ponyscape.bus.events.*;
import com.cab404.ponyscape.utils.Anim;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.Web;
import com.cab404.ponyscape.utils.views.FollowableScrollView;
import com.cab404.sjbus.Bus;

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

		setContentView(R.layout.activity_main);

		list = new ACLIList((ViewGroup) findViewById(R.id.data));

        /* Привязываем локальные переменные */
		line = (TextView) findViewById(R.id.input);
		line.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override public boolean onEditorAction(TextView textView, int ime, KeyEvent kE) {
				if (kE == null || kE.getAction() == KeyEvent.ACTION_UP)
					execute();
				return true;
			}
		});


		/* Обновляем меню ссылок*/
		updateShortcutList();

        /* Убираем меню ссылок */
		hideMenu(0);

		/* Тут проставлено скрытие/показ бара */
		FollowableScrollView view = (FollowableScrollView) findViewById(R.id.data_root);
		view.setHandler(new FollowableScrollView.ScrollHandler() {
			@Override public void onScrolled(int y, int old_y) {
				if (y > old_y)
					hideBar();
				else
					showBar();

			}
			@Override public void onOverScrolled(float y, boolean clamped) {}
		});

	}

	@Bus.Handler
	public void size(DataRequest.ListSize e) {
		View root = findViewById(R.id.data_root);
		e.width = root.getWidth();
		e.height = root.getHeight();
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void expand(Parts.Expand e) {
		findViewById(R.id.root).setPadding(0, 0, 0, 0);

	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void collapse(Parts.Collapse e) {
		findViewById(R.id.root).setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.list_bottom_padding));
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void clear(Parts.Clear clear) {
		for (int i = list.size() - 1; i > -1; i--)
			list.removeSlowly(list.partAt(i));
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void focus(Parts.Focus event) {
		int index = list.indexOf(event.part);
		int scroll = 0;

		LinearLayout viewById = (LinearLayout) findViewById(R.id.data);
		FollowableScrollView scrollView = (FollowableScrollView) findViewById(R.id.data_root);

		for (int i = 0; i < index; i++) {
			scroll += viewById.getChildAt(i).getHeight();
		}

		scrollView.scrollTo(0, scroll);

	}


	@Bus.Handler(executor = AppContextExecutor.class)
	public void add(Parts.Add add) {
		list.add(add.part);
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void remove(Parts.Remove remove) {
		list.removeSlowly(remove.part);
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void hide(Parts.Hide remove) {
		list.hide(remove.part);
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void hide(Parts.Show remove) {
		list.show(remove.part);
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

			} catch (Web.NetworkNotFound nf) {

				Log.e("Command execution", "Error while evaluating '" + data + "' — network not found.");
				line.setError("Нет подключения к Сети");

				Static.bus.send(new Commands.Finished());

			} catch (CommandNotFoundException e) {
				Log.e("Command execution", "Error while evaluating '" + data + "' — command not found.");
				line.setError("Команда не найдена");

				Static.bus.send(new Commands.Finished());

			} catch (Throwable t) {
				throw new RuntimeException("Fatality во время выполнения команды " + data, t);
			}

	}


	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/* Получили что-то от профилей. */
		switch (requestCode) {

			case TOKEN_REQUEST_CODE:

				if (resultCode == RESULT_OK) {
					Static.bus.send(new Login.Success());
					Static.user = TabunAccessProfile.parseString(data.getStringExtra("everypony.tabun.cookie"));
				} else {
					Static.bus.send(new Login.Failure());
				}
				break;

		}

	}

	@Override public void onBackPressed() {
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

	@Bus.Handler(executor = AppContextExecutor.class)
	public void unlock(Commands.Finished event) {
		command_running = false;
		updateInput();
		findViewById(R.id.execution).setVisibility(View.GONE);
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void clear(Commands.Clear event) {
		line.setText("");
	}

	/* Выставляет в командную строку выданную команду и запускает её. */
	@Bus.Handler(executor = AppContextExecutor.class)
	public void runCommand(final Commands.Run event) {
		if (!command_running) {
			line.setText(event.command);
			execute();
		}
	}

	@Bus.Handler(executor = AppContextExecutor.class)
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
		Log.v("Bar", "Hidden");
		bar_enabled = false;
		updateInput();

		Anim.fadeOut(findViewById(R.id.input));
		Anim.fadeOut(findViewById(R.id.command_bg));
		Anim.fadeOut(findViewById(R.id.command_button));

	}

	protected void showBar() {
		if (findViewById(R.id.input).isEnabled()) return;

		Log.v("Bar", "Shown");
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
					Static.bus.send(new Commands.Run(shortcut.command));
					closeMenu(view);
				}
			});
			views.addView(view);
		}

		Static.settings.put("main.shortcuts", shortcuts);
		Static.settings.save();
	}
}
