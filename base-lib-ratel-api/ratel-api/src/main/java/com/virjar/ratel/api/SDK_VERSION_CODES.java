package com.virjar.ratel.api;

/**
 * Created by virjar on 2019/2/12.<br>
 * 定义Android sdk版本
 */

public class SDK_VERSION_CODES {
    /**
     * Magic version number for a current development build, which has
     * not yet turned into an official release.
     */
    //public static final int CUR_DEVELOPMENT = VMRuntime.SDK_VERSION_CUR_DEVELOPMENT;

    /**
     * October 2008: The original, first, version of Android.  Yay!
     */
    public static final int BASE = 1;

    /**
     * February 2009: First Android update, officially called 1.1.
     */
    public static final int BASE_1_1 = 2;

    /**
     * May 2009: Android 1.5.
     */
    public static final int CUPCAKE = 3;

    /**
     * September 2009: Android 1.6.
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li> They must explicitly request the
     * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE} permission to be
     * able to modify the contents of the SD card.  (Apps targeting
     * earlier versions will always request the permission.)
     * <li> They must explicitly request the
     * {@link android.Manifest.permission#READ_PHONE_STATE} permission to be
     * able to be able to retrieve phone state info.  (Apps targeting
     * earlier versions will always request the permission.)
     * <li> They are assumed to support different screen densities and
     * sizes.  (Apps targeting earlier versions are assumed to only support
     * medium density normal size screens unless otherwise indicated).
     * They can still explicitly specify screen support either way with the
     * supports-screens manifest tag.
     * <li> {@link android.widget.TabHost} will use the new dark tab
     * background design.
     * </ul>
     */
    public static final int DONUT = 4;

    /**
     * November 2009: Android 2.0
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li> The {@link android.app.Service#onStartCommand
     * Service.onStartCommand} function will return the new
     * {@link android.app.Service#START_STICKY} behavior instead of the
     * old compatibility {@link android.app.Service#START_STICKY_COMPATIBILITY}.
     * <li> The {@link android.app.Activity} class will now execute back
     * key presses on the key up instead of key down, to be able to detect
     * canceled presses from virtual keys.
     * <li> The {@link android.widget.TabWidget} class will use a new color scheme
     * for tabs. In the new scheme, the foreground tab has a medium gray background
     * the background tabs have a dark gray background.
     * </ul>
     */
    public static final int ECLAIR = 5;

    /**
     * December 2009: Android 2.0.1
     */
    public static final int ECLAIR_0_1 = 6;

    /**
     * January 2010: Android 2.1
     */
    public static final int ECLAIR_MR1 = 7;

    /**
     * June 2010: Android 2.2
     */
    public static final int FROYO = 8;

    /**
     * November 2010: Android 2.3
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li> The application's notification icons will be shown on the new
     * dark status bar background, so must be visible in this situation.
     * </ul>
     */
    public static final int GINGERBREAD = 9;

    /**
     * February 2011: Android 2.3.3.
     */
    public static final int GINGERBREAD_MR1 = 10;

    /**
     * February 2011: Android 3.0.
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li> The default theme for applications is now dark holographic:
     * {@link android.R.style#Theme_Holo}.
     * <li> On large screen devices that do not have a physical menu
     * button, the soft (compatibility) menu is disabled.
     * <li> The activity lifecycle has changed slightly as per
     * {@link android.app.Activity}.
     * <li> An application will crash if it does not call through
     * to the super implementation of its
     * {@link android.app.Activity#onPause Activity.onPause()} method.
     * <li> When an application requires a permission to access one of
     * its components (activity, receiver, service, provider), this
     * permission is no longer enforced when the application wants to
     * access its own component.  This means it can require a permission
     * on a component that it does not itself hold and still access that
     * component.
     * <li> {@link android.content.Context#getSharedPreferences
     * Context.getSharedPreferences()} will not automatically reload
     * the preferences if they have changed on storage, unless
     * {@link android.content.Context#MODE_MULTI_PROCESS} is used.
     * <li> {@link android.view.ViewGroup#setMotionEventSplittingEnabled}
     * will default to true.
     * <li> {@link android.view.WindowManager.LayoutParams#FLAG_SPLIT_TOUCH}
     * is enabled by default on windows.
     * <li> {@link android.widget.PopupWindow#isSplitTouchEnabled()
     * PopupWindow.isSplitTouchEnabled()} will return true by default.
     * <li> {@link android.widget.GridView} and {@link android.widget.ListView}
     * will use {@link android.view.View#setActivated View.setActivated}
     * for selected items if they do not implement {@link android.widget.Checkable}.
     * <li> {@link android.widget.Scroller} will be constructed with
     * "flywheel" behavior enabled by default.
     * </ul>
     */
    public static final int HONEYCOMB = 11;

