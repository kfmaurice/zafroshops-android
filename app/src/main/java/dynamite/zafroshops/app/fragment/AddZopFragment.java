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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import dynamite.zafroshops.app.MainActivity;
import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.data.FullMobileZop;
import dynamite.zafroshops.app.data.LocationBase;
import dynamite.zafroshops.app.data.MobileCountry;
import dynamite.zafroshops.app.data.StorageKeys;
import dynamite.zafroshops.app.data.ZopOrigin;
import dynamite.zafroshops.app.data.ZopServiceType;
import dynamite.zafroshops.app.data.ZopType;

public class AddZopFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    public static ZopType[] zopTypes;
    public static ArrayList<MobileCountry> zopCountries;

    private FullMobileZop newZop;
    private String errors;

    public ArrayAdapter<ZopType> zopTypeAdapter;
    public ArrayAdapter<MobileCountry> zopCountryAdapter;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static AddZopFragment newInstance(int sectionNumber) {
        AddZopFragment fragment = new AddZopFragment();
        Bundle args = new Bundle();

        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        if (zopTypes == null) {
            zopTypes = ZopType.values();
        }

        return fragment;
    }

    public AddZopFragment() {
        zopCountries = new ArrayList<>();
    }

    public boolean ValidateNewZop(final FullMobileZop zop) {
        boolean result = zop != null;
        boolean temp = true;
        errors = "";

        temp = zop.Name == null || TextUtils.isEmpty(zop.Name);
        if(temp) {
            errors = getString(R.string.new_zop_name_error);
            return false;
        }

        temp = zop.City == null || TextUtils.isEmpty(zop.City)
                || zop.CountryID == null || TextUtils.isEmpty(zop.CountryID)
                || zop.Street == null || TextUtils.isEmpty(zop.Street);
        if(temp) {
            errors = getString(R.string.new_zop_location_error);
            return false;
        }

        return result;
    }

    public void setMessage() {
        ((TextView)getView().findViewById(R.id.newZopValidation)).setText(errors);
    }

    public FullMobileZop getFullMobileZop(){
        View view = getView();
        Object nc = ((Spinner)view.findViewById(R.id.newZopCountry)).getSelectedItem();
        newZop = new FullMobileZop();

        newZop.Name = ((EditText)view.findViewById(R.id.newZopName)).getText().toString();
        newZop.Type = (ZopType)((Spinner)view.findViewById(R.id.newZopType)).getSelectedItem();
        newZop.Origin = ZopOrigin.Android;
        newZop.CountryID = nc != null ? ((MobileCountry)nc).ID : "0";
        newZop.City = ((EditText)view.findViewById(R.id.newZopCity)).getText().toString();
        newZop.Street = ((EditText)view.findViewById(R.id.newZopStreet)).getText().toString();
        newZop.StreetNumber = ((EditText)view.findViewById(R.id.newZopStreetNumber)).getText().toString();
        newZop.PhoneNumber = ((EditText)view.findViewById(R.id.newZopPhoneNumber)).getText().toString();
        newZop.Details = ((EditText)view.findViewById(R.id.newZopDetails)).getText().toString();

        return newZop;
    }

    public void clearForm(View v) {
        View view = v == null ? getView() : v;

        ((EditText)view.findViewById(R.id.newZopName)).setText("");
        ((Spinner)view.findViewById(R.id.newZopType)).setSelection(0);
        setLocation(view);
        ((EditText)view.findViewById(R.id.newZopPhoneNumber)).setText("");
        ((EditText)view.findViewById(R.id.newZopDetails)).setText("");
        ((Button)view.findViewById(R.id.newZopOpeningsButton)).setText(getString(R.string.openings_edit) + " 0/7");
        ((Button)view.findViewById(R.id.newZopServicesButton)).setText(getString(R.string.services_edit) + " 0/" + ZopServiceType.values().length);
    }

    public void setLocation(View v) {
        LocationBase address = ((MainActivity)getActivity()).getAddress(false);
        MobileCountry current = null;

        if (address != null && zopCountries.size() > 0) {
            final String country = address.CountryCode;
            if (country != null) {
                current = Iterables.find(zopCountries, new Predicate<MobileCountry>() {
                    @Override
                    public boolean apply(MobileCountry input) {
                        return input.ID.compareToIgnoreCase(country) == 0;
                    }
                });
            }

            View view = v == null ? getView() : v;
            if (view != null && current != null) {
                ((Spinner)view.findViewById(R.id.newZopCountry)).setSelection(zopCountryAdapter.getPosition(current));
                ((EditText)view.findViewById(R.id.newZopCity)).setText(address.Town);
                ((EditText)view.findViewById(R.id.newZopStreet)).setText(address.Street);
                ((EditText)view.findViewById(R.id.newZopStreetNumber)).setText(address.StreetNumber);
            }
        }
    }

    public void setVisibility()
    {
        MainActivity activity = (MainActivity)getActivity();
        RelativeLayout loader = (RelativeLayout) activity.findViewById(R.id.relativeLayoutLoader);
        LinearLayout zop = (LinearLayout) activity.findViewById(R.id.itemZopAdd);

        loader.setVisibility(View.VISIBLE);
        zop.setVisibility(View.INVISIBLE);
    }

    public void resetVisibility()
    {
        View view = getView();
        RelativeLayout loader = (RelativeLayout) view.findViewById(R.id.relativeLayoutLoader);
        LinearLayout zop = (LinearLayout) view.findViewById(R.id.itemZopAdd);

        loader.setVisibility(View.INVISIBLE);
        zop.setVisibility(View.VISIBLE);
    }

    public ArrayList<MobileCountry> getCountries() {
        ArrayList<MobileCountry> result = new ArrayList<>();

        final SharedPreferences preferences = getActivity().getPreferences(0);
        final SharedPreferences.Editor editor = preferences.edit();
        boolean fromFile =false;

        if (preferences.contains(StorageKeys.COUNTRIES_KEY)) {
            try {
                ObjectInputStream ois = new ObjectInputStream(getActivity().getBaseContext().openFileInput(StorageKeys.COUNTRIES_KEY));

                zopCountries = (ArrayList<MobileCountry>)ois.readObject();
                zopCountryAdapter.clear();
                zopCountryAdapter.addAll(zopCountries);
                fromFile = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if(!fromFile) {
            if (zopCountries == null || zopCountries.size() == 0) {
                ListenableFuture<JsonElement> query = MainActivity.MobileClient.invokeApi("mobileCountry", "GET", new ArrayList<Pair<String, String>>());
                Futures.addCallback(query, new FutureCallback<JsonElement>() {
                    @Override
                    public void onSuccess(JsonElement result) {
                        JsonArray typesAsJson = result.getAsJsonArray();
                        if (typesAsJson != null) {
                            zopCountries = new Gson().fromJson(result, new TypeToken<ArrayList<MobileCountry>>() {
                            }.getType());
                        }
                        zopCountryAdapter.clear();
                        zopCountryAdapter.addAll(zopCountries);
                        setLocation(null);

                        try {
                            ObjectOutputStream oos = new ObjectOutputStream(getActivity().getBaseContext().openFileOutput(StorageKeys.COUNTRIES_KEY, Context.MODE_PRIVATE));

                            oos.writeObject(zopCountries);
                            oos.flush();
                            oos.close();
                            editor.putString(StorageKeys.COUNTRIES_KEY, StorageKeys.COUNTRIES_KEY);
                            editor.commit();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {

                    }
                });
            }
        }

        return  result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OpeningsDialogFragment.NEW_ZOP_OPENINGS_RESULT_CODE)
        {
            int count = data.getIntExtra(OpeningsDialogFragment.NEW_ZOP_OPENINGS_COUNT_KEY, 0);
            ((Button)getView().findViewById(R.id.newZopOpeningsButton)).setText(getString(R.string.openings_edit) + " " + count + "/7");
        }
        if (requestCode == ZopServicesFragment.NEW_ZOP_SERVICES_RESULT_CODE)
        {
            int count = data.getIntExtra(ZopServicesFragment.NEW_ZOP_SERVICES_COUNT_KEY, 0);
            ((Button)getView().findViewById(R.id.newZopServicesButton)).setText(getString(R.string.services_edit) + " " + count + "/" + ZopServiceType.values().length);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MainActivity activity = (MainActivity)getActivity();
        activity.getMenuInflater().inflate(R.menu.menu_add, menu);
        activity.restoreActionBar();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Activity activity = getActivity();

        zopTypeAdapter = new ArrayAdapter<>(activity, R.layout.spinner_item, zopTypes);
        zopCountryAdapter = new ArrayAdapter<>(activity, R.layout.spinner_item, zopCountries);
        zopTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zopCountryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getCountries();
    }

    @Override
    public void onResume() {
        super.onResume();
        zopCountryAdapter.clear();
        zopCountryAdapter.addAll(zopCountries);
        zopCountryAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_zop, container, false);
        Spinner zopTypeSpinner = (Spinner) rootView.findViewById(R.id.newZopType);
        Spinner zopCountrySpinner = (Spinner) rootView.findViewById(R.id.newZopCountry);

        RelativeLayout loader = (RelativeLayout) rootView.findViewById(R.id.relativeLayoutLoader);
        LinearLayout zop = (LinearLayout) rootView.findViewById(R.id.itemZopAdd);
        TextView text = (TextView) rootView.findViewById(R.id.loading_message);
        final TextView phoneCode = (TextView) rootView.findViewById(R.id.newZopPhoneCode);

        loader.setVisibility(View.INVISIBLE);
        zop.setVisibility(View.VISIBLE);
        text.setText(getString(R.string.sending_data));
        zopTypeSpinner.setAdapter(zopTypeAdapter);
        zopCountrySpinner.setAdapter(zopCountryAdapter);
        zopTypeSpinner.setSelection(zopTypeAdapter.getPosition(ZopType.Shop));

        ((Button)rootView.findViewById(R.id.newZopOpeningsButton)).setText(getString(R.string.openings_edit) + " 0/7");
        ((Button)rootView.findViewById(R.id.newZopServicesButton)).setText(getString(R.string.services_editing) + " 0/" + ZopServiceType.values().length);
        zopCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                MobileCountry country = (MobileCountry) parent.getItemAtPosition(position);

                phoneCode.setText(country.PhoneCode);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        clearForm(rootView);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}