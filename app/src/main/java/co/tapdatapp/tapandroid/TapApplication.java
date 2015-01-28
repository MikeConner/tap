package co.tapdatapp.tapandroid;

import android.app.Application;

public class TapApplication extends Application {
  private static TapApplication app;

  @Override
  public void onCreate() {
    app = this;
  }

  public static TapApplication get() {
    return app;
  }
}
