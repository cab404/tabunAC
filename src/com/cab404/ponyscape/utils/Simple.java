package com.cab404.ponyscape.utils;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.cab404.libtabun.data.Blog;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.data.PersonalBlog;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.state.AliasUtils;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author cab404
 */
public class Simple {

    public static void msg(CharSequence text) {
        Toast.makeText(Static.ctx, text, Toast.LENGTH_SHORT).show();
    }

    public static String imgurl(String url) {
        return url
                .replace("[", "%5B");
    }

    public static AnimationDrawable makeLuna() {
        AnimationDrawable luna = new AnimationDrawable();
        for (int i = 1; i <= 16; i++) {
            int id = Static.ctx.getResources().getIdentifier("anim_luna_loading_" + i, "drawable", Static.ctx.getPackageName());
            luna.addFrame(Static.ctx.getResources().getDrawable(id), 50);
        }
        return luna;
    }

    public static String buildRecipients(Context context, Letter letter) {
        int cut = 2;

        if (letter.recipients.size() > cut + 1) {
            int i = letter.recipients.size() - cut;
            return (
                    SU.join(letter.recipients.subList(0, cut), ", ")
                            + String.format(context.getString(R.string.mail_recipients), i)
            );
        } else
            return (SU.join(letter.recipients, ", ").toString());

    }

    public static String md5(String str) {
        try {
            return Base64.encodeToString(MessageDigest.getInstance("MD5").digest(String.valueOf(str).getBytes(Charset.forName("UTF-8"))), Base64.URL_SAFE);
        } catch (NoSuchAlgorithmException e) {
            Log.wtf("MD5", "We cannot md5.");
            return "NO-MD-CAN-BE-CALCULATED";
        }
    }

    public static void checkNetworkConnection() {
        ConnectivityManager net =
                (ConnectivityManager) Static.ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        for (NetworkInfo info : net.getAllNetworkInfo())
            if (info.isAvailable() && info.isConnected())
                return;

        throw new NetworkNotFound();
    }

    public static void checkNonCellularConnection() {
        ConnectivityManager net =
                (ConnectivityManager) Static.ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        for (NetworkInfo info : net.getAllNetworkInfo())
            if (info.isAvailable() && info.isConnected() && info.getType() != ConnectivityManager.TYPE_MOBILE)
                return;

        throw new NetworkNotFound();
    }

    public static void redirect(String to) {
        Static.bus.send(new E.Commands.Finished());
        Static.bus.send(new E.Commands.Run(to));
    }

    public static AliasUtils.Alias getAliasForCommand(List<AliasUtils.Alias> list, String command) {
        for (AliasUtils.Alias alias : list)
            if (command.equals(alias.command))
                return alias;

        return null;
    }

    public static String resolveBlogSimpleUrl(Blog blog) {
        return blog instanceof PersonalBlog ? blog.resolveURL() : blog.url_name;
    }

    public static class NetworkNotFound extends RuntimeException {
    }
}
