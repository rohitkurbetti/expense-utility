package com.example.expenseutility.python;

import android.content.Context;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.ByteBuffer;

public class TFLiteModel {

    private Interpreter interpreter;

    public TFLiteModel(Context context) throws IOException {
        // Load the TensorFlow Lite model from the assets folder
        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, "real_estate_model.tflite");
        interpreter = new Interpreter(tfliteModel);
    }

    public float[][] runInference(float[][] input) {
        float[][] output = new float[1][1]; // Change this to match your model's output shape
        interpreter.run(input, output);
        return output;
    }
}

