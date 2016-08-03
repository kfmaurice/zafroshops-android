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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;

import dynamite.zafroshops.app.adapter.AllZopsGridViewAdapter;
import dynamite.zafroshops.app.MainActivity;
import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.data.MobileZop;
import dynamite.zafroshops.app.data.StorageKeys;

/**
 * A placeholder fragment containing zops by category.
 */
public class AllZopsFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_FORCE = "force";

    private AllZopsGridViewAdapter adapter;
    public ArrayList<MobileZop> types;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static AllZopsFragment newInstance(int sectionNumber, boolean forceRefresh) {
        AllZopsFragment fragment = new AllZopsFragment();
        Bundle args = new Bundle();

        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putBoolean(ARG_FORCE, forceRefresh);

        fragment.setArguments(args);

        return fragment;
    }

    public AllZopsFragment() {
        types = new ArrayList<MobileZop>() { };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MainActivity activity = (MainActivity)getActivity();
        activity.getMenuInflater().inflate(R.menu.menu_all, menu);
        activity.restoreActionBar();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        boolean force = getArguments().getBoolean(ARG_FORCE, false);
        MainActivity activity = (MainActivity)getActivity();

        if (force || activity.DataVersion == 0) {
            setZops(true);
        }
        else {
            setZops(false);
        }
    }

    private void setZops(boolean force) {
        Activity activity = getActivity();
        adapter = new AllZopsGridViewAdapter(activity, R.id.gridItem, types);

        final SharedPreferences preferences = activity.getPreferences(0);
        final SharedPreferences.Editor editor = preferences.edit();

        if(!preferences.contains(StorageKeys.ZOPCATEGORY_KEY)){
            InputStream is = getResources().openRawResource(R.raw.zops);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            HashMap<String, MobileZop> temp = new HashMap<>();

            types = new ArrayList<>((ArrayList<MobileZop>) new Gson().fromJson(reader, new TypeToken<ArrayList<MobileZop>>() {}.getType()));
            for (MobileZop type: types) {
                String key = type.Type.toString();

                if(!temp.containsKey(key)) {
                    temp.put(key, type);
                }
            }
            types = new ArrayList<>(temp.values());
            if (adapter != null) {
                adapter.setObjects(types);
                adapter.notifyDataSetChanged();
            }

            try {
                FileOutputStream fos = activity.openFileOutput(StorageKeys.ZOPCATEGORY_KEY, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                oos.writeObject(types);
                oos.close();
                fos.close();
                editor.putString(StorageKeys.ZOPCATEGORY_KEY, Integer.toString(types.size()));
                editor.commit();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            if (preferences.contains(StorageKeys.ZOPCATEGORY_KEY)) {
                try {
                    FileInputStream fis = activity.openFileInput(StorageKeys.ZOPCATEGORY_KEY);
                    ObjectInputStream ois = new ObjectInputStream(fis);

                    types = (ArrayList)ois.readObject();
                    ois.close();
                    fis.close();

                    if (adapter != null) {
                        adapter.setObjects(types);
                        adapter.notifyDataSetChanged();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (StreamCorruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            ListenableFuture<JsonElement> result = MainActivity.MobileClient.invokeApi("mobileZop", "GET", new ArrayList<Pair<String, String>>() {{
                add(new Pair<String, String>("count", "true"));
            }});

            Futures.addCallback(result, new FutureCallback<JsonElement>() {

                @Override
                public void onSuccess(JsonElement result) {
                    Activity activity = getActivity();
                    JsonArray typesAsJson = result.getAsJsonArray();
                    if (typesAsJson != null) {
                        types = new Gson().fromJson(result, new TypeToken<ArrayList<MobileZop>>() {
                        }.getType());
                        try {
                            FileOutputStream fos = activity.openFileOutput(StorageKeys.ZOPCATEGORY_KEY, Context.MODE_PRIVATE);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);

                            oos.writeObject(types);
                            oos.close();
                            fos.close();
                            editor.putString(StorageKeys.ZOPCATEGORY_KEY, Integer.toString(types.size()));
                            editor.commit();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (adapter != null) {
                        adapter.setObjects(types);
                        GridView zops = (GridView) activity.findViewById(R.id.gridViewZops);
                        RelativeLayout loader = (RelativeLayout) activity.findViewById(R.id.relativeLayoutLoader);
                        loader.setVisibility(View.INVISIBLE);
                        zops.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    Activity activity = getActivity();
                    if (activity != null && types.size() == 0) {
                        GridView zops = (GridView) activity.findViewById(R.id.gridViewZops);
                        RelativeLayout loader = (RelativeLayout) activity.findViewById(R.id.relativeLayoutLoader);
                        zops.setVisibility(View.INVISIBLE);
                        loader.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.setObjects(types);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_zops, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridViewZops);

        if (adapter.getCount() > 0) {
            rootView.findViewById(R.id.relativeLayoutLoader).setVisibility(View.INVISIBLE);
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MobileZop item = (MobileZop)parent.getAdapter().getItem(position);
                ((MainActivity)getActivity()).nextMenu(TypedZopsFragment.newInstance(item.Type, false), true, 10);
            }
        });
        gridView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}