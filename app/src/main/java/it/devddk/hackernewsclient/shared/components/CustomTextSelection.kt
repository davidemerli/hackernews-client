package it.devddk.hackernewsclient.shared.components

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.DoNotInline
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

class CustomTextToolbar(private val view: View) : TextToolbar {
    private var actionMode: ActionMode? = null

    override var status: TextToolbarStatus = TextToolbarStatus.Shown
        private set

    private val textActionModeCallback: TextActionModeCallback = TextActionModeCallback()

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        actionMode = null
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
    ) {
        println("showMenu")

        textActionModeCallback.rect = rect
        textActionModeCallback.onCopyRequested = onCopyRequested
        textActionModeCallback.onPasteRequested = onPasteRequested
        textActionModeCallback.onCutRequested = onCutRequested
        textActionModeCallback.onSelectAllRequested = onSelectAllRequested

        TextToolbarHelperMethods.startActionMode(
            view,
            FloatingTextActionModeCallback(textActionModeCallback),
            ActionMode.TYPE_FLOATING
        )
    }
}

const val MENU_ITEM_COPY = 0
const val MENU_ITEM_PASTE = 1
const val MENU_ITEM_CUT = 2
const val MENU_ITEM_SELECT_ALL = 3

class TextActionModeCallback(
    var rect: Rect = Rect.Zero,
    var onCopyRequested: (() -> Unit)? = null,
    var onPasteRequested: (() -> Unit)? = null,
    var onCutRequested: (() -> Unit)? = null,
    var onSelectAllRequested: (() -> Unit)? = null,
) : ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        requireNotNull(menu)
        requireNotNull(mode)

//        onCopyRequested?.let {
        menu.add(0, MENU_ITEM_COPY, 0, android.R.string.copy)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
//        }

//        onPasteRequested?.let {
        menu.add(0, MENU_ITEM_PASTE, 1, android.R.string.paste)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
//        }

//        onCutRequested?.let {
        menu.add(0, MENU_ITEM_CUT, 2, android.R.string.cut)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
//        }

//        onSelectAllRequested?.let {
        menu.add(0, MENU_ITEM_SELECT_ALL, 3, android.R.string.selectAll)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
//        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when (item!!.itemId) {
            MENU_ITEM_COPY -> onCopyRequested?.invoke()
            MENU_ITEM_PASTE -> onPasteRequested?.invoke()
            MENU_ITEM_CUT -> onCutRequested?.invoke()
            MENU_ITEM_SELECT_ALL -> onSelectAllRequested?.invoke()
            else -> return false
        }
        mode?.finish()

        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
    }
}

object TextToolbarHelperMethods {
    @DoNotInline
    fun startActionMode(
        view: View,
        actionModeCallback: ActionMode.Callback,
        type: Int,
    ): ActionMode {
        return view.startActionMode(
            actionModeCallback,
            type
        )
    }

    fun invalidateContentRect(actionMode: ActionMode) {
        actionMode.invalidateContentRect()
    }
}

class FloatingTextActionModeCallback(
    private val callback: TextActionModeCallback,
) : ActionMode.Callback2() {
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return callback.onActionItemClicked(mode, item)
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return callback.onCreateActionMode(mode, menu)
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return callback.onPrepareActionMode(mode, menu)
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        callback.onDestroyActionMode(mode)
    }

    override fun onGetContentRect(
        mode: ActionMode?,
        view: View?,
        outRect: android.graphics.Rect?,
    ) {
        val rect = callback.rect

        outRect?.set(
            rect.left.toInt(),
            rect.top.toInt(),
            rect.right.toInt(),
            rect.bottom.toInt()
        )
    }
}
