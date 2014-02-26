package com.aksalj.rpg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

public class Generator {

	private final static String BASE_URL = "https://www.random.org/passwords";
	public final static int MIN_PWD_LEN = 6;
	private static Generator sInstance;
	
	public static interface OnGenrateListener{
		public void onGenerate(ArrayList<String> passwords);
		public void onError(String errorMessage);
	}
	
	public static Generator getInstance(){
		if(sInstance == null)
			sInstance = new Generator();
		return sInstance;
	}
	
	private Generator() {}
	
	public void generate(int number, int length, OnGenrateListener listener){
		new GenJob(listener).execute(new String[] {String.valueOf(number), String.valueOf(length)});
	}
	
	class GenJob extends AsyncTask<String, Void, ArrayList<String>>{

		OnGenrateListener callback;
		String errorMessage = null;
		
		public GenJob(OnGenrateListener listener) {
			callback = listener;
		}
		
		@Override
		protected ArrayList<String> doInBackground(String... args) {
			try{
				String numberOfPasswords = args[0];
				String lengthOfPassword = args[1];
				
				DefaultHttpClient client = new DefaultHttpClient();
				
			    List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			    params.add(new BasicNameValuePair("num", numberOfPasswords));
			    params.add(new BasicNameValuePair("len", lengthOfPassword));
			    params.add(new BasicNameValuePair("format", "plain"));
			    params.add(new BasicNameValuePair("rnd", "new"));
			    
			    String url = BASE_URL + "?" + URLEncodedUtils.format(params, "UTF-8");
			   
			    HttpResponse response = client.execute(new HttpGet(url));
			    int code = response.getStatusLine().getStatusCode();

			    if(code != 200) {
			      throw new IOException("Got HTTP response code " + code);
			    }

			    String[] strs = EntityUtils.toString(response.getEntity()).split("\r\n|\n");
			    return new ArrayList<String>(Arrays.asList(strs));
				
				
			}catch(Exception ex){ errorMessage = ex.getMessage(); }
			
			
			return null;
		}
		
		@Override
		protected void onPostExecute(ArrayList<String> result) {
			if(callback != null){
				if(result == null) 
					callback.onError(errorMessage);
				else
					callback.onGenerate(result);
			}
		}
	}

}