    /**
     * May 2011: Android 3.1.
     */
    public static final int HONEYCOMB_MR1 = 12;

    /**
     * June 2011: Android 3.2.
     * <p>
     * <p>Update to Honeycomb MR1 to support 7 inch tablets, improve
     * screen compatibility mode, etc.</p>
     * <p>
     * <p>As of this version, applications that don't say whether they
     * support XLARGE screens will be assumed to do so only if they target
     * {@link #HONEYCOMB} or later; it had been {@link #GINGERBREAD} or
     * later.  Applications that don't support a screen size at least as
     * large as the current screen will provide the user with a UI to
     * switch them in to screen size compatibility mode.</p>
     * <p>
     * <p>This version introduces new screen size resource qualifiers
     * based on the screen size in dp: see
     * {@link android.content.res.Configuration#screenWidthDp},
     * {@link android.content.res.Configuration#screenHeightDp}, and
     * {@link android.content.res.Configuration#smallestScreenWidthDp}.
     * Supplying these in &lt;supports-screens&gt; as per
     * {@link android.content.pm.ApplicationInfo#requiresSmallestWidthDp},
     * {@link android.content.pm.ApplicationInfo#compatibleWidthLimitDp}, and
     * {@link android.content.pm.ApplicationInfo#largestWidthLimitDp} is
     * preferred over the older screen size buckets and for older devices
     * the appropriate buckets will be inferred from them.</p>
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li><p>New {@link android.content.pm.PackageManager#FEATURE_SCREEN_PORTRAIT}
     * and {@link android.content.pm.PackageManager#FEATURE_SCREEN_LANDSCAPE}
     * features were introduced in this release.  Applications that target
     * previous platform versions are assumed to require both portrait and
     * landscape support in the device; when targeting Honeycomb MR1 or
     * greater the application is responsible for specifying any specific
     * orientation it requires.</p>
     * <li><p>{@link android.os.AsyncTask} will use the serial executor
     * by default when calling {@link android.os.AsyncTask#execute}.</p>
     * <li><p>{@link android.content.pm.ActivityInfo#configChanges
     * ActivityInfo.configChanges} will have the
     * {@link android.content.pm.ActivityInfo#CONFIG_SCREEN_SIZE} and
     * {@link android.content.pm.ActivityInfo#CONFIG_SMALLEST_SCREEN_SIZE}
     * bits set; these need to be cleared for older applications because
     * some developers have done absolute comparisons against this value
     * instead of correctly masking the bits they are interested in.
     * </ul>
     */
    public static final int HONEYCOMB_MR2 = 13;

    /**
     * October 2011: Android 4.0.
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li> For devices without a dedicated menu key, the software compatibility
     * menu key will not be shown even on phones.  By targeting Ice Cream Sandwich
     * or later, your UI must always have its own menu UI affordance if needed,
     * on both tablets and phones.  The ActionBar will take care of this for you.
     * <li> 2d drawing hardware acceleration is now turned on by default.
     * You can use
     * {@link android.R.attr#hardwareAccelerated android:hardwareAccelerated}
     * to turn it off if needed, although this is strongly discouraged since
     * it will result in poor performance on larger screen devices.
     * <li> The default theme for applications is now the "device default" theme:
     * {@link android.R.style#Theme_DeviceDefault}. This may be the
     * holo dark theme or a different dark theme defined by the specific device.
     * The {@link android.R.style#Theme_Holo} family must not be modified
     * for a device to be considered compatible. Applications that explicitly
     * request a theme from the Holo family will be guaranteed that these themes
     * will not change character within the same platform version. Applications
     * that wish to blend in with the device should use a theme from the
     * {@link android.R.style#Theme_DeviceDefault} family.
     * <li> Managed cursors can now throw an exception if you directly close
     * the cursor yourself without stopping the management of it; previously failures
     * would be silently ignored.
     * <li> The fadingEdge attribute on views will be ignored (fading edges is no
     * longer a standard part of the UI).  A new requiresFadingEdge attribute allows
     * applications to still force fading edges on for special cases.
     * <li> {@link android.content.Context#bindService Context.bindService()}
     * will not automatically add in {@link android.content.Context#BIND_WAIVE_PRIORITY}.
     * <li> App Widgets will have standard padding automatically added around
     * them, rather than relying on the padding being baked into the widget itself.
     * <li> An exception will be thrown if you try to change the type of a
     * window after it has been added to the window manager.  Previously this
     * would result in random incorrect behavior.
     * <li> {@link android.view.animation.AnimationSet} will parse out
     * the duration, fillBefore, fillAfter, repeatMode, and startOffset
     * XML attributes that are defined.
     * <li> {@link android.app.ActionBar#setHomeButtonEnabled
     * ActionBar.setHomeButtonEnabled()} is false by default.
     * </ul>
     */
    public static final int ICE_CREAM_SANDWICH = 14;

