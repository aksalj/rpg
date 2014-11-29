package me.aksalj.rpg;

/**
 * Copyright (c) 2014 Salama AB
 * All rights reserved
 * Contact: aksalj@aksalj.me
 * Website: http://www.aksalj.me
 * <p/>
 * Project : RPG
 * File : me.aksalj.rpg.MainActivity
 * Date : Nov, 29 2014 11:04 AM
 * Description :
 */

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements Generator.OnGenerateListener,
        SeekBar.OnSeekBarChangeListener {

    private Context mCtx;
    private ActionBar mActionBar;
    private Generator mGenerator = Generator.getInstance();

    private Button btnGenerate;
    private TextView lblPwdNum, lblPwdLen, lblBits, lblInstruction;
    private SeekBar skbPwdNum, skbPwdLen;
    private ListView lstPwds;

    Generator.OnGenerateListener quotaListener = new Generator.OnGenerateListener() {

        @Override
        public void onGenerate(ArrayList<String> quotas) {
            String str = quotas.get(0);
            long bits = Long.parseLong(str);
            lblBits.setText(getString(R.string.bits_format, NumberFormat.getInstance().format(bits)));
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

        setContentView(R.layout.activity_main);

        mCtx = this;

        mActionBar = getSupportActionBar();
        mActionBar.setTitle(R.string.app_name_long);


        btnGenerate = (Button) findViewById(R.id.btnGen);

        lblPwdNum = (TextView) findViewById(R.id.lblPwdNum);
        lblPwdLen = (TextView) findViewById(R.id.lblPwdLen);
        lblBits = (TextView)findViewById(R.id.lblBitsAllowance);
        lblInstruction = (TextView)findViewById(R.id.lblInstuction);

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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_clear:

                break;
            case R.id.action_about:
                showAboutDialog();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showEULADialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setTitle(R.string.disclaimer);
        alert.setPositiveButton(R.string.i_understand, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        WebView view = new WebView(this);
        view.loadUrl("file:///android_asset/html/disclaimer.html");
        alert.setView(view);
        alert.show();
    }

    private void showAboutDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(true);
        alert.setTitle(R.string.about);

        WebView view = new WebView(this);
        view.loadUrl("file:///android_asset/html/about.html");
        alert.setView(view);
        alert.show();
    }

    private void toggleLoadingMode() {
        btnGenerate.setEnabled(!btnGenerate.isEnabled());
        skbPwdLen.setEnabled(!skbPwdLen.isEnabled());
        skbPwdNum.setEnabled(!skbPwdNum.isEnabled());

        int id = !btnGenerate.isEnabled() ? R.string.please_wait : R.string.generate;
        btnGenerate.setText(getString(id));
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

        if(passwords.size() > 0){
            lblInstruction.setVisibility(View.VISIBLE);
        } else {
            lblInstruction.setVisibility(View.GONE);
        }

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