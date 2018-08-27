package com.ttcompany.trongtuyen.carmap.services

import com.ttcompany.trongtuyen.carmap.services.models.APIError
import com.ttcompany.trongtuyen.carmap.services.APIServiceGenerator
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import retrofit2.Converter

/**
 * Created by tuyen on 07/05/2018.
 */

object ErrorUtils {

    fun parseError(response: Response<*>): APIError {
        val converter : Converter<ResponseBody, APIError> = APIServiceGenerator.retrofit()
                .responseBodyConverter(APIError::class.java, arrayOfNulls(0))

        val error: APIError

        try {
            error = converter.convert(response.errorBody())
        } catch (e: IOException) {
            return APIError()
        }

        return error
    }
}