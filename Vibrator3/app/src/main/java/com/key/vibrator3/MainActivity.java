package com.key.vibrator3;
import android.annotation.SuppressLint;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private final String TAG = "Vibrator";
    private Handler handler;
    private Calendar calendar;
    private Vibrator vibrator;
    private CountDownTimer timer;
    private SensorManager sensorManager;

    private int line = 0;
    // private final int vibratingMaxTime = 10000;//最长振动时间
    private int currentVibratingTime = 1000;
    private long vibrationTime = 0L;
    private long timeRemaining = 0L;
    private float lowPositionTime;//低位抬起时间
    private float highPositionTime;//高位抬起时间
    private boolean vibrating = false;
    private boolean detectRaison = false;

    private TextView vibrationTimeText;
    private TextView vibrateText;
    private TextView accxText;
    private TextView accyText;
    private TextView acczText;
    private TextView gyroxText;
    private TextView gyroyText;
    private TextView gyrozText;


    private FileOutputStream writer;
    private String newestFile;



    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5;
    private Button btn6;
    private Button btn7;
    private Button btn8;
    private BroadcastReceiver mPhoneRaisedReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("phone_raised_event")) {
                boolean phoneRaised = intent.getBooleanExtra("phone_raised", false);
                // TODO: handle the phoneRaised value
                if(phoneRaised){
                    if(!vibrating){
                        //振动时间
                        setVibration();
                    }
                }
            }
        }
    };


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrateText = findViewById(R.id.text_vibrating);
        vibrationTimeText = findViewById(R.id.text_vibrationTimeText);
        btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(view -> {
            if (vibrating) {
                unvibrate();
            } else {
                vibrateText.setText("正在振动");
                if (!detectRaison) {
                    setVibration();
                } else {
                    LocalBroadcastManager.getInstance(MainActivity.this)
                            .registerReceiver(mPhoneRaisedReceiver, new IntentFilter("phone_raised_event"));
                }
            }
        });

        btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(view -> shareFileToWeChat(newestFile));

        btn3 = findViewById(R.id.btn3);
        btn3.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, FileActivity.class)));

        btn4 = findViewById(R.id.btn4);
        btn4.setOnClickListener(view -> clearCsvFile());

        btn5 = findViewById(R.id.btn5);
        btn5.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SecondActivity.class)));

        btn6 = findViewById(R.id.btn6);
        btn6.setOnClickListener(view -> clearZipFile());

        btn7 = findViewById(R.id.btn7);
        btn7.setOnClickListener(view -> {
            if (!isMyServiceRunning(RaisonService.class)) {
                startService(new Intent(MainActivity.this, RaisonService.class));
                detectRaison = true;
                Toast.makeText(getApplicationContext(), "开始检测抬起动作", Toast.LENGTH_SHORT).show();
                TextView txt = findViewById(R.id.text_detectingRaison);
                txt.setText("正在检测抬起");
                txt.setTextColor(Color.BLUE);
            }
        });

        btn8 = findViewById(R.id.btn8);
        btn8.setOnClickListener(view -> {
            if (isMyServiceRunning(RaisonService.class)) {
                stopService(new Intent(MainActivity.this, RaisonService.class));
                detectRaison = false;
                Toast.makeText(getApplicationContext(), "结束检测抬起动作", Toast.LENGTH_SHORT).show();
                TextView txt = findViewById(R.id.text_detectingRaison);
                txt.setText("未检测抬起");
                txt.setTextColor(Color.BLACK);
            }
        });

        accxText = findViewById(R.id.accx);
        accyText = findViewById(R.id.accy);
        acczText = findViewById(R.id.accz);
        gyroxText = findViewById(R.id.gyrox);
        gyroyText = findViewById(R.id.gyroy);
        gyrozText = findViewById(R.id.gyroz);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensora = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensorg = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(listenera, sensora, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(listenerg, sensorg, SensorManager.SENSOR_DELAY_FASTEST);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    public void setVibration(int time) {
        vibrate();
        currentVibratingTime = time;
        timer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                long secondsRemaining = millisUntilFinished / 1000;
                vibrateText.
                        setText("正在振动 剩余" + secondsRemaining + "/" + (currentVibratingTime/1000) + "s");
            }

            @Override
            public void onFinish() {
                unvibrate();
            }
        }.start();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                unvibrate();
