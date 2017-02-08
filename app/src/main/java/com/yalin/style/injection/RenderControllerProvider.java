package com.yalin.style.injection;

import android.content.Context;
import com.yalin.style.render.RealRenderController;
import com.yalin.style.render.RenderController;
import com.yalin.style.render.RenderController.Callbacks;
import com.yalin.style.render.StyleBlurRenderer;

/**
 * YaLin 2016/12/30.
 */

public class RenderControllerProvider {

  private static RenderController stubRenderController;

  public static void setStubRenderController(RenderController renderController) {
    // test
    stubRenderController = renderController;
  }

  public static RenderController providerRenderController(Context context,
      StyleBlurRenderer renderer, Callbacks callbacks) {
    if (stubRenderController != null) {
      return stubRenderController;
    } else {
      return new RealRenderController(context, renderer, callbacks);
    }
  }
}
