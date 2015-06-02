package io.ringle.mo

import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.View
import android.view.ViewGroup
import io.ringle.statesman.Contextual
import io.ringle.statesman.Stateful
import io.ringle.statesman.Statesman

abstract class Presenter<V : View>(override val key: Int = 0) : Contextual, Stateful {

    override var ctx: Context
        get() = stage!!.ctx
        set(c) = throw IllegalAccessException("You may not set property 'ctx' on Presenter")

    override val state: Bundle = {
        val b = Bundle()
        b.putBoolean(Statesman.sKeyNewState, true)
        b
    }()

    var isAttached = false

    var isBound = false

    var isResumed = false

    var stage: Stage? = null

    var tag: String? = null

    var view: V? = null

    @CallSuper
    open fun bindView(parent: ViewGroup): V {
        view = onBindView(parent)
        isBound = true
        return view!!
    }

    @CallSuper
    open fun onAttach(s: Stage) {
        stage = s
        isAttached = true
    }

    abstract
    fun onBindView(parent: ViewGroup): V

    @CallSuper
    open fun onDetach() {
        isAttached = false
        stage = null
    }

    @CallSuper
    open fun onPause() {
        isResumed = false
    }

    @CallSuper
    open fun onReleaseView() {
        isBound = false
        view = null
    }

    @CallSuper
    open fun onRestoreState(inState: Bundle?) {
        if (inState?.identityEquals(state)?.not() ?: false) {
            state.putAll(inState)
        }
        state.putBoolean(Statesman.sKeyNewState, inState == null)
    }

    @CallSuper
    open fun onResume() {
        isResumed = true
    }

    @CallSuper
    open fun onSaveState(outState: Bundle) {
        outState.putBoolean(Statesman.sKeyNewState, false)
    }
}
