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
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import dynamite.zafroshops.app.MainActivity;
import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.data.FullMobileZop;
import dynamite.zafroshops.app.data.MobileOpeningHour;
import dynamite.zafroshops.app.data.MobileOpeningHourData;
import dynamite.zafroshops.app.data.MobileZop;
import dynamite.zafroshops.app.data.MobileZopService;
import dynamite.zafroshops.app.data.ZopServiceType;

public class ZopItemFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_ZOP_ID = "zop_type";

    public FullMobileZop zop;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ZopItemFragment newInstance(String id) {
        ZopItemFragment fragment = new ZopItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ZOP_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public ZopItemFragment() {
        zop = new FullMobileZop() { };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MainActivity activity = (MainActivity)getActivity();
        activity.getMenuInflater().inflate(R.menu.menu_zop, menu);
//        activity.restoreActionBar();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final String id = getArguments().getString(ARG_ZOP_ID);
        InputStream is = getResources().openRawResource(R.raw.zops);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        ArrayList<FullMobileZop> zops = new ArrayList<>(Collections2.filter((ArrayList<FullMobileZop>) new Gson().fromJson(reader, new TypeToken<ArrayList<FullMobileZop>>() {
        }.getType()), new Predicate<FullMobileZop>() {
            @Override
            public boolean apply(FullMobileZop input) {
                return input.id.equals(id);
            }
        }));
        if (zops.size() == 1) {
            zop = zops.get(0);
        }

        ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>() {{
                add(new Pair<>("fullId", id));
            }};

        ListenableFuture<JsonElement> result = MainActivity.MobileClient.invokeApi("mobileZop", "GET", parameters);

        Futures.addCallback(result, new FutureCallback<JsonElement>() {
            Activity activity = getActivity();

            @Override
            public void onSuccess(JsonElement result) {
                JsonObject typesAsJson = result.getAsJsonObject();
                if (typesAsJson != null) {
                    zop = new Gson().fromJson(result, FullMobileZop.class);
                }

                setZop(activity);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                if (zop == null) {
                    LinearLayout itemZop = (LinearLayout) activity.findViewById(R.id.itemZop);
                    RelativeLayout loader = (RelativeLayout) activity.findViewById(R.id.relativeLayoutLoader);
                    itemZop.setVisibility(View.INVISIBLE);
                    loader.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_zop_item, container, false);
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.itemZop);
        RelativeLayout loader = (RelativeLayout) rootView.findViewById(R.id.relativeLayoutLoader);

        setLayout(inflater, layout, zop, loader);

        return rootView;
    }

    private void setZop(Activity activity) {
        if (zop != null && zop.Location != null && MainActivity.LastLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(MainActivity.LastLocation.Latitude, MainActivity.LastLocation.Longitude,
                    zop.Location.Latitude, zop.Location.Longitude, results);
            zop.Distance = results[0] / 1000;
        }

        LinearLayout itemZop = (LinearLayout) activity.findViewById(R.id.itemZop);
        RelativeLayout loader = (RelativeLayout) activity.findViewById(R.id.relativeLayoutLoader);
        ArrayList<MobileOpeningHourData> ohs = zop.getGroupedOpeningHours();

        if (ohs != null) {
            setLayout(activity.getLayoutInflater(), itemZop, zop, loader);
        }
    }

    public static void setOpeningsList(ArrayList<MobileOpeningHourData> data, LinearLayout listContainer, LayoutInflater inflater, TextView label) {
        // set opening hours
        if (data != null && data.size() > 0 && listContainer != null) {
            listContainer.removeAllViews();
            label.setVisibility(View.VISIBLE);

            for (MobileOpeningHourData mohd : data) {
                View listItem = inflater.inflate(R.layout.opening_hour_item, null);
                TextView dayTxt = ((TextView) listItem.findViewById(R.id.openingHoursDay));
                TextView hourTxT = ((TextView) listItem.findViewById(R.id.openingHoursHour));

                try {
                    dayTxt.setText(R.string.class.getField("day" + mohd.Day).getInt(null));
                } catch (IllegalAccessException ignored) {

                } catch (NoSuchFieldException e) {
                    dayTxt.setText("");
                }

                String hour = "";
                for (MobileOpeningHour moh : mohd.Hours) {
                    hour += ((hour.length() > 0) ? ", " : "") + moh.toString();
                }
                hourTxT.setText(hour);

                listContainer.addView(listItem);
            }
        }
        else   {
            if (listContainer != null) {
                listContainer.removeAllViews();
            }
            label.setVisibility(View.INVISIBLE);
        }
    }

    private void setLayout(LayoutInflater inflater, LinearLayout layout, final FullMobileZop zop, RelativeLayout loader ) {
        LinearLayout item = (LinearLayout)layout.findViewById(R.id.itemZop);

        loader.setVisibility(View.VISIBLE);
        item.setVisibility(View.INVISIBLE);
        if (zop != null && zop.id != null && !zop.id.trim().equals("")) {
            MainActivity activity = (MainActivity) getActivity();
            (activity).setCurrentItem(zop.id, zop);

            LinearLayout list = (LinearLayout)layout.findViewById(R.id.zopOpeningHours);
            ArrayList<MobileOpeningHourData> ohs = zop.getGroupedOpeningHours();

            // set other fields
            ((ImageView)item.findViewById(R.id.zopImg)).setImageResource(R.drawable.nopictureyet);
            ((TextView)item.findViewById(R.id.zopName)).setText(zop.Name);
            ((TextView)item.findViewById(R.id.zopStreet)).setText(zop.Street + " " + zop.StreetNumber);
            ((TextView)item.findViewById(R.id.zopCity)).setText(zop.City);
            ((TextView)item.findViewById(R.id.zopCountry)).setText(zop.CountryName);

            LinearLayout linearLayout = ((LinearLayout)item.findViewById(R.id.zopServiceIcons));

            linearLayout.removeAllViews();
            setView(zop.Type.toString(), zop.Type.getText(), linearLayout, inflater);
            for (Object s : zop.Services) {
                ZopServiceType zopServiceType = ((MobileZopService)s).Service;

                if (!zopServiceType.toString().equals(zop.Type.toString())) {
                    setView(zopServiceType.toString(), zopServiceType.getText(), linearLayout, inflater);
                }
            }

            if (zop.Distance > 0) {
                ((TextView) item.findViewById(R.id.locatoin_km)).setText( " (" + String.format("%.0f", zop.Distance) + " " + getString(R.string.far_away) + ")");
            }
            if(zop.PhoneNumber == null || zop.PhoneNumber.trim().equals("")) {
                item.findViewById(R.id.zopPhoneNumberLabel).setVisibility(View.INVISIBLE);
            }
            else {
                item.findViewById(R.id.zopPhoneNumberLabel).setVisibility(View.VISIBLE);
                ((TextView) item.findViewById(R.id.zopPhoneNumber)).setText(zop.CountryPhoneCode + " " + zop.PhoneNumber);
            }

            if(zop.Details == null || zop.Details.trim().equals("")) {
                item.findViewById(R.id.zopDetailsLabel).setVisibility(View.INVISIBLE);
            }
            else {
                item.findViewById(R.id.zopDetailsLabel).setVisibility(View.VISIBLE);
                ((TextView) item.findViewById(R.id.zopDetails)).setText(zop.Details);
            }
            // set opening hours
            setOpeningsList(ohs, list, inflater, (TextView)item.findViewById(R.id.zopOpeningHoursLabel));
            loader.setVisibility(View.INVISIBLE);
            item.setVisibility(View.VISIBLE);
        }
    }

    private void setView(String text, String img, LinearLayout linearLayout, LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.service_grid_item, null);
        ImageView icon = (ImageView) view.findViewById(R.id.imageViewGrid);
        TextView txt = (TextView) view.findViewById(R.id.textViewGrid);
        txt.setText(text);

        try {
            icon.setImageResource(R.drawable.class.getField(img).getInt(null));
        } catch (IllegalAccessException ignored) {

        } catch (NoSuchFieldException e) {
            icon.setImageResource(R.drawable.shop);
        }
        linearLayout.addView(view);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
