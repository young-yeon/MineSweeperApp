package com.example.minesweeper

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TableRow
import com.example.minesweeper.databinding.ActivityMainBinding
import com.example.minesweeper.model.Board
import com.example.minesweeper.model.Cell
import kotlin.math.roundToInt


class MainActivity : Activity() {

    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        guidePlayMode()
    }

    private var isPlaying = false

    private fun guidePlayMode() {
        val modeSpinner = mainBinding.playModeSpinner
        val modeArray = resources.getStringArray(R.array.playMode)
        val modeSpinnerAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, modeArray)
        modeSpinner.adapter = modeSpinnerAdapter
        modeSpinner.onItemSelectedListener = PlayModeSpinnerActivity()
    }

    private fun play(mode: Int) {
        mainBinding.boardLayout.removeAllViews() // clear board
        val board = Board(mode)
        isPlaying = true
        createBoard(board)
    }

    private fun createBoard(board: Board) {
        for (row in 0..<board.ROOT) {
            val boardTableRow = TableRow(this)
            boardTableRow.layoutParams = TableRow.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT)
            boardTableRow.gravity = Gravity.CENTER
            val cellSize: TableRow.LayoutParams = TableRow.LayoutParams(
                convertDPtoPX(40),
                convertDPtoPX(40))
            for (col in 0..<board.ROOT) {
                val cell: Cell = board.getCell(row, col)!!
                cell.cellBtn = Button(this)
                cell.drawCell()
                cell.cellBtn!!.layoutParams = cellSize
                cell.cellBtn!!.setOnClickListener {
                    if (isPlaying) {
                        if (cell.hasMine) {
                            isPlaying = false
                            alertDialog("이런!", "지뢰를 선택하셨습니다.")
                            board.openAll()
                        } else cell.open()
                    } else {
                        alertDialog(
                            "게임이 끝났습니다!",
                            "새 게임을 시작하시려면 난이도를 선택해주세요")
                    }
                }
                cell.cellBtn!!.setOnLongClickListener { changeFlagState(board, cell) }
                boardTableRow.addView(cell.cellBtn)
            }
            mainBinding.boardLayout.addView(boardTableRow)
        }
        mainBinding.remainingMineCount.text = board.remainingMineCount()
    }

    private fun changeFlagState(board: Board, cell: Cell): Boolean {
        if (!isPlaying) {
            alertDialog(
                "게임이 끝났습니다!",
                "새 게임을 시작하시려면 난이도를 선택해주세요")
            board.openAll()
            return true
        }
        if (cell.isOpen)
            return true
        val changedState: Int = cell.setFlag()
        board.updateAnswerCount(changedState)
        board.updateRemainingCount(cell.isFlag)
        mainBinding.remainingMineCount.text = board.remainingMineCount()
        if (board.checkDone()) {
            isPlaying = false
            alertDialog("축하합니다!", "모든 지뢰를 찾아내셨습니다.")
        }
        return true
    }

    inner class PlayModeSpinnerActivity: Activity(), AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            if (position > 0) play(position * 5)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) { }
    }

    private fun alertDialog(title: String, message: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인") { dialog, listener -> }
        dialogBuilder.show()
    }

    private fun convertDPtoPX(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }
}