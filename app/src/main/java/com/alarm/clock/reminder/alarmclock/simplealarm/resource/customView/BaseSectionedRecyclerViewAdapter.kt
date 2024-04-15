package com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemLoadmoreBinding

class BaseViewHolder<B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

abstract class BaseSectionedRecyclerViewAdapter<M, S : ViewBinding, I : ViewBinding> :
    SectionedRecyclerViewAdapter<RecyclerView.ViewHolder>() {
    internal val VIEW_TYPE_HEADER_LOADING = -3

    internal var datas: MutableList<M?> = mutableListOf()

    init {
        shouldShowHeadersForEmptySections(true)
    }

    var isLoadMore = false

    fun loadMore() {
        if (isLoadMore) return
        isLoadMore = true
        datas.add(null)
        notifyDataSetChanged()
    }

    open fun setData(datas: List<M>) {
        if (isLoadMore) {
            this.datas.removeLast(); isLoadMore = false
        }
        this.datas.clear()
        this.datas.addAll(datas)
        notifyDataSetChanged()
    }

    fun addData(list: List<M>, index: Int) {
        datas.addAll(index, list)
        notifyItemRangeChanged(index, list.size)
    }

    fun appendWhenLoadMore(list: List<M>) {
        if (isLoadMore) {
            datas.removeLast(); isLoadMore = false
        }

        datas.addAll(datas.size - 1, list)
        notifyDataSetChanged()
    }

    abstract fun mOnCreateHeaderViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<S>
    abstract fun mOnCreateItemViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<I>
    abstract fun mOnBindHeaderViewHolder(holder: BaseViewHolder<S>?, section: Int)
    abstract fun mOnBindItemViewHolder(
        holder: BaseViewHolder<I>?,
        section: Int,
        relativePosition: Int,
        absolutePosition: Int
    )

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, section: Int) {
        mOnBindHeaderViewHolder(holder as? BaseViewHolder<S>, section)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        section: Int,
        relativePosition: Int,
        absolutePosition: Int
    ) {
        mOnBindItemViewHolder(
            holder as? BaseViewHolder<I>,
            section,
            relativePosition,
            absolutePosition
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> mOnCreateHeaderViewHolder(parent, viewType)
            VIEW_TYPE_ITEM -> mOnCreateItemViewHolder(parent, viewType)
            VIEW_TYPE_HEADER_LOADING -> LoadingViewHolder(
                ItemLoadmoreBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> Empty(View(parent.context))
        }
    }

    override fun getHeaderViewType(section: Int): Int {
        return if (datas[section] == null) {
            VIEW_TYPE_HEADER_LOADING
        } else {
            super.getHeaderViewType(section)
        }
    }

    override fun getSectionCount(): Int {
        return datas.size
    }

    class LoadingViewHolder(itemView: ItemLoadmoreBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
    }

    class Empty(v: View) : RecyclerView.ViewHolder(v)
}