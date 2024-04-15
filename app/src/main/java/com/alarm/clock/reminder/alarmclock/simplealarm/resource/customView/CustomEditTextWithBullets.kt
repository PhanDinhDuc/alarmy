package com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView

import android.R
import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.AppCompatEditText


class CustomEditTextWithBullets @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    companion object {
        const val BULLET_TEXT = "● "
    }

    init {
        disableCopyPaste()
    }

    fun getCText(): String {
        val list = text.toString().split("\n").toMutableList()
        list.removeAll {
            it.trim() == BULLET_TEXT || it.trim().isEmpty() || it.trim() == "●"
        }
        return list.joinToString("\n").trim()
    }

    override fun onTextChanged(
        text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int
    ) {
        var newText = text

        if (lengthAfter > lengthBefore) {
            if (newText.toString().length == 1) {
                newText = "$BULLET_TEXT$newText"
                setText(newText)
                getText()?.let { setSelection(it.length) }
            }

            if (selectionStart == newText.length) {
                fun calText(last: String) {
                    newText = newText.toString().replace("\n", "\n$BULLET_TEXT")
                    newText = newText.toString().replace("$BULLET_TEXT$BULLET_TEXT", BULLET_TEXT)
                    newText = newText.toString().replace("$BULLET_TEXT$BULLET_TEXT", BULLET_TEXT)
                    setText(buildString {
                        append(newText)
                        append(last)
                    })
                    getText()?.let { setSelection(it.length) }
                }

                if (newText.toString().endsWith("\n")) {
                    if (newText.toString().endsWith("\n$BULLET_TEXT\n")) {
                        newText = newText.toString().replace("\n$BULLET_TEXT\n", "\n$BULLET_TEXT")
                        setText(newText)
                        getText()?.let { setSelection(it.length) }
                    } else calText("")
                } else if (newText.toString().endsWith("\n" + newText.last())) {
                    val last = newText.last().toString()
                    newText = newText.toString().dropLast(1)
                    calText(last)
                }
            } else {
                var previousText = newText.substring(0 until selectionStart)

                if (previousText.endsWith("\n")) {
                    val lastText = newText.substring(selectionStart until newText.length)
                    previousText = previousText.replace("\n", "\n$BULLET_TEXT")
                    previousText = previousText.replace("$BULLET_TEXT$BULLET_TEXT", BULLET_TEXT)
                    setText(buildString {
                        append(previousText)
                        append(lastText)
                    })
                    getText()?.let { setSelection(previousText.length) }
                }
            }
        } else {
            if (newText.endsWith("●")) {
                newText = newText.dropLast(2)
                setText(newText)
                getText()?.let { setSelection(it.length) }
            }
        }
        super.onTextChanged(newText, start, lengthBefore, lengthAfter)
    }
}

fun AppCompatEditText.disableCopyPaste() {
    isLongClickable = false
//    setTextIsSelectable(false)
    customSelectionActionModeCallback = object :
        android.view.ActionMode.Callback {

        override fun onCreateActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: android.view.ActionMode?, item: MenuItem?): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: android.view.ActionMode?) {

        }
    }
}