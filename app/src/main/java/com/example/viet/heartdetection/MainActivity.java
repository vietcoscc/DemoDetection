package com.example.viet.heartdetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final Logger LOGGER = new Logger();
    private Classifier detector;
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/biensoxemay.pb";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/biensoxemay.txt";
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.2f;
    private static final boolean MAINTAIN_ASPECT = false;
    private Integer sensorOrientation = 0;
    private ImageView ivImage;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private Handler handler;
    private HandlerThread handlerThread;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<Object> arrObject = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LOGGER.d("onCreate " + this);
        try {
            initTensorflow();
//            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTensorflow() throws Exception {
        detector = TensorFlowObjectDetectionAPIModel.create(
                getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        ivImage = findViewById(R.id.ivImage);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewAdapter = new RecyclerViewAdapter(arrObject);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void initViews() {
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test4);
        final Bitmap mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        int previewWidth = mutable.getWidth();
        int previewHeight = mutable.getHeight();

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        final Bitmap cropedBitmap = Bitmap.createScaledBitmap(mutable, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, false);
        runInBackground(new Runnable() {
            @Override
            public void run() {
                final long startTime = SystemClock.uptimeMillis();
                List<Classifier.Recognition> results = detector.recognizeImage(cropedBitmap);
                long time = SystemClock.uptimeMillis() - startTime;
                System.out.println("Time procs : " + time);
                Canvas canvas = new Canvas(mutable);
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(20.0f);
                for (final Classifier.Recognition result : results) {
                    if (result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                        RectF location = result.getLocation();
                        cropToFrameTransform.mapRect(location);
                        result.setLocation(location);
//                        System.out.println(result);
                        canvas.drawRect(location, paint);
                        int x = (int) location.left;
                        int y = (int) location.top;
                        int width = (int) (location.right - location.left);
                        int height = (int) (location.bottom - location.top);
                        final Bitmap cutBitmap = cutBitmap(mutable, x, y, width, height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerViewAdapter.addItem(new Object(cutBitmap, result.getId() + "," + result.getTitle()+","+result.getConfidence()));
                            }
                        });

                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivImage.setImageBitmap(mutable);
                    }
                });

            }
        });

    }

    private Bitmap cutBitmap(Bitmap originalBitmap, int x, int y, int width, int height) {
        Bitmap cutBitmap = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cutBitmap);
        Rect srcRect = new Rect(x, y, x + width, y + height);
        Rect desRect = new Rect(0, 0, width, height);
        canvas.drawBitmap(originalBitmap, srcRect, desRect, null);
        return cutBitmap;
    }

    @Override
    public synchronized void onResume() {
        LOGGER.d("onResume " + this);
        super.onResume();

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        initViews();
    }

    @Override
    public synchronized void onPause() {
        LOGGER.d("onPause " + this);

        if (!isFinishing()) {
            LOGGER.d("Requesting finish");
            finish();
        }

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            LOGGER.e(e, "Exception!");
        }

        super.onPause();
    }

    @Override
    public synchronized void onStart() {
        LOGGER.d("onStart " + this);
        super.onStart();
    }

    @Override
    public synchronized void onStop() {
        LOGGER.d("onStop " + this);
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        LOGGER.d("onDestroy " + this);
        super.onDestroy();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }
}
