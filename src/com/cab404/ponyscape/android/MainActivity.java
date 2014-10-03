package com.cab404.ponyscape.android;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.cab404.acli.ACLIList;
import com.cab404.jconsol.CommandManager;
import com.cab404.jconsol.CommandNotFoundException;
import com.cab404.jconsol.NonEnclosedParesisException;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.moonlight.util.RU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.AppContextExecutor;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.animation.Anim;
import com.cab404.ponyscape.utils.animation.BounceInterpolator;
import com.cab404.ponyscape.utils.state.AliasUtils;
import com.cab404.ponyscape.utils.views.FollowableScrollView;
import com.cab404.sjbus.Bus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AbstractActivity {

	/**
	 * Командная строка
	 */
	private TextView line;
	/**
	 * Запущеные для результата задания. Точнее, слушалки этих самых результатов.
	 */
	private Map<Integer, E.Android.StartActivityForResult.ResultHandler> running = new HashMap<>();
	/**
	 * Менеджер всея листа.
	 */
	private ACLIList list;

	/**
	 * Кэшированная константа из xml-ей; отвечает за скорость появления/исчезновения меню алиасов
	 */
	private int alias_menu_animation_duration;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/* Кэшируем константы*/
		alias_menu_animation_duration = Static.cfg.ensure("main.anim_delay", 50);

        /* Привязываем локальные переменные */
		list = new ACLIList((ViewGroup) findViewById(R.id.data));
		line = (TextView) findViewById(R.id.input);
		line.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override public boolean onEditorAction(TextView textView, int ime, KeyEvent kE) {
				if (kE == null || kE.getAction() == KeyEvent.ACTION_UP)
					Static.bus.send(new E.Commands.Run(textView.getText().toString()));
				return true;
			}
		});

		/* Обновляем меню ссылок*/
		updateAliasList();

		/* Пытаемся подгрузить юзверя из файла. */
		Object o = Static.obscure.get("main.profile");
		if (o != null)
			Static.user = TabunAccessProfile.parseString((String) o);

		/* Обновляем хинт в поле команды */
		finished(null);

        /* Убираем меню ссылок */
		hideAliases(0);

		/* Делаем бар полупрозрачным */
		line.getBackground().setAlpha(200);

		if (Build.VERSION.SDK_INT >= 11)
			findViewById(R.id.data_root).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
				@Override public void onLayoutChange(View v, int l, int t, int r, int b, int oL, int oT, int oR, int oB) {
					if (oL != l || oR != r)
						Static.bus.send(new E.Android.RootSizeChanged());
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
						if (!bar_locked_by_expansion &&
								findViewById(R.id.data).getHeight() < findViewById(R.id.data_root).getHeight()
								) {
							showBar();
						}
						if (bar_locked_by_expansion) {
							hideBar();
							if (aliases_menu_active)
								hideAliases(0);
						}
						Static.handler.postDelayed(this, 200);
					}
				});

		/* Пытаемся достать init-команду */
		Static.bus.send(new E.Commands.Run(Static.cfg.ensure("main.init", "help")));

	}

	private boolean command_running = false;
	/**
	 * Запускает команду в окошке и блокирует его изменение
	 */
	public void execute() {
		CharSequence data = line.getText();
		line.setError(null);

		/* Рубим в куски.*/
		List<String> commands = CommandManager.splitCommandLines(data.toString());
		/* Если кусков дофига, то остальное кладём в холодильник. Чтоб не портилось. */
		if (commands.size() > 1) {
			command_queue.addAll(commands.subList(1, commands.size()));
			data = commands.get(0);
		}

		if (data.length() != 0)
			try {

				findViewById(R.id.execution).setVisibility(View.VISIBLE);
				command_running = true;
				updateInput();

				try {
					Static.cm.run(data.toString());
				} catch (InvocationTargetException e) {
					/* Достаём из обёртки рефлексии */
					Throwable ex = e.getCause();
					while (ex instanceof InvocationTargetException)
						ex = ex.getCause();
					throw ex;
				}

			} catch (CommandNotFoundException e) {
				Log.e("Command execution", "Error while evaluating '" + data + "' — command not found.");
				Static.bus.send(new E.Commands.Error("Команда не найдена"));
				Static.bus.send(new E.Commands.Finished());

			} catch (Simple.NetworkNotFound nf) {
				Log.e("Command execution", "Error while evaluating '" + data + "' — network not found.");
				Static.bus.send(new E.Commands.Error("Нет подключения к Сети"));

				Static.bus.send(new E.Commands.Finished());
			} catch (NonEnclosedParesisException nf) {

				Log.e("Command execution", "Error while evaluating '" + data + "' — non-enclosed paresis.");
				Static.bus.send(new E.Commands.Error("Незакрытые кавычки."));
				Static.bus.send(new E.Commands.Finished());
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}

	}


	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.v("Main", "Got  activity result!");
		running.remove(requestCode).handle(resultCode, data);
	}

	@Override public void onBackPressed() {
		if (command_running) {
			Static.bus.send(new E.Commands.Abort());
			Static.bus.send(new E.Commands.Finished());
		} else if (aliases_menu_active)
			hideAliases(alias_menu_animation_duration);
		else if (!TextUtils.isEmpty(line.getText())) {
			line.setText("");
		} else if (Static.history.size() > 1) {
			Static.history.remove(Static.history.size() - 1);
			Static.bus.send(new E.Commands.Run(Static.history.remove(Static.history.size() - 1)));
		} else {
			super.onBackPressed();
		}

	}

	/*    / / / BUS
	 * / / /
     */

	/**
	 * Скрывает вводимые символы, отменяется на {@link MainActivity#finished(com.cab404.ponyscape.bus.E.Commands.Finished) finished()}
	 */
	@Bus.Handler
	public void hideInputCharacters(E.Commands.Hide e) {
		line.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
	}

	/**
	 * Убирает анимацию загрузки и снимает блокировку ввода.
	 */
	@Bus.Handler(executor = AppContextExecutor.class)
	public void finished(E.Commands.Finished event) {
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
			Static.bus.send(new E.Commands.Run(command_queue.remove(0)));
		}
	}

	/**
	 * Очищает список частей
	 */
	@Bus.Handler(executor = AppContextExecutor.class)
	public void clear(E.Commands.Clear event) {
		line.setText("");
	}

	/**
	 * Выставляет в командную строку выданную команду и запускает её, либо добавляет в очередь.
	 */
	private List<String> command_queue = new ArrayList<>();
	@Bus.Handler(executor = AppContextExecutor.class)
	public void runCommand(final E.Commands.Run event) {
		if (!command_running) {
			line.setText(event.command);
			execute();
		} else {
			command_queue.add(event.command);
		}
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void updateShortcuts(E.Aliases.Update event) {
		updateAliasList();
		showAliases(0);
		hideAliases(0);
	}

	@Bus.Handler
	public void size(E.DataRequest.ListSize e) {
		View root = findViewById(R.id.data_root);
		e.width = root.getWidth();
		e.height = root.getHeight();
	}


	boolean bar_locked_by_expansion = false;
	@Bus.Handler(executor = AppContextExecutor.class)
	public void expand(E.Parts.Expand e) {
		findViewById(R.id.data).setPadding(0, 0, 0, 0);
		findViewById(R.id.data).requestLayout();
		((FollowableScrollView) findViewById(R.id.data_root)).setScrollEnabled(false);
		bar_locked_by_expansion = true;
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void collapse(E.Parts.Collapse e) {
		findViewById(R.id.data).setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.list_bottom_padding));
		findViewById(R.id.data).requestLayout();
		((FollowableScrollView) findViewById(R.id.data_root)).setScrollEnabled(true);
		bar_locked_by_expansion = false;
	}

	@Bus.Handler
	protected void lockListScroll(E.Parts.Lock event) {
		((FollowableScrollView) findViewById(R.id.data_root)).setScrollEnabled(false);
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void clear(E.Parts.Clear clear) {
		for (int i = list.size() - 1; i > -1; i--)
			list.remove(list.partAt(i));
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void focus(E.Parts.Focus event) {
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
	public void add(E.Parts.Add add) {
		list.add(add.part);
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void remove(E.Parts.Remove remove) {
		list.removeSlowly(remove.part);
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void hide(E.Parts.Hide remove) {
		list.hide(remove.part);
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void show(E.Parts.Show remove) {
		list.show(remove.part);
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void error(E.Commands.Error err) {
		Toast toast = Toast.makeText(this, err.error, Toast.LENGTH_SHORT);
		toast.getView().getBackground().setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY));
		toast.show();
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void success(E.Commands.Success msg) {
		Toast toast = Toast.makeText(this, msg.msg, Toast.LENGTH_SHORT);
		toast.getView().getBackground().setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY));
		toast.show();
	}


	@Bus.Handler
	public synchronized void startActivityFromEvent(E.Android.StartActivityForResult e) {
		int request_key = (int) ((Math.random() - 0.5) * 2 * Integer.MAX_VALUE);
		running.put(request_key, e.handler);
		try {
			startActivityForResult(e.intent, request_key);
		} catch (Throwable t) {
			e.handler.error(t);
			running.remove(request_key);
		}
	}
	@Bus.Handler
	public void startActivityFromEvent(E.Android.StartActivity e) {
		startActivity(e.activity);
	}

	/*    / / / UI
	* / / /
	*/

	/**
	 * Активно ли меню алиасов?
	 */
	private boolean aliases_menu_active = false;

	/**
	 * Переключает меню алиасов.
	 */
	public void onMenuButtonPressed(View view) {
		if (!TextUtils.isEmpty(line.getText()) && !command_running)
			execute();
		else if (aliases_menu_active)
			hideAliases(alias_menu_animation_duration);
		else
			showAliases(alias_menu_animation_duration);
	}

	/**
	 * Прячет круглую менюшку алиасов
	 */
	private void hideAliases(final int delay_per_item) {

		if (!aliases_menu_active) return;

		aliases_menu_active = false;
		updateInput();


		if (Build.VERSION.SDK_INT >= 12) {

			final LinearLayout items = (LinearLayout) findViewById(R.id.commands_root);

			findViewById(R.id.command_bg)
					.animate()
					.scaleX(1).scaleY(1)
					.setInterpolator(null)
					.setDuration(items.getChildCount() * delay_per_item + delay_per_item);

			for (int i = 0; i < items.getChildCount(); i++) {
				final View anim = items.getChildAt(i);

				Static.handler.postDelayed(
						new Runnable() {
							@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
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
					.setDuration(items.getChildCount() * delay_per_item + delay_per_item)
					.alpha(0)
					.setListener(new Anim.AnimatorListenerImpl() {
						@Override public void onAnimationEnd(Animator animator) {
							findViewById(R.id.menu_scroll_pane).setVisibility(View.GONE);
							findViewById(R.id.fade_bg).setVisibility(View.GONE);
						}
					});

		} else {
			Anim.fadeOut(findViewById(R.id.fade_bg));
			Anim.fadeOut(findViewById(R.id.command_bg));
			Anim.fadeOut(findViewById(R.id.commands_root));
			Anim.fadeOut(findViewById(R.id.menu_scroll_pane));
		}
	}

	/**
	 * Вызывается, если пользователь нажал на затемненный фон.
	 * Скрывает список алиасов.
	 */
	public void closeAliases(View view) {
		hideAliases(alias_menu_animation_duration);
	}

	/**
	 * Показывает меню алиасов.
	 */
	private void showAliases(final int delay_per_item) {
		line.setError(null);
		if (aliases_menu_active | !bar_enabled | bar_processing | bar_locked_by_expansion) return;

		aliases_menu_active = true;
		updateInput();

		if (Build.VERSION.SDK_INT >= 12) {

			final LinearLayout items = (LinearLayout) findViewById(R.id.commands_root);
			final View button = findViewById(R.id.command_bg);

			findViewById(R.id.command_bg).animate()
				/* Размер иконки умножен на 4, т.к лень вручную вбивать размер всяких оффсетов и другой галиматьи.*/
					.scaleX(((float) items.getHeight() * 2 + button.getHeight() * 4) / button.getHeight())
					.scaleY(((float) items.getHeight() * 2 + button.getHeight() * 4) / button.getHeight())
					.setInterpolator(new BounceInterpolator())
					.setDuration(items.getChildCount() * delay_per_item + delay_per_item);

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
							@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1) public void run() {
								float dpi = getResources().getDisplayMetrics().density;
								anim.animate()
										.setInterpolator(new BounceInterpolator())
										.setDuration(delay_per_item * 2 + delay_per_item)
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

		} else {

			Anim.fadeIn(findViewById(R.id.fade_bg));
			Anim.fadeIn(findViewById(R.id.command_bg));
			Anim.fadeIn(findViewById(R.id.commands_root));
			Anim.fadeIn(findViewById(R.id.menu_scroll_pane));

		}
	}

	/**
	 * Показана ли командная строка и иже с ней?
	 */
	private boolean bar_enabled = true;
	/**
	 * Запущена ли анимация на командной строке и ей присущем?
	 */
	private boolean bar_processing = false;

	/**
	 * Прячет командную строку
	 */
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

	/**
	 * Показывает командную строку.
	 */
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

	/**
	 * Довольно интересная функция. В зависимости от кучи факторов включает
	 * или выключает возможность редактировать командную строку.
	 */
	protected void updateInput() {
		line.setEnabled(!command_running && bar_enabled && !aliases_menu_active);
	}

	/**
	 * Тут происходит весь резолвинг адресов.
	 */
	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {

			Uri data = intent.getData();
			Log.v("Main", "Получили новый Intent по адресу " + data);
			List<String> segments = data.getPathSegments();
			String command = null;

			/* Корневой адрес */
			if (segments.size() == 0)
				command = "page load /";

			if (segments.size() == 2) {

				/* Обработка постов */
				if ("blog".equals(segments.get(0)))
					command = "page load " + segments.get(1);

				/* Обработка профилей */
				if ("profile".equals(segments.get(0)))
					command = "user load " + segments.get(1);

				/* Обработка ссылок на комментарии  */
				if ("comments".equals(segments.get(0)))
					command = "post by_comment " + segments.get(1);

			}
			if (segments.size() == 3) {
				if ("blog".equals(segments.get(0))) {

					/* Обработка приглашений в блоги */
					if (segments.get(2).equals("accept"))
						new Thread() {
							@Override public void run() {
								Simple.checkNetworkConnection();
								HttpHead accept = new HttpHead(intent.getData().toString());
								HttpResponse response = RU.exec(accept, Static.user);
								if (response.getStatusLine().getStatusCode() / 100 < 4)
									Static.bus.send(new E.Commands.Success("Приглашение принято."));
								else
									Static.bus.send(new E.Commands.Error("Ошибка при принятии приглашения : "
											+ response.getStatusLine().getStatusCode()));
							}
						}.start();
					else if (segments.get(2).equals("reject"))
						new Thread() {
							@Override public void run() {
								Simple.checkNetworkConnection();
								HttpHead accept = new HttpHead(intent.getData().toString());
								HttpResponse response = RU.exec(accept, Static.user);
								if (response.getStatusLine().getStatusCode() / 100 < 4)
									Static.bus.send(new E.Commands.Success("Приглашение отвергнуто."));
								else
									Static.bus.send(new E.Commands.Error("Ошибка при отказе от приглашения : "
											+ response.getStatusLine().getStatusCode()));
							}
						}.start();
					else

					/* Обработка постов */
						command = "post load " + segments.get(2).replace(".html", "");
				}
			}

			if (command != null)
				Static.bus.send(new E.Commands.Run(command));

		} else
			super.onNewIntent(intent);
	}

	/**
	 * Обновляет список алиасов из глобальных настроек
	 */
	protected void updateAliasList() {
		LayoutInflater inflater = getLayoutInflater();
		LinearLayout views = (LinearLayout) findViewById(R.id.commands_root);
		views.removeViews(0, views.getChildCount());

		for (final AliasUtils.Alias alias : AliasUtils.getAliases()) {
			View view = inflater.inflate(R.layout.shortcut, views, false);
			((TextView) view.findViewById(R.id.label)).setText(alias.name);

			view.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View view) {
					Static.bus.send(new E.Commands.Run(alias.command));
					closeAliases(view);
				}
			});
			views.addView(view);
		}

	}
}
