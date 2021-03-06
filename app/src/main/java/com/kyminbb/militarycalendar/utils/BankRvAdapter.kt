package com.kyminbb.militarycalendar.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kyminbb.militarycalendar.R
import kotlinx.android.synthetic.main.recycler_view_item.*
import org.jetbrains.anko.imageResource
import org.threeten.bp.LocalDate
import java.text.DecimalFormat


class BankRvAdapter(val context: Context, val bankList: ArrayList<Bank>) :
    RecyclerView.Adapter<BankRvAdapter.Holder>() {

    private val decimalFormat = DecimalFormat("#,###")

    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    // 리스너 객체 참조를 저장하는 변수
    private var mListener: OnItemClickListener? = null

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_view_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return bankList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(bankList[position], context)
    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){



        val bankItemName = itemView?.findViewById<TextView>(R.id.bankItemName)
        val bankItemTotalDeposit = itemView?.findViewById<TextView>(R.id.bankItemTotalDeposit)
        val bankItemMonthDeposit = itemView?.findViewById<TextView>(R.id.bankItemMonthDeposit)
        val bankItemSetting = itemView?.findViewById<ImageButton>(R.id.bankItemSetting)

        init{
            bankItemSetting!!.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    if(mListener != null){
                       mListener!!.onItemClick(bankItemSetting, position)
                    }
                }
            }
        }


        fun bind (bank: Bank, context: Context){
            bankItemSetting?.imageResource = R.drawable.small_menu_button
            /* Textview 와 String data를 연결한다 */
            bankItemName?.text = bank.bankName
            bankItemTotalDeposit?.text = "총 ${decimalFormat.format(bank.bankTotalDeposit)}원"
            bankItemMonthDeposit?.text = "월 ${decimalFormat.format(bank.monthDeposit)}원"
        }
    }



}