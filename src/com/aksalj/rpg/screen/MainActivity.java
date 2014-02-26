package com.aksalj.rpg.screen;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.aksalj.rpg.Generator;
import com.aksalj.rpg.Generator.OnGenrateListener;
import com.aksalj.rpg.R;

public class MainActivity extends Activity implements OnGenrateListener,
		OnSeekBarChangeListener {

	private Context mCtx;
	private Generator mGenerator = Generator.getInstance();

	private MenuItem mRefreshItem;
	private boolean mRefreshItemState = false;

	private Button btnGenerate;
	private TextView lblPwdNum, lblPwdLen, lblBits;
	private SeekBar skbPwdNum, skbPwdLen;
	private ListView lstPwds;

	OnGenrateListener quotaListener = new OnGenrateListener() {
		
		@Override
		public void onGenerate(ArrayList<String> quotas) {
			String str = quotas.get(0);
			long bits = Long.parseLong(str);
			lblBits.setText(getString(R.string.bits_format,NumberFormat.getInstance().format(bits)));
			if(bits <= 1500){
				lblBits.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
			}else{
				lblBits.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
			}
		}
		
		@Override
		public void onError(String errorMessage) {
			Toast.makeText(mCtx, errorMessage + "", Toast.LENGTH_SHORT).show();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_main);

		setProgressBarIndeterminateVisibility(false);

		mCtx = this;
		btnGenerate = (Button) findViewById(R.id.btnGen);

		lblPwdNum = (TextView) findViewById(R.id.lblPwdNum);
		lblPwdLen = (TextView) findViewById(R.id.lblPwdLen);
		lblBits = (TextView)findViewById(R.id.lblBitsAllowance);

		skbPwdNum = (SeekBar) findViewById(R.id.skbPwdNum);
		skbPwdNum.setOnSeekBarChangeListener(this);
		skbPwdLen = (SeekBar) findViewById(R.id.skbPwdLen);
		skbPwdLen.setOnSeekBarChangeListener(this);

		lstPwds = (ListView) findViewById(R.id.lstPwds);
		lstPwds.setEmptyView(findViewById(android.R.id.empty));
		
		
		//Silently check quota
		mGenerator.checkQuota(quotaListener);
		
		showEULADialog();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		mRefreshItem = menu.findItem(R.id.action_refresh);
		setProgressBarVisible(mRefreshItemState);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(item.getItemId() == R.id.action_about){
			showAboutDialog();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void showEULADialog() {
		Builder alert = new Builder(this);
		alert.setCancelable(true);
		alert.setTitle(R.string.disclaimer);
		
		WebView view = new WebView(this);
		view.loadUrl("file:///android_asset/html/disclaimer.html");
		alert.setView(view);
		alert.show();
    }
	
	private void showAboutDialog(){
		Builder alert = new Builder(this);
		alert.setCancelable(true);
		alert.setTitle(R.string.about);
		
		WebView view = new WebView(this);
		view.loadUrl("file:///android_asset/html/about.html");
		alert.setView(view);
		alert.show();
	}

	public void setProgressBarVisible(boolean animated) {
		mRefreshItemState = animated;
		if (mRefreshItem == null) {
			return;
		}
		if (animated) {
			mRefreshItem.setVisible(true);
			mRefreshItem
					.setActionView(R.layout.actionbar_indeterminate_progress);
		} else {
			mRefreshItem.setVisible(false);
			mRefreshItem.setActionView(null);
		}
	}

	private void toggleLoadingMode() {
		btnGenerate.setEnabled(!btnGenerate.isEnabled());
		skbPwdLen.setEnabled(!skbPwdLen.isEnabled());
		skbPwdNum.setEnabled(!skbPwdNum.isEnabled());

		setProgressBarVisible(!mRefreshItemState);
	}

	public void generate(View view) {
		toggleLoadingMode();

		int num = skbPwdNum.getProgress();
		int len = skbPwdLen.getProgress();
		mGenerator.generate(num, len, this);
	}

	@Override
	public void onGenerate(ArrayList<String> passwords) {
		toggleLoadingMode();

		ArrayList<String> quota = new ArrayList<String>();
		quota.add(String.valueOf(Generator.getQuota()));
		quotaListener.onGenerate(quota);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, passwords);
		lstPwds.setAdapter(adapter);

		lstPwds.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				String password = (String) parent.getItemAtPosition(position);
				ClipboardManager cliboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText(password, password);
				cliboard.setPrimaryClip(clip);
				Toast.makeText(mCtx, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	public void onError(String errorMessage) {
		toggleLoadingMode();
		Toast.makeText(this, errorMessage + "", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		if (!fromUser)
			return;

		switch (seekBar.getId()) {
		case R.id.skbPwdLen:
			if (progress < Generator.MIN_PWD_LEN) {
				// Toast.makeText(this,
				// getString(R.string.pwd_len_req,Generator.MIN_PWD_LEN),
				// Toast.LENGTH_SHORT).show();
				seekBar.setProgress(Generator.MIN_PWD_LEN);
			}
			lblPwdLen.setText(String.valueOf(seekBar.getProgress()));
			break;

		case R.id.skbPwdNum:
			if (progress == 0) {
				seekBar.setProgress(1);
			}
			lblPwdNum.setText(String.valueOf(seekBar.getProgress()));
			break;
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { }

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { }
}
