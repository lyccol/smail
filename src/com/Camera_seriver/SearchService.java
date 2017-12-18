package com.Camera_seriver;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.bkrcl.control_car_video.camerautil.SearchCameraUtil;
import com.example.qcsxcxt.MainActivity;


public class SearchService extends Service{
	//À—À˜…„œÒÕ∑IP¿‡
    private SearchCameraUtil searchCameraUtil=null;
  //…„œÒÕ∑IP
    private String IP=null;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		thread.start();
	}
	private Thread thread=new Thread(new Runnable() {
		
		public void run() {
			// TODO Auto-generated method stub
			while(IP==null||IP.equals("")){
				searchCameraUtil=new SearchCameraUtil();
				IP=searchCameraUtil.send();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			handler.sendEmptyMessage(10);
		}
	});
	private Handler handler=new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what==10){
				Intent intent=new Intent(MainActivity.A_S);
				intent.putExtra("IP", IP+":81");
				sendBroadcast(intent);
				SearchService.this.stopSelf();
			}
		};
	};
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
