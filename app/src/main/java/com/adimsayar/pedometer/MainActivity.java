package com.adimsayar.pedometer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener {

    private SensorManager sensorManager;
    private Sensor countSensor;

    private boolean activityRunning;
    private boolean activityOPen;
    private boolean sayacInputState = false;

    private ProgressBar progress_adim_sayisi;
    private ProgressBar progress_kalori_miktari;

    private TextView tvAdimSayisi;
    private TextView tvKaloriMiktari;

    private ImageView start;
    private ImageView reset;

    final Context context = this;
    TextView tvAciklama = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuButtonDisabled();
        init();

        loadAdimSayisi();
        loadKaloriMiktari();

        loadProgressAdimSayisi();
        loadProgressKaloriMiktari();

        loadProgressMaxAdim();
        loadProgressMaxKalori();

        loadSayacDegeriGirildimi();

        if (sayacInputState)
            loadYeniHedef();
        else
            Toast.makeText(getApplicationContext(), "Kalori hedefi belirlenmedi! Lütfen 'Yeni Hedef' ile kendinize bir kalori hedefi belirleyiniz..", Toast.LENGTH_LONG).show();

        clickListenerLoad();


        activityOPen = true;


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.information:
                showInformationDialog();
                break;
            case R.id.kaloriTable:
                showKaloriTableDialog();
                break;
            case R.id.kaloriCetveli:
                showKaloriCetveliDialog();
                break;
            case R.id.vke:
                showVKEDialog();
                break;
        }
        return true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;

        saveAdimSayisi(StaticValues.LOAD_ADIMSAYISI, Integer.parseInt(tvAdimSayisi.getText().toString()));
        saveProgressAdimSayisi(StaticValues.LOAD_PROGRESSADIMSAYISI, progress_adim_sayisi.getProgress());

        float kalori = Float.parseFloat(tvKaloriMiktari.getText().toString().replace(',', '.'));
        saveKaloriMiktari(StaticValues.LOAD_KALORIMIKTARI, kalori);
        saveProgressKaloriMiktari(StaticValues.LOAD_PROGRESSKALORIMIKTARI, progress_kalori_miktari.getProgress());

        saveProgressMaxAdimSayisi(StaticValues.LOAD_PROGRESSMAXADIM, progress_adim_sayisi.getMax());
        saveProgressMaxKalori(StaticValues.LOAD_PROGRESSMAXKALORI, progress_kalori_miktari.getMax());

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activityOPen) activityOPen = false;

        else {
            if (activityRunning) {

                tvAdimSayisi.setText(String.valueOf(Integer.parseInt(tvAdimSayisi.getText().toString()) + 1));
                progress_adim_sayisi.setProgress(progress_adim_sayisi.getProgress() + 1);
                float kalori = Float.parseFloat(tvAdimSayisi.getText().toString()) * StaticValues.adimKalori;
                tvKaloriMiktari.setText(String.format("%.2f", kalori));
                progress_kalori_miktari.setProgress((int) kalori);

                if (sayacInputState) {
                    if (tvAdimSayisi.getText().toString().compareTo(String.valueOf(progress_adim_sayisi.getMax())) == 0) {
                        finishKalori(tvKaloriMiktari.getText().toString());

                    }
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgStart:
                showHedefKaloriDialog();
                break;
            case R.id.imgReset:
                resetTvProgress();

                progress_adim_sayisi.setMax(0);
                progress_kalori_miktari.setMax(0);
                saveSayacDegeriGirildimi("sayac", false);

                Toast.makeText(context, "Değerler sıfırlandı.Lütfen yeni hedef kalorinizi belirleyiniz", Toast.LENGTH_LONG).show();
                break;

        }

    }

    private void clickListenerLoad() {
        start.setOnClickListener(this);
        reset.setOnClickListener(this);

    }

    private void loadSayacDegeriGirildimi() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sayacInputState = sharedPreferences.getBoolean(StaticValues.LOAD_SAYACDEGERIGIRILDIMI, StaticValues.DEFAULT_BOOL);

    }

    private void loadYeniHedef() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String yeniHedef = sharedPreferences.getString(StaticValues.LOAD_YENIHEDEF, StaticValues.DEFAULT_STR);
        Toast.makeText(context, "Kalori hedefiniz: " + yeniHedef + "\nAtmanız gereken adım sayısı: " + maxAdimHesapla(yeniHedef) + "\nİyi yürüyüşler :)", Toast.LENGTH_LONG).show();

    }

    private void loadAdimSayisi() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int adim_sayisi = sharedPreferences.getInt(StaticValues.LOAD_ADIMSAYISI, StaticValues.DEFAULT_INT);
        tvAdimSayisi.setText(String.valueOf(adim_sayisi));

    }

    private void loadProgressAdimSayisi() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int progressAdimSayisi = sharedPreferences.getInt(StaticValues.LOAD_PROGRESSADIMSAYISI, StaticValues.DEFAULT_INT);
        progress_adim_sayisi.setProgress(progressAdimSayisi);

    }

    private void loadKaloriMiktari() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        float kaloriMiktari = sharedPreferences.getFloat(StaticValues.LOAD_KALORIMIKTARI, StaticValues.DEFAULT_FLT);
        tvKaloriMiktari.setText(String.valueOf(kaloriMiktari));

    }

    private void loadProgressKaloriMiktari() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        float progressKaloriMiktari = sharedPreferences.getFloat(StaticValues.LOAD_PROGRESSKALORIMIKTARI, StaticValues.DEFAULT_FLT);
        progress_kalori_miktari.setProgress(Math.round(progressKaloriMiktari));

    }

    private void loadProgressMaxAdim() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int max = sharedPreferences.getInt(StaticValues.LOAD_PROGRESSMAXADIM, StaticValues.DEFAULT_INT);
        progress_adim_sayisi.setMax(max);

    }

    private void loadProgressMaxKalori() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int max = sharedPreferences.getInt(StaticValues.LOAD_PROGRESSMAXKALORI, StaticValues.DEFAULT_INT);
        progress_kalori_miktari.setMax(max);

    }

    private void saveSayacDegeriGirildimi(String key, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();

    }

    private void saveAdimSayisi(String key, int value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();

    }

    private void saveProgressAdimSayisi(String key, int value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();

    }

    private void saveProgressMaxAdimSayisi(String key, int value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();

    }

    private void saveKaloriMiktari(String key, float value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.commit();

    }

    private void saveProgressKaloriMiktari(String key, float value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.commit();

    }

    private void saveProgressMaxKalori(String key, int value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();

    }

    private void saveYeniHedef(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();

    }

    private void init() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        progress_adim_sayisi = (ProgressBar) findViewById(R.id.progress_adim_sayisi);
        progress_kalori_miktari = (ProgressBar) findViewById(R.id.progress_kalori_miktari);
        tvAdimSayisi = (TextView) findViewById(R.id.adim_sayisi);
        tvKaloriMiktari = (TextView) findViewById(R.id.yakilan_kalori);

        start = (ImageView) findViewById(R.id.imgStart);
        reset = (ImageView) findViewById(R.id.imgReset);

    }

    protected void onResume() {
        super.onResume();
        activityRunning = true;
        countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor,
                    SensorManager.SENSOR_DELAY_UI);
        } else
            Toast.makeText(context, "Sensör bulunamadı!", Toast.LENGTH_SHORT).show();

    }

    private void resetTvProgress() {
        tvAdimSayisi.setText(StaticValues.DEFAULT_STR);
        tvKaloriMiktari.setText(StaticValues.DEFAULT_STR);
        progress_adim_sayisi.setProgress(StaticValues.DEFAULT_INT);
        progress_kalori_miktari.setProgress(StaticValues.DEFAULT_INT);
        progress_adim_sayisi.setMax(StaticValues.DEFAULT_INT);
        progress_kalori_miktari.setMax(StaticValues.DEFAULT_INT);

    }

    private void menuButtonDisabled() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField(StaticValues.MENUHIDEKEY);
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showInformationDialog() {
        final Dialog dialog = new Dialog(context, R.style.Dialog_No_Border);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.information, null);
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.layout_root);

        Button btnTamam = (Button) view.findViewById(R.id.btnTamam);
        layout.setBackgroundResource(R.drawable.round_dialog);

        View.OnClickListener mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        };

        btnTamam.setOnClickListener(mClickListener);
        dialog.setContentView(view);
        dialog.show();

    }

    public void showHedefKaloriDialog() {
        final Dialog dialog = new Dialog(context, R.style.Dialog_No_Border);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.sayac_baslat, null);

        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.layout_root);
        Button btnTamam = (Button) view.findViewById(R.id.btnTamam);
        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);


        final EditText userInput = (EditText) view.findViewById(R.id.hedefAdimSayisi);

        View.OnClickListener mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {

                    case R.id.btnTamam:
                        if (userInput.getText().length() == 0)
                            Toast.makeText(context, "Kalori hedefi girmediniz!", Toast.LENGTH_SHORT).show();
                        else {
                            sayacInputState = true;
                            saveSayacDegeriGirildimi(StaticValues.LOAD_SAYACDEGERIGIRILDIMI, true);
                            saveYeniHedef(StaticValues.LOAD_YENIHEDEF, userInput.getText().toString());

                            resetTvProgress();

                            progress_adim_sayisi.setMax(maxAdimHesapla(userInput.getText().toString()));
                            progress_kalori_miktari.setMax((int) Math.round(Float.parseFloat(userInput.getText().toString())));

                            Toast.makeText(context, "Yeni kalori hedefiniz: " + userInput.getText().toString() + "\nAtmanız gereken adım sayısı: " + maxAdimHesapla(userInput.getText().toString()) + "\nİyi yürüyüşler :)", Toast.LENGTH_LONG).show();
                        }

                        dialog.dismiss();
                        break;
                    case R.id.btnCancel:
                        dialog.dismiss();
                        break;


                }

            }
        };

        btnTamam.setOnClickListener(mClickListener);
        btnCancel.setOnClickListener(mClickListener);


        layout.setBackgroundResource(R.drawable.round_dialog);


        dialog.setContentView(view);
        dialog.show();

    }

    private void showKaloriTableDialog() {
        final Dialog dialog = new Dialog(context, R.style.Dialog_No_Border);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.kalori_table, null);
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.layout_root);
        Button btnTamam = (Button) view.findViewById(R.id.btnTamam);

        layout.setBackgroundResource(R.drawable.round_dialog);

        View.OnClickListener mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        };

        btnTamam.setOnClickListener(mClickListener);
        dialog.setContentView(view);
        dialog.show();

    }

    private void showKaloriCetveliDialog() {
        final Dialog dialog = new Dialog(context, R.style.Dialog_No_Border);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.kalori_cetveli, null);
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.layout_root);
        Button btnTamam = (Button) view.findViewById(R.id.btnTamam);

        layout.setBackgroundResource(R.drawable.round_dialog);

        View.OnClickListener mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        };
        btnTamam.setOnClickListener(mClickListener);
        dialog.setContentView(view);
        dialog.show();

    }

    private void showVKEDialog() {
        final Dialog dialog = new Dialog(context, R.style.Dialog_No_Border);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.vke, null);
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.layout_root);

        Button btnHesapla = (Button) view.findViewById(R.id.btnHesapla);
        Button btnIptal = (Button) view.findViewById(R.id.btnCancel);
        final EditText etBoy = (EditText) view.findViewById(R.id.etBoy);
        final EditText etKg = (EditText) view.findViewById(R.id.etKg);
        final TextView tvSonuc = (TextView) view.findViewById(R.id.tvSonuc);
        tvAciklama = (TextView) view.findViewById(R.id.tvAciklama);

        layout.setBackgroundResource(R.drawable.round_dialog);

        View.OnClickListener mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnHesapla:
                        if (etBoy.getText().length() == 0 || etKg.getText().length() == 0)
                            Toast.makeText(context, "Boy ve kilo değerlerini girmediniz..", Toast.LENGTH_LONG).show();
                        else {
                            float boy = Float.parseFloat(etBoy.getText().toString()) / 100f;
                            float kg = Float.parseFloat(etKg.getText().toString());
                            float vke = computeVKE(boy, kg);
                            tvSonuc.setText(Float.toString(vke));
                            tvAciklama.setText(vkeDescription(vke, view));
                        }

                        break;
                    case R.id.btnCancel:
                        dialog.dismiss();
                        break;
                }
            }
        };

        btnHesapla.setOnClickListener(mClickListener);
        btnIptal.setOnClickListener(mClickListener);
        dialog.setContentView(view);
        dialog.show();

    }

    protected String vkeDescription(float vke, View v) {
        String str = null;
        if (vke > 0 && vke < 18.4) {
            str = "Boyunuza uygun ağırlığa erişmeniz için yeterli ve dengeli beslenmeli, beslenme alışkanlıklarınızı geliştirmeye özen göstermelisiniz.";
            tvAciklama.setTextColor(Color.parseColor("#30B50B"));
        } else if (vke > 18.5 && vke < 24.9) {
            str = "Yeterli ve dengeli beslenerek ve düzenli fiziksel aktivite yaparak bu ağırlığınızı korumaya özen gösteriniz.";
            tvAciklama.setTextColor(Color.parseColor("#30B50B"));
        } else if (vke > 25.0 && vke < 29.9) {
            str = "Fazla kilolu olma durumu gerekli önlemler alınmadığı takdirde pek çok hastalık için risk faktörü olan obeziteye (şişmanlık) yol açar.";
            tvAciklama.setTextColor(Color.parseColor("#30B50B"));
        } else if (vke > 30.0 && vke < 34.9) {
            str = "Şişmanlık, kalp-damar hastalıkları, diyabet, hipertansiyon v.b. kronik hastalıklar için risk faktörüdür. Bir sağlık kuruluşuna başvurarak hekim / diyetisyen kontrolünde zayıflayarak normal ağırlığa inmeniz sağlığınız açısından çok önemlidir. Lütfen, sağlık kuruluşuna başvurunuz.";
            tvAciklama.setTextColor(Color.parseColor("#FF0000"));
        } else if (vke > 35.0 && vke < 44.9) {
            str = "Şişmanlık, kalp-damar hastalıkları, diyabet, hipertansiyon v.b. kronik hastalıklar için risk faktörüdür. Bir sağlık kuruluşuna başvurarak hekim / diyetisyen kontrolünde zayıflayarak normal ağırlığa inmeniz sağlığınız açısından çok önemlidir. Lütfen, sağlık kuruluşuna başvurunuz.";
            tvAciklama.setTextColor(Color.parseColor("#FF0000"));
        } else {
            str = "Şişmanlık, kalp-damar hastalıkları, diyabet, hipertansiyon v.b. kronik hastalıklar için risk faktörüdür. Bir sağlık kuruluşuna başvurarak hekim / diyetisyen kontrolünde zayıflayarak normal ağırlığa inmeniz sağlığınız açısından çok önemlidir. Lütfen, sağlık kuruluşuna başvurunuz.";
            tvAciklama.setTextColor(Color.parseColor("#FF0000"));
        }
        return str;

    }

    protected float computeVKE(float boy, float kg) {
        return kg / (boy * boy);

    }

    private void finishKalori(final String kalori) {
        final Dialog dialog = new Dialog(context, R.style.Dialog_No_Border);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.finishkalori, null);
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.layout_root);
        Button btnTamam = (Button) view.findViewById(R.id.btnTamam);
        Button btnPaylas = (Button) view.findViewById(R.id.btnPaylas);
        TextView hedef = (TextView) view.findViewById(R.id.tvHedef);

        hedef.setText("Tebrikler!" + " tam " + kalori + " kalori yaktınız.\n\n->Her bir faaliyetin kaç kalori olduğunu öğrenmek için 'kalori tablosu'na\n->Her bir yiyeceğin kaç kalori olduğunu öğrenmek için 'kalori cetveli' ne bakabilirsiniz.Sağlıklı yürüyüşler.");

        layout.setBackgroundResource(R.drawable.round_dialog);

        View.OnClickListener mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnTamam:
                        resetTvProgress();
                        saveSayacDegeriGirildimi(StaticValues.LOAD_SAYACDEGERIGIRILDIMI, StaticValues.DEFAULT_BOOL);
                        dialog.dismiss();
                        break;
                    case R.id.btnPaylas:
                        paylas(String.valueOf(kalori));
                        resetTvProgress();
                        saveSayacDegeriGirildimi(StaticValues.LOAD_SAYACDEGERIGIRILDIMI, StaticValues.DEFAULT_BOOL);
                        dialog.dismiss();
                        break;
                }
            }
        };

        btnTamam.setOnClickListener(mClickListener);
        btnPaylas.setOnClickListener(mClickListener);
        dialog.setContentView(view);
        dialog.show();

    }

    private int maxAdimHesapla(String value) {
        float max = Float.parseFloat(value) / StaticValues.adimKalori;
        return (int) Math.round(max);
    }

    private void paylas(String kalori) {

        String formattedString = String.format("Bugün tam %s kalori harcadım.\n%s", kalori, StaticValues.appLink);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");

        share.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
        share.putExtra(Intent.EXTRA_TEXT, formattedString);

        startActivity(Intent.createChooser(share, "Paylaşım Seçenekleri"));
    }

}
