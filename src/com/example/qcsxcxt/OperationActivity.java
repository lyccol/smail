package com.example.qcsxcxt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import RGB_Graying.RGBLuminanceSource;
import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.Camera_seriver.SearchService;
import com.bkrcl.control_car_video.camerautil.CameraCommandUtil;
import com.bkrcl.gametraffictest.util.Coordinates;
import com.car_socket_connect.socket_connect;
import com.coordinate_demo.MyResult;
import com.coordinate_demo.SixShape;
import com.coordinate_demo.Zuobiao;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.googlecode.tesseract.android.TessBaseAPI;

public class OperationActivity extends Activity {
	private TextView title_text, Data_show, voiceText;
	private EditText speed_data, coded_disc_data;
	private ImageButton up_button, left_button, right_button, below_button,
			stop_button;
	int mark=0;
	String carnum;
	private Bitmap bitmaps;
	private Button chief_status_button, chief_control_button;
	public int key = -1;
	
	private static String LANGUAGE = "chi_sim";
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

	/**
	 * ��ѯ����
	 * @param str_num 	����һ����������
	 * @param str 		����������ѯ���
	 * @return			���س��ƺ�
	 */
	//hahaha
	//��������
	String car_num[] = {
		"P779G9", "D582G8", "B543E8", "F467I7",
		"H833E8", "J556C2", "J996E9", "K365G9",
		"B427E8", "D227C3",
	};
	public static String car_num_fun(String car_num[], String str) {
		String str1 = car_num[0];
		
		//�����������飬ʹ��indexOf��������ֵ"str"
		for (int i = 0; i < car_num.length; i++) {
			if(str.indexOf(car_num[i].substring(1,3)) != -1)
				return car_num[i];
		}
		
		return "P779G9";
	}

	/**
	 * 
	 * @param strͨ��ʶ��Ľ�ͨ�ƶ�Ӧ����
	 * @return
	 */
	public static int car_traffic(String car_str){
		int i = 0;
		Random random = new Random();
		if(car_str == "��ɫ����"){
			i = random.nextInt(2)+2;
		}
		if(car_str == "��ɫ����"){
			i = random.nextInt(2);
			if(i == 0) i = 3;
		}
		if(car_str == "��ɫ����"){
			i = 1;
		}
		if(car_str == "��ɫ����"){
			i = 2;
		}
		if(car_str == "��ͷ"){
			i = 3;
		}
		return i;
	}
	
	
	
	
	//public static String str="99999999";

	
	
