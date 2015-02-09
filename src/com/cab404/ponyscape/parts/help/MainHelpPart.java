package com.cab404.ponyscape.parts.help;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.cab404.acli.Part;
import com.cab404.jconsol.CommandHolder;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.parts.ConfirmPart;
import com.cab404.ponyscape.parts.editor.EditorPart;
import com.cab404.ponyscape.parts.editor.plugins.EditorPlugin;
import com.cab404.ponyscape.utils.state.Keys;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.text.HtmlRipper;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
public class MainHelpPart extends Part {

    HtmlRipper ripper;

    @Override
    protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
        LinearLayout data = new LinearLayout(context);
        data.setBackgroundColor(Static.ctx.getResources().getColor(R.color.bg_item));
        data.setOrientation(LinearLayout.VERTICAL);

        int padding = context.getResources().getDimensionPixelOffset(R.dimen.internal_margins);
        data.setPadding(padding, padding, padding, padding);

        StringBuilder commands = new StringBuilder();
        for (CommandHolder h : Static.cm.registered()) {
            commands
                    .append((h.prefix + " " + h.annnotation.command()).trim()).append(" ");
            for (Class clazz : h.annnotation.params()) {
                commands.append(clazz.getSimpleName()).append(" ");
            }
            commands.append("\n");
        }
        commands.deleteCharAt(commands.length() - 1);

        ripper = new HtmlRipper(data);
        ripper.escape(context.getString(R.string.help) + "\n\n" + commands + "\n");

        Button tldr = new Button(context);

        tldr.setText("Настройте всё за меня, мне лень это читать.");

        tldr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final StringBuilder startup_command = new StringBuilder();

                final EditorPart request_login = new EditorPart(
                        "Введите логин и пароль в две строки", "Логин\nПароль",
                        new EditorPart.EditorActionHandler() {

                            @Bus.Handler
                            public void handleSuccess(E.Login.Success f) {
                                Log.v("Init", "SUCCESS");
                                Static.bus.unregister(this);
                                startup_command.append("login \"").append(credentials[0]).append("\" ").append(credentials[1]).append(";");
                                startup_command.append("page load /");
                                Static.cfg.put(Keys.MAIN_INIT, startup_command.toString());
                                Static.cfg.save();
                                Static.bus.send(new E.Commands.Run("autoconf; page load /"));
                            }

                            @Bus.Handler
                            public void handleFailure(E.Login.Failure f) {
                                Log.v("Init", "FAILURE");
                                Static.bus.unregister(this);
                                Static.bus.send(
                                        new E.Parts.Run(
                                                new EditorPart(
                                                        "Логин и/или пароль не подходят",
                                                        credentials[0] + "\n" + credentials[1],
                                                        this,
                                                        new EditorPlugin[0]),
                                                true
                                        ));
                            }

                            private String[] credentials;

                            @Override
                            public boolean finished(CharSequence text) {
                                Static.bus.register(this);

                                credentials = SU.splitToArray(text.toString(), 2, '\n');

                                if (credentials.length == 2) {
                                    Static.bus.send(new E.Commands.Run("login " + credentials[0] + " " + credentials[1]));
                                    return true;
                                } else
                                    return false;

                            }

                            @Override
                            public void cancelled() {
                                startup_command.append("page load /");
                                Static.bus.send(new E.Commands.Run("autoconf; page load /"));
                                Static.cfg.put(Keys.MAIN_INIT, startup_command.toString());
                                Static.cfg.save();
                                Static.bus.unregister(this);
                            }

                        },
                        new EditorPlugin[0]
                );

                final ConfirmPart login_type = new ConfirmPart(
                        "Будем логиниться через Tabun.Auth? В принципе он безопаснее, но как хочешь.",
                        new ConfirmPart.ResultHandler() {
                            @Override
                            public void resolved(boolean ok) {
                                if (ok) {
                                    startup_command.append("login;");
                                    startup_command.append("page load /");
                                    Static.cfg.put("main.init", startup_command.toString());
                                    Static.cfg.save();

                                    Static.bus.send(new E.Commands.Run("login; autoconf; page load /"));
                                } else {
                                    Static.bus.send(new E.Parts.Run(request_login, true));
                                }

                            }
                        });


                Static.bus.send(new E.Parts.Run(login_type, true));

            }
        });

        data.addView(tldr, 0);

        return data;
    }

    @Override
    protected void onRemove(View view, ViewGroup parent, Context context) {
        super.onRemove(view, parent, context);
        ripper.destroy();
    }
}
