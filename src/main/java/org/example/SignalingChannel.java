
import okhttp3.*;
import org.json.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SignalingChannel {
    private final Dispatcher dispatcher;
    private final String id;
    private final String remoteId;
    private URL url;
    private String urlStr;
    private ArrayBlockingQueue<JSONObject> inQ;
    private ArrayBlockingQueue<JSONObject> outQ;
    public SignalingChannel(String serverUrl, ArrayBlockingQueue<JSONObject> inQ, ArrayBlockingQueue<JSONObject> outQ ,String id, String remoteId) throws MalformedURLException {
        this.url = new URL(serverUrl);
        this.urlStr = serverUrl;
        this.inQ = inQ;
        this.outQ = outQ;
        this.id = id;
        this.remoteId = remoteId;
        this.dispatcher = new Dispatcher();
        this.dispatcher.start();
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
                toSend.put("remoteId", remoteId);

                try {
                    JSONObject msg = outQ.take();
                    toSend.put("msg", msg);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                OkHttpClient client = new OkHttpClient.Builder()
                        .readTimeout(1000, TimeUnit.MILLISECONDS)
                        .writeTimeout(1000, TimeUnit.MILLISECONDS)
                        .build();
                String toSendStr = toSend.toString();
                RequestBody body = RequestBody.create(
                        toSendStr,
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    System.out.println(response.body());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

//                HttpURLConnection connection = null;
////
//                try {
//                    connection = (HttpURLConnection) url.openConnection();
//                    connection.setRequestMethod("POST");
//                    connection.setRequestProperty("Content-Type", "application/json");
//
//                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//                    wr.writeBytes(toSend.toString());
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
                toSend.put("remoteId", remoteId );

                OkHttpClient client = new OkHttpClient.Builder()
                        .readTimeout(1000, TimeUnit.MILLISECONDS)
                        .writeTimeout(1000, TimeUnit.MILLISECONDS)
                        .build();


            }
        }
    }
}
