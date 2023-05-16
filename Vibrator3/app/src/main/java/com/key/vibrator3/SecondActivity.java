package com.key.vibrator3;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SecondActivity extends AppCompatActivity {
    private static final int maxFileNum = 20;
    private static final int BUFFER_SIZE = 2 * 1024;
    private static final String TAG = "Vibrator3";
//    private CheckBox[] cbs;
    private Vector<CheckBox> cbs;
    private CheckBox cb_1;
    private CheckBox cb_2;
    private CheckBox cb_3;
    private CheckBox cb_4;
    private CheckBox cb_5;
    private CheckBox cb_6;
    private CheckBox cb_7;
    private CheckBox cb_8;
    private CheckBox cb_9;
    private CheckBox cb_10;
    private CheckBox cb_11;
    private CheckBox cb_12;
    private CheckBox cb_13;
    private CheckBox cb_14;
    private CheckBox cb_15;
    private CheckBox cb_16;
    private CheckBox cb_17;
    private CheckBox cb_18;
    private CheckBox cb_19;
    private CheckBox cb_20;
    private Button btn_1;
    private Button btn_2;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        cb_1 = (CheckBox) findViewById(R.id.cb_1);
        cb_2 = (CheckBox) findViewById(R.id.cb_2);
        cb_3 = (CheckBox) findViewById(R.id.cb_3);
        cb_4 = (CheckBox) findViewById(R.id.cb_4);
        cb_5 = (CheckBox) findViewById(R.id.cb_5);
        cb_6 = (CheckBox) findViewById(R.id.cb_6);
        cb_7 = (CheckBox) findViewById(R.id.cb_7);
        cb_8 = (CheckBox) findViewById(R.id.cb_8);
        cb_9 = (CheckBox) findViewById(R.id.cb_9);
        cb_10 = (CheckBox) findViewById(R.id.cb_10);
        cb_11 = (CheckBox) findViewById(R.id.cb_11);
        cb_12 = (CheckBox) findViewById(R.id.cb_12);
        cb_13 = (CheckBox) findViewById(R.id.cb_13);
        cb_14 = (CheckBox) findViewById(R.id.cb_14);
        cb_15 = (CheckBox) findViewById(R.id.cb_15);
        cb_16 = (CheckBox) findViewById(R.id.cb_16);
        cb_17 = (CheckBox) findViewById(R.id.cb_17);
        cb_18 = (CheckBox) findViewById(R.id.cb_18);
        cb_19 = (CheckBox) findViewById(R.id.cb_19);
        cb_20 = (CheckBox) findViewById(R.id.cb_20);
        btn_1 = (Button)findViewById(R.id.btn_1);
        btn_2 = (Button)findViewById(R.id.btn_2);
        cbs = new Vector<CheckBox>();
        cbs.add(cb_1);
        cbs.add(cb_2);
        cbs.add(cb_3);
        cbs.add(cb_4);
        cbs.add(cb_5);
        cbs.add(cb_6);
        cbs.add(cb_7);
        cbs.add(cb_8);
        cbs.add(cb_9);
        cbs.add(cb_10);
        cbs.add(cb_11);
        cbs.add(cb_12);
        cbs.add(cb_13);
        cbs.add(cb_14);
        cbs.add(cb_15);
        cbs.add(cb_16);
        cbs.add(cb_17);
        cbs.add(cb_18);
        cbs.add(cb_19);
        cbs.add(cb_20);
        TextView txt1 = findViewById(R.id.text1);

        Vector<String> filesNamesVector = PrintCsvFile(getFilesDir());
        if(filesNamesVector.size() == 0){
            txt1.setText("暂无文件");
            Toast.makeText(getBaseContext(),"警告：无文件分享",Toast.LENGTH_SHORT).show();
            //return;
        }
        else {
            txt1.setText("序号 文件名");
        }
        if(filesNamesVector.size()==0){
            txt1.setText("暂无文件");
            Toast.makeText(getBaseContext(),"警告：无文件分享",Toast.LENGTH_SHORT).show();
            for(int i = 0; i < maxFileNum; i++){
                cbs.elementAt(i).setText( (i+1) + ": 不要点击！");
            }
            //return;
        }
        else if(filesNamesVector.size()<=maxFileNum){
            for(int i = 0; i < filesNamesVector.size(); i++){
                cbs.elementAt(i).setText( (i+1) + ": " + filesNamesVector.elementAt(i));
                Log.d(TAG, "Process:"+i);
            }
            for(int i = filesNamesVector.size(); i < maxFileNum; i++){
                cbs.elementAt(i).setText( (i+1) + ": 不要点击！");
            }
        }
        else{
            Toast.makeText(getBaseContext(),"警告：超过"+maxFileNum+"个文件，无法显示所有文件",Toast.LENGTH_SHORT).show();
            for(int i = 0; i < maxFileNum; i++){
                cbs.elementAt(i).setText( (i+1) + ": " + filesNamesVector.elementAt(i));
            }
        }



        //Btn1 压缩所选CSV文件分享
        btn_1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
//                Toast.makeText(getBaseContext(),getFilesDir().toString(),Toast.LENGTH_SHORT).show();
//                for(int i = 0 ; i < filesNamesVector.size(); ++i){
//                    Toast.makeText(getBaseContext(),i +": "+filesNamesVector.elementAt(i),Toast.LENGTH_SHORT).show();
//                }
                //检查多选按钮
                int[] sharedFile = new int[maxFileNum];
                boolean isNull = false;
                for(int i = 0; i < maxFileNum; ++i){
                    if(cbs.elementAt(i).isChecked()){
                        sharedFile[i]=1;
                        isNull = true;
//                        Toast.makeText(getBaseContext(),i + ":True",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        sharedFile[i]=0;
//                        Toast.makeText(getBaseContext(),i + ":False",Toast.LENGTH_SHORT).show();

                    }
                }
                if(!isNull){
                    Toast.makeText(getBaseContext(),"警告：未选择文件分享",Toast.LENGTH_SHORT).show();
                    return;
                }
                //压缩文件
                List<File> fileList = new ArrayList<>();
                for(int i = 0; i < filesNamesVector.size(); ++i){
                    if(sharedFile[i]==1) {
                        fileList.add(new File(getFilesDir(),filesNamesVector.elementAt(i)));
                    }
                }
                    FileOutputStream fos = null;
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
                String zipName = calendar.get(android.icu.util.Calendar.YEAR) + "_"
                        + calendar.get(android.icu.util.Calendar.MONTH) + "_"
                        + calendar.get(android.icu.util.Calendar.DAY_OF_MONTH) + "_"
                        + calendar.get(android.icu.util.Calendar.HOUR_OF_DAY) + "_"
                        + calendar.get(android.icu.util.Calendar.MINUTE) + "_"
                        + calendar.get(android.icu.util.Calendar.SECOND) + ".zip";
                try {
                    fos = new FileOutputStream(new File(getFilesDir(), zipName));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                toZip(fileList, fos);

                //分享文件
                shareZipToWeChat(zipName);
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getBaseContext(),"文件"+zipName+"分享：跳转微信",Toast.LENGTH_SHORT).show();

            }
        });
        //Btn2 压缩目录下所有CSV文件分享
        btn_2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if(filesNamesVector.size()==0){
                    Toast.makeText(getBaseContext(),"警告：无可分享文件",Toast.LENGTH_SHORT).show();
                    return;
                }
                //压缩文件
                List<File> fileList = new ArrayList<>();
                for(int i = 0; i < filesNamesVector.size(); ++i){
                    fileList.add(new File(getFilesDir(),filesNamesVector.elementAt(i)));
                }
                FileOutputStream fos = null;
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
                String zipName = calendar.get(android.icu.util.Calendar.YEAR) + "_"
                        + calendar.get(android.icu.util.Calendar.MONTH) + "_"
                        + calendar.get(android.icu.util.Calendar.DAY_OF_MONTH) + "_"
                        + calendar.get(android.icu.util.Calendar.HOUR_OF_DAY) + "_"
                        + calendar.get(android.icu.util.Calendar.MINUTE) + "_"
                        + calendar.get(android.icu.util.Calendar.SECOND) + ".zip";
                try {
                    fos = new FileOutputStream(new File(getFilesDir(), zipName));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                toZip(fileList, fos);

                //分享文件
                shareZipToWeChat(zipName);
                try {
                    if(fos != null)  fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getBaseContext(),"文件"+zipName+"分享：跳转微信",Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 寻找路径下csv文件
     * @param Dir 路径
     * 输出 存放该路径下所有文件名字的Vector
     */
    public static Vector<String> PrintCsvFile(File Dir) {
        File [] arr=Dir.listFiles();  // 获取文件或文件夹对象
        Vector<String> fileNames = new Vector<String>();
        assert arr != null;
        for (File file : arr) {//遍历File数组
            if(file.isFile()&&file.getName().endsWith(".csv")) {//判断对象是否是以.csv结尾的类型的文件，是的话就输出
                System.out.println(file.getName());
                fileNames.add(file.getName());
            }
        }
        return fileNames;
    }
    /**
     * 压缩成ZIP方法
     * @param srcFiles 需要压缩的文件列表
     * @param out 压缩文件输出流
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static void toZip(List<File> srcFiles, OutputStream out) throws RuntimeException {
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            for (File srcFile : srcFiles) {
                byte[] buf = new byte[BUFFER_SIZE];
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                in.close();
            }
            long end = System.currentTimeMillis();
            System.out.println("ZIP COMPLETE:TIME COST " + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 递归压缩方法
     * @param sourceFile 源文件
     * @param zos zip输出流
     * @param name 压缩后的名称
     * @param KeepDirStructure 是否保留原来的目录结构,true:保留目录结构; false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean KeepDirStructure)
            throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (KeepDirStructure) {
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }

            } else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(), KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(), KeepDirStructure);
                    }

                }
            }
        }
    }

    /**
     * 唤醒微信，分享Csv文件
     * @param filename 文件名
     */
    public void shareCsvToWeChat(String filename){
        if(filename == null) {
            Toast.makeText(SecondActivity.this, "暂无新文件", Toast.LENGTH_SHORT).show();
            return;
        }
        final Uri uri;
        final File file = new File(getFilesDir(),filename);  // getPath() + "file_1571813009280.pdf"
        // Toast.makeText(MainActivity.this, "文件"+filename, Toast.LENGTH_SHORT).show();
        if(!file.exists()) {
            Log.i(TAG,"File \""+filename+"\" not exist");
            Toast.makeText(SecondActivity.this, "文件"+filename+"不存在或已删除", Toast.LENGTH_SHORT).show();
            return;
        }
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 24) {  // 若SDK大于等于24  获取uri采用共享文件模式
            uri = FileProvider.getUriForFile(this.getApplicationContext(), "com.key.vibrator3.fileprovider", file);
        } else {
            uri = Uri.fromFile(file);  // Uri: Uniform Resource Identifier
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setType("text/comma-separated-values"/*getMimeType(file.getAbsolutePath())*/);//此处可发送多种文件
        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.addCategory(Intent.CATEGORY_DEFAULT);
        share.setPackage("com.tencent.mm");  // "com.tencent.mobileqq"

        try{
            SecondActivity.this.startActivity(Intent.createChooser(share, "分享文件"));
        } catch (Exception e) {
            Toast.makeText(SecondActivity.this, "分享失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
//        if (share.resolveActivity(MainActivity.this.getPackageManager()) != null) {
//            MainActivity.this.startActivity(share);
//        } else {
//            Toast.makeText(MainActivity.this, "分享失败", Toast.LENGTH_SHORT).show();
//        }
    }

    /**
     * 唤醒微信，分享Zip文件
     * @param filename 文件名
     */
    public void shareZipToWeChat(String filename){
        if(filename == null) {
            Toast.makeText(SecondActivity.this, "暂无新文件", Toast.LENGTH_SHORT).show();
            return;
        }
        final Uri uri;
        final File file = new File(getFilesDir(),filename);  // getPath() + "file_1571813009280.pdf"
        // Toast.makeText(MainActivity.this, "文件"+filename, Toast.LENGTH_SHORT).show();
        if(!file.exists()) {
            Log.i(TAG,"File \""+filename+"\" not exist");
            Toast.makeText(SecondActivity.this, "文件"+filename+"不存在或已删除", Toast.LENGTH_SHORT).show();
            return;
        }
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 24) {  // 若SDK大于等于24  获取uri采用共享文件模式
            uri = FileProvider.getUriForFile(this.getApplicationContext(), "com.key.vibrator3.fileprovider", file);
        } else {
            uri = Uri.fromFile(file);  // Uri: Uniform Resource Identifier
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setType("application/zip"/*getMimeType(file.getAbsolutePath())*/);//此处可发送多种文件
        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.addCategory(Intent.CATEGORY_DEFAULT);
        share.setPackage("com.tencent.mm");  // "com.tencent.mobileqq"

        try{
            SecondActivity.this.startActivity(Intent.createChooser(share, "分享文件"));
        } catch (Exception e) {
            Toast.makeText(SecondActivity.this, "分享失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
//        if (share.resolveActivity(MainActivity.this.getPackageManager()) != null) {
//            MainActivity.this.startActivity(share);
//        } else {
//            Toast.makeText(MainActivity.this, "分享失败", Toast.LENGTH_SHORT).show();
//        }
    }

}