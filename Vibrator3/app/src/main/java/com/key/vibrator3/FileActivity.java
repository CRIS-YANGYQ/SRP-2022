package com.key.vibrator3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Vector;

public class FileActivity extends AppCompatActivity {

    private String[] options =
        {"1st:null","2nd:null","3th:null","4th:null","5th:null","6th:null", "7th:null","8th:null",
                "9th:null", "10th:null","11th:null","12th:null","13th:null","14th:null","15th:null"};
    private final String TAG = "Vibrator3";
    private String[] finalOptions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        TextView noFile = findViewById(R.id.noFile);
        Vector<String> filesNamesVector = PrintCsvFile(getFilesDir());
        if(filesNamesVector.size() == 0){
            noFile.setText("暂无文件");
        }else
            noFile.setText("序号 文件名");
        finalOptions = new String[filesNamesVector.size()];
        for(int i = 0; i < filesNamesVector.size(); i++){
            finalOptions[i] = (i + 1) + ". " + filesNamesVector.get(i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(FileActivity.this,
                android.R.layout.simple_list_item_1, finalOptions);
        ListView listView = (ListView) findViewById(R.id.List_View);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position<filesNamesVector.size())
                    shareFileToWeChat(filesNamesVector.elementAt(position));
                else{
                    Toast.makeText(FileActivity.this, "找不到文件"+filesNamesVector.elementAt(position), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

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

    public void shareFileToWeChat(String filename){
        if(filename == null) {
            Toast.makeText(FileActivity.this, "暂无新文件", Toast.LENGTH_SHORT).show();
            return;
        }
        final Uri uri;
        final File file = new File(getFilesDir(),filename);  // getPath() + "file_1571813009280.pdf"
        // Toast.makeText(MainActivity.this, "文件"+filename, Toast.LENGTH_SHORT).show();
        if(!file.exists()) {
            Log.i(TAG,"File \""+filename+"\" not exist");
            Toast.makeText(FileActivity.this, "文件"+filename+"不存在或已删除", Toast.LENGTH_SHORT).show();
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
            FileActivity.this.startActivity(Intent.createChooser(share, "分享文件"));
        } catch (Exception e) {
            Toast.makeText(FileActivity.this, "分享失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}