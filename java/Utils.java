package vn.mclab.nursing.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.AndroidRuntimeException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.GsonBuilder;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import vn.mclab.nursing.BuildConfig;
import vn.mclab.nursing.R;
import vn.mclab.nursing.app.AppConstants;
import vn.mclab.nursing.base.App;
import vn.mclab.nursing.base.BaseFragment;
import vn.mclab.nursing.base.EnumAnnotation;
import vn.mclab.nursing.model.Age;
import vn.mclab.nursing.model.AppMemo;
import vn.mclab.nursing.model.api.Api101;


public class Utils {
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    /**
     * Generate random String
     *
     * @param len
     * @return
     */
    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static String getDUID() {
        if (TextUtils.isEmpty(SharedPreferencesHelper.getStringValue(AppConstants.DUID))) {
            long time = System.currentTimeMillis();
            Date date = new Date();
            date.setTime(time);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(randomString(35));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            stringBuffer.append("_");
            stringBuffer.append(simpleDateFormat.format(date));
//            LogUtils.e("duid", "" + stringBuffer.toString());
            SharedPreferencesHelper.storeStringValue(AppConstants.DUID, stringBuffer.toString());
            return stringBuffer.toString();
        } else {
//            LogUtils.e("duid", "" + SharedPreferencesHelper.getStringValue(AppConstants.DUID));
            return SharedPreferencesHelper.getStringValue(AppConstants.DUID);
        }

    }

