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
        val refreshToken = request?.headers?.get("refreshToken")

        var status = 400
        var response = ""

        if (refreshToken != null) {
            response = Gson().toJson(cognito.adminRefreshTokens(refreshToken))
            status = 200
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}