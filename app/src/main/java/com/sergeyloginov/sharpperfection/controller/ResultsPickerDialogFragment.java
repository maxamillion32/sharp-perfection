package com.sergeyloginov.sharpperfection.controller;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.sergeyloginov.sharpperfection.R;

import java.lang.reflect.Field;
import java.util.Locale;

public class ResultsPickerDialogFragment extends DialogFragment {

    public static final String EXTRA_WEIGHT = "extra_weight";
    public static final String EXTRA_REPS = "extra_reps";
    private String hundreds = "";
    private String dozens = "00";
    private String hundredths = "00";
    private String reps = "0";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_result_picker, null);

        NumberPicker npHundreds = (NumberPicker) v.findViewById(R.id.np_hundreds);
        setNumberPickerDividerColor(npHundreds, Color.argb(255, 0, 0, 0));
        npHundreds.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npHundreds.setMinValue(0);
        npHundreds.setMaxValue(9);
        npHundreds.setWrapSelectorWheel(true);
        npHundreds.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal == 0) {
                    hundreds = "";
                } else {
                    hundreds = newVal + "";
                }
            }
        });

        NumberPicker npDozens = (NumberPicker) v.findViewById(R.id.np_dozens);
        setNumberPickerDividerColor(npDozens, Color.argb(255, 0, 0, 0));
        npDozens.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npDozens.setMinValue(0);
        npDozens.setMaxValue(99);
        npDozens.setWrapSelectorWheel(true);
        npDozens.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format(Locale.getDefault(), "%02d", value);
            }
        });
        npDozens.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (hundreds.equals("")) {
                    dozens = newVal + "";
                } else if (newVal < 10) {
                    dozens = "0" + newVal;
                } else {
                    dozens = "" + newVal;
                }
            }
        });

        int maxValue = 75;
        int step = 25;
        final String[] values = new String[maxValue/step + 1];
        for (int i = 0; i < values.length; i++) {
            values[i] = String.valueOf(i * step);
        }
        NumberPicker npHundredths = (NumberPicker) v.findViewById(R.id.np_hundredths);
        setNumberPickerDividerColor(npHundredths, Color.argb(255, 0, 0, 0));
        npHundredths.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npHundredths.setMinValue(0);
        npHundredths.setMaxValue(3);
        npHundredths.setWrapSelectorWheel(true);
        npHundredths.setDisplayedValues(values);
        npHundredths.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal == 0) {
                    hundredths = "00";
                } else if (newVal == 1) {
                    hundredths = "25";
                } else if (newVal == 2) {
                    hundredths = "50";
                } else if (newVal == 3) {
                    hundredths = "75";
                }
            }
        });

        NumberPicker npReps = (NumberPicker) v.findViewById(R.id.np_reps);
        setNumberPickerDividerColor(npReps, Color.argb(255, 0, 0, 0));
        npReps.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npReps.setMinValue(0);
        npReps.setMaxValue(99);
        npReps.setWrapSelectorWheel(true);
        npReps.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                reps = newVal + "";
            }
        });

        android.support.v7.app.AlertDialog.Builder builder =
                new android.support.v7.app.AlertDialog.Builder(getActivity());
        final android.support.v7.app.AlertDialog alert = builder
                .setView(v)
                .setTitle(R.string.enter_results)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (getTargetFragment() == null) {
                            return;
                        }
                        String weight = hundreds + dozens + "." + hundredths;
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_WEIGHT, weight);
                        intent.putExtra(EXTRA_REPS, reps);
                        getTargetFragment().onActivityResult(
                                getTargetRequestCode(),
                                Activity.RESULT_OK,
                                intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelSet();
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int i, KeyEvent keyEvent) {
                        if (i == KeyEvent.KEYCODE_BACK
                                && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                            cancelSet();
                        }
                        return false;
                    }
                })
                .create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alert.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(getActivity(), android.R.color.black));
                alert.getButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(getActivity(), android.R.color.black));
            }
        });
        alert.setCanceledOnTouchOutside(false);
        alert.setCancelable(false);
        return alert;
    }

    private void cancelSet() {
        Toast.makeText(getActivity(), R.string.set_canceled, Toast.LENGTH_SHORT).show();
        getTargetFragment().onActivityResult(
                1,
                Activity.RESULT_OK,
                null);
    }

    private void setNumberPickerDividerColor(NumberPicker np, int color) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(np, colorDrawable);
                } catch (IllegalArgumentException
                        | Resources.NotFoundException
                        | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
