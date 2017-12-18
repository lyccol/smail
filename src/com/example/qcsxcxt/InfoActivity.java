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
	// ͼƬ��������������͵�������λ��
		private final int MINLEN = 30;
		private float x1 = 0;
		private float x2 = 0;
		private float y1 = 0;
		private float y2 = 0;
		private int state_camera;
		private WifiManager wifiManager;
		// ������������
		private DhcpInfo dhcpInfo;
		// С��ip
		private String IPCar;
		// ����ͷIP
		private String IPCamera = "bkrcjk.eicp.net:88";
		private byte[] mByte = new byte[11];
		private socket_connect socket_connect;
		// ���ܴ�����
		long psStatus = 0;// ״̬
		long UltraSonic = 0;// ������
		long Light = 0;// ����
		long CodedDisk = 0;// ����ֵ
		String Camera_show_ip = null;
		
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.info);
	        control_Init();								//�ؼ���ʼ��		
	        wifi_Init();								//WiFi��ʼ��
	        Camer_Init();								//����ͷ��ʼ��
	        search();									//����Service ��������ͷIP
	        socket_connect = new socket_connect();		//ʵ����socket������ 
	        connect_thread();							//�������������߳�
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
			// �õ���������IP��ַ
			wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			dhcpInfo = wifiManager.getDhcpInfo();
			IPCar = Formatter.formatIpAddress(dhcpInfo.gateway);   	
	    }
	    
		// ��������cameraIP������
		private void search() {
			Intent intent = new Intent();
			intent.setClass(InfoActivity.this, SearchService.class);
			startService(intent);
		}
		
		// �㲥����
		public static final String A_S = "com.a_s";
		// ����ͷ����
		private CameraCommandUtil cameraCommandUtil;
		// �㲥����������SearchService����������ͷIP��ַ�Ӷ˿�
		private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
			public void onReceive(Context arg0, Intent arg1) {
				IPCamera = arg1.getStringExtra("IP");
			}
		};
		
	    private void Camer_Init()
	    {  	
	    	//�㲥������ע��
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(A_S);
			registerReceiver(myBroadcastReceiver, intentFilter);
			// ��������ͷͼƬ����
			cameraCommandUtil = new CameraCommandUtil();
	    }
	
	
		public boolean flag_camera;
		// ������ʾС�����͵�����
		private boolean chief_status_flag = true;
		private Handler rehHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					mByte = (byte[]) msg.obj;
					if (mByte[0] == 0x55) {
						// ����״̬
						psStatus = mByte[3] & 0xff;
						// ����������
						UltraSonic = mByte[5] & 0xff;
						UltraSonic = UltraSonic << 8;
						UltraSonic += mByte[4] & 0xff;
						// ����ǿ��
						Light = mByte[7] & 0xff;
						Light = Light << 8;
						Light += mByte[6] & 0xff;
						// ����
						CodedDisk = mByte[9] & 0xff;
						CodedDisk = CodedDisk << 8;
						CodedDisk += mByte[8] & 0xff;
						Camera_show_ip = IPCamera.substring(0, 14);
						if (mByte[1] == (byte) 0xaa) {
							if(chief_status_flag == true)
							{
							// ��ʾ����
							/*txt.setText("WIFIIP:"+IPCar+""+"CameraIP"+Camera_show_ip+"\n"+"������״̬��Ϣ:" + "������:" + UltraSonic
									+ "mm ����:" + Light + "lx" + " ����:" + CodedDisk
									+ "����״̬:" + psStatus + "״̬:" + (mByte[2]));*/
								// ��ʾ����

								gz2.setText(String.valueOf(Light));
								cj2.setText(String.valueOf(UltraSonic));
								zt2.setText(String.valueOf(mByte[2]));
								if(Light>200){
									gz1.setText("��������");
							}
								else {
									gz1.setText("���չ�ǿ");
								}
								if(UltraSonic<100){
									cj1.setText("����");
								}
								else{
									cj1.setText("ͨ��");
								}
								switch(mByte[2]){
								case 1:
									zt1.setText("ѭ��");
									break;
								case 2:
									zt1.setText("ǰ��/�˺�");
									break;
								case 3:
									zt1.setText("��ת/��ת");
									break;
								case 5:
									zt1.setText("բ��");
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
