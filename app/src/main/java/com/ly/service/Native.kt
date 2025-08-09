package com.ly.service

import com.ly.type.Response

class Native {
    companion object {
        init {
            System.loadLibrary("ly")
        }

        external fun OpenProcess(pid: Int): Response
        external fun ReadMemory(hProcess: Long, address: Long, size: Long, isForce: Boolean): Response
        external fun Exit()
    }
}