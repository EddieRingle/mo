package io.ringle.mo

import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.View
import android.view.ViewGroup
import java.util.LinkedHashSet
import kotlin.properties.ReadWriteProperty

public abstract class Stage(key: Int = 0) : Presenter<ViewGroup>(key) {

    override var ctx: Context
        get() = stageCtx ?: stage!!.ctx
        set(c) {
            stageCtx = c
        }

    private var stageCtx: Context? = null

    var parentView: ViewGroup? by StageParent()

    val presenters: MutableSet<Presenter<*>> = LinkedHashSet()

    val presenterListeners: MutableSet<PresenterListener> = LinkedHashSet()

    var containerId = -1

    public val presenterCount: Int
        get() = presenters.size()

    private fun attachTo(context: Context?, root: ViewGroup?) {
        var wasAttached = isAttached
        var wasBound = isBound
        var wasResumed = isResumed
        stageCtx = context
        if (wasAttached) {
            if (wasBound) {
                if (wasResumed) {
                    onPause()
                }
                parentView = view!!.getParent() as ViewGroup
                parentView!!.removeView(view!!)
                onReleaseView()
            }
            onDetach()
        }
        if (context != null) {
            isAttached = true
            if (!presenters.isEmpty()) {
                for (p in presenters) {
                    p.onAttach(this)
                }
            }
            if (wasBound) {
                bindView(root ?: parentView!!)
                parentView = null
            }
            if (wasResumed) {
                onResume()
            }
        }
    }

    public fun attachTo(context: Context) {
        attachTo(context, parentView)
    }

    public fun bindTo(root: ViewGroup) {
        if (isAttached) {
            attachTo(ctx!!, root)
        } else {
            attachTo(root.getContext(), root)
        }
    }

    public fun detach() {
        attachTo(null, null)
    }

    public fun add(p: Presenter<*>): Boolean {
        return if (presenters.add(p)) {
            if (isAttached) {
                p.onAttach(this)
                if (isBound) {
                    val v = p.bindView(view!!)
                    onAttachPresenterView(p, v)
                    if (isResumed) {
                        p.onResume()
                    }
                }
                for (l in presenterListeners) {
                    l.onPresenterAttached(p)
                }
            }
            true
        } else {
            false
        }
    }

    @suppress("nothing_to_inline")
    inline
    public fun plus(p: Presenter<*>): Stage {
        add(p)
        return this
    }

    public fun remove(p: Presenter<*>): Boolean {
        return if (presenters.contains(p)) {
            if (p.isAttached) {
                if (isBound && p.isBound) {
                    if (isResumed && p.isResumed) {
                        p.onPause()
                    }
                    p.view.removeSelf()
                    p.onReleaseView()
                }
                p.onDetach()
            }
            presenters.remove(p)
            for (l in presenterListeners) {
                l.onPresenterDetached(p)
            }
            true
        } else {
            false
        }
    }

    @suppress("nothing_to_inline")
    inline
    public fun minus(p: Presenter<*>): Stage {
        remove(p)
        return this
    }

    public fun removeAllPresenters() {
        if (!presenters.isEmpty()) {
            for (p in presenters) {
                if (p.isAttached) {
                    if (p.isBound) {
                        if (p.isResumed) {
                            p.onPause()
                        }
                        p.view.removeSelf()
                        p.onReleaseView()
                    }
                    p.onDetach()
                }
                for (l in presenterListeners) {
                    l.onPresenterDetached(p)
                }
            }
            presenters.clear()
        }
    }

    @CallSuper
    override fun bindView(parent: ViewGroup): ViewGroup {
        val vg = super.bindView(parent)
        if (!presenters.isEmpty()) {
            for (p in presenters) {
                val v = p.bindView(vg)
                onAttachPresenterView(p, v)
            }
        }
        return vg
    }

    public fun findPresenterByTag(tag: String): Presenter<*>? {
        for (p in presenters) {
            if (p.tag?.equals(tag) ?: false) {
                return p
            }
        }
        return null
    }

