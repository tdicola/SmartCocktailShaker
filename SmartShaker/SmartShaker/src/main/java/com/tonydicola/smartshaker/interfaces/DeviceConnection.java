package com.tonydicola.smartshaker.interfaces;

import android.os.Handler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Tony on 2/24/14.
 */
public interface DeviceConnection {
    public void requestPermission(Runnable granted);
    public void open() throws IOException;
    public Double getMeasure() throws IOException;
    public void close();

}
