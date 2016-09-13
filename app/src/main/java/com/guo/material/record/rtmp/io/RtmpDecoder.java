package com.guo.material.record.rtmp.io;

import android.util.Log;

import com.guo.material.record.rtmp.packets.Abort;
import com.guo.material.record.rtmp.packets.Acknowledgement;
import com.guo.material.record.rtmp.packets.Audio;
import com.guo.material.record.rtmp.packets.Command;
import com.guo.material.record.rtmp.packets.Data;
import com.guo.material.record.rtmp.packets.RtmpHeader;
import com.guo.material.record.rtmp.packets.RtmpPacket;
import com.guo.material.record.rtmp.packets.SetChunkSize;
import com.guo.material.record.rtmp.packets.SetPeerBandwidth;
import com.guo.material.record.rtmp.packets.UserControl;
import com.guo.material.record.rtmp.packets.Video;
import com.guo.material.record.rtmp.packets.WindowAckSize;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author francois
 */
public class RtmpDecoder {

    private static final String TAG = "RtmpDecoder";

    private RtmpSessionInfo rtmpSessionInfo;

    public RtmpDecoder(RtmpSessionInfo rtmpSessionInfo) {
        this.rtmpSessionInfo = rtmpSessionInfo;
    }

    public RtmpPacket readPacket(InputStream in) throws IOException {

        RtmpHeader header = RtmpHeader.readHeader(in, rtmpSessionInfo);
        RtmpPacket rtmpPacket;
        Log.d(TAG, "readPacket(): header.messageType: " + header.getMessageType());

        ChunkStreamInfo chunkStreamInfo = rtmpSessionInfo.getChunkStreamInfo(header.getChunkStreamId());

        chunkStreamInfo.setPrevHeaderRx(header);

        if (header.getPacketLength() > rtmpSessionInfo.getRxChunkSize()) {
            //Log.d(TAG, "readPacket(): packet size (" + header.getPacketLength() + ") is bigger than chunk size (" + rtmpSessionInfo.getChunkSize() + "); storing chunk data");
            // This packet consists of more than one chunk; store the chunks in the chunk stream until everything is read
            if (!chunkStreamInfo.storePacketChunk(in, rtmpSessionInfo.getRxChunkSize())) {
                Log.d(TAG, " readPacket(): returning null because of incomplete packet");
                return null; // packet is not yet complete
            } else {
                Log.d(TAG, " readPacket(): stored chunks complete packet; reading packet");
                in = chunkStreamInfo.getStoredPacketInputStream();
            }
        } else {
            //Log.d(TAG, "readPacket(): packet size (" + header.getPacketLength() + ") is LESS than chunk size (" + rtmpSessionInfo.getChunkSize() + "); reading packet fully");
        }

        switch (header.getMessageType()) {

            case SET_CHUNK_SIZE:
                SetChunkSize setChunkSize = new SetChunkSize(header);
                setChunkSize.readBody(in);
                Log.d(TAG, "readPacket(): Setting chunk size to: " + setChunkSize.getChunkSize());
                rtmpSessionInfo.setRxChunkSize(setChunkSize.getChunkSize());
                return null;
            case ABORT:
                rtmpPacket = new Abort(header);
                break;
            case USER_CONTROL_MESSAGE:
                rtmpPacket = new UserControl(header);
                break;
            case WINDOW_ACKNOWLEDGEMENT_SIZE:
                rtmpPacket = new WindowAckSize(header);
                break;
            case SET_PEER_BANDWIDTH:
                rtmpPacket = new SetPeerBandwidth(header);
                break;
            case AUDIO:
                rtmpPacket = new Audio(header);
                break;
            case VIDEO:
                rtmpPacket = new Video(header);
                break;
            case COMMAND_AMF0:
                rtmpPacket = new Command(header);
                break;
            case DATA_AMF0:
                rtmpPacket = new Data(header);
                break;
            case ACKNOWLEDGEMENT:
                rtmpPacket = new Acknowledgement(header);
                break;
            default:
                throw new IOException("No packet body implementation for message type: " + header.getMessageType());
        }                
        rtmpPacket.readBody(in);                        
        return rtmpPacket;
    }
}
