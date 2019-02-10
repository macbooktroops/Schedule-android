package com.playgilround.schedule.client.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.playgilround.schedule.client.R;
import com.playgilround.schedule.client.model.Schedule;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

import static com.playgilround.schedule.client.activity.ScheduleInfoActivity.ADD_SCHEDULE;

/**
 * 18-12-30
 * 스케줄 추가 관련 Activity
 */
public class AddScheduleActivity extends AppCompatActivity implements OnSelectDateListener, OnDateSetListener {

    static final String TAG = AddScheduleActivity.class.getSimpleName();

    @BindView(R.id.tv_date)
    TextView tvDate;

    @BindView(R.id.tvScheduleTime)
    TextView tvTime;

    @BindView(R.id.tvScheduleLocation)
    TextView tvLocation;

    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    @BindView(R.id.etScheduleTitle)
    EditText etTitle;

    @BindView(R.id.etScheduleDesc)
    EditText etDesc;

    Realm realm;

    String strMDay, strMTime, strMYearMonth;

    //Schedule DB  dateDay 컬럼에 들어갈 항목
    ArrayList<String> arrDateDay;

    //Schedule DB date 컬럼에 들어갈 항목
    ArrayList<String> arrDate;
    int chooseSize;

    //SetLocationActivity.class 에서 받은 위치정보.
    String resLocation;
    Double resLatitude = 0.0;
    Double resLongitude = 0.0;

    public static final int LOCATION_START = 0x1000;
    public static final int LOCATION_OK = 0x1001;
    public static final String HOUR_MINUTE = "hour_minute";

    TimePickerDialog timePickerDialog;

