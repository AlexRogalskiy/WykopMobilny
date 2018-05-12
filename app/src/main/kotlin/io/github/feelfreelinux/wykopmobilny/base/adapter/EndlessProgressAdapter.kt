package io.github.feelfreelinux.wykopmobilny.base.adapter

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.feelfreelinux.wykopmobilny.R
import io.github.feelfreelinux.wykopmobilny.ui.helpers.EndlessScrollListener

abstract class EndlessProgressAdapter<T : RecyclerView.ViewHolder, A : Any> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val dataset = arrayListOf<A?>()
    var isLoading = false

    var loadNewDataListener : () -> Unit = {}

    override fun getItemId(position: Int): Long {
        return dataset[position]?.hashCode()?.toLong() ?: position.toLong()
    }

    val data: List<A>
        get() = dataset.filterNotNull()

    companion object {
        val ITEM_PROGRESS = 0
    }

    // Attach EndlessScrollListener
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (recyclerView.layoutManager is LinearLayoutManager) {
            recyclerView.addOnScrollListener(EndlessScrollListener(recyclerView.layoutManager as LinearLayoutManager) {
                if (dataset.last() == null) {
                    if (!isLoading) loadNewDataListener()
                    isLoading = true
                }
            })
        }
    }

    fun disableLoading() {
        if (dataset.isNotEmpty() && dataset.last() == null) {
            val size = dataset.size - 1
            dataset.removeAt(size)
            notifyItemRemoved(size)
        }
        isLoading = false
    }

    fun addData(items: List<A>, shouldClearAdapter: Boolean) {
        if (shouldClearAdapter || dataset.isEmpty()) {
            dataset.apply {
                clear()
                addAll(items)
                add(null)
            }
            notifyDataSetChanged()
        } else {
            if (dataset.last() == null) {
                dataset.removeAt(dataset.size - 1)
            }
            dataset.addAll(items)
            dataset.add(null)
            notifyItemRangeInserted(dataset.size - items.size, items.size + 1)
        }
        isLoading = false
    }


    override fun getItemCount() = dataset.size

    class ProgressViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    abstract fun getViewType(position : Int) : Int

    override fun getItemViewType(position: Int): Int =
            if (dataset[position] == null) ITEM_PROGRESS
            else getViewType(position)

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (dataset[position] != null) bindHolder(holder as T, position)
    }

    abstract fun constructViewHolder(parent: ViewGroup, viewType: Int): T

    abstract fun bindHolder(holder: T, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_PROGRESS -> ProgressViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.progress_item, parent, false))
            else -> constructViewHolder(parent, viewType)
        }
    }
}
