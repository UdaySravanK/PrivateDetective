package com.usk.personal.privatedetective;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
//		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
		context.startService(new Intent(context, USKIntentService.class));
//		}
	}
}
