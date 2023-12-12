// replace with your package
package com.qrcodescannerlite

import android.util.Log
import android.view.Choreographer
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactPropGroup
import kotlin.math.roundToInt

class QrCodeScannerLiteViewManager(
  private val reactContext: ReactApplicationContext
) : ViewGroupManager<FrameLayout>() {
  private lateinit var scannerFragment: ScannerFragment
  private var propWidth: Int? = null
  private var propHeight: Int? = null

  override fun getName() = REACT_CLASS

  override fun getExportedCustomBubblingEventTypeConstants(): Map<String, Any> {
    return mapOf(
      "QR_CODE_SCANNED" to mapOf(
        "phasedRegistrationNames" to mapOf(
          "bubbled" to "onQrCodeScanned"
        )
      ),
      "ERROR" to mapOf(
        "phasedRegistrationNames" to mapOf(
          "bubbled" to "onError"
        )
      )
    )
  }

  /**
   * Return a FrameLayout which will later hold the Fragment
   */
  override fun createViewInstance(reactContext: ThemedReactContext) =
    FrameLayout(reactContext)

  /**
   * Map the "create" command to an integer
   */
  override fun getCommandsMap() = mapOf("create" to COMMAND_CREATE, "resume" to COMMAND_RESUME)

  /**
   * Handle "create" command (called from JS) and call createFragment method
   */
  override fun receiveCommand(
    root: FrameLayout,
    commandId: String,
    args: ReadableArray?
  ) {
    super.receiveCommand(root, commandId, args)
    Log.d("test", "cmd: $commandId")
    val reactNativeViewId = requireNotNull(args).getInt(0)

    when (commandId.toInt()) {
      COMMAND_CREATE -> createFragment(root, reactNativeViewId)
      COMMAND_RESUME -> scannerFragment.resumeScan()
      COMMAND_PAUSE -> scannerFragment.unbindCamera()
    }
  }

  @ReactPropGroup(names = ["width", "height"], customType = "Style")
  fun setStyle(view: FrameLayout, index: Int, value: Int) {
    val density = reactContext.resources.displayMetrics.density
    if (index == 0) propWidth = (value * density).roundToInt()
    if (index == 1) propHeight = (value * density).roundToInt()
  }


  /**
   * Replace your React Native view with a custom fragment
   */
  fun createFragment(root: FrameLayout, reactNativeViewId: Int) {
    val parentView = root.findViewById<ViewGroup>(reactNativeViewId)
    setupLayout(parentView)

    scannerFragment = ScannerFragment(reactContext)
    val activity = reactContext.currentActivity as FragmentActivity
    activity.supportFragmentManager
      .beginTransaction()
      .replace(reactNativeViewId, scannerFragment, reactNativeViewId.toString())
      .commit()
  }

  fun setupLayout(view: View) {
    Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
      override fun doFrame(frameTimeNanos: Long) {
        manuallyLayoutChildren(view)
        view.viewTreeObserver.dispatchOnGlobalLayout()
        Choreographer.getInstance().postFrameCallback(this)
      }
    })
  }

  /**
   * Layout all children properly
   */
  private fun manuallyLayoutChildren(view: View) {
    // propWidth and propHeight coming from react-native props

    val width = propWidth ?: view.width
    val height = propHeight ?: view.width

    view.measure(
      View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
      View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
    )

    view.layout(0, 0, width, height)
  }

  companion object {
    private const val REACT_CLASS = "QrCodeScannerLiteViewManager"
    private const val COMMAND_CREATE = 0
    private const val COMMAND_RESUME = 1
    private const val COMMAND_PAUSE = 2
  }
}