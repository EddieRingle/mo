package io.ringle.mo

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.platform.platformStatic
import kotlin.properties.Delegates

public class Mo() : Fragment() {

    companion object {

        platformStatic val sKeyPrefix = "__mo_"

        platformStatic val sKeyFragment = "${sKeyPrefix}fragment"

        platformStatic val sKeyPresenterNames = "${sKeyPrefix}presenterNames"

        platformStatic val sKeyPresenterStates = "${sKeyPrefix}presenterStates"

        platformStatic val sKeyRootState = "${sKeyPrefix}rootState"

        public fun of(activity: Activity, containerId: Int = android.R.id.content): Mo {
            val fm = activity.getFragmentManager()
            var mo = fm.findFragmentByTag(sKeyFragment) as? Mo
            if (mo == null) {
                mo = Mo()
                fm.beginTransaction().add(containerId, mo, sKeyFragment).commit()
            }
            return mo
        }
    }

    public var isCreated: Boolean = false

    public val stage: Stage = MoStage()

    public var userStage: Class<out Stage>? = null

    inline public fun present<reified S : Stage>() {
        val oldStage = userStage
        userStage = javaClass<S>()
        if (isCreated) {
            if (oldStage != null) {
                stage.removeAllPresenters()
            }
            stage.add(userStage!!.newInstance())
        }
    }

    override fun onAttach(activity: Activity?) {
        super<Fragment>.onAttach(activity)
        stage.attachTo(activity!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)
        setRetainInstance(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return stage.bindView(container!!)
    }

    override fun onDestroyView() {
        stage.onReleaseView()
        super.onDestroyView()
    }

    override fun onDetach() {
        stage.detach()
        isCreated = false
        super<Fragment>.onDetach()
    }

    override fun onPause() {
        stage.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        stage.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        stage.onSaveState(stage.state)
        outState.putBundle(Mo.sKeyRootState, stage.state)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (stage.presenterCount < 1 && userStage != null) {
            stage.add(userStage!!.newInstance())
        }
        stage.onRestoreState(savedInstanceState?.getBundle(Mo.sKeyRootState))
        isCreated = true
    }

    class MoStage : Stage() {
        override fun onBindView(parent: ViewGroup): ViewGroup {
            return FrameLayout(ctx)
        }
    }
}
