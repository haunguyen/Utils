package vn.mclab.nursing.utils;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class KeyboardHelper {
    int keyboardshown = 0;

    public static KeyboardHelper getInstance() {
        if (instance == null) {
            synchronized (KeyboardHelper.class) {
                instance = new KeyboardHelper();
            }
        }
        return instance;
    }

    private static KeyboardHelper instance;

    private KeyboardHelper() {
        callBacks = new ArrayList<>();
    }

    public void setupUI(View view, final Activity activity) {

        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(activity);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, activity);
            }
        }
    }

    public void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            notifyKeyboardCallBack();
        }
    }

    public void showSoftKeyboard(Activity activity){
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            inputMethodManager.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
            notifyKeyboardCallBack();
        }
    }

    private void notifyKeyboardCallBack() {
        if (callBacks != null && !callBacks.isEmpty()) {
            for (KeyboardCallBack callBack : callBacks) {
                callBack.onKeyBoardHidden();
            }
        }
    }

    protected List<KeyboardCallBack> callBacks;

    public void addCallBack(KeyboardCallBack callBack) {
        if (callBacks != null && !callBacks.contains(callBack)) {
            callBacks.add(callBack);
        }
    }

    public void removeCallBack(KeyboardCallBack callBack) {
        if (callBacks != null && callBacks.contains(callBack)) {
            callBacks.remove(callBack);
        }
    }

    public interface KeyboardCallBack {
        void onKeyBoardHidden();
    }

}
