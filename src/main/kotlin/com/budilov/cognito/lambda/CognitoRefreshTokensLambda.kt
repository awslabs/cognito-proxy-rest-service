package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.CognitoService
import com.google.gson.Gson

/**
 * @author Vladimir Budilov
 */
class CognitoRefreshTokensLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {

    val cognito = CognitoService()

    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse? {
        val logger = context?.logger

        val refreshToken = request?.headers?.get("refreshToken")

        var status = 400
        var response: String = ""

        if (refreshToken != null) {
            response = Gson().toJson(cognito.adminRefreshTokens(refreshToken))
            logger?.log("Got the response from Cognito: " + response)
            status = 200
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}