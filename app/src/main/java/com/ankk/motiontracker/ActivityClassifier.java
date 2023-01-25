package com.ankk.motiontracker;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

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
    private float[][] input;
    private float[][] output;
    private boolean useGpu = true;
    private boolean useNnApi = true;

    public ActivityClassifier(Activity activity) throws IOException {
        // Load the TensorFlow Lite model
        MappedByteBuffer tfliteModel = loadModelFile(activity);

        // Choose the type of delegate to use


        // Initialize the Interpreter with the TensorFlow Lite model and the chosen delegate
        //Interpreter.Options options = new Interpreter.Options();
        interpreter = new Interpreter(tfliteModel);

        // Initialize the input and output arrays
        input = new float[1][6];
        output = new float[1][3];
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

    public ActivityType classifyActivity(float[][] input) {


        // Classify the activity based on the input data
        interpreter.run(input, output);



        // Find the index of the highest output value
        int index = -1;
        float max = 0.0f;
        for (int i = 0; i < 3; i++) {
            System.out.println(output[0][i]);
            if (output[0][i] > max) {
                max = output[0][i];
                index = i;
            }
        }
        // Return the activity corresponding to the highest output value
        switch (index) {
            case 0:
                return ActivityType.NOTHING;
            case 1:
                return ActivityType.WALKING;
            case 2:
                return ActivityType.RUNNING;
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
    public enum ActivityType {
        RUNNING,
        WALKING,
        OTHER_ACTIVITY,
        NOTHING,
    }
}

