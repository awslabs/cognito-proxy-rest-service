package com.budilov.cognito.lambda

/**
 * @author Vladimir Budilov
 */
data class ApiGatewayResponse(val statusCode: Int,
                              val headers: MutableMap<String, String>? = null,
                              val body: String)