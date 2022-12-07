package org.example;

import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaStream;

public class PeerConnectionClient implements PeerConnectionObserver {
    private final PeerConnectionFactory peerConnectionFactory;
    private final RTCPeerConnection peerConnection;

    public PeerConnectionClient(){
        this.peerConnectionFactory = new PeerConnectionFactory();
        this.peerConnection = peerConnectionFactory.createPeerConnection(new RTCConfiguration(), this);
    }
    /**
     * @param state
     */
    @Override
    public void onSignalingChange(RTCSignalingState state) {
        PeerConnectionObserver.super.onSignalingChange(state);
    }

    @Override
    public void onConnectionChange(RTCPeerConnectionState state) {
        PeerConnectionObserver.super.onConnectionChange(state);
    }

    /**
     * @param state
     */
    @Override
    public void onIceConnectionChange(RTCIceConnectionState state) {
        PeerConnectionObserver.super.onIceConnectionChange(state);
    }

    /**
     * @param state
     */
    @Override
    public void onStandardizedIceConnectionChange(RTCIceConnectionState state) {
        PeerConnectionObserver.super.onStandardizedIceConnectionChange(state);
    }

    /**
     * @param receiving
     */
    @Override
    public void onIceConnectionReceivingChange(boolean receiving) {
        PeerConnectionObserver.super.onIceConnectionReceivingChange(receiving);
    }

    /**
     * @param state
     */
    @Override
    public void onIceGatheringChange(RTCIceGatheringState state) {
        PeerConnectionObserver.super.onIceGatheringChange(state);
    }

    /**
     * @param rtcIceCandidate
     */
    @Override
    public void onIceCandidate(RTCIceCandidate rtcIceCandidate) {

    }

    /**
     * @param event
     */
    @Override
    public void onIceCandidateError(RTCPeerConnectionIceErrorEvent event) {
        PeerConnectionObserver.super.onIceCandidateError(event);
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
    }

    /**
     * @param stream
     */
    @Override
    public void onRemoveStream(MediaStream stream) {
        PeerConnectionObserver.super.onRemoveStream(stream);
    }

    /**
     * @param dataChannel
     */
    @Override
    public void onDataChannel(RTCDataChannel dataChannel) {
        PeerConnectionObserver.super.onDataChannel(dataChannel);
    }

    /**
     *
     */
    @Override
    public void onRenegotiationNeeded() {
        PeerConnectionObserver.super.onRenegotiationNeeded();
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
