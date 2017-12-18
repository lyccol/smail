package com.example.qcsxcxt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.Camera_seriver.SearchService;
import com.bkrcl.control_car_video.camerautil.CameraCommandUtil;
import com.car_socket_connect.socket_connect;

public class MainActivity extends Activity {
	private ImageView img;
	private Button btn1,btn2,btn3;
	public TextView txt;
	// 图片区域滑屏监听点击和弹起坐标位置
		private final int MINLEN = 30;
		private float x1 = 0;
		private float x2 = 0;
		private float y1 = 0;
		private float y2 = 0;
		private int state_camera;
		private WifiManager wifiManager;
		// 服务器管理器
		private DhcpInfo dhcpInfo;
		// 小车ip
		private String IPCar;
		// 摄像头IP
		private String IPCamera = "bkrcjk.eicp.net:88";
		private byte[] mByte = new byte[11];
		private socket_connect socket_connect;
		// 接受传感器
		long psStatus = 0;// 状态
		long UltraSonic = 0;// 超声波
		long Light = 0;// 光照
		long CodedDisk = 0;// 码盘值
		String Camera_show_ip = null;
		long car_program_init = 0;
		
		
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.activity_main);
	        control_Init();								//控件初始化		
	        wifi_Init();								//WiFi初始化
	        Camer_Init();								//摄像头初始化
	        search();									//开启Service 搜索摄像头IP
	        socket_connect = new socket_connect();		//实例化socket连接类 
	        connect_thread();							//开启网络连接线程
	    }
	    
	    private void control_Init() {
			// TODO Auto-generated method stub
			txt = (TextView) findViewById(R.id.txt);
			btn1 = (Button) findViewById(R.id.ssck);
			btn2 = (Button) findViewById(R.id.hjcx);
			btn3 = (Button) findViewById(R.id.jgcx);
			img = (ImageView) findViewById(R.id.img);
			img.setOnTouchListener(new ontouchlistener1());
		
			
		}

	  
		private void wifi_Init()
	    {
			// 得到服务器的IP地址
			wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			dhcpInfo = wifiManager.getDhcpInfo();
			IPCar = Formatter.formatIpAddress(dhcpInfo.gateway);   	
	    }
	    
		// 搜索摄像cameraIP进度条
		private void search() {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, SearchService.class);
			startService(intent);
		}
		
		// 广播名称
		public static final String A_S = "com.a_s";
		// 摄像头工具
		private CameraCommandUtil cameraCommandUtil;
		// 广播接收器接受SearchService搜索的摄像头IP地址加端口
			private  BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
				public void onReceive(Context arg0, Intent arg1) {
					IPCamera = arg1.getStringExtra("IP");
					try {
						phThread2.start();
					} catch (Exception e) {
						// TODO: handle exception
					}
					
				}
			};
	    private void Camer_Init()
	    {  	
	    	//广播接收器注册
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(A_S);
			registerReceiver(myBroadcastReceiver, intentFilter);
			// 搜索摄像头图片工具
			cameraCommandUtil = new CameraCommandUtil();
	    }
		// 图片
		private Bitmap bitmap;
		// 得到当前摄像头的图片信息
		public void getBitmap() {
			bitmap = cameraCommandUtil.httpForImage(IPCamera);
			phHandler.sendEmptyMessage(10);
		}
		
		// 显示图片
		public Handler phHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 10) {
					img.setImageBitmap(bitmap);
				}
			}
		};
		
		public boolean flag_camera;
		// 开启线程接受摄像头当前图片
		private Thread phThread2 = new Thread(new Runnable() {
			public void run() {
				while (true) {
					getBitmap();
					switch (state_camera) {
					case 1:
						cameraCommandUtil.postHttp(IPCamera, 0, 1);
						break;
					case 2:
						cameraCommandUtil.postHttp(IPCamera, 2, 1);
						break;
					case 3:
						cameraCommandUtil.postHttp(IPCamera, 4, 1);
						break;
					case 4:
						cameraCommandUtil.postHttp(IPCamera, 6, 1);
						break;
					// /预设位1到3
					case 5:
						cameraCommandUtil.postHttp(IPCamera, 30, 0);
						break;
					case 6:
						cameraCommandUtil.postHttp(IPCamera, 32, 0);
						break;
					case 7:
						cameraCommandUtil.postHttp(IPCamera, 34, 0);
						break;
					case 8:
						cameraCommandUtil.postHttp(IPCamera, 31, 0);
						break;
					case 9:
						cameraCommandUtil.postHttp(IPCamera, 33, 0);
						break;
					case 10:
						cameraCommandUtil.postHttp(IPCamera, 35, 0);
						break;
					default:
						break;
					}
					state_camera = 0;
				}

			}
		});
		// 接受显示小车发送的数据
		private boolean chief_status_flag = true;
		private Handler rehHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					mByte = (byte[]) msg.obj;
					if (mByte[0] == 0x55) {
						// 光敏状态
						psStatus = mByte[3] & 0xff;
						// 超声波数据
						UltraSonic = mByte[5] & 0xff;
						UltraSonic = UltraSonic << 8;
						UltraSonic += mByte[4] & 0xff;
						// 光照强度
						Light = mByte[7] & 0xff;
						Light = Light << 8;
						Light += mByte[6] & 0xff;
						// 码盘
						CodedDisk = mByte[9] & 0xff;
						CodedDisk = CodedDisk << 8;
						CodedDisk += mByte[8] & 0xff;
						Camera_show_ip = IPCamera.substring(0, 14);
						car_program_init = mByte[10] & 0xff;
						
						if (mByte[1] == (byte) 0xaa) {
							if(chief_status_flag == true)
							{
							// 显示数据
							/*txt.setText("WIFIIP:"+IPCar+""+"CameraIP"+Camera_show_ip+"\n"+"主车各状态信息:" + "超声波:" + UltraSonic
									+ "mm 光照:" + Light + "lx" + " 码盘:" + CodedDisk
									+ "光敏状态:" + psStatus + "状态:" + (mByte[2]));*/
								//txt.setText("车牌号:" + OperationActivity.str);
							}
						}
						if(mByte[1] == (byte) 0x02)
						{
							if(chief_status_flag == false)
							{
							// 显示数据
							txt.setText("WIFI模块IP:"+IPCar+"\n"+"从车各状态信息:" + "超声波:" + UltraSonic
									+ "mm 光照:" + Light + "lx" + "码盘:" + CodedDisk
									+ "光敏状态:" + psStatus + "状态:" + (mByte[2]));	
							}
						}
					}
				}
			}
		};
		  public void click(View v){
		    	switch(v.getId()){
		    	case R.id.ssck:
		    	Intent i = new Intent();
				i.setClass(MainActivity.this, OperationActivity.class);
				startActivity(i);
				break;
				case R.id.hjcx:
					final Intent i2 = new Intent();
					i2.setClass(MainActivity.this, InfoActivity.class);
					startActivity(i2);
					break;
				case R.id.jgcx:
					Log.e("Eeeeeeeeeeeeeeeeee", "Eee");
					Intent i3 = new Intent();
					i3.setClass(MainActivity.this, ResultActivity.class);
					startActivity(i3);
		    	}
		    }
		private void connect_thread()
		{
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					socket_connect.connect(rehHandler, IPCar);
				}
			}).start();	
		}
		
		
		//控制摄像头
	    private class ontouchlistener1 implements OnTouchListener
	    {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO 自动生成的方法存根
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				// 点击位置坐标
				case MotionEvent.ACTION_DOWN:
					x1 = event.getX();
					y1 = event.getY();
					break;
				// 弹起坐标
				case MotionEvent.ACTION_UP:
					x2 = event.getX();
					y2 = event.getY();
					float xx = x1 > x2 ? x1 - x2 : x2 - x1;
					float yy = y1 > y2 ? y1 - y2 : y2 - y1;
					// 判断滑屏趋势
					if (xx > yy) {
						if ((x1 > x2) && (xx > MINLEN)) {        // left
							state_camera = 3;
						} else if ((x1 < x2) && (xx > MINLEN)) { // right
							state_camera = 4;
						}
					} else {
						if ((y1 > y2) && (yy > MINLEN)) {        // down
							state_camera = 2;
						} else if ((y1 < y2) && (yy > MINLEN)) { // up
							state_camera = 1;
						}
					}
					x1 = 0;
					x2 = 0;
					y1 = 0;
					y2 = 0;
					break;
				}
				return true;
			}
	    }
	  

}
