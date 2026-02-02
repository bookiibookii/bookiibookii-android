package com.bookiibookii.bookiibookii.trkDirectHost

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ActivityDirectHostBinding

class DirectHostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDirectHostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDirectHostBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_direct_host)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.cardWidget.setOnClickListener{
            DirectHostStartBottomDialogFragment()
                .show(
                    supportFragmentManager,
                    DirectHostStartBottomDialogFragment.TAG
                )
        }

        // 일단 액티비티 실행되면 바로 나오도록
        if (savedInstanceState == null) {
            val bottomSheet = DirectHostStartBottomDialogFragment()
            bottomSheet.show(supportFragmentManager, DirectHostStartBottomDialogFragment.TAG)
        }
    }
}