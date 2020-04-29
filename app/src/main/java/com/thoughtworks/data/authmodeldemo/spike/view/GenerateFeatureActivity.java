package com.thoughtworks.data.authmodeldemo.spike.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import com.thoughtworks.data.authmodeldemo.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

public class GenerateFeatureActivity extends AppCompatActivity {
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();
    private MappedByteBuffer tfliteModel;
    protected Interpreter tflite;
    private TensorBuffer inputBuffer;
    private TensorBuffer outputBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {//系统会在创建 Activity 时调用此方法
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature);
        try {
            loadModel("NAIVE-MINMAX-2D_model.tflite");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("*****************load model success***************");
        generateFeature(loadSample());
    }

    private void loadModel(String filename) throws IOException {
        int inputTensorIndex = 0;
        int outputTensorIndex = 0;
        tfliteModel = FileUtil.loadMappedFile(this, filename);
        tfliteOptions.setNumThreads(4);
        tflite = new Interpreter(tfliteModel, tfliteOptions);
        int[] inputShape = tflite.getInputTensor(inputTensorIndex).shape(); //{1,25,9,1}
        DataType inputDataType = tflite.getInputTensor(inputTensorIndex).dataType();
        int[] featureShape =
                tflite.getOutputTensor(outputTensorIndex).shape(); // {1, 64}
        DataType featureDataType = tflite.getOutputTensor(outputTensorIndex).dataType();

        // Creates the input tensor.
        inputBuffer = TensorBuffer.createFixedSize(inputShape, inputDataType);

        // Creates the output tensor and its processor.
        outputBuffer = TensorBuffer.createFixedSize(featureShape, featureDataType);

        System.out.println("Created a Tensorflow Lite CNN model.");
    }

    public float[] generateFeature(List<Float> sample) {
        //sample size = 25 * 9  ->  (9+9+...+9)
        inputBuffer.loadArray(ArrayUtils.toPrimitive(sample.toArray(new Float[sample.size()]), 0.8F));
        tflite.run(inputBuffer.getBuffer(), outputBuffer.getBuffer().rewind());
        System.out.println("generate success!");
        return outputBuffer.getFloatArray();
    }

    @NotNull
    private List<Float> loadSample() {
        InputStream inputStream;
        List<Float> sample = new ArrayList<>();
        try {
            inputStream = getAssets().open("one_sample.txt");
            InputStreamReader isr = new InputStreamReader(inputStream,
                    "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line = "";

            while ((line = br.readLine()) != null) {
                List<String> strings = Arrays.asList(line.split(","));
                List<Float> collect = strings.stream().map(Float::parseFloat).collect(Collectors.toList());
                sample.addAll(collect);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sample;
    }
}
