package com.example.shortinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView confirmedText;
    private TextView confirmedVarText;
    private TextView confirmedDetailText;
    private TextView releaseText;
    private TextView deadText;
    private TextView deadVarText;
    private TextView stdDateText;
    private TextView vaccineFirstText;
    private TextView distancingText;
    private TextView vaccineSecondText;
    private TextView vaccineThirdText;
    private TextView worldStdTime;
    private TextView currentTime;
    private TextView currentDate;
    private TextView currentWeeks;
    private TextView currentLocation;
    private TextView worldConfirmedText;
    private TextView worldConfirmedVarText;
    private TextView temperatureText;
    private TextView PM10Text;
    private TextView PM2_5Text;
    private TextView weatherLocation;
    private TextView currentWeatherStatus;
    private TextView ultravioletText;
    private TextView compareYesterday;
    private TextView issueKeywordStdTime;
    private TextView rainPercentText;
    private TextView humidityPercentText;
    private TextView windStateText;
    private TextView windStateValueText;
    private TextView sunsetValueText;
    private ImageView weatherImage;
    private Bundle bundle;
    private ImageButton currentLocationWeather;
    private Button layoutRefreshButton;
    private ProgressBar progressBar;
    private ProgressBar networkProgressBar;
    private GpsTracker gpsTracker;
    private ListView keywordListView;

    private LinearLayout backgroundScreen;
    private LinearLayout foregroundScreen;
    private String vaccineFirst;
    private String vaccineSecond;
    private String vaccineThird;
    private String address;
    private String inputAddress;
    private String area1;
    private String area2;
    private String area3;
    private String detailName;
    private String detailNumber;
    private String building;
    private String groundNumber;
    private ArrayList<String> distanceList;
    private ArrayAdapter<String> adapter;
    private double latitude;
    private double longitude;
    private boolean useCurrentAddress = false;

    public static final int DEFAULT_REGION_NUMBER = 8; //?????????
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    public static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final String[] WEEKS = {"?????????", "?????????", "?????????", "?????????", "?????????", "?????????", "?????????"};
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_main);
        initializeObjects(); // ???????????? ????????? ??????

        int status = getNetworkConnectState();
        if (status == 1) { // ????????????
        } else if (status == 2) { // ?????????
        } else { // ????????????
            foregroundScreen.setVisibility(View.GONE);
            backgroundScreen.setVisibility(View.VISIBLE);
        }

        layoutRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backgroundScreen.getVisibility() == View.VISIBLE) {
                    layoutRefreshButton.setBackgroundResource(R.drawable.ic_baseline_refresh_24);
                    int status = getNetworkConnectState();
                    layoutRefreshButton.setVisibility(View.GONE);
                    networkProgressBar.setVisibility(View.VISIBLE);
                    if (status == 1 || status == 2) {

                        layoutRefreshButton.setVisibility(View.GONE);
                        networkProgressBar.setVisibility(View.VISIBLE);

                        backgroundScreen.setVisibility(View.GONE);
                        foregroundScreen.setVisibility(View.VISIBLE);
                        getInitialLocation(); // ?????? ??????, ???????????? ?????? ???????????? ???????????? +?????? ?????? ????????????
                        getRegionDistanceInfo(); // ????????? ???????????? ??????
                        getTodayOccurrence(); // ?????? ?????? ??????(???????????? ??? ???????????? ??????)
                        executeTimeClock(); // ?????? ??????
                        getVaccineInfo(); // ?????? ?????? ?????? ??????
                        getCoronaInfoInNaver(); // ????????? ?????? ?????? in ?????????
                        getCoronaInfoInOfficial(); // ????????? ?????? ?????? in ??????
                        getLiveIssuesKeywords(); // ????????? ?????? ????????? ??????
                    } else {
                        layoutRefreshButton.setVisibility(View.VISIBLE);
                        networkProgressBar.setVisibility(View.GONE);
                        layoutRefreshButton.setBackgroundResource(R.drawable.ic_baseline_close_24);
                    }
                }
            }
        });

        getSupportActionBar().setTitle("Short Information");
        final Intent intent = new Intent(getApplicationContext(), ScreenService.class);
        startService(intent);

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        currentLocationWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useCurrentAddress) {
                    useCurrentAddress = false;
                    currentLocationWeather.setBackgroundColor(Color.parseColor("#ffffff"));
                } else {
                    useCurrentAddress = true;
                    currentLocationWeather.setBackgroundColor(Color.parseColor("#46BEFF"));
                    getCurrentLocationWeather();
                }
                editor.putBoolean("isCurrent", useCurrentAddress);
                editor.apply();
            }
        });

        getInitialLocation(); // ?????? ??????, ???????????? ?????? ???????????? ???????????? +?????? ?????? ????????????
        getRegionDistanceInfo(); // ????????? ???????????? ??????
        getTodayOccurrence(); // ?????? ?????? ??????(???????????? ??? ???????????? ??????)
        executeTimeClock(); // ?????? ??????
        getVaccineInfo(); // ?????? ?????? ?????? ??????
        getCoronaInfoInNaver(); // ????????? ?????? ?????? in ?????????
        getCoronaInfoInOfficial(); // ????????? ?????? ?????? in ??????
        getLiveIssuesKeywords(); // ????????? ?????? ????????? ????????????
    }
    /* ------------------------------------onCreate-------------------------------------------- */

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Toast.makeText(getApplicationContext(), "?????? ????????? ?????? ????????????...", Toast.LENGTH_SHORT).show();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            getAddressUsingNaverAPI();
        }
    };

    /**
     * ?????? ???????????? ?????? ????????? ???????????? ?????????
     *
     * @return if (????????????) 1, (????????? ?????????) 2, (?????? ??????) 3
     * update on 2022-02-29
     */
    public int getNetworkConnectState() {
        int TYPE_WIFI = 1;
        int TYPE_MOBILE = 2;
        int TYPE_NOT_CONNECTED = 3;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_MOBILE) {
                return TYPE_MOBILE;
            } else if (type == ConnectivityManager.TYPE_WIFI) {
                return TYPE_WIFI;
            }
        }
        return TYPE_NOT_CONNECTED;
    }

    /**
     * [Thread part]
     * ???????????? ????????????????????? ???????????? URL ??? ???????????? ?????? JSON ??? ???????????? ListView ??? ??????????????? ?????? ?????????
     * - HTTP connection
     * update on 2022-01-29
     */
    public void getLiveIssuesKeywords() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection;
                try {
                    URL url = new URL("https://news.nate.com/today/keywordList");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

                        String line;
                        String page = "";
                        while ((line = reader.readLine()) != null) {
                            page += line;
                        }
                        JSONObject jsonObject = new JSONObject(page);
                        String stdTime = jsonObject.getString("service_dtm");
                        JSONObject data = jsonObject.getJSONObject("data");
                        ArrayList<String> keywordList = new ArrayList<>();
                        for (int i = 0; i <= 9; ++i) {
                            keywordList.add((i + 1) + ". " + data.getJSONObject(i + "").optString("keyword_service").replace("<br />", " "));
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                issueKeywordStdTime.setText("????????? ?????? " + stdTime);
                                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, keywordList);
                                keywordListView.setAdapter(adapter);
                                setListViewHeightBasedOnChildren(keywordListView);
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * ?????? ???????????? ?????? ListView??? ?????? ????????? ?????? ???
     * ListView ?????? ????????? ?????? ?????? Item ??? wrap content ?????? ListView ??? ????????? ????????? ??????
     *
     * @param listView - ????????? ??? ?????? ListView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            //listItem.measure(0, 0);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight;
        listView.setLayoutParams(params);

        listView.requestLayout();
    }


    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            try {
                confirmedText.setText("????????? ???  " + (msg.getData().getString("confirmed") == null ? "" : msg.getData().getString("confirmed")));
                confirmedVarText.setText("??? " + (msg.getData().getString("confirmed_var") == null ? "" : msg.getData().getString("confirmed_var")));
                releaseText.setText("?????? ?????? ???  " + (msg.getData().getString("release") == null ? "" : msg.getData().getString("release")));
                deadText.setText("????????? ???  " + (msg.getData().getString("dead") == null ? "" : msg.getData().getString("dead")));
                deadVarText.setText("??? " + (msg.getData().getString("dead_var") == null ? "" : msg.getData().getString("dead_var")));
                stdDateText.setText("??? ?????? ?????? ?????? " + (msg.getData().getString("today_std_time") == null ? "" : msg.getData().getString("today_std_time")));
                vaccineFirstText.setText(msg.getData().getString("domestic_vaccine_first") == null ? "" : msg.getData().getString("domestic_vaccine_first"));
                vaccineSecondText.setText(msg.getData().getString("domestic_vaccine_second") == null ? "" : msg.getData().getString("domestic_vaccine_second"));
                vaccineThirdText.setText(msg.getData().getString("domestic_vaccine_third") == null ? "" : msg.getData().getString("domestic_vaccine_third"));
                worldConfirmedText.setText(" ??? " + (msg.getData().getString("world") == null ? "" : msg.getData().getString("world")));
                worldConfirmedVarText.setText(" ??? " + (msg.getData().getString("world_var") == null ? "" : msg.getData().getString("world_var")));
                worldStdTime.setText("??? " + (msg.getData().getString("world_std_time") == null ? "" : msg.getData().getString("world_std_time")));
                DecimalFormat formatter = new DecimalFormat("###,###");
                confirmedDetailText.setText("(?????? ??????: " + (formatter.format(msg.getData().getInt("today_domestic") - msg.getData().getInt("today_abroad")))
                        + " , ?????? ??????: " + (formatter.format(msg.getData().getInt("today_abroad"))) + ")");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    /**
     * View ????????? ??????????????? ????????? ?????? ?????????????????? ????????????????????? ?????????
     * onCreate() ????????? ??????
     * update on 2022-01-29
     */
    private void initializeObjects() {
        distanceList = new ArrayList<>();
        sharedPreferences = getSharedPreferences("useCurLoc", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        bundle = new Bundle();
        gpsTracker = new GpsTracker(this);
        backgroundScreen = findViewById(R.id.background_screen);
        foregroundScreen = findViewById(R.id.foreground_screen);
        networkProgressBar = findViewById(R.id.network_progressbar);
        layoutRefreshButton = findViewById(R.id.layout_refresh);
        progressBar = findViewById(R.id.progressBar);
        currentLocation = findViewById(R.id.cur_location);
        confirmedText = findViewById(R.id.corona_text_confirmed);
        confirmedVarText = findViewById(R.id.corona_text_confirmed_var);
        confirmedDetailText = findViewById(R.id.corona_text_confirmed_detail);
        releaseText = findViewById(R.id.corona_text_release);
        deadText = findViewById(R.id.corona_text_dead);
        deadVarText = findViewById(R.id.corona_text_dead_var);
        stdDateText = findViewById(R.id.corona_std_date);
        vaccineFirstText = findViewById(R.id.corona_text_vaccine_first);
        vaccineSecondText = findViewById(R.id.corona_text_vaccine_second);
        vaccineThirdText = findViewById(R.id.corona_text_vaccine_third);
        distancingText = findViewById(R.id.corona_text_distancing);
        worldConfirmedText = findViewById(R.id.corona_text_world);
        worldConfirmedVarText = findViewById(R.id.corona_text_world_var);
        worldStdTime = findViewById(R.id.corona_text_world_std_time);
        currentTime = findViewById(R.id.cur_time);
        currentDate = findViewById(R.id.cur_date);
        currentWeeks = findViewById(R.id.weeks);
        temperatureText = findViewById(R.id.temperature);
        PM10Text = findViewById(R.id.PM10_text);
        PM2_5Text = findViewById(R.id.PM2_5_text);
        weatherLocation = findViewById(R.id.location);
        currentLocationWeather = findViewById(R.id.curloc_wt_button);
        currentWeatherStatus = findViewById(R.id.status);
        ultravioletText = findViewById(R.id.ultraviolet_text);
        compareYesterday = findViewById(R.id.cmp_yesterday);
        weatherImage = findViewById(R.id.weather_image);
        rainPercentText = findViewById(R.id.rain_percent_value);
        humidityPercentText = findViewById(R.id.humidity_percent_value);
        windStateText = findViewById(R.id.wind_state);
        windStateValueText = findViewById(R.id.wind_state_value);
        sunsetValueText = findViewById(R.id.sunset_value);
        issueKeywordStdTime = findViewById(R.id.issue_std_time);
        keywordListView = findViewById(R.id.keyword_list);
    }

    /**
     * ??? ?????? ?????? ?????? ???????????? ????????? ????????? ?????? ????????? ???????????? ????????? ?????? middle bridge ?????????
     * update on 2022-01-29
     */
    public void getCurrentLocationWeather() {
        if (address != null) {
            String[] divide = address.split(" ");
            if (area3 != null) {
                inputAddress = divide[1] + " " + divide[2] + " " + area3;
            } else {
                inputAddress = divide[1] + " " + divide[2];
            }
            weatherLocation.setText(inputAddress);
            getWeatherOfLocation();
        } else {
            Log.d("nullll", "dwdw");
        }
    }

    /**
     * ?????? ????????? ???????????? ?????? setting ??? ????????? ????????? ????????? ?????? ????????? ????????? ????????? ?????? ?????????
     * update on 2022-01-29
     */
    public void getInitialLocation() {
        if (gpsTracker == null) {
            gpsTracker = new GpsTracker(this);
        }
        gpsTracker.getLocation(locationListener);
        new Thread(new Runnable() {
            @Override
            public void run() {
                latitude = gpsTracker.getLatitude(); //??????
                longitude = gpsTracker.getLongitude(); // ??????
                Log.d("lon, lati", longitude + " " + latitude + " ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getAddressUsingNaverAPI();
                    }
                });
            }
        }).start();
    }

    /**
     * [Thread part]
     * ????????? ?????? ?????????????????? ?????? ?????? ??????????????? ?????? ????????? ??? ?????? ????????? ???????????? ?????? crawling ????????? ??????
     * ????????? ?????? ???????????? Handler ??? ????????? view ??????????????? ???
     * update on 2022-01-29
     */
    public void getTodayOccurrence() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document document = null;
                try {
                    document = Jsoup.connect("http://ncov.mohw.go.kr/bdBoardList_Real.do?brdId=1&brdGubun=11&ncvContSeq=&contSeq=&board_id=&gubun=").get();
                    Element element = document.select("div.data_table.mgt16").select("tr.sumline td").first();
                    int abroad = Integer.parseInt(element.text().replaceAll(",", "").trim());
                    bundle.putInt("today_abroad", abroad);
                    Elements elements = document.select("div.caseTable ul.ca_body").select("dd.ca_value");
                    int i = 0;
                    for (Element e : elements) {
                        if (i == 6) {
                            int domestic = Integer.parseInt(e.text().replaceAll(",", "").trim());
                            bundle.putInt("today_domestic", domestic);
                            break;
                        }
                        i++;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * [Thread part]
     * ????????? ?????? ???????????? ?????? ???????????? ?????? {?????? ????????? ??????, ?????? ??????} ?????? crawling ????????? ??????
     * ?????? ???????????? Handler ??? ???????????? view ??????????????? ???
     * update on 2022-01-29
     */
    private void getCoronaInfoInOfficial() {
        new Thread() {
            @Override
            public void run() {
                Document officialDoc = null;
                try {
                    officialDoc = Jsoup.connect("http://ncov.mohw.go.kr/").get();

                    Elements officalElement = officialDoc.select("div.live_left").select("div.occurrenceStatus div.occur_graph tbody");
                    String[] todayCovidSplit = officalElement.text().trim().split(" ");
                    /**
                     * todayCovidSplit array all elements by each index
                     * [0] : "??????"
                     * [1] : ??????
                     * [2] : ?????? ?????????
                     * [3] : ?????? ??????
                     * [4] : ??????
                     * [5] : "??????"
                     * [6] : "7??????"
                     * [7] : "?????????"
                     * [8] : ??????
                     * [9] : ?????? ?????????
                     * [10] : ?????? ??????
                     * [11] : ??????
                     */

                    officalElement = officialDoc.select("div.live_left").select("div.occurrenceStatus div.occur_num");
                    String[] cumulativeCovidSplit = officalElement.text().replaceAll("\\(??????\\)", "").replaceAll("????????????", "").split(" ");
                    /**
                     * cumulativeCovidSplit array all elements by each index
                     * [0] : (??????)?????? n
                     * [1] : (??????)?????? n????????????
                     * ??? removed string (??????), ????????????
                     */
                    bundle.putString("confirmed", cumulativeCovidSplit[1].replace("??????", "") + "???");
                    bundle.putString("confirmed_var", todayCovidSplit[4]);
                    bundle.putString("release", todayCovidSplit[3] + "???");
                    bundle.putString("dead", cumulativeCovidSplit[0].replace("??????", "") + "???");
                    bundle.putString("dead_var", todayCovidSplit[1]);

                    officalElement = officialDoc.select("div.live_left").select("div.occurrenceStatus h2.title1 span.livedate");
                    bundle.putString("today_std_time", officalElement.text().split(",")[0] + ")");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    /**
     * [Thread part]
     * ??????????????? "?????????"??? ???????????? ??? ????????? ??????????????? {?????? ????????? ?????????, ?????? ????????? ?????????, ?????? ?????? ?????? ??????} ?????? crawling ????????? ??????
     * ?????? ???????????? Handler ??? ???????????? view ??? ?????? ??????????????? ???
     * update on 2022-01-29
     */
    private void getCoronaInfoInNaver() {
        new Thread() {
            @Override
            public void run() {
                Document doc = null;
                try {
                    doc = Jsoup.connect("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%BD%94%EB%A1%9C%EB%82%98").get();

                    Element contents = doc.select("div.status_info.abroad_info li.info_01").select(".info_num").first();
                    bundle.putString("world", contents.text() == null ? "????????? ??????" : contents.text() + "???");

                    contents = doc.select("div.status_info.abroad_info li.info_01").select("em.info_variation").first();
                    bundle.putString("world_var", contents.text() == null ? "????????? ??????" : contents.text());

                    Elements elements = doc.select("div.patients_info div.csp_infoCheck_area._togglor_root a.info_text._trigger");
                    if (elements.text() == null) {
                        bundle.putString("world_std_time", "????????? ??????");
                    } else {
                        int id = elements.text().indexOf("????????????");
                        String s1 = elements.text().substring(id);
                        int id2 = s1.substring(4).indexOf("????????????");
                        String sub = s1.substring(4 + id2);
                        bundle.putString("world_std_time", sub);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    /**
     * [Thread part]
     * ????????? ?????? ???????????? ?????? ???????????? ?????? ?????? ?????? ??????{1???, 2???, 3??? ??????, ??????, ??????} ?????? crawling ????????? ??????
     * ?????? ???????????? Handler ??? ???????????? view ??? ?????? ??????????????? ???
     * update on 2022-01-29
     */
    public void getVaccineInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document vaccineUrl = null;
                try {
                    // ????????? ??????
                    vaccineUrl = Jsoup.connect("http://ncov.mohw.go.kr/").get();
                    Elements elements;
                    elements = vaccineUrl.select("div.liveboard_layout").select("div.vaccine_list");
                    /** HTML of vaccine information is changed (updated on 2022-01-27)
                     *  ?????? ??? : elements?????? ????????? ???????????? ?????????
                     *  ??? 1??? ??????, 2??? ?????? {??????, ?????? ?????????, ??? ?????????}
                     *
                     * ?????? ??? : elements ??????????????? ?????? ???????????? ??? ????????? ??????
                     *  ??? ?????? 1??? ??????, ?????? 2??? ??????, ?????? 3??? ?????? {??????, ?????? ?????????}
                     *  {"??????"}??? ???????????? split ???
                     *  split ?????? : Length = 4 (0?????? ???????????? ?????? empty string ?????? ??????)
                     *
                     *  ??? 2??? ?????? ??? : ????????? ??? ????????? ?????? ??????????????? ??????
                     */
                    if (elements.text() == null) {
                        bundle.putString("domestic_vaccine_first", "????????? ??????");
                        bundle.putString("domestic_vaccine_second", "????????? ??????");
                        bundle.putString("domestic_vaccine_third", "????????? ??????");
                        return;
                    }

                    String tmp = elements.text().trim();
                    String[] split = tmp.split(" ");
                    /**
                     * ??? split array all elements by each index
                     * [0] : "1?????????"
                     * [1] : 1??? ?????? ??????
                     * [2] : 1??? ?????? ??????
                     * [3] : 1??? ?????? ??????
                     * [4] : "2?????????"
                     * [5] : 2??? ?????? ??????
                     * [6] : 2??? ?????? ??????
                     * [7] : 2??? ?????? ??????
                     * [8] : "3?????????"
                     * [9] : 3??? ?????? ??????
                     * [10] : 3??? ?????? ??????
                     * [11] : 3??? ?????? ??????
                     * [12] : 3??? ?????? 60??? ?????? ??????
                     */
                    vaccineFirst = "?????? " + processVaccineFirst(Arrays.copyOfRange(split, 0, 4)); // split ??? 0 ~ 3 ????????? ?????? ??????
                    vaccineSecond = "?????? " + processVaccineFirst(Arrays.copyOfRange(split, 4, 8)); // split ??? 4 ~ 7 ????????? ?????? ??????
                    vaccineThird = "?????? " + processVaccineFirst(Arrays.copyOfRange(split, 8, 12)); // split ??? 8 ~ 11 ????????? ?????? ??????

                    bundle.putString("domestic_vaccine_first", vaccineFirst);
                    bundle.putString("domestic_vaccine_second", vaccineSecond);
                    bundle.putString("domestic_vaccine_third", vaccineThird);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * [Thread part]
     * ????????? ???????????? ???????????? ???
     * AM | PM / ???, ???, ???, ?????? / ???:???:???
     * update on 2022-01-29
     */
    public void executeTimeClock() {
        new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Calendar calendar = Calendar.getInstance();
                            boolean isPM = false;
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH); // 1??? : 0
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            int week = calendar.get(Calendar.DAY_OF_WEEK);
                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            int second = calendar.get(Calendar.SECOND);
                            if (hour >= 12) {
                                isPM = true;
                            } else {
                                isPM = false;
                            }
                            currentDate.setText(year + "??? " + (month + 1) + "??? " + day + "???");
                            String colors;
                            if (WEEKS[week - 1].equals("?????????")) {
                                colors = "#FF0000";
                            } else if (WEEKS[week - 1].equals("?????????")) {
                                colors = "#3CA0E1";
                            } else {
                                colors = "#000000";
                            }
                            currentWeeks.setTextColor(Color.parseColor(colors));
                            currentWeeks.setText(WEEKS[week - 1]);
                            currentTime.setText((isPM ? "?????? " + (hour == 12 ? hour : (hour - 12)) : " ?????? " + (hour == 0 ? 12 : hour)) + ":"
                                    + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second));
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * [Thread part]
     * ????????? ?????? ?????????????????? js ????????? ?????? "RSS_DATA"(???????????? ????????? ??????)??? crawling ??? ??? ???,
     * RSS_DATA ??? ?????? ????????? ????????? ??? ???????????? ??? ????????? ?????? parsing ?????? ????????? ???
     * ????????? ??? ????????? ArrayList ??? ????????? view ??? ????????????.
     * update on 2022-01-29
     */
    public void getRegionDistanceInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document coronaUrl = null;
                try {
                    coronaUrl = Jsoup.connect("http://ncov.mohw.go.kr/regSocdisBoardView.do?brdId=6&brdGubun=68&ncvContSeq=495").get();

                    Elements scripts = coronaUrl.getElementsByTag("script");
                    if (scripts.text() == null) {
                        distanceList.add("????????? ??????");
                        return;
                    }
                    for (Element e : scripts) {
                        if (e.data().contains("RSS_DATA")) {
                            int idx_begin = e.data().indexOf("RSS_DATA");
                            String front = e.data().substring(idx_begin);

                            int idx_end = front.indexOf(";");

                            String cutStr = front.substring(0, idx_end + 1);
                            cutStr = cutStr.replaceAll(" ", "");
                            cutStr = cutStr.replace("\n", "");

                            String[] temp = cutStr.split("\\{");

                            // index 0?????? "RSS_DATA = [" ?????? ????????? index 1?????????
                            for (int i = 1; i < temp.length; ++i) {
                                int be = temp[i].indexOf("-"); // -??????
                                int end = temp[i].substring(be).indexOf("}"); // }?????? ???????????? ?????? index ?????????

                                String endStr = temp[i].substring(be, be + end);
                                endStr = endStr.replace("'", ""); // ' ??????
                                endStr = endStr.replaceAll("<br/>", ""); // <br/> ??????

                                String first = endStr.substring(0, 3);
                                String second = endStr.substring(3);
                                String complete = first + " " + second;
                                distanceList.add(complete);
                            }
                            break;
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            distancingText.setText("??? ???????????? " + distanceList.get(DEFAULT_REGION_NUMBER));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * [Thread part]
     * ????????? API ??? ???????????? ????????? ?????? ?????? ?????? ??? ?????? ?????? ???????????? parameter ?????? ????????? ????????? ??????.
     * ????????? ?????? ??? ?????? ????????? ?????? ?????? ?????? JSON ????????? ?????????.
     * ??? JSON ???????????? ???????????? ????????? address ??? view ??? ????????????.
     * ??? ??? ?????? address ??? ?????? ?????? ?????? ???????????? ???????????? ????????????.
     * update on 2022-01-29
     */
    private void getAddressUsingNaverAPI() {
        new Thread() {
            @Override
            public void run() {
                HttpURLConnection urlConnection;
                try {
                    Log.d("longitude & latitude", longitude + ", " + latitude);
                    URL url = new URL("https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=" + longitude + "," + latitude + "&orders=legalcode,admcode,addr,roadaddr&output=json");

                    //?????? ?????? ?????? ????????? ??? ?????????, orders ??????????????? ????????? ???????????????.
                    //???) orders=legalcode
                    //orders=addr,admcode
                    //orders=addr,admcode,roadaddr
                    //orders=legalcode,addr,admcode,roadaddr
                    // legalcode : ?????????
                    // admcode : ?????????
                    // addr : ?????? ??????
                    // roadaddr: ????????? ??????

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    urlConnection.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "78obj02dm7");
                    urlConnection.setRequestProperty("X-NCP-APIGW-API-KEY", "26ZH2x2dbDxREayPjwWOziWD1ZcJOMp0aRmUiW8K");

                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

                        String line;
                        StringBuilder page = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            page.append(line); // ?????? ????????? ?????? json String
                        }

                        JSONObject json = new JSONObject(page.toString()); // convert string to json
                        int statusCode = json.optJSONObject("status").optInt("code");
                        if (statusCode == 0) {
                            int orderJsonArrLength = json.optJSONArray("results").length();
                            Log.d("Length of order Json", orderJsonArrLength + " ");
                            switch (orderJsonArrLength) {
                                case 0: // result ??????
                                    break;
                                case 1: // legalcode
                                    JSONObject legal = json.optJSONArray("results").getJSONObject(0).getJSONObject("region");
                                    area1 = legal.optJSONObject("area1").optString("name");
                                    area2 = legal.optJSONObject("area2").optString("name");
                                    area3 = legal.optJSONObject("area3").optString("name");
                                    address = area1 + " " + area2 + " " + area3;
                                    break;
                                case 2: // legalcode, admcode
                                    JSONObject admcode = json.optJSONArray("results").getJSONObject(1).getJSONObject("region");
                                    area1 = admcode.getJSONObject("area1").optString("name");
                                    area2 = admcode.getJSONObject("area2").optString("name");
                                    area3 = admcode.getJSONObject("area3").optString("name");
                                    address = area1 + " " + area2 + " " + area3;
                                    break;
                                case 3: // legalcode, admcode, addr
                                    JSONObject addr = json.optJSONArray("results").getJSONObject(2).getJSONObject("region");
                                    area1 = addr.getJSONObject("area1").optString("name");
                                    area2 = addr.getJSONObject("area2").optString("name");
                                    area3 = addr.getJSONObject("area3").optString("name");
                                    groundNumber = json.optJSONArray("results").getJSONObject(2).getJSONObject("land").optString("number1");
                                    address = area1 + " " + area2 + " " + area3 + " " + groundNumber;
                                    break;
                                case 4: // legalcode, admcode, addr, roadaddr
                                    JSONObject roadaddr = json.optJSONArray("results").getJSONObject(3).getJSONObject("region");
                                    area1 = roadaddr.getJSONObject("area1").optString("name");
                                    area2 = roadaddr.getJSONObject("area2").optString("name");
                                    area3 = roadaddr.getJSONObject("area3").optString("name");
                                    detailName = json.optJSONArray("results").getJSONObject(3).getJSONObject("land").optString("name");
                                    detailNumber = json.optJSONArray("results").getJSONObject(3).getJSONObject("land").optString("number1");
                                    building = json.optJSONArray("results").getJSONObject(3).getJSONObject("land").getJSONObject("addition0").optString("value");
                                    address = area1 + " " + area2 + " " + detailName + " " + detailNumber + " (" + area3 + (building.equals("") ? building : ", " + building) + ")";
                                    break;
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (sharedPreferences.getBoolean("isCurrent", false)) {
                                        useCurrentAddress = sharedPreferences.getBoolean("isCurrent", false);
                                        currentLocationWeather.setBackgroundColor(Color.parseColor("#46BEFF"));
                                        getCurrentLocationWeather();
                                    } else {
                                        compareYesterday.setText("????????? ?????? ??? ????????? ????????????.");
                                    }
                                    currentLocation.setText(address);
                                    currentLocation.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                            //
                            // ??? : results[0] -> region -> area1 -> name
                            // ??? & ??? : results[0] -> region -> area2 -> name
                            // ??? : results[0] -> region -> area3 -> name
                            // ?????? ?????? ????????? : results[0] -> land /-> number1 : ????????????(??????)
                            // name : ?????? ??????(????????? ??????)
                            // addition0 -> value : ??????
                            // addition1 -> value : ????????????
                            // addition2 -> value : ????????????
                            // addition3 -> value : ???
                            // addition4 -> value : ???
                            // ??? ??? ??? ????????? ???????????? (??? ??????)
                            //Reference : https://api.ncloud-docs.com/docs/ai-naver-mapsreversegeocoding-gc

                            // 1??? : ???, ???
                            // 2??? : ???, ???, ???
                            // 3??? : ???, ???, ???, ???, ???
                        }
                    } else {
                        currentLocation.setText("http error");
                    }
                } catch (MalformedURLException e) {
                    currentLocation.setText("Malformed");
                    e.printStackTrace();
                } catch (IOException e) {
                    currentLocation.setText("???????????? ????????? ???????????? ????????????.");
                    e.printStackTrace();
                } catch (JSONException e) {
                    currentLocation.setText("?????? ???????????? ????????? ?????? ?????? ????????????.");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * [Button Click event] for Refresh Button of current location
     *
     * @param view
     */
    public void onGetAddress(View view) {
        if (gpsTracker == null) {
            gpsTracker = new GpsTracker(this);
        }
        if (gpsTracker.getLocation(locationListener) != null) {
            currentLocation.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    latitude = gpsTracker.getLatitude(); // ??????
                    longitude = gpsTracker.getLongitude(); // ??????
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getAddressUsingNaverAPI();
                        }
                    });
                }
            }).start();
        } else {
            currentLocation.setText("?????? ???????????? ????????? ??? ????????????. ????????? ????????????.");
        }
    }

    /**
     * ????????? ????????? ?????? ?????? ?????? ????????? ???????????? ?????? String array ??? ????????? ??? ??????, ??? ???????????? ???????????? ????????? ????????? ?????? ?????????
     *
     * @param vaccineSplit
     * @return vaccineSplit ?????? ?????? ??? ???????????? ????????? ?????? ??????
     */
    private String processVaccineFirst(String[] vaccineSplit) {
        /**
         * updated on 2022-01-27
         * 1. changed entire logic according to change of vaccine information
         * 2. added defensive part
         * 3. changed parameter name
         */
        if (vaccineSplit == null || vaccineSplit.length == 0) {
            return "None";
        }
        String title = vaccineSplit[0];
        String percent = vaccineSplit[1];
        String cumulative = vaccineSplit[2].substring(0, 2) + " " + vaccineSplit[2].substring(2) + "???";
        String newer = vaccineSplit[3].substring(0, 2) + " " + vaccineSplit[3].substring(2) + "???";
        return title + " " + percent + "\n" + newer + "  /  " + cumulative;
    }

    /**
     * [Thread part]
     * ?????? ?????? ?????? ???????????? ?????? ????????? ????????? ???????????? ??? ?????? ????????? ???????????? ??????
     * ???????????? {??????} ????????? ????????? ????????? ?????? crawling ?????? ?????? ????????????.
     * update on 2022-01-29
     */
    public void getWeatherOfLocation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document weatherDoc = null;
                try {
                    inputAddress = inputAddress.replace(' ', '+');

                    getWeatherImageAccordingToWeather();

                    weatherDoc = Jsoup.connect("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=" + inputAddress + "+??????").get();
                    final String temperature = weatherDoc.select("div.temperature_text").first().text().replace("?????? ??????", ""); // ?????? ??????
                    final String tempInfo = weatherDoc.select("div.temperature_info").first().text(); // ?????? ??????
                    final String airInfo = weatherDoc.select("div.report_card_wrap").first().text(); // ?????? ??????

                    /**
                     * "temperature_info" text split information (updated on 2021-11-11)
                     * 0 : ????????????
                     * 1 : {n}??
                     * 2 : ????????? or ?????????
                     * 3 : ?????? ??????(??????, ?????????)
                     * 4 : ????????????
                     * 5 : {n}%
                     * 6 : ??????
                     * 7 : {n}%
                     * 8 : ??????(##???)
                     * 9 : {n}m/s
                     */
                    String[] temps = tempInfo.split(" ");
                    String[] airs = airInfo.split(" ");

                    String cmp = temps[0] + " " + temps[1] + " " + temps[2];
                    String stateText = temps[3];

                    String rainValue = temps[5];
                    String humidityValue = temps[7];

                    String windText = temps[8];
                    String windValue = temps[9];

                    // index 1, 3, 5??? ?????? ??? ????????????
                    PM10Text.setTextColor(Color.parseColor(getColorAccordingStd(airs[1])));
                    PM2_5Text.setTextColor(Color.parseColor(getColorAccordingStd(airs[3])));
                    ultravioletText.setTextColor(Color.parseColor(getColorAccordingStd(airs[5])));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PM10Text.setText(airs[1]);
                            PM2_5Text.setText(airs[3]);
                            ultravioletText.setText(airs[5]);
                            temperatureText.setText(temperature);
                            currentWeatherStatus.setText(stateText);
                            compareYesterday.setText(cmp);
                            rainPercentText.setText(rainValue);
                            humidityPercentText.setText(humidityValue);
                            windStateText.setText(windText + " ");
                            windStateValueText.setText(windValue);
                            sunsetValueText.setText(airs[7]);
                        }
                    });
                } catch (Exception e) {
                    String[] reTryInputAddressSplit = inputAddress.split("\\+");

                    if (reTryInputAddressSplit.length == 1) {
                        compareYesterday.setText("?????? ????????? ????????? ??? ?????? ???????????????.");
                        return;
                    }
                    StringBuilder newAddress = new StringBuilder();
                    for (int i = 0; i < reTryInputAddressSplit.length - 1; ++i) {
                        newAddress.append(reTryInputAddressSplit[i]);
                        if (i == reTryInputAddressSplit.length - 2) {
                            break;
                        }
                        newAddress.append("+");
                    }
                    inputAddress = newAddress.toString();
                    getWeatherOfLocation();
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * [Thread part]
     * ????????? ????????? ?????? ????????? ?????? ???????????? ?????????
     * ???????????? ?????? ????????? crawling ?????? ?????? ????????? ????????? ????????? ???????????? ????????? ????????? ????????????.
     * image ??? ???????????? class ??????, ?????? ?????? ?????? ???????????? ????????????.
     * ?????? ?????? ????????? .svg ????????? ????????? ????????? ????????? ????????? ????????? ???????????? ????????????. (GlideToVectorYou ??????????????? ?????? -> .svg ??????)
     * ????????? ????????? ???????????? Image view??? ???????????? ????????????.
     * update on 2022-01-29
     */
    public void getWeatherImageAccordingToWeather() {
        new Thread() {
            @Override
            public void run() {
                try {
                    /**
                     * Expecting value of @imageClassName : wt_icon icon_wt{number}
                     * Image format for URL : [URL]icon_wt_{number}.svg
                     */
                    Document doc = Jsoup.connect("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=" + inputAddress + "+??????").get();
                    String imageClassName = doc.select("div.weather_graphic div.weather_main").select("i").attr("class");
                    String state = imageClassName.split(" ")[1];
                    int num = Integer.parseInt(state.split("_")[1].substring(2)); // get integer value

                    final String param = num > 9 ? String.valueOf(num) : "0" + num; // int to String

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                GlideToVectorYou.justLoadImage(MainActivity.this, Uri.parse("https://ssl.pstatic.net/sstatic/keypage/outside/scui/weather_new/img/weather_svg/icon_wt_" + param + ".svg"), weatherImage);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * ????????? ???????????? ?????? ?????? ????????? ??????????????? ?????? ????????? ????????? ???????????? ?????????
     *
     * @param std
     * @return ????????? ?????? ?????? hex ????????? ???
     */
    public String getColorAccordingStd(String std) {
        switch (std) {
            case "??????":
                return "#32a1ff";
            case "??????":
                return "#03c75a";
            case "??????":
                return "#fd9b5a";
            case "????????????":
                return "#ff5959";
        }
        return "#000000";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.distance_location:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("???????????? ?????? ??????");
                builder.setItems(R.array.Region, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int[] indexItems = getResources().getIntArray(R.array.Region_index);
                        distancingText.setText("??? ???????????? " + distanceList.get(indexItems[which]));
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            case R.id.weather_location:
                FrameLayout container = new FrameLayout(this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);

                final EditText editText = new EditText(this);
                editText.setLayoutParams(params);
                container.addView(editText);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("?????? ????????? ??????")
                        .setMessage("???????????? ????????? ???????????? ???????????????. (??? ?????? ??? ?????? ??? ????????? ???????????? ??????)")
                        .setView(container)
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //????????? ?????? cache??? ?????? ??? ??? ????????? ?????? ??? URL??? parameter??? ?????? ??????
                                inputAddress = editText.getText().toString();
                                getWeatherOfLocation();
                                weatherLocation.setText(inputAddress);
                                currentLocationWeather.setBackgroundColor(Color.parseColor("#ffffff"));
                                useCurrentAddress = false;
                            }
                        });
                AlertDialog alertDialog1 = builder2.create();
                alertDialog1.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {

            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    //????????? ?????? ?????? ?????? ???????????? ????????? ??????????????????
                } else {
                    // ????????? ??????, ???????????? ???????????? ???????????????
                }
            }
        }
    }

    public void checkRunTimePermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // ??? ?????? ??????????????? ?????? ?????? ?????? ??????
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    public String getCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocation(latitude, longitude, 5);
        } catch (IOException e) {
            // ???????????? ??????
            return "????????? ????????????";
        } catch (IllegalArgumentException e) {
            return "????????? GPS ??????";
        }
        if (addressList == null || addressList.size() == 0) {
            return "?????? ?????????";
        }

        Address address = addressList.get(0);
        return address.getAddressLine(0) + "\n";
    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("?????? ????????? ?????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n?????? ????????? ?????????????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPS, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                if (checkLocationServicesStatus()) {
                    // GPS ????????? ?????????
                    checkRunTimePermission();
                    return;
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}