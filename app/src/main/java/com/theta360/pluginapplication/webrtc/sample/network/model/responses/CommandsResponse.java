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

package com.theta360.pluginapplication.webrtc.sample.network.model.responses;

import com.google.gson.annotations.SerializedName;
import com.theta360.pluginapplication.webrtc.sample.network.model.commands.CommandsName;
import com.theta360.pluginapplication.webrtc.sample.network.model.objects.ErrorObject;
import com.theta360.pluginapplication.webrtc.sample.network.model.objects.ProgressObject;
import com.theta360.pluginapplication.webrtc.sample.network.model.values.State;

/**
 * CommandsResponse
 */
public class CommandsResponse {
    @SerializedName("name")
    String mName;

    @SerializedName("state")
    String mState;

    @SerializedName("id")
    String mId;

    @SerializedName("error")
    ErrorObject mError;

    @SerializedName("progress")
    ProgressObject mProgress;

    public CommandsResponse(CommandsName commandsName, State state) {
        this.mName = commandsName.toString();
        this.mState = state.toString();
    }

    public String getName() {
        return this.mName;
    }

    public String getState() {
        return this.mState;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public ErrorObject getError() {
        return this.mError;
    }

    public void setError(ErrorObject error) {
        this.mError = error;
    }

    public ProgressObject getProgress() {
        return this.mProgress;
    }

    public void setProgress(ProgressObject progress) {
        this.mProgress = progress;
    }
}
