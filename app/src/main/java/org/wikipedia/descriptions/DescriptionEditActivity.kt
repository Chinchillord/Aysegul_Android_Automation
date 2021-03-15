package org.wikipedia.descriptions

import android.content.Context
import android.content.Intent
import androidx.annotation.ColorInt
import org.wikipedia.Constants
import org.wikipedia.Constants.InvokeSource
import org.wikipedia.R
import org.wikipedia.activity.SingleFragmentActivity
import org.wikipedia.analytics.SuggestedEditsFunnel
import org.wikipedia.history.HistoryEntry
import org.wikipedia.json.GsonMarshaller
import org.wikipedia.json.GsonUnmarshaller
import org.wikipedia.page.ExclusiveBottomSheetPresenter
import org.wikipedia.page.PageActivity
import org.wikipedia.page.PageTitle
import org.wikipedia.page.linkpreview.LinkPreviewDialog
import org.wikipedia.readinglist.AddToReadingListDialog.Companion.newInstance
import org.wikipedia.suggestededits.PageSummaryForEdit
import org.wikipedia.util.ClipboardUtil
import org.wikipedia.util.DeviceUtil.hideSoftKeyboard
import org.wikipedia.util.FeedbackUtil
import org.wikipedia.util.ShareUtil
import org.wikipedia.views.ImagePreviewDialog

class DescriptionEditActivity : SingleFragmentActivity<DescriptionEditFragment>(), DescriptionEditFragment.Callback, LinkPreviewDialog.Callback {
    enum class Action {
        ADD_DESCRIPTION, TRANSLATE_DESCRIPTION, ADD_CAPTION, TRANSLATE_CAPTION, ADD_IMAGE_TAGS
    }

    private lateinit var action: Action
    private val bottomSheetPresenter = ExclusiveBottomSheetPresenter()

    override fun onDescriptionEditSuccess() {
        setResult(DescriptionEditSuccessActivity.RESULT_OK_FROM_EDIT_SUCCESS)
        finish()
    }

    override fun onBottomBarContainerClicked(action: Action) {
        val summary: PageSummaryForEdit = if (action == Action.TRANSLATE_DESCRIPTION) {
            GsonUnmarshaller.unmarshal(PageSummaryForEdit::class.java, intent.getStringExtra(EXTRA_TARGET_SUMMARY))
        } else {
            GsonUnmarshaller.unmarshal(PageSummaryForEdit::class.java, intent.getStringExtra(EXTRA_SOURCE_SUMMARY))
        }
        if (action == Action.ADD_CAPTION || action == Action.TRANSLATE_CAPTION) {
            bottomSheetPresenter.show(supportFragmentManager,
                    ImagePreviewDialog.newInstance(summary, action))
        } else {
            bottomSheetPresenter.show(supportFragmentManager,
                    LinkPreviewDialog.newInstance(HistoryEntry(summary.pageTitle,
                            if (intent.hasExtra(Constants.INTENT_EXTRA_INVOKE_SOURCE) && intent.getSerializableExtra
                                    (Constants.INTENT_EXTRA_INVOKE_SOURCE) === InvokeSource.PAGE_ACTIVITY)
                                HistoryEntry.SOURCE_EDIT_DESCRIPTION else HistoryEntry.SOURCE_SUGGESTED_EDITS), null))
        }
    }

    override fun onLinkPreviewLoadPage(title: PageTitle, entry: HistoryEntry, inNewTab: Boolean) {
        startActivity(PageActivity.newIntentForCurrentTab(this, entry, entry.title))
    }

    override fun onLinkPreviewCopyLink(title: PageTitle) {
        copyLink(title.uri)
    }

    override fun onLinkPreviewAddToList(title: PageTitle) {
        bottomSheetPresenter.show(supportFragmentManager,
                newInstance(title, InvokeSource.LINK_PREVIEW_MENU))
    }

    override fun onLinkPreviewShareLink(title: PageTitle) {
        ShareUtil.shareText(this, title)
    }

    fun updateStatusBarColor(@ColorInt color: Int) {
        setStatusBarColor(color)
    }

    fun updateNavigationBarColor(@ColorInt color: Int) {
        setNavigationBarColor(color)
    }

    private fun copyLink(url: String) {
        ClipboardUtil.setPlainText(this, null, url)
        FeedbackUtil.showMessage(this, R.string.address_copied)
    }

    public override fun createFragment(): DescriptionEditFragment {
        val invokeSource = intent.getSerializableExtra(Constants.INTENT_EXTRA_INVOKE_SOURCE) as InvokeSource?
        action = intent.getSerializableExtra(Constants.INTENT_EXTRA_ACTION) as Action
        val title: PageTitle = intent.getParcelableExtra(EXTRA_TITLE)!!
        SuggestedEditsFunnel.get().click(title.displayText, action)
        return DescriptionEditFragment.newInstance(title,
                intent.getStringExtra(EXTRA_HIGHLIGHT_TEXT),
                intent.getStringExtra(EXTRA_SOURCE_SUMMARY),
                intent.getStringExtra(EXTRA_TARGET_SUMMARY),
                action,
                invokeSource!!)
    }

    override fun onBackPressed() {
        if (fragment.editView.showingReviewContent()) {
            fragment.editView.loadReviewContent(false)
        } else {
            hideSoftKeyboard(this)
            SuggestedEditsFunnel.get().cancel(action)
            super.onBackPressed()
        }
    }

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_HIGHLIGHT_TEXT = "highlightText"
        private const val EXTRA_SOURCE_SUMMARY = "sourceSummary"
        private const val EXTRA_TARGET_SUMMARY = "targetSummary"

        @JvmStatic
        fun newIntent(context: Context,
                      title: PageTitle,
                      highlightText: String?,
                      sourceSummary: PageSummaryForEdit?,
                      targetSummary: PageSummaryForEdit?,
                      action: Action,
                      invokeSource: InvokeSource): Intent {
            return Intent(context, DescriptionEditActivity::class.java)
                    .putExtra(EXTRA_TITLE, title)
                    .putExtra(EXTRA_HIGHLIGHT_TEXT, highlightText)
                    .putExtra(EXTRA_SOURCE_SUMMARY, if (sourceSummary == null) null else GsonMarshaller.marshal(sourceSummary))
                    .putExtra(EXTRA_TARGET_SUMMARY, if (targetSummary == null) null else GsonMarshaller.marshal(targetSummary))
                    .putExtra(Constants.INTENT_EXTRA_ACTION, action)
                    .putExtra(Constants.INTENT_EXTRA_INVOKE_SOURCE, invokeSource)
        }
    }
}
