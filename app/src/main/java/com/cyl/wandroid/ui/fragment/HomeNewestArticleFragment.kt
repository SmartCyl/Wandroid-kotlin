package com.cyl.wandroid.ui.fragment

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.cyl.wandroid.R
import com.cyl.wandroid.base.BaseRecyclerViewModelFragment
import com.cyl.wandroid.common.bus.Bus
import com.cyl.wandroid.common.bus.JUMP_TO_PROJECT_FRAGMENT
import com.cyl.wandroid.http.bean.ArticleBean
import com.cyl.wandroid.http.bean.HomeBannerBean
import com.cyl.wandroid.tools.checkLoginThenAction
import com.cyl.wandroid.tools.start
import com.cyl.wandroid.ui.activity.MyTodoActivity
import com.cyl.wandroid.ui.activity.PublicAccountContainerActivity
import com.cyl.wandroid.ui.activity.QaActivity
import com.cyl.wandroid.ui.adapter.HomeArticleAdapter
import com.cyl.wandroid.ui.adapter.HomeBannerAdapter
import com.cyl.wandroid.viewmodel.HomeNewestArticleViewModel
import com.youth.banner.Banner
import com.youth.banner.config.IndicatorConfig
import com.youth.banner.indicator.CircleIndicator
import kotlinx.android.synthetic.main.header_view_fragment_home.view.*
import kotlinx.android.synthetic.main.layout_swipe_recycler.*

/**
 * 首页最新文章Tab
 */
class HomeNewestArticleFragment :
    BaseRecyclerViewModelFragment<ArticleBean, HomeNewestArticleViewModel>(), OnItemClickListener,
    OnItemChildClickListener {
    private lateinit var adapter: HomeArticleAdapter
    override fun getLayoutRes() = R.layout.layout_swipe_recycler
    private lateinit var banner: Banner<HomeBannerBean, HomeBannerAdapter>

    override fun lazyInitData() {
        mViewModel.refreshHomeNewestArticle()
        mViewModel.getHomeBanner()
    }

    override fun initView() {
        super.initView()
        initBanner()
    }

    private fun initBanner() {
        banner.apply {
            banner.addBannerLifecycleObserver(this@HomeNewestArticleFragment)
            adapter = HomeBannerAdapter(mViewModel.banners.value)
            indicator = CircleIndicator(activity)
            setIndicatorGravity(IndicatorConfig.Direction.RIGHT)
        }
    }

    override fun initRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener { mViewModel.refreshHomeNewestArticle() }
    }

    override fun initRecyclerView() {
        val manager = LinearLayoutManager(mContext)
        recyclerView.layoutManager = manager
        adapter = HomeArticleAdapter()
        adapter.loadMoreModule.setOnLoadMoreListener {
            mViewModel.loadMoreArticles()
        }

        val headerView =
            LayoutInflater.from(mContext).inflate(R.layout.header_view_fragment_home, null)
        setHeaderMenuClick(headerView)
        adapter.addHeaderView(headerView)
        banner = headerView.findViewById(R.id.banner)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)
        adapter.setOnItemChildClickListener(this)
    }

    private fun setHeaderMenuClick(headerView: View) {
        headerView.hivProject.setOnClickListener {
            Bus.post(JUMP_TO_PROJECT_FRAGMENT, R.id.project)
        }

        headerView.hivPublicAccount.setOnClickListener {
            start(mContext, PublicAccountContainerActivity::class.java)
        }

        headerView.hivQA.setOnClickListener {
            start(mContext, QaActivity::class.java)
        }

        headerView.hivTodo.setOnClickListener {
            start(mContext, MyTodoActivity::class.java, needLogin = true)
        }
    }

    override fun observe() {
        super.observe()
        mViewModel.apply {
            articles.observe(viewLifecycleOwner, Observer {
                adapter.setList(it)
            })

            banners.observe(viewLifecycleOwner, Observer {
                setBanners(it)
            })
        }
    }

    override fun getViewModelArticles(): MutableLiveData<MutableList<ArticleBean>> {
        return mViewModel.articles
    }

    private fun setBanners(it: List<HomeBannerBean>) {
        banner.apply {
            adapter.apply {
                setDatas(it)
                notifyDataSetChanged()
            }
            setCurrentItem(1, false)
            setIndicatorPageChange()
            start()
        }
    }

    override fun getViewModelClass() = HomeNewestArticleViewModel::class.java

    override fun getAdapter() = adapter

    override fun getSwipeRefreshLayout(): SwipeRefreshLayout = swipeRefreshLayout
    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        onItemClickToAgentWeb(position)
    }

    override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (view.id == R.id.ivCollection && checkLoginThenAction(mContext)) {
            // 收藏
            collectItemChildClick(position)
        }
    }
}
