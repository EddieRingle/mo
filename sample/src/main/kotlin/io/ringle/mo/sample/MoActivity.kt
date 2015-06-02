package io.ringle.mo.sample

import android.R
import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import io.ringle.mo.Mo
import io.ringle.mo.Stage

public open class MoActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Activity>.onCreate(savedInstanceState)
        Mo.of(this).present<MainStage>()
    }

    class MainStage : Stage() {
        var counter = 0

        override fun onBindView(parent: ViewGroup): ViewGroup {
            val vertframe = LinearLayout(ctx)
            vertframe.setLayoutParams(ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            vertframe.setOrientation(LinearLayout.VERTICAL)
            val hello = TextView(ctx)
            hello.setText("Hello, Mo!")
            vertframe.addView(hello)
            val edit = EditText(ctx)
            edit.setHint("Edit me!")
            edit.setId(R.id.text2)
            vertframe.addView(edit)
            val btn = Button(ctx)
            btn.setText("Pressed ${counter} times!")
            btn.setOnClickListener {
                counter += 1
                btn.setText("Pressed ${counter} times!")
            }
            vertframe.addView(btn)
            (hello.getLayoutParams() as LinearLayout.LayoutParams).gravity = Gravity.CENTER_HORIZONTAL
            return vertframe
        }
    }
}
