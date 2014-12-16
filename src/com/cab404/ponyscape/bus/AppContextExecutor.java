package com.cab404.ponyscape.bus;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * @author cab404
 */
public class AppContextExecutor implements Executor {

    Looper looper = Looper.getMainLooper();
    Handler handler = new Handler(looper);

    @Override
    public void execute(Runnable runnable) {
        if (Thread.currentThread() != looper.getThread())
            handler.post(runnable);
        else
            runnable.run();
    }

}
