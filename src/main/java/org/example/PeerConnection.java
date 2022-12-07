package org.example;

import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaStream;
import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.video.VideoDesktopSource;
import dev.onvoid.webrtc.media.video.VideoDevice;
import dev.onvoid.webrtc.media.video.VideoTrack;
import dev.onvoid.webrtc.media.video.VideoTrackSink;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import static java.util.Objects.nonNull;

public class PeerConnection implements PeerConnectionObserver {
    private final SignalingChannel signallingChannel;
    private final PeerConnectionFactory factory;
    private RTCPeerConnection peerConnection;
    private VideoDesktopSource desktopSource;
    private RTCDataChannel dataChannel;


    public PeerConnection(String serverUrl, ArrayBlockingQueue<JSONObject> inQ, ArrayBlockingQueue<JSONObject> outQ, int id) {
        this.factory = new PeerConnectionFactory();
        this.setPeerConnection();
        try {
            this.signallingChannel = new SignalingChannel(serverUrl, inQ,outQ,id) {
                @Override
                void onMessage(JSONObject msg) {
                    if (msg.getString("type").equals("offer")){
                        JSONObject offer = msg.getJSONObject("offer");
                        peerConnection.setRemoteDescription(new RTCSessionDescription(RTCSdpType.OFFER,
                                offer.getString("sdp")), new SetSessionDescriptionObserver() {
                            @Override
                            public void onSuccess() {
                                System.out.println("successful setting remote description");
                                //init media
                                addDesktop();
                                addDataChannel();
                                //todo activate desktop here?
                                setDesktopActive(true);
                                peerConnection.createAnswer(new RTCAnswerOptions(), new CreateSessionDescriptionObserver() {
                                    @Override
                                    public void onSuccess(RTCSessionDescription rtcSessionDescription) {
                                        peerConnection.setLocalDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                                            @Override
                                            public void onSuccess() {
                                                System.out.println("successful setting local description ");
                                                JSONObject msg = new JSONObject().put("type", "answer")
                                                        .put("answer" ,new JSONObject().put("type", "answer").put("sdp", rtcSessionDescription.sdp));
                                                signallingChannel.send(msg);
                                            }

                                            @Override
                                            public void onFailure(String s) {
                                                System.out.println("setting Answer failed: "+s);

                                            }
                                        });

                                    }

                                    @Override
                                    public void onFailure(String s) {
                                        System.out.println("create answer failed "+s);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(String s) {
                                System.out.println("set remote description failed: "+s);

                            }
                        });

                    } else if (msg.getString("type").equals("candidate")) {
                        JSONObject candidate = msg.getJSONObject("candidate");
//                        System.out.println(candidate.toString());
                        peerConnection.addIceCandidate(new RTCIceCandidate(
                                candidate.getString("sdpMid"),
                                candidate.getInt("sdpMLineIndex"),
                                candidate.getString("candidate")
                        ));

                    }


                }
            };
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void setPeerConnection() {
//        PeerConnectionFactory factory = new PeerConnectionFactory();

        RTCIceServer iceServer = new RTCIceServer();
        iceServer.urls.add("stun:stun.l.google.com:19302");

        RTCConfiguration configuration = new RTCConfiguration();
        configuration.iceServers.add(iceServer);

        this.peerConnection = factory.createPeerConnection(configuration, this);
//        addDataChannel();
    }

    private void addDataChannel(){
        RTCDataChannelInit options = new RTCDataChannelInit();
        options.id=0;
        options.negotiated=true;
        this.dataChannel = peerConnection.createDataChannel("test", options);
        dataChannel.registerObserver(new RTCDataChannelObserver() {
            @Override
            public void onBufferedAmountChange(long l) {

            }

            @Override
            public void onStateChange() {
                System.out.println("data channel state changed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                if (dataChannel.getState().toString().equals("OPEN")){
//                    dataChannel.send();
                    System.out.println("data channel open##################################################");
                    String firstMessage = "Hello from Java8!!!!!!!!!!!!";
                    ByteBuffer data = ByteBuffer.wrap(firstMessage.getBytes(StandardCharsets.UTF_8));
                    RTCDataChannelBuffer buffer = new RTCDataChannelBuffer(data, false);
                    try {
                        System.out.println(buffer.data);
                        dataChannel.send(buffer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onMessage(RTCDataChannelBuffer rtcDataChannelBuffer) {
                ByteBuffer byteBuffer = rtcDataChannelBuffer.data;
                byte[] payload;
                if (byteBuffer.hasArray()){
                    payload=byteBuffer.array();
                }else {
                    payload=new byte[byteBuffer.limit()];
                    byteBuffer.get(payload);
                }
                String data = new String(payload, StandardCharsets.UTF_8);
                System.out.println(data);
            }
        });
    }

    public void setDesktopActive(boolean toActivate){
        RTCRtpSender[] senders = peerConnection.getSenders();
        System.out.println("senderrrrrrrrrrrrrssssssssssssssssssssssssssssssssss");
        if (nonNull(senders)){

            for (RTCRtpSender sender :
                    senders) {
                MediaStreamTrack track = sender.getTrack();

//                if (track.getKind().equals("desktopTrack")){
                    track.setEnabled(toActivate);

                    System.out.println("track"+ track.getKind()+"set enabled to "+toActivate);
//                }
            }
        }
    }
    
    public void addDesktop(){
        desktopSource = new VideoDesktopSource();
        //todo set frame rate

        VideoTrack videoTrack = factory.createVideoTrack("desktopTrack", desktopSource);
//        VideoTrackSink sink = System.out::println;

//        videoTrack.addSink(sink);
        desktopSource.start();
        ArrayList<String> streamIds = new ArrayList<>();
        streamIds.add("stream");
        peerConnection.addTrack(videoTrack, streamIds);

        for (RTCRtpTransceiver transceiver :
                peerConnection.getTransceivers()) {
            MediaStreamTrack track = transceiver.getSender().getTrack();
            if (nonNull(track)&&
            track.getKind().equals(MediaStreamTrack.VIDEO_TRACK_KIND)&&
            track.getId().equals("desktopTrack")){
                transceiver.setDirection(RTCRtpTransceiverDirection.SEND_ONLY);
            }
        }
    }

    /**
     * @param state
     */
    @Override
    public void onSignalingChange(RTCSignalingState state) {
        PeerConnectionObserver.super.onSignalingChange(state);
        System.out.println("signaling changed: "+state.toString());
    }

    /**
     * @param state
     */
    @Override
    public void onConnectionChange(RTCPeerConnectionState state) {
        PeerConnectionObserver.super.onConnectionChange(state);
        System.out.println("connection changed: "+state.toString());
    }

    /**
     * @param state
     */
    @Override
    public void onIceConnectionChange(RTCIceConnectionState state) {
        PeerConnectionObserver.super.onIceConnectionChange(state);
        System.out.println("ice connection changed: "+state.toString());
    }

    /**
     * @param state
     */
    @Override
    public void onStandardizedIceConnectionChange(RTCIceConnectionState state) {
        PeerConnectionObserver.super.onStandardizedIceConnectionChange(state);
        System.out.println("standardized ice connection changed:  "+state.toString());
    }

    /**
     * @param receiving
     */
    @Override
    public void onIceConnectionReceivingChange(boolean receiving) {
        PeerConnectionObserver.super.onIceConnectionReceivingChange(receiving);
        System.out.println("ice connection receiving changed: "+receiving);
    }

    /**
     * @param state
     */
    @Override
    public void onIceGatheringChange(RTCIceGatheringState state) {
        PeerConnectionObserver.super.onIceGatheringChange(state);
        System.out.println("ice gathering changed: "+state.toString());
    }

    /**
     * @param rtcIceCandidate
     */
    @Override
    public void onIceCandidate(RTCIceCandidate rtcIceCandidate) {
//        System.out.println("ice candidate!!!!!!!!!:  "+rtcIceCandidate.toString());
        JSONObject msg = new JSONObject().put("type", "candidate").put("candidate",
                new JSONObject().put("sdpMLineIndex", rtcIceCandidate.sdpMLineIndex)
                        .put("sdpMid",rtcIceCandidate.sdpMid)
                        .put("sdp", rtcIceCandidate.sdp));
        signallingChannel.send(msg);
    }

    /**
     * @param event
     */
    @Override
    public void onIceCandidateError(RTCPeerConnectionIceErrorEvent event) {
        PeerConnectionObserver.super.onIceCandidateError(event);
        System.out.println("ice candidate error: "+event.toString());
    }

    /**
     * @param candidates
     */
    @Override
    public void onIceCandidatesRemoved(RTCIceCandidate[] candidates) {
        PeerConnectionObserver.super.onIceCandidatesRemoved(candidates);
    }

    /**
     * @param stream
     */
    @Override
    public void onAddStream(MediaStream stream) {
        PeerConnectionObserver.super.onAddStream(stream);
        System.out.println("add stream: "+stream.toString());
    }

    /**
     * @param stream
     */
    @Override
    public void onRemoveStream(MediaStream stream) {
        PeerConnectionObserver.super.onRemoveStream(stream);
        System.out.println("remove stream: "+stream);
    }

    /**
     * @param dataChannel
     */
    @Override
    public void onDataChannel(RTCDataChannel dataChannel) {
        PeerConnectionObserver.super.onDataChannel(dataChannel);
        System.out.println("data channel: "+ dataChannel.toString());//todo here we maybe have todo some real work
    }

    /**
     *
     */
    @Override
    public void onRenegotiationNeeded() {
        PeerConnectionObserver.super.onRenegotiationNeeded();
        System.out.println("renegotiation needed");
        this.peerConnection.createOffer(new RTCOfferOptions(), new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription rtcSessionDescription) {
                System.out.println("successful offer creation");
                peerConnection.setLocalDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        System.out.println("successful setting local description ");
                        JSONObject msg = new JSONObject().put("type", "offer").put("offer",new JSONObject().put("type","offer").put("sdp",rtcSessionDescription.sdp));
//                        JSONObject msg = new JSONObject().put("type", "offer").put("sdp", rtcSessionDescription.sdp);
                        signallingChannel.send(msg);
                    }

                    @Override
                    public void onFailure(String s) {
                        System.out.println("setting offer failed: "+s);
                    }
                });
            }

            @Override
            public void onFailure(String s) {
                System.out.println("create offer failed   "+s);
            }
        });

    }

    /**
     * @param receiver
     * @param mediaStreams
     */
    @Override
    public void onAddTrack(RTCRtpReceiver receiver, MediaStream[] mediaStreams) {
        PeerConnectionObserver.super.onAddTrack(receiver, mediaStreams);
    }

    /**
     * @param receiver
     */
    @Override
    public void onRemoveTrack(RTCRtpReceiver receiver) {
        PeerConnectionObserver.super.onRemoveTrack(receiver);
    }

    /**
     * @param transceiver
     */
    @Override
    public void onTrack(RTCRtpTransceiver transceiver) {
        PeerConnectionObserver.super.onTrack(transceiver);
    }
}