    /**
     * December 2011: Android 4.0.3.
     */
    public static final int ICE_CREAM_SANDWICH_MR1 = 15;

    /**
     * June 2012: Android 4.1.
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li> You must explicitly request the {@link android.Manifest.permission#READ_CALL_LOG}
     * and/or {@link android.Manifest.permission#WRITE_CALL_LOG} permissions;
     * access to the call log is no longer implicitly provided through
     * {@link android.Manifest.permission#READ_CONTACTS} and
     * {@link android.Manifest.permission#WRITE_CONTACTS}.
     * <li> {@link android.widget.RemoteViews} will throw an exception if
     * setting an onClick handler for views being generated by a
     * {@link android.widget.RemoteViewsService} for a collection container;
     * previously this just resulted in a warning log message.
     * <li> New {@link android.app.ActionBar} policy for embedded tabs:
     * embedded tabs are now always stacked in the action bar when in portrait
     * mode, regardless of the size of the screen.
     * <li> {@link android.webkit.WebSettings#setAllowFileAccessFromFileURLs(boolean)
     * WebSettings.setAllowFileAccessFromFileURLs} and
     * {@link android.webkit.WebSettings#setAllowUniversalAccessFromFileURLs(boolean)
     * WebSettings.setAllowUniversalAccessFromFileURLs} default to false.
     * <li> Calls to {@link android.content.pm.PackageManager#setComponentEnabledSetting
     * PackageManager.setComponentEnabledSetting} will now throw an
     * IllegalArgumentException if the given component class name does not
     * exist in the application's manifest.
     * <li> {@link android.nfc.NfcAdapter#setNdefPushMessage
     * NfcAdapter.setNdefPushMessage},
     * {@link android.nfc.NfcAdapter#setNdefPushMessageCallback
     * NfcAdapter.setNdefPushMessageCallback} and
     * {@link android.nfc.NfcAdapter#setOnNdefPushCompleteCallback
     * NfcAdapter.setOnNdefPushCompleteCallback} will throw
     * IllegalStateException if called after the Activity has been destroyed.
     * <li> Accessibility services must require the new
     * {@link android.Manifest.permission#BIND_ACCESSIBILITY_SERVICE} permission or
     * they will not be available for use.
     * <li> {@link android.accessibilityservice.AccessibilityServiceInfo#FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
     * AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS} must be set
     * for unimportant views to be included in queries.
     * </ul>
     */
    public static final int JELLY_BEAN = 16;

    /**
     * November 2012: Android 4.2, Moar jelly beans!
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li>Content Providers: The default value of {@code android:exported} is now
     * {@code false}. See
     * <a href="{@docRoot}guide/topics/manifest/provider-element.html#exported">
     * the android:exported section</a> in the provider documentation for more details.</li>
     * <li>{@link android.view.View#getLayoutDirection() View.getLayoutDirection()}
     * can return different values than {@link android.view.View#LAYOUT_DIRECTION_LTR}
     * based on the locale etc.
     * <li> {@link android.webkit.WebView#addJavascriptInterface(Object, String)
     * WebView.addJavascriptInterface} requires explicit annotations on methods
     * for them to be accessible from Javascript.
     * </ul>
     */
    public static final int JELLY_BEAN_MR1 = 17;

    /**
     * July 2013: Android 4.3, the revenge of the beans.
     */
    public static final int JELLY_BEAN_MR2 = 18;