	int car_program_init = 0;//Ƕ�װ�ť���� 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.operation);
		control_Init(); // �ؼ���ʼ��
		wifi_Init(); // WiFi��ʼ��
		Camer_Init(); // ����ͷ��ʼ��
		search(); // ����Service ��������ͷIP
		socket_connect = new socket_connect(); // ʵ����socket������
		connect_thread(); // �������������߳�
		quanthread.start();
	}

	private void wifi_Init() {
		// �õ���������IP��ַ
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		dhcpInfo = wifiManager.getDhcpInfo();
		IPCar = Formatter.formatIpAddress(dhcpInfo.gateway);
	}

	// ��������cameraIP������
	private void search() {
		Intent intent = new Intent();
		intent.setClass(OperationActivity.this, SearchService.class);
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
			phThread.start();

		}
	};

	private void Camer_Init() {
		// �㲥������ע��
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(A_S);
		registerReceiver(myBroadcastReceiver, intentFilter);
		// ��������ͷͼƬ����
		cameraCommandUtil = new CameraCommandUtil();
	}

	// ͼƬ
	private Bitmap bitmap;

	// �õ���ǰ����ͷ��ͼƬ��Ϣ
	public void getBitmap() {
		bitmap = cameraCommandUtil.httpForImage(IPCamera);
	}

	public boolean flag_camera;
	// �����߳̽�������ͷ��ǰͼƬ
	private Thread phThread = new Thread(new Runnable() {
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
				// /Ԥ��λ1��3
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

	// �ؼ���ʼ������
	private void control_Init() {

		speed_data = (EditText) findViewById(R.id.speed_data);
		coded_disc_data = (EditText) findViewById(R.id.coded_disc_data);

		up_button = (ImageButton) findViewById(R.id.up_button);
		left_button = (ImageButton) findViewById(R.id.left_button);
		right_button = (ImageButton) findViewById(R.id.right_button);
		below_button = (ImageButton) findViewById(R.id.below_button);
		stop_button = (ImageButton) findViewById(R.id.stop_button);
		

		
		up_button.setOnTouchListener(new ontouchlistener2());
		left_button.setOnTouchListener(new ontouchlistener2());
		right_button.setOnTouchListener(new ontouchlistener2());
		below_button.setOnTouchListener(new ontouchlistener2());
		stop_button.setOnTouchListener(new ontouchlistener2());
		
		chief_status_button = (Button) findViewById(R.id.chief_status_button);
		chief_control_button = (Button) findViewById(R.id.chief_control_button);
	}

	// ������ʾС�����͵�����
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
					
					car_program_init = mByte[10] & 0xff;
				}
			}
		}
	};

	private void connect_thread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				socket_connect.connect(rehHandler, IPCar);
			}
		}).start();
	}

	private Timer timer;
	private String result_qr;
	// ��ά�롢���ƴ���
	Handler qrHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 10:

				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						Result result = null;
						Log.i("bitmap", "" + bitmap);
						RGBLuminanceSource rSource = new RGBLuminanceSource(
								bitmap);
						try {
							BinaryBitmap binaryBitmap = new BinaryBitmap(
									new HybridBinarizer(rSource));
							Map<DecodeHintType, String> hint = new HashMap<DecodeHintType, String>();
							hint.put(DecodeHintType.CHARACTER_SET, "utf-8");
							QRCodeReader reader = new QRCodeReader();
							result = reader.decode(binaryBitmap, hint);
							if (result.toString() != null) {
								result_qr = result.toString();
								put(result_qr, "2wm");
								try {
									saveMyBitmap("2wm", bitmap);
								} catch (Exception e) {
									e.printStackTrace();
								}
								timer.cancel();
								qrHandler.sendEmptyMessage(20);
							}
							System.out.println("����ʶ��");
						} catch (NotFoundException e) {
							e.printStackTrace();
						} catch (ChecksumException e) {
							e.printStackTrace();
						} catch (FormatException e) {
							e.printStackTrace();
						}
					}
				}, 0, 200);
				break;
			case 20:
				Toast.makeText(OperationActivity.this, result_qr,
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		};
	};

	// ��������ͷ
	private class ontouchlistener1 implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			// ���λ������
			case MotionEvent.ACTION_DOWN:
				x1 = event.getX();
				y1 = event.getY();
				Log.e("eeeeeee", x1+"ddd"+x2);
				break;
			// ��������
			case MotionEvent.ACTION_UP:
				x2 =event.getX();
				y2 = event.getY();
				Log.e("eeeeeee", x1+"ddd"+x2);
				float xx = x1 > x2 ? x1 - x2 : x2 - x1;
				float yy = y1 > y2 ? y1 - y2 : y2 - y1;
				// �жϻ�������
				if (xx > yy) {
					if ((x1 > x2) && (xx > MINLEN)) { // left
						state_camera = 3;
					} else if ((x1 < x2) && (xx > MINLEN)) { // right
						state_camera = 4;
					}
				} else {
					if ((y1 > y2) && (yy > MINLEN)) { // down
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

	// �ٶȺ����̷���
	private int getSpeed() {
		String src = speed_data.getText().toString();
		int speed = 40;
		if (!src.equals("")) {
			speed = Integer.parseInt(src);
		} else {
			Toast.makeText(OperationActivity.this, "�������ٶ�ֵ", 500).show();
		}
		return speed;
	}

	private int getEncoder() {
		String src = coded_disc_data.getText().toString();
		int encoder = 70;
		if (!src.equals("")) {
			encoder = Integer.parseInt(src);
		} else {
			Toast.makeText(OperationActivity.this, "����������ֵ", 500).show();
		}
		return encoder;
	}

	// �ٶ�������ֵ
	private int sp_n, en_n;
	// ǰ���������ܱ�־λ
	private boolean flag_multiplexing_up_button = true;

	private class ontouchlistener2 implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) // ��������ʱ
			{
				sp_n = getSpeed();
				en_n = getEncoder();
				switch (v.getId()) {
				case R.id.up_button:
					flag_multiplexing_up_button = true;
					up_button.setImageResource(R.drawable.up_button_g);
					System.out.println("����2");
					break;
				case R.id.left_button:
					left_button.setImageResource(R.drawable.left_button_g);
					break;
				case R.id.right_button:
					right_button.setImageResource(R.drawable.right_button_g);
					break;
				case R.id.below_button:
					below_button.setImageResource(R.drawable.below_button_g);
					break;
				case R.id.stop_button:
					stop_button.setImageResource(R.drawable.stop_button_g);
					break;
				}
			}
			if (v.getId() == R.id.up_button) {
				if (event.getEventTime() - event.getDownTime() >= 900) {
					flag_multiplexing_up_button = false;
					up_button.setImageResource(R.drawable.up_button_r);
				}
			}
			if (event.getAction() == MotionEvent.ACTION_UP) { // �����뿪ʱ
				switch (v.getId()) {
				case R.id.up_button:
					if (flag_multiplexing_up_button == false) {
						socket_connect.line(sp_n);
					} else if (flag_multiplexing_up_button == true) {
						socket_connect.go(sp_n, en_n);
					}
					up_button.setImageResource(R.drawable.up_button);
					break;
				case R.id.left_button:
						left_button.setImageResource(R.drawable.left_button);
						socket_connect.left(sp_n);
					break;
				case R.id.right_button:
					right_button.setImageResource(R.drawable.right_button);
					socket_connect.right(sp_n);
					break;
				case R.id.below_button:
					below_button.setImageResource(R.drawable.below_button);
					socket_connect.back(sp_n, en_n);
					break;
				case R.id.stop_button:
					stop_button.setImageResource(R.drawable.stop_button);
					socket_connect.stop();
					break;
				}
			}
			return true;
		}
	}

	private boolean chief_status_flag = true;
	private boolean chief_control_flag = true;

	public void myonClick(View v) throws Exception

	{
		switch (v.getId()) {
		case R.id.cs1://�Զ�1
			car_program_init=1;
//			socket_connect.cs1();
//			socket_connect.yanchi(5000);
//			while(true){
//				if(car_program_init == 0)
//						break;
//			}
//			socket_connect.yanchi(5000);
//			car_program_init = 0;
//			socket_connect.yanchi(2000);
//			socket_connect.cs2();
//			
			break;
		case R.id.qzd://ȫ�Զ�
			key = 0;
			break;
		case R.id.xj://ѭ��
			socket_connect.line(50);
			break;
		case R.id.position_button: // Preset bit
			position_Dialog();
			break;
		case R.id.qr_button: // QR code
			qrHandler.sendEmptyMessage(10);
			System.out.println("��ά���Ѿ�����");
			break;
		case R.id.infrare_button: // infrared
			infrare_Dialog();
			break;
		case R.id.zigbee_button: // zigbee
			zigbee_Dialog();
			break;
//		case R.id.buzzer_button: // buzzer
//			buzzerController();
//			break;
//		case R.id.pilot_lamp_button: // pilot lamp
//			lightController();
//			break;
		case R.id.chief_status_button: // ����ת̬
			chief_status_flag = !chief_status_flag;
			if (chief_status_flag == false) {
				chief_status_button.setText("�ӳ�״̬");
				socket_connect.vice(1);
			}
			if (chief_status_flag == true) {
				chief_status_button.setText("����״̬");
				socket_connect.vice(2);
			}

			break;
		case R.id.chief_control_button: // ��������
			chief_control_flag = !chief_control_flag;
			if (chief_control_flag == false) {
				chief_control_button.setText("�ӳ�����");
				socket_connect.TYPE = 0x02;
			}
			if (chief_control_flag == true) {
				chief_control_button.setText("��������");
				socket_connect.TYPE = 0xAA;
			}
			break;
		case R.id.tx://ͼ��
			bitmaps = convertToBlack(bitmap,1);
			bitmaps=convertToBlackAll11(bitmaps);
			String s = getShapeInfo(bitmaps);
			shape_recognition1(bitmaps);	
			byte[] b;
			  try {
				  b = bytesend(s.getBytes("GBK"));
			      socket_connect.send_voice(b); 
			      } 
			  catch (UnsupportedEncodingException e) { 
				  e.printStackTrace(); 
				  }
			break;
		case R.id.cp://����
			bitmap =cutBorder2(bitmap);
			bitmap = ImgPretreatment
					.converyToGrayImg(bitmap);
			String str = doOcr(bitmap, LANGUAGE);
			 try {
				  b = bytesend(str.getBytes("GBK"));
			      socket_connect.send_voice(b); 
			      } 
			  catch (UnsupportedEncodingException e) { 
				  e.printStackTrace(); 
				  }
		}
	}

	// ָʾ��ң����
	private void lightController() {
		AlertDialog.Builder lt_builder = new AlertDialog.Builder(
				OperationActivity.this);
		lt_builder.setTitle("ָʾ��");
		String[] item = { "����", "ȫ��", "����", "ȫ��" };
		lt_builder.setSingleChoiceItems(item, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							socket_connect.light(1, 0);
						} else if (which == 1) {
							socket_connect.light(1, 1);
						} else if (which == 2) {
							socket_connect.light(0, 1);
						} else if (which == 3) {
							socket_connect.light(0, 0);
						}
						dialog.dismiss();
					}
				});
		lt_builder.create().show();
	}

	private void position_Dialog() {
		final Builder builder = new Builder(OperationActivity.this);
		builder.setTitle("Ԥ��λ����");
		String[] set_item = { "set1", "set2", "set3", "call1", "call2", "call3" };
		builder.setSingleChoiceItems(set_item, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO �Զ����ɵķ������
						state_camera = which + 5;
						Log.i("state", "" + state_camera);
						dialog.cancel();
					}
				});
		builder.create().show();
	}

	// ������
	private void buzzerController() {
		AlertDialog.Builder build = new AlertDialog.Builder(
				OperationActivity.this);
		build.setTitle("������");
		String[] im = { "��", "��" };
		build.setSingleChoiceItems(im, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (which == 0) {
							// �򿪷�����
							socket_connect.buzzer(1);
						} else if (which == 1) {
							// �رշ�����
							socket_connect.buzzer(0);
						}
						dialog.dismiss();
					}
				});
		build.create().show();
	}

	private Thread quanthread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				try {
					cs();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// Log.i("quan", "quan");
		}
	});
	
	

	// ȫ�Զ�����
	private void quan() {
		switch (key) {
		case 0:
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(500);
			}
			key = 1;
			break;
		case 1:
			socket_connect.line(70);
			socket_connect.yanchi(500);
			key = 2;
			break;
		case 2:
			socket_connect.line(70);
			socket_connect.yanchi(500);
			key = 3;
			break;
		case 3:
			socket_connect.left(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(500);
			}
			key = 4;
			break;
		case 4:
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(500);
			}
			key = 5;
			break;
		case 5:
			socket_connect.line(70);
			socket_connect.yanchi(500);
			key = 6;
			break;
		case 6:
			socket_connect.left(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(500);
			}
			key = 7;
			break;
		case 7:
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(500);
			}
			key = 8;
			break;
		case 8:
			socket_connect.left(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(500);
			}
			key = 9;
			break;
		case 9:
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(500);
			}
			key = 10;
			break;
		case 10:
			socket_connect.right(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(500);
			}
			key = 11;
			break;
		case 11:
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(500);
			}
			key = 12;
			break;
		case 12:
			key = -1;
			break;
		}
	}

	private void quan1() {
		String str;
		byte[] b;
		switch (key) {
		case 0:
			socket_connect.gate(1);
			while(mByte[2]!=5);
			socket_connect.yanchi(2000);
			key = 1;
			Log.i("key0", "" + key);
			break;
		case 1:
			socket_connect.digital_open();
			socket_connect.yanchi(3000);
			// ��������
			str = "��բ��,��ʼ��ʱ";
			try {
				b = bytesend(str.getBytes("GBK"));
				socket_connect.send_voice(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			socket_connect.yanchi(1500);
			key = 2;
			Log.i("key1", "" + key);
			break;
		case 2:
			socket_connect.line(70);
			socket_connect.yanchi(2000);
			Log.i("key2", "" + key);
			key = 3;
			break;
		case 3:
			socket_connect.light(1, 0);
			socket_connect.yanchi(2000);
			socket_connect.light(0, 1);
			socket_connect.yanchi(2000);
			socket_connect.light(0, 0);
			socket_connect.yanchi(2000);
			socket_connect.buzzer(1);
			socket_connect.yanchi(3000);
			socket_connect.buzzer(0);
			socket_connect.yanchi(2000);
			key = 4;
			break;
		case 4:
			socket_connect.left(70);
			while (mByte[2] != 2);
			socket_connect.yanchi(2000);
			key = 5;
			break;
		case 5:
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1000);
			}
			socket_connect.yanchi(500);
			Log.i("key5", "" + key);
			key = 6;
			break;
		case 6:
			socket_connect.left(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(1000);
			}
			Log.i("key6", "" + key);
			key = 7;
			break;
		case 7:
			/*socket_connect.gear(1);
			socket_connect.yanchi(1000);
			// ��������
			str = "��ǰ���յ�λ1��";
			try {
				b = bytesend(str.getBytes("GBK"));
				socket_connect.send_voice(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			socket_connect.yanchi(1000);
			socket_connect.gear(2);
			socket_connect.yanchi(2000);
			// ��������
			str = "��ǰ���յ�λ2��";
			try {
				b = bytesend(str.getBytes("GBK"));
				socket_connect.send_voice(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			socket_connect.yanchi(1000);*/
			socket_connect.gear(3);
			socket_connect.yanchi(2000);
			// ��������
			str = "��ǰ���յ�λ3��";
			try {
				b = bytesend(str.getBytes("GBK"));
				socket_connect.send_voice(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			socket_connect.yanchi(3000);
			key = 8;
			break;
		case 8:
			socket_connect.right(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(1000);
			}
			socket_connect.right(70);
			socket_connect.yanchi(2000);
			key = 9;
			break;
		case 9:
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1500);
			}
			socket_connect.yanchi(500);
			socket_connect.go(70, 2);
			socket_connect.yanchi(800);
			key = 10;
			break;
		case 10:
			key = 11;
			break;
		case 11:
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1000);
			}
			socket_connect.go(70, 2);
			socket_connect.yanchi(500);
			key = 12;
			break;
		case 12:
			// ��������
						str = "����������";
						try {
							b = bytesend(str.getBytes("GBK"));
							socket_connect.send_voice(b);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						socket_connect.yanchi(1500);
			socket_connect.infrared((byte) 0x03, (byte) 0x05, (byte) 0x14,
					(byte) 0x45, (byte) 0xDE, (byte) 0x92);
			socket_connect.yanchi(2500);
			socket_connect.infrared((byte) 0x67, (byte) 0x34, (byte) 0x78,
					(byte) 0xA2, (byte) 0xFD, (byte) 0x27);
			socket_connect.yanchi(2000);
			key = 13;
			break;
		case 13:
			socket_connect.left(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(1000);
			}
			Log.i("zha", "" + key);
			key = 14;
			break;
		case 14:
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1000);
			}
			socket_connect.yanchi(500);
			key = 15;
			break;
		case 15:
			socket_connect.right(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(1000);
			}
			key = 16;
			break;
		case 16:
			socket_connect.picture(1);
			socket_connect.yanchi(1500);
			socket_connect.picture(2);
			socket_connect.yanchi(2500);
			key = 17;
			break;
		case 17:
			int s = Integer.parseInt(String.valueOf(UltraSonic));
			socket_connect.digital_dic(s);
			socket_connect.yanchi(1000);
			// ��������
			str = "���Ϊ"+s;
			try {
				b = bytesend(str.getBytes("GBK"));
				socket_connect.send_voice(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			socket_connect.yanchi(2500);
			key = 18;
			break;
		case 18:
			socket_connect.left(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(1000);
			}
			socket_connect.left(70);
			socket_connect.yanchi(2000);
			key = 19;
			break;
		case 19:
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1000);
			}
			socket_connect.yanchi(500);
			socket_connect.go(70, 2);
			socket_connect.yanchi(800);
			socket_connect.left(70);
			socket_connect.yanchi(500);
			socket_connect.stop();
			socket_connect.yanchi(500);
			short[] data2={0x14,0x01,0x00,0x00,0x00};
			socket_connect.infrared_stereo(data2);
			socket_connect.yanchi(1000);
			socket_connect.right(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(1000);
			}
			socket_connect.digital_close();
			key = -1;
			break;
		default:
			break;
		}
	}

	// 4.15
	private void quan2() throws Exception {
		String str;
		byte[] b;
		switch (key) {
		case 0:
		socket_connect.digital_open();
		socket_connect.yanchi(3000);
			key = 1;
			break;
		case 1:
			socket_connect.gate(1);
			while(mByte[2]!=5){
				socket_connect.yanchi(1000);
			}
			socket_connect.yanchi(3000);
			// ��������
			str = "��բ��,��ʼ��ʱ";
			try {
				b = bytesend(str.getBytes("GBK"));
				socket_connect.send_voice(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			socket_connect.yanchi(5000);
			key = 2;
			break;
		case 2:
			socket_connect.line(80);
			while(mByte[2]!=1){
				socket_connect.yanchi(1000);
			}
			socket_connect.yanchi(2000);
			socket_connect.line(80);
			socket_connect.yanchi(2000);
			while(mByte[2]!=1){
				socket_connect.yanchi(1000);
			}
			key = 3;
			break;
		case 3:
			socket_connect.line(80);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1000);
			}
			socket_connect.yanchi(1000);
			socket_connect.go(80, 2);
			socket_connect.yanchi(1000);
			key = 4;
			break;
		case 4:
			int s = Integer.parseInt(String.valueOf(UltraSonic));
			socket_connect.digital_dic(s);
			socket_connect.yanchi(1500);
			str ="���Ϊ" + s;
			put(str, "jl");
			try {
				b = bytesend(str.getBytes("GBK"));
				socket_connect.send_voice(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			socket_connect.yanchi(3000);
			qrHandler.sendEmptyMessage(10);
			socket_connect.yanchi(1000);
			key = 5;
			break;
		case 5:
			socket_connect.left(80);
			while (mByte[2] != 2) {
				socket_connect.yanchi(1000);
			}
			key = 6;
			break;
		case 6:
			socket_connect.line(80);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1000);
			}
			socket_connect.go(50, 3);
			socket_connect.yanchi(1000);
			key = 7;
			break;
		case 7:
			socket_connect.left(80);
			while (mByte[2] != 2) {
				socket_connect.yanchi(1000);
			}
			key = 8;
			break;
		case 8:
			socket_connect.line(80);
			while (mByte[2] != 1) {
				socket_connect.yanchi(500);
			}
			key = 9;
			break;
		case 9:
			// socket_connect.line(50);
			// socket_connect.yanchi(1500);
			key = 10;
			break;
		case 10:
			socket_connect.line(80);
			socket_connect.yanchi(3000);
			key = 11;
			break;
		case 11:
			int d = (int) ((4 - ((300 - 2017 * 10) / 50.0)) % 4 + 1);
			socket_connect.gear(d);
			socket_connect.yanchi(1000);
			 str = "��ǰ��λΪ��" + d;
			try {
				b = bytesend(str.getBytes("GBK"));
				socket_connect.send_voice(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			socket_connect.yanchi(4000);
			key = 12;
			break;
		case 12:
			socket_connect.right(80);
			while (mByte[2] != 2) {
				socket_connect.yanchi(1000);
			}
			socket_connect.line(80);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1000);
			}
			socket_connect.right(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(1000);
			}
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1000);
			}
			socket_connect.yanchi(1000);
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1000);
			}
			socket_connect.yanchi(2000);
			saveMyBitmap("cp", bitmap);
			socket_connect.yanchi(3000);
			bitmap =cutBorder(bitmap);
			bitmap = ImgPretreatment
					.converyToGrayImg(bitmap);
			 str = doOcr(bitmap, LANGUAGE);
			 put(str, "cp");
			 socket_connect.yanchi(3000);
			socket_connect.picture(1);
			socket_connect.yanchi(3000);
			key=13;
			break;
		case 13:
			saveMyBitmap("tx", bitmap);
			socket_connect.yanchi(3000);
			/*bitmaps = convertToBlack(bitmap,1);
			str = getShapeInfo(bitmaps);
			shape_recognition1(bitmaps);*/
			socket_connect.yanchi(3000);
			//put(str, "tx");
			key=14;
			break;
		case 14:
			socket_connect.right(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(1000);
			}
			socket_connect.right(70);
			socket_connect.yanchi(2000);
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1000);
			}
			socket_connect.left(70);
			socket_connect.yanchi(500);
			socket_connect.stop();
			socket_connect.yanchi(1000);
			lt("888888F4");
			socket_connect.yanchi(3000);
			key=15;
			break;
		case 15:
			socket_connect.left(70);
			socket_connect.yanchi(2000);
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1000);
			}
			socket_connect.left(70);
			while (mByte[2] != 2) {
				socket_connect.yanchi(1000);
			}
			socket_connect.line(70);
			while (mByte[2] != 1) {
				socket_connect.yanchi(1000);
			}
			key=16;
			break;
		case 16:
			// ��������
			str = "����������";
			try {
				b = bytesend(str.getBytes("GBK"));
				socket_connect.send_voice(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			socket_connect.yanchi(1500);
			socket_connect.infrared((byte) 0x03, (byte) 0x05, (byte) 0x14,
					(byte) 0x45, (byte) 0xDE, (byte) 0x92);
			socket_connect.yanchi(2500);
			socket_connect.infrared((byte) 0x67, (byte) 0x34, (byte) 0x78,
					(byte) 0xA2, (byte) 0xFD, (byte) 0x27);
			socket_connect.yanchi(2000);
			socket_connect.left(70);
			socket_connect.yanchi(2000);
			socket_connect.line(70);
			socket_connect.yanchi(3000);
			saveMyBitmap("jtd", bitmap);
			 str=shapeIdentification(bitmap, 1);
			 try {
					b = bytesend(str.getBytes("GBK"));
					socket_connect.send_voice(b);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			 put(str, "j");
			 socket_connect.yanchi(2000);
			 socket_connect.left(70);
			 socket_connect.yanchi(2000);
			 socket_connect.line(70);
			 socket_connect.yanchi(2000);
			 socket_connect.line(70);
			 socket_connect.yanchi(2000);
			 socket_connect.buzzer(1);
			 socket_connect.yanchi(3000);
			 socket_connect.buzzer(0);
			 socket_connect.yanchi(2000);
			 socket_connect.digital_close();
			 key=-1;
			 break;
		}
	}
	//4.22
	private void quan3(){
		String  str=null;
		byte[] b=null;
		int s=0;
		switch (key) {
		case 0:
			socket_connect.gate(1);
			socket_connect.yanchi(2000);
			key = 1;
			break;
		case 1:
			socket_connect.digital_open();
			while(mByte[2]!=5);
			socket_connect.yanchi(3000);
			key = 2;
			break;
		case 2:
			socket_connect.line(50);
			while(mByte[2]!=1);
			socket_connect.yanchi(2000);
			key = 3;
			break;
		case 3:
			socket_connect.go(80, 1);
			socket_connect.yanchi(2000);
			socket_connect.left(70);
			socket_connect.yanchi(3000);
			key=4;
			break;
		case 4://ɨ���ά��
			socket_connect.go(70,35);
			for(int i=0;i<=4;i++){
				socket_connect.yanchi(2000);
				state_camera=4;
			}
			socket_connect.yanchi(2000);
			qrHandler.sendEmptyMessage(10);
			socket_connect.yanchi(1000);
			for(int i=0;i<=4;i++){
				socket_connect.yanchi(2000);
				state_camera=3;
			}
			socket_connect.yanchi(3000);
			socket_connect.line(40);
			socket_connect.yanchi(2000);
			socket_connect.go(60, 2);
			socket_connect.yanchi(3000);
			key=5;
			break;
		case 5://ʶ��ͼ��
			try {
				saveMyBitmap("tx", bitmap);
				bitmaps = convertToBlack(bitmap,1);
				bitmaps=convertToBlackAll11(bitmaps);
				 str = getShapeInfo(bitmaps);
				 put("ͼ��ʶ����"+str, "tx");
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}finally{
				socket_connect.yanchi(3000);
				socket_connect.picture(1);
				key=6;
			}
			break;
		case 6://����ʶ��
				socket_connect.yanchi(3000);
			try {
				saveMyBitmap("cp", bitmap);
				bitmaps =cutBorder2(bitmap);
				bitmaps = ImgPretreatment
						.converyToGrayImg(bitmaps);
				 str = doOcr(bitmap, LANGUAGE);
				 carnum=str.substring(str.length()-6, str.length());
				 put(carnum, "cp");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			finally{
				 socket_connect.yanchi(3000);
					key=7;
			}
					break;
		case 7:
			socket_connect.left(70);
			socket_connect.yanchi(3000);
			 s = Integer.parseInt(String.valueOf(UltraSonic));
			try {
				saveMyBitmap("jl", bitmap);
				put("���Ϊ��"+UltraSonic, "jl");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			socket_connect.yanchi(3000);
			socket_connect.digital_dic(s);
			socket_connect.yanchi(3000);
			socket_connect.right(70);
			socket_connect.yanchi(3000);
			socket_connect.right(70);
			socket_connect.yanchi(3000);
			socket_connect.line(40);
			socket_connect.yanchi(2000);
			socket_connect.go(70, 2);
			socket_connect.yanchi(2500);
			socket_connect.right(70);
			socket_connect.yanchi(450);
			socket_connect.stop();
			socket_connect.stop();
			socket_connect.yanchi(2500);
			lt(carnum+"E8");
			socket_connect.yanchi(5500);
			socket_connect.left(55);
			socket_connect.yanchi(550);
			socket_connect.stop();
			socket_connect.stop();
			socket_connect.yanchi(3000);
			key = 8;
			break;
		case 8:
			socket_connect.line(40);
			socket_connect.yanchi(3000);
			socket_connect.go(50, 2);
			socket_connect.yanchi(2000);
			int d =(s/50)%4+1;
			socket_connect.gear(d);
			socket_connect.yanchi(3000);
			key=9;
			break;
		case 9:
			socket_connect.right(70);
			socket_connect.yanchi(3000);
			socket_connect.line(50);
			socket_connect.yanchi(3000);
			socket_connect.line(50);
			socket_connect.yanchi(3000);
			socket_connect.go(60, 2);
			socket_connect.yanchi(2000);
			key=10;
			break;
		case 10:
			try {
				saveMyBitmap("jtd", bitmap);
				 str=shapeIdentification(bitmap, 1);
				 put(str, "j");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			 try {
					b = bytesend(str.getBytes("GBK"));
					socket_connect.send_voice(b);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			 socket_connect.yanchi(4000);
				 socket_connect.left(70);
				 socket_connect.yanchi(3000);
				 socket_connect.line(30);
				 socket_connect.yanchi(3000);
				socket_connect.infrared((byte) 0x03, (byte) 0x05, (byte) 0x14,
							(byte) 0x45, (byte) 0xDE, (byte) 0x92);
				socket_connect.yanchi(2500);
				socket_connect.infrared((byte) 0x67, (byte) 0x34, (byte) 0x78,
							(byte) 0xA2, (byte) 0xFD, (byte) 0x27);
				socket_connect.yanchi(1500);
				socket_connect.back(50, 50);
				socket_connect.yanchi(3000);
				key=11;
				break;
		case 11:
			socket_connect.left(70);
			socket_connect.yanchi(2500);
			socket_connect.line(50);
			socket_connect.yanchi(2500);
			socket_connect.go(60, 2);
			socket_connect.yanchi(3000);
			socket_connect.left(55);
			socket_connect.yanchi(3000);
			socket_connect.line(40);
			socket_connect.yanchi(2000);
			socket_connect.back(70, 145);
			key=12;
			break;
		case 12:
			socket_connect.yanchi(3000);
			socket_connect.buzzer(1);
			socket_connect.yanchi(2000);
			socket_connect.buzzer(0);
			socket_connect.yanchi(3000);
			socket_connect.digital_close();
			socket_connect.yanchi(3000);
			key=-1;
			break;
		default:
			break;
		}
	}
	
	
	public  void  car_program_cj(){
		int s = Integer.parseInt(String.valueOf(UltraSonic));
		socket_connect.digital_dic(s);
		socket_connect.yanchi(1500);
		String st ="���Ϊ" + s;
		put(st, "jl");
	}
//	public void car_program_xz(){   //��״
//		try {
//			saveMyBitmap("tx", bitmap);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		bitmaps = convertToBlack(bitmap,1);
//		bitmaps=convertToBlackAll11(bitmaps);
//		 str = getShapeInfo(bitmaps);
//		 put("ͼ��ʶ����"+str, "tx");
//	}
	//����ʶ��
	static int car_program_1 = 0;
	static int car_program_2 = 0;
	static int car_program_3 = 0;
	static int car_program_4 = 0;
	static int car_program_5 = 0;
	static String str="99999999";
		private void cs(){
			byte[] b;
			switch(car_program_init){
			case 1:
				if(car_program_1++ == 0){
					car_program_cj();
					socket_connect.yanchi(2000);
					put(str, "cp");
					socket_connect.cs1();
				}
				break;
			case 2:
				if(car_program_2++ == 0){
					socket_connect.yanchi(10000);
					for(int i=0;i<=5;i++){
					socket_connect.yanchi(2000);
					state_camera=3;
					socket_connect.yanchi(3000);
					}
					socket_connect.yanchi(2000);
					qrHandler.sendEmptyMessage(10);
					socket_connect.yanchi(1000);
					qrHandler.sendEmptyMessage(10);
					for(int i=0;i<=5;i++){
						socket_connect.yanchi(2000);
						state_camera=4;
						socket_connect.yanchi(3000);
					}
					try {
						b = bytesend(result_qr.getBytes("GBK"));
						socket_connect.send_voice(b);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					socket_connect.yanchi(4000);
					socket_connect.cs2();
				}
					break;
			case 3:
				if(car_program_3++ == 0){
					socket_connect.yanchi(12000);
					socket_connect.picture(0);
					socket_connect.yanchi(4000);
					try {
						saveMyBitmap("cp", bitmap);
						bitmaps =cutBorder2(bitmap);
						bitmaps = ImgPretreatment
								.converyToGrayImg(bitmaps);
						str = doOcr(bitmaps, LANGUAGE);
						str=str.substring(str.length()-6, str.length());
						str = car_num_fun(car_num, str);
						 put(str, "cp");
						 socket_connect.cs3();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
					break;
			case 4:
				if(car_program_4++ == 0){
					socket_connect.yanchi(12000);
					put(str + "00", "cp");
					lt(str + "00");
					socket_connect.yanchi(4000);
					socket_connect.cs4();
				}
				break;
			case 5:
				if(car_program_5++ == 0){
					socket_connect.yanchi(12000);
					try {
						saveMyBitmap("jtd", bitmap);
						str=shapeIdentification(bitmap, 1);
						
						if(car_traffic(str) == 1)
						{
							socket_connect.cs5();
						}else if (car_traffic(str) == 2) {
							socket_connect.cs6();
						}else {
							socket_connect.cs7();
						}
						put(str, "j");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				break;
			}
		}
	//���Է���   һ����ťʵ�ֵײ�Ͱ�׿�����ӵײ�Ĺ���
//	static int car_program_1 = 0;
//	static int car_program_2 = 0;
//	static int car_program_3 = 0;
//		private void cs(){
//			switch(car_program_init){
//			case  1:
//				if(car_program_1++ == 0){
//				socket_connect.cs1();
//				}
//				break;
//			case 2:
//				socket_connect.yanchi(12000);
//				socket_connect.left(70);
//				socket_connect.yanchi(5000);
//				socket_connect.right(70);
//				socket_connect.yanchi(5000);
//				car_program_init=3;
//				break;
//			case 3:
//				if(car_program_2++ == 0){
//					socket_connect.cs2();
//			}
//				break;
//		}
//		}
	 private void quan4(){
		 String str="88888888";
		 switch(key){
		 case 0:
			socket_connect.digital_open();
			 socket_connect.yanchi(2000);
			 socket_connect.line(50);
			 socket_connect.yanchi(2000);
			 key=1;
			 break;
		 case 1:
			 socket_connect.left(70);
			 socket_connect.yanchi(3000);
			 socket_connect.line(50);
			 socket_connect.yanchi(2500);
			 socket_connect.line(50);
			 socket_connect.yanchi(2500);
			 socket_connect.left(70);
			 socket_connect.yanchi(2500);
			 socket_connect.line(50);
			 socket_connect.yanchi(2500);
			 key=2;
			 break;
		 case 2:
			 socket_connect.gear(2);
			 socket_connect.yanchi(3000);
			 socket_connect.back(50, 50);
			 socket_connect.yanchi(2000);
			socket_connect.left(70);
			socket_connect.yanchi(2500);
			socket_connect.line(50);
			socket_connect.yanchi(2500);
			socket_connect.line(50);
			socket_connect.yanchi(2500);
			socket_connect.left(70);
			key=3;
			break;
		 case 3:
			socket_connect.yanchi(2500);
			socket_connect.line(40);
			 socket_connect.yanchi(6500);
			 socket_connect.line(50);
			socket_connect.yanchi(2500);
			socket_connect.left(70);
			socket_connect.yanchi(2500);
			socket_connect.line(50);
			socket_connect.yanchi(3000);
			socket_connect.line(50);
			socket_connect.yanchi(2500);
			socket_connect.right(70);
			socket_connect.yanchi(2000);
			key=4;
			break;
		 case 4:
			socket_connect.line(50);
			socket_connect.yanchi(2500);
			socket_connect.left(80);
			socket_connect.yanchi(3000);
			socket_connect.picture(0);
			socket_connect.yanchi(4000);
			try {
				saveMyBitmap("cp", bitmap);
				bitmap =cutBorder2(bitmap);
				bitmap = ImgPretreatment
						.converyToGrayImg(bitmap);
				str = doOcr(bitmap, LANGUAGE);
				put("��"+str, "cp");
			} catch (Exception e) {
				// TODO: handle exception
			}finally{
				socket_connect.yanchi(2000);
				socket_connect.left(70);
				socket_connect.yanchi(2000);
				socket_connect.line(50);
				socket_connect.yanchi(2000);
				socket_connect.go(70, 1);
				socket_connect.yanchi(3000);
				socket_connect.right(70);
				socket_connect.yanchi(600);
				socket_connect.stop();
				socket_connect.yanchi(2000);
				lt(str);
				socket_connect.yanchi(4000);
				socket_connect.left(60);
				socket_connect.yanchi(3000);
				key=5;
			}
			break;
		 case 5:
			socket_connect.line(40);
			socket_connect.yanchi(2000);
			socket_connect.go(70, 1);
			socket_connect.yanchi(3000);
			socket_connect.left(70);
			socket_connect.yanchi(2000);
			key=6;
			break;
		 case 6:
			socket_connect.yanchi(2000);
			socket_connect.line(70);
			socket_connect.yanchi(2500);
			key=7;
			break;
		 case 7:
				 key=8;
			 break;
		 case 8:
			socket_connect.left(70);
			socket_connect.yanchi(2500);
			socket_connect.line(50);
			socket_connect.yanchi(2500);
			socket_connect.right(70);
			socket_connect.yanchi(2500);
			socket_connect.line(50);
			socket_connect.yanchi(2500);
			socket_connect.left(70);
			socket_connect.yanchi(2500);
			socket_connect.gate(1);
			socket_connect.yanchi(2500);
			socket_connect.line(35);
			socket_connect.yanchi(3000);
			socket_connect.buzzer(1);
			socket_connect.yanchi(2100);
			socket_connect.buzzer(0);
			socket_connect.yanchi(3000);
			socket_connect.digital_close();
			socket_connect.yanchi(2000);
			key=-1;
			break;
		 }
	 }
	
	 private void infrare_Dialog() {
		Builder builder = new Builder(OperationActivity.this);
		builder.setTitle("����");
		String[] infrare_item = { "������", "��λ��", "����", "������ʾ", "LCD��ʾ��" };
		builder.setSingleChoiceItems(infrare_item, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO �Զ����ɵķ������
						switch (which) {
						case 0:
							policeController();
							break;
						case 1:
							gearController();
							break;
						case 2:
							// 0x00,0xFF,0x45,~(0x45)
							socket_connect.infrared((byte) 0x03, (byte) 0x05,
									(byte) 0x14, (byte) 0x45, (byte) 0xDE,
									(byte) 0x92);
							// socket_connect.fan();
							break;
						case 3:
							threeDisplay();
							break;
						case 4:
							page();
							break;
						}
						dialog.cancel();
					}
				});
		builder.create().show();
	}

	// LCD��ʾ��
	private void page() {
		Builder builder = new Builder(OperationActivity.this);
		builder.setTitle("��ҳ");
		String[] item2 = { "�Ϸ�", "�·�" };
		builder.setSingleChoiceItems(item2, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						int page = which + 1;
						socket_connect.picture(page);
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	// ������
	private void policeController() {
		Builder builder = new Builder(OperationActivity.this);
		builder.setTitle("������");
		String[] item2 = { "��", "��" };
		builder.setSingleChoiceItems(item2, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (which == 0) {
							socket_connect.infrared((byte) 0x03, (byte) 0x05,
									(byte) 0x14, (byte) 0x45, (byte) 0xDE,
									(byte) 0x92);
						} else if (which == 1) {
							socket_connect.infrared((byte) 0x67, (byte) 0x34,
									(byte) 0x78, (byte) 0xA2, (byte) 0xFD,
									(byte) 0x27);
						}
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private void gearController() {
		Builder builder = new AlertDialog.Builder(OperationActivity.this);
		builder.setTitle("��λң����");
		String[] gr_item = { "��ǿ��1��", "��ǿ��2��", "��ǿ��3��" };
		builder.setSingleChoiceItems(gr_item, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (which == 0) {// ��һ��
							socket_connect.gear(1);
						} else if (which == 1) {// �Ӷ���
							socket_connect.gear(2);
						} else if (which == 2) {// ������
							socket_connect.gear(3);
						}
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private short[] data = { 0x00, 0x00, 0x00, 0x00, 0x00 };

	private void threeDisplay() {
		Builder Builder = new Builder(OperationActivity.this);
		Builder.setTitle("������ʾ");
		String[] three_item = { "��ɫ��Ϣ", "ͼ����Ϣ", "������Ϣ", "������Ϣ", "·����Ϣ", "Ĭ����Ϣ" };
		Builder.setSingleChoiceItems(three_item, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (which) {
						case 0:
							color();
							break;
						case 1:
							shape();
							break;
						case 2:
							dis();
							break;
						case 3:
							lic();
							break;
						case 4:
							road();
							break;
						case 5:
							data[0] = 0x15;
							data[1] = 0x01;
							socket_connect.infrared_stereo(data);
							break;
						default:
							break;
						}
						dialog.cancel();
					}
				});
		Builder.create().show();
	}

	private void color() {
		Builder colorBuilder = new AlertDialog.Builder(this);
		colorBuilder.setTitle("��ɫ��Ϣ");
		String[] lg_item = { "��ɫ", "��ɫ", "��ɫ", "��ɫ", "��ɫ", "��ɫ", "��ɫ", "��ɫ" };
		colorBuilder.setSingleChoiceItems(lg_item, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						data[0] = 0x13;
						data[1] = (short) (which + 0x01);
						socket_connect.infrared_stereo(data);
						dialog.cancel();
					}
				});
		colorBuilder.create().show();
	}

	private void shape() {
		Builder shapeBuilder = new AlertDialog.Builder(this);
		shapeBuilder.setTitle("ͼ����Ϣ");
		String[] shape_item = { "����", "Բ��", "������", "����", "����", "��ͼ", "��ͼ",
				"����ͼ" };
		shapeBuilder.setSingleChoiceItems(shape_item, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						data[0] = 0x12;
						data[1] = (short) (which + 0x01);
						socket_connect.infrared_stereo(data);
						dialog.cancel();
					}
				});
		shapeBuilder.create().show();
	}

	private void road() {
		Builder roadBuilder = new AlertDialog.Builder(this);
		roadBuilder.setTitle("·����Ϣ");
		String[] road_item = { "������¹ʣ�������", "ǰ��ʩ����������" };
		roadBuilder.setSingleChoiceItems(road_item, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						data[0] = 0x14;
						data[1] = (short) (which + 0x01);
						socket_connect.infrared_stereo(data);
						dialog.cancel();
					}
				});
		roadBuilder.create().show();
	}

	private void dis() {
		Builder disBuilder = new AlertDialog.Builder(this);
		disBuilder.setTitle("������Ϣ");
		final String[] road_item = { "10cm", "15cm", "20cm", "28cm", "39cm" };
		disBuilder.setSingleChoiceItems(road_item, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int disNum = Integer.parseInt(road_item[which]
								.substring(0, 2));
						data[0] = 0x11;
						data[1] = (short) (disNum / 10 + 0x30);
						data[2] = (short) (disNum % 10 + 0x30);
						socket_connect.infrared_stereo(data);
						dialog.cancel();
					}
				});
		disBuilder.create().show();
	}

	// ��string�еõ�short��������
	private short[] StringToBytes(String licString) {
		if (licString == null || licString.equals("")) {
			return null;
		}
		licString = licString.toUpperCase();
		int length = licString.length();
		char[] hexChars = licString.toCharArray();
		short[] d = new short[length];
		for (int i = 0; i < length; i++) {
			d[i] = (short) hexChars[i];
		}
		return d;
	}
  private void lt(String num){
		short[] li = StringToBytes(num);
		data[0] = 0x20;
		data[1] = (short) (li[0]);
		data[2] = (short) (li[1]);
		data[3] = (short) (li[2]);
		data[4] = (short) (li[3]);
		socket_connect.infrared_stereo(data);
		data[0] = 0x10;
		data[1] = (short) (li[4]);
		data[2] = (short) (li[5]);
		data[3] = (short) (li[6]);
		data[4] = (short) (li[7]);
		socket_connect.infrared_stereo(data);
  }
	private Handler licHandler = new Handler() {
		public void handleMessage(Message msg) {
			short[] li = StringToBytes(lic_item[msg.what]);
			data[0] = 0x20;
			data[1] = (short) (li[0]);
			data[2] = (short) (li[1]);
			data[3] = (short) (li[2]);
			data[4] = (short) (li[3]);
			socket_connect.infrared_stereo(data);
			data[0] = 0x10;
			data[1] = (short) (li[4]);
			data[2] = (short) (li[5]);
			data[3] = (short) (li[6]);
			data[4] = (short) (li[7]);
			socket_connect.infrared_stereo(data);
		};
	};
	private int lic = -1;
	private String[] lic_item = { "N300Y7A4", "N600H5B4", "N400Y6G6",
			"J888B8C8" };

	private void lic() {
		Builder licBuilder = new AlertDialog.Builder(this);
		licBuilder.setTitle("������Ϣ");
		licBuilder.setSingleChoiceItems(lic_item, lic,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						lic = which;
						licHandler.sendEmptyMessage(which);
						dialog.cancel();
					}
				});
		licBuilder.create().show();
	}

	private void zigbee_Dialog() {
		Builder builder = new AlertDialog.Builder(OperationActivity.this);
		builder.setTitle("zigbee");
		String[] zg_item = { "բ��", "�����", "��������", "������", "TFT��ʾ��" };
		builder.setSingleChoiceItems(zg_item, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							gateController(); // բ��
							break;
						case 1:
							digital(); // �����
							break;
						case 2:
							voiceController(); // ��������
							break;
						case 3:
							magnetic_suspension(); // ������
							break;
						case 4:
							TFT_LCD(); // TFTҺ����ʾ
							break;
						}
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private boolean flag_voice;

	private void voiceController() {
		View view = LayoutInflater.from(OperationActivity.this).inflate(
				R.layout.item_car, null);
		voiceText = (EditText) view.findViewById(R.id.voiceText);

		Builder voiceBuilder = new AlertDialog.Builder(OperationActivity.this);
		voiceBuilder.setTitle("��������");
		voiceBuilder.setView(view);
		voiceBuilder.setPositiveButton("����",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String src = voiceText.getText().toString();
						if (src.equals("")) {
							src = "��������Ҫ����������";
						}
						try {
							flag_voice = true;
							byte[] sbyte = bytesend(src.getBytes("GBK"));
							socket_connect.send_voice(sbyte);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						dialog.cancel();
					}
				});
		voiceBuilder.setNegativeButton("ȡ��", null);
		voiceBuilder.create().show();
	}

	private byte[] bytesend(byte[] sbyte) {
		byte[] textbyte = new byte[sbyte.length + 5];
		textbyte[0] = (byte) 0xFD;
		textbyte[1] = (byte) (((sbyte.length + 2) >> 8) & 0xff);
		textbyte[2] = (byte) ((sbyte.length + 2) & 0xff);
		textbyte[3] = 0x01;// �ϳ���������
		textbyte[4] = (byte) 0x01;// �����ʽ
		for (int i = 0; i < sbyte.length; i++) {
			textbyte[i + 5] = sbyte[i];
		}
		return textbyte;
	}

	private void gateController() {
		Builder gt_builder = new AlertDialog.Builder(OperationActivity.this);
		gt_builder.setTitle("բ�ſ���");
		String[] gt = { "��", "��" };
		gt_builder.setSingleChoiceItems(gt, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							// ��բ��
							socket_connect.gate(1);
						} else if (which == 1) {
							// �ر�բ��
							socket_connect.gate(2);
						}
						dialog.dismiss();
					}
				});
		gt_builder.create().show();
	}

	private void digital() {// �����
		AlertDialog.Builder dig_timeBuilder = new AlertDialog.Builder(
				OperationActivity.this);
		dig_timeBuilder.setTitle("�����");
		String[] dig_item = { "�������ʾ", "����ܼ�ʱ", "��ʾ����" };
		dig_timeBuilder.setSingleChoiceItems(dig_item, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (which == 0) {// �������ʾ
							digitalController();

						} else if (which == 1) {// ����ܼ�ʱ
							digital_time();

						} else if (which == 2) {// ��ʾ����
							digital_dis();

						}
						dialog.dismiss();
					}
				});
		dig_timeBuilder.create().show();
	}

	private int dgtime_index = -1;

	private void digital_time() {// ����ܼ�ʱ
		AlertDialog.Builder dg_timeBuilder = new AlertDialog.Builder(
				OperationActivity.this);
		dg_timeBuilder.setTitle("����ܼ�ʱ");
		String[] dgtime_item = { "��ʱ����", "��ʱ��ʼ", "��ʱ����" };
		dg_timeBuilder.setSingleChoiceItems(dgtime_item, dgtime_index,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.i("****", "" + which);
						if (which == 0) {// ��ʱ����
							socket_connect.digital_close();

						} else if (which == 1) {// ��ʱ����
							socket_connect.digital_open();
							Log.i("��ʱ����", "111111111111111111111111");

						} else if (which == 2) {// ��ʱ����
							socket_connect.digital_clear();

						}
						dialog.dismiss();
					}
				});
		dg_timeBuilder.create().show();
	}

	private int dgdis_index = -1;

	private void digital_dis() {
		AlertDialog.Builder dis_timeBuilder = new AlertDialog.Builder(
				OperationActivity.this);
		dis_timeBuilder.setTitle("��ʾ����");
		final String[] dis_item = { "10cm", "20cm", "40cm", "���" };
		dis_timeBuilder.setSingleChoiceItems(dis_item, dgdis_index,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {// ����10cm
							socket_connect.digital_dic(Integer
									.parseInt(dis_item[which].substring(0, 2)));
						} else if (which == 1) {// ����20cm
							socket_connect.digital_dic(Integer
									.parseInt(dis_item[which].substring(0, 2)));
						} else if (which == 2) {// ����40cm
							socket_connect.digital_dic(Integer
									.parseInt(dis_item[which].substring(0, 2)));
						} else if (which == 3) {// ���
							int c = Integer.parseInt(String.valueOf(UltraSonic));
							socket_connect.digital_dic(c);
							Log.i("ceju", "" + UltraSonic);
						}
						dialog.dismiss();
					}
				});
		dis_timeBuilder.create().show();
	}

	// �������ʾ����
	private String[] itmes = { "1", "2" };
	int main, one, two, three;

	private void digitalController() {

		AlertDialog.Builder dg_Builder = new AlertDialog.Builder(
				OperationActivity.this);
		View view = LayoutInflater.from(OperationActivity.this).inflate(
				R.layout.item_digital, null);
		dg_Builder.setTitle("�������ʾ");
		dg_Builder.setView(view);
		// �����б�
		Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
		final EditText editText1 = (EditText) view.findViewById(R.id.editText1);
		final EditText editText2 = (EditText) view.findViewById(R.id.editText2);
		final EditText editText3 = (EditText) view.findViewById(R.id.editText3);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				OperationActivity.this, android.R.layout.simple_spinner_item,
				itmes);
		spinner.setAdapter(adapter);
		// �����б�ѡ�����
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				main = position + 1;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});
		dg_Builder.setPositiveButton("ȷ��",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String ones = editText1.getText().toString();
						String twos = editText2.getText().toString();
						String threes = editText3.getText().toString();
						// ��ʾ���ݣ�һ���ı�������������������ʾ��Ŀ������������
						if (ones.equals(""))
							one = 0x00;
						else
							one = Integer.parseInt(ones) / 10 * 16
									+ Integer.parseInt(ones) % 10;
						if (twos.equals(""))
							two = 0x00;
						else
							two = Integer.parseInt(twos) / 10 * 16
									+ Integer.parseInt(twos) % 10;
						if (threes.equals(""))
							three = 0x00;
						else
							three = Integer.parseInt(threes) / 10 * 16
									+ Integer.parseInt(threes) % 10;
						socket_connect.digital(main, one, two, three);
					}
				});

		dg_Builder.setNegativeButton("ȡ��",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
		dg_Builder.create().show();
	}

	private void magnetic_suspension() {
		Builder builder = new Builder(OperationActivity.this);
		builder.setTitle("������");
		String[] item2 = { "��", "��" };
		builder.setSingleChoiceItems(item2, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (which == 0) {
							socket_connect.magnetic_suspension(0x01, 0x01,
									0x00, 0x00);
						} else if (which == 1) {
							socket_connect.magnetic_suspension(0x01, 0x02,
									0x00, 0x00);
						}
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private void TFT_LCD() {
		Builder TFTbuilder = new Builder(OperationActivity.this);
		TFTbuilder.setTitle("TFT��ʾ��");
		String[] TFTitem = { "ͼƬ��ʾģʽ", "������ʾ", "��ʱģʽ", "������ʾ", "HEX��ʾģʽ" };
		TFTbuilder.setSingleChoiceItems(TFTitem, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (which) {
						case 0:
							TFT_Image();
							break;
						case 1:
							TFT_plate_number();
							break;
						case 2:
							TFT_Timer();
							break;
						case 3:
							Distance();
							break;
						case 4:
							Hex_show();
							break;
						}
						dialog.dismiss();
					}
				});
		TFTbuilder.create().show();
	}

	private void TFT_Image() {
		Builder TFT_Image_builder = new Builder(OperationActivity.this);
		TFT_Image_builder.setTitle("ͼƬ��ʾģʽ");
		String[] TFT_Image_item = { "ָ����ʾ", "�Ϸ�һҳ", "�·�һҳ", "�Զ���ҳ" };
		TFT_Image_builder.setSingleChoiceItems(TFT_Image_item, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO �Զ����ɵķ������
						switch (which) {
						case 0:
							LCD_vo_show();
							break;
						case 1:
							socket_connect.TFT_LCD(0x10, 0x01, 0x00, 0x00);
							break;
						case 2:
							socket_connect.TFT_LCD(0x10, 0x02, 0x00, 0x00);
							break;
						case 3:
							socket_connect.TFT_LCD(0x10, 0x03, 0x00, 0x00);
							break;
						}
						dialog.cancel();
					}
				});
		TFT_Image_builder.create().show();
	}

	private void LCD_vo_show() {
		Builder TFT_Image_builder = new Builder(OperationActivity.this);
		TFT_Image_builder.setTitle("ָ��ͼƬ��ʾ");
		String[] TFT_Image_item = { "1", "2", "3", "4", "5" };
		TFT_Image_builder.setSingleChoiceItems(TFT_Image_item, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO �Զ����ɵķ������
						switch (which) {
						case 0:
							socket_connect.TFT_LCD(0x00, 0x01, 0x00, 0x00);
							break;
						case 1:
							socket_connect.TFT_LCD(0x00, 0x02, 0x00, 0x00);
							break;
						case 2:
							socket_connect.TFT_LCD(0x00, 0x03, 0x00, 0x00);
							break;
						case 3:
							socket_connect.TFT_LCD(0x00, 0x04, 0x00, 0x00);
							break;
						case 4:
							socket_connect.TFT_LCD(0x00, 0x05, 0x00, 0x00);
							break;
						}
						dialog.cancel();
					}
				});
		TFT_Image_builder.create().show();
	}

	int Car_one, Car_two, Car_three, Car_four, Car_five, Car_six;

	private void TFT_plate_number() {
		Builder TFT_plate_builder = new Builder(OperationActivity.this);
		TFT_plate_builder.setTitle("������ʾģʽ");
		final String[] TFT_Image_item = { "A123B4", "B567C8", "D910E1" };
		TFT_plate_builder.setSingleChoiceItems(TFT_Image_item, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO �Զ����ɵķ������
						switch (which) {
						case 0:
							socket_connect.TFT_LCD(0x20, 'A', '1', '2');
							socket_connect.TFT_LCD(0x20, '3', 'B', '4');
							break;
						case 1:
							socket_connect.TFT_LCD(0x20, 'B', '5', '6');
							socket_connect.TFT_LCD(0x20, '7', 'C', '8');
							break;
						case 2:
							socket_connect.TFT_LCD(0x20, 'D', '9', '1');
							socket_connect.TFT_LCD(0x20, '0', 'E', '1');
							break;
						}
						dialog.cancel();
					}
				});
		TFT_plate_builder.create().show();
	}

	private void TFT_Timer() {
		Builder TFT_Iimer_builder = new Builder(OperationActivity.this);
		TFT_Iimer_builder.setTitle("��ʱģʽ");
		String[] TFT_Image_item = { "��ʼ", "�ر�", "ֹͣ" };
		TFT_Iimer_builder.setSingleChoiceItems(TFT_Image_item, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO �Զ����ɵķ������
						switch (which) {
						case 0:
							socket_connect.TFT_LCD(0x30, 0x01, 0x00, 0x00);
							break;
						case 1:
							socket_connect.TFT_LCD(0x30, 0x02, 0x00, 0x00);
							break;
						case 2:
							socket_connect.TFT_LCD(0x30, 0x00, 0x00, 0x00);
							break;
						}
						dialog.cancel();
					}
				});
		TFT_Iimer_builder.create().show();
	}

	private void Distance() {
		Builder TFT_Distance_builder = new Builder(OperationActivity.this);
		TFT_Distance_builder.setTitle("������ʾģʽ");
		String[] TFT_Image_item = { "10cm", "20cm", "30cm" };
		TFT_Distance_builder.setSingleChoiceItems(TFT_Image_item, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO �Զ����ɵķ������
						if (which == 0) {
							socket_connect.TFT_LCD(0x50, 0x00, 0x01, 0x00);
						}
						if (which == 1) {
							socket_connect.TFT_LCD(0x50, 0x00, 0x02, 0x00);
						}
						if (which == 2) {
							socket_connect.TFT_LCD(0x50, 0x00, 0x03, 0x00);
						}
						dialog.cancel();
					}
				});
		TFT_Distance_builder.create().show();
	}

	private void Hex_show() {
		Builder TFT_Hex_builder = new Builder(OperationActivity.this);
		TFT_Hex_builder.setTitle("HEX��ʾģʽ");
		String[] TFT_Image_item = { "�ݶ�" };
		TFT_Hex_builder.setSingleChoiceItems(TFT_Image_item, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO �Զ����ɵķ������
					}
				});
		TFT_Hex_builder.create().show();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// socket_connect.onDestory();
	}

	// ��ͨ��
	/*
	 * // // ����ͼƬ��ߵ��������� ArrayList<Coordinates> rlistl = new
	 * ArrayList<Coordinates>(); ArrayList<Coordinates> glistl = new
	 * ArrayList<Coordinates>(); // // ����ͼƬ���ұߵ��������� ArrayList<Coordinates>
	 * rlistr = new ArrayList<Coordinates>(); ArrayList<Coordinates> glistr =
	 * new ArrayList<Coordinates>(); private Bitmap convertToBlack(Bitmap bip)
	 * {// ���ش�������Ϊ��ɫ�����̻Ʋ���
	 * 
	 * int width = bip.getWidth(); int height = bip.getHeight(); int[] pixels =
	 * new int[width * height]; bip.getPixels(pixels, 0, width, 0, 0, width,
	 * height); int[] pl = new int[bip.getWidth() * bip.getHeight()]; for (int y
	 * = 0; y < height; y++) { int offset = y * width; for (int x = 0; x <
	 * width; x++) { int pixel = pixels[offset + x]; int r = (pixel >> 16) &
	 * 0xff; int g = (pixel >> 8) & 0xff; int b = pixel & 0xff; if (r > 200 && g
	 * < 150 && b < 150) {// ��ɫ pl[offset + x] = 0xffff0000;; } else if (r < 200
	 * && g > 150 && b < 150) {// ��ɫ pl[offset + x] = 0xff00ff00;; } else {
	 * pl[offset + x] = 0xff000000;// ��ɫ } } } Log.i("ttt", "�ڶ���111111"); Bitmap
	 * result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	 * result.setPixels(pl, 0, width, 0, 0, width, height); return result;
	 * 
	 * }
	 * 
	 * private String shapeIdentification(Bitmap bp, int flag) {
	 * 
	 * bp=convertToBlack(bp); rlistl.clear(); glistl.clear(); rlistr.clear();
	 * glistr.clear(); int search_index=0; int width = bp.getWidth(); int height
	 * = bp.getHeight(); int[] pixels = new int[width * height];
	 * bp.getPixels(pixels, 0, width, 0, 0, width, height);
	 * 
	 * 
	 * for(int y=0;y<height;y++){ for (int x = width - 1; x > 0; x--)
	 * {//�õ����ұߵ����� int pixel = pixels[y*width + x]; if (pixel != 0xff000000) {
	 * int r = (pixel >> 16) & 0xff; int g = (pixel >> 8) & 0xff; int b = pixel
	 * & 0xff; if (r > 200 ) {// ��ɫ rlistl.add(new Coordinates(x, y)); break; }
	 * else if ( g > 150 ) {// ��ɫ glistl.add(new Coordinates(x, y)); break; } }
	 * } }
	 * 
	 * boolean flag1=false; int colum=0; for (int x = width - 1; x > 0; x--)
	 * {//�õ����ұߵ����� for(int y=0;y<height;y++){ int pixel = pixels[y*width + x];
	 * if (pixel != 0xff000000) { int r = (pixel >> 16) & 0xff; int g = (pixel
	 * >> 8) & 0xff; int b = pixel & 0xff; if (r > 200 ) {// ��ɫ flag1=true; if
	 * (colum >=3) { rlistr.add(new Coordinates(x, y)); search_index++; } } else
	 * if ( g > 150 ) {// ��ɫ flag1=true; if (colum >=3) { glistr.add(new
	 * Coordinates(x, y)); search_index++; } } } }
	 * 
	 * if (flag1) { colum++; flag1=false; }
	 * 
	 * if (colum >=3) { if(search_index>2)//Ѱ�ҵ����ұ����ص� { break;
	 * 
	 * } }
	 * 
	 * } if (rlistl.size() > glistl.size() ) { shape(rlistl,rlistr,1);
	 * Log.e("��ɫ����", rlistl.size() + ""); }
	 * 
	 * else if (glistl.size() > rlistl.size()) { shape(glistl,glistr,2);
	 * Log.e("��ɫ����", glistl.size() + ""); }
	 * 
	 * return shapeResult; } //��ͨ�ƴ��� String shapeResult = null; Double minNum=
	 * 1.0/8; Double midNum= 5.0/12; private String shape(ArrayList<Coordinates>
	 * listl,ArrayList<Coordinates> listr,int sort) { int index =
	 * listl.size();//���ص��ܸ߶� String turn = null; if(index>8){ Double
	 * midderNum=(double)
	 * (listr.get(listr.size()-1).getY()-listr.get(0).getY());
	 * Log.i("midderNum", ""+midderNum); if(midderNum/index<minNum){
	 * if(sort==1){//��ɫ Toast.makeText(MainActivity.this,
	 * "��ͨ��Ϊ����ɫ���Ҽ�ͷ",Toast.LENGTH_SHORT).show(); turn="��ɫ����"; } else
	 * if(sort==2){//��ɫ Toast.makeText(MainActivity.this,
	 * "��ͨ��Ϊ����ɫ���Ҽ�ͷ",Toast.LENGTH_SHORT).show(); turn="��ɫ����"; } } else
	 * if(midderNum/index<midNum){////��ͷ��� if(sort==1){//��ɫ
	 * Toast.makeText(MainActivity.this,
	 * "��ͨ��Ϊ����ɫ�����ͷ",Toast.LENGTH_SHORT).show(); turn="��ɫ����"; } else
	 * if(sort==2){//��ɫ Toast.makeText(MainActivity.this,
	 * "��ͨ��Ϊ����ɫ�����ͷ",Toast.LENGTH_SHORT).show(); turn="��ɫ����"; } } else{//��ͷ����
	 * Toast.makeText(MainActivity.this, "��ͨ��Ϊ����ͷ",Toast.LENGTH_SHORT).show(); }
	 * } else{ Toast.makeText(MainActivity.this,
	 * "��ͨ����ɫʶ��ʧ��",Toast.LENGTH_SHORT).show(); } return turn; }
	 */
	// // ����ͼƬ��ߵ���������
	ArrayList<Coordinates> rlistl = new ArrayList<Coordinates>();
	ArrayList<Coordinates> glistl = new ArrayList<Coordinates>();
	// // ����ͼƬ���ұߵ���������
	ArrayList<Coordinates> rlistr = new ArrayList<Coordinates>();
	ArrayList<Coordinates> glistr = new ArrayList<Coordinates>();

	private Bitmap convertToBlack(Bitmap bip) {// ���ش�������Ϊ��ɫ�����̻Ʋ���
		int width = bip.getWidth();
		int height = bip.getHeight();
		int[] pixels = new int[width * height];
		bip.getPixels(pixels, 0, width, 0, 0, width, height);
		int[] pl = new int[bip.getWidth() * bip.getHeight()];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;

				// Log.e("rrrrrrrrrrrrrrrr", r+"*****"+g);
				if (r > 200 && g < 150 && b < 150) {// ��ɫ
					pl[offset + x] = 0xffff0000;
				} else if (r < 200 && g > 150 && b < 100) {// ��ɫ
					pl[offset + x] = 0xff00ff00;
				} else {
					pl[offset + x] = 0xff000000;// ��ɫ
				}
			}
		}
		Bitmap result = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		result.setPixels(pl, 0, width, 0, 0, width, height);
		return result;

	}

	private String shapeIdentification(Bitmap bp, int flag) {
		rlistl.clear();
		glistl.clear();
		rlistr.clear();
		glistr.clear();
		int search_index = 0;
		int width = bp.getWidth();
		int height = bp.getHeight();
		int[] pixels = new int[width * height];
		bp = convertToBlack(bp);
		// showView.setImageBitmap(bp);
		bp.getPixels(pixels, 0, width, 0, 0, width, height);

		for (int y = 0; y < height; y++) {
			for (int x = width - 1; x > 0; x--) {// �õ����ұߵ�����
				int pixel = pixels[y * width + x];
				if (pixel != 0xff000000) {
					int r = (pixel >> 16) & 0xff;
					int g = (pixel >> 8) & 0xff;
					int b = pixel & 0xff;
					if (r > 200) {// ��ɫ
						rlistl.add(new Coordinates(x, y));
						break;
					} else if (g > 150) {// ��ɫ
						glistl.add(new Coordinates(x, y));
						break;
					}
				}
			}
		}
		int column = 0;
		boolean flag1 = false;
		for (int x = width - 1; x > 0; x--) {// �õ����ұߵ�����
			for (int y = 0; y < height; y++) {
				int pixel = pixels[y * width + x];
				if (pixel != 0xff000000) {
					int r = (pixel >> 16) & 0xff;
					int g = (pixel >> 8) & 0xff;
					int b = pixel & 0xff;
					if (r > 200) {// ��ɫ

						if (column >= 2) {
							rlistr.add(new Coordinates(x, y));
							search_index++;
						}
						flag1 = true;

					} else if (g > 150) {// ��ɫ

						if (column >= 2) {
							glistr.add(new Coordinates(x, y));
							search_index++;
						}
						flag1 = true;
					}
				}
			}

			if (flag1) {
				column++;
				flag1 = false;
			}

			if (column >= 2) {
				if (search_index > 2)// Ѱ�ҵ����ұ����ص�
					break;
			}

		}
		if (rlistl.size() > glistl.size()) {
			shapeResult = shape(rlistl, rlistr, 1);
			Log.e("��ɫ����", rlistl.size() + "");
		}

		else if (glistl.size() > rlistl.size()) {
			shapeResult = shape(glistl, glistr, 2);
			Log.e("��ɫ����", glistl.size() + "");
		}

		return shapeResult;
	}

	// ��ͨ�ƴ���
	String shapeResult = null;
	double minNum = 1.0 / 8;
	double midNum = 5.0 / 12;

	private String shape(ArrayList<Coordinates> listl,
			ArrayList<Coordinates> listr, int sort) {
		String turn = null;
		int index = listl.size();// ���ص��ܸ߶�
		if (index > 9) {
			double midderNum = listr.get(listr.size() - 1).getY()
					- listr.get(0).getY();

			double tt = midderNum / index;
			if (tt < minNum) {// ��ͷ�ұ�

				if (sort == 1) {// ��ɫ
					// Toast.makeText(MainActivity.this,
					// "��ͨ��Ϊ����ɫ���Ҽ�ͷ"+tt,Toast.LENGTH_SHORT).show();
					turn = "��ɫ����";
				} else if (sort == 2) {// ��ɫ
					// Toast.makeText(MainActivity.this,
					// "��ͨ��Ϊ����ɫ���Ҽ�ͷ"+tt,Toast.LENGTH_SHORT).show();
					turn = "��ɫ����";
				}
			} else if (tt < midNum) {// //��ͷ���
				if (sort == 1) {// ��ɫ
					// Toast.makeText(MainActivity.this,
					// "��ͨ��Ϊ����ɫ�����ͷ"+tt,Toast.LENGTH_SHORT).show();
					turn = "��ɫ����";
				} else if (sort == 2) {// ��ɫ
					// Toast.makeText(MainActivity.this,
					// "��ͨ��Ϊ����ɫ�����ͷ"+tt,Toast.LENGTH_SHORT).show();
					turn = "��ɫ����";
				}
			} else {// ��ͷ����
					// Toast.makeText(MainActivity.this,
					// "��ͨ��Ϊ����ͷ"+tt,Toast.LENGTH_SHORT).show();
				turn = "��ͷ";
			}

			Log.e("sortsort", sort + "");
			Log.e("tttttttt", tt + "");
			Log.e("ssssssssssss", String.valueOf(midderNum));

			Log.e("KKKKKKKKKKKKKKK", String.valueOf(index));
		} else {
			// Toast.makeText(MainActivity.this,
			// "��ͨ����ɫʶ��ʧ��",Toast.LENGTH_SHORT).show();
			turn = "ʶ��ʧ��";
		}

		return turn;
	}

	// ͼ��ʶ��

	ArrayList<Coordinates> list = new ArrayList<Coordinates>();
	ArrayList<Coordinates> list_above = new ArrayList<Coordinates>();
	ArrayList<Coordinates> list_among = new ArrayList<Coordinates>();
	ArrayList<Coordinates> list_below = new ArrayList<Coordinates>();

	private Bitmap shape_recognition1(Bitmap bitmap_new) {
		list.clear(); // ����б�
		list_above.clear();
		list_among.clear();
		list_below.clear();
		int width = bitmap_new.getWidth(); // �õ�
		int height = bitmap_new.getHeight();

		System.out.println("ͼƬ�ܸ߶�" + height);
		System.out.println("ͼƬ�ܿ��" + width);
		int[] pixels = new int[width * height];
		bitmap_new.getPixels(pixels, 0, width, 0, 0, width, height);

		int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = pixels[y * width + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				if ((r > 200) && (g < 100) && (b < 100)) { // ��ɫ
					list.add(new Coordinates(x, y));
					if (x1 == 0)
						x1 = x;
					else if (x < x1)
						x1 = x;
					if (x2 == 0)
						x2 = x;
					else if (x > x2)
						x2 = x;

					if (y1 == 0)
						y1 = y;
					else if (y < y1)
						y1 = y;
					if (y2 == 0)
						y2 = y;
					else if (y > y2)
						y2 = y;

				}
				if ((r < 100) && (g > 150) && (b < 100)) { // ��ɫ
					list.add(new Coordinates(x, y));
					if (x1 == 0)
						x1 = x;
					else if (x < x1)
						x1 = x;
					if (x2 == 0)
						x2 = x;
					else if (x > x2)
						x2 = x;

					if (y1 == 0)
						y1 = y;
					else if (y < y1)
						y1 = y;
					if (y2 == 0)
						y2 = y;
					else if (y > y2)
						y2 = y;
				}
				if ((r > 100) && (g > 100) && (b < 200)) {// ��ɫ
					list.add(new Coordinates(x, y));
					if (x1 == 0)
						x1 = x;
					else if (x < x1)
						x1 = x;
					if (x2 == 0)
						x2 = x;
					else if (x > x2)
						x2 = x;

					if (y1 == 0)
						y1 = y;
					else if (y < y1)
						y1 = y;
					if (y2 == 0)
						y2 = y;
					else if (y > y2)
						y2 = y;
				}
			}
		}

		Log.e("��״�ܸ߶�wwwwww", (y2 - y1) + "");

		Log.e("��״�ܿ��wwwwwwwwww", (x2 - x1) + "");

		int hhh = y2 - y1;
		int www = x2 - x1;

		int[] pixels11 = new int[www * hhh];
		bitmap_new.getPixels(pixels11, 0, www, x1, y1, www, hhh);

		Bitmap result = Bitmap.createBitmap(www, hhh, Bitmap.Config.ARGB_8888);
		result.setPixels(pixels11, 0, www, 0, 0, www, hhh);

		return result;
	}

	private int red_max = 0, green_max = 0, blues_max = 0;
	private int red_min = 0, green_min = 0, blues_min = 0;
	Bitmap[] bips = new Bitmap[6];

	private Bitmap convertToBlack(Bitmap bip, int index) {// ���ش�������Ϊ��ɫ������������
		bip = cutBorder(bip);
		//bip = convertToBlackAll(bip);
		return bip;
	}

	ArrayList<SixShape> listShape = new ArrayList<SixShape>();

	private long ScanShape(Bitmap bip) {
		int width = bip.getWidth();
		int height = bip.getHeight();
		int[] pixels = new int[width * height];
		bip.getPixels(pixels, 0, width, 0, 0, width, height);
		long listR = 0, listG = 0, listY = 0;

		long maxShape = 0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = pixels[y * width + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				if ((r > 200) && (g < 100) && (b < 100)) { // ��ɫ
					listR++;
				}
				if ((r < 100) && (g > 150) && (b < 100)) { // ��ɫ
					listG++;
				}
				if ((r > 200) && (g > 200) && (b < 100)) {// ��ɫ
					listY++;
				}
			}
		}

		if (listR > 300) {
			SixShape r = new SixShape(listR, "��", "��");

			listShape.add(r);

			maxShape += listR;

		}

		if (listG > 300) {
			SixShape b = new SixShape(listG, "��", "��");
			listShape.add(b);
			maxShape += listG;

		}

		if (listY > 200) {
			SixShape y = new SixShape(listY, "��", "��");
			listShape.add(y);
			maxShape += listY;

		}

		return maxShape;

	}

	private Bitmap[] getBitmaps(Bitmap bip) {
		Bitmap[] kk = new Bitmap[6];

		for (int i = 1; i <= 6; i++) {
			kk[i] = shape_mysix(bip, i);
			ScanShape(kk[i]);

		}

		return kk;

	}

	private String getShapeInfo(Bitmap bip) {
		Bitmap kk = null;

		listShape.clear();

		long myMax = 1, tempMax = 1;
		for (int i = 1; i <= 6; i++) {
			kk = shape_mysix(bip, i);

			Log.e("sssssssssssssssssssssssssss", i + "" + myMax);
			tempMax = ScanShape(kk);

			if (tempMax > myMax) {
				myMax = tempMax;
			}

		}

		for (SixShape ss : listShape) {

		double i = ss.getNum() * 1.0 / myMax;
		Log.e("rrrrreeew", ""+ss.getNum());
			Log.e("eeeeeeeeeeeeeeeeeeeeeeeeee", ""+i);
			if (i < 0.58) {
				ss.setShapeName("������");
			} else if (i < 0.99) {
				ss.setShapeName("Բ��");
			} else {
				ss.setShapeName("����");
			}

		}

		int iii = 0;

		ArrayList<MyResult> kkkk = new ArrayList<MyResult>();

		for (SixShape ss : listShape) {

			MyResult mys = new MyResult(ss.getColorName() + ss.getShapeName(),
					1);

			boolean flag = false;

			for (MyResult myResult : kkkk) {

				if (myResult.getThename().equals(
						ss.getColorName() + ss.getShapeName())) {
					flag = true;
					myResult.setMynum(myResult.getMynum() + 1);
				}

			}

			if (!flag) {
				kkkk.add(mys);
			}

			Log.e("�����򸾸�����������������", ss.toString());
		}

		String mystring = "";

		for (MyResult myddd : kkkk) {

			mystring += myddd.getThename() + myddd.getMynum();
		}

		Log.e("��������������", mystring);
		return mystring;
		// fruit_show.setText(mystring);
	}

	private Bitmap shape_mysix(Bitmap bip, int index) {
		// TODO Auto-generated method stub

		int width = bip.getWidth();
		int height = bip.getHeight();

		int x1 = 0, y1 = 0;
		int[] pixels = new int[width / 3 * height / 2];

		if (index == 1) {
			x1 = 0;
			y1 = 0;
		}
		if (index == 2) {
			x1 = width / 3;
			y1 = 0;
		}
		if (index == 3) {
			x1 = width * 2 / 3;
			y1 = 0;
		}
		if (index == 4) {
			x1 = 0;
			y1 = height / 2;
		}
		if (index == 5) {
			x1 = width / 3;
			y1 = height / 2;
		}
		if (index == 6) {
			x1 = width * 2 / 3;
			y1 = height / 2;
		}

		bip.getPixels(pixels, 0, width / 3, x1, y1, width / 3, height / 2);

		Bitmap result = Bitmap.createBitmap(width / 3, height / 2,
				Bitmap.Config.ARGB_8888);
		result.setPixels(pixels, 0, width / 3, 0, 0, width / 3, height / 2);
		return result;
	}

	private Bitmap cutBorder(Bitmap bip) {// ���ش�������Ϊ��ɫ������������
		int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		int whiteCount = 0;
		boolean flag = false;

		int width = bip.getWidth();
		int height = bip.getHeight();

		x2 = width;
		y2 = height;

		int[] pixels = new int[width * height];
		bip.getPixels(pixels, 0, width, 0, 0, width, height);
		int[] pl = new int[bip.getWidth() * bip.getHeight()];
		for (int y = 0; y < height; y++) {
			whiteCount = 0;
			int offset = y * width;
			for (int x = 0; x < width; x++) {

				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;

				if (r > 150 && g > 150 && b > 150) {
					whiteCount++;
				}

			}
			if (whiteCount > 200) {
				y1 = y;
				break;
			}
		}

		for (int y = height - 1; y > height - 50; y--) {
			whiteCount = 0;
			int offset = y * width;
			for (int x = 0; x < width; x++) {

				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;

				if (r > 150 && g > 150 && b > 150) {
					whiteCount++;
				}

			}
			if (whiteCount > 200) {
				y2 = y;
				break;
			}
		}

		for (int x = 0; x < width / 2; x++) {
			whiteCount = 0;

			for (int y = 0; y < height; y++) {
				int offset = y * width;
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;

				if (r > 150 && g > 150 && b > 150) {
					whiteCount++;
				}

			}
			if (whiteCount > 50) {
				x1 = x;
				break;
			}
		}

		for (int x = width - 1; x > width / 2; x--) {
			whiteCount = 0;

			for (int y = 0; y < height; y++) {
				int offset = y * width;
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;

				if (r > 150 && g > 150 && b > 150) {
					whiteCount++;
				}

			}
			if (whiteCount > 50) {
				x2 = x;
				break;
			}
		}
		if (y2 == height)
			y2 = y2 - 20;
		x2 = x2 - 10;

		int width1 = x2 - x1;
		int height1 = y2 - y1;

		Log.e("fffffffffffffffffffffffffff", x1 + "ffffff" + x2 + "yyyy" + y1
				+ "yyyy" + y2);

		Log.e("fffffffffffffffffffffffffff", width1 + "ffffff" + height1);

		int[] p2 = new int[width1 * height1];

		bip.getPixels(p2, 0, width1, x1, y1, width1, height1);

		Bitmap result = Bitmap.createBitmap(width1, height1,
				Bitmap.Config.ARGB_8888);
		result.setPixels(p2, 0, width1, 0, 0, width1, height1);

		// result=shape_recognition1(result);

		return result;
	}

	private Bitmap convertToBlackAll11(Bitmap bip) {// ���ش�������Ϊ��ɫ������������

		int width = bip.getWidth();
		int height = bip.getHeight();
		int[] pixels = new int[width * height];
		bip.getPixels(pixels, 0, width, 0, 0, width, height);
		int[] pl = new int[bip.getWidth() * bip.getHeight()];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;

				if (r >200 && g < 10 && b < 10) // ��ɫ
					pl[offset + x] = 0xFFFF0000;
				else if (r < 10 && g > 150 && b < 10) // ��ɫ
					pl[offset + x] = 0xFF00FF00;
				else if (r > 220 && g > 220 && b < 10) // ��ɫ
					pl[offset + x] = 0xFFFFFF00;

				else
					pl[offset + x] = 0xff000000;// ��ɫ
			}
		}

		Bitmap result = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		result.setPixels(pl, 0, width, 0, 0, width, height);

		// result=shape_recognition1(result);

		return result;
	}

	private Bitmap convertToBlackred(Bitmap bip) {// ���ش�������Ϊ��ɫ������������

		int width = bip.getWidth();
		int height = bip.getHeight();
		int[] pixels = new int[width * height];
		bip.getPixels(pixels, 0, width, 0, 0, width, height);
		int[] pl = new int[bip.getWidth() * bip.getHeight()];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {

				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				if (r > 100 && g < 100 && b < 100){ // ��ɫ
					pl[offset + x] = 0xFFFF0000;
				}
				/*else if (r < 100 && g > 100 && b < 100) // ��ɫ
					pl[offset + x] = 0xFF00FF00;
				else if (r > 150 && g > 150 && b < 100) // ��ɫ
					pl[offset + x] = 0xFFFFFF00;*/
				else
					pl[offset + x] = 0xff000000;// ��ɫ
			}
		}

		Bitmap result = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		result.setPixels(pl, 0, width, 0, 0, width, height);

		result = shape_recognition1(result);

		return result;
	}
	private Bitmap convertToBlackgreen(Bitmap bip) {// ���ش�������Ϊ��ɫ������������

		int width = bip.getWidth();
		int height = bip.getHeight();
		int[] pixels = new int[width * height];
		bip.getPixels(pixels, 0, width, 0, 0, width, height);
		int[] pl = new int[bip.getWidth() * bip.getHeight()];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {

				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				if (r < 100 && g > 100 && b < 100) // ��ɫ
					pl[offset + x] = 0xFF00FF00;
				else
					pl[offset + x] = 0xff000000;// ��ɫ
			}
		}

		Bitmap result = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		result.setPixels(pl, 0, width, 0, 0, width, height);

		result = shape_recognition1(result);

		return result;
	}
	private Bitmap convertToBlackblue(Bitmap bip) {// ���ش�������Ϊ��ɫ������������

		int width = bip.getWidth();
		int height = bip.getHeight();
		int[] pixels = new int[width * height];
		bip.getPixels(pixels, 0, width, 0, 0, width, height);
		int[] pl = new int[bip.getWidth() * bip.getHeight()];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {

				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
			 if (r > 150 && g > 150 && b < 100) // ��ɫ
					pl[offset + x] = 0xFFFFFF00;
				else
					pl[offset + x] = 0xff000000;// ��ɫ
			}
		}

		Bitmap result = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		result.setPixels(pl, 0, width, 0, 0, width, height);

		result = shape_recognition1(result);

		return result;
	}
	private Bitmap convertToSingle(Bitmap bip, Zuobiao zb) {// ���ش�������Ϊ��ɫ������������

		int width = bip.getWidth();
		int height = bip.getHeight();
		int[] pixels = new int[width * height];
		bip.getPixels(pixels, 0, width, 0, 0, width, height);
		int[] pl = new int[bip.getWidth() * bip.getHeight()];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {

				if (x >= zb.getX1() - 2 && x <= zb.getX2() + 2) {
					if (y >= zb.getY1() - 2 && y <= zb.getY2() + 2) {

						pl[offset + x] = 0xff000000;// ��ɫ
						continue;
					}
				}

				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				if (r > 200 && g < 100 && b < 100) // ��ɫ
					pl[offset + x] = 0xFFFF0000;
				else if (r < red_max && g > green_min && b < blues_max) // ��ɫ
					pl[offset + x] = 0xFF00FF00;
				else if (r > red_max && g > green_max && b < blues_min) // ��ɫ
					pl[offset + x] = 0xFFFFFF00;
				else
					pl[offset + x] = 0xff000000;// ��ɫ
			}
		}
		Bitmap result = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		result.setPixels(pl, 0, width, 0, 0, width, height);

		return result;
	}

	ArrayList<Coordinates> list11 = new ArrayList<Coordinates>();

	private Bitmap shape_six(Bitmap bip, int index) {

		Zuobiao zuobiao = getX(bip, index);

		bip = convertToSingle(bip, zuobiao);
		//
		// zuobiao=getX(bip,index);
		//
		// bip=convertToSingle(bip,zuobiao);

		zuobiao = getX(bip, index);

		Log.e("maxxxxxxxxxxxxxxxxx", zuobiao.getX1() + "xxx" + zuobiao.getX2()
				+ "");

		Log.e("ma���������������������������",
				zuobiao.getY1() + "xxyyyyyx" + zuobiao.getY2() + "");

		// return bip;

		return getShape(bip, zuobiao);

	}

	private Bitmap getShape(Bitmap bip, Zuobiao zuobiao) {

		int hhh = zuobiao.getY2() - zuobiao.getY1();
		int www = zuobiao.getX2() - zuobiao.getX1();

		int[] pixels11 = new int[www * hhh];
		bip.getPixels(pixels11, 0, www, zuobiao.getX1(), zuobiao.getY1(), www,
				hhh);

		Bitmap result = Bitmap.createBitmap(www, hhh, Bitmap.Config.ARGB_8888);
		result.setPixels(pixels11, 0, www, 0, 0, www, hhh);

		return result;
	}

	private Zuobiao getX(Bitmap bip, int index) {
		int width = 0;
		int height = 0;

		int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		list11.clear();

		width = bip.getWidth();
		height = bip.getHeight();

		int[] pixels = new int[width * height];
		bip.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			int ink = 0, outk = 0;
			boolean flag = false;
			for (int x = 0; x < width; x++) {
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;

				if (index == 1 && r > red_min && g < green_max && b < blues_max) // ��ɫ
				{

					flag = true;
					if (x1 == 0)
						x1 = x;
					else if (x < x1)
						x1 = x;
					if (x2 == 0)
						x2 = x;
					else if (x > x2)
						x2 = x;

					if (y1 == 0)
						y1 = y;
					else if (y < y1)
						y1 = y;
					if (y2 == 0)
						y2 = y;
					else if (y > y2)
						y2 = y;

				} else if (index == 2 && r < red_max && g > green_min
						&& b < blues_max) // ��ɫ
				{

					flag = true;
					if (x1 == 0)
						x1 = x;
					else if (x < x1)
						x1 = x;
					if (x2 == 0)
						x2 = x;
					else if (x > x2)
						x2 = x;

					if (y1 == 0)
						y1 = y;
					else if (y < y1)
						y1 = y;
					if (y2 == 0)
						y2 = y;
					else if (y > y2)
						y2 = y;

				} else if (index == 3 && r > red_max && g > green_max
						&& b < blues_min) // ��ɫ
				{
					flag = true;
					if (x1 == 0)
						x1 = x;
					else if (x < x1)
						x1 = x;
					if (x2 == 0)
						x2 = x;
					else if (x > x2)
						x2 = x;

					if (y1 == 0)
						y1 = y;
					else if (y < y1)
						y1 = y;
					if (y2 == 0)
						y2 = y;
					else if (y > y2)
						y2 = y;

				}

				else {

					if (flag) {
						outk++;

						if (outk >= 2) {

							break;
						}
					}
				}

			}

			if (outk >= 2 && y < height - 1) {

				boolean myflag = true;
				for (int x = 0; x < x2; x++) {
					int pixel = pixels[(y + 1) * width + x];
					int r = (pixel >> 16) & 0xff;
					int g = (pixel >> 8) & 0xff;
					int b = pixel & 0xff;

					if (index == 1 && r > red_min && g < green_max
							&& b < blues_max) // ��ɫ
					{
						myflag = false;
						break;
					} else if (index == 2 && r < red_max && g > green_min
							&& b < blues_max) // ��ɫ
					{
						myflag = false;
						break;
					} else if (index == 3 && r > red_max && g > green_max
							&& b < blues_min) // ��ɫ
					{
						myflag = false;
						break;
					}

				}

				if (myflag) {
					Log.e("maxxxxxxxxxxxxxxxxx", width + "xxx" + x2 + "");
					height = y;
					break;

				}
			}

		}
		return new Zuobiao(x1, x2, y1, y2);
	}

	// ͼƬ����
	public void saveMyBitmap(String bitName, Bitmap mBitmap) throws Exception {
		File f = new File("/sdcard/" + bitName + ".png");
		f.createNewFile();
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		Log.e("����ɹ�", "����ɹ�");
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// �����ļ�
	public static void put(String s, String name) {
		try {
			FileOutputStream outStream = new FileOutputStream("/sdcard/" + name
					+ ".txt");
			OutputStreamWriter writer = new OutputStreamWriter(outStream);
			writer.write(s);
			writer.flush();
			writer.close();// �ǵùر�
			outStream.close();
		} catch (Exception e) {
			Log.e("m", "file write error");
		}
	}
	///����ʶ��
	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // �ж�sd���Ƿ����
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// ��ȡ���Ŀ¼
		}
		return sdDir.toString();
	}
	public String doOcr(Bitmap bitmap, String language) {
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.init(getSDPath(), language);
		System.gc();
		// ����Ӵ��У�tess-twoҪ��BMP����Ϊ������
		bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		baseApi.setImage(bitmap);
		String text =baseApi.getUTF8Text();
		baseApi.clear();
		baseApi.end();
		return text;
	}

	//ȥ���ƶ���߿�
	private Bitmap cutBorder2(Bitmap bip) {// ���ش�������Ϊ��ɫ������������
		 int x1=0,x2=0,y1=0,y2=0;
		 int whiteCount=0;
		 boolean flag=false;

		 
		int width = bip.getWidth();
		int height = bip.getHeight();
		
		x2=width;
		y2=height;
		
		int[] pixels = new int[width * height];
		bip.getPixels(pixels, 0, width, 0, 0, width, height);
		int[] pl = new int[bip.getWidth() * bip.getHeight()];
		for (int y =50; y < height; y++) {
			whiteCount=0;
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff; 
					 
				  if ( r >150 && g >150  && b > 150)    
					{
					  whiteCount++;						  
					}
				
			}
			if(whiteCount>100)
			{
				y1=y;
				break;
			}					
		}
		
		for (int y = height-1; y > height-200; y--) {
			whiteCount=0;
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff; 
					 
				  if ( r >150 && g >150  && b > 150)    
					{
					  whiteCount++;						  
					}
				
			}
			if(whiteCount>50)
			{
				y2=y-45;
				break;
			}					
		}
		
		for (int x = 0; x < width/2; x++) {
			whiteCount=0;
			
			for (int y = 0; y < height; y++) {
				int offset = y * width;
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff; 
					 
				  if ( r >150 && g >150  && b > 150)    
					{
					  whiteCount++;						  
					}
				
			}
			if(whiteCount>50)
			{
				x1=x+45;
				break;
			}					
		}
		
		for (int x = width-1; x > width/2; x--) {
			whiteCount=0;
			
			for (int y = 0; y < height; y++) {
				int offset = y * width;
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff; 
					 
				  if ( r >150 && g >150  && b > 150)    
					{
					  whiteCount++;						  
					}
				
			}
			if(whiteCount>50)
			{
				x2=x-15;
				break;
			}					
		}
		y1=y1+50;
	//	if(y2==height)
	//		y2=y2-20;
		x2=x2-10;
		
		int width1=x2-x1;
		int height1=y2-y1;
		
		Log.e("fffffffffffffffffffffffffff",x1+"ffffff"+x2+"yyyy"+y1+"yyyy"+y2);
		
		Log.e("fffffffffffffffffffffffffff",width1+"ffffff"+height1);
		
		int[] p2=new int[width1*height1];
		
		bip.getPixels(p2, 0, width1, x1, y1, width1, height1);

		
		Bitmap result = Bitmap.createBitmap(width1, height1,
				Bitmap.Config.ARGB_8888);
		result.setPixels(p2, 0, width1, 0, 0, width1, height1);
		
		//result=shape_recognition1(result);
		
		return result;
	}
	//��ȡ�ļ�
    public String read(String fileName) throws IOException {  
    	File file = new File(Environment.getExternalStorageDirectory(),fileName);
    	if(file.exists()){
    		FileInputStream fileInputStream=new FileInputStream(file); 
            //��ÿ�ζ�ȡ������д�뵽�ڴ��У�Ȼ����ڴ��л�ȡ  
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();  
            byte[] buffer = new byte[1024];  
            int len =0;  
            //ֻҪû���꣬���ϵĶ�ȡ  
            while((len=fileInputStream.read(buffer))!=-1){  
                outputStream.write(buffer, 0, len);  
            }  
            //�õ��ڴ���д�����������  
            byte[] data = outputStream.toByteArray();  
            fileInputStream.close();
            return new String(data);  
    	}
    	else
    		return "";
    	
    }  
}
