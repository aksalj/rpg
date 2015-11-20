package me.aksalj.rpg;

/**
 * Copyright (c) 2014 Salama AB
 * All rights reserved
 * Contact: aksalj@aksalj.me
 * Website: http://www.aksalj.me
 * <p/>
 * Project : RPG
 * File : me.aksalj.rpg.Generator
 * Date : Nov, 29 2014 11:04 AM
 * Description :
 */

import java.io.IOException;
import java.util.ArrayList;


import android.os.AsyncTask;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class Generator {

    private final static String UA = "RPG Android - aksalj@aksalj.me";
    private final static String BASE_URL = "https://www.random.org/passwords";
    private final static String QUOTA_URL = "https://www.random.org/quota/?format=plain";
    public final static int MIN_PWD_LEN = 6;
    private static Generator sInstance;

    private static long sQuota = 1500; //Assume we have more than enough?

    public static interface OnGenerateListener {
        public void onGenerate(ArrayList<String> data);

        public void onError(String errorMessage);
    }

    public static Generator getInstance() {
        if (sInstance == null)
            sInstance = new Generator();
        return sInstance;
    }

    public static long getQuota() {
        return sQuota;
    }

    private Generator() {
    }

    public void generate(int number, int length, OnGenerateListener listener) {
        new GenJob(listener).execute(new String[]{String.valueOf(number), String.valueOf(length)});
    }

    public void checkQuota(OnGenerateListener listener) {
        new QuotaJob(listener).execute(new String[]{});
    }

    class GenJob extends AsyncTask<String, Void, ArrayList<String>> {

        OnGenerateListener callback;
        String errorMessage = null;

        public GenJob(OnGenerateListener listener) {
            callback = listener;
        }


        protected boolean isValidResponseData(Response response) {
            return response.header("Content-Type").contains("text/plain");
        }

        @Override
        protected ArrayList<String> doInBackground(String... args) {
            try {

                if (sQuota <= 50) {
                    throw new Exception("Your current bits allowance (" + sQuota + ") is low!");
                }

                String numberOfPasswords = args[0];
                String lengthOfPassword = args[1];


                String url = BASE_URL + String.format("?num=%s&len=%s&format=plain&rnd=new", numberOfPasswords, lengthOfPassword);

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", UA)
                        .build();
                Response response = client.newCall(request).execute();

                int code = response.code();
                boolean validData = isValidResponseData(response);

                if (code != 200 || !validData) {
                    throw new IOException("Unable to generate passowrds :(");
                }

                String body = response.body().string();

                //Get Set Quota
                new QuotaJob(null).doInBackground(new String[]{});

                String[] strs = body.split("\r\n|\n");
                ArrayList<String> passwords = new ArrayList<String>();
                for (String str : strs) {
                    str.trim();
                    if (str.isEmpty()) continue;
                    passwords.add(str);
                }
                return passwords;


            } catch (Exception ex) {
                errorMessage = ex.getMessage();
            }


            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            try {
                if (callback != null) {
                    if (result == null)
                        callback.onError(errorMessage);
                    else
                        callback.onGenerate(result);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    class QuotaJob extends GenJob {

        public QuotaJob(OnGenerateListener listener) {
            super(listener);
        }

        @Override
        protected ArrayList<String> doInBackground(String... args) {

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(QUOTA_URL)
                        .header("User-Agent", UA)
                        .build();
                Response response = client.newCall(request).execute();
                int code = response.code();
                boolean validData = isValidResponseData(response);

                if (code != 200 || !validData) {
                    throw new IOException("Unable to get quota :(");
                }

                String quota = response.body().string().trim();

                sQuota = Long.parseLong(quota);

                ArrayList<String> res = new ArrayList<String>();
                res.add(quota);
                return res;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }

}
