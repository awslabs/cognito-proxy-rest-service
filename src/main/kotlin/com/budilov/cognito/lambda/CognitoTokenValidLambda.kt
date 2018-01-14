package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.CognitoService
import com.google.gson.Gson

/**
 * @author Vladimir Budilov
 */
class CognitoTokenValidLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {


    val cognito = CognitoService()

    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse? {

        val logger = context?.logger

        val idToken = request?.headers?.get("idToken")

        var status = 400
        var response: String = ""

        if (idToken != null) {
            val result = try {
                cognito.isTokenValid(idToken)
                status = 200
            } catch (e: Exception) {
                logger?.log("Exception: ${e.message}")
                false
            }

            response = Gson().toJson(result)
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}