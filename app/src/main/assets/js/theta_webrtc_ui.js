/**
 * Action sheet definition
 */
let actions;
// English
let actionsEn = {
    // Exposure mode sheet
    mode: {
        name: 'mode',
        items: [
            { icon: 'mode-auto', title: 'Auto', val: 'auto' },
            { icon: 'mode-shutter', title: 'Shutter priority', val: 'shutter' },
            { icon: 'mode-iso', title: 'ISO priority', val: 'iso' },
            { icon: 'mode-manual', title: 'Manual', val: 'manual' }
        ],
        checked: 'auto'
    },
    volume: {
        name: 'volume',
        items: [
            { icon: '', title: 'Loud ', val: 100 },
            { icon: '', title: 'Medium', val: 67 },
            { icon: '', title: 'Small', val: 33 },
            { icon: '', title: 'OFF', val: 0 }
        ],
        checked: -1     // Don't hold checked value
    }
}
// Japanese
let actionsJp = {
    // Exposure mode sheet
    mode: {
        name: 'mode',
        items: [
            { icon: 'mode-auto', title: 'オート', val: 'auto' },
            { icon: 'mode-shutter', title: 'シャッター優先', val: 'shutter' },
            { icon: 'mode-iso', title: 'ISO優先', val: 'iso' },
            { icon: 'mode-manual', title: 'マニュアル', val: 'manual' }
        ],
        checked: 'auto'
    },
    volume: {
        name: 'volume',
        items: [
            { icon: '', title: '大', val: 100 },
            { icon: '', title: '中', val: 67 },
            { icon: '', title: '小', val: 33 },
            { icon: '', title: 'OFF', val: 0 }
        ],
        checked: -1     // Don't hold checked value
    }
}

/**
 * Exposure program index value definition
 */
const EXPOSURE_PROGRAM_IDX = {
    auto: '2',
    shutter: '4',
    iso: '9',
    manual: '1'
};

/**
 * ISO support value definition
 */
const ISO_SUPPORT = [ 64, 80, 100, 125, 160, 200, 250, 320, 400, 500, 640, 800, 1000, 1250, 1600, 2000, 2500, 3200 ];

/**
 * Shutter speed support value definition (Manual: 1/25000 - 60)
 */
const MANUAL_SHUTTERSPEED_SUPPORT = [
    { val: 60, text: '60' },
    { val: 30, text: '30' },
    { val: 25, text: '25' },
    { val: 20, text: '20' },
    { val: 15, text: '15' },
    { val: 13, text: '13' },
    { val: 10, text: '10' },
    { val: 8, text: '8' },
    { val: 6, text: '6' },
    { val: 5, text: '5' },
    { val: 4, text: '4' },
    { val: 3.2, text: '3.2' },
    { val: 2.5, text: '2.5' },
    { val: 2, text: '2' },
    { val: 1.6, text: '1.6' },
    { val: 1.3, text: '1.3' },
    { val: 1, text: '1' },
    { val: 0.76923076, text: '1/1.3' },
    { val: 0.625, text: '1/1.6' },
    { val: 0.5, text: '1/2' },
    { val: 0.4, text: '1/2.5' },
    { val: 0.33333333, text: '1/3' },
    { val: 0.25, text: '1/4' },
    { val: 0.2, text: '1/5' },
    { val: 0.16666666, text: '1/6' },
    { val: 0.125, text: '1/8' },
    { val: 0.1, text: '1/10' },
    { val: 0.07692307, text: '1/13' },
    { val: 0.06666666, text: '1/15' },
    { val: 0.05, text: '1/20' },
    { val: 0.04, text: '1/25' },
    { val: 0.03333333, text: '1/30' },
    { val: 0.025, text: '1/40' },
    { val: 0.02, text: '1/50' },
    { val: 0.01666666, text: '1/60' },
    { val: 0.0125, text: '1/80' },
    { val: 0.01, text: '1/100' },
    { val: 0.008, text: '1/125' },
    { val: 0.00625, text: '1/160' },
    { val: 0.005, text: '1/200' },
    { val: 0.004, text: '1/250' },
    { val: 0.003125, text: '1/320' },
    { val: 0.0025, text: '1/400' },
    { val: 0.002, text: '1/500' },
    { val: 0.0015625, text: '1/640' },
    { val: 0.00125, text: '1/800' },
    { val: 0.001, text: '1/1000' },
    { val: 0.0008, text: '1/1250' },
    { val: 0.000625, text: '1/1600' },
    { val: 0.0005, text: '1/2000' },
    { val: 0.0004, text: '1/2500' },
    { val: 0.0003125, text: '1/3200' },
    { val: 0.00025, text: '1/4000' },
    { val: 0.0002, text: '1/5000' },
    { val: 0.00015625, text: '1/6400' },
    { val: 0.000125, text: '1/8000' },
    { val: 0.0001, text: '1/10000' },
    { val: 0.00008, text: '1/12500' },
    { val: 0.0000625, text: '1/16000' },
    { val: 0.00005, text: '1/20000' },
    { val: 0.00004, text: '1/25000' }
];

/**
 * Shutter speed support value definition (Shutter speed priority: 1/25000 - 1/8)
 */
const SHUTTERSPEED_SUPPORT = [
    { val: 0.125, text: '1/8' },
    { val: 0.1, text: '1/10' },
    { val: 0.07692307, text: '1/13' },
    { val: 0.06666666, text: '1/15' },
    { val: 0.05, text: '1/20' },
    { val: 0.04, text: '1/25' },
    { val: 0.03333333, text: '1/30' },
    { val: 0.025, text: '1/40' },
    { val: 0.02, text: '1/50' },
    { val: 0.01666666, text: '1/60' },
    { val: 0.0125, text: '1/80' },
    { val: 0.01, text: '1/100' },
    { val: 0.008, text: '1/125' },
    { val: 0.00625, text: '1/160' },
    { val: 0.005, text: '1/200' },
    { val: 0.004, text: '1/250' },
    { val: 0.003125, text: '1/320' },
    { val: 0.0025, text: '1/400' },
    { val: 0.002, text: '1/500' },
    { val: 0.0015625, text: '1/640' },
    { val: 0.00125, text: '1/800' },
    { val: 0.001, text: '1/1000' },
    { val: 0.0008, text: '1/1250' },
    { val: 0.000625, text: '1/1600' },
    { val: 0.0005, text: '1/2000' },
    { val: 0.0004, text: '1/2500' },
    { val: 0.0003125, text: '1/3200' },
    { val: 0.00025, text: '1/4000' },
    { val: 0.0002, text: '1/5000' },
    { val: 0.00015625, text: '1/6400' },
    { val: 0.000125, text: '1/8000' },
    { val: 0.0001, text: '1/10000' },
    { val: 0.00008, text: '1/12500' },
    { val: 0.0000625, text: '1/16000' },
    { val: 0.00005, text: '1/20000' },
    { val: 0.00004, text: '1/25000' }
];

