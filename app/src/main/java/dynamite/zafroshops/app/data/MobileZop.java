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

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MobileZop implements Serializable {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String Name;

    @SerializedName("location")
    public Location Location;

    @SerializedName("countryID")
    public String CountryID;

    @SerializedName("city")
    public String City;

    @SerializedName("street")
    public String Street;

    @SerializedName("streetNumber")
    public String StreetNumber;

    @SerializedName("phoneNumber")
    public String PhoneNumber;

    @SerializedName("details")
    public String Details;

    @SerializedName("type")
    public ZopType Type;

    @SerializedName("origin")
    public ZopOrigin Origin;

    @SerializedName("countryName")
    public String CountryName;

    @SerializedName("countryPhoneCode")
    public String CountryPhoneCode;

    @SerializedName("rating")
    public int Rating;

    @SerializedName("count")
    public int Count;

    @SerializedName("distance")
    public double Distance;

    @SerializedName("dataVersion")
    public int DataVersion;
}
