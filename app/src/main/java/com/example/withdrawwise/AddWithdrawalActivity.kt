package com.example.withdrawwise

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.withdrawwise.databinding.ActivityAddWithdrawalBinding


class AddWithdrawalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddWithdrawalBinding;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myApplication = application as MyApplication

        enableEdgeToEdge()
        binding = ActivityAddWithdrawalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        val selectedName = intent.getStringExtra("selectedATM")
        val selectedATM = myApplication.getSelectedATM(selectedName)

        binding.textAtmName.text = "ATM Full Name:\n" + selectedATM.name
        if(selectedATM.bank.name == myApplication.getUser().bank.name){
            binding.textAtmBank.text = "Bank Name\n" + selectedATM.bank.name + " (Local)"
            binding.textAtmFee.text = "Withdrawal Fee does not apply"
        }
        else{
            binding.textAtmBank.text = "Bank Name\n" + selectedATM.bank.name + " (Foreign)"
            binding.textAtmFee.text = "Withdrawal Fee: " + selectedATM.withdrawalFee.toString() + "%"
        }


        binding.btnLog.setOnClickListener() {
            val amount = binding.editAmount.text.toString().toDouble()
            myApplication.getUser().makeWithdrawal(selectedATM, amount)
            myApplication.saveUserWithdrawals()
            finish()
        }
    }
}