    public static void clearChildFragment(FragmentManager fm) {
        if (fm != null) {
            int countChild = fm.getBackStackEntryCount();
            for (int entry = 0; entry < countChild; entry++) {
                String tag = fm.getBackStackEntryAt(
                        countChild - 1).getName();
                Fragment f = fm.findFragmentByTag(tag);
                if (f != null) {
                    ((BaseFragment) f).setmTransitionAnimation(false);
                }
            }
            try {
                fm.popBackStack(null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }


    public static int convertDipToPixels(float dips) {
        return (int) (dips * App.getAppContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    public static void getDeviceSize(Activity activity, Point devicesize) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        display.getSize(devicesize);
    }

    public static Point getDeviceSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static void fixBackgroundRepeat(View view) {
        try {
            Drawable bg = view.getBackground();
            if (bg != null) {
                if (bg instanceof BitmapDrawable) {
                    BitmapDrawable bmp = (BitmapDrawable) bg;
                    bmp.mutate(); // make sure that we aren't sharing state anymore
                    bmp.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disableClipOnParents(View v) {
        if (v.getParent() == null) {
            return;
        }

        if (v instanceof ViewGroup) {
            ((ViewGroup) v).setClipChildren(false);
        }

        if (v.getParent() instanceof View) {
            disableClipOnParents((View) v.getParent());
        }
    }

    public static void enableClipOnParents(View v) {
        if (v.getParent() == null) {
            return;
        }

        if (v instanceof ViewGroup) {
            ((ViewGroup) v).setClipChildren(true);
        }

        if (v.getParent() instanceof View) {
            enableClipOnParents((View) v.getParent());
        }
    }

    public static String getTimeString(long time) {
        Date date = new Date(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        return dateFormat.format(date);
    }


    public static String getNumberStringFormat(String number) {
        try {
            return new DecimalFormat("#,###,###").format(Integer.valueOf(number).intValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int setListViewHeightBasedOnChildren(ListView listView) {
        try {
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null)
                return 0;

            int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            int totalHeight = 0;
            View view = null;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                view = listAdapter.getView(i, view, listView);
                if (i == 0)
                    view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

                view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += view.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.requestLayout();
            return params.height;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getTimeInChat(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String timestamp = "";
        timestamp = format.format(date);
        return timestamp;
    }

    /**
     * Get absolute width of the display in pixels
     *
     * @param activity
     * @return The absolute width of the display in pixels
     */
    public static int getScreenWidth(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay()
                .getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    public static int getScreenHeight(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay()
                .getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    public static String getTimerString(float time) {
        String hours, minutes, seconds, milliseconds;
        long secs, mins, hrs, msecs;
        secs = (long) (time / 1000);
        mins = (long) ((time / 1000) / 60);
        hrs = (long) (((time / 1000) / 60) / 60);

		/* Convert the seconds to String
         * and format to ensure it has
		 * a leading zero when required
		 */
        secs = secs % 60;
        seconds = String.valueOf(secs);
        if (secs == 0) {
            seconds = "00";
        }
        if (secs < 10 && secs > 0) {
            seconds = "0" + seconds;
        }

		/* Convert the minutes to String and format the String */

        mins = mins % 60;
        minutes = String.valueOf(mins);
        if (mins == 0) {
            minutes = "00";
        }
        if (mins < 10 && mins > 0) {
            minutes = "0" + minutes;
        }

    	/* Convert the hours to String and format the String */

        hours = String.valueOf(hrs);
        if (hrs == 0) {
            hours = "00";
        }
        if (hrs < 10 && hrs > 0) {
            hours = "0" + hours;
        }

    	/* Although we are not using milliseconds on the timer in this example
         * I included the code in the event that you wanted to include it on your own
    	 */
        milliseconds = String.valueOf((long) time);
        if (milliseconds.length() == 2) {
            milliseconds = "0" + milliseconds;
        }
        if (milliseconds.length() <= 1) {
            milliseconds = "00";
        }
        String timeString = hours + minutes + ":" + seconds;

		/* Setting the timer text to the elapsed time */
        return timeString;
    }

    public static HashMap<String, String> getTimerHashMap(float time, boolean enableHour, boolean enableSecond, int maxCharacterHour, int maxCharacterMinute) {
        HashMap<String, String> hashMapTime = new HashMap<String, String>();
        String hours, minutes, seconds, milliseconds;
        long secs, mins, hrs, msecs;
        secs = (long) (time / 1000);
        mins = (long) ((time / 1000) / 60);
        hrs = (long) (((time / 1000) / 60) / 60);

		/* Convert the seconds to String
         * and format to ensure it has
		 * a leading zero when required
		 */
        secs = secs % 60;
        seconds = String.valueOf(secs);
        if (secs == 0) {
            seconds = "00";
        }
        if (secs < 10 && secs > 0) {
            seconds = "0" + seconds;
        }

		/* Convert the minutes to String and format the String */

        mins = mins % 60;
        minutes = String.valueOf(mins);
        if (maxCharacterMinute > 1) {
            if (mins == 0) {
                minutes = "00";
            }
            if (mins < 10 && mins > 0) {
                minutes = "0" + minutes;
            }
        }


    	/* Convert the hours to String and format the String */

        hours = String.valueOf(hrs);
        if (maxCharacterHour > 1) {
            if (hrs == 0) {
                hours = "00";
            }
            if (hrs < 10 && hrs > 0) {
                hours = "0" + hours;
            }
        }


    	/* Although we are not using milliseconds on the timer in this example
         * I included the code in the event that you wanted to include it on your own
    	 */
        milliseconds = String.valueOf((long) time);
        if (milliseconds.length() == 2) {
            milliseconds = "0" + milliseconds;
        }
        if (milliseconds.length() <= 1) {
            milliseconds = "00";
        }

        if (hrs == 0 || !enableHour) {
            hours = "";
        }
        if (!enableSecond) {
            seconds = "";
        }
        hashMapTime.put(AppConstants.HOUR, hours);
        hashMapTime.put(AppConstants.MINUTE, minutes);
        hashMapTime.put(AppConstants.SECOND, seconds);
        /* Setting the timer text to the elapsed time */
        return hashMapTime;
    }

    public static HashMap<String, String> getDateTimeHashMap(long time, int maxCharacterHour, int maxCharacterMinute) {
        HashMap<String, String> hashMapTime = new HashMap<String, String>();
        String hours, minutes, seconds, milliseconds;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        long secs, mins, hrs, msecs;

		/* Convert the seconds to String
         * and format to ensure it has
		 * a leading zero when required
		 */
        mins = calendar.get(Calendar.MINUTE);
        hrs = calendar.get(Calendar.HOUR_OF_DAY);

		/* Convert the minutes to String and format the String */

        minutes = String.valueOf(mins);
        if (maxCharacterMinute > 1) {
            if (mins == 0) {
                minutes = "00";
            }
            if (mins < 10 && mins > 0) {
                minutes = "0" + minutes;
            }
        }


    	/* Convert the hours to String and format the String */

        hours = String.valueOf(hrs);
        if (maxCharacterHour > 1) {
            if (hrs == 0) {
                hours = "00";
            }
            if (hrs < 10 && hrs > 0) {
                hours = "0" + hours;
            }
        }


    	/* Although we are not using milliseconds on the timer in this example
         * I included the code in the event that you wanted to include it on your own
    	 */
        milliseconds = String.valueOf((long) time);
        if (milliseconds.length() == 2) {
            milliseconds = "0" + milliseconds;
        }
        if (milliseconds.length() <= 1) {
            milliseconds = "00";
        }
        hashMapTime.put(AppConstants.YEAR, String.valueOf(calendar.get(Calendar.YEAR)));
        hashMapTime.put(AppConstants.MONTH, String.valueOf(calendar.get(Calendar.MONTH) + 1));
        hashMapTime.put(AppConstants.DATE, String.valueOf(calendar.get(Calendar.DATE)));
        hashMapTime.put(AppConstants.HOUR, hours);
        hashMapTime.put(AppConstants.MINUTE, minutes);
        return hashMapTime;
    }

    public static boolean chooseDate;


    public static String formatBirthday(String nonformatted) {
        StringBuilder strbuilder = new StringBuilder(nonformatted);
        int i = 0;
        for (int j = 0; j < strbuilder.length(); j++) {
            if (strbuilder.charAt(j) == '-') {
                if (i == 0) {
                    strbuilder.replace(j, j + 1, "年");
                    i++;
                } else if (i == 1) {
                    strbuilder.replace(j, j + 1, "月");
                    i++;
                }
            }
        }
        strbuilder.append("日");

        return strbuilder.toString();
    }

    public static int birthTimeToInt(String str) {
        int result;
        if (str == null || str.length() == 0)
            return -1;
        String[] strs = str.split(":");
        result = Integer.valueOf(strs[0]);
        return result;
    }

    public static String stringToBirthTime(int hour) {
        if (hour >= 10) {
            return hour + ":00:00";
        } else {
            return "0" + hour + ":00:00";
        }
    }



    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    public static void goAppSetting(Activity context, int responseCode) {
        Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivityForResult(i, responseCode);
//        context.startActivity(i);
    }

    public static String formatCurrency(double number) {
        String result = null;
        try {
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
            numberFormat.setMaximumFractionDigits(1);
            result = numberFormat.format(number);
//            number = NumberFormat.getNumberInstance(Locale.US).format(Double.valueOf(number));
        } catch (Exception e) {
            result = "";
        }
        return result;
    }

    public static String dateMonthString(int val) {
        String valStr = "" + val;
        if (val < 10) {
            valStr = "0" + valStr;
        }
        return valStr;

    }

    public interface OnDateSelected {
        void onSelected(int year, int monthOfYear, int dayOfMonth, View v);
    }

    public static double convertMlToOz(int dMl) {
        return round(dMl / 29.5735296, 1);
    }

    public static int convertOzToMl(double dOz) {
        return (int) (round(dOz * 29.5735296, 0));
    }

    public static double convertGramToOz(int dGram) {
        return round(dGram / 28.3495, 1);
    }

    public static int convertOzToGram(double dOz) {
        return (int) (round(dOz * 28.3495, 0));
    }

    public static double convertGramToLb(int dGram) {
        return round(dGram / 453.59237, 1);
    }

    public static int convertLbToGram(double dLb) {
        return (int) (round(dLb * 453.59237, 1));
    }

    public static double convertCmToInc(double dCm) {
        return round(dCm / 2.54, 1);
    }

    public static double convertIncToCm(double dInc) {
        return round(dInc * 2.54, 1);
    }

    public static double convertCtoF(double dC) {
        return round(dC * 9 / 5 + 32, 1);
    }

    public static double convertFToC(double dF) {
        return round((dF - 32) * 5 / 9, 1);
    }

    public static double round(double value, int decimalPlace) {
        //TuanNM remove: using noo round
        //return (Math.round(value * Math.pow(10, decimalPlace)) / (double) (Math.pow(10, decimalPlace)));
        return (Math.round(value * Math.pow(10, decimalPlace))) / (double) (Math.pow(10, decimalPlace));
    }

    public static int daysBetween(Calendar day1, Calendar day2) {
        Calendar dayOne = (Calendar) day1.clone(),
                dayTwo = (Calendar) day2.clone();

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
        } else {
            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
                //swap them
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            int extraDays = 0;

            int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
                dayOne.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays;
        }
    }



    public static Age calculateAge(long birthDate) {
        int years = 0;
        int months = 0;
        int days = 0;
        //create calendar object for birth day
        Calendar birthDay = Calendar.getInstance();
        birthDay.setTimeInMillis(birthDate);
        //Create new Age object
        LocalDate today = LocalDate.now();
        LocalDate birthdayDate = LocalDate.fromCalendarFields(birthDay);
        Period p = new Period(birthdayDate, today);
        LogUtils.e("Age ", "You are " + p.getYears() + " years, " + p.getMonths() +
                " months, and " + p.getDays() +
                " days old");
        years = p.getYears();
        months = p.getMonths();
        if (years < 0) {
            years = 0;
            months = 0;
        }
        if (months < 0) {
            months = 0;
        }
        return new Age(p.getDays(), months, years);
    }

    public static String getStringAge(Context context, long birthDate) {
        Age age = calculateAge(birthDate);
        String ageString = "";
        ageString = ageString + age.getYears() + context.getString(R.string.age_year);
        if (SharedPreferencesHelper.getIntValue(AppConstants.LANGUAGE_ID) == AppConstants.LANG_VN) {
            ageString = ageString + " ";
        }
        ageString = ageString + age.getMonths() + context.getString(R.string.age_month);

        if (!TextUtils.isEmpty(ageString)) {
            ageString = " (" + ageString + ")";
        }
        return ageString;
    }

    public static boolean isNumber(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    //    public static boolean isNumberic(String str) {
//        NumberFormat numberFormat = NumberFormat.getInstance();
//        ParsePosition parsePosition = new ParsePosition(0);
//        numberFormat.parse(str, parsePosition);
//        return str.length() == parsePosition.getIndex();
//    }
    public static GsonBuilder createGson(GsonBuilder builder) {
        return builder
                .registerTypeAdapter(Integer.class, new IntTypeAdapter())
                .registerTypeAdapter(int.class, new IntTypeAdapter())
                .registerTypeAdapter(Api101.class, new Api101JsonSerializer())
                .registerTypeAdapter(AppMemo.class, new AppMemoTypeAdapter());

    }

    public static GsonBuilder createGsonApi101(GsonBuilder builder) {
        return builder
                .registerTypeAdapter(Integer.class, new IntTypeAdapter())
                .registerTypeAdapter(int.class, new IntTypeAdapter())
                .registerTypeAdapter(AppMemo.class, new AppMemoTypeAdapter());

    }

    public static String formatDate(Context context, long timeinmillis) {
        if (timeinmillis <= 0) {
            return "";
        } else {
            HashMap<String, String> hashMap = Utils.getDateTimeHashMap(timeinmillis, 1, 2);
            String date = hashMap.get(AppConstants.YEAR) + "/" + hashMap.get(AppConstants.MONTH) + "/" + hashMap.get(AppConstants.DATE);
            String hour = hashMap.get(AppConstants.HOUR) + context.getString(R.string.hour) + hashMap.get(AppConstants.MINUTE) + context.getString(R.string.minute);
            if (SharedPreferencesHelper.getIntValue(AppConstants.LANGUAGE_ID) == AppConstants.LANG_VN) {
                date = hashMap.get(AppConstants.DATE) + "/" + hashMap.get(AppConstants.MONTH) + "/" + hashMap.get(AppConstants.YEAR);
                hour = hashMap.get(AppConstants.HOUR) + context.getString(R.string.hour) + " " + hashMap.get(AppConstants.MINUTE) + context.getString(R.string.minute);
            }
            return date + " " + hour;
        }
    }

    public static String formatDatePhoto(Context context, long timeinmillis) {
        if (timeinmillis <= 0) {
            return "";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            if (SharedPreferencesHelper.getIntValue(AppConstants.LANGUAGE_ID) == AppConstants.LANG_VN) {
                sdf = new SimpleDateFormat("dd/MM/yyyy");
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeinmillis);
            return sdf.format(calendar.getTime());
        }
    }

    public static String convertToHighestTime(Context context, long sumDuration) {
        int[] sepTime = BindingAdapterUtils.getSeparateTime(sumDuration);
        StringBuilder stringBuilder = new StringBuilder();
        if (sepTime[0] > 0) {
            stringBuilder.append(Utils.formatCurrency(sepTime[0])).append(context.getString(R.string.hour_2));
            if (sepTime[1] > 0) {
                stringBuilder.append(sepTime[1]).append(context.getString(R.string.minute));
            }
        } else {
            stringBuilder.append(sepTime[1]).append(context.getString(R.string.minute));
        }

        return stringBuilder.toString();
    }

    public static String convertToHighestTimeWithSeconds(Context context, long sumDuration) {
        int[] sepTime = BindingAdapterUtils.getSeparateTime(sumDuration);
        StringBuilder stringBuilder = new StringBuilder();
        if (sepTime[0] > 0) {
            stringBuilder.append(sepTime[0]).append(context.getString(R.string.hour_2));
        }
        stringBuilder.append(sepTime[1]).append(context.getString(R.string.minute));
        if (SharedPreferencesHelper.getIntValue(AppConstants.LANGUAGE_ID) == AppConstants.LANG_VN) {
            stringBuilder.append(" ");
        }
        stringBuilder.append(sepTime[2]).append(context.getString(R.string.second));


        return stringBuilder.toString();
    }

    private static CharSequence filterBody(CharSequence source, int start, int end, Spanned dest, int dstart, int dend, final EditText text, final int MAX_VALUE, final int MIN_VALUE){
        if (SharedPreferencesHelper.getBooleanValue(AppConstants.UNDER_SETTING_TEXT_EDITTEXT)){
            SharedPreferencesHelper.storeBooleanValue(AppConstants.UNDER_SETTING_TEXT_EDITTEXT, false);
            return null;
        }

        CharSequence originalSource = source;

        StringBuilder stringBuilder = new StringBuilder(text.getText().toString());
        stringBuilder.replace(dstart, dend, source.toString());

        String temp = stringBuilder.toString();

        if (source.toString().contains("-"))    return "";

        if (text.getInputType() == (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
                || text.getInputType() == (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED)) {
            if (temp.equals("-"))   return null;
            int posDot = temp.indexOf('.');
            if (posDot == 0){
                source =  "0" + source;
                stringBuilder = new StringBuilder(text.getText().toString());
                stringBuilder.replace(dstart, dend, source.toString());
                temp = stringBuilder.toString();
                end++;
            }
            if (posDot > 0){
                char beforeDot = temp.charAt(posDot - 1);
                if (beforeDot == '-'){
                    source =  "0" + source;
                    stringBuilder = new StringBuilder(text.getText().toString());
                    stringBuilder.replace(dstart, dend, source.toString());
                    temp = stringBuilder.toString();
                    end++;
                }
            }
        }
        if (text.getInputType() == InputType.TYPE_CLASS_NUMBER && source.toString().contains(".")){
            return "";
        }
        if (Utils.isNumber(temp)){
            double val = Double.parseDouble(temp);
            //neu ngoai range min max thi ko nhap
            if (val > MAX_VALUE || val < MIN_VALUE) return "";
            //
            String str = temp;
            str = str.trim();
            str = trimZeroPrefix(str);
            str = trimZeroSuffix(str);
            //truong hop danh cho 0.4 va 0.400000 ~ 0.40004
            if (str == null)    return "";
            if (str.equals(text.getText().toString())){
                return text.getText().toString().substring(dstart,dend);
            }
            if (!str.equals(temp)){
                SharedPreferencesHelper.storeBooleanValue(AppConstants.UNDER_SETTING_TEXT_EDITTEXT, true);
                text.setText(str);
                int select = dstart + end - temp.length() + str.length();
                if (select <= str.length() && select >= 0){
                    text.setSelection(select);
                }
                return "";
            }
        }else{
            return "";
        }
        if (!originalSource.toString().equals(source))   return source;
        return null;
    }

    private static InputFilter[] getInputFilter(final EditText text, final int MAX_VALUE,
                                                final int MIN_VALUE, boolean hasNegative, boolean hasDecimal){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return new InputFilter[]{new DigitsKeyListener(hasNegative, hasDecimal) {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    CharSequence result = filterBody(source,start,end,dest,dstart,dend,text,MAX_VALUE,MIN_VALUE);
                    if (result == null){
                        return super.filter(source, start, end, dest, dstart, dend);
                    }else{
                        return result;
                    }
                }
            }};
        }else{
            return new InputFilter[]{new DigitsKeyListener(Locale.US,false, true) {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    CharSequence result = filterBody(source,start,end,dest,dstart,dend,text,MAX_VALUE,MIN_VALUE);
                    if (result == null){
                        return super.filter(source, start, end, dest, dstart, dend);
                    }else{
                        return result;
                    }
                }
            }};
        }
    }

  

    private static String trimZeroSuffix(String str) {
        int indexDot = str.indexOf('.');
        if (indexDot > 0 && str.length() > indexDot + 1){
            double acceptedValue = Double.parseDouble(str.substring(0, indexDot + 2));
            double fullValue = Double.parseDouble(str);
            if (fullValue != acceptedValue)  return null;
            return str.substring(0, indexDot + 2);
        }else{
            return str;
        }

    }

    private static String trimZeroPrefix(String temp) {
        int indexZero = temp.indexOf('0');
        if (indexZero == 0 && temp.length() > 1){
            String newStr = temp.substring(1);
            if (newStr.indexOf('.') == 0){
                return temp;
            }else{
                return trimZeroPrefix(newStr);
            }
        }
        int indexNegative = temp.indexOf('-');
        if (indexZero == 1 && indexNegative == 0 && temp.length() > 2){
            String newStr = temp.substring(2);
            if (newStr.indexOf('.') == 0){
                return temp;
            }else{
                return trimZeroPrefix("-" + newStr);
            }
        }
        return temp;
    }

   

    public static Point getSizeScreen(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else {
            display.getSize(size); // correct for devices with hardware navigation buttons
        }
        return size;
    }

    public static String readKernelVersion() {
        try {

            Process p = Runtime.getRuntime().exec("uname -s -m", null, null);
            InputStream is = null;
            if (p.waitFor() == 0) {
                is = p.getInputStream();
            } else {
                is = p.getErrorStream();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is),
                    1024);
            String line = br.readLine();
            br.close();
            return line;
        } catch (Exception ex) {
            final String[] abis;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                abis = Build.SUPPORTED_ABIS;
            } else {
                abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
            }
            String ext = " ";
            for (String abi : abis) {
                if (TextUtils.equals(abi, "x86_64")) {
                    ext = "amd64";
                    break;
                } else if (TextUtils.equals(abi, "x86")) {
                    ext = "x86";
                    break;
                } else if (TextUtils.equals(abi, "armeabi-v7a")) {
                    ext = "armeabi-v7a";
                    break;
                }
            }
            return System.getProperty("os.name") + " " + ext;
        }
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static int getTextWidth(Context context, String text, int textSize, int deviceWidth) {
        TextView textView = new TextView(context);
        textView.setText(text);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(0, 0);
        return textView.getMeasuredWidth();
    }


    public static void saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
//        Random generator = new Random();
//        int n = 10000;
//        n = generator.nextInt(n);
        String fname = "Image-" + Calendar.getInstance().getTime().toString() + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void goStore(Context context, Intent i) {
        if (context == null) return;
        String packageName = BuildConfig.APPLICATION_ID;
        Intent intent;
        if (i != null) intent = i;
        else intent = new Intent();

        intent.setAction(Intent.ACTION_VIEW);
        try {
            intent.setData(Uri.parse("market://details?id=" + packageName));
            context.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            context.startActivity(intent);
        } catch (AndroidRuntimeException are) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            goStore(context, intent);
        }
    }

    public static long getTimeFromExif(Context context, Uri uri) {
        InputStream inputStream;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            android.support.media.ExifInterface exifInterface = null;
            if (inputStream != null) {
                exifInterface = new android.support.media.ExifInterface(inputStream);
                if (exifInterface.getDateTime() != -1) {
                    return exifInterface.getDateTime();
                }
                return (new File(uri.getPath())).lastModified();
//                Log.i("hieu", "getDateTime() -> " + exifInterface.getDateTime());
//                Log.i("hieu", "The date and time of image creation.  -> " + exifInterface.getAttribute(android.support.media.ExifInterface.TAG_DATETIME));
//                Log.i("hieu", "The date and time when the image was stored as digital data. -> " + exifInterface.getAttribute(android.support.media.ExifInterface.TAG_DATETIME_DIGITIZED));
//                Log.i("hieu", "The date and time when the original image data was generated. -> " + exifInterface.getAttribute(android.support.media.ExifInterface.TAG_DATETIME_ORIGINAL));
//                Log.i("hieu", "File - last modified. -> " + (new File(uri.getPath())).lastModified());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis();
    }

    public static android.support.media.ExifInterface getExifInterface(Context context, Uri uri) {
        InputStream inputStream;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            android.support.media.ExifInterface exifInterface = null;
            if (inputStream != null) {
                exifInterface = new android.support.media.ExifInterface(inputStream);
                return exifInterface;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static CharSequence trimLeadingZeros(CharSequence source, String oldString) {
        int length = source.length();
        boolean containDot = oldString.contains(".");
        if (length < 2)
            return source;
        /// check leading zero;

        int i;
        for (i = 0; i < length; i++) {
            char c = source.charAt(i);
            if (c != '0') {
                if (!containDot && c == '.') {
                    i--;
                    break;
                }
                break;
            }

        }
        LogUtils.e(" Leading zero", " Leading zero" + source + "/// index" + i);

        if (i == 0)
            return trimDecimalZeros(source, oldString);
        if (i < 0 || i >= length)
            return "";

        return trimDecimalZeros(source.subSequence(i, length), oldString);
    }

    private static CharSequence trimDecimalZeros(CharSequence source, String oldString) {
        int length = source.length();
        boolean containDot = oldString.contains(".");
        LogUtils.e(" trailing zero", " trailing zero" + source);

        if (length < 2)
            return source;
        /// check  zero;

        int i;
        if (!containDot) {
            String sourceTemp = source.toString();
            if (sourceTemp.contains(".")) {
                int indexDot = sourceTemp.indexOf(".");
                if (indexDot <= length - 2) {
                    for (i = indexDot + 2; i < length; i++) {
                        char c = source.charAt(i);
                        if (c != '0') {
                            break;
                        }

                    };
                    if (i == length) {
                        return source.subSequence(0, indexDot + 2);
                    } else
                        return "";

                } else
                    return source.subSequence(0, indexDot + 1);


            }
        } else {
            String sourceTemp = source.toString();
            if (sourceTemp.contains(".")) {
                return "";
            } else {
                if(length<2)
                    return source;
                else
                    return "";
            }

        }

        return source;


    }


}
