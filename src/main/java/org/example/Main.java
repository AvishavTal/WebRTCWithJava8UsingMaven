package org.example;

import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.audio.AudioDevice;
import dev.onvoid.webrtc.media.video.VideoCaptureCapability;
import dev.onvoid.webrtc.media.video.VideoDevice;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.concurrent.ArrayBlockingQueue;

public class Main {
    public static void main(String[] args) {
        
//        System.out.println("Hello world!");
//
//        for (AudioDevice mic :
//                MediaDevices.getAudioCaptureDevices()) {
//            System.out.println(mic);
//
//        }
//        for (AudioDevice speaker :
//                MediaDevices.getAudioRenderDevices()) {
//            System.out.println(speaker);
//        }
//        for (VideoDevice cam :
//                MediaDevices.getVideoCaptureDevices()) {
//            System.out.println(cam);
//        }
//
//        for (VideoDevice cam :
//                MediaDevices.getVideoCaptureDevices()) {
//            for (VideoCaptureCapability capability :
//                    MediaDevices.getVideoCaptureCapabilities(cam)) {
//                System.out.println(capability);
//            }
//        }
        try {
            ArrayBlockingQueue<JSONObject> inQ = new ArrayBlockingQueue<>(256);
            ArrayBlockingQueue<JSONObject> outQ = new ArrayBlockingQueue<>(256);
            String serverUrl = "http://192.168.56.1:999/javaWebRTC";
            int id = 0;
            PeerConnection peerConnection = new PeerConnection(serverUrl,inQ,outQ,id);
        }catch (Exception e){
            e.printStackTrace();
        }


//        JSONObject helloServer = new JSONObject().put("msg", "hello java server!");
//        try {
//            outQ.put(helloServer);
//            System.out.println(outQ);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        String serverUrl = "http://127.0.0.1:999/javaWebRTC";
//        int id = 0;
//        try {
//            SignalingChannel signalingChannel = new SignalingChannel(serverUrl, inQ, outQ, id) {
//                @Override
//                void onMessage(JSONObject msg) {
//                    System.out.println("helloooo from onMessage!!!!!!!!!!!"+msg);
//                    System.out.println(msg.get("msg"));
//                }
//            };
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
    }
}