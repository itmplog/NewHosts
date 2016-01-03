package com.example.hz.newhosts;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceCategory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private TextView downloads = null;
    private Button btn = null;
    private Button deleteHosts = null;
    final int REQUEST_CODE_ASK_PERMISSIONS = 123;


    public String runAsRoot(String[] cmds, boolean hasOutput) throws Exception {
        Process p = Runtime.getRuntime().exec("su");
        DataOutputStream os = new DataOutputStream(p.getOutputStream());
        InputStream is = p.getInputStream();
        String result = null;
        for (String tmpCmd : cmds) {
            os.writeBytes(tmpCmd + "\n");
            int readed = 0;
            byte[] buff = new byte[4096];
           // boolean cmdRequiresAnOutput = true;
            if (hasOutput) {
                while (is.available() <= 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                    }
                }

                while (is.available() > 0) {
                    readed = is.read(buff);
                    if (readed <= 0) break;
                    result = new String(buff, 0, readed);
                   // result = seg; //result is a string to show in textview
                }
            }
        }
        os.writeBytes("exit\n");
        os.flush();
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloads = (TextView)findViewById(R.id.downloads);
        downloads.setMovementMethod(ScrollingMovementMethod.getInstance());

        File file = new File("/system/xbin/su");
        if(!file.exists()){
            downloads.setText("手机没有root， 无法进行更新hosts\n");
            return;
        }

        /* Add Tab at ActionBar
        *  isCaptivePortal()
        *  clients3.google.com/generate_204
        *  1. adb shell "settings put global captive_portal_detection_enabled 0"
        *  2. adb shell "settings put global captive_portal_server ii.itmp.top"
         */





        btn = (Button)findViewById(R.id.update);
        int hasWriteStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
           // return;
        }
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownTask task = new DownTask();

                try {
                    task.execute(new URL("https://raw.githubusercontent.com/racaljk/hosts/master/hosts"));   //, new URL("https://raw.githubusercontent.com/racaljk/hosts/master/hosts"));    // https://raw.githubusercontent.com/racaljk/hosts/master/hosts"));
                        /* Todo: update
                        *  https://api.github.com/repos/racaljk/hosts/commits?path=hosts&page=1&per_page=1
                        *  get the lastest commit id; and check if update existes;
                        *  json support;
                        */
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        deleteHosts = (Button)findViewById(R.id.delete);
        deleteHosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Process process = Runtime.getRuntime().exec("/system/xbin/su");
                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
                    InputStream is = process.getInputStream();

                    os.writeBytes("/system/bin/mount -o rw,remount /system && rm -rf /system/etc/hosts\n");
                    os.writeBytes("if [ ! -e /system/etc/hosts ]\n then echo -e \"delete /system/etc/hosts success.\n\"\nelse echo -e \"delete /system/etc/hosts failed.\"\nfi\n");
                    while (is.available() <= 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ex) {
                        }
                    }
                    while (is.available() > 0) {
                        int readed = 0;

                        byte[] buff = new byte[4096];
                        readed = is.read(buff);
                        Log.d("read", String.valueOf(readed));
                        if (readed <= 0) break;
                        String seg = new String(buff, 0, readed);
                        downloads.append(seg);
                        Log.d("reply", seg);
                    }
                    os.writeBytes("exit\n");
                    os.flush();
                    process.waitFor();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        /*   settings get global captive_portal_server  get the captive_portal_server;
        *    setting set global captive_portal_server "ii.itmp.top" can ignore this .
        *   get this .
        */

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), "更新hosts.",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "无法写入sd卡",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    class DownTask extends AsyncTask<URL, Integer, String>{
        ProgressDialog progressDialog;
        int length;
        int hasRead = 0;
        int i = 0;
        Context mContext;

        @Override
        protected String doInBackground(URL...params) {
            StringBuilder sb = new StringBuilder();
            try{
                URLConnection conn = params[0].openConnection();
                conn .setRequestProperty("Accept-Encoding", "identity");  //设置不使用 gzip 下载文件， 从而获得字节数
                conn.connect();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8")
                );
                length = conn.getContentLength();
                Log.d("debug", conn.getContentType() + "||" + String.valueOf(length));
                String line = null;


                while(( line = br.readLine()) != null){
                    sb.append(line + "\n");
                    hasRead++;


                    if(i == sb.length() * 100 / length) {
                        publishProgress(i++);

                        Log.d("percent", sb.length() + "!!" + length);
                    }
                }
                return sb.toString();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);
           // downloads.setText(s);
            downloads.setText("下载完成.\n");
            progressDialog.dismiss();
            //获取SDCard状态,如果SDCard插入了手机且为非写保护状态
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Log.d("sdcard", Environment.getExternalStorageDirectory().toString());
                File file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "hosts");
                if(!file.getParentFile().exists())
                    file.getParentFile().mkdirs();

                OutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                } catch(FileNotFoundException e){
                    e.printStackTrace();
                }
                try {
                    out.write(s.getBytes());
                }catch (IOException e){
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), "hosts下载完成， 保存在" + Environment.getExternalStorageDirectory().toString() + File.separator + "hosts", Toast.LENGTH_SHORT).show();
                try {
                    File su_file = new File("/system/xbin/su");
                    if(! su_file.exists()){
                        downloads.append("手机没有root， 无法替换hosts\n");
                        return;
                    }
                    Process process =  Runtime.getRuntime().exec("/system/xbin/su");
                    //os = new DataOutputStream(process.getOutputStream());
                    //is = new DataInputStream(process.getInputStream());
                    //BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    //String line = "";
                  //  Process execute = Runtime.getRuntime().exec("screenrecord --time-limit 10 /sdcard/MyVideo.mp4");

                    DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

                    // for respone message for textView;
                    InputStream is = process.getInputStream();
                    byte[] buff = new byte[1024];
                    int readed = 0;

                   // DataInputStream is = new DataInputStream(process.getInputStream());

                    // for android 5.x.x version

                    /*
                    outputStream.writeBytes("/system/xbin/mount -o rw,remount /system\n");
                    outputStream.writeBytes("/system/xbin/mv /sdcard/hosts /system/etc/hosts\n");
                    outputStream.writeBytes("/system/bin/chmod 644 /system/etc/hosts\n");
                    */

                    // for android 6.x.x
                    outputStream.writeBytes("/system/bin/mount -o rw,remount /system && /system/bin/mv " + Environment.getExternalStorageDirectory().toString() + File.separator + "hosts" + " /system/etc/hosts && /system/bin/chmod 644 /system/etc/hosts && chown root:root /system/etc/hosts\n");

                    Thread.sleep(1000);
                    File hosts_file = new File("/system/etc/hosts");
                    if(!hosts_file.exists()) {
                        downloads.append("/system/etc/hosts doesnot exists!\n");
                        outputStream.flush();
                        outputStream.writeBytes("exit\n");
                        return;
                    }

                    outputStream.writeBytes("stat -c \"%n %s\"bytes\"\n%z %U:%G\" /system/etc/hosts\n");
                    outputStream.writeBytes("ls -al /system/etc/hosts\n");
                    while(is.available() <= 0){
                        try{ Thread.sleep(1000);}catch (Exception e){}
                    }
                    while(is.available() > 0){
                        readed = is.read(buff);
                        if ( readed <= 0 ) break;
                        String seg = new String(buff,0,readed);
                        downloads.append(seg);
                    }

                    outputStream.flush();

                    outputStream.writeBytes("exit\n");
                    outputStream.flush();
                    process.waitFor();


                }catch (IOException e){
                    e.printStackTrace();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

        }

        @Override
        protected void onPreExecute() {
           /* //super.onPreExecute();
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
                DataInputStream is = new DataInputStream(process.getInputStream());

                outputStream.writeBytes("\n");
                outputStream.flush();

                outputStream.writeBytes("exit\n");
                outputStream.flush();
                process.waitFor();
            } catch (IOException e){
                e.printStackTrace();
            } catch (InterruptedException e){
                e.printStackTrace();
            } */
            mContext = MainActivity.this;
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle("下载正在进行中...");
            progressDialog.setMessage("下载正在进行中, 请等待....");
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            // Log.d("pre", length + "");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // 设置进度条风格
            progressDialog.setIndeterminate(false); // set the indeterminate for true  cause it will be downloaded so soon
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //super.onProgressUpdate(values);
            progressDialog.setMessage("已经下载了" + values[0] + "%...");
            downloads.setText("已经下载了 [ " + values[0] + "%]\n");
            progressDialog.setProgress(values[0]);
        }
    }
}
