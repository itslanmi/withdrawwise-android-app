package com.example.withdrawwise

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.withdrawwise.databinding.ActivityChartBinding

import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random


private const val HORIZONTAL_LINE_Y = 5.0
private const val HORIZONTAL_LINE_COLOR = -1080492 // Color("#EF8354")
private const val HORIZONTAL_LINE_THICKNESS_DP = 2f
private const val HORIZONTAL_LINE_LABEL_HORIZONTAL_PADDING_DP = 8f
private const val HORIZONTAL_LINE_LABEL_VERTICAL_PADDING_DP = 2f
private const val HORIZONTAL_LINE_LABEL_MARGIN_DP = 4f

private val monthNames = DateFormatSymbols.getInstance(Locale.US).shortMonths
private val bottomAxisValueFormatter = CartesianValueFormatter { _, x, _ ->
    "${monthNames[x.toInt() % 12]} ’${23 + x.toInt() / 12}"
}

class ChartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChartBinding
    private val modelProducer = CartesianChartModelProducer()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater).apply {
            with(chartView) {
                chart =
                    chart?.copy(
                        decorations = listOf(getViewHorizontalLine()),
                        bottomAxis =
                        (chart?.bottomAxis as HorizontalAxis).copy(
                            valueFormatter = bottomAxisValueFormatter
                        )
                    )
                this.modelProducer = modelProducer
            }
        }

        val myApplication = application as MyApplication

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.chartView.modelProducer = modelProducer
        lifecycleScope.launch {
            modelProducer.runTransaction { columnSeries { series(List(22) { (2 + Random.nextFloat() * 6).roundToInt().toFloat() }) }}
        }

        fun getLocalWithdrawalCount() = myApplication.getUser().withdrawals.filter { it.atm.bank.name == myApplication.getUser().bank.name }.size

        binding.textName.text = myApplication.getUser().name
        binding.textBank.text = myApplication.getUser().bank.name

        binding.textData1.text = getString(R.string.total_amount_data,myApplication.getUser().withdrawals.sumOf { it.amount }.toString())
        binding.textData2.text = getString(R.string.lost_in_fees_data, myApplication.getUser().withdrawals.sumOf { it.amount*(it.fee/100) }.toString())
        binding.textData3.text = getString(R.string.local_withdrawals_data, getLocalWithdrawalCount().toString())
        binding.textData4.text = getString ( R.string.foreign_withdrawals_data ,(myApplication.getUser().withdrawals.size - getLocalWithdrawalCount()).toString())
    }



    private fun getViewHorizontalLine() =
        HorizontalLine(
            y = { HORIZONTAL_LINE_Y },
            line = LineComponent(Fill(HORIZONTAL_LINE_COLOR), HORIZONTAL_LINE_THICKNESS_DP),
            labelComponent =
            TextComponent(
                margins = Dimensions(HORIZONTAL_LINE_LABEL_MARGIN_DP),
                padding =
                Dimensions(
                    HORIZONTAL_LINE_LABEL_HORIZONTAL_PADDING_DP,
                    HORIZONTAL_LINE_LABEL_VERTICAL_PADDING_DP,
                ),
                background = ShapeComponent(Fill(HORIZONTAL_LINE_COLOR), CorneredShape.Pill),
            ),
        )








}