package top.itmp.newhosts;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
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

/**
 * Created by hz on 2016/2/23.
 */
public class DownTask extends AsyncTask<URL, Integer, String> {
    ProgressDialog progressDialog;
    int length;
    int hasRead = 0;
    int i = 0;
    Context mContext;

    public DownTask(Context c){
        mContext = c;
    }

    public static void downHosts(Context context,String url){
        DownTask downTask = new DownTask(context);
        try{
            downTask.execute(new URL(url));
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }
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
        //downloads.setText("下载完成.\n");
        progressDialog.dismiss();
        //获取SDCard状态,如果SDCard插入了手机且为非写保护状态
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //Log.d("sdcard", Environment.getExternalStorageDirectory().toString());
            File file = new File(mContext.getFilesDir().toString() + File.separator + "hosts");
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

            Toast.makeText(mContext.getApplicationContext(), "hosts下载完成， 保存在" + mContext.getFilesDir().toString() + File.separator + "hosts", Toast.LENGTH_SHORT).show();
            try {
                File su_file = new File("/system/xbin/su");
                if(! su_file.exists()){
                    //downloads.append("手机没有root， 无法替换hosts\n");
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
                outputStream.writeBytes("/system/bin/mount -o rw,remount /system && /system/bin/cp " + mContext.getFilesDir().toString() + File.separator + "hosts" + " /system/etc/hosts && /system/bin/chmod 644 /system/etc/hosts && chown root:root /system/etc/hosts\n");

                Thread.sleep(1000);
                File hosts_file = new File("/system/etc/hosts");
                if(!hosts_file.exists()) {
                    //downloads.append("/system/etc/hosts doesnot exists!\n");
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
                    //downloads.append(seg);
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
        //downloads.setText("已经下载了 [ " + values[0] + "%]\n");
        progressDialog.setProgress(values[0]);
    }
}
