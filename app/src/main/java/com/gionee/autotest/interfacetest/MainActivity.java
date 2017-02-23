package com.gionee.autotest.interfacetest;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gionee.auto.auto1salarm.R;
import com.gionee.autotest.interfacetest.Util.Instrument;
import com.gionee.autotest.interfacetest.Util.Preference;
import com.gionee.autotest.interfacetest.Util.Util;

import java.util.Calendar;

import static com.gionee.autotest.interfacetest.Util.Util.getTime;


public class MainActivity extends Activity implements OnClickListener{
	private static final int _1 = 1;
	private Button start, stop, resultButton;
	private EditText times;
	private TextView info;
	private Handler mHandler;
	private int TIMES_ALL = 0;// 操作次数
	private int curtimes = 0;
	private int failCount = 0;
	private IntentFilter mIntentFilter;
	private ContentResolver resolver;
	private boolean is24 = false;
	private boolean isHasAlarm = false;
	private SharedPreferences sharedPrefrences;
	private Editor editor;
	private Util mUtil;
	boolean is5701 = true;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			isHasAlarm = true;

		}
	};
	public void initView(){
		start = (Button) findViewById(R.id.start);
		stop = (Button) findViewById(R.id.stop);
		times = (EditText) findViewById(R.id.editText1);
		info = (TextView) findViewById(R.id.info);
		resultButton = (Button) findViewById(R.id.result);
		start.setOnClickListener(this);
		stop.setOnClickListener(this);
		resultButton.setOnClickListener(this);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initView();
		sharedPrefrences = getSharedPreferences("preferences",
				Context.MODE_PRIVATE);
		editor = sharedPrefrences.edit();

		/*
		 * resolver = this.getContentResolver(); String strTimeFormat =
		 * android.provider.Settings.System.getString( resolver,
		 * android.provider.Settings.System.TIME_12_24);
		 *
		 * if (strTimeFormat.equals("24")) { is24 = true;
		 *
		 * }
		 */
		is24 = DateFormat.is24HourFormat(this.getApplicationContext());
		Log.i("song", "activity 24H:" + is24);
		mIntentFilter = new IntentFilter();
		// mIntentFilter = new
		// IntentFilter("com.android.deskclock.ALARM_ALERT");
		mIntentFilter.addAction("com.android.deskclock.ALARM_ALERT");
		registerReceiver(receiver, mIntentFilter);

		// 屏幕保持唤醒状态
		Settings.System.putInt(getContentResolver(),
				Settings.System.STAY_ON_WHILE_PLUGGED_IN, 1);

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (msg.what == _1) {
					info.setText("正在进行第" + curtimes + "次测试中，请稍后...");
					info.setTextColor(Color.GREEN);
					isHasAlarm = false;
				} else if (msg.what == 2) {
					info.setText("测试完成!");
					resultButton.setEnabled(true);
				}
				super.handleMessage(msg);
			}

		};



	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.start:
				sharedPrefrences = getSharedPreferences("preferences",
						Context.MODE_PRIVATE);

				Editor editor = sharedPrefrences.edit();
				editor.clear(); // 清空sharedPrefrences数据
				editor.commit();
				info.setText(" ");

				// TODO Auto-generated method stub
				TIMES_ALL = Integer.parseInt(times.getText().toString().trim());// 获取得到的设置的次数
				// mHandler.postDelayed(mTest, 3000);// 3秒钟测试启动测试线程
				Toast.makeText(getApplicationContext(), "测试开始", Toast.LENGTH_SHORT).show();
				// curtimes = 0;// 重置当前次数
				// failCount = 0;// 失败次数
				isHasAlarm = false;
				stop.setEnabled(true);
				start.setEnabled(false);
				resultButton.setEnabled(false);
				// delectAllAlarm(); //8600 no permission
				Thread thread = new Thread(new Test());
				thread.start();
				break;
			case R.id.stop:
				// TODO Auto-generated method stub
				if (receiver != null) {
					unregisterReceiver(receiver);
				}
				// delectAllAlarm();
				Toast.makeText(MainActivity.this, "测试已经取消",Toast.LENGTH_SHORT).show();
				android.os.Process.killProcess(android.os.Process.myPid());
				break;
			case R.id.result:
				// TODO Auto-generated method stub
				ShowResult();

		}
	}
	// 注册广播接受


	/**
	 * 删除所有闹铃
	 */
	public void delectAllAlarm() {
		Uri uri = Uri.parse("content://com.android.deskclock/alarm");
		ContentResolver mResolver = getContentResolver();
		mResolver.delete(uri, null, null);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// registerReceiver(receiver, mIntentFilter);
	}

	/**
	 * 按返回健停止测试
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			final AlertDialog isExit = new AlertDialog.Builder(this).create();
			isExit.setTitle("系统提示");
			isExit.setMessage("确定要终止测试吗?");
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
						case DialogInterface.BUTTON1:
							NotificationManager notificationManager = (NotificationManager) MainActivity.this
									.getSystemService(NOTIFICATION_SERVICE);
							notificationManager.cancel(0);
							String packagename = getPackageName();
							ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
							if (receiver != null) {
								unregisterReceiver(receiver);
							}
							finish();
							manager.killBackgroundProcesses(packagename);
							break;
						case DialogInterface.BUTTON2:
							isExit.cancel();
							break;
						default:
							break;
					}
				}
			};
			isExit.setButton("确定", listener);
			isExit.setButton2("取消", listener);
			isExit.show();

		}
		return false;
	}

	/**
	 * 添加闹铃线程
	 */
	class Test extends Thread implements Runnable {
		@Override
		public void run() {

			curtimes = 0;// 重置当前次数
			failCount = 0;// 失败次数

			editor.putInt("cishu", TIMES_ALL);
			editor.commit();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String result = "", result_ok = "";
			for (int i = 1; i <= TIMES_ALL; i++) {
				curtimes = i;
				mHandler.sendEmptyMessage(1);
				SystemClock.sleep(2000); // save 5701
				Calendar cal = Calendar.getInstance();
				Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
				int min = cal.getTime().getMinutes();
				int hour = cal.getTime().getHours();
				int second = cal.getTime().getSeconds();
				if (min == 59) {// min+1
					if (is24) { // 23:59----+1min=============00:00
						if (hour == 23)
							hour = 0;
						else
							hour = hour + 1;
					} else {
						if (hour == 12)// 12:59-------+1min===========1:00
							hour = 1;
						else
							hour = hour + 1;
					}
					min = 0;
				} else {
					min = min + 1;
				}

				Log.i("song", "hour:" + hour + " min:" + min);

				/*
				 * intent.putExtra(AlarmClock.EXTRA_HOUR,
				 * cal.getTime().getHours());
				 * intent.putExtra(AlarmClock.EXTRA_MINUTES, cal.getTime()
				 * .getMinutes() + 1);
				 */
				intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
				intent.putExtra(AlarmClock.EXTRA_MINUTES, min);
				// intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
				startActivity(intent);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// touch.clickOnScreen(390, 85); // save
				// SystemClock.sleep(3000);
				/*if (is5701) {
					touch.sendKey(KeyEvent.KEYCODE_BACK);
					SystemClock.sleep(2000); // save 5701
				}*/
				Instrument.clickKey(KeyEvent.KEYCODE_BACK);
				SystemClock.sleep(2000); // save 5701

				try {
					Thread.sleep((62 - second) * 1000);// 多休眠2s
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

//				touch.drag(347, 1061, 650, 1071, 2);//cancel
				//touch.drag(200, 881, 200, 881, 2);// cancel
//				clickOnScreen(533, 162);//close ringingAlarm
				Instrument.click(762, 268);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (!isHasAlarm) {
					failCount = failCount + 1;
					result = "第" + i + "次增加闹铃，闹铃提醒失败";
					result_ok = getTime() + result;
					editor.putString("result" + failCount, result_ok);
				}

				editor.putInt("count", curtimes);
				editor.putInt("failcount", failCount);
				editor.commit();
			}

			mHandler.sendEmptyMessage(2);// finish

		}
	};



	/**
	 * 显示测试结果
	 */
	public void ShowResult() {

		String str = "上一次测试参数:" + "\n";
		Preference.initName("preferences");
		int count= Preference.getInt(this,"count",0);
		int fail=Preference.getInt(this,"failcount",0);
		int cishu=Preference.getInt(this,"cishu",0);
		str += "测试次数:" + cishu;
		str += "测试结果:" + "\n";
		str += "测试总次数为:" + count + "\n";
		str += "失败总次数为:" + fail + "\n";
		for (int i = 1; i <= fail; i++) {
			str +=Preference.getString(this,"result" + i, "")+ "\n";
		}
		info.setText(str);
	}


}