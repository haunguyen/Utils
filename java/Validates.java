package vn.mclab.nursing.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by manhtuan on 5/28/2015.
 */
public class Validates {

    public static boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        Pattern p = Pattern.compile(ePattern);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static boolean isValidatePassword(String pass) {
        if ((pass.trim().length() > 32) || (pass.trim().length() <= 8)) {
            return false;
        }
        return true;
    }

    public static boolean isValidateNickname(String nickName) {
        if (nickName.trim().length() > 10) {
            return false;
        }
        return true;
    }

    public static boolean isMatchPassword(String pass1, String pass2) {
        if (!pass1.equals(pass2)) {
            return false;
        }
        return true;
    }
}
