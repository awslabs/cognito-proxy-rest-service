package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.CognitoService
import com.google.gson.Gson

/**
 * @author Vladimir Budilov
 */
class CognitoResetPasswordLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {


    val cognito = CognitoService()

    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse? {

        val username = request?.headers?.get("username")

        var status = 400
        var response = ""

        if (username != null) {
            val result = cognito.forgotPassword(username = username)
            status = 200
            response = Gson().toJson(result)
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}