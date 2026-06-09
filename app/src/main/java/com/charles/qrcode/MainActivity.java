package com.charles.qrcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.compose.ui.platform.ComposeView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton scanBtn;
    private TextView messageText, messageFormat;
    private ImageView placeholderIcon;
    private LinearLayout resultActions;

    private FirebaseAnalytics mFirebaseAnalytics;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    private Timer timer;
    private TimerTask timerTask;

    // Database & History properties
    private DatabaseHelper dbHelper;
    private HistoryAdapter historyAdapter;
    private List<ScanItem> historyList;
    private RecyclerView historyRecyclerView;
    private LinearLayout historyEmptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        dbHelper = new DatabaseHelper(this);

        // Referencing Scan Screen UI components
        scanBtn = findViewById(R.id.scanBtn);
        messageText = findViewById(R.id.textContent);
        messageFormat = findViewById(R.id.textFormat);
        placeholderIcon = findViewById(R.id.placeholderIcon);
        resultActions = findViewById(R.id.resultActions);

        // Add scan button click listener
        scanBtn.setOnClickListener(this);

        // Action Buttons inside the Scan result card
        View copyBtn = findViewById(R.id.copyBtn);
        View shareBtn = findViewById(R.id.shareBtn);

        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyToClipboard(messageText.getText().toString());
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareText(messageText.getText().toString());
            }
        });

        // Referencing History Screen UI components
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyEmptyLayout = findViewById(R.id.historyEmptyLayout);
        ImageButton clearAllBtn = findViewById(R.id.clearAllBtn);

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyList, new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ScanItem item) {
                // Navigate to scan page and show selected item content
                BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
                bottomNavigation.setSelectedItemId(R.id.nav_scan);
                displayScanResult(item.getContent(), item.getFormat());
            }

            @Override
            public void onCopyClick(ScanItem item) {
                copyToClipboard(item.getContent());
            }

            @Override
            public void onShareClick(ScanItem item) {
                shareText(item.getContent());
            }

            @Override
            public void onDeleteClick(ScanItem item) {
                deleteHistoryItem(item);
            }
        });
        historyRecyclerView.setAdapter(historyAdapter);

        clearAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllHistory();
            }
        });

        // Initialize and Setup Bottom Navigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_scan) {
                findViewById(R.id.layout_scan).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_history).setVisibility(View.GONE);
                findViewById(R.id.layout_support).setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.nav_history) {
                findViewById(R.id.layout_scan).setVisibility(View.GONE);
                findViewById(R.id.layout_history).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_support).setVisibility(View.GONE);
                loadHistory();
                return true;
            } else if (itemId == R.id.nav_support) {
                findViewById(R.id.layout_scan).setVisibility(View.GONE);
                findViewById(R.id.layout_history).setVisibility(View.GONE);
                findViewById(R.id.layout_support).setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

        // Initialize Compose feedback screen
        ComposeView composeView = findViewById(R.id.composeView);
        com.charles.qrcode.ui.feedback.FeedbackBridgeKt.setupFeedbackView(composeView, this);

        // Initialize Ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Load Interstitial ad
        InterstitialAd.load(this, getString(R.string.admob_interstitial_unit_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
            }
        });
    }

    @Override
    public void onClick(View v) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // Show interstitial ad
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(MainActivity.this);
                }

                String contents = intentResult.getContents();
                String formatName = intentResult.getFormatName();

                // Save scan to SQLite history
                dbHelper.insertScan(contents, formatName);

                // Populate views with result
                displayScanResult(contents, formatName);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void displayScanResult(String contents, String formatName) {
        messageText.setText(contents);
        messageFormat.setText(formatName);
        placeholderIcon.setVisibility(View.GONE);
        resultActions.setVisibility(View.VISIBLE);
    }

    private void copyToClipboard(String text) {
        if (text == null || text.trim().isEmpty() || text.startsWith("Ready to scan")) {
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("message", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), "Message copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void shareText(String text) {
        if (text == null || text.trim().isEmpty() || text.startsWith("Ready to scan")) {
            return;
        }
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void loadHistory() {
        historyList = dbHelper.getAllScans();
        if (historyList.isEmpty()) {
            historyRecyclerView.setVisibility(View.GONE);
            historyEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            historyRecyclerView.setVisibility(View.VISIBLE);
            historyEmptyLayout.setVisibility(View.GONE);
            historyAdapter.setItems(historyList);
        }
    }

    private void deleteHistoryItem(ScanItem item) {
        dbHelper.deleteScan(item.getId());
        loadHistory();
        Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
    }

    private void clearAllHistory() {
        if (historyList == null || historyList.isEmpty()) {
            Toast.makeText(this, "History is already empty", Toast.LENGTH_SHORT).show();
            return;
        }
        dbHelper.clearAll();
        loadHistory();
        Toast.makeText(this, "Scan history cleared", Toast.LENGTH_SHORT).show();
    }
}