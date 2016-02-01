package net.zhuoweizhang.mercator;

import android.os.*;
import com.google.android.gms.ads.*;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.content.DialogInterface;

public class MainActivity extends MercatorActivity {

	private AdView adView;
	private static final int PERMISSION_REQUEST_STORAGE = 1234;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (Build.VERSION.SDK_INT >= 23) grabPermissions();
		adView = (AdView) findViewById(R.id.ad);
		AdRequest adRequest = new AdRequest.Builder().
			addTestDevice("DF28838C26BDFAE7EB063BFEB7A241D3").
			addTestDevice("C0ABF0B025E43414E6EF63D720DCEFDE").
			build();
		adView.loadAd(adRequest);
	}

	@Override
	protected void onPause() {
		super.onPause();
		adView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		adView.resume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		adView.destroy();
	}

	private void grabPermissions() {
		if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_STORAGE) {
			if (permissions.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// yay
			} else {
				new AlertDialog.Builder(this).setMessage(R.string.storage_permission_required).
					setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).show();
			}
		}
	}
}
