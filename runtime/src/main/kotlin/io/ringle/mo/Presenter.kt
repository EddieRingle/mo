package io.ringle.mo

import android.content.Context
import android.support.annotation.CallSuper
import android.view.View
import android.view.ViewGroup
import io.ringle.statesman.Contextual

abstract class Presenter<V : View>() : Contextual {

    override var ctx: Context
        get() = stage!!.ctx
        set(c) = throw IllegalAccessException("You may not set property 'ctx' on Presenter")

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
    open fun onResume() {
        isResumed = true
    }
}
