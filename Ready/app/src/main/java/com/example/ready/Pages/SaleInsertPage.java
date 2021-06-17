package com.example.ready.Pages;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ready.Calendar.SaleDecorator;
import com.example.ready.Calendar.SaturdayDecorator;
import com.example.ready.Calendar.SundayDecorator;
import com.example.ready.Calendar.WeekDayDecorator;
import com.example.ready.DB.DBHelper;
import com.example.ready.DB.Model.Menu;
import com.example.ready.DB.Model.Sale;
import com.example.ready.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SaleInsertPage extends Fragment {
    private View v;
    private MaterialCalendarView materialCalendarView;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private TableLayout saleInsertTable;
    private RadioGroup radioGroup;
    private Boolean isHoliday = false, isNoon = false;
    private String dateString;
    private DBHelper dbHelper;

    private ArrayList<Menu> menus = new ArrayList<>();
    private ArrayList<Sale> sales = new ArrayList<>();
    private ArrayList<EditText> saleQty = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.sale_page, container, false);

        dbHelper = DBHelper.getInstance(v.getContext());

        materialCalendarView = v.findViewById(R.id.calendarView);
        slidingUpPanelLayout = v.findViewById(R.id.sliding_layout);
        saleInsertTable = v.findViewById(R.id.saleInsertTableLayout);
        radioGroup = v.findViewById(R.id.radioGroup);

        saleInsertInit();
        calendarInit();
        radioGroupInit();

        return v;
    }

    private void saleInsertInit() {
        menus = dbHelper.getMenu();
        Button saveBtn, closeBtn;

        for(int i = 0; i < menus.size(); i++) {
            TableRow tableRow = new TableRow(v.getContext());
            tableRow.setBackgroundResource(R.drawable.menu_insert_shape);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
            ));

            // menu name
            TextView textView = new TextView(v.getContext());
            textView.setText(menus.get(i).menu_name);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(16);

            tableRow.addView(textView);

            // price
            EditText editText = new EditText(v.getContext());
            editText.setGravity(Gravity.CENTER);
            editText.setTextSize(16);
            editText.setId(i);

            tableRow.addView(editText);

            saleInsertTable.addView(tableRow);
            saleQty.add(editText);
        }

        saveBtn = v.findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(saveOnClickListener);

        closeBtn = v.findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
    }

    private void calendarInit() {
        materialCalendarView.setSelectedDate(CalendarDay.today());
        materialCalendarView.addDecorators(
                new SaturdayDecorator(),
                new SundayDecorator(),
                new WeekDayDecorator(),
                new SaleDecorator(v.getContext())
        );
        // 날짜 클릭 마다 해당되는 데이터 로드
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                saleDateInit(date);

                sales = dbHelper.getSale(dateString);
                loadDBData();
            }
        });
    }

    private void saleDateInit(CalendarDay date) {
        TextView currentDateText;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
        dateString = simpleDateFormat.format(date.getDate());

        currentDateText = v.findViewById(R.id.currentDateText);
        currentDateText.setText(dateString);

        int week = date.getCalendar().get(Calendar.DAY_OF_WEEK);
        if(week == 1 || week == 7)
            isHoliday = true;
        else
            isHoliday = false;
    }

    private View.OnClickListener saveOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Sale sale = new Sale();

            if(sales.isEmpty()) {
                sale.date = dateString;
                sale.holiday = isHoliday;
                sale.time = isNoon;

                for (int i = 0; i < saleQty.size(); i++) {
                    sale.menu_id = menus.get(i)._id;

                    try {
                        sale.qty = Integer.parseInt(saleQty.get(i).getText().toString());

                        dbHelper.insertSale(sale);
                        System.out.println(menus.get(i).menu_name + ": DB INSERT");
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("DB INSERT FAIL");
                    }
                }
            }

            else {
                sale.date = dateString;
                sale.time = isNoon;

                for (int i = 0; i < saleQty.size(); i++) {
                    sale.menu_id = menus.get(i)._id;
                    sale.qty = Integer.parseInt(saleQty.get(i).getText().toString());

                    try {
                        dbHelper.updateSale(sale);
                        System.out.println(menus.get(i).menu_name + ": DB UPDATE");
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("DB UPDATE FAIL");
                    }
                }
            }
        }
    };

    private void loadDBData() {
        if(sales.isEmpty()) {
            for(int i = 0; i < saleQty.size(); i++)
                saleQty.get(i).setText("");
            radioGroup.clearCheck();
        }

        else {
            System.out.println("DB LOAD");
            System.out.println(sales.size());
            System.out.println(saleQty.size());
            for (int i = 0; i < sales.size(); i++)
                saleQty.get(i).setText(String.valueOf(sales.get(i).qty));

            RadioButton radioButton;
            if(sales.get(0).time)
                radioButton = v.findViewById(R.id.noon);
            else
                radioButton = v.findViewById(R.id.morning);
            radioButton.setChecked(true);
        }
    }

    private void radioGroupInit() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.morning:
                        isNoon = false;
                        break;
                    case R.id.noon:
                        isNoon = true;
                        break;
                }
            }
        });
    }
}
