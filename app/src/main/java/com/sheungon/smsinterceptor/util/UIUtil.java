package com.sheungon.smsinterceptor.util;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * @author John
 */
public class UIUtil {


    public static void hideSoftKeyboard(@NonNull Activity activity) {
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            hideSoftKeyboard(activity, currentFocus);
        } else {
            Log.w("Cannot hide keyboard as no focus in the provided activity.");
        }
    }

    static void hideSoftKeyboard(@NonNull Context context,
                                 @NonNull View currentFocusView) {

        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(), 0);
    }
}
