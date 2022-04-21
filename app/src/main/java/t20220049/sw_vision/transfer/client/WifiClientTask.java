package t20220049.sw_vision.transfer.client;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import t20220049.sw_vision.transfer.common.Constants;
import t20220049.sw_vision.transfer.model.FileTransfer;
import t20220049.sw_vision.transfer.util.Md5Util;
import t20220049.sw_vision.ui.SendFileActivity;

//后台: 发送文件
public class WifiClientTask extends AsyncTask<Object, Integer, Boolean> {

    private static final String TAG = "WifiClientTask";

//    private final ProgressDialog progressDialog;

    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private boolean usePath = false;

    public WifiClientTask(Context context) {
        this.context = context.getApplicationContext();
//        progressDialog = new ProgressDialog(context);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        progressDialog.setCancelable(false);
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.setTitle("正在发送文件");
//        progressDialog.setMax(100);
        Log.e(TAG, "构造");
    }

    public WifiClientTask(Context context, boolean usePath) {
        this.context = context.getApplicationContext();
//        progressDialog = new ProgressDialog(context);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        progressDialog.setCancelable(false);
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.setTitle("正在发送文件");
//        progressDialog.setMax(100);
        Log.e(TAG, "构造");
        this.usePath = usePath;
    }

    @Override
    protected void onPreExecute() {
//        progressDialog.show();
    }

    private String getOutputFilePath(Uri fileUri) throws Exception {
        String outputFilePath = context.getExternalCacheDir().getAbsolutePath() +
                File.separatorChar + new Random().nextInt(10000) +
                new Random().nextInt(10000) + ".jpg";
        File outputFile = new File(outputFilePath);
        if (!outputFile.exists()) {
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
        }
        Uri outputFileUri = Uri.fromFile(outputFile);
        copyFile(context, fileUri, outputFileUri);
        return outputFilePath;
    }


    @Override
    protected Boolean doInBackground(Object... params) {


        Log.e(TAG, "backGround");
        Socket socket = WifiClientService.socket;
        OutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        InputStream inputStream = null;
        try {
//            String hostAddress = params[0].toString();

            if (params[1] != null && params[1].toString().equals("photo")) {
                WifiClientService.serverOut.println("sendPhoto");
            } else if (params[1] != null && params[1].toString().equals("video")) {
                WifiClientService.serverOut.println("sendVideo");
            } else {
                WifiClientService.serverOut.println("sendFile");
            }
            WifiClientService.serverOut.flush();

            //获取文件
            String outputFilePath;
            if (!usePath) {
                Uri fileUri = Uri.parse(params[0].toString());
                outputFilePath = getOutputFilePath(fileUri);
            } else {
                outputFilePath = params[0].toString();
            }
            File outputFile = new File(outputFilePath);

            //将文件转化为对象
            FileTransfer fileTransfer = new FileTransfer();
            String fileName = outputFile.getName();
            String fileMa5 = Md5Util.getMd5(outputFile);
            long fileLength = outputFile.length();
            fileTransfer.setFileName(fileName);
            fileTransfer.setMd5(fileMa5);
            fileTransfer.setFileLength(fileLength);

            Log.e(TAG, "文件的MD5码值是：" + fileTransfer.getMd5());

            //inputStream负责读文件，outputStream负责向服务器传输文件流
            outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.flush();
            Log.e(TAG,"already sended");
            objectOutputStream.writeObject(fileTransfer);
            inputStream = new FileInputStream(outputFile);

            long fileSize = fileTransfer.getFileLength();
            long total = 0;
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
                total += len;
                int progress = (int) ((total * 100) / fileSize);
                publishProgress(progress);
                Log.e(TAG, "文件发送进度：" + progress);
            }
//            socket.close();
            inputStream.close();
//            outputStream.close();
//            objectOutputStream.close();
//            socket = null;
//            inputStream = null;
//            outputStream = null;
//            objectOutputStream = null;
            Log.e(TAG, "文件发送成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "文件发送异常 Exception: " + e.getMessage());
        } finally {
//            if (socket != null && !socket.isClosed()) {
//                try {
//                    socket.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            if (inputStream != null) {
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (outputStream != null) {
//                try {
//                    outputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (objectOutputStream != null) {
//                try {
//                    objectOutputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
        return false;
    }

    private void copyFile(Context context, Uri inputUri, Uri outputUri) throws NullPointerException,
            IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(inputUri);
             OutputStream outputStream = new FileOutputStream(outputUri.getPath())) {
            if (inputStream == null) {
                throw new NullPointerException("InputStream for given input Uri is null");
            }
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
//        progressDialog.setProgress(values[0]);
    }

    private void showToast(String message) {
        Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
//        progressDialog.cancel();
        Log.e(TAG, "onPostExecute: " + aBoolean);
        showToast("发送文件成功");
    }

}
