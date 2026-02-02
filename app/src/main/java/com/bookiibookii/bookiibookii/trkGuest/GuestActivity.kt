package com.bookiibookii.bookiibookii.trkGuest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ActivityGuestBinding
import com.bookiibookii.bookiibookii.databinding.ActivityHostBinding
import com.bookiibookii.bookiibookii.trkHost.HostStartBottomDialogFragment

class GuestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGuestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.cardWidget.setOnClickListener{
            GuestReadingStatusBottomDialogFragment()
                .show(
                    supportFragmentManager,
                    GuestReadingStatusBottomDialogFragment.TAG
                )
        }

        if (savedInstanceState == null) {
            val bottomSheet = GuestReadingStatusBottomDialogFragment()
            bottomSheet.show(supportFragmentManager, GuestReadingStatusBottomDialogFragment.TAG)
        }
    }
}