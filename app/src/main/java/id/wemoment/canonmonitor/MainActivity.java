package id.wemoment.canonmonitor;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

public class MainActivity extends Activity {
    private FrameLayout root;
    private TextureView preview;
    private TextView status;
    private TextView connectButton;
    private USBMonitor usbMonitor;
    private UVCCamera camera;
    private USBMonitor.UsbControlBlock pendingControlBlock;
    private boolean previewReady = false;

    private final USBMonitor.OnDeviceConnectListener deviceListener =
        new USBMonitor.OnDeviceConnectListener() {
            @Override public void onAttach(UsbDevice device) {
                runOnUiThread(() -> setStatus("USB terdeteksi: " + device.getDeviceName() + "\nKetuk HUBUNGKAN."));
            }
            @Override public void onDettach(UsbDevice device) {
                runOnUiThread(() -> {
                    stopCamera();
                    setStatus("Capture card terputus. Sambungkan kembali melalui USB OTG.");
                });
            }
            @Override public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                pendingControlBlock = ctrlBlock;
                runOnUiThread(() -> startCamera(ctrlBlock));
            }
            @Override public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                runOnUiThread(() -> {
                    stopCamera();
                    setStatus("Perangkat USB terputus.");
                });
            }
            @Override public void onCancel(UsbDevice device) {
                runOnUiThread(() -> setStatus("Izin USB dibatalkan."));
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        preview = new TextureView(this);
        root.addView(preview, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        preview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override public void onSurfaceTextureAvailable(android.graphics.SurfaceTexture surface, int w, int h) {
                previewReady = true;
                if (pendingControlBlock != null) startCamera(pendingControlBlock);
            }
            @Override public void onSurfaceTextureSizeChanged(android.graphics.SurfaceTexture surface, int w, int h) {}
            @Override public boolean onSurfaceTextureDestroyed(android.graphics.SurfaceTexture surface) {
                previewReady = false;
                stopCamera();
                return true;
            }
            @Override public void onSurfaceTextureUpdated(android.graphics.SurfaceTexture surface) {}
        });

        LinearLayout hud = new LinearLayout(this);
        hud.setOrientation(LinearLayout.HORIZONTAL);
        hud.setGravity(Gravity.CENTER_VERTICAL);
        hud.setPadding(18, 10, 18, 10);
        hud.setBackgroundColor(0xB0000000);
        FrameLayout.LayoutParams hudParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
        root.addView(hud, hudParams);

        status = new TextView(this);
        status.setTextColor(Color.WHITE);
        status.setTextSize(13);
        status.setText("CANON HDMI MONITOR  •  Menunggu capture card USB…");
        status.setMaxLines(2);
        hud.addView(status, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        connectButton = new TextView(this);
        connectButton.setText("HUBUNGKAN");
        connectButton.setTextColor(Color.WHITE);
        connectButton.setTextSize(13);
        connectButton.setGravity(Gravity.CENTER);
        connectButton.setPadding(18, 12, 18, 12);
        connectButton.setBackgroundColor(0xFF1769AA);
        hud.addView(connectButton);
        connectButton.setOnClickListener(v -> requestDevice());

        TextView gridButton = new TextView(this);
        gridButton.setText(" GRID ");
        gridButton.setTextColor(Color.WHITE);
        gridButton.setGravity(Gravity.CENTER);
        gridButton.setPadding(10, 12, 10, 12);
        hud.addView(gridButton);
        gridButton.setOnClickListener(v -> toggleGrid());

        setContentView(root);
        usbMonitor = new USBMonitor(this, deviceListener);
    }

    private boolean gridVisible = false;
    private View gridOverlay;

    private void toggleGrid() {
        if (gridOverlay != null) {
            root.removeView(gridOverlay);
            gridOverlay = null;
            gridVisible = false;
            return;
        }
        // Simple rule-of-thirds grid drawn as a transparent custom View.
        gridOverlay = new View(this) {
            private final android.graphics.Paint paint = new android.graphics.Paint(1);
            @Override protected void onDraw(android.graphics.Canvas canvas) {
                super.onDraw(canvas);
                paint.setColor(0x88FFFFFF);
                paint.setStrokeWidth(1.5f);
                float w = getWidth(), h = getHeight();
                canvas.drawLine(w/3f, 0, w/3f, h, paint);
                canvas.drawLine(2*w/3f, 0, 2*w/3f, h, paint);
                canvas.drawLine(0, h/3f, w, h/3f, paint);
                canvas.drawLine(0, 2*h/3f, w, 2*h/3f, paint);
            }
        };
        gridOverlay.setClickable(false);
        root.addView(gridOverlay, new FrameLayout.LayoutParams(-1, -1));
        gridVisible = true;
    }

    private void requestDevice() {
        if (usbMonitor == null) return;
        try {
            for (UsbDevice device : usbMonitor.getDeviceList()) {
                usbMonitor.requestPermission(device);
                setStatus("Meminta izin USB untuk capture card…");
                return;
            }
            setStatus("Capture card belum terdeteksi. Sambungkan USB-C OTG dan capture card.");
        } catch (Exception e) {
            setStatus("Gagal memeriksa USB: " + e.getMessage());
        }
    }

    private void startCamera(USBMonitor.UsbControlBlock controlBlock) {
        if (!previewReady || preview.getSurfaceTexture() == null || controlBlock == null) {
            setStatus("USB tersambung. Menyiapkan layar preview…");
            return;
        }
        stopCamera();
        try {
            camera = new UVCCamera();
            camera.open(controlBlock);
            // Prefer 720p for lower latency and less USB/CPU load; device may negotiate another mode.
            try {
                camera.setPreviewSize(1280, 720, UVCCamera.FRAME_FORMAT_MJPEG);
            } catch (Exception ignored) {
                camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                    UVCCamera.FRAME_FORMAT_MJPEG);
            }
            camera.setPreviewDisplay(new Surface(preview.getSurfaceTexture()));
            camera.startPreview();
            setStatus("LIVE • HDMI USB aktif  |  Ketuk layar untuk menyembunyikan panel");
            connectButton.setText("SAMBUNG ULANG");
        } catch (Exception e) {
            stopCamera();
            setStatus("Tidak bisa memulai video. Coba kabel OTG lain atau mode HDMI 720p. " + e.getMessage());
        }
    }

    private void stopCamera() {
        if (camera != null) {
            try { camera.stopPreview(); } catch (Exception ignored) {}
            try { camera.close(); } catch (Exception ignored) {}
            try { camera.destroy(); } catch (Exception ignored) {}
            camera = null;
        }
    }

    private void setStatus(String message) {
        if (status != null) status.setText(message);
    }

    @Override protected void onStart() {
        super.onStart();
        if (usbMonitor != null) usbMonitor.register();
    }

    @Override protected void onStop() {
        if (usbMonitor != null) usbMonitor.unregister();
        stopCamera();
        super.onStop();
    }

    @Override protected void onDestroy() {
        stopCamera();
        if (usbMonitor != null) {
            usbMonitor.destroy();
            usbMonitor = null;
        }
        super.onDestroy();
    }
}
