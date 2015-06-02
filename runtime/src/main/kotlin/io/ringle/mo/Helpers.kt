package io.ringle.mo

import android.view.View
import android.view.ViewGroup

public fun View.removeSelf(): Unit = (getParent() as? ViewGroup)?.removeView(this)
