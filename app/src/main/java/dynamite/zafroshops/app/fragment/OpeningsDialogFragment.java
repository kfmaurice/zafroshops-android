/*
 * Copyright 2016 Maurice Kenmeue Fonwe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dynamite.zafroshops.app.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;

import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.data.MobileOpeningHour;
import dynamite.zafroshops.app.data.MobileOpeningHourData;

public class OpeningsDialogFragment extends DialogFragment {
    public static final String NEW_ZOP_OPENINGS = "new_zop_openings";
    public static final int NEW_ZOP_OPENINGS_RESULT_CODE = 0;
    public static final String NEW_ZOP_OPENINGS_COUNT_KEY = "new_zop_openings_count";
    public static ArrayList<String> days;
    public static ArrayList<String> hours;
    public static ArrayList<String> minutes;

    private TextView openingHoursLabel;
    private LinearLayout openingHoursContainer;
    private Button openingHoursDeleteButton;

    private ArrayList<MobileOpeningHourData> data;
    private ArrayList<MobileOpeningHourData> tempData;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        data = (ArrayList<MobileOpeningHourData>)getArguments().get(NEW_ZOP_OPENINGS);
        tempData = new ArrayList();

        for (int i = 0; i < data.size(); i++) {
            tempData.add(data.get(i).Copy());
        }

        // set spinners
        ArrayAdapter<String> openingsDayAdapter;
        ArrayAdapter<String> openingsHoursAdapter;
        ArrayAdapter<String> openingsMinutesAdapter;

        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.openings_dialog, null);

        builder.setView(view).setTitle(R.string.openings_editing)
        .setPositiveButton(R.string.button_submit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                data.clear();
                for (int i = 0; i < tempData.size(); i++) {
                    data.add(tempData.get(i).Copy());
                }

                Intent intent = new Intent();
                intent.putExtra(NEW_ZOP_OPENINGS_COUNT_KEY, OpeningsDialogFragment.this.data.size());
                getTargetFragment().onActivityResult(getTargetRequestCode(), NEW_ZOP_OPENINGS_RESULT_CODE, intent);
            }
        })
        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        final Spinner ohd = (Spinner)view.findViewById(R.id.openingsDay);
        final Spinner ohsh = (Spinner)view.findViewById(R.id.openingsStartHour);
        final Spinner ohsm = (Spinner)view.findViewById(R.id.openingsStartMinute);
        final Spinner oheh = (Spinner)view.findViewById(R.id.openingsEndHour);
        final Spinner ohem = (Spinner)view.findViewById(R.id.openingsEndMinute);

        if (days == null) {
            days = new ArrayList();
            for (int i = 0; i < 7; i++) {
                try {
                    days.add(getString(R.string.class.getField("day" + i).getInt(null)));
                } catch (IllegalAccessException ignored) {
                } catch (NoSuchFieldException e) {
                }
            }
        }

        if (hours == null) {
            hours = new ArrayList();
            for(int i = 0; i < 24; i++) {
                hours.add(String.format("%02d", i));
            }
        }

        if (minutes == null) {
            minutes = new ArrayList();
            for(int i = 0; i < 4; i++) {
                minutes.add(String.format("%02d", i * 15));
            }
        }

        openingsDayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, days);
        openingsDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ohd.setAdapter(openingsDayAdapter);

        openingsHoursAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, hours);
        openingsHoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ohsh.setAdapter(openingsHoursAdapter);
        oheh.setAdapter(openingsHoursAdapter);

        openingsMinutesAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, minutes);
        openingsMinutesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ohsm.setAdapter(openingsMinutesAdapter);
        ohem.setAdapter(openingsMinutesAdapter);

        // set current openings
        openingHoursContainer = (LinearLayout)view.findViewById(R.id.openingHours);
        openingHoursLabel = (TextView)view.findViewById(R.id.openingHoursLabel);
        openingHoursDeleteButton = (Button)view.findViewById(R.id.openingsHoursDelete);

        ZopItemFragment.setOpeningsList(tempData, openingHoursContainer, inflater, openingHoursLabel);

        // set buttons
        view.findViewById(R.id.openingsHoursAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get data
                final int position = ohd.getSelectedItemPosition();
                MobileOpeningHourData item = null;
                try {
                    item = Iterables.find(tempData, new Predicate<MobileOpeningHourData>() {
                        @Override
                        public boolean apply(MobileOpeningHourData input) {
                            return input.Day == position;
                        }
                    });
                } catch (NoSuchElementException ignored) {
                }

                // update data
                if (item == null) {
                    item = new MobileOpeningHourData((byte) position);
                    tempData.add(item);
                }

                final MobileOpeningHour moh = new MobileOpeningHour();
                moh.StartTimeHour = (byte) ohsh.getSelectedItemPosition();
                moh.StartTimeMinute = (byte) (ohsm.getSelectedItemPosition() * 15);
                moh.EndTimeHour = (byte) oheh.getSelectedItemPosition();
                moh.EndTimeMinute = (byte) (ohem.getSelectedItemPosition() * 15);

                MobileOpeningHour temp = null;
                try {
                    temp = Iterables.find(item.Hours, new Predicate<MobileOpeningHour>() {
                        @Override
                        public boolean apply(MobileOpeningHour input) {
                            return input.compareTo(moh) == 0;
                        }
                    });
                } catch (NoSuchElementException ignored) {
                }

                if (temp == null) {
                    item.Hours.add(moh);
                }

                // sort data
                Collections.sort(item.Hours);
                Collections.sort(tempData);

                // update view
                ZopItemFragment.setOpeningsList(tempData, openingHoursContainer, inflater, openingHoursLabel);
                openingHoursDeleteButton.setEnabled(true);
            }
        });
        openingHoursDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get data
                final int position = ohd.getSelectedItemPosition();
                MobileOpeningHourData item = null;
                try {
                    item = Iterables.find(tempData, new Predicate<MobileOpeningHourData>() {
                        @Override
                        public boolean apply(MobileOpeningHourData input) {
                            return input.Day == position;
                        }
                    });
                } catch (NoSuchElementException ignored) {
                }
                // update data
                if (item != null) {
                    tempData.remove(item);
                    openingHoursDeleteButton.setEnabled(false);
                }
                // update view
                ZopItemFragment.setOpeningsList(tempData, openingHoursContainer, inflater, openingHoursLabel);
            }
        });
        ohd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                MobileOpeningHourData item = null;
                // get data
                try {
                    item = Iterables.find(tempData, new Predicate<MobileOpeningHourData>() {
                        @Override
                        public boolean apply(MobileOpeningHourData input) {
                            return input.Day == position;
                        }
                    });
                } catch (NoSuchElementException ignored) {
                }
                // update view
                if (item != null) {
                    openingHoursDeleteButton.setEnabled(true);
                }
                else {
                    openingHoursDeleteButton.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return builder.create();
    }
}
