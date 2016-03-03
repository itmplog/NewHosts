package top.itmp.newhosts.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by hz on 2016/2/23.
 */
public class UpdateCheck extends AsyncTask<URL, Integer, String> {
    ProgressDialog progressDialog;
    int length;
    int hasRead = 0;
    int i = 0;
    Context mContext;

    public UpdateCheck(Context c){
        mContext = c;
    }

    public static void check(Context context, String url) {
        UpdateCheck updateCheck = new UpdateCheck(context);
        try {
            updateCheck.execute(new URL(url));
        } catch (MalformedURLException e) {
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
        if(s == null)
        {Toast.makeText(mContext.getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();}
        Log.v("json", s.toString());
        try {
            JSONArray jsonArray = new JSONArray(s);
            JSONObject jsonRootObject = jsonArray.getJSONObject(0);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

            if(sharedPreferences.getString("sha", null) == null){
                DownTask.downHosts(mContext, "https://raw.githubusercontent.com/itmplog/hosts/master/hosts");
                sharedPreferences.edit().putString("sha", jsonRootObject.getString("sha")).commit();
                sharedPreferences.edit().putString("update", jsonRootObject.getJSONObject("commit").getJSONObject("author").getString("date")).commit();

            }
            if(sharedPreferences.getString("sha", null).equals(jsonRootObject.getString("sha")) && (new File("/system/etc/hosts")).exists()){
                Toast.makeText(mContext.getApplicationContext(), "No Update hosts useful.", Toast.LENGTH_SHORT).show();
                return;
            }
            sharedPreferences.edit().putString("sha", jsonRootObject.getString("sha")).commit();
            sharedPreferences.edit().putString("update", jsonRootObject.getJSONObject("commit").getJSONObject("author").getString("date")).commit();

            DownTask.downHosts(mContext, "https://raw.githubusercontent.com/itmplog/hosts/master/hosts");
            /*
            DownTask downTask = new DownTask(mContext);
            try{
            downTask.execute(new URL("https://raw.githubusercontent.com/itmplog/hosts/master/hosts.txt"));
            }catch (MalformedURLException e){
                e.printStackTrace();
            }
            */

            //Log.v("json", jsonRootObject.getString("sha"));
        }catch(JSONException e){
            e.printStackTrace();
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
        progressDialog.setTitle("正在检查更新...");
        progressDialog.setMessage("检查中, 请等待....");
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        // Log.d("pre", length + "");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // 设置进度条风格
        progressDialog.setIndeterminate(false); // set the indeterminate for true  cause it will be downloaded so soon
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        //super.onProgressUpdate(values);
        progressDialog.setMessage(values[0] + "%...");
        //downloads.setText("已经下载了 [ " + values[0] + "%]\n");
        progressDialog.setProgress(values[0]);
    }
}
