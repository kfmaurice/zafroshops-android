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

package dynamite.zafroshops.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.data.MobileZop;
import dynamite.zafroshops.app.data.ZopType;

public class AllZopsGridViewAdapter extends ArrayAdapter<MobileZop> {

    private ArrayList<MobileZop> objects;
    private Ordering<MobileZop> ordering;

    public AllZopsGridViewAdapter(Context context, int resource, ArrayList<MobileZop> objects) {
        super(context, resource, objects);

        this.objects = objects;
        ordering = Ordering.from(new Comparator<MobileZop>() {
            @Override
            public int compare(MobileZop zop, MobileZop z1) {
                if (zop.Type.getText().compareTo(ZopType.Shop.getText()) == 0) {
                    return -1;
                } else if (z1.Type.getText().compareTo(ZopType.Shop.getText()) == 0) {
                    return 1;
                } else {
                    return zop.Type.getText().compareTo(z1.Type.getText());
                }
            }
        });

        Collections.sort(objects, ordering);
    }

    class ViewHolder {
        ImageView icon;
        TextView text;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public MobileZop getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            Context context = this.getContext();
            LayoutInflater la = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = la.inflate(R.layout.grid_item, null);

            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.imageViewGrid);
            viewHolder.text = (TextView) convertView.findViewById(R.id.textViewGrid);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if (objects != null) {
            MobileZop item = this.objects.get(position);
            viewHolder.text.setText(item.Type.toString());
            try {
                viewHolder.icon.setImageResource(R.drawable.class.getField(item.Type.getText()).getInt(null));
            } catch (IllegalAccessException ignored) {

            } catch (NoSuchFieldException e) {
                viewHolder.icon.setImageResource(R.drawable.shop);
            }
        }

        return convertView;
    }

    public void setObjects(ArrayList<MobileZop> objects) {
        if (this.objects == null) {
            this.objects = new ArrayList<>();
        }
        else {
            this.objects.clear();
        }
        if (objects != null) {
            this.objects.addAll(objects);
            Collections.sort(this.objects, ordering);
        }
    }
}
