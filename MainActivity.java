package ye.mahatta.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.net.Uri;

public class MainActivity extends Activity {
    private static final String PREFS = "mahatta_mobile";
    private static final String SERVER_KEY = "server_url";
    private static final int NAVY = Color.rgb(6, 41, 77);
    private static final int BLUE = Color.rgb(8, 121, 216);
    private static final int BG = Color.rgb(243, 247, 252);
    private WebView webView;
    private String serverUrl = "";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        serverUrl = prefs.getString(SERVER_KEY, "");
        if (serverUrl.trim().isEmpty()) showServerSetup();
        else openWebApp(serverUrl);
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radiusDp * getResources().getDisplayMetrics().density);
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.RIGHT);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void showServerSetup() {
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setGravity(Gravity.CENTER);
        outer.setPadding(dp(24), dp(30), dp(24), dp(30));
        outer.setBackgroundColor(NAVY);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(22), dp(26), dp(22), dp(24));
        card.setBackground(rounded(Color.WHITE, 22));
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView logo = text("⛽  محطتك", 27, NAVY, true);
        logo.setGravity(Gravity.CENTER);
        TextView subtitle = text("تطبيق إدارة المحطات النفطية", 15, Color.rgb(95,115,140), false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(9), 0, dp(20));

        TextView label = text("عنوان خادم النظام", 14, NAVY, true);
        label.setPadding(0, dp(8), 0, dp(8));

        EditText url = new EditText(this);
        url.setSingleLine(true);
        url.setTextSize(15);
        url.setHint("https://your-domain.com");
        url.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        url.setTextDirection(View.TEXT_DIRECTION_LTR);
        url.setPadding(dp(12), dp(12), dp(12), dp(12));
        url.setBackground(rounded(BG, 10));
        if (!serverUrl.isEmpty()) url.setText(serverUrl);

        TextView hint = text("أدخل رابط الخادم الذي يشغّل نظام محطتك. يجب تشغيل الخادم وقاعدة البيانات أولاً.", 12, Color.rgb(90,108,130), false);
        hint.setPadding(0, dp(12), 0, dp(18));

        Button connect = new Button(this);
        connect.setText("اتصال وفتح النظام");
        connect.setTextColor(Color.WHITE);
        connect.setTextSize(15);
        connect.setAllCaps(false);
        connect.setBackground(rounded(BLUE, 12));
        connect.setPadding(dp(10), dp(7), dp(10), dp(7));

        Button change = new Button(this);
        change.setText("تغيير عنوان الخادم");
        change.setTextSize(13);
        change.setAllCaps(false);
        change.setBackground(rounded(Color.rgb(232,242,251), 12));
        change.setTextColor(NAVY);
        change.setVisibility(serverUrl.isEmpty() ? View.GONE : View.VISIBLE);

        card.addView(logo);
        card.addView(subtitle);
        card.addView(label);
        card.addView(url, new LinearLayout.LayoutParams(-1, dp(52)));
        card.addView(hint);
        card.addView(connect, new LinearLayout.LayoutParams(-1, dp(52)));
        card.addView(change, new LinearLayout.LayoutParams(-1, dp(48)));
        outer.addView(card, new LinearLayout.LayoutParams(-1, -2));
        setContentView(outer);

        connect.setOnClickListener(v -> {
            String value = url.getText().toString().trim();
            if (value.isEmpty()) {
                Toast.makeText(this, "أدخل عنوان الخادم أولاً", Toast.LENGTH_LONG).show();
                return;
            }
            if (!value.startsWith("https://") && !value.startsWith("http://")) value = "https://" + value;
            Uri uri = Uri.parse(value);
            if (uri.getHost() == null) {
                Toast.makeText(this, "عنوان الخادم غير صحيح", Toast.LENGTH_LONG).show();
                return;
            }
            if (value.endsWith("/")) value = value.substring(0, value.length()-1);
            serverUrl = value;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(SERVER_KEY, serverUrl).apply();
            ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(url.getWindowToken(), 0);
            openWebApp(serverUrl);
        });
        change.setOnClickListener(v -> {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(SERVER_KEY).apply();
            serverUrl = "";
            showServerSetup();
        });
    }

    private void openWebApp(String url) {
        webView = new WebView(this);
        webView.setBackgroundColor(BG);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri target = request.getUrl();
                String host = target.getHost();
                Uri base = Uri.parse(serverUrl);
                if (host != null && host.equals(base.getHost())) return false;
                Toast.makeText(MainActivity.this, "تم منع فتح رابط خارجي داخل التطبيق", Toast.LENGTH_SHORT).show();
                return true;
            }
            @Override public void onReceivedError(WebView view, android.webkit.WebResourceRequest request,
                    android.webkit.WebResourceError error) {
                if (request.isForMainFrame()) {
                    Toast.makeText(MainActivity.this, "تعذر الاتصال بالخادم. تحقق من الرابط والإنترنت.", Toast.LENGTH_LONG).show();
                }
            }
        });
        setContentView(webView);
        webView.loadUrl(url);
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else if (webView != null) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("محطتك")
                .setMessage("هل تريد الخروج من التطبيق؟")
                .setNegativeButton("إلغاء", (d,w) -> d.dismiss())
                .setPositiveButton("خروج", (d,w) -> finish())
                .show();
        } else super.onBackPressed();
    }
}