    //단일인지 다중인지 판단하는 플래그.
    public boolean isManyDay = false;

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_add_schedule);

        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();

        arrDateDay = new ArrayList<>();
        arrDate = new ArrayList<>();
        Intent intent = getIntent();
        if (intent.getStringExtra("date") != null) {
            //단일 날짜 선택 일 경우
            String date = intent.getStringExtra("date");

            String strYear = date.substring(0, 4);
            String strMonth = date.substring(4, 6);
            String strDay = date.substring(6, 8);

            String strDate = strYear + "년 " + strMonth + "월 " + strDay + "일";

            strMYearMonth = strYear + "-" + strMonth;
            strMDay = strYear + "-" + strMonth + "-" + strDay;

            arrDate.add(strMYearMonth);
            arrDateDay.add(strMDay);

            strMTime = "00:00";

            String strTime = strYear + "-" + strMonth + "-" + strDay + " " + strMTime;
            tvDate.setText(strDate);
            tvTime.setText(strTime);
        } else if (intent.getStringArrayListExtra("dateArr") != null) {
            //다중 날짜 선택일 경우
            arrDateDay = intent.getStringArrayListExtra("dateArr");

            //yyyy-MM 포맷으로 arrDate 저장
            for (String date : arrDateDay) {
                String strYear = date.substring(0, 4);
                String strMonth = date.substring(5, 7);
                strMYearMonth = strYear + "-" + strMonth;
                arrDate.add(strMYearMonth);
            }

            strMTime = "00:00";
            String strTime = arrDateDay.get(0);
            String strRetTime = strTime + " 외 " + (arrDateDay.size() -1) + "일";

            chooseSize = arrDateDay.size();
            isManyDay = true;
            tvDate.setText(arrDateDay.get(0) + " ~ " + arrDateDay.get(arrDateDay.size() -1));
            tvTime.setText(strRetTime);
        }
        btnConfirm.setOnClickListener(l -> confirm());

        //TimePicker
        timePickerDialog = new TimePickerDialog.Builder()
                .setType(Type.HOURS_MINS)
                .setCallBack(this)
                .setSureStringId(getString(R.string.button_confirm))
                .setCancelStringId(getString(R.string.button_cancel))
                .setTitleStringId(getString(R.string.text_time_set))
                .setThemeColor(getResources().getColor(R.color.colorGreen))
                .setHourText(getString(R.string.text_hour))
                .setMinuteText(getString(R.string.text_minute))
                .setWheelItemTextSize(20)
                .build();
    }

    @OnClick(R.id.llScheduleTime)
    void onShowTimeDialog() {
        DatePickerBuilder dateBuilder = new DatePickerBuilder(this, this)
                .headerColor(R.color.colorGreen)
                .headerLabelColor(android.R.color.white)
                .selectionColor(R.color.colorGreen)
                .todayLabelColor(R.color.colorAccent)
                .dialogButtonsColor(android.R.color.holo_green_dark)
                .previousButtonSrc(R.drawable.ic_chevron_left_black_24dp)
                .forwardButtonSrc(R.drawable.ic_chevron_right_black_24dp);
        if (isManyDay) {
            dateBuilder.pickerType(CalendarView.MANY_DAYS_PICKER);
        } else {
            dateBuilder.pickerType(CalendarView.ONE_DAY_PICKER);
        }

        DatePicker datePicker = dateBuilder.build();
        datePicker.show();
    }

    @OnClick(R.id.llScheduleLocation)
    void onShowLocationActivity() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(this, SetLocationActivity.class);
            startActivityForResult(intent, LOCATION_START);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_msg_gps_enable), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((WindowManager.LayoutParams) params);
    }

    //Click Confirm Button
    private void confirm() {
        if (etTitle.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_msg_input_schedule), Toast.LENGTH_LONG).show();
        } else {
            realm.executeTransaction(realm -> {
//                for (String strDateDay : arrDateDay) {
                for (int i = 0; i < arrDateDay.size(); i++) {
                    Number currentIdNum = realm.where(Schedule.class).max("id");
                    int nextId;

                    if (currentIdNum == null) {
                        nextId = 0;
                    } else {
                        nextId = currentIdNum.intValue() + 1;
                    }

                    Schedule mSchedule = realm.createObject(Schedule.class, nextId);
                    mSchedule.setTitle(etTitle.getText().toString());
                    mSchedule.setDate(arrDate.get(i));
                    mSchedule.setDateDay(arrDateDay.get(i));
                    try {
                        String retTime = arrDateDay.get(i) + " " + strMTime;
                        Date date = new SimpleDateFormat(getString(R.string.text_date_day_time), Locale.ENGLISH).parse(retTime);
                        long milliseconds = date.getTime(); //add 9 hour
                        mSchedule.setTime(milliseconds);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    mSchedule.setLocation(resLocation);
                    mSchedule.setLatitude(resLatitude);
                    mSchedule.setLongitude(resLongitude);
                    mSchedule.setDesc(etDesc.getText().toString());
                }
                Toast.makeText(getApplicationContext(), getString(R.string.toast_msg_save_schedule), Toast.LENGTH_LONG).show();
                Intent intent = new Intent();

                if (!isManyDay) {
                    intent.putExtra("date", tvDate.getText());
                    intent.putExtra("dateDay", arrDateDay.get(0));
                    setResult(ADD_SCHEDULE, intent);
                } else {
                    intent.putExtra("date", arrDate.get(0));
                    setResult(ADD_SCHEDULE, intent);
                }
                finish();
            });

        }
    }

    //Dialog Day Click Event
    @SuppressLint("SetTextI18n")
    @Override
    public void onSelect(List<Calendar> calendars) {
        arrDateDay = new ArrayList<>();
        arrDate = new ArrayList<>();
        String strDateTime;
        String strDateTitle = "";
        try {
            for (Calendar calendar : calendars) {
                String strSelect = calendar.getTime().toString();
                Date date = new SimpleDateFormat(getString(R.string.text_date_all_format), Locale.ENGLISH).parse(strSelect);
                long milliseconds = date.getTime();

                DateTime dateTime = new DateTime(Long.valueOf(milliseconds), DateTimeZone.UTC);

                strDateTime = dateTime.plusHours(9).toString(getString(R.string.text_date_year_month_day));
                strDateTitle = dateTime.plusHours(9).toString(getString(R.string.text_date_year_month_day_title));
                arrDateDay.add(strDateTime);

                String strYear = strDateTime.substring(0, 4);
                String strMonth = strDateTime.substring(5, 7);
                strMYearMonth = strYear + "-" + strMonth;
                arrDate.add(strMYearMonth);
            }
            strMDay = arrDateDay.get(0);
            if (calendars.size() > 1) {
                chooseSize = calendars.size() - 1;
                tvDate.setText(arrDateDay.get(0) + " ~ " + arrDateDay.get(arrDateDay.size() -1));
                tvTime.setText(strMDay + " 외 " + chooseSize + "일");
                isManyDay = true;
            } else {
                isManyDay = false;
                tvDate.setText(strDateTitle);
                tvTime.setText(strMDay); //변경한 날짜로 재 표시
            }

            //시, 분을 설정하는 다이얼로그 표시
            timePickerDialog.show(getSupportFragmentManager(), HOUR_MINUTE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Dialog Time Click Event
    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSet(TimePickerDialog timePickerDialog, long milliseconds) {
        DateTime dateTime = new DateTime(Long.valueOf(milliseconds), DateTimeZone.UTC);
        strMTime = dateTime.plusHours(9).toString(getString(R.string.text_date_time));

        String strDayTime = strMDay + " " + strMTime;

        if (isManyDay) {
            tvTime.setText(strDayTime + " 외 " + chooseSize + "일");
        } else {
            tvTime.setText(strDayTime);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == LOCATION_OK) {
            switch (requestCode) {
                case LOCATION_START:

                    resLocation = data.getStringExtra(SetLocationActivity.INTENT_EXTRA_LOCATION);
                    resLatitude = data.getDoubleExtra(SetLocationActivity.INTENT_EXTRA_LATITUDE, 0);
                    resLongitude = data.getDoubleExtra(SetLocationActivity.INTENT_EXTRA_LONGITUDE, 0);

                    if (TextUtils.isEmpty(resLocation)) {
                        tvLocation.setText(R.string.text_add_location);
                    } else {
                        tvLocation.setText(resLocation);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
