package net.zhuoweizhang.mercator;

import android.os.*;
import com.google.android.gms.ads.*;

public class MainActivity extends MercatorActivity {

	private AdView adView;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
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
}
