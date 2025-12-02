package zlian.netgap.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import zlian.netgap.ui.SplashActivity;


public class BootListenerReceiver extends BroadcastReceiver{

	
	@Override
	public void onReceive(Context context, Intent intent) {
			
//			PendingIntent activityIntent = PendingIntent  
//	                .getActivity(context, 0, new Intent(context,MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
//			
//			long time = SystemClock.elapsedRealtime();  
//	        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);  
//	        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, activityIntent);
	        
	        Intent ootStartIntent=new Intent(context,SplashActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ootStartIntent);
	}
	
}
