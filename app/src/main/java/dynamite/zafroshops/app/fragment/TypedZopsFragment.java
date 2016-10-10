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
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.Collections;
import java.util.Comparator;

import dynamite.zafroshops.app.adapter.TypedZopListViewAdapter;
import dynamite.zafroshops.app.MainActivity;
import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.data.MobileZop;
import dynamite.zafroshops.app.data.StorageKeys;
import dynamite.zafroshops.app.data.ZopType;

public class TypedZopsFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_ZOP_TYPE = "zop_type";
    private static final String ARG_FORCE = "force";
    private boolean force;

    private TypedZopListViewAdapter adapter;
    private int dataVersion;
    public ArrayList<MobileZop> typedZops;
    public static ZopType zopType;

    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static TypedZopsFragment newInstance(ZopType type, boolean forceRefresh) {
        TypedZopsFragment fragment = new TypedZopsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ZOP_TYPE, type);
        args.putBoolean(ARG_FORCE, forceRefresh);

        fragment.setArguments(args);
        return fragment;
    }

    public TypedZopsFragment() {
        typedZops = new ArrayList<MobileZop>() { };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final MainActivity activity = (MainActivity)getActivity();
        activity.getMenuInflater().inflate(R.menu.menu_typed, menu);
        activity.restoreActionBar();

        // swipe to refresh
        swipeRefreshLayout = (SwipeRefreshLayout) activity.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                activity.refresh();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        force = getArguments().getBoolean(ARG_FORCE, false);
        MainActivity activity = (MainActivity)getActivity();

        if ((ZopType) getArguments().get(ARG_ZOP_TYPE) != null) {
            ((MainActivity)getActivity()).Counts.put((ZopType) getArguments().get(ARG_ZOP_TYPE), 0);
        }

        if (force) {
            setZops(true);
        }
        else {
            setZops(false);
            if (activity.DataVersion != 0 && activity.Versions.get(zopType) != null && activity.Versions.get(zopType) < activity.DataVersion) {
                setZops(true);
            }
        }
    }

    private void setZops(final boolean force) {
        Activity activity = getActivity();
        adapter = new TypedZopListViewAdapter(getActivity(), R.id.listViewItem, typedZops);
        final SharedPreferences preferences = activity.getPreferences(0);

        final SharedPreferences.Editor editor = preferences.edit();
        final ZopType type = (ZopType) getArguments().get(ARG_ZOP_TYPE);
        final String key = StorageKeys.ZOPS_KEY + type.toString();

        zopType = type;

        if(!preferences.contains(key)) {
            InputStream is = getResources().openRawResource(R.raw.zops);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            typedZops = new ArrayList<>(Collections2.filter((ArrayList<MobileZop>) new Gson().fromJson(reader, new TypeToken<ArrayList<MobileZop>>() {
            }.getType()), new Predicate<MobileZop>() {
                @Override
                public boolean apply(MobileZop input) {
                    return input.Type == type && (!preferences.contains(StorageKeys.COUNTRY_KEY) || preferences.getString(StorageKeys.COUNTRY_KEY, "").equals("") || input.CountryID.equals(preferences.getString(StorageKeys.COUNTRY_KEY, "")));
                }
            }));

            try {
                FileOutputStream fos = activity.openFileOutput(key, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                int count = typedZops.size();

                oos.writeObject(typedZops);
                oos.close();
                fos.close();
                editor.putString(key, Integer.toString(count));
                editor.commit();
                setCount(count);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if (preferences.contains(key)) {
            try {
                FileInputStream fis = activity.openFileInput(key);
                ObjectInputStream ois = new ObjectInputStream(fis);

                ArrayList<MobileZop> temp = (ArrayList)ois.readObject();
                typedZops = new ArrayList<>(Collections2.filter(temp, new Predicate<MobileZop>() {
                    @Override
                    public boolean apply(MobileZop input) {
                        return input.Type == type && (!preferences.contains(StorageKeys.COUNTRY_KEY) || preferences.getString(StorageKeys.COUNTRY_KEY, "").equals("") || input.CountryID.equals(preferences.getString(StorageKeys.COUNTRY_KEY, "")));
                    }
                }));
                ois.close();
                fis.close();
                setCount(typedZops.size());
                if (adapter != null) {
                    adapter.setObjects(typedZops);
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

        if(force) {
            final ArrayList<Pair<String, String>> parameters;
            if (MainActivity.LastLocation != null) {
                parameters = new ArrayList<Pair<String, String>>() {{
                    add(new Pair<>("latitude", Double.toString(MainActivity.LastLocation.Latitude)));
                    add(new Pair<>("longitude", Double.toString(MainActivity.LastLocation.Longitude)));
                    add(new Pair<>("type", type.getText()));
                }};
            } else {
                parameters = new ArrayList<Pair<String, String>>() {{
                    add(new Pair<>("type", type.getText()));
                }};
            }

            ListenableFuture<JsonElement> result = MainActivity.MobileClient.invokeApi("mobileZop", "GET", parameters);

            Futures.addCallback(result, new FutureCallback<JsonElement>() {
                Activity activity = getActivity();

                @Override
                public void onSuccess(JsonElement result) {
                    JsonArray typesAsJson = result.getAsJsonArray();
                    if (typesAsJson != null) {
                        ArrayList<MobileZop> temp = new Gson().fromJson(result, new TypeToken<ArrayList<MobileZop>>() {
                        }.getType());

                        int max = Collections.max(temp, new Comparator<MobileZop>() {
                            @Override
                            public int compare(MobileZop lhs, MobileZop rhs) {
                                if (lhs.DataVersion < rhs.DataVersion) {
                                    return -1;
                                } else if (lhs.DataVersion > rhs.DataVersion) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            }
                        }).DataVersion;
                        ((MainActivity) activity).Versions.put(zopType, max);

                        try {
                            FileOutputStream fos = activity.openFileOutput(key, Context.MODE_PRIVATE);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            int count = typedZops.size();

                            typedZops = new ArrayList<>(Collections2.filter(temp, new Predicate<MobileZop>() {
                                @Override
                                public boolean apply(MobileZop input) {
                                    return input.Type == type && (!preferences.contains(StorageKeys.COUNTRY_KEY) || preferences.getString(StorageKeys.COUNTRY_KEY, "").equals("") || input.CountryID.equals(preferences.getString(StorageKeys.COUNTRY_KEY, "")));
                                }
                            }));
                            oos.writeObject(typedZops);
                            oos.close();
                            fos.close();
                            editor.putString(key, Integer.toString(count));
                            editor.commit();
                            setCount(count);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (adapter != null) {
                        adapter.setObjects(typedZops);
                        resetVisibility(false);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    ListView zops = (ListView) activity.findViewById(R.id.listViewZops);
                    RelativeLayout loader = (RelativeLayout) activity.findViewById(R.id.relativeLayoutLoader);
                    LinearLayout noZops = (LinearLayout) activity.findViewById(R.id.noZops);

                    resetVisibility(zops, noZops, loader, false);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.setObjects(typedZops);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_typed_zops, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listViewZops);

        resetVisibility(listView, (LinearLayout) rootView.findViewById(R.id.noZops), (RelativeLayout) rootView.findViewById(R.id.relativeLayoutLoader), force);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MobileZop item = (MobileZop)parent.getAdapter().getItem(position);
                MainActivity activity = (MainActivity)getActivity();

                if (activity.AdsActive && activity.Interstitial.isLoaded()) {
                    activity.Interstitial.show();
                }
                activity.setCurrentItem(item.id, item);
                activity.nextMenu(ZopItemFragment.newInstance(item.id), true, 100);
            }
        });

        listView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private void setCount(int count) {
        if (count > 0) {
            ((MainActivity)getActivity()).Counts.put(zopType, count);
        }
    }

    private void resetVisibility(boolean showLoader) {
        Activity activity = getActivity();
        ListView zops = (ListView) activity.findViewById(R.id.listViewZops);
        LinearLayout noZops = (LinearLayout) activity.findViewById(R.id.noZops);
        RelativeLayout loader = (RelativeLayout) activity.findViewById(R.id.relativeLayoutLoader);

        resetVisibility(zops, noZops, loader, showLoader);
    }

    private void resetVisibility(ListView zops, LinearLayout noZops, RelativeLayout loader, boolean showLoader) {
        Activity activity = getActivity();
        MainActivity mainActivity = (MainActivity)activity;

        if (typedZops.size() == 0 && mainActivity.Counts.get(zopType) == 0) {
            noZops.setVisibility(View.VISIBLE);
            zops.setVisibility(View.INVISIBLE);
        } else {
            noZops.setVisibility(View.INVISIBLE);
            zops.setVisibility(View.VISIBLE);
        }

        if(showLoader) {
            zops.setVisibility(View.INVISIBLE);
            noZops.setVisibility(View.INVISIBLE);
            loader.setVisibility(View.VISIBLE);
        }
        else {
            loader.setVisibility(View.INVISIBLE);
        }
    }
}
