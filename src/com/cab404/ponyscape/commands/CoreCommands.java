package com.cab404.ponyscape.commands;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.bus.events.*;
import com.cab404.ponyscape.bus.events.Shortcuts.LaunchShortcut;
import com.cab404.ponyscape.parts.CreditsPart;
import com.cab404.ponyscape.parts.EditorPart;
import com.cab404.ponyscape.parts.HelpPart;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.Web;
import com.cab404.sjbus.Bus;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cab404
 */
@CommandClass(prefix = "")
public class CoreCommands {


	@Command(command = "help")
	public void displayHelp() {
		Static.bus.send(new Parts.Run((new HelpPart())));
		Static.bus.send(new Commands.Finished());
		Static.bus.send(new Commands.Clear());
	}

	@Command(command = "about")
	public void displayCredits() {
		Static.bus.send(new Parts.Run((new CreditsPart())));
		Static.bus.send(new Commands.Finished());
		Static.bus.send(new Commands.Clear());
	}


	@Command(command = "aliases")
	public void aliases() {
		final StringBuilder aliases = new StringBuilder();
		JSONArray shortcuts = (JSONArray) Static.cfg.get("main.shortcuts");

		if (shortcuts == null) shortcuts = new JSONArray();

		for (Object string_actulally : shortcuts) {
			final LaunchShortcut shortcut = new LaunchShortcut(string_actulally.toString());
			aliases.append(shortcut.name + "->" + shortcut.command + "\n");
		}

		EditorPart editorPart = new EditorPart("Редактируем меню", aliases, new EditorPart.EditorActionHandler() {
			@Override public boolean finished(CharSequence text) {
				List<String> lines = SU.split(text.toString(), "\n");
				List<LaunchShortcut> new_shortcuts = new ArrayList<>();


				int line_num = 0;
				for (String line : lines) {
					line_num++;

					if (line.isEmpty()) continue;

					if (!line.contains("->")) {
						Static.bus.send(new Commands.Error("Нет разделителя в строке " + line_num));
						return false;
					} else {
						List<String> parts = SU.split(line, "->", 2);
						LaunchShortcut n = new LaunchShortcut(parts.get(0), parts.get(1));
						new_shortcuts.add(n);
					}

				}

				JSONArray shkts = new JSONArray();
				for (LaunchShortcut shortcut : new_shortcuts) {
					shkts.add(shortcut.toString());
				}

				Static.cfg.put("main.shortcuts", shkts);
				Static.bus.send(new Shortcuts.Update());
				Static.bus.send(new Commands.Finished());
				Static.bus.send(new Commands.Clear());
				return true;
			}
			@Override public void cancelled() {

			}
		}, new EditorPart.EditorPlugin[]{});

		Static.bus.send(new Parts.Run(editorPart));

	}

	@Command(command = "clear")
	public void clear() {
		Static.bus.send(new Parts.Clear());
		Static.bus.send(new Commands.Finished());
		Static.bus.send(new Commands.Clear());
	}

	@Command(command = "login")
	public void login() {
		try {
			Static.bus.send(
					new Android.StartActivityForResult(
							new Intent("everypony.tabun.auth.TOKEN_REQUEST"),
							new Android.StartActivityForResult.ResultHandler() {
								@Override public void handle(int resultCode, Intent data) {
									if (resultCode == Activity.RESULT_OK) {
										Toast.makeText(Static.app_context, "Вошли", Toast.LENGTH_SHORT).show();
										Static.user = TabunAccessProfile.parseString(data.getStringExtra("everypony.tabun.cookie"));
										Static.bus.send(new Login.Success());
										Static.bus.send(new Commands.Clear());
										Static.bus.send(new Commands.Finished());
									} else {
										Toast.makeText(Static.app_context, "Не вошли", Toast.LENGTH_SHORT).show();
										Static.bus.send(new Login.Failure());
										Static.bus.send(new Commands.Clear());
										Static.bus.send(new Commands.Finished());
									}
								}
								@Override public void error(Throwable e) {
									Toast.makeText(Static.app_context, "Не вошли, нет Tabun.Auth", Toast.LENGTH_SHORT).show();
									Intent download = new Intent(
											Intent.ACTION_VIEW,
											Uri.parse("market://details?id=everypony.tabun.auth")
									);
									Static.bus.send(new Android.StartActivity(download));
									Static.bus.send(new Commands.Clear());
									Static.bus.send(new Commands.Finished());
								}

							}
					)
			);
		} catch (ActivityNotFoundException e) {
			Static.bus.send(new Commands.Error("TabunAuth не установлен."));
		}

	}

	@Command(command = "search", params = Str.class)
	public void search(final String term) {
		Static.bus.send(new Commands.Run("page load \"/search/topics/?q=" + SU.rl(term.replace("\"", "\\\"")) + "\""));
		Static.bus.send(new Commands.Finished());
		Static.bus.send(new Commands.Clear());
	}


	@Command(command = "login", params = {Str.class, Str.class})
	public void login(final String login, final String password) {
		Web.checkNetworkConnection();

		Static.bus.send(new Commands.Hide());

		new Thread(new Runnable() {
			@Bus.Handler
			public void handle(Commands.Abort e) {
				// Защищаемся от чтения пароля с экрана при отмене
				Static.bus.send(new Commands.Clear());
			}

			@Override public void run() {
				Static.bus.register(this);
				final boolean success = (Static.user.login(login, password));

				Static.handler.post(new Runnable() {
					@Override public void run() {
						if (success) {
							Toast.makeText(Static.app_context, "Вошли", Toast.LENGTH_SHORT).show();
							Static.cfg.put("main.profile", Static.user.serialize());
							Static.bus.send(new Login.Success());
							Static.cfg.save();
							Static.bus.send(new Commands.Clear());
						} else {
							Toast.makeText(Static.app_context, "Не вошли", Toast.LENGTH_SHORT).show();
							Static.bus.send(new Login.Failure());
						}
						Static.bus.send(new Commands.Finished());
					}
				});

				Static.bus.unregister(this);
			}
		}).start();
	}

}