    /**
     * October 2013: Android 4.4, KitKat, another tasty treat.
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li> The default result of
     * {@link android.preference.PreferenceActivity#isValidFragment(String)
     * PreferenceActivity.isValueFragment} becomes false instead of true.</li>
     * <li> In {@link android.webkit.WebView}, apps targeting earlier versions will have
     * JS URLs evaluated directly and any result of the evaluation will not replace
     * the current page content.  Apps targetting KITKAT or later that load a JS URL will
     * have the result of that URL replace the content of the current page</li>
     * <li> {@link android.app.AlarmManager#set AlarmManager.set} becomes interpreted as
     * an inexact value, to give the system more flexibility in scheduling alarms.</li>
     * <li> {@link android.content.Context#getSharedPreferences(String, int)
     * Context.getSharedPreferences} no longer allows a null name.</li>
     * <li> {@link android.widget.RelativeLayout} changes to compute wrapped content
     * margins correctly.</li>
     * <li> {@link android.app.ActionBar}'s window content overlay is allowed to be
     * drawn.</li>
     * <li>The {@link android.Manifest.permission#READ_EXTERNAL_STORAGE}
     * permission is now always enforced.</li>
     * <li>Access to package-specific external storage directories belonging
     * to the calling app no longer requires the
     * {@link android.Manifest.permission#READ_EXTERNAL_STORAGE} or
     * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}
     * permissions.</li>
     * </ul>
     */
    public static final int KITKAT = 19;

    /**
     * June 2014: Android 4.4W. KitKat for watches, snacks on the run.
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li>{@link android.app.AlertDialog} might not have a default background if the theme does
     * not specify one.</li>
     * </ul>
     */
    public static final int KITKAT_WATCH = 20;

    /**
     * Temporary until we completely switch to {@link #LOLLIPOP}.
     *
     * @hide
     */
    public static final int L = 21;

    /**
     * November 2014: Lollipop.  A flat one with beautiful shadows.  But still tasty.
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li> {@link android.content.Context#bindService Context.bindService} now
     * requires an explicit Intent, and will throw an exception if given an implicit
     * Intent.</li>
     * <li> {@link android.app.Notification.Builder Notification.Builder} will
     * not have the colors of their various notification elements adjusted to better
     * match the new material design look.</li>
     * <li> {@link android.os.Message} will validate that a message is not currently
     * in use when it is recycled.</li>
     * <li> Hardware accelerated drawing in windows will be enabled automatically
     * in most places.</li>
     * <li> {@link android.widget.Spinner} throws an exception if attaching an
     * adapter with more than one item type.</li>
     * <li> If the app is a launcher, the launcher will be available to the user
     * even when they are using corporate profiles (which requires that the app
     * use {@link android.content.pm} to correctly populate its
     * apps UI).</li>
     * <li> Calling {@link android.app.Service#stopForeground Service.stopForeground}
     * with removeNotification false will modify the still posted notification so that
     * it is no longer forced to be ongoing.</li>
     * <li> A {@link android.service.dreams.DreamService} must require the
     * {@link android.Manifest.permission} permission to be usable.</li>
     * </ul>
     */
    public static final int LOLLIPOP = 21;

    /**
     * March 2015: Lollipop with an extra sugar coating on the outside!
     */
    public static final int LOLLIPOP_MR1 = 22;

    /**
     * M is for Marshmallow!
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li> Runtime permissions.  Dangerous permissions are no longer granted at
     * install time, but must be requested by the application at runtime through
     * {@link android.app.Activity}.</li>
     * <li> Bluetooth and Wi-Fi scanning now requires holding the location permission.</li>
     * <li> {@link android.app.AlarmManager#setTimeZone AlarmManager.setTimeZone} will fail if
     * the given timezone is non-Olson.</li>
     * <li> Activity transitions will only return shared
     * elements mapped in the returned view hierarchy back to the calling activity.</li>
     * <li> {@link android.view.View} allows a number of behaviors that may break
     * existing apps: Canvas throws an exception if restore() is called too many times,
     * widgets may return a hint size when returning UNSPECIFIED measure specs, and it
     * will respect the attributes {@link android.R.attr#foreground},
     * {@link android.R.attr#foregroundGravity}, {@link android.R.attr}, and
     * {@link android.R.attr}.</li>
     * <li> {@link android.view.MotionEvent#getButtonState MotionEvent.getButtonState}
     * will no longer report {@link android.view.MotionEvent#BUTTON_PRIMARY}
     * and {@link android.view.MotionEvent#BUTTON_SECONDARY} as synonyms for
     * {@link android.view.MotionEvent} and
     * {@link android.view.MotionEvent}.</li>
     * <li> {@link android.widget.ScrollView} now respects the layout param margins
     * when measuring.</li>
     * </ul>
     */
    public static final int M = 23;

