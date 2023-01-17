package com.ankk.motiontracker;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Delegate;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Here is a "test/sandbox" version. It will be modified through our development.
 * @author : nicolasperrin, alexismerienne, kevinegbohou, kevinconstantin
 */

public class ActivityClassifier {
    private Interpreter interpreter;
    private Delegate delegate = null;
    private float[][] input;
    private float[][] output;
    private boolean useGpu = true;
    private boolean useNnApi = true;

    public ActivityClassifier(Activity activity) throws IOException {
        // Load the TensorFlow Lite model
        MappedByteBuffer tfliteModel = loadModelFile(activity);

        // Choose the type of delegate to use
        if (useGpu) {
            delegate = new GpuDelegate();
        } else if (useNnApi) {
            delegate = new NnApiDelegate();
        }

        // Initialize the Interpreter with the TensorFlow Lite model and the chosen delegate
        Interpreter.Options options = new Interpreter.Options();
        options.addDelegate(delegate);
        interpreter = new Interpreter(tfliteModel, options);

        // Initialize the input and output arrays
        input = new float[1][6];
        output = new float[1][4];
    }

    // Load the TensorFlow Lite model from the assets folder
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public ActivityType classifyActivity(float accelerometer_x, float accelerometer_y, float accelerometer_z,
                                         float gyroscope_x, float gyroscope_y, float gyroscope_z) {
        input[0][0] = accelerometer_x;
        input[0][1] = accelerometer_y;
        input[0][2] = accelerometer_z;
        input[0][3] = gyroscope_x;
        input[0][4] = gyroscope_y;
        input[0][5] = gyroscope_z;

        // Classify the activity based on the input data
        interpreter.run(input, output);

        // Find the index of the highest output value
        int index = -1;
        float max = 0.0f;
        for (int i = 0; i < 4; i++) {
            if (output[0][i] > max) {
                max = output[0][i];
                index = i;
            }
        }

        // Return the activity corresponding to the highest output value
        switch (index) {
            case 0:
                return ActivityType.RUNNING;
            case 1:
                return ActivityType.WALKING;
            case 3:
                return ActivityType.NOTHING;
            case 2:
                return ActivityType.OTHER_ACTIVITY;
            default:
                return ActivityType.OTHER_ACTIVITY;
        }
    }

    // Close the interpreter and the delegate when the app is closed
    public void close() {
        interpreter.close();
        //TODO : Voir comment close() le delegate.
        /*if (delegate != null) {
            delegate.close();
        }*/
    }

    // Enum for the different types of activities
    private enum ActivityType {
        RUNNING,
        WALKING,
        OTHER_ACTIVITY,
        NOTHING,
    }
}

