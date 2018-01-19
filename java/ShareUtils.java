package vn.mclab.nursing.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import java.util.List;

import vn.mclab.nursing.base.App;

public class ShareUtils {
    public static final String TWITTER_PACKAGE_NAME = "com.twitter.android";
    public static final String LINE_PACKAGE_NAME = "jp.naver.line.android";
    public static final String FACEBOOK_PACKAGE_NAME = "com.facebook.katana";
    public static final String GMAIL_PACKAGE_NAME = "com.google.android.gm";

    public static boolean isAppInstalled(Context context, String packageName) {
        Intent mIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        return mIntent != null;
    }

    public static void shareViaGmail(Context mContext, String subject, String text) {
        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        tweetIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, text);
        tweetIntent.setType("text/plain");

        PackageManager packManager = mContext.getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith(GMAIL_PACKAGE_NAME)) {
                tweetIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            mContext.startActivity(tweetIntent);
        } else {
            try {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + GMAIL_PACKAGE_NAME)));
            } catch (android.content.ActivityNotFoundException anfe) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + GMAIL_PACKAGE_NAME)));
            }
        }
    }

    public static void shareTextFacebook(Context mContext, String text) {
        try {
            if (isAppInstalled(mContext, FACEBOOK_PACKAGE_NAME)) {
                if (null != text) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                    final PackageManager pm = mContext.getPackageManager();
                    final List<ResolveInfo> matches = pm.queryIntentActivities(sendIntent, 0);
                    ResolveInfo best = null;
                    for (final ResolveInfo info : matches) {
                        if (info.activityInfo.packageName.equals(FACEBOOK_PACKAGE_NAME) && info.activityInfo.name.contains("ImplicitShareIntentHandlerDefaultAlias")) {
                            best = info;
                            LogUtils.e("info", info.activityInfo.name + "");
                            break;
                        }
                    }
                    if (best != null) {
                        sendIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
                        mContext.startActivity(sendIntent);
                    }
                }
            } else {
                try {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + FACEBOOK_PACKAGE_NAME)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + FACEBOOK_PACKAGE_NAME)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shareTextTW(Context mContext, String text) {
        try {
            if (isAppInstalled(mContext, TWITTER_PACKAGE_NAME)) {
                if (null != text) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                    final PackageManager pm = mContext.getPackageManager();
                    final List<ResolveInfo> matches = pm.queryIntentActivities(sendIntent, 0);
                    ResolveInfo best = null;
                    for (final ResolveInfo info : matches) {
                        if (info.activityInfo.packageName.equals(TWITTER_PACKAGE_NAME) && info.activityInfo.name.contains("composer")) {
                            best = info;
                            LogUtils.e("info", info.activityInfo.name + "");
                            //break;
                        }
                    }
                    if (best != null) {
                        sendIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
                        mContext.startActivity(sendIntent);
                    }
                }
            } else {
                try {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + TWITTER_PACKAGE_NAME)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + TWITTER_PACKAGE_NAME)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, text);
        tweetIntent.setType("text/plain");

        PackageManager packManager = mContext.getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith(TWITTER_PACKAGE_NAME)) {
                tweetIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            mContext.startActivity(tweetIntent);
        } else {
            try {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + TWITTER_PACKAGE_NAME)));
            } catch (android.content.ActivityNotFoundException anfe) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + TWITTER_PACKAGE_NAME)));
            }
        }*/

    }

    public static void shareTextLine(Context mContext, String text) {
        try {
            if (isAppInstalled(mContext, LINE_PACKAGE_NAME)) {
                if (null != text) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                    final PackageManager pm = App.getAppContext().getPackageManager();
                    final List<ResolveInfo> matches = pm.queryIntentActivities(sendIntent, 0);
                    ResolveInfo best = null;
                    for (final ResolveInfo info : matches) {
                        if (info.activityInfo.packageName.equals(LINE_PACKAGE_NAME) && info.activityInfo.name.contains("SelectChatActivity")) {
                            best = info;
                            LogUtils.e("info", info.activityInfo.name + "");
                            break;
                        }
                    }
                    if (best != null) {
                        sendIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
                        mContext.startActivity(sendIntent);
                    }
                }
            } else {
                try {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + LINE_PACKAGE_NAME)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + LINE_PACKAGE_NAME)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