/**
 * Exposure compensation support value definition
 */
const EXPOSURE_COMPENSATION_SUPPORT = [ -2, -1.7, -1.3, -1, -0.7, -0.3, 0, 0.3, 0.7, 1, 1.3, 1.7, 2 ];

/**
 * White balance mode support value definition
 */
const WHITE_BALANCE_SUPPORT = [
    { val: 'auto', text: 'Auto' ,icon: 'icon--wb_auto' },
    { val: 'daylight', text: 'Outdoor' ,icon: 'icon--wb_sun-current' },
    { val: 'shade', text: 'Shade' ,icon: 'icon--wb_shade-current' },
    { val: 'cloudy-daylight', text: 'Cloudy' ,icon: 'icon--wb_cloud-current' },
    { val: 'incandescent', text: 'Incandescent light 1' ,icon: 'icon--wb_inc_1' },
    { val: '_warmWhiteFluorescent', text: 'Incandescent light 2' ,icon: 'icon--wb_inc_2' },
    { val: '_dayLightFluorescent', text: 'Daylight color fluorescent light' ,icon: 'icon--wb_fluorescent-d-current' },
    { val: '_dayWhiteFluorescent', text: 'Natural white fluorescent light' ,icon: 'icon--wb_fluorescent-n-current' },
    { val: 'fluorescent', text: 'White fluorescent light' ,icon: 'icon--wb_fluorescent-w-current' },
    { val: '_bulbFluorescent', text: 'Light bulb color fluorescent light' ,icon: 'icon--wb_fluorescent-l-current' }
];
const WHITE_BALANCE_SUPPORT_COLOR_TEMPERATURE_VAL = '_colorTemperature';    // White balance mode 'Specify color temperature'

/**
 * Color temperature support value definition
 */
const WHITE_BALANCE_COLOR_TEMPERATURE_SUPPORT = [
    2500, 2600, 2700, 2800, 2900, 3000, 3100, 3200, 3300, 3400, 3500, 3600, 3700, 3800, 3900, 4000, 4100, 4200, 4300,
    4400, 4500, 4600, 4700, 4800, 4900, 5000, 5100, 5200, 5300, 5400, 5500, 5600, 5700, 5800, 5900, 6000, 6100, 6200,
    6300, 6400, 6500, 6600, 6700, 6800, 6900, 7000, 7100, 7200, 7300, 7400, 7500, 7600, 7700, 7800, 7900, 8000, 8100,
    8200, 8300, 8400, 8500, 8600, 8700, 8800, 8900, 9000, 9100, 9200, 9300, 9400, 9500, 9600, 9700, 9800, 9900, 10000
];

/**
 * Option (filter) support value definition
 */
let optionSupport;
const OPTION_SUPPORT_EN = [
    { val: 'off', text: 'OFF' },
    { val: 'Noise Reduction', text: 'Noise reduct.' },
    { val: 'DR Comp', text: 'DR Compensat.' },
    { val: 'hdr', text: 'HDR Rendering.' }
];
const OPTION_SUPPORT_JP = [
    { val: 'off', text: 'OFF' },
    { val: 'Noise Reduction', text: 'ノイズ低減' },
    { val: 'DR Comp', text: 'DR補正' },
    { val: 'hdr', text: 'HDR合成' }
];

/**
 * Label string definition
 */
let labelStrings;
const LABEL_STRINGS_EN = {
    cancelBtnLabel: 'Cancel',
    settingListTitle: 'Configuration',
    settingListCloseBtnLabel: 'Close',
    settingColorTemperatureLabel: 'Specify color temperature',
    settingImageSizeLabel: 'Image size',
    settingShutterVolumeLabel: 'Shutter volume',
    settingRemainingPicturesLabel: 'Remaining pictures',
    settingRemainingPicturesNumber: 'pictures',
    settingBatteryLevelLabel: 'Battery level'
};
const LABEL_STRINGS_JP = {
    cancelBtnLabel: 'キャンセル',
    settingListTitle: '撮影設定',
    settingListCloseBtnLabel: '完了',
    settingColorTemperatureLabel: '色温度指定',
    settingImageSizeLabel: '画像サイズ',
    settingShutterVolumeLabel: 'シャッター音量',
    settingRemainingPicturesLabel: '残り枚数',
    settingRemainingPicturesNumber: '枚',
    settingBatteryLevelLabel: '電池残量'
};

/**
 * Default value definition
 */
const EXPOSURE_PROGRAM_DEFAULT = 'auto';
const EV_SUPPORT_DEFAULT = 0;
const ISO_SUPPORT_DEFAULT = 100;
const SHUTTERSPEED_SUPPORT_DEFAULT = 0.01666666;
const WB_SUPPORT_DEFAULT = 'auto';
const WBMANUAL_SUPPORT_DEFAULT = 5000;
const OPTION_SUPPORT_DEFAULT = 'off'
const SHUTTER_VOLUME_DEFAULT = 100;

/**
 * Settings
 */
let shootSetting = {
    specifyColorTemp: false,        // Specify color temperature (true: enable, false: disable)
    volume: SHUTTER_VOLUME_DEFAULT  // Shutter volume (Max: 100, OFF: 0)
};

/**
 * Swiper objects
 */
let swipers = {
    ev: null,
    iso: null,
    wb: null,
    colortemp: null,
    shutter: null,
    manual_shutter: null,
    option: null
};

/**
 * Entry point
 * - First executed after HTML loading is completed
 */
