package com.example.qcsxcxt;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends Activity {
	private ImageView qr,traffic,shape,carnum,dis,other;
	private TextView qrtext,traffictext,shapetext,carnumtext,distext,othertext;
	private Bitmap ewm,jtd,cp,tx,jl,qt;
	String e,t,c,j,j2,q;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		  requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.result);
			init();
			set();
	}
	
	private void init(){
		qr=(ImageView) findViewById(R.id.qr);
		traffic=(ImageView) findViewById(R.id.traffic);
		shape=(ImageView) findViewById(R.id.shape);
		carnum=(ImageView) findViewById(R.id.carnum);
		other=(ImageView) findViewById(R.id.other);
		dis=(ImageView) findViewById(R.id.dis);
		
		qrtext=(TextView) findViewById(R.id.qrtext);
		traffictext=(TextView) findViewById(R.id.traffictext);
		shapetext=(TextView) findViewById(R.id.shapetext);
		carnumtext=(TextView) findViewById(R.id.carnumtext);
		othertext=(TextView) findViewById(R.id.othertext);
		distext=(TextView) findViewById(R.id.distext);
	}
	//����
	private void set(){
		 ewm= getDiskBitmap("/sdcard/2wm.png");
		 jtd =  getDiskBitmap("/sdcard/jtd.png");
		 tx =  getDiskBitmap("/sdcard/tx.png");
		 cp =  getDiskBitmap("/sdcard/cp.png");
		 jl =  getDiskBitmap("/sdcard/jl.png");
		 qt =  getDiskBitmap("/sdcard/qt.png");
		try {
			q = read("2wm.txt");
			qrtext.setText(q);
			t = read("tx.txt");
			shapetext.setText(t);
			j=read("j.txt");
			traffictext.setText(j);
			j2=read("jl.txt");
			distext.setText(j2);
			c=read("cp.txt");
			carnumtext.setText(c);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	
		qr.setImageBitmap(ewm);
		traffic.setImageBitmap(jtd);
		shape.setImageBitmap(tx);
		carnum.setImageBitmap(cp);
		other.setImageBitmap(jl);
		dis.setImageBitmap(jl);
	}
	//��ȡͼƬ
	private Bitmap getDiskBitmap(String pathString)  
	{  
	    Bitmap bitmap = null;  
	    try  
	    {  
	        File file = new File(pathString);  
	        if(file.exists())  
	        {  
	            bitmap = BitmapFactory.decodeFile(pathString);  
	            Log.e("d", "��ȡ�ɹ�");
	        }  
	    } catch (Exception e)  
	    {  
	        // TODO: handle exception  
	    }  
	      
	      
	    return bitmap;  
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
