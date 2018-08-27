package com.ttcompany.trongtuyen.carmap.services.models


/**
 * Created by tuyen on 07/05/2018.
 */

class APIError {

    private val success: Boolean? = null
    private val message: String? = null

    fun success(): Boolean? {
        return success
    }

    fun message(): String? {
        return message
    }
}