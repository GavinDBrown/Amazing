
package com.GavinDev.Amazing.activities;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Subclass of Handler that contains a WeakReference to an Activity and passes
 * messages through for the activity to handle.
 * 
 * @param <T>
 */
public class ActivityHandler extends Handler {
    private final WeakReference<ActivityHandlerCallback> mActivity;

    // Interface the container activity must implement
    public interface ActivityHandlerCallback {
        public void handleMessage(Message msg);
    }

    ActivityHandler(ActivityHandlerCallback activity, Looper looper) {
        super(looper);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            @SuppressWarnings("unused")
            ActivityHandlerCallback mCallback = activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDialogButtonPressedCallback");
        }
        mActivity = new WeakReference<ActivityHandlerCallback>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        ActivityHandlerCallback act = mActivity.get();
        if (act != null) {
            act.handleMessage(msg);
        }
    }
}
