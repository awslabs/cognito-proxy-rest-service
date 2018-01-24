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

        var status = 200
        var response = ""

        if (idToken != null) {
            val result = try {
                cognito.isTokenValid(idToken)
            } catch (e: Exception) {
                logger?.log("Exception: ${e.message}")
                status = 400
                false
            }

            response = Gson().toJson(result)
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}