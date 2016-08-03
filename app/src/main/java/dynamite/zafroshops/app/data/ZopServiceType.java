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

package dynamite.zafroshops.app.data;

import com.google.common.collect.Ordering;
import com.google.gson.annotations.SerializedName;

public enum ZopServiceType {
    @SerializedName("shop")
    Shop,

    @SerializedName("bar")
    Bar,

    @SerializedName("barberShop")
    BarberShop,

    @SerializedName("cafe")
    Cafe,

    @SerializedName("callBox")
    CallBox,

    @SerializedName("nightClub")
    NightClub,

    @SerializedName("cosmetics")
    Cosmetics,

    @SerializedName("catering")
    Catering;

    public static String[] names() {
        ZopServiceType[] zt = values();
        String[] names = new String[zt.length];

        for (int i = 0; i < zt.length; i++) {
            names[i] = zt[i].toString();
        }

        return names;
    }

    @Override
    public String toString() {
        return name().replaceAll("([A-Z])", " $1").trim();
    }

    public String getText()
    {
        return name().toLowerCase();
    }
}
