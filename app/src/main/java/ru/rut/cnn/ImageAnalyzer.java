package ru.rut.cnn;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import ru.rut.cnn.ml.Landmarks;

public class ImageAnalyzer implements ImageAnalysis.Analyzer {
    private RecognitionListener listener;
    private Landmarks model;

    public ImageAnalyzer(Context context, RecognitionListener listener) {
        try {
            this.listener = listener;
            this.model = Landmarks.newInstance(context);
        } catch (IOException e) {
            Log.e("ImageAnalyzer", "Error: " + e.getMessage());
        }
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        TensorImage tensorImage = TensorImage.fromBitmap(image.toBitmap());
        Landmarks.Outputs outputs = model.process(tensorImage);

        List<Category> sortedOutputs = outputs.getProbabilityAsCategoryList()
                .stream()
                .sorted((item1, item2) -> Float.compare(item1.getScore(), item2.getScore()))
                .collect(Collectors.toList());

        listener.onResult(sortedOutputs.get(sortedOutputs.size() - 1));
        image.close();
    }
}
