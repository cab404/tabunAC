package com.cab404.ponyscape.commands;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.libtabun.data.CommonInfo;
import com.cab404.libtabun.data.Profile;
import com.cab404.libtabun.pages.ProfilePage;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.moonlight.parser.HTMLTree;
import com.cab404.moonlight.parser.Tag;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.parts.CreditsPart;
import com.cab404.ponyscape.parts.HelpPart;
import com.cab404.ponyscape.parts.editor.EditorPart;
import com.cab404.ponyscape.parts.editor.plugins.EditorPlugin;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.state.AliasUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author cab404
 */
@CommandClass(prefix = "")
public class CoreCommands {


	@Command(command = "help")
	public void displayHelp() {
		Static.bus.send(new E.Parts.Add(new HelpPart()));
		Static.bus.send(new E.Commands.Finished());
		Static.bus.send(new E.Commands.Clear());
	}

	@Command(command = "about")
	public void displayCredits() {
		Static.bus.send(new E.Parts.Run((new CreditsPart()), true));
		Static.bus.send(new E.Commands.Finished());
		Static.bus.send(new E.Commands.Clear());
	}


	@Command(command = "aliases")
	public void aliases() {
		final StringBuilder aliases = new StringBuilder();

		for (AliasUtils.Alias shortcut : AliasUtils.getAliases()) {
			aliases.append(shortcut.name).append("->").append(shortcut.command).append("\n");
		}

		EditorPart editorPart = new EditorPart("Редактируем меню", aliases, new EditorPart.EditorActionHandler() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean finished(CharSequence text) {
				List<String> lines = SU.split(text.toString(), "\n");
				Collection<AliasUtils.Alias> new_shortcuts = new ArrayList<>();


				int line_num = 0;
				for (String line : lines) {
					line_num++;

					if (line.isEmpty()) continue;

					if (!line.contains("->")) {
						Static.bus.send(new E.Commands.Failure("Нет разделителя в строке " + line_num));
						return false;
					} else {
						List<String> parts = SU.split(line, "->", 2);
						AliasUtils.Alias n = new AliasUtils.Alias(parts.get(0), parts.get(1));
						new_shortcuts.add(n);
					}

				}

				AliasUtils.setAliases(new_shortcuts);
				Static.bus.send(new E.Aliases.Update());
				Static.bus.send(new E.Commands.Finished());
				Static.bus.send(new E.Commands.Clear());
				return true;
			}
			@Override public void cancelled() {
				Static.bus.send(new E.Commands.Finished());
				Static.bus.send(new E.Commands.Clear());
			}
		}, new EditorPlugin[]{});