    @CallSuper
    override fun onAttach(s: Stage) {
        super.onAttach(s)
        if (!presenters.isEmpty()) {
            for (p in presenters) {
                p.onAttach(this)
            }
        }
    }

    public fun onAttachPresenterView(presenter: Presenter<*>, child: View) {
        if (containerId > -1) {
            (view!!.findViewById(containerId) as ViewGroup).addView(child)
        } else {
            view!!.addView(child)
        }
    }

    @CallSuper
    override fun onDetach() {
        if (!presenters.isEmpty()) {
            for (p in presenters) {
                p.onDetach()
            }
        }
        super.onDetach()
    }

    @CallSuper
    override fun onPause() {
        if (!presenters.isEmpty()) {
            for (p in presenters) {
                p.onPause()
            }
        }
        super.onPause()
    }

    @CallSuper
    override fun onReleaseView() {
        if (!presenters.isEmpty()) {
            for (p in presenters) {
                p.view.removeSelf()
                p.onReleaseView()
            }
        }
        super.onReleaseView()
    }

    override fun onRestoreState(inState: Bundle?) {
        super.onRestoreState(inState)
        if (presenterCount > 0) {
            return
        }
        val pNames = inState?.getStringArray(Mo.sKeyPresenterNames)
        val pStates = inState?.getParcelableArray(Mo.sKeyPresenterStates)
        if (pNames != null && pStates != null && pNames.size() == pStates.size()) {
            val len = pNames.size()
            var i = 0
            while (i < len) {
                val name = pNames[i]
                val state = pStates[i] as Bundle?
                try {
                    val p = Class.forName(name).newInstance() as? Presenter<*>
                    if (p != null) {
                        add(p)
                        p.onRestoreState(state)
                    }
                } catch (ignored: ClassNotFoundException) {
                } catch (ignored: ClassCastException) {
                }
                i += 1
            }
        }
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        if (!presenters.isEmpty()) {
            for (p in presenters) {
                p.onResume()
            }
        }
    }

    @CallSuper
    override fun onSaveState(outState: Bundle) {
        val presenterList = presenters.toList()
        val presenterNames = Array(presenters.size()) { i ->
            presenterList.get(i).javaClass.getName()
        }
        val presenterStates = Array(presenters.size()) { i ->
            val p = presenterList.get(i)
            p.onSaveState(p.state)
            p.state
        }
        outState.putStringArray(Mo.sKeyPresenterNames, presenterNames)
        outState.putParcelableArray(Mo.sKeyPresenterStates, presenterStates)
        super.onSaveState(outState)
    }

    public fun addPresenterListener(l: PresenterListener): Boolean {
        return presenterListeners.add(l)
    }

    public fun removePresenterListener(l: PresenterListener): Boolean {
        return presenterListeners.remove(l)
    }

    interface PresenterListener {

        fun onPresenterAttached(p: Presenter<*>) {
        }

        fun onPresenterDetached(p: Presenter<*>) {
        }
    }

    open
    class PresenterListenerAdapter(fn: PresenterListenerAdapter.() -> Unit) : PresenterListener {

        private var attachFn: (Presenter<*>) -> Unit = {}

        private var detachFn: (Presenter<*>) -> Unit = {}

        fun onPresenterAttached(inline fn: (Presenter<*>) -> Unit) {
            attachFn = fn
        }

        fun onPresenterDetached(inline fn: (Presenter<*>) -> Unit) {
            detachFn = fn
        }

        override fun onPresenterAttached(p: Presenter<*>) {
            attachFn(p)
        }

        override fun onPresenterDetached(p: Presenter<*>) {
            detachFn(p)
        }
    }

    private class StageParent() : ReadWriteProperty<Stage, ViewGroup?> {
        private var value: ViewGroup? = null

        public override fun get(thisRef: Stage, desc: PropertyMetadata): ViewGroup? {
            return if (value == null && thisRef.view != null) {
                thisRef.view?.getParent() as? ViewGroup
            } else {
                value
            }
        }

        public override fun set(thisRef: Stage, desc: PropertyMetadata, value: ViewGroup?) {
            this.value = value
        }
    }
}
