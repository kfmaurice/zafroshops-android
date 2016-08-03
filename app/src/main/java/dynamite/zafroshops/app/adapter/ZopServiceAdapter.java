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
import android.widget.CheckBox;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import dynamite.zafroshops.app.R;
import dynamite.zafroshops.app.data.ZopServiceType;

public class ZopServiceAdapter extends ArrayAdapter<ZopServiceType> {

    public ZopServiceType[] objects;
    private ArrayList<ZopServiceType> selection;

    public ZopServiceAdapter(Context context, int resource, ArrayList<ZopServiceType> selection) {
        super(context, resource);

        this.objects = ZopServiceType.values();
        final Ordering<ZopServiceType> ordering  = Ordering.from(new Comparator<ZopServiceType>() {
            @Override
            public int compare(ZopServiceType zopServiceType, ZopServiceType t1) {
                if (zopServiceType.getText().compareTo(ZopServiceType.Shop.getText()) == 0){
                    return -1;
                }
                else if (t1.getText().compareTo(ZopServiceType.Shop.getText()) == 0) {
                    return 1;
                }
                else {
                    return zopServiceType.getText().compareTo(t1.getText());
                }
            }
        });

        Arrays.sort(objects, ordering);
        this.selection = selection;
    }

    class ViewHolder {
        CheckBox checkbox;
    }

    @Override
    public int getCount() {
        return objects.length;
    }

    @Override
    public ZopServiceType getItem(int position) {
        return objects[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            Context context = this.getContext();
            LayoutInflater la = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = la.inflate(R.layout.service_item, null);

            viewHolder = new ViewHolder();
            viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.serviceCheckBox);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if (objects != null) {
            final ZopServiceType item = this.getItem(position);

            viewHolder.checkbox.setText(item.toString());
            viewHolder.checkbox.setChecked(Iterables.contains(selection, item));
            viewHolder.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox checkBox = (CheckBox) view;
                    if (checkBox.isChecked()) {
                        selection.add(item);
                    } else {
                        selection.remove(item);
                    }
                }
            });
        }

        return convertView;
    }
}
