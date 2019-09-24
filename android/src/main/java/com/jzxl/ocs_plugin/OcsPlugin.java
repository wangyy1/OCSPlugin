package com.jzxl.ocs_plugin;

import com.jzxl.ocs_plugin.utils.InitUtils;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** OcsPlugin */
public class OcsPlugin implements MethodCallHandler {

  private Registrar registrar;
  private LocationDelegate locationDelegate;


  public OcsPlugin(Registrar registrar, LocationDelegate locationDelegate) {
    this.registrar = registrar;
    this.locationDelegate = locationDelegate;
    InitUtils.initBaiDuSDK(registrar.activity().getApplicationContext());
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "ocs_plugin");
    LocationDelegate locationDelegate = new LocationDelegate(registrar.activity());
    registrar.addActivityResultListener(locationDelegate);
    registrar.addRequestPermissionsResultListener(locationDelegate);
    channel.setMethodCallHandler(new OcsPlugin(registrar, locationDelegate));
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("sendLocation")) { // 选择位置
      locationDelegate.chooseLocation(call, result);
    } else if (call.method.equals("lookLocation")) { // 查看位置
      locationDelegate.lookLocation(call, result);
    } else {
      result.notImplemented();
    }
  }
}
