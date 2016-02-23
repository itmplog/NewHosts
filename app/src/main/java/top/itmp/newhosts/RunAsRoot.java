package top.itmp.newhosts;

import java.io.DataOutputStream;
import java.io.InputStream;

/**
 * Created by hz on 2016/2/23.
 */
public class RunAsRoot {
    public static String exec(String[] cmds, boolean hasOutput) throws Exception {
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
                    result += new String(buff, 0, readed);
                    // result = seg; //result is a string to show in textview
                }
            }
        }
        os.writeBytes("exit\n");
        os.flush();
        return result;
    }
}
