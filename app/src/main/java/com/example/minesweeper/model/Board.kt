package com.example.minesweeper.model

import kotlin.random.Random

class Board(percent: Int) {
    val ROOT = 9   // 정방형 판 크기
    private var arrayOfCell = Array(ROOT) { Array(ROOT) { Cell() } } // 보드 판

    private var remainingMineCount: Int = 0 // 남은 지뢰 갯수 (flag 기준)
    private var remainingAnswerCount: Int // 남은 지뢰 갯수 (정답 기준) -> 지뢰 배치 후 값 저장

    // 인자가 0 이상 ROOT 미만인지 확인
    private val isSafeIndex: (Int) -> Boolean = { idx -> idx in 0..<ROOT }

    // 전체 칸에서 지뢰가 있을 확률만큼 임의 배치하는 초기화 블록
    init {
        for (row: Int in 0..<ROOT) {
            for (col: Int in 0..<ROOT) {
                // graph 구조로 저장하기 위해 각 cell 에 이웃 cell 저장
                SearchDirection.entries.forEach { direction ->
                    val nextRow: Int = row + direction.rowDelta
                    val nextCol: Int = col + direction.colDelta
                    if (isSafeIndex(nextRow) && isSafeIndex(nextCol))
                        arrayOfCell[row][col].addNeighbor(arrayOfCell[nextRow][nextCol])
                }
                // 난이도(%) 만큼의 확률로 지뢰 배치
                if (Random.nextInt(100) <= percent) {
                    arrayOfCell[row][col].hasMine = true
                    remainingMineCount += 1 // 남은 지뢰 + 1
                    // 지뢰가 있다면 주변 칸에 지뢰 갯수 + 1
                    arrayOfCell[row][col].increaseSurroundingMines()
                }
            }
        }
        remainingAnswerCount = remainingMineCount // 남은 지뢰 갯수 초기화
    }

    // 게임 클리어 여부 확인
    fun checkDone(): Boolean {
        return (remainingAnswerCount == 0) && (remainingMineCount == 0)
    }

    fun openAll() {
        for (row in 0..<ROOT)
            for (col in 0..<ROOT)
                arrayOfCell[row][col].drawCell(true)
    }

    // 남은 지뢰 갯수 출력
    fun remainingMineCount(): String {
        return "남은 지뢰 갯수 : $remainingMineCount"
    }

    // 보드 내 특정 위치의 cell 반환
    fun getCell(row: Int, col: Int): Cell? {
        return if (isSafeIndex(row) && isSafeIndex(col)) arrayOfCell[row][col]
        else null
    }

    // flag 변동사항 반영 (남은 정답)
    fun updateAnswerCount(changedState: Int) {
        remainingAnswerCount += changedState
    }

    // flag 변동사항 반영 (남은 지뢰 수량 표시용)
    fun updateRemainingCount(isFlag: Boolean) {
        if (isFlag) remainingMineCount -= 1
        else remainingMineCount += 1
    }
}