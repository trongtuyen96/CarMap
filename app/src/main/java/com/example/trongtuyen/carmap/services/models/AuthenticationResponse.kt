package com.example.trongtuyen.carmap.services.models

import com.example.trongtuyen.carmap.models.User

/**
 * Created by tuyen 07/05/2018.
 */

class AuthenticationResponse {
    var success: Boolean? = null
    var token: String? = null
    var user: User? = null
    var newUser: Boolean? = null
}