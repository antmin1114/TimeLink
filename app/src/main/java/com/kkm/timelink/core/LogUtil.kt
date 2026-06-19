package com.kkm.timelink.core

import android.util.Log
import com.kkm.timelink.BuildConfig

object LogUtil {

    // 1. 메시지 앞에 [파일명:줄번호]를 붙여주는 헬퍼 함수
    private fun buildMessage(message: String): String {
        val stackTrace = Thread.currentThread().stackTrace
        // 호출 스택을 분석하여 이 함수를 호출한 지점을 찾음
        // 보통 [4]번 인덱스가 LogUtil.d() 등을 호출한 실제 소스 코드 지점임
        val ste = stackTrace[4]
        return "[${ste.fileName}:${ste.lineNumber}] $message"
    }

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, buildMessage(message))
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            val formattedMessage = buildMessage(message)
            if (throwable != null) {
                Log.e(tag, formattedMessage, throwable)
            } else {
                Log.e(tag, formattedMessage)
            }
        }
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, buildMessage(message))
        }
    }

    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, buildMessage(message))
        }
    }

    fun v(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, buildMessage(message))
        }
    }
}