$(function() {
    /*
     * Language setting
     */
    initLanguage();

    /*
     * Show controller for exposure mode 'Auto'
     */
    $('#shoot__setting-auto').removeClass('hide');

    /*
     * Initialize detail menus
     */
    initEvMenu();                   // Initialize Exposure compensation menu
    initISOMenu();                  // Initialize ISO menu
    initWBMenu();                   // Initialize White balance menu
    initWBColorTemperatureMenu();   // Initialize Color temperature specified white balance menu
    initShutterSpeedMenu();         // Initialize Shutter speed menu of exposure mode 'Shutter speed priority'
    initManualShutterSpeedMenu();   // Initialize Shutter speed menu of exposure mode 'Manual'
    initOptionMenu();               // Initialize Option menu

    /*
     * Bind handler when clicking menu items
     */
    $('.btn--data').on('click', function(event) {
        event.preventDefault();                                             // Prevent default action

        let setting = $(this).attr('data-setting');
        let position = $(this).attr('data-position');

        if ((setting === 'wb') && shootSetting.specifyColorTemp) {
            setting = 'colortemp';                                        // Target items in the color temperature menu
        }

        if (!$('#' + setting + '_menu').hasClass('show')) {
            hideAllMenu();
            console.log('setting:' + setting);
            console.log('position:' + position);
            $('#' + setting + '_menu').addClass('show');
            $('#' + setting + '_menu').addClass('position' + position);
            updateMenus(setting, true);
        } else {
            hideAllMenu();
        }
    });

    /*
     * Bind handler when clicking exposure mode button
     */
    $('#mode_btn').on('click', function(event) {
        event.preventDefault();

        showActionSheet(actions.mode);
    });

    /*
     * Bind handler when clicking cancel button on action sheet
     */
    $('#cancel_btn').on('click', function(event) {
        event.preventDefault();

        hideActionSheet();
    });

    /*
     * Bind handler when clicking setting button
     */
    $('#setting_btn').on('click', function(event) {
        event.preventDefault();

        showSettingSheet();
    });

    /*
     * Bind handler when clicking close button on setting sheet
     */
    $('#setting_close_btn').on('click', function(event) {
        event.preventDefault();

        hideSettingSheet();
    });

    /*
     * Bind handler when clicking shutter volume buttone on setting sheet
     */
    $('#shutter_volume_btn').on('click', function(event) {
        event.preventDefault();

        showActionSheet(actions.volume);
    });

/* for debug
    $( 'input[name='action']:radio' ).on('change', function(e) {
        console.log('s');
    });
*/
    /*
     * Bind handler when clicking image list button (Currently unused)
     */
    $('#camera_image_btn').on('click', function(event) {
        event.preventDefault();

        showCameraImageList();
    });

    /*
     * Bind handler when clicking back button (Currently unused)
     */
    $('#back_btn').on('click', function(event) {
        event.preventDefault();

        hideCameraImageList();
    });

    /*
     * Bind handler when clicking shoot button
     */
    $('#shutter_btn').on('click', function(event) {
        event.preventDefault();

        changeShootButtonState(btnState);
    });

    /*
     * Bind handler when orientation changed of device
     */
    $(window).on('orientationchange', function() {
        window.location.reload();
    });
});

/**
 * Language setting
 */
function initLanguage() {
    let language = (window.navigator.languages && window.navigator.languages[0]) ||
                window.navigator.language ||
                window.navigator.userLanguage ||
                window.navigator.browserLanguage;

//    language = 'en';  // for debug

    if (language === 'ja' || language === 'ja-JP') {
        actions = actionsJp;
        optionSupport = OPTION_SUPPORT_JP;
        labelStrings = LABEL_STRINGS_JP;
    } else {
        actions = actionsEn;
        optionSupport = OPTION_SUPPORT_EN;
        labelStrings = LABEL_STRINGS_EN;
    }
}

/**
 * Initialize Exposure compensation menu
 */
function initEvMenu() {
    /*
     * Generate menu code and set it to HTML
     */
    let setData = '';
    setData += '<div class="swiper-wrapper">';
    for (let i = 0; i < EXPOSURE_COMPENSATION_SUPPORT.length; i++) {
        setData += '  <label class="scroll_btn swiper-slide">';
        setData += '    <input name="ev" type="radio" value="' + EXPOSURE_COMPENSATION_SUPPORT[i] + '" id="ev' + i + '">';
        setData += '    <i for="ev' + i + '">' + EXPOSURE_COMPENSATION_SUPPORT[i] + '</i>';
        setData += '  </label>';
    }
    setData += '</div>';
    $('#ev_menu_wrap').html(setData);

    /*
     * Select default item
     */
    $('input[name="ev"]:eq(' + EXPOSURE_COMPENSATION_SUPPORT.indexOf(EV_SUPPORT_DEFAULT) + ')').prop('checked', true);
    setEvLabel(EV_SUPPORT_DEFAULT);

    /*
     * Bind handler when clicking item on menu
     */
    $('input[name="ev"]').on('click', function(event) {
        setEvLabel($(event.target).val());
        updateMenus('ev', false);
    });

    /*
     * Hide menu in initial state
     */
    setTimeout(function() {
        $('#ev_menu').removeClass('inithide');
        $('#ev_menu').removeClass('show');
    }, 1000);
}

/**
 * Set EV label to UI
 */
function setEvLabel(val) {
    let htmlvalue = '<p class="btn--data__value">' + val +'</p>';
    $('#evlabel1').html(htmlvalue);
    $('#evlabel1').val(val);
    $('#evlabel2').html(htmlvalue);
    $('#evlabel2').val(val);
    $('#evlabel3').html(htmlvalue);
    $('#evlabel3').val(val);
}

/**
 * Initialize ISO menu
 */
function initISOMenu(){
    /*
     * Generate menu code and set it to HTML
     */
    let setData = '';
    setData += '<div class="swiper-wrapper">';
    for (let i = 0; i < ISO_SUPPORT.length; i++) {
        setData += '  <label class="scroll_btn swiper-slide">';
        setData += '    <input name="iso" type="radio" value="' + ISO_SUPPORT[i] + '" id="iso' + i + '">';
        setData += '    <i for="iso' + i + '">' + ISO_SUPPORT[i] + '</i>';
        setData += '  </label>';
    }
    setData += '</div>';
    $('#iso_menu_wrap').html(setData);

    /*
     * Select default item
     */
    $('input[name="iso"]:eq(' + ISO_SUPPORT.indexOf(ISO_SUPPORT_DEFAULT) + ')').prop('checked', true);
    setISOLabel(ISO_SUPPORT_DEFAULT);

    /*
     * Bind handler when clicking item on menu
     */
    $('input[name="iso"]').on('click',function(event) {
        setISOLabel($(event.target).val());
        updateMenus('iso', false);
    });

    /*
     * Hide menu in initial state
     */
    setTimeout(function() {
        $('#iso_menu').removeClass('inithide');
        $('#iso_menu').removeClass('show');
    }, 1000);
}

/**
 * Set ISO label to UI
 */
function setISOLabel(val) {
    let htmlvalue = '<p class="btn--data__value">' + val +'</p>';
    $('#isolabel2').html(htmlvalue);
    $('#isolabel2').val(val);
    $('#isolabel3').html(htmlvalue);
    $('#isolabel3').val(val);
}

/**
 * Initialize White balance menu
 */
function initWBMenu() {
    /*
     * Generate menu code and set it to HTML
     */
    let setData = '';
    setData += '<div class="swiper-wrapper">';
    for (let i = 0; i < WHITE_BALANCE_SUPPORT.length; i++) {
        setData += '  <label class="scroll_btn wb swiper-slide">';
        setData += '    <input name="wb" type="radio" value="' + WHITE_BALANCE_SUPPORT[i].val + '" id="wb' + i + '">';
        setData += '    <i for="wb' + i + '" class="wbicon' + i + '">' + WHITE_BALANCE_SUPPORT[i].text + '</i>';
        setData += '  </label>';
    }
    setData += '</div>';
    $('#wb_menu_wrap').html(setData);

    /*
     * Select default item
     */
    $('input[name="wb"]:eq(' + getWhiteBalanceMenuIndex(WB_SUPPORT_DEFAULT) + ')').prop('checked', true);
    if (! shootSetting.specifyColorTemp) {
        setWBLabel(WB_SUPPORT_DEFAULT);
    }

    /*
     * Bind handler when clicking item on menu
     */
    $('input[name="wb"]').on('click', function(event) {
        setWBLabel($(event.target).val());
        updateMenus('wb', false);
    });

    /*
     * Hide menu in initial state
     */
    setTimeout(function() {
        $('#wb_menu').removeClass('inithide');
        $('#wb_menu').removeClass('show');
    }, 1000);
}

