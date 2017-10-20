package com.clsroom.dialogs;

import android.app.DatePickerDialog;
import android.view.View;
import android.widget.NumberPicker;

import com.clsroom.R;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MonthYearPickerDialog extends CustomDialogFragment
{

    private static final int MAX_YEAR = 2099;
    private static final int MIN_YEAR = 1900;
    private DatePickerDialog.OnDateSetListener listener;
    public static final String[] MONTH_ARRAY = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July",
            "Aug", "Sep", "Oct", "Nov", "Dec"};
    private Calendar mCurrentDate;

    public static MonthYearPickerDialog getInstance(Calendar currentDate)
    {
        MonthYearPickerDialog fragment = new MonthYearPickerDialog();
        fragment.mCurrentDate = currentDate;
        return fragment;
    }

    public MonthYearPickerDialog()
    {

    }

    public void setListener(DatePickerDialog.OnDateSetListener listener)
    {
        this.listener = listener;
    }

    @Bind(R.id.picker_month)
    NumberPicker monthPicker;

    @Bind(R.id.picker_year)
    NumberPicker yearPicker;

    @Override
    public void submit(View view)
    {
        super.submit(view);
        if (listener != null)
        {
            listener.onDateSet(null, yearPicker.getValue(), monthPicker.getValue(), 0);
        }
        dismiss();
    }

    @Override
    public void onCreateView(View parentView)
    {
        ButterKnife.bind(this, parentView);
        Calendar cal = Calendar.getInstance();
        cal.set(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH), mCurrentDate.get(Calendar.DAY_OF_MONTH));

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setValue(cal.get(Calendar.MONTH));
        monthPicker.setDisplayedValues(MONTH_ARRAY);

        int year = cal.get(Calendar.YEAR);
        yearPicker.setMinValue(MIN_YEAR);
        yearPicker.setMaxValue(MAX_YEAR);
        yearPicker.setValue(year);
        setDialogTitle(R.string.selectMonth);
        setSubmitBtnTxt(R.string.ok);
        setSubmitBtnImg(R.mipmap.ok);
    }

    @Override
    protected int getContentViewLayoutRes()
    {
        return R.layout.month_year_picker;
    }
}