package com.caiotf.android_app_ros;

import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.ros.address.InetAddressFactory;
import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.RosActivity;
import org.ros.android.view.RosImageView;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.CameraControlLayer;
import org.ros.android.view.visualization.layer.CameraControlListener;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.OccupancyGridLayer;
import org.ros.android.view.visualization.layer.RobotLayer;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.time.NtpTimeProvider;

import java.util.concurrent.TimeUnit;

import sensor_msgs.CompressedImage;

public class MainActivity extends RosActivity {
    private int currentApiVersion;
    private RosImageView<CompressedImage> rosImageView;

    private static final String MAP_FRAME = "map";
    private static final String ROBOT_FRAME = "base_link";

    private final SystemCommands systemCommands;

    private VisualizationView visualizationView;
    private ToggleButton followMeToggleButton;
    private CameraControlLayer cameraControlLayer;

    private ConstraintLayout constraintMap;
    private LinearLayout linearLayoutMap;

    public MainActivity() {
        super("Controle", "Controle");
        systemCommands = new SystemCommands();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });
        }

        rosImageView = findViewById(R.id.image);
        rosImageView.setTopicName("/front_cam/camera/image/compressed");
        rosImageView.setMessageType("sensor_msgs/CompressedImage");
        rosImageView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        constraintMap = findViewById(R.id.constraintMapId);
        linearLayoutMap = findViewById(R.id.linearMapId);

        visualizationView = findViewById(R.id.visualization);
        cameraControlLayer = new CameraControlLayer();
        visualizationView.onCreate(Lists.<Layer>newArrayList(cameraControlLayer,
                new OccupancyGridLayer("map"), new LaserScanLayer("scan"), new RobotLayer(ROBOT_FRAME)));
        followMeToggleButton = findViewById(R.id.follow_me_toggle_button);
        enableFollowMe();

    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {

        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(), getMasterUri());

        NodeControle nodeControle = new NodeControle(this);

        nodeMainExecutor.execute(rosImageView, nodeConfiguration);
        nodeMainExecutor.execute(nodeControle, nodeConfiguration);


        visualizationView.init(nodeMainExecutor);
        cameraControlLayer.addListener(new CameraControlListener() {
            @Override
            public void onZoom(float focusX, float focusY, float factor) {
                disableFollowMe();
            }

            @Override
            public void onTranslate(float distanceX, float distanceY) {
                disableFollowMe();
            }

            @Override
            public void onRotate(float focusX, float focusY, double deltaAngle) {
                disableFollowMe();
            }

            @Override
            public void onDoubleTap(float x, float y) {
            }
        });

        nodeMainExecutor.execute(visualizationView, nodeConfiguration);
        nodeMainExecutor.execute(systemCommands, nodeConfiguration);

    }

    public void onClearMapButtonClicked(View view) {
        toast("Limpando mapa...");
        systemCommands.reset();
        enableFollowMe();
    }

    public void onSaveMapButtonClicked(View view) {
        toast("Salvando mapa...");
        systemCommands.saveGeotiff();
    }

    private void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void onFollowMeToggleButtonClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            enableFollowMe();
        } else {
            disableFollowMe();
        }
    }

    private void enableFollowMe() {
        Preconditions.checkNotNull(visualizationView);
        Preconditions.checkNotNull(followMeToggleButton);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                visualizationView.getCamera().jumpToFrame(ROBOT_FRAME);
                followMeToggleButton.setChecked(true);
            }
        });
    }

    private void disableFollowMe() {
        Preconditions.checkNotNull(visualizationView);
        Preconditions.checkNotNull(followMeToggleButton);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                visualizationView.getCamera().setFrame(MAP_FRAME);
                followMeToggleButton.setChecked(false);
            }
        });
    }

    public void onMapButtonClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            constraintMap.setVisibility(View.VISIBLE);
            linearLayoutMap.setVisibility(View.VISIBLE);
        } else {
            constraintMap.setVisibility(View.GONE);
            linearLayoutMap.setVisibility(View.GONE);
        }
    }

}
