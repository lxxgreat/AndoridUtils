
package com.miui.common;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.widget.Toast;

import com.miui.common.NotificationHelper.NotificationKey;

import miui.os.Build;
import miui.telephony.PhoneNumberUtils;
import miui.telephony.TelephonyManager;
import miui.text.ExtraTextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

public final class AndroidUtils {

    // 虚拟ROOT包名
    public static final String ROOT_PKG_NAME = "root";

    // 虚拟ROOT Label
    public static final CharSequence ROOT_PKG_LABEL = "root";

    // 虚拟SHELL包名
    public static final String SHELL_PKG_NAME = "com.android.shell";

    // 虚拟SHELL Label
    public static final CharSequence SHELL_PKG_LABEL = "Interactive Shell";

    /**
     * File path = Environment.getExternalStorageDirectory()
     * 
     * @return B
     */
    public static long getExternalStorageDirectoryTotalSize() {
        if (isExternalStorageWritable()) {
            File path = Environment.getExternalStorageDirectory();
            return getStorageDirectoryTotalSize(path.getPath());
        } else {
            return 0;
        }
    }

    /**
     * File path = Environment.getExternalStorageDirectory()
     * 
     * @return B
     */
    public static long getExternalStorageDirectoryAvailableSize() {
        if (isExternalStorageWritable()) {
            File path = Environment.getExternalStorageDirectory();
            return getStorageDirectoryAvailableSize(path.getPath());
        } else {
            return 0;
        }
    }

    /**
     * File path = Environment.getDataDirectory()
     * 
     * @return B
     */
    public static long getInternalStorageDirectoryTotalSize() {
        File path = Environment.getDataDirectory();
        return getStorageDirectoryTotalSize(path.getPath());
    }

    /**
     * File path = Environment.getDataDirectory()
     * 
     * @return B
     */
    public static long getInternalStorageDirectoryAvailableSize() {
        File path = Environment.getDataDirectory();
        return getStorageDirectoryAvailableSize(path.getPath());
    }

