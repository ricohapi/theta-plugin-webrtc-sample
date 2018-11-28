/**
 * Copyright 2018 Ricoh Company, Ltd.
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
 */

package com.theta360.pluginapplication.webrtc.sample.network.model.requests;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.theta360.pluginapplication.webrtc.sample.network.model.commands.CommandsName;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * CommandsRequest
 */
public class CommandsRequest {
    @SerializedName("name")
    String mName;

    @SerializedName("parameters")
    Object mParameters;

    private CommandsRequest(CommandsName commandsName) {
        this.mName = commandsName.toString();
    }

    private CommandsRequest(CommandsName commandsName, Object parameters) {
        this.mName = commandsName.toString();
        this.mParameters = parameters;
    }

    public CommandsName getCommandsName() {
        return CommandsName.getValue(mName);
    }

    public JSONObject getCommandsParameters() {
        JSONObject json = null;
        try {
            json = new JSONObject(new GsonBuilder().create().toJson(this.mParameters));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  json;
    }

    public void setCommandParameters(JSONObject options) {
        String name = this.mName;
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("parameters", options);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.mParameters = new GsonBuilder().create().fromJson(parameters.toString(), CommandsRequest.class);
        this.mName = name;
    }
}