		Static.bus.send(new E.Parts.Run(editorPart, true));

	}

	@Command(command = "configure")
	public void configure() {
		final StringBuilder config_serialized = new StringBuilder(Static.cfg.data.toJSONString());
		int level = 0;
		for (int i = 0; i < config_serialized.length(); i++) {
			switch (config_serialized.charAt(i)) {
				case '{':
				case '[':
					level++;
					config_serialized.insert(i + 1, "\n" + SU.tabs(level));
					break;
				case '}':
				case ']':
					level--;
					config_serialized.insert(i, "\n" + SU.tabs(level));
					i += level + 1;
					break;
				case ',':
					config_serialized.insert(i + 1, "\n" + SU.tabs(level));
					break;
			}
		}

		EditorPart editorPart = new EditorPart("Редактируем настройки", config_serialized, new EditorPart.EditorActionHandler() {
			@Override
			public boolean finished(CharSequence text) {
				int line_num = 0;
				try {
					Static.cfg.data = (JSONObject) new JSONParser().parse(text.toString());
				} catch (ParseException e) {
					Static.bus.send(new E.Commands.Failure("Неправильный json ._."));
					return false;
				}

				Static.bus.send(new E.Commands.Success("Настройки сохранены. Лучше перезапустить клиент."));
				Static.cfg.save();
				Static.bus.send(new E.Commands.Finished());
				Static.bus.send(new E.Commands.Clear());
				return true;
			}
			@Override public void cancelled() {
				Static.bus.send(new E.Commands.Finished());
				Static.bus.send(new E.Commands.Clear());

			}
		}, new EditorPlugin[]{});

		Static.bus.send(new E.Parts.Run(editorPart, true));

	}

	@Command(command = "clear")
	public void clear() {
		Static.bus.send(new E.Parts.Clear());
		Static.bus.send(new E.Commands.Finished());
		Static.bus.send(new E.Commands.Clear());
	}

	@Command(command = "login")
	public void login() {
		Simple.checkNetworkConnection();

		try {
			Static.bus.send(
					new E.Android.StartActivityForResult(
							new Intent("everypony.tabun.auth.TOKEN_REQUEST"),
							new E.Android.StartActivityForResult.ResultHandler() {
								@Override public void handle(int resultCode, Intent data) {
									if (resultCode == Activity.RESULT_OK) {
										Static.bus.send(new E.Commands.Success("Вошли"));
										Static.user = TabunAccessProfile.parseString(data.getStringExtra("everypony.tabun.cookie"));
										Static.obscure.put("main.profile", Static.user.serialize());
										Static.obscure.save();
										Static.bus.send(new E.Login.Success());
									} else {
										Static.bus.send(new E.Commands.Failure("Не вошли"));
										Static.bus.send(new E.Login.Failure());
									}
									Static.bus.send(new E.Commands.Clear());
									Static.bus.send(new E.Commands.Finished());
								}
								@Override public void error(Throwable e) {
									Static.bus.send(new E.Commands.Failure("Не вошли, установи Tabun.Auth и попробуй снова"));
									Intent download = new Intent(
											Intent.ACTION_VIEW,
											Uri.parse("market://details?id=everypony.tabun.auth")
									);
									Static.bus.send(new E.Android.StartActivity(download));
									Static.bus.send(new E.Commands.Clear());
									Static.bus.send(new E.Commands.Finished());
								}

							}
					)
			);
		} catch (ActivityNotFoundException e) {
			Static.bus.send(new E.Commands.Failure("TabunAuth не установлен."));
		}

	}

	@Command(command = "search", params = Str.class)
	public void search(final String term) {
		Simple.redirect("page load \"/search/topics/?q=" + SU.rl(term.replace("\"", "\\\"")) + "\"");
	}


	@Command(command = "login", params = {Str.class, Str.class})
	public void login(final String login, final String password) {
		Simple.checkNetworkConnection();

		Static.bus.send(new E.Commands.Hide());

		new Thread(new Runnable() {
			@Override public void run() {

				boolean run_result;
				try {
					run_result = Static.user.login(login, password);
				} catch (Exception e) {
					run_result = false;
				}

				final boolean success = run_result;

				Static.handler.post(new Runnable() {
					@Override public void run() {
						if (success) {
							Static.bus.send(new E.Commands.Success("Вошли"));
							Static.obscure.put("main.profile", Static.user.serialize());
							Static.obscure.save();
							Static.bus.send(new E.Login.Success());
							Static.bus.send(new E.Commands.Clear());
						} else {
							Static.bus.send(new E.Commands.Failure("Не вошли"));
							Static.bus.send(new E.Login.Failure());
						}
						Static.bus.send(new E.Commands.Finished());
					}
				});

			}
		}).start();
	}

	@Command(command = "mailbox")
	public void mailbox_shortcut() {
		Simple.redirect("mail box");
	}

	@Command(command = "autoconf")
	public void autoconf() {
		new Thread() {
			@Override public void run() {
				Simple.checkNetworkConnection();

				CommonInfo inf = Static.last_page == null ? null : Static.last_page.c_inf;

				if (inf == null) {

					TabunPage test_page = new TabunPage();
					test_page.fetch(Static.user);

					if (test_page.c_inf == null) {
						Static.bus.send(new E.Commands.Failure("Войдите за пользователя, чтобы заполнить ссылки подписками."));
					}

					inf = test_page.c_inf;
				}

				Collection<AliasUtils.Alias> aliases = new ArrayList<>();

				if (inf != null) {
					ProfilePage page = new ProfilePage(inf.username);
					page.fetch(Static.user);
					ArrayList<String> add_as_links = new ArrayList<>();

					add_as_links.add(page.user_info.get(Profile.UserInfoType.BELONGS));
					add_as_links.add(page.user_info.get(Profile.UserInfoType.ADMIN));
					add_as_links.add(page.user_info.get(Profile.UserInfoType.CREATED));
					add_as_links.add(page.user_info.get(Profile.UserInfoType.MODERATOR));

					for (String s : add_as_links) {
						if (s == null) continue;

						HTMLTree tree = new HTMLTree(s);

						for (Tag tag : tree.xPath("a"))
							aliases.add(
									new AliasUtils.Alias(
											tree.getContents(tag),
											"page load " + SU.sub(tag.get("href"), "/blog/", "/")
									)
							);

					}

				}

				aliases.add(new AliasUtils.Alias("Настройки", "configure"));
				aliases.add(new AliasUtils.Alias("Главная", "page load /"));
				aliases.add(new AliasUtils.Alias("Архив", "saved posts"));
				aliases.add(new AliasUtils.Alias("Почта", "mailbox"));
				aliases.add(new AliasUtils.Alias("Вход", "login"));

				AliasUtils.setAliases(aliases);

				Static.bus.send(new E.Commands.Finished());
				Static.bus.send(new E.Commands.Clear());

				Static.bus.send(new E.Aliases.Update());

			}
		}.start();

	}

}