/**
 * Set WB label to UI
 */
function setWBLabel(val) {
    let htmlvalue = getWhiteBalanceLabel(val);
    $('#wblabel0').html(htmlvalue);
    $('#wblabel0').val(val);
    $('#wblabel1').html(htmlvalue);
    $('#wblabel1').val(val);
    $('#wblabel2').html(htmlvalue);
    $('#wblabel2').val(val);
    $('#wblabel3').html(htmlvalue);
    $('#wblabel3').val(val);
}

/**
 * Initialize Color temperature specified white balance menu
 */
function initWBColorTemperatureMenu() {
    /*
     * Generate menu code and set it to HTML
     */
    let setData = '';
    setData += '<div class="swiper-wrapper">';
    for (let i = 0; i < WHITE_BALANCE_COLOR_TEMPERATURE_SUPPORT.length; i++){
        setData += '  <label class="scroll_btn swiper-slide">';
        setData += '    <input name="colortemp" type="radio" value="' + WHITE_BALANCE_COLOR_TEMPERATURE_SUPPORT[i] + '" id="wb' + i + '">';
        setData += '    <i for="wb' + i + '">' + WHITE_BALANCE_COLOR_TEMPERATURE_SUPPORT[i] + '</i>';
        setData += '  </label>';
    }
    setData += '</div>';
    $('#colortemp_menu_wrap').html(setData);

    /*
     * Select default item
     */
    $('input[name="colortemp"]:eq(' + WHITE_BALANCE_COLOR_TEMPERATURE_SUPPORT.indexOf(WBMANUAL_SUPPORT_DEFAULT) + ')').prop('checked', true);
    if (shootSetting.specifyColorTemp) {
        setColorTempLabel(WBMANUAL_SUPPORT_DEFAULT);
    }

    /*
     * Bind handler when clicking item on menu
     */
    $('input[name="colortemp"]').on('click', function(event) {
        setColorTempLabel($(event.target).val());
        updateMenus('colortemp', false);
    });

    /*
     * Hide menu in initial state
     */
    setTimeout(function() {
        $('#colortemp_menu').removeClass('inithide');
        $('#colortemp_menu').removeClass('show');
    }, 1000);
}

/**
 * Set Color Temperature label to UI
 */
function setColorTempLabel(val) {
    let htmlvalue = '<p class="btn--data__value">'+ val +'</p>';
    $('#colortemplabel0').html(htmlvalue);
    $('#colortemplabel0').val(val);
    $('#colortemplabel1').html(htmlvalue);
    $('#colortemplabel1').val(val);
    $('#colortemplabel2').html(htmlvalue);
    $('#colortemplabel2').val(val);
    $('#colortemplabel3').html(htmlvalue);
    $('#colortemplabel3').val(val);
}

/**
 * Initialize Shutter speed menu of exposure mode 'Shutter speed priority'
 */
function initShutterSpeedMenu() {
    /*
     * Generate menu code and set it to HTML
     */
    let setData = '';
    setData += '<div class="swiper-wrapper">';
    for (let i = 0; i < SHUTTERSPEED_SUPPORT.length; i++) {
        setData += '  <label class="scroll_btn swiper-slide">';
        setData += '    <input name="shutter" type="radio" value="' + SHUTTERSPEED_SUPPORT[i].val + '" id="shutter' + i + '">';
        setData += '    <i for="shutter' + i + '">' + SHUTTERSPEED_SUPPORT[i].text + '</i>';
        setData += '  </label>';
    }
    setData += '</div>';
    $('#shutter_menu_wrap').html(setData);

    /*
     * Select default item
     */
    $('input[name="shutter"]:eq(' + getShutterSpeedIndex(SHUTTERSPEED_SUPPORT_DEFAULT) + ')').prop('checked', true);
    setShutterSpeedLabel(SHUTTERSPEED_SUPPORT_DEFAULT);

    /*
     * Bind handler when clicking item on menu
     */
    $('input[name="shutter"]').on('click', function(event) {
        setShutterSpeedLabel($(event.target).val());
        updateMenus('shutter', false);
    });

    /*
     * Hide menu in initial state
     */
    setTimeout(function() {
        $('#shutter_menu').removeClass('inithide');
        $('#shutter_menu').removeClass('show');
    }, 1000);
}

/**
 * Initialize Shutter speed menu of exposure mode 'Manual'
 */
function initManualShutterSpeedMenu(){
    /*
     * Generate menu code and set it to HTML
     */
    let setData = '';
    setData += '<div class="swiper-wrapper">';
    for (let i = 0; i < MANUAL_SHUTTERSPEED_SUPPORT.length; i++) {
        setData += '  <label class="scroll_btn swiper-slide">';
        setData += '    <input name="manual_shutter" type="radio" value="' + MANUAL_SHUTTERSPEED_SUPPORT[i].val + '" id="manual_shutter' + i + '">';
        setData += '    <i for="manual_shutter' + i + '">' + MANUAL_SHUTTERSPEED_SUPPORT[i].text + '</i>';
        setData += '  </label>';
    }
    setData += '</div>';
    $('#manual_shutter_menu_wrap').html(setData);

    /*
     * Select default item
     */
    $('input[name="manual_shutter"]:eq(' + getManualShutterSpeedIndex(SHUTTERSPEED_SUPPORT_DEFAULT) + ')').prop('checked', true);
    setShutterSpeedLabel(SHUTTERSPEED_SUPPORT_DEFAULT);

    /*
     * Bind handler when clicking item on menu
     */
    $('input[name="manual_shutter"]').on('click', function(event) {
        setShutterSpeedLabel($(event.target).val());
        updateMenus('manual_shutter', false);
    });

    /*
     * Hide menu in initial state
     */
    setTimeout(function() {
        $('#manual_shutter_menu').removeClass('inithide');
        $('#manual_shutter_menu').removeClass('show');
    }, 1000);
}

/**
 * Set ShutterSpeed label to UI
 */
function setShutterSpeedLabel(val) {
    let htmlvalue = '<p class="btn--data__value">' + getShutterSpeedLabel(val) + '</p>';
    $('#splabel1').html(htmlvalue);
    $('#splabel1').val(val);
    $('#splabel2').html(htmlvalue);
    $('#splabel2').val(val);
}

