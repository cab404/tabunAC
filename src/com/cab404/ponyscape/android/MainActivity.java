package com.cab404.ponyscape.android;

import android.animation.Animator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Movie;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
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
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.Web;
import com.cab404.ponyscape.utils.views.FollowableScrollView;
import com.cab404.ponyscape.utils.views.animation.Anim;
import com.cab404.ponyscape.utils.views.animation.BounceInterpolator;
import com.cab404.sjbus.Bus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AbstractActivity {
	private TextView line;

	private Map<Integer, Android.StartActivityForResult.ResultHandler> running = new HashMap<>();
	private ACLIList list;

	private int anim_duration;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		/* Кэшируем константы*/
		anim_duration = getResources().getInteger(R.integer.menu_animation_duration);

        /* Привязываем локальные переменные */
		list = new ACLIList((ViewGroup) findViewById(R.id.data));
		line = (TextView) findViewById(R.id.input);
		line.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override public boolean onEditorAction(TextView textView, int ime, KeyEvent kE) {
				if (kE == null || kE.getAction() == KeyEvent.ACTION_UP)
					Static.bus.send(new Commands.Run(textView.getText().toString()));
				return true;
			}
		});

		/* Обновляем меню ссылок*/
		updateShortcutList();

		/* Пытаемся подгрузить юзверя из файла. */
		Object o = Static.cfg.get("main.profile");
		if (o != null)
			Static.user = TabunAccessProfile.parseString((String) o);

		/* Обновляем хинт в поле команды */
		finished(null);

        /* Убираем меню ссылок */
		hideAliases(0);

		findViewById(R.id.data_root).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override public void onLayoutChange(View v, int l, int t, int r, int b, int oL, int oT, int oR, int oB) {
				Static.bus.send(new Android.RootSizeChanged());
			}
		});

		/* Тут проставлено скрытие/показ бара */
		FollowableScrollView view = (FollowableScrollView) findViewById(R.id.data_root);
		view.setHandler(new FollowableScrollView.ScrollHandler() {
			final int CAPACITY = 5;
			int charge = 0;
			@Override public void onScrolled(int y, int old_y) {
				charge += (y - old_y);
				if (charge > CAPACITY) {
					hideBar();
					charge = CAPACITY;
				}
				if (charge < -CAPACITY) {
					showBar();
					charge = -CAPACITY;
				}

			}
			@Override public void onOverScrolled(float y, boolean clamped) {

			}
		});

		/* Автопоказ бара, если контент меньше прокручиваемой вьюхи.*/
		Static.handler.post(
				new Runnable() {
					@Override public void run() {
						if (findViewById(R.id.data).getHeight() < findViewById(R.id.data_root).getHeight()
								&& !bar_locked_by_expansion
								) {
							showBar();
						}
						if (bar_locked_by_expansion) {
							hideBar();
							if (aliases_menu_active)
								hideAliases(0);
						}
						Static.handler.post(this);
					}
				});

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
				Log.v("MAIN", "IM OUT!");


			} catch (CommandNotFoundException e) {
				Log.e("Command execution", "Error while evaluating '" + data + "' — command not found.");
				line.setError("Команда не найдена");

				Static.bus.send(new Commands.Finished());

			} catch (RuntimeException nf) {
				Throwable lowest = nf;
				while (lowest.getCause() != null)
					lowest = lowest.getCause();

				if (lowest instanceof Web.NetworkNotFound) {
					Log.e("Command execution", "Error while evaluating '" + data + "' — network not found.");
					line.setError("Нет подключения к Сети");
				} else {
					throw nf;
				}

				Static.bus.send(new Commands.Finished());

			}

	}


	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		running.get(requestCode).handle(resultCode, data);
	}

	@Override public void onBackPressed() {
		if (command_running)
			Static.bus.send(new Commands.Abort());
		else if (Static.history.size() > 1) {
			Static.history.remove(Static.history.size() - 1);
			Static.bus.send(new Commands.Run(Static.history.remove(Static.history.size() - 1)));
		}

	}

	/*    / / / BUS
	 * / / /
     */

	/**
	 * Скрывает вводимые символы, отменяется на {@link MainActivity#finished(Commands.Finished) finished()}
	 */
	@Bus.Handler
	public void hideInputCharacters(Commands.Hide e) {
		line.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
	}

	/**
	 * Убирает анимацию загрузки и снимает блокировку ввода.
	 */
	@Bus.Handler(executor = AppContextExecutor.class)
	public void finished(Commands.Finished event) {
		command_running = false;
		line.setInputType(InputType.TYPE_CLASS_TEXT);
		updateInput();

		/* Выставляем имя пользователя в хинт (если пользователь вошел)*/
		if (Static.last_page != null && Static.last_page.c_inf != null) {
			line.setHint(Static.last_page.c_inf.username + "@tabun:#");
		} else {
			line.setHint("pony@tabun:$");
		}

		findViewById(R.id.execution).setVisibility(View.GONE);
		if (!command_queue.isEmpty()) {
			Log.v("MainActivity", "Command queue is not empty, executing command from queue. Queue size: " + command_queue.size());
			Static.bus.send(new Commands.Run(command_queue.remove(0)));
		}
	}

	/**
	 * Очищает список частей
	 */
	@Bus.Handler(executor = AppContextExecutor.class)
	public void clear(Commands.Clear event) {
		line.setText("");
	}

	/**
	 * Выставляет в командную строку выданную команду и запускает её, либо добавляет в очередь.
	 */

	private List<String> command_queue = new ArrayList<>();
	@Bus.Handler(executor = AppContextExecutor.class)
	public void runCommand(final Commands.Run event) {
		if (!command_running) {
			line.setText(event.command);
			execute();
		} else {
			command_queue.add(event.command);
		}
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void updateShortcuts(Shortcuts.Update event) {
		updateShortcutList();
		showAliases(0);
		hideAliases(0);
	}

	@Bus.Handler
	public void size(DataRequest.ListSize e) {
		View root = findViewById(R.id.data_root);
		e.width = root.getWidth();
		e.height = root.getHeight();
	}


	boolean bar_locked_by_expansion = false;
	@Bus.Handler(executor = AppContextExecutor.class)
	public void expand(Parts.Expand e) {
		findViewById(R.id.data).setPadding(0, 0, 0, 0);
		findViewById(R.id.data).requestLayout();
		((FollowableScrollView) findViewById(R.id.data_root)).setScrollEnabled(false);
		bar_locked_by_expansion = true;
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void collapse(Parts.Collapse e) {
		findViewById(R.id.data).setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.list_bottom_padding));
		findViewById(R.id.data).requestLayout();
		((FollowableScrollView) findViewById(R.id.data_root)).setScrollEnabled(true);
		bar_locked_by_expansion = false;
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
	public void show(Parts.Show remove) {
		list.show(remove.part);
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void error(Commands.Error err) {
		showBar();
		line.setError(err.error);
	}

	@Bus.Handler
	public synchronized void startActivityFromEvent(Android.StartActivityForResult e) {
		int request_key = (int) ((Math.random() - 0.5) * 2 * Integer.MAX_VALUE);
		running.put(request_key, e.handler);
		try {
			startActivityForResult(e.intent, request_key);
		} catch (Throwable t) {
			e.handler.error(t);
			running.remove(request_key);
		}
	}

	public void startActivityFromEvent(Android.StartActivity e) {
		startActivity(e.activity);
	}

	/*    / / / UI
	* / / /
	*/
	private boolean aliases_menu_active = false;

	public void toggleMenu(View view) {
		if (aliases_menu_active)
			hideAliases(anim_duration);
		else
			showAliases(anim_duration);
	}

	private void hideAliases(final int delay_per_item) {
		if (!aliases_menu_active) return;

		aliases_menu_active = false;
		updateInput();

		final LinearLayout items = (LinearLayout) findViewById(R.id.commands_root);
		findViewById(R.id.command_bg)
				.animate()
				.scaleX(1).scaleY(1)
				.setInterpolator(null)
				.setDuration(items.getChildCount() * delay_per_item);

		for (int i = 0; i < items.getChildCount(); i++) {
			final View anim = items.getChildAt(i);

			Static.handler.postDelayed(
					new Runnable() {
						public void run() {
							anim.animate()
									.setInterpolator(null)
									.setDuration(delay_per_item * 2)
									.x(items.getWidth() + anim.getWidth());
						}
					},
					i * delay_per_item
			);

		}

		findViewById(R.id.fade_bg).animate()
				.setDuration(items.getChildCount() * delay_per_item)
				.alpha(0)
				.setListener(new Anim.AnimatorListenerImpl() {
					@Override public void onAnimationEnd(Animator animator) {
						findViewById(R.id.menu_scroll_pane).setVisibility(View.GONE);
						findViewById(R.id.fade_bg).setVisibility(View.GONE);
					}
				});

	}

	/**
	 * Вызывается, если пользователь нажал на затемненный фон.
	 * Скрывает список алиасов.
	 */
	public void closeAliases(View view) {
		hideAliases(anim_duration);
	}

	private void showAliases(final int delay_per_item) {
		line.setError(null);
		if (aliases_menu_active) return;

		aliases_menu_active = true;
		updateInput();


		final LinearLayout items = (LinearLayout) findViewById(R.id.commands_root);
		final View button = findViewById(R.id.command_bg);

		findViewById(R.id.command_bg).animate()
				/* Размер иконки умножен на 4, т.к лень вручную вбивать размер всяких оффсетов и другой галиматьи.*/
				.scaleX(((float) items.getHeight() * 2 + button.getHeight() * 4) / button.getHeight())
				.scaleY(((float) items.getHeight() * 2 + button.getHeight() * 4) / button.getHeight())
				.setInterpolator(new BounceInterpolator())
				.setDuration(items.getChildCount() * delay_per_item);

		new Drawable() {
			Movie movie = Movie.decodeFile("");
			@Override public void draw(Canvas canvas) {
				movie.draw(canvas, 0, 0);
			}
			@Override public void setAlpha(int alpha) {
			}
			@Override public void setColorFilter(ColorFilter cf) {
			}
			@Override public int getOpacity() {
				return PixelFormat.OPAQUE;
			}
		};

		for (int i = 0; i < items.getChildCount(); i++) {
			final View anim = items.getChildAt(i);
			anim.setX(items.getWidth() + anim.getWidth());

			Static.handler.postDelayed(
					new Runnable() {
						public void run() {
							float dpi = getResources().getDisplayMetrics().density;
							anim.animate()
									.setInterpolator(new BounceInterpolator())
									.setDuration(delay_per_item * 2)
									.x(items.getWidth() - anim.getWidth() + 2 * dpi);
						}
					},
					(items.getChildCount() - i) * delay_per_item
			);

		}

		findViewById(R.id.fade_bg).animate()
				.setDuration(items.getChildCount() * delay_per_item + delay_per_item)
				.alpha(1)
				.setListener(new Anim.AnimatorListenerImpl() {
					@Override public void onAnimationStart(Animator animator) {
						findViewById(R.id.fade_bg).setVisibility(View.VISIBLE);
					}
				});

		findViewById(R.id.menu_scroll_pane).setVisibility(View.VISIBLE);
	}


	private boolean bar_enabled = true;
	private boolean bar_processing = false;

	protected void hideBar() {
		if (!bar_enabled || bar_processing || aliases_menu_active) return;
		Log.v("Bar", "Hidden");
		bar_enabled = false;
		bar_processing = true;
		updateInput();

		Anim.fadeOut(findViewById(R.id.input), 200);
		Anim.fadeOut(findViewById(R.id.command_bg), 200);
		Anim.fadeOut(findViewById(R.id.command_button), 200, new Runnable() {
			@Override public void run() {
				bar_processing = false;
			}
		});

	}

	protected void showBar() {
		if (bar_enabled || bar_processing) return;

		Log.v("Bar", "Shown");
		bar_enabled = true;
		bar_processing = true;
		updateInput();

		Anim.fadeIn(findViewById(R.id.input), 200);
		Anim.fadeIn(findViewById(R.id.command_bg), 200);
		Anim.fadeIn(findViewById(R.id.command_button), 200, new Runnable() {
			@Override public void run() {
				bar_processing = false;
			}
		});

	}

	protected void updateInput() {
		line.setEnabled(!command_running && bar_enabled && !aliases_menu_active);
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

	@Override protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	protected void updateShortcutList() {
		ArrayList<LaunchShortcut> shortcuts = Static.cfg.get("main.shortcuts");
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
					closeAliases(view);
				}
			});
			views.addView(view);
		}

		Static.cfg.put("main.shortcuts", shortcuts);
		Static.cfg.save();
	}
}
