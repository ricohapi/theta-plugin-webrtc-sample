/**
 * Constants definition for HTTP request
 */
const READYSTATE_COMPLETED = 4;
const HTTP_STATUS_OK = 200;
const POST = 'POST';
const CONTENT_TYPE = 'content-Type';
const TYPE_JSON = 'application/json';
const COMMAND = 'webrtc/commands/execute';

/**
 * Command name
 */
const TAKE_PICTURE = 'camera.takePicture';
const GET_SHOOTING_STATUS = 'camera.getShootingStatus';
const COMMANDS_EXECUTE = 'camera.commands.execute';
const SET_OPTIONS = 'camera.setOptions';
const GET_OPTIONS = 'camera.getOptions';
const START_LIVE_PREVIEW = 'camera.startLivePreview';
const STOP_LIVE_PREVIEW = 'camera.stopLivePreview';
const SET_SETTINGS = 'camera.setSettings';
const GET_SETTINGS = 'camera.getSettings';
const GET_STATUS = 'camera.getStatus';

/**
 * Camera option name list definition
 */
const OPTION_NAME_LIST = [
    'exposureProgram',
    'iso',
    'shutterSpeed',
    'whiteBalance',
    '_colorTemperature',
    'exposureCompensation',
    '_filter'
];

const SETTING_NAME_LIST = [
    '_shutterVolume',
    'fileFormat',
    'remainingPictures'
];

/**
 * Send 'startLivePreview' command with HTTP request
 */
function startLivePreview(videoSize) {
    let command = {};
    let option = new Object();
    command.name = 'camera.startLivePreview';
    option.videoSize = videoSize;
    command.parameters = { options : option };
    console.log(command.name);
    return new Promise(function(resolve) {
        let xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.onreadystatechange = function() {
            if (this.readyState === READYSTATE_COMPLETED) {
                if (this.status === HTTP_STATUS_OK) {
                    console.log(this.responseText);
                    resolve(this.responseText);
                } else {
                    console.log('start live preview failed');
                    resolve('Failed. HttpStatus: ' + this.statusText);
                }
            }
        };
        xmlHttpRequest.open(POST, COMMAND, true);
        xmlHttpRequest.setRequestHeader(CONTENT_TYPE, TYPE_JSON);
        xmlHttpRequest.send(JSON.stringify(command));
    });
}

/**
 * Send 'stopLivePreview' command with HTTP request
 */
function stopLivePreview() {
    let command = {};
    command.name = 'camera.stopLivePreview';
    console.log(command.name);
    return new Promise(function(resolve) {
        let xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.onreadystatechange = function() {
            if (this.readyState === READYSTATE_COMPLETED) {
                if (this.status === HTTP_STATUS_OK) {
                    console.log(this.responseText);
                    resolve(this.responseText);
                } else {
                    console.log('stop live preview failed');
                    resolve('Failed. HttpStatus: ' + this.statusText);
                }
            }
        };
        xmlHttpRequest.open(POST, COMMAND, true);
        xmlHttpRequest.setRequestHeader(CONTENT_TYPE, TYPE_JSON);
        xmlHttpRequest.send(JSON.stringify(command));
    });
}

/**
 * Send 'takePicture' command with HTTP request
 */
function takePictureInternal() {
    let command = {};
    command.name = TAKE_PICTURE;
    console.log(command.name);
    return new Promise(function(resolve) {
        let xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.onreadystatechange = function() {
            if (this.readyState === READYSTATE_COMPLETED) {
                if (this.status === HTTP_STATUS_OK) {
                    console.log(this.responseText);
                    resolve(this.responseText);
                } else {
                    console.log('takePicture failed');
                    resolve('Failed. HttpStatus: ' + this.statusText);
                }
            }
        }
        xmlHttpRequest.open(POST, COMMAND, true);
        xmlHttpRequest.setRequestHeader(CONTENT_TYPE, TYPE_JSON);
        xmlHttpRequest.send(JSON.stringify(command));
    });
}

/**
 * Send 'getShootingStatus' command  with HTTP request
 */
function getShootingStatus() {
    let command = {};
    command.name = GET_SHOOTING_STATUS;
    console.log(command.name);
    return new Promise(function(resolve) {
        let xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.onreadystatechange = function() {
            if (this.readyState === READYSTATE_COMPLETED) {
                if (this.status === HTTP_STATUS_OK) {
                    console.log(this.responseText);
                    resolve(this.responseText);
                } else {
                    console.log('get state failed');
                    resolve('Failed. HttpStatus: ' + this.statusText);
                }
            }
        };
        xmlHttpRequest.open(POST, COMMAND, true);
        xmlHttpRequest.setRequestHeader(CONTENT_TYPE, TYPE_JSON);
        xmlHttpRequest.send(JSON.stringify(command));
    });
}