/**
 * Initialize Option menu
 */
function initOptionMenu() {
    /*
     * Generate menu code and set it to HTML
     */
    let setData = '';
    setData += '<div class="swiper-wrapper">';
    for (let i = 0; i < optionSupport.length; i++) {
        setData += '  <label class="scroll_btn option swiper-slide">';
        setData += '    <input name="option" type="radio" value="' + optionSupport[i].val + '" id="option' + i + '">';
        setData += '    <i for="option' + i + '">' + optionSupport[i].text + '</i>';
        setData += '  </label>';
    }
    setData += '</div>';
    $('#option_menu_wrap').html(setData);

    /*
     * Select default item
     */
    $('input[name="option"]:eq(' + getOptionIdx(OPTION_SUPPORT_DEFAULT) + ')').prop('checked', true);
    setOptionLabel(OPTION_SUPPORT_DEFAULT);

    /*
     * Bind handler when clicking item on menu
     */
    $('input[name="option"]').on('click', function(event) {
        setOptionLabel($(event.target).val());
        updateMenus('option', false);
    });

    /*
     * Hide menu in initial state
     */
    setTimeout(function(){
        $('#option_menu').removeClass('inithide');
        $('#option_menu').removeClass('show');
    }, 1000);
}

/**
 * Set Option label to UI
 */
function setOptionLabel(val) {
    let htmlvalue = getOptionLabel(val);
    $('#ftlabel').html(htmlvalue);
}

/**
 * Update menus
 */
function updateMenus(setting, reInit) {
    let index = -1;
    let checked = $('input[name="' + setting + '"]:checked').val();
    switch (setting) {
    case 'ev':
        index = EXPOSURE_COMPENSATION_SUPPORT.indexOf(parseFloat(checked));
        break;
    case 'wb':
        index = getWhiteBalanceMenuIndex(checked);
        break;
    case 'colortemp':
        index = WHITE_BALANCE_COLOR_TEMPERATURE_SUPPORT.indexOf(parseInt(checked));
        break;
    case 'iso':
        index = ISO_SUPPORT.indexOf(parseInt(checked));
        break;
    case 'shutter':
        index = getShutterSpeedIndex(parseFloat(checked));
        break;
    case 'manual_shutter':
        index = getManualShutterSpeedIndex(parseFloat(checked));
        break;
    case 'option':
        index = getOptionIdx(checked);
        break;
    default:
        break;
    }
    if (reInit) {
        if (swipers[setting] != null) {
            swipers[setting].destroy();
            swipers[setting] = null;
        }
        swipers[setting] = new Swiper('.pluginctrl__shoot__setting__detail__btn_wrap.' + setting, {
            slidesPerView: 'auto',
            centeredSlides: true,
            initialSlide: index,
            onSlideChangeEnd: function(swiper) {
                let index = swiper.activeIndex;
                console.log('onSlideChangeEnd: idx = ' + index);
                $('input[name="' + setting + '"]:eq(' + index + ')').prop('checked', true);
                setOptions();
            }
        });
    } else {
        if (swipers[setting] != null) {
            swipers[setting].slideTo(index);
        }
    }
}

/**
 * Create a label from the value of the selected white balance menu item
 */
function getWhiteBalanceLabel(value) {
    let index = 0;
    let label = WHITE_BALANCE_SUPPORT[index].text;
    let icon = WHITE_BALANCE_SUPPORT[index].icon;
    for (let i = 0; i < WHITE_BALANCE_SUPPORT.length; i++) {
        if (WHITE_BALANCE_SUPPORT[i].val == value) {
            label = WHITE_BALANCE_SUPPORT[i].text;
            icon = WHITE_BALANCE_SUPPORT[i].icon;
            index = i;
            break;
        }
    }
    return '<p class="btn--data__value--icon ' + icon + '">' + label + '</p>';
}

/**
 * Get the index value from the value of the selected white balance menu item
 */
function getWhiteBalanceMenuIndex(value) {
    let label = '';
    let index = 0;
    let icon ='';
    for (let i = 0; i < WHITE_BALANCE_SUPPORT.length; i++) {
        if (WHITE_BALANCE_SUPPORT[i].val == value) {
            index = i;
            break;
        }
    }
   return index;
}

/**
 * Get a label from the value of the selected shutter speed menu item
 */
function getShutterSpeedLabel(value) {
    let label = '';
    for (let i = 0; i < MANUAL_SHUTTERSPEED_SUPPORT.length; i++) {
        if (MANUAL_SHUTTERSPEED_SUPPORT[i].val == value) {
            label = MANUAL_SHUTTERSPEED_SUPPORT[i].text;
            break;
        }
    }
    return label;
}

/**
 * Get the index value from the value of the selected shutter speed menu item
 */
function getShutterSpeedIndex(value) {
    let index = 0;
    for (index = 0; index < SHUTTERSPEED_SUPPORT.length; index++) {
        if (SHUTTERSPEED_SUPPORT[index].val == value) {
            break;
        }
    }
    return index;
}

/**
 * Get the index value from the value of the selected manual shutter speed menu item
 */
function getManualShutterSpeedIndex(value) {
    let index = 0;
    for (index = 0; index < MANUAL_SHUTTERSPEED_SUPPORT.length; index++) {
        if (MANUAL_SHUTTERSPEED_SUPPORT[index].val == value) {
            break;
        }
    }
    return index;
}

function getOptionLabel(value) {
    let label = optionSupport[0].text;
    for (let i = 0; i < optionSupport.length; i++) {
        if (optionSupport[i].val == value) {
            label = optionSupport[i].text;
            break;
        }
    }
    return  '<p class="btn--data__value">'+ label +'</p>';
}

function getOptionIdx(value) {
    let index = 0;
    for (let i = 0; i < optionSupport.length; i++) {
        if (optionSupport[i].val == value) {
            index = i;
            break;
        }
    }
    return  index;
}

/**
 * Hide all menus
 */
function hideAllMenu() {
    $('#ev_menu').removeAttr('class');
    $('#iso_menu').removeAttr('class');
    $('#wb_menu').removeAttr('class');
    $('#colortemp_menu').removeAttr('class');
    $('#shutter_menu').removeAttr('class');
    $('#manual_shutter_menu').removeAttr('class');
    $('#option_menu').removeAttr('class');

    $('#ev_menu').addClass('pluginctrl__shoot__setting__detail');
    $('#iso_menu').addClass('pluginctrl__shoot__setting__detail');
    $('#wb_menu').addClass('pluginctrl__shoot__setting__detail');
    $('#colortemp_menu').addClass('pluginctrl__shoot__setting__detail');
    $('#shutter_menu').addClass('pluginctrl__shoot__setting__detail');
    $('#manual_shutter_menu').addClass('pluginctrl__shoot__setting__detail');
    $('#option_menu').addClass('pluginctrl__shoot__setting__detail');
}

