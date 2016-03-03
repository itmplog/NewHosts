package top.itmp.newhosts.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import top.itmp.newhosts.NewHostsActivity;
import top.itmp.newhosts.NewHostsFragment;
import top.itmp.newhosts.R;

/**
 * Created by hz on 2016/3/3.
 */
public class Utils {
    //public static SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(activity);
    //public static SharedPreferences.Editor sPEditor = PreferenceManager.getDefaultSharedPreferences(activity).edit();

    public static String getLanguage(Context context){
        String language = getSp(context).getString("language", null);
       //return getSp().contains("language") ? Locale.getDefault().getLanguage() : getSp().getString("language", null);
        if(language == null){
            language = Locale.getDefault().getLanguage();
            getSpEditor(context).putString("language", language).commit();
        }
        return language;
    }

    public static void setLocale(Context context){
        Locale locale = new Locale(getLanguage(context));
        locale.setDefault(locale);
        Configuration conf = new Configuration();
        conf.locale = locale;
        context.getResources().updateConfiguration(conf, context.getResources().getDisplayMetrics());
    }

    public static SharedPreferences getSp(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences.Editor getSpEditor(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).edit();
    }

    public static void runOnUiThread(final Activity activity, final String toast){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity.getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void checkHostsVersionInfo(final Context context, TextView version){
        File file = new File("/system/etc/hosts");
        if(!file.exists()){
            version.setText("Last Modified Time:\n\t" + "The hosts files doesn't exists.");
        } else {
            version.setText("Last Modified Time:\n\t" + new Date(file.lastModified()));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateMd5(context);
                }
            }).start();
        }
        String checkedUpdate = getSp(context).getString("update", null);
        //PreferenceManager.getDefaultSharedPreferences(main).getString("update", null);
        if(checkedUpdate != null){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            try {
                Date checked = simpleDateFormat.parse(checkedUpdate);
                version.append("\nServer Update Time:\n\t" + checked);
            }catch (ParseException e){
                e.printStackTrace();
            }
        }
    }

    public static void updateMd5(Context context){
        String md5 = getSp(context).getString("hosts", null);
        File hosts_file = new File("/system/etc/hosts");
        if(!hosts_file.exists()){
            runOnUiThread((Activity)context, context.getString(R.string.hosts_not_exists));
            return;
        }
        String hosts_md5 = MD5.calculateMD5(hosts_file);
        if(md5 == null || md5 != null && !hosts_md5.equalsIgnoreCase(md5)){
            getSpEditor(context).putString("hosts", hosts_md5).commit();
        }
    }

    public static void resetHosts(final Context context){
        new Thread(new Runnable() {
            public void run() {
                File original = new File(context.getFilesDir().toString() + File.separator + "original");

                if (original.getParentFile().exists()) {
                    original.getParentFile().mkdir();
                }
                String originalMd5 = getSp(context).getString("original", null);
                // PreferenceManager.getDefaultSharedPreferences(main).getString("original", null);
                if (!original.exists() || original.exists() && !MD5.checkMD5(originalMd5, original)) {
                    //original.delete();
                    Log.v("vvv", "hehe");
                    OutputStream out = null;

                    try {
                        out = new FileOutputStream(original);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    try {
                        String tmp = "127.0.0.1 localhost\n" +
                                "::1 localhost";
                        out.write(tmp.getBytes());
                        out.close();
                        getSpEditor(context).putString("original", MD5.calculateMD5(original)).commit();
                        //PreferenceManager.getDefaultSharedPreferences(main).edit().putString("original", MD5.calculateMD5(original)).commit();
                        //debug
                        /*if(!original.exists()){
                            main.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(main.getApplicationContext(), "No no no", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }*/
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                File hosts = new File("/system/etc/hosts");
                //final String md5 = MD5.calculateMD5(original);
                final String md5 = getSp(context).getString("original", null);

                if (hosts.exists() && MD5.checkMD5(md5, hosts)) {
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context.getApplicationContext(), context.getString(R.string.already_reset), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        Process process = Runtime.getRuntime().exec("/system/xbin/su");
                        DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

                        outputStream.writeBytes("/system/bin/mount -o rw,remount /system && /system/bin/cp " + context.getFilesDir().toString() + File.separator + "original" + " /system/etc/hosts"
                                + "&& /system/bin/chmod 644 /system/etc/hosts && chown root:root /system/etc/hosts\n");
                        outputStream.writeBytes("sync");

                        outputStream.writeBytes("exit\n");
                        outputStream.flush();
                        //process.waitFor();
                        outputStream.close();
                        Thread.sleep(1000);
                        // checkMD5


                        if (!hosts.exists()) {
                            ((Activity)context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context.getApplicationContext(), context.getString(R.string.reset_host_failed), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else if (MD5.checkMD5(md5, hosts)) {
                            ((Activity)context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context.getApplicationContext(), context.getString(R.string.reset_done) + "\nmd5: " + md5, Toast.LENGTH_SHORT).show();
                                    checkHostsVersionInfo(context, NewHostsFragment.versionText());
                                }
                            });

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static String versionInfo(Context context){
        String version = "";
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pi.versionName + "-" + pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }
}
