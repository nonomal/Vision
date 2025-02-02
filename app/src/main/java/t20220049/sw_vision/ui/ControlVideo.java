package t20220049.sw_vision.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import t20220049.sw_vision.R;
import t20220049.sw_vision.bean.MemberBean;
import t20220049.sw_vision.webRTC_utils.IViewCallback;
import t20220049.sw_vision.webRTC_utils.ProxyVideoSink;
import t20220049.sw_vision.webRTC_utils.WebRTCManager;

public class ControlVideo extends AppCompatActivity implements IViewCallback {

    private FrameLayout wr_video_view;

    private WebRTCManager manager;
    private Map<String, SurfaceViewRenderer> _videoViews = new HashMap<>();
    private Map<String, ProxyVideoSink> _sinks = new HashMap<>();
    private List<MemberBean> _infos = new ArrayList<>();
    private VideoTrack _localVideoTrack;

    private int mScreenWidth;

    private EglBase rootEglBase;

    @Override
    public void onSetLocalStream(MediaStream stream, String socketId, SurfaceTextureHelper surfaceTextureHelper) {

    }

    @Override
    public void onAddRemoteStream(MediaStream stream, String socketId) {

    }

    @Override
    public void onCloseWithId(String socketId) {

    }

    public class Device {
        String type;
        String name;
    }

    RelativeLayout bottomSheet;
    BottomSheetBehavior behavior;
    RecyclerView v1;
    deviceAdapter deviceAdapter;
    List<Device> mDevicesList = new ArrayList<>();

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, ControlVideo.class);
        activity.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_control);
        //底部抽屉栏展示地址
        bottomSheet = findViewById(R.id.bottom_sheet);

//        bottomSheet.getBackground().setAlpha(60);
        behavior = BottomSheetBehavior.from(bottomSheet);
        v1 = findViewById(R.id.recyclerview);

        for (int i = 0; i < 20; i++) {
            Device device = new Device();
            device.type = "标题" + i;
            device.name = "内容" + i;
            mDevicesList.add(device);
        }

        deviceAdapter = new deviceAdapter();
        v1.setAdapter(deviceAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ControlVideo.this);
        v1.setLayoutManager(layoutManager);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
//                String state = "null";
//                switch (newState) {
//                    case 1:
//                        state = "STATE_DRAGGING";//过渡状态此时用户正在向上或者向下拖动bottom sheet
//                        behavior.setState(newState);
//                        break;
//                    case 2:
//                        state = "STATE_SETTLING"; // 视图从脱离手指自由滑动到最终停下的这一小段时间
//                        break;
//                    case 3:
//                        state = "STATE_EXPANDED"; //处于完全展开的状态
//                        break;
//                    case 4:
//                        state = "STATE_COLLAPSED"; //默认的折叠状态
//                        break;
//                    case 5:
//                        state = "STATE_HIDDEN"; //下滑动完全隐藏 bottom sheet
//                        break;
//                }
                behavior.setState(newState);

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d("BottomSheetDemo", "slideOffset:" + slideOffset);
            }
        });



    }

    class deviceAdapter extends RecyclerView.Adapter<MyViewHolder> {
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(ControlVideo.this, R.layout.device_list, null);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Device device = mDevicesList.get(position);
            holder.mType.setText(device.type);
            holder.mName.setText(device.name);
        }

        @Override
        public int getItemCount() {
            return mDevicesList.size();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mType;
        TextView mName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mType = itemView.findViewById(R.id.txt_mType);
            mName = itemView.findViewById(R.id.txt_mName);
        }
    }
}
