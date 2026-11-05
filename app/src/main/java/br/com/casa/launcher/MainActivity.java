package br.com.casa.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final Service[] SERVICES = {
        new Service("YouTube", "com.google.android.youtube.tv", Color.rgb(255, 235, 233)),
        new Service("Netflix", "com.netflix.ninja", Color.rgb(246, 232, 232)),
        new Service("Prime Video", "com.amazon.amazonvideo.livingroom", Color.rgb(229, 242, 250)),
        new Service("Disney+", "com.disney.disneyplus", Color.rgb(233, 235, 250))
    };
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("pt", "BR"));
    private TextView clock;
    private TextView date;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            Date now = new Date();
            clock.setText(timeFormat.format(now));
            String formatted = dateFormat.format(now);
            date.setText(formatted.substring(0, 1).toUpperCase(new Locale("pt", "BR")) + formatted.substring(1));
            clock.postDelayed(this, 30_000L);
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setBackgroundColor(Color.rgb(248, 247, 243));
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(makeScreen());
        tick.run();
    }

    @Override protected void onDestroy() {
        if (clock != null) clock.removeCallbacks(tick);
        super.onDestroy();
    }

    private View makeScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(dp(88), dp(48), dp(88), dp(42));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView greeting = new TextView(this);
        greeting.setText("Sua TV");
        greeting.setTextSize(22f);
        greeting.setTextColor(Color.rgb(105, 108, 108));
        clock = new TextView(this);
        clock.setTextSize(48f);
        clock.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        clock.setTextColor(Color.rgb(35, 39, 39));
        top.addView(greeting);
        top.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        top.addView(clock);
        root.addView(top, new LinearLayout.LayoutParams(-1, -2));

        date = new TextView(this);
        date.setTextSize(18f);
        date.setTextColor(Color.rgb(115, 117, 114));
        date.setPadding(0, dp(4), 0, dp(34));
        root.addView(date);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        for (int i = 0; i < SERVICES.length; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(190), 1f);
            if (i > 0) params.setMarginStart(dp(18));
            row.addView(makeServiceCard(SERVICES[i]), params);
        }
        root.addView(row, new LinearLayout.LayoutParams(-1, -2));

        TextView footer = new TextView(this);
        footer.setText("Pressione OK para abrir um aplicativo");
        footer.setTextSize(14f);
        footer.setTextColor(Color.rgb(145, 146, 143));
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(28), 0, 0);
        root.addView(footer, new LinearLayout.LayoutParams(-1, -2));
        root.post(new Runnable() {
            @Override public void run() { row.getChildAt(0).requestFocus(); }
        });
        return root;
    }

    private View makeServiceCard(Service service) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setFocusable(true);
        card.setFocusableInTouchMode(true);
        card.setBackground(rounded(service.tint, 22, Color.TRANSPARENT));
        card.setPadding(dp(12), dp(12), dp(12), dp(12));

        ImageView icon = new ImageView(this);
        try {
            icon.setImageDrawable(getPackageManager().getApplicationIcon(service.packageName));
        } catch (Exception ignored) {
            icon.setImageResource(android.R.drawable.ic_media_play);
            icon.setColorFilter(Color.rgb(63, 100, 129));
        }
        icon.setContentDescription(service.label);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        card.addView(icon, new LinearLayout.LayoutParams(dp(60), dp(60)));

        TextView label = new TextView(this);
        label.setText(service.label);
        label.setTextSize(19f);
        label.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        label.setTextColor(Color.rgb(42, 45, 45));
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, dp(14), 0, 0);
        card.addView(label);

        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                android.content.Intent launch = getPackageManager().getLaunchIntentForPackage(service.packageName);
                if (launch != null) startActivity(launch);
                else Toast.makeText(MainActivity.this, service.label + " não está instalado", Toast.LENGTH_SHORT).show();
            }
        });
        card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean focused) {
                view.animate().scaleX(focused ? 1.06f : 1f).scaleY(focused ? 1.06f : 1f).setDuration(120).start();
                view.setBackground(rounded(focused ? Color.WHITE : service.tint, 22,
                    focused ? Color.rgb(100, 150, 176) : Color.TRANSPARENT));
            }
        });
        return card;
    }

    private GradientDrawable rounded(int color, int radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        if (stroke != Color.TRANSPARENT) drawable.setStroke(dp(2), stroke);
        return drawable;
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density); }

    private static final class Service {
        final String label;
        final String packageName;
        final int tint;
        Service(String label, String packageName, int tint) {
            this.label = label;
            this.packageName = packageName;
            this.tint = tint;
        }
    }
}
