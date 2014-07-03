package com.cab404.ponyscape.commands;

import android.widget.Toast;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.ponyscape.events.Login;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.parts.Credits;
import com.cab404.ponyscape.parts.Help;
import com.cab404.ponyscape.utils.Bus;

/**
 * @author cab404
 */
@CommandClass(prefix = "")
public class Core {


    @Command(command = "help")
    public void displayHelp() {
        Static.list.add(new Help());
    }

    @Command(command = "credits")
    public void displayCredits() {
        Static.list.add(new Credits());
    }


    @Command(command = "clear")
    public void clear() {
        while (Static.list.size() > 0)
            Static.list.remove(Static.list.partAt(0));
    }

    @Command(command = "login")
    public void login() {
        Bus.send(new Login.Requested());
    }


    @Command(command = "login", params = {Str.class, Str.class})
    public void login(final String login, final String password) {
        new Thread(new Runnable() {
            @Override public void run() {
                final boolean success = (Static.user.login(login, password));

                Static.handler.post(new Runnable() {
                    @Override public void run() {
                        if (success)
                            Toast.makeText(Static.app_context, "Yup", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(Static.app_context, "Nope", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).start();
    }

}
