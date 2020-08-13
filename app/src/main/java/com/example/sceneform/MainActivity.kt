package com.example.sceneform

import android.app.AlertDialog
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {
    private var fragment: ArFragment? = null
    private val pointer = PointerDrawable()
    private var isTracking = false
    private var isHitting = false
    private var modelLoader: ModelLoader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment?
        modelLoader = ModelLoader(WeakReference(this))

        fragment!!.arSceneView.scene
            .addOnUpdateListener { frameTime: FrameTime? ->
                fragment!!.onUpdate(frameTime)
                onUpdate()
            }
        initializeGallery()
    }

    private fun onUpdate() {
        val trackingChanged: Boolean = updateTracking()
        val contentView: View = findViewById(android.R.id.content)
        if (trackingChanged) {
            if (isTracking) {
                contentView.overlay.add(pointer)
            } else {
                contentView.overlay.remove(pointer)
            }
            contentView.invalidate()
        }
        if (isTracking) {
            val hitTestChanged: Boolean = updateHitTest()
            if (hitTestChanged) {
                pointer.setEnabled(isHitting)
                contentView.invalidate()
            }
        }
    }

    private fun updateTracking(): Boolean {
        val frame: Frame? = fragment!!.arSceneView.arFrame
        val wasTracking = isTracking
        isTracking = frame != null &&
                frame.camera.trackingState === TrackingState.TRACKING
        return isTracking != wasTracking
    }

    private fun updateHitTest(): Boolean {
        val frame: Frame? = fragment!!.arSceneView.arFrame
        val pt = getScreenCenter()
        val hits: List<HitResult>
        val wasHitting = isHitting
        isHitting = false
        if (frame != null) {
            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane &&
                    trackable.isPoseInPolygon(hit.hitPose)
                ) {
                    isHitting = true
                    break
                }
            }
        }
        return wasHitting != isHitting
    }

    private fun getScreenCenter(): Point {
        val vw = findViewById<View>(android.R.id.content)
        return Point(vw.width / 2, vw.height / 2)
    }

    private fun initializeGallery() {
        val gallery = findViewById<LinearLayout>(R.id.gallery_layout)
        val andy = ImageView(this)
        andy.setImageResource(R.drawable.droid_thumb)
        andy.contentDescription = ("andy")
        andy.setOnClickListener { addObject(Uri.parse("andy.sfb")) }
        gallery.addView(andy)

        val cabin = ImageView(this)
        cabin.setImageResource(R.drawable.cabin_thumb)
        cabin.contentDescription = ("cabin")
        cabin.setOnClickListener { addObject(Uri.parse("Cabin.sfb"))
        }
        gallery.addView(cabin)

        val house = ImageView(this)
        house.setImageResource(R.drawable.house_thumb)
        house.contentDescription = ("house")
        house.setOnClickListener { addObject(Uri.parse("House.sfb")) }
        gallery.addView(house)

        val igloo = ImageView(this)
        igloo.setImageResource(R.drawable.igloo_thumb)
        igloo.contentDescription = ("igloo")
        igloo.setOnClickListener { addObject(Uri.parse("igloo.sfb")) }
        gallery.addView(igloo)
    }

    private fun addObject(model: Uri) {
        val frame = fragment!!.arSceneView.arFrame
        val pt = getScreenCenter()
        val hits: List<HitResult>
        if (frame != null) {
            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane &&
                    trackable.isPoseInPolygon(hit.hitPose)
                ) {
                    modelLoader!!.loadModel(hit.createAnchor(), model)
                    break
                }
            }
        }
    }

    fun onException(throwable: Throwable) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(throwable.message)
            .setTitle("Codelab error!")
        val dialog: AlertDialog = builder.create()
        dialog.show()
        return
    }

    fun addNodeToScene(anchor: Anchor?, renderable: ModelRenderable?) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment!!.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment!!.arSceneView.scene.addChild(anchorNode)
        node.select()
        startAnimation(node, renderable)
    }

    private fun startAnimation(node: TransformableNode?, renderable: ModelRenderable?) {
        if (renderable == null || renderable.animationDataCount == 0) {
            return
        }
        for (i in 0 until renderable.animationDataCount) {
            val animationData = renderable.getAnimationData(i)
        }
        val animator = ModelAnimator(renderable.getAnimationData(0), renderable)
        animator.start()
        node!!.setOnTapListener { _, _ -> togglePauseAndResume(animator) }
    }

    private fun togglePauseAndResume(animator: ModelAnimator) {
        when {
            animator.isPaused -> {
                animator.resume()
            }
            animator.isStarted -> {
                animator.pause()
            }
            else -> {
                animator.start()
            }
        }
    }
}

class ModelLoader internal constructor(private val owner: WeakReference<MainActivity?>?) {
    fun loadModel(anchor: Anchor?, uri: Uri?) {
        if (owner!!.get() == null) {
            Log.d(TAG, "Activity is null.  Cannot load model.")
            return
        }
        ModelRenderable.builder()
            .setSource(owner.get(), uri)
            .build()
            .handle<Any?> { renderable: ModelRenderable?, throwable: Throwable? ->
                val activity: MainActivity? = owner.get()
                when {
                    activity == null -> {
                        return@handle null
                    }
                    throwable != null -> {
                        activity.onException(throwable)
                    }
                    else -> {
                        activity.addNodeToScene(anchor, renderable)
                    }
                }
                null
            }
        return
    }

    companion object {
        private val TAG: String? = "ModelLoader"
    }


}