package com.bookiibookii.bookiibookii.trkHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ActivityHostBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHostBinding
    private val vm: HostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }

        supportFragmentManager.setFragmentResultListener(
            HostStartBottomDialogFragment.RESULT_KEY,
            this
        ) { _, bundle ->
            when (bundle.getString(HostStartBottomDialogFragment.BUNDLE_ACTION)) {
                "START_READING" -> vm.onAction(HostAction.SET_HOST_READING)
                "HOST_SHIPPING_READY" -> vm.onAction(HostAction.SET_HOST_SHIPPING_READY)
                "HOST_SHIPPED" -> vm.onAction(HostAction.SET_HOST_SHIPPED)
                "GUEST_READING" -> vm.onAction(HostAction.SET_GUEST_READING)
                "GUEST_SHIPPING_READY" -> vm.onAction(HostAction.SET_GUEST_SHIPPING_READY)
                "GUEST_SHIPPED" -> vm.onAction(HostAction.SET_GUEST_SHIPPED)
                "FINISHED" -> vm.onAction(HostAction.SET_FINISHED)
            }
        }

        binding.cardWidget.setOnClickListener{
            HostStartBottomDialogFragment()
                .show(
                    supportFragmentManager,
                    HostStartBottomDialogFragment.TAG
                )
        }

        // 일단 액티비티 실행되면 바로 나오도록
        if (savedInstanceState == null) {
            val bottomSheet = HostStartBottomDialogFragment()
            bottomSheet.show(supportFragmentManager, HostStartBottomDialogFragment.TAG)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.steps.collectLatest { steps ->
                    updateStatusList(steps)
                }
            }
        }
    }

    private fun updateStatusList(steps: List<TradeStatusItem>){
        binding.stepContainer.removeAllViews()

        val inflater = LayoutInflater.from(this)

        steps.forEachIndexed { index, item ->
            val row = inflater.inflate(R.layout.item_trade_status, binding.stepContainer, false)

            row.findViewById<TextView>(R.id.tv_title).text = item.title
            row.findViewById<TextView>(R.id.tv_desc).text = item.description
            row.findViewById<TextView>(R.id.tv_badge).text = item.badge

            val divider = row.findViewById<View>(R.id.divider)
            divider.visibility = if (index == steps.lastIndex) View.GONE else View.VISIBLE

            binding.stepContainer.addView(row)
        }
    }
}