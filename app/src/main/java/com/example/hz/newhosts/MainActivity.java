package com.example.hz.newhosts;

import android.app.ProgressDialog;
import android.content.Context;
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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private TextView downloads = null;
    private Button btn = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloads = (TextView)findViewById(R.id.downloads);
        downloads.setMovementMethod(ScrollingMovementMethod.getInstance());
        btn = (Button)findViewById(R.id.update);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownTask task = new DownTask();

                try {
                    task.execute(new URL("https://raw.githubusercontent.com/racaljk/hosts/master/hosts"));   //, new URL("https://raw.githubusercontent.com/racaljk/hosts/master/hosts"));    // https://raw.githubusercontent.com/racaljk/hosts/master/hosts"));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }


    class DownTask extends AsyncTask<URL, Integer, String>{
        ProgressDialog progressDialog;
        int length;
        int hasRead = 0;
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


                while((line = br.readLine()) != null){
                    sb.append(line + "\n");
                    hasRead++;
                    publishProgress((sb.length()*100/length));
                    Log.d("percent", sb.length() + "!!" + length);
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
            downloads.setText(s);
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

                Toast.makeText(getApplicationContext(), "hosts下载完成， 保存在" + Environment.getExternalStorageDirectory().toString() + "/" + "hosts", Toast.LENGTH_SHORT).show();
                try {
                    Process process =  Runtime.getRuntime().exec("/system/xbin/su");
                    //os = new DataOutputStream(process.getOutputStream());
                    //is = new DataInputStream(process.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = "";
                  //  Process execute = Runtime.getRuntime().exec("screenrecord --time-limit 10 /sdcard/MyVideo.mp4");

                    DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
                   // DataInputStream is = new DataInputStream(process.getInputStream());

                    outputStream.writeBytes("mount -o rw,remount /system\n");
                    outputStream.writeBytes("mv /sdcard/hosts /system/etc/hosts\n");
                    outputStream.writeBytes("chmod 644 /system/etc/hosts\n");
                    outputStream.flush();

                    outputStream.writeBytes("exit\n");
                    outputStream.flush();
                    downloads.setText("");
                    while((line = reader.readLine()) != null){
                        downloads.setText(downloads.getText() + line);
                        Log.d("line", line);
                    }
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
            progressDialog.setIndeterminate(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //super.onProgressUpdate(values);
            downloads.setText("已经下载了 [ " + values[0] + " ]行！");
            progressDialog.setProgress(values[0]);
            downloads.setText(downloads.getText() + " " + length);
        }
    };
}