/**
 * Show action sheet
 */
function showActionSheet(data){
    console.log(data);

    /*
     * Create action sheet
     */
    createActionSheet(data, labelStrings.cancelBtnLabel);

    /*
     * Bind handler when clicking item on sheet
     */
    $('input[name="action"]:radio').on('click', function(event) {
        if (data.checked == $(this).val()) {        // When the same item is selected, close the sheet without setting.
            hideActionSheet();
        }
    });

    /*
     * Bind handler when select item changed
     */
    $('input[name="action"]:radio').change(function() {
        switch (data.name) {
        case 'mode':
            data.checked = $(this).val();
            changeModeControl($(this).val());
            break;
        case 'volume':
            changeShutterVolume(parseInt($(this).val()));
            break;
        }
        hideActionSheet();
    });

    /*
     * Show action sheet with animation from bottom to top
     */
    $('.action_sheet').css('transform', 'translateY(0%)');
    $('#setting_action').addClass('show');
}

/**
 * Hide action sheet
 */
function hideActionSheet(){
    /*
     * Hide action sheet with animation
     */
    $('#setting_action').removeClass('show');
    setTimeout(function() {
        $('.action_sheet').css('transform', 'translateY(100%)');
    }, 500);
}

/**
 * Create action sheet
 */
function createActionSheet(data, label) {
    $('#action_sheet__btn_list').empty();
    for (let i = 0; i < data.items.length; i++) {
        let id = 'action' + i;
        if (data.items[i].icon === '') {
            if (data.items[i].val === data.checked) {
                $('#action_sheet__btn_list').append('<li><input type="radio" name="action" checked="checked" value="' + data.items[i].val + '" id="' + id + '"/><label for="' + id + '" class="action">' + data.items[i].title + '</label></li>');
            } else {
                $('#action_sheet__btn_list').append('<li><input type="radio" name="action" value="' + data.items[i].val + '" id="' + id + '"/><label for="' + id + '" class="action">' + data.items[i].title + '</label></li>');
            }
        } else {
            if (data.items[i].val === data.checked) {
                $('#action_sheet__btn_list').append('<li><input type="radio" name="action" checked="checked" value="' + data.items[i].val + '" id="' + id + '"/><label for="' + id + '" class="action"><div class="icon icon--' + data.items[i].icon + '"></div>' + data.items[i].title + '</label></li>');
            } else {
                $('#action_sheet__btn_list').append('<li><input type="radio" name="action" value="' + data.items[i].val + '" id="' + id + '"/><label for="' + id + '" class="action"><div class="icon icon--' + data.items[i].icon + '"></div>' + data.items[i].title + '</label></li>');
            }
        }
    }

    /*
     * Set cancel button label
     */
    if ((label != null) && (label !== '')) {
        $('#cancel_btn').text(label);
    }
}

/**
 * Show setting sheet
 */
function showSettingSheet() {
    let fileFormat = '';
    let batteryLevel = 0;
    let remainingPictures = 0;

    /*
     * Disable shooting button and setting button
     */
    disableShootButton();

    /*
     * Pause video
     */
    pauseVideo();

    /*
     * Stop live view
     */
    stopLivePreview()
    .then(function() {
        return sleep(200);
    })
    .then(function() {
        /*
         * Get device status
         */
        return getStatus();
    })
    .then(function(responseText) {
        /*
         * Get current battery level
         */
        let json = JSON.parse(responseText);
        if (json.state.batteryLevel !== undefined) {
            batteryLevel = Math.floor(json.state.batteryLevel * 100);
        }

        /*
         * Get shoot settings
         */
        return getSettings();
    })
    .then(function(responseText) {
        /*
         * Get current image size and shutter volume and remaining pictures
         */
        let json = JSON.parse(responseText);
        let settings = json.results.options;
        if (settings.fileFormat != undefined) {
            fileFormat = settings.fileFormat;
        }
        if (settings._shutterVolume !== undefined) {
            shootSetting.volume = settings._shutterVolume;
        }
        if (settings.remainingPictures !== undefined) {
            remainingPictures = settings.remainingPictures;
        }

        /*
         * Hide all menus and controls
         */
        hideActionSheet();
        hideAllMenu();
        $('.pluginctrl__shoot__shutter').css('visibility', 'hidden');
        $('.pluginctrl__shoot__setting').css('visibility', 'hidden');
        $('.pluginpreview').css('visibility', 'hidden');

        /*
         * Set each labels on sheet
         */
        $('#setting_list_title').text(labelStrings.settingListTitle);                       // Set title label
        $('#setting_close_btn').text(labelStrings.settingListCloseBtnLabel);                // Set close button label
        $('#color_temp_label').text(labelStrings.settingColorTemperatureLabel);             // Set color temperature label
        $('#image_size_label').text(labelStrings.settingImageSizeLabel);                    // Set image size label
        $('#shutter_volume_btn_label').text(labelStrings.settingShutterVolumeLabel);        // Set shutter volume label
        $('#remaining_pictures_label').text(labelStrings.settingRemainingPicturesLabel);    // Set remaining pictures label
        $('#battery_level_label').text(labelStrings.settingBatteryLevelLabel);              // Set battery level label

        /*
         * Set color temperature check to UI
         */
        if (shootSetting.specifyColorTemp) {
            document.getElementById('color_temp').checked = true;
        } else {
            document.getElementById('color_temp').checked = false;
        }

        /*
         * Set image size to UI
         */
        $('#image_size').text(fileFormat.width + 'x' + fileFormat.height);

        /*
         * Set current shutter volume to UI
         */
        $('#shutter_volume_btn').text(getShutterVolumeBtnLabel(shootSetting.volume));

        /*
         * Set remaining pictures to UI
         */
        $('#remaining_pictures').text(remainingPictures + labelStrings.settingRemainingPicturesNumber);

        /*
         * Set current battery level to UI
         */
        $('#battery_level').text(batteryLevel + '%');

        /*
         * Show sheet with animation from top to bottom
         */
        $('#setting_page').addClass('show');
    });
}

/**
 * Hide setting sheet
 */
function hideSettingSheet() {
    /*
     * Get setteing on sheet, and reflect to camera setting
     */
    let specifyColorTemp = document.getElementById('color_temp').checked;
    shootSetting.specifyColorTemp = specifyColorTemp;
    setOptions();

    /*
     * Set shutter volume
     */
    let settings = new Object();
    settings._shutterVolume = shootSetting.volume;
    setSettings(settings)
    .then(function() {
        return sleep(200);
    })
    .then(function() {
        /*
         * Start live view
         */
        return startLivePreview(videSize);
    })
    .then(function() {
        /*
         * Play video
         */
        playVideo();

        /*
         * Hide sheet with animation
         */
        $('#setting_page').removeClass('show');

        /*
         * Show controls
         */
        $('.pluginpreview').css('visibility', 'visible');
        $('.pluginctrl__shoot__setting').css('visibility', 'visible');
        $('.pluginctrl__shoot__shutter').css('visibility', 'visible');
    });
}

