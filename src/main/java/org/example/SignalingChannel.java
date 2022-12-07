package org.example;

import okhttp3.*;
import org.json.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class SignalingChannel {
    private final Dispatcher dispatcher;
    private final int id;
    private final Receiver receiver;

    private URL url;
    private String urlStr;
    private ArrayBlockingQueue<JSONObject> inQ;
    private ArrayBlockingQueue<JSONObject> outQ;
    public SignalingChannel(String serverUrl, ArrayBlockingQueue<JSONObject> inQ, ArrayBlockingQueue<JSONObject> outQ, int id) throws MalformedURLException {
        this.url = new URL(serverUrl);
        this.urlStr = serverUrl;
        this.inQ = inQ;
        this.outQ = outQ;
        this.id = id;
        this.dispatcher = new Dispatcher();
        this.dispatcher.start();
        this.receiver = new Receiver();
        this.receiver.start();

    }
    abstract void onMessage(JSONObject msg);
    public void send(JSONObject msg) {
        this.outQ.offer(msg);
    }
    private class Dispatcher extends Thread{
//        Dispatcher(ThreadGroup group){
//            super(group, "dispatcher");
//        }
        @Override
        public void run (){
            while (true){
                JSONObject toSend = new JSONObject();
                toSend.put("type", "msg");
                toSend.put("id", id);

                try {
                    JSONObject msg = outQ.take();
//                    System.out.println("msg from outQ inside dispatcher: "+msg);
                    toSend.put("msg", msg.toString());
                    System.out.println(toSend);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
//                OkHttpClient client = new OkHttpClient.Builder()
//                        .readTimeout(1000, TimeUnit.MILLISECONDS)
//                        .writeTimeout(1000, TimeUnit.MILLISECONDS)
//                        .build();
                String toSendStr = toSend.toString();
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);
                    try (OutputStream outputStream = connection.getOutputStream()){
                        byte[] input = toSendStr.getBytes(StandardCharsets.UTF_8);
                        outputStream.write(input,0,input.length);
                    }

                    try (BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                    )){
                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine=bufferedReader.readLine())!=null){
                            response.append(responseLine.trim());
                        }
//                        System.out.println(response.toString());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                RequestBody body = RequestBody.create(
//                        toSendStr,
//                        MediaType.parse("application/json")
//                );
//
//                Request request = new Request.Builder()
//                        .url(url)
//                        .header("Connection", "close")
//                        .post(body)
//                        .build();
//
//                try (Response response = client.newCall(request).execute()) {
//                    System.out.println(response.body());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }


            }
        }
    }

    private class Receiver extends Thread{
        @Override
        public void run(){
            while (true) {
                JSONObject toSend = new JSONObject();
                toSend.put("type", "getMessage");
                toSend.put("id", id);

                String toSendStr = toSend.toString();

                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);

                    try (OutputStream outputStream = connection.getOutputStream()){
                        byte[] input = toSendStr.getBytes(StandardCharsets.UTF_8);
                        outputStream.write(input, 0, input.length);

                    }

                    try (BufferedReader bufferedreader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                    )){
                        StringBuilder response = new StringBuilder();
                        String responseLIne = null;

                        while ((responseLIne = bufferedreader.readLine())!=null){
                            response.append(responseLIne.trim());
                        }
//                        System.out.println(response.toString());
                        inQ.put(new JSONObject(response.toString()));
                        onMessage(new JSONObject(new JSONObject(response.toString()).getString("msg")));

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


//                OkHttpClient client = new OkHttpClient.Builder()
//                        .readTimeout(1000, TimeUnit.MILLISECONDS)
//                        .writeTimeout(1000, TimeUnit.MILLISECONDS)
//                        .build();
//
//                RequestBody body = RequestBody.create(
//                        toSendStr,
//                        MediaType.parse("application/json")
//                );
//
//                Request request = new Request.Builder()
//                        .url(url)
//                        .header("Connection", "close")
//                        .post(body)
//                        .build();
//
//                try (Response response = client.newCall(request).execute()){
//                    System.out.println(response.body().string());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }


            }
        }
    }
}