    /**
     * N is for Nougat.
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li> {@link android.app.DownloadManager.Request#setAllowedNetworkTypes
     * DownloadManager.Request.setAllowedNetworkTypes}
     * will disable "allow over metered" when specifying only
     * {@link android.app.DownloadManager.Request#NETWORK_WIFI}.</li>
     * <li> {@link android.app.DownloadManager} no longer allows access to raw
     * file paths.</li>
     * <li> {@link android.app.Notification.Builder#setShowWhen
     * Notification.Builder.setShowWhen}
     * must be called explicitly to have the time shown, and various other changes in
     * {@link android.app.Notification.Builder Notification.Builder} to how notifications
     * are shown.</li>
     * <li>{@link android.content.Context#MODE_WORLD_READABLE} and
     * {@link android.content.Context#MODE_WORLD_WRITEABLE} are no longer supported.</li>
     * <li>{@link android.os} will be thrown to applications.</li>
     * <li>Applications will see global drag and drops as per
     * {@link android.view.View}.</li>
     * <li>{@link android.webkit.WebView#evaluateJavascript WebView.evaluateJavascript}
     * will not persist state from an empty WebView.</li>
     * <li>{@link android.animation.AnimatorSet} will not ignore calls to end() before
     * start().</li>
     * <li>{@link android.app.AlarmManager#cancel(android.app.PendingIntent)
     * AlarmManager.cancel} will throw a NullPointerException if given a null operation.</li>
     * <li>{@link android.app.FragmentManager} will ensure fragments have been created
     * before being placed on the back stack.</li>
     * <li>{@link android.app.FragmentManager} restores fragments in
     * {@link android.app.Fragment#onCreate Fragment.onCreate} rather than after the
     * method returns.</li>
     * <li>{@link android.R.attr} defaults to true.</li>
     * <li>{@link android.graphics.drawable} throws exceptions when
     * opening invalid VectorDrawable animations.</li>
     * <li>{@link android.view.ViewGroup.MarginLayoutParams} will no longer be dropped
     * when converting between some types of layout params (such as
     * {@link android.widget.LinearLayout.LayoutParams LinearLayout.LayoutParams} to
     * {@link android.widget.RelativeLayout.LayoutParams RelativeLayout.LayoutParams}).</li>
     * <li>Your application processes will not be killed when the device density changes.</li>
     * <li>Drag and drop. After a view receives the
     * {@link android.view.DragEvent#ACTION_DRAG_ENTERED} event, when the drag shadow moves into
     * a descendant view that can accept the data, the view receives the
     * {@link android.view.DragEvent#ACTION_DRAG_EXITED} event and won’t receive
     * {@link android.view.DragEvent#ACTION_DRAG_LOCATION} and
     * {@link android.view.DragEvent#ACTION_DROP} events while the drag shadow is within that
     * descendant view, even if the descendant view returns <code>false</code> from its handler
     * for these events.</li>
     * </ul>
     */
    public static final int N = 24;

    /**
     * N MR1: Nougat++.
     */
    public static final int N_MR1 = 25;

    /**
     * O.
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li>{@link android.R.attr#focusable} defaults to a new state ({@code auto}) where it will
     * inherit the value of {@link android.R.attr#clickable} unless explicitly overridden.</li>
     * <li>A default theme-appropriate focus-state highlight will be supplied to all Views
     * which don't provide a focus-state drawable themselves. This can be disabled by setting
     * {@link android.R.attr} to false.</li>
     * </ul>
     */
    public static final int O = 26;

    /**
     * O MR1.
     */
    public static final int O_MR1 = 27;

    /**
     * P.
     * <p>
     * <p>Applications targeting this or a later release will get these
     * new changes in behavior:</p>
     * <ul>
     * <li>{@link android.app.Service#startForeground Service.startForeground} requires
     * that apps hold the permission
     * {@link android.Manifest.permission}.</li>
     * <li>{@link android.widget.LinearLayout} will always remeasure weighted children,
     * even if there is no excess space.</li>
     * </ul>
     */
    public static final int P = 28;
}