/**
 * Change exposure mode button and controls
 */
function changeModeControl(val) {
    hideAllMenu();

    $('#mode_btn').removeClass();

    /*
     * Once, hide all mode controls
     */
    $('#shoot__setting-auto').addClass('hide');
    $('#shoot__setting-shutter').addClass('hide');
    $('#shoot__setting-iso').addClass('hide');
    $('#shoot__setting-manual').addClass('hide');

    /*
     * Show the control of the selected mode
     */
    switch (val) {
    case 'auto':
        $('#mode_btn').addClass('btn--mode-auto-normal');
        $('#shoot__setting-auto').removeClass('hide');
        break;
    case 'shutter':
        $('#mode_btn').addClass('btn--mode-shutter-normal');
        $('#shoot__setting-shutter').removeClass('hide');
        break;
    case 'iso':
        $('#mode_btn').addClass('btn--mode-iso-normal');
        $('#shoot__setting-iso').removeClass('hide');
        break;
    case 'manual':
        $('#mode_btn').addClass('btn--mode-manual-normal');
        $('#shoot__setting-manual').removeClass('hide');
        break;
    }
    actions.mode.checked = val;

    $('#mode_btn').addClass('btn');
}

/**
 * Change shutter volume button
 */
function changeShutterVolume(val) {
    shootSetting.volume = val;
    $('#shutter_volume_btn').text(getShutterVolumeBtnLabel(val));
}

/**
 * Get shutter volume button label
 */
function getShutterVolumeBtnLabel(vol) {
    for (let i = 0; i < actions.volume.items.length; i++) {
         if (actions.volume.items[i].val === vol) {
            return actions.volume.items[i].title;
         }
    }
    return null;
}

/**
 * Show image list (Currently unused)
 */
function showCameraImageList(){
    $('#list_page').addClass('show');
    $('#shooting_page').addClass('hide');
}

/**
 * Hide image list (Currently unused)
 */
function hideCameraImageList(){
    $('#list_page').removeClass('show');
    $('#shooting_page').removeClass('hide');
}

/**
 * Executing still image shooting
 */
function takePicture() {
    shoot();
}

/**
 * Change shooting button state
 */
function changeShootButtonState() {
    let curState = $.data($('#shutter_btn').get(0), 'state');
    let newState = '';
    if ((curState == 'off') || (curState == 'undefined')) {
        newState = 'on';
        $.data($('#shutter_btn').get(0), 'state', 'on');
        $('#shutter_btn').removeClass('btn--shutter-normal');
        $('#shutter_btn').addClass('btn--shutter_recording-normal');
    } else {
        newState = 'off';
        $.data($('#shutter_btn').get(0), 'state', 'off');
        $('#shutter_btn').removeClass('btn--shutter_recording-normal');
        $('#shutter_btn').addClass('btn--shutter-normal');
    }
    console.log('Shoot button state: ' + curState + ' -> ' + newState);
}

/**
 * Disable shooting button
 */
function disableShootButton() {
    $('.btn.btn--setting').prop('disabled', true);
    $('.pluginctrl__shoot').css('pointer-events', 'none');
}

/**
 * Enable shooting button
 */
function enableShootButton() {
    $('.pluginctrl__shoot').css('pointer-events', 'auto');
    $('.btn.btn--setting').prop('disabled', false);
}

/**
 * Change exposure mode
 */
function changeExposureMode() {
    let options = new Object();
    let exposureProgram = $('input[name="action"]:checked').val();
    if (typeof exposureProgram === 'undefined') {
        exposureProgram = 'auto';
    }

    getOptionsInternal()
    .then(function(responseText) {
        let json = JSON.parse(responseText);
        options = json.results.options;
        options.exposureProgram = EXPOSURE_PROGRAM_IDX[exposureProgram];
        setUIOptions(options, true);
        setOptions();
    });
}

/**
 * Get shooting settings from UI, and set it to camera.
 */
function setOptions() {
    let options = getUIOptions();

    /*
     * Setting to camera.
     */
    setOptionsInternal(options)
    .then(function() {
        return sleep(200);
    })
    .then(function() {
        return getOptionsInternal();
    })
    .then(function(responseText) {
        let json = JSON.parse(responseText);
        options = json.results.options;
        setUIOptions(options, false);
    });
}

/**
 * Get shooting settings from UI
 */
function getUIOptions() {
    let options = new Object();

    /*
     * Get shooting settings from UI
     */
    // Exposure mode
    options.exposureProgram = EXPOSURE_PROGRAM_IDX[actions.mode.checked];

    // ISO
    options.iso = $('input[name="iso"]:checked').val();
    // Shutter speed
    if (options.exposureProgram == 4) {
        options.shutterSpeed = $('input[name="shutter"]:checked').val();
    } else if (options.exposureProgram == 1) {
        options.shutterSpeed = $('input[name="manual_shutter"]:checked').val();
    }

    // White balance mode
    if (shootSetting.specifyColorTemp) {
        options.whiteBalance = WHITE_BALANCE_SUPPORT_COLOR_TEMPERATURE_VAL;
    } else {
        options.whiteBalance = $('input[name="wb"]:checked').val();
    }

    // Color temperature
    options._colorTemperature = $('input[name="colortemp"]:checked').val();

    // Exposure compensation
    options.exposureCompensation = $('input[name="ev"]:checked').val();

    // Filter
    options._filter = $('input[name="option"]:checked').val();

    /*
     * Filtering setting items
     */
    let memberfilter = new Array();
    memberfilter.push('exposureProgram');
    if (typeof options.iso != 'undefined') {
        memberfilter.push('iso');
    }
    if (typeof options.shutterSpeed != 'undefined') {
        memberfilter.push('shutterSpeed');
    }
    if (typeof options.whiteBalance != 'undefined') {
        memberfilter.push('whiteBalance');
    }
    if (typeof options._colorTemperature != 'undefined') {
        memberfilter.push('_colorTemperature');
    }
    if (typeof options.exposureCompensation != 'undefined') {
        memberfilter.push('exposureCompensation');
    }
    if (typeof options._filter != 'undefined') {
        memberfilter.push('_filter');
    }
    let setOptions = JSON.stringify(options, memberfilter, '\t');
    options = JSON.parse(setOptions);

    return options;
}

/**
 * Set shooting settings to UI
 */
