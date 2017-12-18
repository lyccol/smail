package com.car_socket_connect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class socket_connect {
//	private int port = 60000;
	private int port = 9999;
	private DataInputStream bInputStream;
	private DataOutputStream bOutputStream;
	private Socket socket;
	private byte[] rbyte = new byte[40];
	private Handler reHandler;
	public short TYPE=0xAA;
	public short MAJOR = 0x00;
	public short FIRST = 0x00;
	public short SECOND = 0x00;
	public short THRID = 0x00;
	public short CHECKSUM=0x00;
	
public void onDestory(){
		try {
			if(socket!=null&&!socket.isClosed()){
				socket.close();
				bInputStream.close();
				bOutputStream.close();
			}	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void connect(Handler handler, String IP) {
		try {
			this.reHandler=handler;
			socket = new Socket(IP, port);
			bInputStream = new DataInputStream(socket.getInputStream());
			bOutputStream = new DataOutputStream(socket.getOutputStream());
			reThread.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Thread reThread = new Thread(new Runnable() {
		@Override
		public void run() {
			// TODO Auto1-generated method stub
			while (socket != null && !socket.isClosed()) {
					try {
						bInputStream.read(rbyte);
						Message msg = new Message();
						msg.what = 1;
						msg.obj = rbyte;
						reHandler.sendMessage(msg);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		}
	});

	private void send()
	{
		CHECKSUM=(short) ((MAJOR+FIRST+SECOND+THRID)%256);
		// ���������ֽ�����
		
		final byte[] sbyte = { 0x55, (byte) TYPE, (byte) MAJOR, (byte) FIRST, (byte) SECOND, (byte) THRID ,(byte) CHECKSUM,(byte) 0xBB};
		new Thread(new Runnable() {		
			@Override
			public void run() {
				// TODO Auto-generated method stub	
				try {
					if(socket!=null&&!socket.isClosed()){
						bOutputStream.write(sbyte, 0, sbyte.length);
						bOutputStream.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		}).start();	
		System.out.println("ָ���Ѿ�����");
	}	
	
	public void send_voice(final byte [] textbyte) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {

				try {
					// ���������ֽ�����
					if (socket != null && !socket.isClosed()) {
						bOutputStream.write(textbyte, 0, textbyte.length);
						bOutputStream.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
					Log.i("err2", "***********");
				}				
			}
		}).start();
	}
	
		
		// ǰ��
		public void go(int sp_n, int en_n) {
			MAJOR = 0x02;
			FIRST = (byte) (sp_n & 0xFF);
			SECOND = (byte) (en_n & 0xff);
			THRID = (byte) (en_n >> 8);
			send();
		}
		// ����
		public void back(int sp_n, int en_n) {
			MAJOR = 0x03;
			FIRST = (byte) (sp_n & 0xFF);
			SECOND = (byte) (en_n & 0xff);
			THRID = (byte) (en_n >> 8);
			send();
		}
		// ��ת
		public void left(int sp_n) {
			MAJOR = 0x04;
			FIRST = (byte) (sp_n & 0xFF);
			SECOND = 0x00;
			THRID = 0x00;
			send();
		}
	
		// ��ת
		public void right(int sp_n) {
			MAJOR = 0x05;
			FIRST = (byte) (sp_n & 0xFF);
			SECOND = 0x00;
			THRID = 0x00;
			send();
		}
	
		// ͣ��
		public void stop() {
			MAJOR = 0x01;
			FIRST = 0x00;
			SECOND = 0x00;
			THRID = 0x00;
			send();
		}
	
		// ѭ��
		public void line(int sp_n) {
			MAJOR = 0x06;
			FIRST = (byte) (sp_n & 0xFF);
			SECOND = 0x00;
			THRID = 0x00;
			send();
		}
		//��������
		public void clear(){
			MAJOR = 0x07;
			FIRST = 0x00;
			SECOND = 0x00;
			THRID = 0x00;
			send();
		}
		//����
		public void cs1(){
			MAJOR = 0x13;
			FIRST = 0x00;
			SECOND = 0x00;
			THRID = 0x00;
			send();
		}
	
		//����
				public void cs2(){
					MAJOR = 0x14;
					FIRST = 0x00;
					SECOND = 0x00;
					THRID = 0x00;
					send();
				}
				public void cs3(){
					MAJOR = 0x15;
					FIRST = 0x00;
					SECOND = 0x00;
					THRID = 0x00;
					send();
				}
				public void cs4(){
					MAJOR = 0x16;
					FIRST = 0x00;
					SECOND = 0x00;
					THRID = 0x00;
					send();
				}
				public void cs5(){
					MAJOR = 0x17;
					FIRST = 0x00;
					SECOND = 0x00;
					THRID = 0x00;
					send();
				}
				public void cs6(){
					MAJOR = 0x18;
					FIRST = 0x00;
					SECOND = 0x00;
					THRID = 0x00;
					send();
				}
				public void cs7(){
					MAJOR = 0x19;
					FIRST = 0x00;
					SECOND = 0x00;
					THRID = 0x00;
					send();
				}
				
				
			
	
		public void vice(int i){//���ӳ�״̬ת��
			if(i==1){//�ӳ�״̬
				TYPE=0x02;
				MAJOR = 0x80;
				FIRST = 0x01;
				SECOND = 0x00;
				THRID = 0x00;
				send();
				yanchi(500);
				
				TYPE=(byte) 0xAA;
				MAJOR = 0x80;
				FIRST = 0x01;
				SECOND = 0x00;
				THRID = 0x00;
				send();
				TYPE= 0x02;
			}
			else if(i==2){//����״̬
				TYPE=0x02;
				MAJOR = 0x80;
				FIRST = 0x00;
				SECOND = 0x00;
				THRID = 0x00;
				send();
				yanchi(500);
				
				TYPE=(byte) 0xAA;
				MAJOR = 0x80;
				FIRST = 0x00;
				SECOND = 0x00;
				THRID = 0x00;
				send();
				TYPE= 0xAA;
			}
			
		}
	// ����
	public void infrared(byte one, byte two, byte thrid, byte four, byte five,
			byte six) {
		MAJOR = 0x10;
		FIRST = one;
		SECOND = two;
		THRID = thrid;
		send();
		yanchi(1000);
		MAJOR = 0x11;
		FIRST = four;
		SECOND = five;
		THRID = six;
		send();
		yanchi(1000);
		MAJOR = 0x12;
		FIRST = 0x00;
		SECOND = 0x00;
		THRID = 0x00;
		send();
		yanchi(2000);
	}
	
	// ˫ɫLED��
	public void lamp(byte command) {
		MAJOR = 0x40;
		FIRST = command;
		SECOND = 0x00;
		THRID = 0x00;
		send();
	}
	
	// ָʾ��
	public void light(int left, int right) {
		if (left == 1 && right == 1) {
			MAJOR = 0x20;
			FIRST = 0x01;
			SECOND = 0x01;
			THRID = 0x00;
			send();
		} else if (left == 1 && right == 0) {
			MAJOR = 0x20;
			FIRST = 0x01;
			SECOND = 0x00;
			THRID = 0x00;
			send();
		} else if (left == 0 && right == 1) {
			MAJOR = 0x20;
			FIRST = 0x00;
			SECOND = 0x01;
			THRID = 0x00;
			send();
		} else if (left == 0 && right == 0) {
			MAJOR = 0x20;
			FIRST = 0x00;
			SECOND = 0x00;
			THRID = 0x00;
			send();
		}
	}
	
	// ������
	public void buzzer(int i) {
		if (i == 1)
			FIRST = 0x01;
		else if (i == 0)
			FIRST = 0x00;
		MAJOR = 0x30;
		SECOND = 0x00;
		THRID = 0x00;
		send();
	}
	
	public void picture(int i) {// ͼƬ�Ϸ����·�
		if (i == 1)
			MAJOR = 0x50;
		else
			MAJOR = 0x51;
		FIRST = 0x00;
		SECOND = 0x00;
		THRID = 0x00;
		send();
	}
	
	public void gear(int i) {// ���յ�λ��
		if (i == 1)
			MAJOR = 0x61;
		else if (i == 2)
			MAJOR = 0x62;
		else if (i == 3)
			MAJOR = 0x63;
		FIRST = 0x00;
		SECOND = 0x00;
		THRID = 0x00;
		send();
	}
	
	public void fan() {// ����
		MAJOR = 0x70;
		FIRST = 0x00;
		SECOND = 0x00;
		THRID = 0x00;
		send();
	}
	//������ʾ
	public void infrared_stereo(short [] data){
		MAJOR = 0x10;
		FIRST =  0xff;
		SECOND = data[0];
		THRID = data[1];
		send();
		yanchi(500);
		MAJOR = 0x11;
		FIRST = data[2];
		SECOND = data[3];
		THRID = data[4];
		send();
		yanchi(500);
		MAJOR = 0x12;
		FIRST = 0x00;
		SECOND = 0x00;
		THRID = 0x00;
		send();
		yanchi(500);
	}
	public void gate(int i) {// բ��
		byte type=(byte) TYPE;
		if (i == 1) {
			TYPE = 0x03;
			MAJOR = 0x01;
			FIRST = 0x01;
			SECOND = 0x00;
			THRID = 0x00;
			send();
		} else if (i == 2) {
			TYPE = 0x03;
			MAJOR = 0x01;
			FIRST = 0x02;
			SECOND = 0x00;
			THRID = 0x00;
			send();
		}
		TYPE = type;
	}
	//LCD��ʾ��־������ʱģʽ
	public void digital_close(){//����ܹر�
		byte type=(byte) TYPE;
		TYPE = 0x04;
		MAJOR = 0x03;
		FIRST = 0x00;
		SECOND = 0x00;
		THRID = 0x00;
		send();
		TYPE = type;
	}
	public void digital_open(){//����ܴ�
		byte type=(byte) TYPE;
		TYPE = 0x04;
		MAJOR = 0x03;
		FIRST = 0x01;
		SECOND = 0x00;
		THRID = 0x00;
		send();
		TYPE = type;
	}
	public void digital_clear(){//���������
		byte type=(byte) TYPE;
		TYPE = 0x04;
		MAJOR = 0x03;
		FIRST = 0x02;
		SECOND = 0x00;
		THRID = 0x00;
		send();
		TYPE = type;
	}
	public void digital_dic(int dis){//LCD��ʾ��־��ڶ�����ʾ����
		byte type=(byte) TYPE;
		TYPE = 0x04;
		MAJOR = 0x04;
		FIRST = 0x00;
		SECOND = (short) (dis/100);
		THRID = (short) (dis%100);
		send();
		TYPE = type;
	}
	public void digital(int i, int one, int two, int three) {// �����
		byte type=(byte) TYPE;
		TYPE = 0x04;
		if (i == 1) {//����д���һ�������
			MAJOR = 0x01;
			FIRST = (byte) one;
			SECOND = (byte) two;
			THRID = (byte) three;
		} else if (i == 2) {//����д��ڶ��������
			MAJOR = 0x02;
			FIRST = (byte) one;
			SECOND = (byte) two;
			THRID = (byte) three;
		}
		send();
		TYPE = type;
	}
	public void arm(int MAIN, int KIND, int COMMAD, int DEPUTY){
		MAJOR = (short) MAIN;
		FIRST = (byte)KIND ;
		SECOND = (byte) COMMAD;
		THRID = (byte) DEPUTY;
		send();
	}
	
	public void TFT_LCD(int MAIN, int KIND, int COMMAD, int DEPUTY)
	{
		byte type=(byte) TYPE;
		TYPE = (short)0x0B;
		MAJOR = (short) MAIN;
		FIRST = (byte)KIND ;
		SECOND = (byte) COMMAD;
		THRID = (byte) DEPUTY;
		send();	
		TYPE = type;
	}
	
	public void magnetic_suspension(int MAIN, int KIND, int COMMAD, int DEPUTY) 
	{
		byte type=(byte) TYPE;
		TYPE = (short)0x0A;
		MAJOR = (short) MAIN;
		FIRST = (byte)KIND ;
		SECOND = (byte) COMMAD;
		THRID = (byte) DEPUTY;
		send();	
		TYPE = type;
	}
	
	// ��˯
	public void yanchi(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.i("err", "**********");
			e.printStackTrace();
		}
	}
}
