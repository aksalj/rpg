package com.aksalj.rpg;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.aksalj.rpg.Generator.OnGenrateListener;

public class MainActivity extends Activity implements OnGenrateListener, OnSeekBarChangeListener {

	
	private Generator mGenerator = Generator.getInstance();
	
	MenuItem mRefreshItem;
	boolean mRefreshItemState = false;
	
	Button btnGenerate;
	TextView lblPwdNum, lblPwdLen;
	SeekBar skbPwdNum, skbPwdLen;
	ListView lstPwds;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.activity_main);
		
		setProgressBarIndeterminateVisibility(false);
		
		btnGenerate = (Button)findViewById(R.id.btnGen);
		
		lblPwdNum = (TextView)findViewById(R.id.lblPwdNum);
		lblPwdLen = (TextView)findViewById(R.id.lblPwdLen);
		
		skbPwdNum = (SeekBar)findViewById(R.id.skbPwdNum);
		skbPwdNum.setOnSeekBarChangeListener(this);
		skbPwdLen = (SeekBar)findViewById(R.id.skbPwdLen);
		skbPwdLen.setOnSeekBarChangeListener(this);
		
		lstPwds = (ListView)findViewById(R.id.lstPwds);
	}
	
	public void setProgressBarVisible(boolean animated) {
		mRefreshItemState = animated;
		if (mRefreshItem == null) { return; }

		if (animated) {
			mRefreshItem.setVisible(true);
			mRefreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
		} else {
			mRefreshItem.setVisible(false);
			mRefreshItem.setActionView(null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		mRefreshItem = menu.findItem(R.id.action_refresh);
		setProgressBarVisible(mRefreshItemState);
		return true;
	}

	public void generate(View view){
		btnGenerate.setEnabled(false);
		setProgressBarVisible(true);
		
		int num = skbPwdNum.getProgress();
		int len = skbPwdLen.getProgress();
		mGenerator.generate(num, len, this);
	}

	@Override
	public void onGenerate(ArrayList<String> passwords) {
		btnGenerate.setEnabled(true);
		setProgressBarVisible(false);
		
		//TODO: add refresh list
		
	}

	@Override
	public void onError(String errorMessage) {
		Toast.makeText(this, errorMessage+"", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
		if(!fromUser) return;
		
		switch (seekBar.getId()) {
		case R.id.skbPwdLen:
			if(progress < Generator.MIN_PWD_LEN){
				//Toast.makeText(this, 
				//		getString(R.string.pwd_len_req,Generator.MIN_PWD_LEN),
				//		Toast.LENGTH_SHORT).show();
				seekBar.setProgress(Generator.MIN_PWD_LEN);
			}
			lblPwdLen.setText(String.valueOf(seekBar.getProgress()));
			break;

		case R.id.skbPwdNum:
			if(progress == 0){
				seekBar.setProgress(1);
			}
			lblPwdNum.setText(String.valueOf(progress));
			break;
		}
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}
}