/**
 * Send 'setOptions' command with HTTP request
 */
function setOptionsInternal(cameraParams) {
    let command = {};
    command.name = SET_OPTIONS;
    command.parameters = { options : JSON.parse(JSON.stringify(cameraParams)) };
    console.log(command.name);
    return new Promise(function(resolve) {
        let xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.onreadystatechange = function() {
            if (this.readyState === READYSTATE_COMPLETED) {
                if (this.status === HTTP_STATUS_OK) {
                    console.log(this.responseText);
                    resolve(this.responseText);
                } else {
                    console.log('set camera options failed');
                    resolve('Failed. HttpStatus: ' + this.statusText);
                }
            }
        };
        xmlHttpRequest.open(POST, COMMAND, true);
        xmlHttpRequest.setRequestHeader(CONTENT_TYPE, TYPE_JSON);
        xmlHttpRequest.send(JSON.stringify(command));
    });
}

/**
 * Send 'getOptions' command with HTTP request
 */
function getOptionsInternal() {
    let command = {};
    command.name = GET_OPTIONS;
    command.parameters = { optionNames : OPTION_NAME_LIST };
    console.log(command.name);
    return new Promise(function(resolve) {
        let xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.onreadystatechange = function() {
            if (this.readyState === READYSTATE_COMPLETED) {
                if (this.status === HTTP_STATUS_OK) {
                    console.log(this.responseText);
                    resolve(this.responseText);
                } else {
                    console.log('get camera options failed');
                    resolve('Failed. HttpStatus: ' + this.statusText);
                }
            }
        };
        xmlHttpRequest.open(POST, COMMAND, true);
        xmlHttpRequest.setRequestHeader(CONTENT_TYPE, TYPE_JSON);
        xmlHttpRequest.send(JSON.stringify(command));
    });
}

/**
 * Send 'setSettings' command with HTTP request
 */
function setSettings(settings) {
    let command = {};
    command.name = SET_SETTINGS;
    command.parameters = { options : JSON.parse(JSON.stringify(settings)) };
    console.log(command.name);
    return new Promise(function(resolve) {
        let xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.onreadystatechange = function() {
            if (this.readyState === READYSTATE_COMPLETED) {
                if (this.status === HTTP_STATUS_OK) {
                    console.log(this.responseText);
                    resolve(this.responseText);
                } else {
                    console.log('set camera setting failed');
                    resolve('Failed. HttpStatus: ' + this.statusText);
                }
            }
        };
        xmlHttpRequest.open(POST, COMMAND, true);
        xmlHttpRequest.setRequestHeader(CONTENT_TYPE, TYPE_JSON);
        xmlHttpRequest.send(JSON.stringify(command));
    });
}

/**
 * Send 'getSettings' command with HTTP request
 */
function getSettings() {
    let command = {};
    command.name = GET_SETTINGS;
    command.parameters = { optionNames : SETTING_NAME_LIST };
    console.log(command.name);
    return new Promise(function(resolve) {
        let xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.onreadystatechange = function() {
            if (this.readyState === READYSTATE_COMPLETED) {
                if (this.status === HTTP_STATUS_OK) {
                    console.log(this.responseText);
                    resolve(this.responseText);
                } else {
                    console.log('get camera setting failed');
                    resolve('Failed. HttpStatus: ' + this.statusText);
                }
            }
        };
        xmlHttpRequest.open(POST, COMMAND, true);
        xmlHttpRequest.setRequestHeader(CONTENT_TYPE, TYPE_JSON);
        xmlHttpRequest.send(JSON.stringify(command));
    });
}

/**
 * Send 'getStatus' command with HTTP request
 */
function getStatus() {
    let command = {};
    command.name = GET_STATUS;
    console.log(command.name);
    return new Promise(function(resolve) {
        let xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.onreadystatechange = function() {
            if (this.readyState === READYSTATE_COMPLETED) {
                if (this.status === HTTP_STATUS_OK) {
                    console.log(this.responseText);
                    resolve(this.responseText);
                } else {
                    console.log('get device state failed');
                    resolve('Failed. HttpStatus: ' + this.statusText);
                }
            }
        };
        xmlHttpRequest.open(POST, COMMAND, true);
        xmlHttpRequest.setRequestHeader(CONTENT_TYPE, TYPE_JSON);
        xmlHttpRequest.send(JSON.stringify(command));
    });
}

//
// ---------------------- Utility -----------------------
//
/**
 * Sleep
 */
function sleep(milisec) {
    return new Promise(function(resolve) {
        setTimeout(resolve, milisec);
    });
}
