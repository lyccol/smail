package com.example.qcsxcxt;

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

public class InfoActivity extends Activity {
	private ImageView img;
	private Button btn1,btn2,btn3;
	private TextView gz1,gz2,cj1,cj2,zt1,zt2;
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
		
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.info);
	        control_Init();								//控件初始化		
	        wifi_Init();								//WiFi初始化
	        Camer_Init();								//摄像头初始化
	        search();									//开启Service 搜索摄像头IP
	        socket_connect = new socket_connect();		//实例化socket连接类 
	        connect_thread();							//开启网络连接线程
	    }
	    
	    private void control_Init() {
	    	gz1=(TextView) findViewById(R.id.gz1);
			gz2=(TextView) findViewById(R.id.gz2);
			cj1=(TextView) findViewById(R.id.cj1);
			cj2=(TextView) findViewById(R.id.cj2);
			zt1=(TextView) findViewById(R.id.zt1);
			zt2=(TextView) findViewById(R.id.zt2);
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
			intent.setClass(InfoActivity.this, SearchService.class);
			startService(intent);
		}
		
		// 广播名称
		public static final String A_S = "com.a_s";
		// 摄像头工具
		private CameraCommandUtil cameraCommandUtil;
		// 广播接收器接受SearchService搜索的摄像头IP地址加端口
		private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
			public void onReceive(Context arg0, Intent arg1) {
				IPCamera = arg1.getStringExtra("IP");
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
	
	
		public boolean flag_camera;
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
						if (mByte[1] == (byte) 0xaa) {
							if(chief_status_flag == true)
							{
							// 显示数据
							/*txt.setText("WIFIIP:"+IPCar+""+"CameraIP"+Camera_show_ip+"\n"+"主车各状态信息:" + "超声波:" + UltraSonic
									+ "mm 光照:" + Light + "lx" + " 码盘:" + CodedDisk
									+ "光敏状态:" + psStatus + "状态:" + (mByte[2]));*/
								// 显示数据

								gz2.setText(String.valueOf(Light));
								cj2.setText(String.valueOf(UltraSonic));
								zt2.setText(String.valueOf(mByte[2]));
								if(Light>200){
									gz1.setText("光照正常");
							}
								else {
									gz1.setText("光照过强");
								}
								if(UltraSonic<100){
									cj1.setText("阻塞");
								}
								else{
									cj1.setText("通畅");
								}
								switch(mByte[2]){
								case 1:
									zt1.setText("循迹");
									break;
								case 2:
									zt1.setText("前进/退后");
									break;
								case 3:
									zt1.setText("左转/右转");
									break;
								case 5:
									zt1.setText("闸门");
									break;
								}
							}
						}
					}
				}
			}
		};
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
		
	

}