//            }
//        }, time);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setVibration() {
        EditText et = findViewById(R.id.et_maxTime);
        int time = Integer.parseInt(et.getText().toString());
        vibrate();
        currentVibratingTime = time;
        timer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                long secondsRemaining = millisUntilFinished / 1000;
                vibrateText.
                        setText("正在振动 剩余" + secondsRemaining + "/" + (currentVibratingTime/1000) + "s");
            }

            @Override
            public void onFinish() {
                unvibrate();
            }
        }.start();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                unvibrate();
//            }
//        }, time);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    public void vibrate() {
        if(vibrating) return;
        btn1.setBackgroundResource(R.drawable.logo_university);
        writerOpen();

        vibrating = true;

        if (vibrator.hasVibrator()) {
            long[] interval = {1, 500, 1000};
            EditText et1 = findViewById(R.id.et_vibTime);
            String text = et1.getText().toString();
            if (!text.equals(""))
                interval[1] = Long.parseLong(text);
            EditText et2 = findViewById(R.id.et_interval);
            text = et2.getText().toString();
            if (!text.equals(""))
                interval[2] = Long.parseLong(text);
            vibrator.vibrate(interval, 1);
        }
    }

    private void unvibrate() {
        // stop vibration
        vibrator.cancel();

        // clear vibration time
        vibrationTime = 0;

        // clear TextView
        vibrationTimeText.setText("");

        btn1.setBackgroundResource(R.drawable.btn1_selector);
        writerClose();
        vibrating = false;
        vibrator.cancel();
        if(handler!=null)
            handler.removeCallbacksAndMessages(null);
        TextView text_vibrating = (TextView) findViewById(R.id.text_vibrating);
        text_vibrating.setText("");
        if (timer != null) {
            timer.cancel();
            timeRemaining = 0;
        }
    }

    public String format(int num, int length){
        StringBuilder strBuilder = new StringBuilder(Integer.toString(num));
        while (strBuilder.length()<length)
            strBuilder.insert(0, "0");
        return strBuilder.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void writerOpen(){
        boolean success = true;
        // FileOutputStream out = null;
        try {
            Log.d(TAG, "writerOpen: -------1");

            //使用openFileOutput()函数,直接在/data/data/包名/files/目录下创建文件
            calendar = Calendar.getInstance();
            String name = calendar.get(Calendar.YEAR) + "_"
                    + calendar.get(Calendar.MONTH) + "_"
                    + calendar.get(Calendar.DAY_OF_MONTH) + "_"
                    + calendar.get(Calendar.HOUR_OF_DAY) + "_"
                    + calendar.get(Calendar.MINUTE) + "_"
                    + format(calendar.get(Calendar.SECOND), 2) + "_"
                    + format(calendar.get(Calendar.MILLISECOND), 3) + ".csv";
            newestFile = name;
            Log.d(TAG, "name: " + name);
            writer = openFileOutput(name, Context.MODE_PRIVATE); //私有模式写文件
            Log.d(TAG, "writerOpen: -------2");
            writer.write("row,time,accx,accy,accz,gyrox,gyroy,gyroz\n".getBytes());  // time(hour_minute_second_millisecond)
        } catch (IOException e) {
            Log.d(TAG, "writerOpen: -------3");
            success = false;
            e.printStackTrace();
        }
        if(!success)
            Toast.makeText(MainActivity.this, "创建csv文件失败!", Toast.LENGTH_SHORT).show();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void writeData(){
//        if(writer==null)
//            Log.d(TAG, "writeData: -------0");
        if (writer != null) {
            Log.d(TAG, "writeData: -------1");
            //calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE)
            //                    + "_" + calendar.get(Calendar.SECOND) + "_" + calendar.get(Calendar.MILLISECOND)
            line++;
            Log.d(TAG, Integer.toString(line));
            calendar = Calendar.getInstance();
            String temp = line + "," + calendar.get(Calendar.HOUR_OF_DAY)
                    + "_" + format(calendar.get(Calendar.MINUTE), 2)
                    + "_" + format(calendar.get(Calendar.SECOND), 2)
                    + "_" + format(calendar.get(Calendar.MILLISECOND), 3) + ","
                    + ((String) accxText.getText()).substring(5) + ','
                    + ((String) accyText.getText()).substring(5) + ','
                    + ((String) acczText.getText()).substring(5) + ','
                    + ((String) gyroxText.getText()).substring(6) + ','
                    + ((String) gyroyText.getText()).substring(6) + ','
                    + ((String) gyrozText.getText()).substring(6) + '\n';
            Log.d(TAG, "temp: " + temp);
            try {
                writer.write(temp.getBytes());
                Log.d(TAG, "writeData: -------2");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writerClose(){
        line = 0;
        if(writer != null){
            try {
                writer.close();
                writer = null;
                Toast.makeText(MainActivity.this,"文件"+newestFile+"保存成功", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this,"文件"+newestFile+"关闭失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public void shareFileToWeChat(String filename){
        if(filename == null) {
            Toast.makeText(MainActivity.this, "暂无新文件", Toast.LENGTH_SHORT).show();
            return;
        }
        final Uri uri;
        final File file = new File(getFilesDir(),filename);  // getPath() + "file_1571813009280.pdf"
        // Toast.makeText(MainActivity.this, "文件"+filename, Toast.LENGTH_SHORT).show();
        if(!file.exists()) {
            Log.i(TAG,"File \""+filename+"\" not exist");
            Toast.makeText(MainActivity.this, "文件"+filename+"不存在或已删除", Toast.LENGTH_SHORT).show();
            return;
        }
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 24) {//若SDK大于等于24  获取uri采用共享文件模式
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
            MainActivity.this.startActivity(Intent.createChooser(share, "分享文件"));
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "分享失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void clearCsvFile(){
        File [] arr = getFilesDir().listFiles();//获取文件或文件夹对象
        assert arr != null;
        boolean success = true;
        for (File file : arr) {//遍历File数组
            if(file.isFile()&&file.getName().endsWith(".csv")) {//判断对象是否是以.csv结尾的类型的文件
                System.out.println(file.getName());
                try{
                    boolean suc = file.delete();
                    if(!suc) {
                        success = false;
                        Toast.makeText(MainActivity.this, "文件" + file.getName() + "删除失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "文件"+file.getName()+"删除失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
        if(success)
            Toast.makeText(MainActivity.this, "已删除所有csv文件", Toast.LENGTH_SHORT).show();
    }

    public void clearZipFile(){
        File [] arr = getFilesDir().listFiles();//获取文件或文件夹对象
        assert arr != null;
        boolean success = true;
        for (File file : arr) {//遍历File数组
            if(file.isFile()&&file.getName().endsWith(".zip")) {//判断对象是否是以.csv结尾的类型的文件
                System.out.println(file.getName());
                try{
                    boolean suc = file.delete();
                    if(!suc) {
                        success = false;
                        Toast.makeText(MainActivity.this, "文件" + file.getName() + "删除失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "文件"+file.getName()+"删除失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
        if(success)
            Toast.makeText(MainActivity.this, "已删除所有zip文件", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void smartVibrate(SensorEvent event){
        if(vibrating)
            return;
        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];

        if (Math.abs(x) < 3 && 0 < y && y < 4 && 9 < z && z < 9.8) {
            lowPositionTime = SystemClock.uptimeMillis();
        }

        if (Math.abs(x) < 3 && 4 < y && y < 9 && 2 < z && z < 9) {
            float timeDiff = SystemClock.uptimeMillis() - lowPositionTime;
            if(timeDiff >= 0 && timeDiff < 200){
                setVibration(500);
            }
        }
    }

    private final SensorEventListener listenera = new SensorEventListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent event) {
            float accx = event.values[0];
            float accy = event.values[1];
            float accz = event.values[2];
            accxText.setText("accx:" + accx);
            accyText.setText("accy:" + accy);
            acczText.setText("accz:" + accz);
            writeData();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private final SensorEventListener listenerg = new SensorEventListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent event) {
            float gyrox = event.values[0];
            float gyroy = event.values[1];
            float gyroz = event.values[2];
            gyroxText.setText("gyrox:" + gyrox);
            gyroyText.setText("gyroy:" + gyroy);
            gyrozText.setText("gyroz:" + gyroz);
            writeData();
        }



        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    protected void onDestroy() {
        super.onDestroy();
        // Stop vibrating and cancel timer
        unvibrate();
    }
}
//package com.key.vibrator3;
//import android.annotation.SuppressLint;
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.FileProvider;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//
//import android.app.ActivityManager;
//import android.content.Context;
//import android.content.BroadcastReceiver;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Color;
//import android.icu.util.Calendar;
//import android.net.Uri;
//import android.os.CountDownTimer;
//import android.os.Build;
//import android.os.Handler;
//import android.os.SystemClock;
//import android.os.Bundle;
//import android.os.Vibrator;
//import android.util.Log;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//
//import android.widget.TextView;
//import android.widget.EditText;
//import android.widget.Button;
//import android.widget.Toast;
//
//
//import java.io.File;

//import java.io.FileOutputStream;
//import java.io.IOException;
//
//
//public class MainActivity extends AppCompatActivity {
//    private final String TAG = "Vibrator";
//    private Handler handler;
//    private Calendar calendar;
//    private Vibrator vibrator;
//    private CountDownTimer timer;
//    private SensorManager sensorManager;
//
//    private int line = 0;
//    private final int vibratingMaxTime = 10000;//最长振动时间
//    private long vibrationTime = 0L;
//    private long timeRemaining = 0L;
//    private float lowPositionTime;//低位抬起时间
//    private float highPositionTime;//高位抬起时间
//    private boolean vibrating = false;
//    private boolean detectRaison = false;
//
//    private TextView vibrationTimeText;
//    private TextView vibrateText;
//    private TextView accxText;
//    private TextView accyText;
//    private TextView acczText;
//    private TextView gyroxText;
//    private TextView gyroyText;
//    private TextView gyrozText;
//
//
//    private FileOutputStream writer;
//    private String newestFile;
//
//
//
//    private Button btn1;
//    private Button btn2;
//    private Button btn3;
//    private Button btn4;
//    private Button btn5;
//    private Button btn6;
//    private Button btn7;
//    private Button btn8;
//    private BroadcastReceiver mPhoneRaisedReceiver = new BroadcastReceiver() {
//        @RequiresApi(api = Build.VERSION_CODES.N)
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals("phone_raised_event")) {
//                boolean phoneRaised = intent.getBooleanExtra("phone_raised", false);
//                // TODO: handle the phoneRaised value
//                if(phoneRaised){
//                    if(!vibrating){
//                        //振动时间
//                        vibrate(vibratingMaxTime);
//                        vibrating = true;
//                    }
//                }
//            }
//        }
//    };
//
//
//    private boolean isMyServiceRunning(Class<?> serviceClass) {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    @SuppressLint("SetTextI18n")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        handler = new Handler();
//        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//        vibrateText = findViewById(R.id.text_vibrating);
//        vibrationTimeText = findViewById(R.id.text_vibrationTimeText);
//        btn1 = findViewById(R.id.btn1);
//        btn1.setOnClickListener(view -> {
//            if (vibrating) {
//                unvibrate();
//            } else {
//                vibrateText.setText("正在振动");
//                if (!detectRaison) {
//                    vibrate(vibratingMaxTime);
//                } else {
//                    LocalBroadcastManager.getInstance(MainActivity.this)
//                            .registerReceiver(mPhoneRaisedReceiver, new IntentFilter("phone_raised_event"));
//                }
//            }
//        });
//
//        btn2 = findViewById(R.id.btn2);
//        btn2.setOnClickListener(view -> shareFileToWeChat(newestFile));
//
//        btn3 = findViewById(R.id.btn3);
//        btn3.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, FileActivity.class)));
//
//        btn4 = findViewById(R.id.btn4);
//        btn4.setOnClickListener(view -> clearCsvFile());
//
//        btn5 = findViewById(R.id.btn5);
//        btn5.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SecondActivity.class)));
//
//        btn6 = findViewById(R.id.btn6);
//        btn6.setOnClickListener(view -> clearZipFile());
//
//        btn7 = findViewById(R.id.btn7);
//        btn7.setOnClickListener(view -> {
//            if (!isMyServiceRunning(RaisonService.class)) {
//                startService(new Intent(MainActivity.this, RaisonService.class));
//                detectRaison = true;
//                Toast.makeText(getApplicationContext(), "开始检测抬起动作", Toast.LENGTH_SHORT).show();
//                TextView txt = findViewById(R.id.text_detectingRaison);
//                txt.setText("正在检测抬起");
//                txt.setTextColor(Color.BLUE);
//            }
//        });
//
//        btn8 = findViewById(R.id.btn8);
//        btn8.setOnClickListener(view -> {
//            if (isMyServiceRunning(RaisonService.class)) {
//                stopService(new Intent(MainActivity.this, RaisonService.class));
//                detectRaison = false;
//                Toast.makeText(getApplicationContext(), "结束检测抬起动作", Toast.LENGTH_SHORT).show();
//                TextView txt = findViewById(R.id.text_detectingRaison);
//                txt.setText("未检测抬起");
//                txt.setTextColor(Color.BLACK);
//            }
//        });
//
//        accxText = findViewById(R.id.accx);
//        accyText = findViewById(R.id.accy);
//        acczText = findViewById(R.id.accz);
//        gyroxText = findViewById(R.id.gyrox);
//        gyroyText = findViewById(R.id.gyroy);
//        gyrozText = findViewById(R.id.gyroz);
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        Sensor sensora = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        Sensor sensorg = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        sensorManager.registerListener(listenera, sensora, SensorManager.SENSOR_DELAY_FASTEST);
//        sensorManager.registerListener(listenerg, sensorg, SensorManager.SENSOR_DELAY_FASTEST);
//
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    @SuppressLint("SetTextI18n")
//    public void vibrate(int time) {
//        vibrate();
//        timer = new CountDownTimer(time, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                timeRemaining = millisUntilFinished;
//                long secondsRemaining = millisUntilFinished / 1000;
//                vibrateText.
//                        setText("正在振动 剩余" + secondsRemaining + "/" + (vibratingMaxTime/1000) + "s");
//            }
//
//            @Override
//            public void onFinish() {
//                unvibrate();
//            }
//        }.start();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                unvibrate();
//            }
//        }, time);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    @SuppressLint("SetTextI18n")
//    public void vibrate() {
//        btn1.setBackgroundResource(R.drawable.logo_university);
//        if(vibrating) return;
//
//        writerOpen();
//
//        vibrating = true;
//
//        if (vibrator.hasVibrator()) {
//            long[] interval = {1, 500, 1000};
//            EditText et1 = findViewById(R.id.et1);
//            String text = et1.getText().toString();
//            if (!text.equals(""))
//                interval[1] = Long.parseLong(text);
//            EditText et2 = findViewById(R.id.et2);
//            text = et2.getText().toString();
//            if (!text.equals(""))
//                interval[2] = Long.parseLong(text);
//            vibrator.vibrate(interval, 1);
//        }
//    }
//
//private void unvibrate() {
//    // stop vibration
//    vibrator.cancel();
//
//    // clear vibration time
//    vibrationTime = 0;
//
//    // clear TextView
//    vibrationTimeText.setText("");
//
//    btn1.setBackgroundResource(R.drawable.btn1_selector);
//    writerClose();
//    vibrating = false;
//    vibrator.cancel();
//    if(handler!=null)
//        handler.removeCallbacksAndMessages(null);
//    TextView text_vibrating = (TextView) findViewById(R.id.text_vibrating);
//    text_vibrating.setText("");
//    if (timer != null) {
//        timer.cancel();
//        timeRemaining = 0;
//    }
//}
//
//    public String format(String str){
//        if(str.length() == 1)
//            return "0" + str;
//        return str;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public void writerOpen(){
//        boolean success = true;
//        // FileOutputStream out = null;
//        try {
//            Log.d(TAG, "writerOpen: -------1");
//            //使用openFileOutput()函数,直接在/data/data/包名/files/目录下创建文件
//            calendar = Calendar.getInstance();
//            String name = calendar.get(Calendar.YEAR) + "_"
//                    + calendar.get(Calendar.MONTH) + "_"
//                    + calendar.get(Calendar.DAY_OF_MONTH) + "_"
//                    + calendar.get(Calendar.HOUR_OF_DAY) + "_"
//                    + calendar.get(Calendar.MINUTE) + "_"
//                    + format(Integer.toString(calendar.get(Calendar.SECOND))) + "_"
//                    + calendar.get(Calendar.MILLISECOND) + ".csv";
//            newestFile = name;
//            Log.d(TAG, "name: " + name);
//            writer = openFileOutput(name, Context.MODE_PRIVATE); //私有模式写文件
//            Log.d(TAG, "writerOpen: -------2");
//            writer.write("row,accx,accy,accz,gyrox,gyroy,gyroz\n".getBytes());  // time(hour_minute_second_millisecond)
//        } catch (IOException e) {
//            Log.d(TAG, "writerOpen: -------3");
//            success = false;
//            e.printStackTrace();
//        }
//        if(!success)
//            Toast.makeText(MainActivity.this, "创建csv文件失败!", Toast.LENGTH_SHORT).show();
//    }
//
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public void writeData(){
////        if(writer==null)
////            Log.d(TAG, "writeData: -------0");
//        if (writer != null) {
//            Log.d(TAG, "writeData: -------1");
//            //calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE)
//            //                    + "_" + calendar.get(Calendar.SECOND) + "_" + calendar.get(Calendar.MILLISECOND)
//            line++;
//            String temp = line + ","
//                    + ((String) accxText.getText()).substring(5) + ','
//                    + ((String) accyText.getText()).substring(5) + ','
//                    + ((String) acczText.getText()).substring(5) + ','
//                    + ((String) gyroxText.getText()).substring(6) + ','
//                    + ((String) gyroyText.getText()).substring(6) + ','
//                    + ((String) gyrozText.getText()).substring(6) + '\n';
//            Log.d(TAG, "temp: " + temp);
//            try {
//                writer.write(temp.getBytes());
//                Log.d(TAG, "writeData: -------2");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public void writerClose(){
//        line = 0;
//        if(writer != null){
//            try {
//                writer.close();
//                writer = null;
//                Toast.makeText(MainActivity.this,"文件"+newestFile+"保存成功", Toast.LENGTH_SHORT).show();
//            } catch (IOException e) {
//                Toast.makeText(MainActivity.this,"文件"+newestFile+"关闭失败", Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public void shareFileToWeChat(String filename){
//        if(filename == null) {
//            Toast.makeText(MainActivity.this, "暂无新文件", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        final Uri uri;
//        final File file = new File(getFilesDir(),filename);  // getPath() + "file_1571813009280.pdf"
//        // Toast.makeText(MainActivity.this, "文件"+filename, Toast.LENGTH_SHORT).show();
//        if(!file.exists()) {
//            Log.i(TAG,"File \""+filename+"\" not exist");
//            Toast.makeText(MainActivity.this, "文件"+filename+"不存在或已删除", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//        if (currentapiVersion >= 24) {//若SDK大于等于24  获取uri采用共享文件模式
//            uri = FileProvider.getUriForFile(this.getApplicationContext(), "com.key.vibrator3.fileprovider", file);
//        } else {
//            uri = Uri.fromFile(file);  // Uri: Uniform Resource Identifier
//        }
//
//        Intent share = new Intent(Intent.ACTION_SEND);
//        share.putExtra(Intent.EXTRA_STREAM, uri);
//        share.setType("text/comma-separated-values"/*getMimeType(file.getAbsolutePath())*/);//此处可发送多种文件
//        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        share.addCategory(Intent.CATEGORY_DEFAULT);
//        share.setPackage("com.tencent.mm");  // "com.tencent.mobileqq"
//
//        try{
//            MainActivity.this.startActivity(Intent.createChooser(share, "分享文件"));
//        } catch (Exception e) {
//            Toast.makeText(MainActivity.this, "分享失败", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
//    }
//
//    public void clearCsvFile(){
//        File [] arr = getFilesDir().listFiles();//获取文件或文件夹对象
//        assert arr != null;
//        boolean success = true;
//        for (File file : arr) {//遍历File数组
//            if(file.isFile()&&file.getName().endsWith(".csv")) {//判断对象是否是以.csv结尾的类型的文件
//                System.out.println(file.getName());
//                try{
//                    boolean suc = file.delete();
//                    if(!suc) {
//                        success = false;
//                        Toast.makeText(MainActivity.this, "文件" + file.getName() + "删除失败", Toast.LENGTH_SHORT).show();
//                    }
//                } catch (Exception e) {
//                    Toast.makeText(MainActivity.this, "文件"+file.getName()+"删除失败", Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                }
//            }
//        }
//        if(success)
//            Toast.makeText(MainActivity.this, "已删除所有csv文件", Toast.LENGTH_SHORT).show();
//    }
//
//    public void clearZipFile(){
//        File [] arr = getFilesDir().listFiles();//获取文件或文件夹对象
//        assert arr != null;
//        boolean success = true;
//        for (File file : arr) {//遍历File数组
//            if(file.isFile()&&file.getName().endsWith(".zip")) {//判断对象是否是以.csv结尾的类型的文件
//                System.out.println(file.getName());
//                try{
//                    boolean suc = file.delete();
//                    if(!suc) {
//                        success = false;
//                        Toast.makeText(MainActivity.this, "文件" + file.getName() + "删除失败", Toast.LENGTH_SHORT).show();
//                    }
//                } catch (Exception e) {
//                    Toast.makeText(MainActivity.this, "文件"+file.getName()+"删除失败", Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                }
//            }
//        }
//        if(success)
//            Toast.makeText(MainActivity.this, "已删除所有zip文件", Toast.LENGTH_SHORT).show();
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public void smartVibrate(SensorEvent event){
//        if(vibrating)
//            return;
//        float[] values = event.values;
//        float x = values[0];
//        float y = values[1];
//        float z = values[2];
//
//        if (Math.abs(x) < 3 && 0 < y && y < 4 && 9 < z && z < 9.8) {
//            lowPositionTime = SystemClock.uptimeMillis();
//        }
//
//        if (Math.abs(x) < 3 && 4 < y && y < 9 && 2 < z && z < 9) {
//            float timeDiff = SystemClock.uptimeMillis() - lowPositionTime;
//            if(timeDiff >= 0 && timeDiff < 200){
//                vibrate(500);
//            }
//        }
//    }
//
//    private final SensorEventListener listenera = new SensorEventListener() {
//        @RequiresApi(api = Build.VERSION_CODES.N)
//        @SuppressLint("SetTextI18n")
//        @Override
//        public void onSensorChanged(SensorEvent event) {
//            float accx = event.values[0];
//            float accy = event.values[1];
//            float accz = event.values[2];
//            accxText.setText("accx:" + accx);
//            accyText.setText("accy:" + accy);
//            acczText.setText("accz:" + accz);
//            writeData();
//        }
//
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        }
//    };
//
//    private final SensorEventListener listenerg = new SensorEventListener() {
//        @RequiresApi(api = Build.VERSION_CODES.N)
//        @SuppressLint("SetTextI18n")
//        @Override
//        public void onSensorChanged(SensorEvent event) {
//            float gyrox = event.values[0];
//            float gyroy = event.values[1];
//            float gyroz = event.values[2];
//            gyroxText.setText("gyrox:" + gyrox);
//            gyroyText.setText("gyroy:" + gyroy);
//            gyrozText.setText("gyroz:" + gyroz);
//            writeData();
//        }
//
//
//
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        }
//    };
//
//    protected void onDestroy() {
//        super.onDestroy();
//        // Stop vibrating and cancel timer
//        unvibrate();
//    }
//}