function setUIOptions(options, expFlag) {
    let evindex;
    let isoindex;
    let spindex;
    let wbindex;

    /*
     * Set labels
     */
    setEvLabel(options.exposureCompensation);
    setISOLabel(options.iso);
    setShutterSpeedLabel(options.shutterSpeed);
    if (options.whiteBalance === WHITE_BALANCE_SUPPORT_COLOR_TEMPERATURE_VAL) {
        shootSetting.specifyColorTemp = true;
        setColorTempLabel(options._colorTemperature);
    } else {
        shootSetting.specifyColorTemp = false;
        setWBLabel(options.whiteBalance);
    }
    setOptionLabel(options._filter);

    switch (options.exposureProgram) {
    case 2: // Exposure mode 'Auto'
        if (expFlag) {
            changeModeControl('auto');
        }
        // Exposure compersation
        evindex = EXPOSURE_COMPENSATION_SUPPORT.indexOf(options.exposureCompensation);
        $('input[name="ev"]:eq(' + evindex + ')').prop('checked', true);

        // White balance mode and color temperature
        if (shootSetting.specifyColorTemp) {
            // Specify color temperature
            $('#wblabel0').css('display', 'none');
            $('#colortemplabel0').css('display', 'inline');
            wbindex = WHITE_BALANCE_COLOR_TEMPERATURE_SUPPORT.indexOf(options._colorTemperature);
            $('input[name="colortemp"]:eq(' + wbindex + ')').prop('checked', true);
        } else {
            // Preset white balance
            $('#wblabel0').css('display', 'block');
            $('#colortemplabel0').css('display', 'none');
            wbindex = getWhiteBalanceMenuIndex(options.whiteBalance);
            $('input[name="wb"]:eq(' + wbindex + ')').prop('checked', true);
        }

        // Option (filter)
        let opindex = getOptionIdx(options._filter);
        $('input[name="option"]:eq(' + opindex + ')').prop('checked', true);

        break;
    case 4:  // Exposure mode 'Shutter speed priority'
        if (expFlag) {
            changeModeControl('shutter');
        }
        // Shutter speed
        spindex = getShutterSpeedIndex(options.shutterSpeed);
        $('input[name="shutter"]:eq(' + spindex + ')').prop('checked', true);

        // Exposure compersation
        evindex = EXPOSURE_COMPENSATION_SUPPORT.indexOf(options.exposureCompensation);
        $('input[name="ev"]:eq(' + evindex + ')').prop('checked', true);

        // White balance mode and color temperature
        if (shootSetting.specifyColorTemp) {
            // Specify color temperature
            $('#wblabel1').css('display', 'none');
            $('#colortemplabel1').css('display', 'inline');
            wbindex = WHITE_BALANCE_COLOR_TEMPERATURE_SUPPORT.indexOf(options._colorTemperature);
            $('input[name="colortemp"]:eq(' + wbindex + ')').prop('checked', true);
        } else {
            // Preset white balance
            $('#wblabel1').css('display', 'block');
            $('#colortemplabel1').css('display', 'none');
            wbindex = getWhiteBalanceMenuIndex(options.whiteBalance);
            $('input[name="wb"]:eq(' + wbindex + ')').prop('checked', true);
        }

        break;
    case 9:  // Exposure mode 'ISO priority'
        if (expFlag) {
            changeModeControl('iso');
        }
        // ISO
        isoindex = ISO_SUPPORT.indexOf(options.iso);
        $('input[name="iso"]:eq('+ isoindex +')').prop('checked', true);

        // Exposure compersation
        evindex = EXPOSURE_COMPENSATION_SUPPORT.indexOf(options.exposureCompensation);
        $('input[name="ev"]:eq(' + evindex + ')').prop('checked', true);

        // White balance mode and color temperature
        if (shootSetting.specifyColorTemp) {
            // Specify color temperature
            $('#wblabel2').css('display', 'none');
            $('#colortemplabel2').css('display', 'inline');
            wbindex = WHITE_BALANCE_COLOR_TEMPERATURE_SUPPORT.indexOf(options._colorTemperature);
            $('input[name="colortemp"]:eq(' + wbindex + ')').prop('checked', true);
        } else {
            // Preset white balance
            $('#wblabel2').css('display', 'block');
            $('#colortemplabel2').css('display', 'none');
            wbindex = getWhiteBalanceMenuIndex(options.whiteBalance);
            $('input[name="wb"]:eq(' + wbindex + ')').prop('checked', true);
        }

        break;
    case 1:  // Exposure mode 'Manual'
        if (expFlag) {
            changeModeControl('manual');
        }
        // ISO
        isoindex = ISO_SUPPORT.indexOf(options.iso);
        $('input[name="iso"]:eq('+ isoindex +')').prop('checked', true);

        // Shutter speed
        spindex = getManualShutterSpeedIndex(options.shutterSpeed);
        $('input[name="manual_shutter"]:eq(' + spindex + ')').prop('checked', true);

        // White balance mode and color temperature
        if (shootSetting.specifyColorTemp) {
            // Specify color temperature
            $('#wblabel3').css('display', 'none');
            $('#colortemplabel3').css('display', 'inline');
            wbindex = WHITE_BALANCE_COLOR_TEMPERATURE_SUPPORT.indexOf(options._colorTemperature);
            $('input[name="colortemp"]:eq(' + wbindex + ')').prop('checked', true);
        } else {
            // Preset white balance
            $('#wblabel3').css('display', 'block');
            $('#colortemplabel3').css('display', 'none');
            wbindex = getWhiteBalanceMenuIndex(options.whiteBalance);
            $('input[name="wb"]:eq(' + wbindex + ')').prop('checked', true);
        }

        break;
    }
}

/**
 * Change view size
 */
function changeViewSize() {
    let previewAreaHeight = document.getElementById('shooting_page').clientHeight
        - ($('.pluginctrl__shoot__setting__detail').height() + 1)
        - ($('.pluginctrl__shoot__setting').height() + 1)
        - ($('.pluginctrl__shoot__shutter').height() + 1);
    let previewAreaWidth = document.body.clientWidth;
    let headerHeight = $('header').height() + 1;

    if (previewAreaHeight <= 0) {
        previewAreaHeight = 0;
        previewAreaWidth = 0;
    }

    let videoWidth = previewAreaWidth;
    let videoHeight = previewAreaWidth / 2;

    if (videoHeight > previewAreaHeight) {
        videoHeight = previewAreaHeight;
        videoWidth = previewAreaHeight * 2;
    }

    let viewTop = (previewAreaHeight - videoHeight) / 2;
    let viewLeft = (previewAreaWidth - videoWidth) / 2;

    let videoStyle = 'position: absolute; top: ' + viewTop + 'px; left: ' + viewLeft + 'px; width: ' + videoWidth + 'px; height: ' + videoHeight + 'px;';
    document.getElementById('previewImage').style = videoStyle;
}

/**
 * When resizing the window, fit the view size to the width of the window
 */
window.onresize = function() {
    changeViewSize();
}
