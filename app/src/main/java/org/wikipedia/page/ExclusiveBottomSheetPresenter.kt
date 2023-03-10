package org.wikipedia.page

import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.wikipedia.Constants.InvokeSource
import org.wikipedia.readinglist.AddToReadingListDialog
import org.wikipedia.readinglist.MoveToReadingListDialog

object ExclusiveBottomSheetPresenter {
    const val BOTTOM_SHEET_FRAGMENT_TAG = "bottom_sheet_fragment"

    fun showAddToListDialog(fm: FragmentManager,
                            title: PageTitle,
                            source: InvokeSource) {
        show(fm, AddToReadingListDialog.newInstance(title, source))
    }

    fun showAddToListDialog(fm: FragmentManager,
                            title: PageTitle,
                            source: InvokeSource,
                            listener: DialogInterface.OnDismissListener?) {
        show(fm, AddToReadingListDialog.newInstance(title, source, listener))
    }

    fun showMoveToListDialog(fm: FragmentManager,
                             sourceReadingListId: Long,
                             title: PageTitle,
                             source: InvokeSource,
                             showDefaultList: Boolean,
                             listener: DialogInterface.OnDismissListener?) {
        show(fm, MoveToReadingListDialog.newInstance(sourceReadingListId, listOf(title), source, showDefaultList, listener))
    }

    fun show(manager: FragmentManager, dialog: DialogFragment) {
        if (manager.isStateSaved || manager.isDestroyed) {
            return
        }
        dismiss(manager)
        dialog.show(manager, BOTTOM_SHEET_FRAGMENT_TAG)
    }

    fun getCurrentBottomSheet(manager: FragmentManager): DialogFragment? {
        if (manager.isStateSaved || manager.isDestroyed) {
            return null
        }
        return manager.findFragmentByTag(BOTTOM_SHEET_FRAGMENT_TAG) as DialogFragment?
    }

    fun dismiss(manager: FragmentManager) {
        getCurrentBottomSheet(manager)?.dismiss()
    }
}
