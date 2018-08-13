package com.caiotf.android_app_ros;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.constraint.ConstraintLayout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import java.util.Timer;
import java.util.TimerTask;

import geometry_msgs.Twist;

public class NodeControle implements NodeMain {

    ConstraintLayout layout_joystick, layout_joystick2;

    JoyStickClass js, js2;

    MainActivity activity;

    public NodeControle(MainActivity mainActivity) {
        activity = mainActivity;
    }

    private ToggleButton botaoAcelerometro;

    private Publisher<Twist> publisher;
    private geometry_msgs.Twist cmdVelCorrente;
    private Timer publisherTimer;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorEventListener sensorEventListener;

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {

        botaoAcelerometro = activity.findViewById(R.id.buttonAceler);

        mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        layout_joystick = activity.findViewById(R.id.layout_joystick);
        layout_joystick2 = activity.findViewById(R.id.layout_joystick2);

        String nomeTopico = "~cmd_vel";
        publisher = connectedNode.newPublisher(nomeTopico, geometry_msgs.Twist._TYPE);
        cmdVelCorrente = publisher.newMessage();

        publisherTimer = new Timer();
        publisherTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                publisher.publish(cmdVelCorrente);
            }
        }, 0, 80);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sensorEventListener = new SensorEventListener() {

                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        Float y = event.values[1];
                        Float z = event.values[2];

                        if (z >= -2 && z <= 8) {
                            cmdVelCorrente.getLinear().setX(0);
                        } else {
                            if (z > 8) {
                                cmdVelCorrente.getLinear().setX(1.0);
                            }
                            if (z < -2) {
                                cmdVelCorrente.getLinear().setX(-1.0);
                            }
                        }

                        if (y >= -1 && y <= 1) {
                            cmdVelCorrente.getAngular().setZ(0);
                        } else {
                            if (y > 1) {
                                cmdVelCorrente.getAngular().setZ(-1.0);
                            }
                            if (y < -1) {
                                cmdVelCorrente.getAngular().setZ(1.0);
                            }
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };

                js = new JoyStickClass(activity.getApplicationContext()
                        , layout_joystick, R.drawable.image_button);
                js.setStickSize(75, 75);
                js.setLayoutSize(250, 250);
                js.setLayoutAlpha(35);
                js.setStickAlpha(20);
                js.setOffset(45);
                js.setMinimumDistance(50);

                js2 = new JoyStickClass(activity.getApplicationContext()
                        , layout_joystick2, R.drawable.image_button);
                js2.setStickSize(75, 75);
                js2.setLayoutSize(250, 250);
                js2.setLayoutAlpha(35);
                js2.setStickAlpha(20);
                js2.setOffset(45);
                js2.setMinimumDistance(50);

                botaoAcelerometro.setChecked(false);
                if(!botaoAcelerometro.isChecked()){
                    mSensorManager.unregisterListener(sensorEventListener, mAccelerometer);

                    layout_joystick.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View arg0, MotionEvent arg1) {
                            js.drawStick(arg1);
                            if (arg1.getAction() == MotionEvent.ACTION_DOWN
                                    || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                                int direction = js.get8Direction();
                                if (direction == JoyStickClass.STICK_UP) {
                                    cmdVelCorrente.getLinear().setZ(1.0);
                                    cmdVelCorrente.getAngular().setZ(0);
                                } else if (direction == JoyStickClass.STICK_UPRIGHT) {
                                    cmdVelCorrente.getLinear().setZ(1.0);
                                    cmdVelCorrente.getAngular().setZ(-1.0);
                                } else if (direction == JoyStickClass.STICK_RIGHT) {
                                    cmdVelCorrente.getAngular().setZ(-1.0);
                                    cmdVelCorrente.getLinear().setZ(0);
                                } else if (direction == JoyStickClass.STICK_DOWNRIGHT) {
                                    cmdVelCorrente.getLinear().setZ(-1.0);
                                    cmdVelCorrente.getAngular().setZ(-1.0);
                                } else if (direction == JoyStickClass.STICK_DOWN) {
                                    cmdVelCorrente.getLinear().setZ(-1.0);
                                    cmdVelCorrente.getAngular().setZ(0);
                                } else if (direction == JoyStickClass.STICK_DOWNLEFT) {
                                    cmdVelCorrente.getLinear().setZ(-1.0);
                                    cmdVelCorrente.getAngular().setZ(1.0);
                                } else if (direction == JoyStickClass.STICK_LEFT) {
                                    cmdVelCorrente.getAngular().setZ(1.0);
                                    cmdVelCorrente.getLinear().setZ(0);
                                } else if (direction == JoyStickClass.STICK_UPLEFT) {
                                    cmdVelCorrente.getLinear().setZ(1.0);
                                    cmdVelCorrente.getAngular().setZ(1.0);
                                }
                            } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                                cmdVelCorrente.getLinear().setZ(0);
                                cmdVelCorrente.getAngular().setZ(0);
                            }
                            return true;
                        }
                    });

                    layout_joystick2.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View arg0, MotionEvent arg1) {
                            js2.drawStick(arg1);
                            if (arg1.getAction() == MotionEvent.ACTION_DOWN
                                    || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                                int direction = js2.get8Direction();
                                if (direction == JoyStickClass.STICK_UP) {
                                    cmdVelCorrente.getLinear().setX(1.0);
                                    cmdVelCorrente.getLinear().setY(0);
                                } else if (direction == JoyStickClass.STICK_UPRIGHT) {
                                    cmdVelCorrente.getLinear().setX(1.0);
                                    cmdVelCorrente.getLinear().setY(-1.0);
                                } else if (direction == JoyStickClass.STICK_RIGHT) {
                                    cmdVelCorrente.getLinear().setY(-1.0);
                                    cmdVelCorrente.getLinear().setX(0);
                                } else if (direction == JoyStickClass.STICK_DOWNRIGHT) {
                                    cmdVelCorrente.getLinear().setX(-1.0);
                                    cmdVelCorrente.getLinear().setY(-1.0);
                                } else if (direction == JoyStickClass.STICK_DOWN) {
                                    cmdVelCorrente.getLinear().setX(-1.0);
                                    cmdVelCorrente.getLinear().setY(0);
                                } else if (direction == JoyStickClass.STICK_DOWNLEFT) {
                                    cmdVelCorrente.getLinear().setX(-1.0);
                                    cmdVelCorrente.getLinear().setY(1.0);
                                } else if (direction == JoyStickClass.STICK_LEFT) {
                                    cmdVelCorrente.getLinear().setY(1.0);
                                    cmdVelCorrente.getLinear().setX(0);
                                } else if (direction == JoyStickClass.STICK_UPLEFT) {
                                    cmdVelCorrente.getLinear().setX(1.0);
                                    cmdVelCorrente.getLinear().setY(1.0);
                                }
                            } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                                cmdVelCorrente.getLinear().setX(0);
                                cmdVelCorrente.getLinear().setY(0);
                            }
                            return true;
                        }
                    });
                }

                botaoAcelerometro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                            layout_joystick.setOnTouchListener(new View.OnTouchListener() {
                                public boolean onTouch(View arg0, MotionEvent arg1) {
                                    js.drawStick(arg1);
                                    if (arg1.getAction() == MotionEvent.ACTION_DOWN
                                            || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                                        int direction = js.get8Direction();
                                        if (direction == JoyStickClass.STICK_UP) {
                                            cmdVelCorrente.getLinear().setZ(1.0);
                                        } else if (direction == JoyStickClass.STICK_DOWN) {
                                            cmdVelCorrente.getLinear().setZ(-1.0);
                                        }
                                    } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                                        cmdVelCorrente.getLinear().setZ(0);
                                    }
                                    return true;
                                }
                            });

                            layout_joystick2.setOnTouchListener(new View.OnTouchListener() {
                                public boolean onTouch(View arg0, MotionEvent arg1) {
                                    js2.drawStick(arg1);
                                    if (arg1.getAction() == MotionEvent.ACTION_DOWN
                                            || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                                        int direction = js2.get8Direction();
                                        if (direction == JoyStickClass.STICK_RIGHT) {
                                            cmdVelCorrente.getLinear().setY(-1.0);
                                        } else if (direction == JoyStickClass.STICK_LEFT) {
                                            cmdVelCorrente.getLinear().setY(1.0);
                                            cmdVelCorrente.getLinear().setX(0);
                                        }
                                    } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                                        cmdVelCorrente.getLinear().setY(0);
                                    }
                                    return true;
                                }
                            });
                        }

                        if(!botaoAcelerometro.isChecked()) {
                            mSensorManager.unregisterListener(sensorEventListener, mAccelerometer);

                            layout_joystick.setOnTouchListener(new View.OnTouchListener() {
                                public boolean onTouch(View arg0, MotionEvent arg1) {
                                    js.drawStick(arg1);
                                    if (arg1.getAction() == MotionEvent.ACTION_DOWN
                                            || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                                        int direction = js.get8Direction();
                                        if (direction == JoyStickClass.STICK_UP) {
                                            cmdVelCorrente.getLinear().setZ(1.0);
                                            cmdVelCorrente.getAngular().setZ(0);
                                        } else if (direction == JoyStickClass.STICK_UPRIGHT) {
                                            cmdVelCorrente.getLinear().setZ(1.0);
                                            cmdVelCorrente.getAngular().setZ(-1.0);
                                        } else if (direction == JoyStickClass.STICK_RIGHT) {
                                            cmdVelCorrente.getAngular().setZ(-1.0);
                                            cmdVelCorrente.getLinear().setZ(0);
                                        } else if (direction == JoyStickClass.STICK_DOWNRIGHT) {
                                            cmdVelCorrente.getLinear().setZ(-1.0);
                                            cmdVelCorrente.getAngular().setZ(-1.0);
                                        } else if (direction == JoyStickClass.STICK_DOWN) {
                                            cmdVelCorrente.getLinear().setZ(-1.0);
                                            cmdVelCorrente.getAngular().setZ(0);
                                        } else if (direction == JoyStickClass.STICK_DOWNLEFT) {
                                            cmdVelCorrente.getLinear().setZ(-1.0);
                                            cmdVelCorrente.getAngular().setZ(1.0);
                                        } else if (direction == JoyStickClass.STICK_LEFT) {
                                            cmdVelCorrente.getAngular().setZ(1.0);
                                            cmdVelCorrente.getLinear().setZ(0);
                                        } else if (direction == JoyStickClass.STICK_UPLEFT) {
                                            cmdVelCorrente.getLinear().setZ(1.0);
                                            cmdVelCorrente.getAngular().setZ(1.0);
                                        }
                                    } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                                        cmdVelCorrente.getLinear().setZ(0);
                                        cmdVelCorrente.getAngular().setZ(0);
                                    }
                                    return true;
                                }
                            });

                            layout_joystick2.setOnTouchListener(new View.OnTouchListener() {
                                public boolean onTouch(View arg0, MotionEvent arg1) {
                                    js2.drawStick(arg1);
                                    if (arg1.getAction() == MotionEvent.ACTION_DOWN
                                            || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                                        int direction = js2.get8Direction();
                                        if (direction == JoyStickClass.STICK_UP) {
                                            cmdVelCorrente.getLinear().setX(1.0);
                                            cmdVelCorrente.getLinear().setY(0);
                                        } else if (direction == JoyStickClass.STICK_UPRIGHT) {
                                            cmdVelCorrente.getLinear().setX(1.0);
                                            cmdVelCorrente.getLinear().setY(-1.0);
                                        } else if (direction == JoyStickClass.STICK_RIGHT) {
                                            cmdVelCorrente.getLinear().setY(-1.0);
                                            cmdVelCorrente.getLinear().setX(0);
                                        } else if (direction == JoyStickClass.STICK_DOWNRIGHT) {
                                            cmdVelCorrente.getLinear().setX(-1.0);
                                            cmdVelCorrente.getLinear().setY(-1.0);
                                        } else if (direction == JoyStickClass.STICK_DOWN) {
                                            cmdVelCorrente.getLinear().setX(-1.0);
                                            cmdVelCorrente.getLinear().setY(0);
                                        } else if (direction == JoyStickClass.STICK_DOWNLEFT) {
                                            cmdVelCorrente.getLinear().setX(-1.0);
                                            cmdVelCorrente.getLinear().setY(1.0);
                                        } else if (direction == JoyStickClass.STICK_LEFT) {
                                            cmdVelCorrente.getLinear().setY(1.0);
                                            cmdVelCorrente.getLinear().setX(0);
                                        } else if (direction == JoyStickClass.STICK_UPLEFT) {
                                            cmdVelCorrente.getLinear().setX(1.0);
                                            cmdVelCorrente.getLinear().setY(1.0);
                                        }
                                    } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                                        cmdVelCorrente.getLinear().setX(0);
                                        cmdVelCorrente.getLinear().setY(0);
                                    }
                                    return true;
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onShutdown(Node node) {

    }

    @Override
    public void onShutdownComplete(Node node) {
        publisherTimer.cancel();
        publisherTimer.purge();
    }


    @Override
    public void onError(Node node, Throwable throwable) {

    }

}