    /**
     * @return B
     */
    public static long getStorageDirectoryTotalSize(String path) {
        try {
            // 取得sdcard文件路径
            StatFs statfs = new StatFs(path);
            // 获取block的SIZE
            long blocSize = statfs.getBlockSize();
            // 获取BLOCK数量
            long totalBlocks = statfs.getBlockCount();
            return totalBlocks * blocSize;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @return B
     */
    public static long getStorageDirectoryAvailableSize(String path) {
        try {
            StatFs statfs = new StatFs(path);
            // 获取block的SIZE
            long blocSize = statfs.getBlockSize();
            // 己使用的Block的数量
            long availaBlock = statfs.getAvailableBlocks();
            return availaBlock * blocSize;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean deleteFile(File file) {
        if (file == null) {
            return false;
        }
        return deleteFile(file.getPath());
    }

    public static boolean deleteFile(String filePath) {
        return deleteFile(filePath, false);
    }

    public static boolean deleteFile(String filePath, boolean onlyFile) {
        if (TextUtils.isEmpty(filePath)) {
            return true;
        }
        File file = new File(filePath);
        if (file.isFile()) {
            return file.delete();
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return file.delete();
            }

            for (int i = 0; i < childFiles.length; i++) {
                deleteFile(childFiles[i].getPath());
            }
            if (!onlyFile) {
                return file.delete();
            }
        }
        return true;
    }

    /**
     * @param context
     * @param packageName target app package name
     * @return delete success
     * @throws NameNotFoundException
     */
    public static boolean deleteAppCache(Context context, String packageName)
            throws NameNotFoundException {
        Context targetContext = context.createPackageContext(packageName,
                Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        return deleteFile(targetContext.getCacheDir());
    }

    /**
     * @param context
     * @param apkPath apk文件绝对路径
     */
    public static void installApk(Context context, String apkPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://" + apkPath);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(uri, type);
        context.startActivity(intent);
    }

    /**
     * @param context
     * @param packageName 需要卸载的应用包名
     */
    public static void uninstallApk(Context context, String packageName) {
        if (isInstalledPackage(context, packageName)) {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            Uri uri = Uri.parse("package:" + packageName);
            intent.setData(uri);
            context.startActivity(intent);
        }
    }

    /**
     * 判断应用是否安装，通过是否报错判断，未安装会报错
     * 
     * @param context
     * @param packageName
     * @return true installed false uninstalled
     */
    public static boolean isInstalledPackage(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);

            return info != null;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param context
     * @param packageName
     * @return
     */
    public static PackageInfo getPackageInfoByName(Context context, String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PackageInfo getPackageInfoByPath(Context context, String packagePath) {
        if (packagePath == null) {
            return null;
        }
        PackageManager pkgManager = context.getPackageManager();
        PackageInfo packageInfo = pkgManager.getPackageArchiveInfo(
                packagePath, PackageManager.GET_ACTIVITIES);
        if (packageInfo == null) {
            return null;
        }
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        applicationInfo.sourceDir = packagePath;
        applicationInfo.publicSourceDir = packagePath;

        return packageInfo;
    }

    public static CharSequence getAppName(Context context, String pkgName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(pkgName, 0);
            if (packageInfo != null) {
                return packageInfo.applicationInfo.loadLabel(pm);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return pkgName;
    }

    private static final long UNIT = 1000;

    public static final long KB = ExtraTextUtils.KB;
    public static final long MB = ExtraTextUtils.MB;
    public static final long GB = ExtraTextUtils.GB;
    public static final long TB = ExtraTextUtils.GB * UNIT;

    public static String formatFileSizeWithoutSuffix(long number) {
        String suffix = null;
        float result = number;
        if (result > 900) {
            result = result / UNIT;
        }
        if (result > 900) {
            result = result / UNIT;
        }
        if (result > 900) {
            result = result / UNIT;
        }
        if (result > 900) {
            result = result / UNIT;
        }
        if (result > 900) {
            result = result / UNIT;
        }
        String value;
        if (result < 1) {
            value = String.format("%.2f", result);
        } else if (result < 10) {
            value = String.format("%.2f", result);
        } else if (result < 100) {
            value = String.format("%.2f", result);
        } else {
            value = String.format("%.0f", result);
        }
        return value;
    }

    public static String getFileSizeSuffix(long number) {
        float result = number;
        String suffix = "B";
        if (result > 900) {
            suffix = "KB";
            result = result / UNIT;
        }
        if (result > 900) {
            suffix = "MB";
            result = result / UNIT;
        }
        if (result > 900) {
            suffix = "GB";
            result = result / UNIT;
        }
        if (result > 900) {
            suffix = "TB";
            result = result / UNIT;
        }
        if (result > 900) {
            suffix = "PB";
            result = result / UNIT;
        }
        return suffix;
    }

    public static int getFormatIntSizeMaxM(long number) {
        if (number < KB) {
            return (int) number;
        } else if (number < MB) {
            return (int) (number / KB);
        } else {
            return (int) (number / MB);
        }
    }

    public static String getFormatIntSuffixMaxM(long number) {
        if (number < KB) {
            return "B";
        } else if (number < MB) {
            return "KB";
        } else {
            return "MB";
        }
    }

    public static String getFormatMaxM(long number) {
        int size = 0;
        if (number < KB) {
            size = (int) number;
        } else if (number < MB) {
            size = (int) (number / KB);
        } else {
            size = (int) (number / MB);
        }

        if (number < KB) {
            return size + "B";
        } else if (number < MB) {
            return size + "KB";
        } else {
            return size + "MB";
        }
    }

    /**
     * 杀死后台进程，包括系统进程
     * 
     * @param context
     */
    public static void killBackgroundProcess(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo info : processes) {
            if (info != null && info.processName != null && info.processName.length() > 0) {
                try {
                    am.killBackgroundProcesses(info.processName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 把bitmap转换成byte[]
     * 
     * @param bmp
     * @return null if exception occur
     */
    public static byte[] bitmapToBytes(Bitmap bmp) {
        if (bmp == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * @param bmp
     * @return
     */
    public static Drawable bitmapToDrawable(Bitmap bmp) {
        if (bmp != null) {
            return new BitmapDrawable(bmp);
        } else {
            return null;
        }
    }

    /**
     * 当读去res文件加下的图片时
     * 
     * @param bmp
     * @return
     */
    public static Drawable bitmapToDrawable(Resources res, Bitmap bmp) {
        if (bmp != null) {
            return new BitmapDrawable(res, bmp);
        } else {
            return null;
        }
    }

    /**
     * @param bytes
     * @return
     */
    public static Bitmap bytesToBitmap(byte[] bytes) {
        if (bytes.length != 0) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    /**
     * @param bytes
     * @return
     */
    public static Drawable bytesToDrawable(byte[] bytes) {
        if (bytes.length != 0) {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return bitmapToDrawable(bmp);
        } else {
            return null;
        }
    }

    /**
     * @param drawable
     * @return
     */
    public static byte[] drawableToBytes(Drawable drawable) {
        if (drawable != null) {
            return bitmapToBytes(drawableToBitmap(drawable));
        } else {
            return null;
        }
    }

    /**
     * Checks if external storage is available for read and write
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available to at least read
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * @param targetTime
     * @return days
     */
    public static int getIntervalDays(long targetTime) {
        long nowInMillis = System.currentTimeMillis();
        long interval = nowInMillis - targetTime;
        long day = 24 * 60 * 60 * 1000;
        return (int) (interval / day);
    }

    /**
     * @param original
     * @param color
     * @param targets
     * @return
     */
    public static SpannableString getHighLightString(String original, int color,
            String... targets) {
        SpannableString sp = new SpannableString(original);
        try {
            for (String target : targets) {
                int start = original.indexOf(target);
                int end = start + target.length();
                sp.setSpan(new ForegroundColorSpan(color), start, end,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sp;
    }

    /**
     * give the targets accroding to the order in original
     * 
     * @param original
     * @param color
     * @param targets
     * @return
     */
    public static SpannableString getHighLightStringInOrder(String original, int color,
            String... targets) {
        SpannableString sp = new SpannableString(original);
        int endOfSub = 0;
        int end = 0;
        try {
            for (String target : targets) {
                original = original.substring(endOfSub);
                int start = original.indexOf(target) + end;
                endOfSub = original.indexOf(target) + target.length();
                end = start + target.length();
                sp.setSpan(new ForegroundColorSpan(color), start, end,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sp;
    }

    /**
     * @param context
     * @param pkgName package name
     * @return null or ic_launcher
     */
    public static Drawable getAppLauncher(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(pkgName, PackageManager.PERMISSION_GRANTED);
            if (info != null) {
                return info.applicationInfo.loadIcon(pm);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Toast.LENGTH_SHORT
     * 
     * @param context
     * @param resId
     */
    public static void showShortToast(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

    /**
     * Toast.LENGTH_LONG
     * 
     * @param context
     * @param resId
     */
    public static void showLongToast(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
    }

    /**
     * Toast.LENGTH_SHORT
     * 
     * @param context
     * @param resId
     */
    public static void showShortToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Toast.LENGTH_LONG
     * 
     * @param context
     * @param resId
     */
    public static void showLongToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    /**
     * 判断网络是否可用，包括2G，3G，WIFI
     * 
     * @param context
     * @return
     */
    public static boolean isNetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否仅WIFI可用
     * 
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = cm.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            return activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) == false);
    }

    /**
     * 如果是一ICON开头的String，可以使用该方法,其他不可以
     * 
     * @param content
     * @param drawable
     * @return
     */
    public static SpannableString getHeaderIconSpannableString(String content, Drawable drawable) {
        SpannableString ss = new SpannableString(content);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        // 0 - 4 is icon
        ss.setSpan(new ImageSpan(drawable), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    /**
     * 把mask加在target上
     * 
     * @param res
     * @param target
     * @param mask
     * @return
     */
    public static Drawable getMaskDrawable(Resources res, Drawable target, Drawable mask) {
        int targetWidth = target.getIntrinsicWidth();
        int targetHeight = target.getIntrinsicHeight();
        int maskWidth = mask.getIntrinsicWidth();
        int maskHeight = mask.getIntrinsicHeight();

        BitmapDrawable targetDrawable = (BitmapDrawable) target;
        Bitmap targetBitmap = targetDrawable.getBitmap();

        BitmapDrawable maskDrawable = (BitmapDrawable) mask;
        Bitmap maskBitmap = maskDrawable.getBitmap();

        Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        canvas.save();
        canvas.drawBitmap(targetBitmap, 0, 0, null);
        canvas.drawBitmap(maskBitmap, targetWidth / 2 - maskWidth / 2, targetWidth / 2 - maskHeight
                / 2, null);
        canvas.restore();

        return new BitmapDrawable(res, bitmap);
    }

    public static CharSequence loadAppLabel(Context context, String pkgName) {
        if (ROOT_PKG_NAME.equals(pkgName))
            return ROOT_PKG_LABEL;
        if (SHELL_PKG_NAME.equals(pkgName))
            return SHELL_PKG_LABEL;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
            return appInfo.loadLabel(pm);
        } catch (NameNotFoundException e) {
            // ignore
        }
        return pkgName;
    }

    public static boolean isThirdPartApp(ApplicationInfo info) {
        if (info.uid >= 10000 && (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            return true;
        }
        return false;
    }

    public static boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    public static boolean isVoiceCapable(Context context) {
        TelephonyManager telephony = TelephonyManager.getDefault();
        return telephony != null && telephony.isVoiceCapable();
    }

    public static void cancelNotification(Context context, String notificationKey) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NotificationHelper.getInstance(context).getNotificationIdByKey(notificationKey));
    }

    public static void rebootPhone(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.reboot(null);
    }

    private static final String ACCOUNT_TYPE_STR = "com.xiaomi";
    private static final String EMPTY_STR = "";

    public static String getAccountId(Context context) {
        AccountManager am = AccountManager.get(context);
        if (am == null) {
            return EMPTY_STR;
        }
        Account[] accountArr = am.getAccountsByType(ACCOUNT_TYPE_STR);
        if (accountArr == null || accountArr.length == 0) {
            return EMPTY_STR;
        }
        String accountId = accountArr[0].name;
        if (TextUtils.isEmpty(accountId)) {
            return EMPTY_STR;
        }
        return accountId;
    }

    public static String getLocation(Context context) {
        TelephonyManager tm = TelephonyManager.getDefault();
        if (tm == null) {
            return EMPTY_STR;
        }
        String phoneNumber = tm.getLine1Number();
        if (TextUtils.isEmpty(phoneNumber)) {
            return EMPTY_STR;
        }
        String location = PhoneNumberUtils.PhoneNumber.getLocation(context, phoneNumber);
        return location;
    }

    public static String getAreaCode(Context context) {
        TelephonyManager tm = TelephonyManager.getDefault();
        if (tm == null) {
            return EMPTY_STR;
        }
        String phoneNumber = tm.getLine1Number();
        if (TextUtils.isEmpty(phoneNumber)) {
            return EMPTY_STR;
        }
        String areaCode = PhoneNumberUtils.PhoneNumber.getLocationAreaCode(context, phoneNumber);
        return areaCode;
    }

    public static String getImeiCode(Context context) {
        String imeiCode = TelephonyManager.getDefault().getDeviceId();
        if (imeiCode == null) {
            return EMPTY_STR;
        }
        return imeiCode;
    }

    public static final String getMiuiVersionType() {
        if (Build.IS_STABLE_VERSION) {
            return "stable";
        }
        else if (Build.IS_DEVELOPMENT_VERSION) {
            return "development";
        }
        else {
            return "alpha";
        }
    }

    public static String getDeviceId(Context context) {
        String androidId = null;
        try {
            androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(androidId)) {
            return EMPTY_STR;
        }
        return androidId;
    }
}
