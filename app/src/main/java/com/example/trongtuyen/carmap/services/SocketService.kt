package com.example.trongtuyen.carmap.services

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

class SocketService {
    // Socket
    private var socket: Socket

    constructor(){
        try {
            socket = IO.socket("https://carmap-test.herokuapp.com/")
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }
     fun getSocket() : Socket{
         return socket
     }
}